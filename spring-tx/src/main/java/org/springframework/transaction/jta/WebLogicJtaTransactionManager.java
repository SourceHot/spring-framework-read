/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.transaction.jta;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.springframework.lang.Nullable;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.util.Assert;

/**
 * Special {@link JtaTransactionManager} variant for BEA WebLogic (9.0 and higher).
 * Supports the full power of Spring's transaction definitions on WebLogic's
 * transaction coordinator, <i>beyond standard JTA</i>: transaction names,
 * per-transaction isolation levels, and proper resuming of transactions in all cases.
 *
 * <p>Uses WebLogic's special {@code begin(name)} method to start a JTA transaction,
 * in order to make <b>Spring-driven transactions visible in WebLogic's transaction
 * monitor</b>. In case of Spring's declarative transactions, the exposed name will
 * (by default) be the fully-qualified class name + "." + method name.
 *
 * <p>Supports a <b>per-transaction isolation level</b> through WebLogic's corresponding
 * JTA transaction property "ISOLATION LEVEL". This will apply the specified isolation
 * level (e.g. ISOLATION_SERIALIZABLE) to all JDBC Connections that participate in the
 * given transaction.
 *
 * <p>Invokes WebLogic's special {@code forceResume} method if standard JTA resume
 * failed, to <b>also resume if the target transaction was marked rollback-only</b>.
 * If you're not relying on this feature of transaction suspension in the first
 * place, Spring's standard JtaTransactionManager will behave properly too.
 *
 * <p>By default, the JTA UserTransaction and TransactionManager handles are
 * fetched directly from WebLogic's {@code TransactionHelper}. This can be
 * overridden by specifying "userTransaction"/"userTransactionName" and
 * "transactionManager"/"transactionManagerName", passing in existing handles
 * or specifying corresponding JNDI locations to look up.
 *
 * <p><b>NOTE: This JtaTransactionManager is intended to refine specific transaction
 * demarcation behavior on Spring's side. It will happily co-exist with independently
 * configured WebLogic transaction strategies in your persistence provider, with no
 * need to specifically connect those setups in any way.</b>
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.transaction.TransactionDefinition#getName
 * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel
 * @see weblogic.transaction.UserTransaction#begin(String)
 * @see weblogic.transaction.Transaction#setProperty
 * @see weblogic.transaction.TransactionManager#forceResume
 * @see weblogic.transaction.TransactionHelper
 */
@SuppressWarnings("serial")
public class WebLogicJtaTransactionManager extends JtaTransactionManager {
	/**
	 * UserTransaction 类名
	 */
	private static final String USER_TRANSACTION_CLASS_NAME = "weblogic.transaction.UserTransaction";

	/**
	 * 事务管理器名称
	 */
	private static final String CLIENT_TRANSACTION_MANAGER_CLASS_NAME = "weblogic.transaction.ClientTransactionManager";

	/**
	 * 事务类名
	 */
	private static final String TRANSACTION_CLASS_NAME = "weblogic.transaction.Transaction";

	/**
	 * 事务帮助类的类名
	 */
	private static final String TRANSACTION_HELPER_CLASS_NAME = "weblogic.transaction.TransactionHelper";

	/**
	 * 事务隔离级别的键
	 */
	private static final String ISOLATION_LEVEL_KEY = "ISOLATION LEVEL";

	/**
	 *weblogic 事务是否可以使用
	 */
	private boolean weblogicUserTransactionAvailable;

	/**
	 * 开始事务的方法,根据名称
	 */
	@Nullable
	private Method beginWithNameMethod;

	/**
	 * 开始事务的方法,根据名称和超时时间
	 */
	@Nullable
	private Method beginWithNameAndTimeoutMethod;

	/**
	 *weblogic 事务管理器是否可用
	 */
	private boolean weblogicTransactionManagerAvailable;

	/**
	 *强制恢复的方法
	 */
	@Nullable
	private Method forceResumeMethod;

	/**
	 * 设置属性的方法
	 */
	@Nullable
	private Method setPropertyMethod;

	/**
	 * 事务帮助类
	 */
	@Nullable
	private Object transactionHelper;


	@Override
	public void afterPropertiesSet() throws TransactionSystemException {
		super.afterPropertiesSet();
		loadWebLogicTransactionClasses();
	}

	@Override
	@Nullable
	protected UserTransaction retrieveUserTransaction() throws TransactionSystemException {
		Object helper = loadWebLogicTransactionHelper();
		try {
			logger.trace("Retrieving JTA UserTransaction from WebLogic TransactionHelper");
			Method getUserTransactionMethod = helper.getClass().getMethod("getUserTransaction");
			return (UserTransaction) getUserTransactionMethod.invoke(this.transactionHelper);
		}
		catch (InvocationTargetException ex) {
			throw new TransactionSystemException(
					"WebLogic's TransactionHelper.getUserTransaction() method failed", ex.getTargetException());
		}
		catch (Exception ex) {
			throw new TransactionSystemException(
					"Could not invoke WebLogic's TransactionHelper.getUserTransaction() method", ex);
		}
	}

	@Override
	@Nullable
	protected TransactionManager retrieveTransactionManager() throws TransactionSystemException {
		Object helper = loadWebLogicTransactionHelper();
		try {
			logger.trace("Retrieving JTA TransactionManager from WebLogic TransactionHelper");
			Method getTransactionManagerMethod = helper.getClass().getMethod("getTransactionManager");
			return (TransactionManager) getTransactionManagerMethod.invoke(this.transactionHelper);
		}
		catch (InvocationTargetException ex) {
			throw new TransactionSystemException(
					"WebLogic's TransactionHelper.getTransactionManager() method failed", ex.getTargetException());
		}
		catch (Exception ex) {
			throw new TransactionSystemException(
					"Could not invoke WebLogic's TransactionHelper.getTransactionManager() method", ex);
		}
	}

	/**
	 * 初始化 weblogic.transaction.TransactionHelper
	 */
	private Object loadWebLogicTransactionHelper() throws TransactionSystemException {
		// 获取成员变量中的事务帮助对象
		Object helper = this.transactionHelper;
		// 事务帮助对象为空的情况下在进行搜搜操作
		if (helper == null) {
			try {
				// 加载 weblogic.transaction.TransactionHelper 类
				Class<?> transactionHelperClass = getClass().getClassLoader().loadClass(TRANSACTION_HELPER_CLASS_NAME);
				// 获取getTransactionHelper方法
				Method getTransactionHelperMethod = transactionHelperClass.getMethod("getTransactionHelper");
				// 通过 getTransactionHelper 方法调用后获取对象
				helper = getTransactionHelperMethod.invoke(null);
				this.transactionHelper = helper;
				logger.trace("WebLogic TransactionHelper found");
			}
			catch (InvocationTargetException ex) {
				throw new TransactionSystemException(
						"WebLogic's TransactionHelper.getTransactionHelper() method failed", ex.getTargetException());
			}
			catch (Exception ex) {
				throw new TransactionSystemException(
						"Could not initialize WebLogicJtaTransactionManager because WebLogic API classes are not available",
						ex);
			}
		}
		return helper;
	}

	private void loadWebLogicTransactionClasses() throws TransactionSystemException {
		try {
			// 加载weblogic.transaction.UserTransaction
			Class<?> userTransactionClass = getClass().getClassLoader().loadClass(USER_TRANSACTION_CLASS_NAME);
			this.weblogicUserTransactionAvailable = userTransactionClass.isInstance(getUserTransaction());
			// 是接口的情况下进行begin的两个方法获取
			if (this.weblogicUserTransactionAvailable) {
				// 根据名称搜索的方法对象
				this.beginWithNameMethod = userTransactionClass.getMethod("begin", String.class);
				// 根据名称和超时时间搜索的对象
				this.beginWithNameAndTimeoutMethod = userTransactionClass.getMethod("begin", String.class, int.class);
				logger.debug("Support for WebLogic transaction names available");
			}
			else {
				logger.debug("Support for WebLogic transaction names not available");
			}

			// Obtain WebLogic ClientTransactionManager interface.
			// 加载ClientTransactionManager类
			Class<?> transactionManagerClass =
					getClass().getClassLoader().loadClass(CLIENT_TRANSACTION_MANAGER_CLASS_NAME);
			logger.trace("WebLogic ClientTransactionManager found");

			this.weblogicTransactionManagerAvailable = transactionManagerClass.isInstance(getTransactionManager());
			if (this.weblogicTransactionManagerAvailable) {
				Class<?> transactionClass = getClass().getClassLoader().loadClass(TRANSACTION_CLASS_NAME);
				// 强制恢复的方法
				this.forceResumeMethod = transactionManagerClass.getMethod("forceResume", Transaction.class);
				// 获取设置属性的方法
				this.setPropertyMethod = transactionClass.getMethod("setProperty", String.class, Serializable.class);
				logger.debug("Support for WebLogic forceResume available");
			}
			else {
				logger.debug("Support for WebLogic forceResume not available");
			}
		}
		catch (Exception ex) {
			throw new TransactionSystemException(
					"Could not initialize WebLogicJtaTransactionManager because WebLogic API classes are not available",
					ex);
		}
	}

	private TransactionManager obtainTransactionManager() {
		TransactionManager tm = getTransactionManager();
		Assert.state(tm != null, "No TransactionManager set");
		return tm;
	}


	@Override
	protected void doJtaBegin(JtaTransactionObject txObject, TransactionDefinition definition)
			throws NotSupportedException, SystemException {

		// 确认超时时间
		int timeout = determineTimeout(definition);

		// Apply transaction name (if any) to WebLogic transaction.
		// weblogic事务是否可以使用 并且名称不为空
		if (this.weblogicUserTransactionAvailable && definition.getName() != null) {
			try {
				// 超时时间大于默认时间的处理
				if (timeout > TransactionDefinition.TIMEOUT_DEFAULT) {
					/*
					weblogic.transaction.UserTransaction wut = (weblogic.transaction.UserTransaction) ut;
					wut.begin(definition.getName(), timeout);
					*/
					Assert.state(this.beginWithNameAndTimeoutMethod != null, "WebLogic JTA API not initialized");
					this.beginWithNameAndTimeoutMethod.invoke(txObject.getUserTransaction(), definition.getName(), timeout);
				}
				// 超时时间小于等于默认时间的处理
				else {
					/*
					weblogic.transaction.UserTransaction wut = (weblogic.transaction.UserTransaction) ut;
					wut.begin(definition.getName());
					*/
					Assert.state(this.beginWithNameMethod != null, "WebLogic JTA API not initialized");
					this.beginWithNameMethod.invoke(txObject.getUserTransaction(), definition.getName());
				}
			}
			catch (InvocationTargetException ex) {
				throw new TransactionSystemException(
						"WebLogic's UserTransaction.begin() method failed", ex.getTargetException());
			}
			catch (Exception ex) {
				throw new TransactionSystemException(
						"Could not invoke WebLogic's UserTransaction.begin() method", ex);
			}
		}
		else {
			// No WebLogic UserTransaction available or no transaction name specified
			// -> standard JTA begin call.
			// 应用超时时间
			applyTimeout(txObject, timeout);
			// 开始事务
			txObject.getUserTransaction().begin();
		}

		// Specify isolation level, if any, through corresponding WebLogic transaction property.
		// 判断 weblogic事务管理器是否可用
		if (this.weblogicTransactionManagerAvailable) {
			// 事务隔离级别不等于默认事务隔离级别
			if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
				try {
					// 获取事务对象
					Transaction tx = obtainTransactionManager().getTransaction();
					// 从事务定义对象中获取事务隔离级别
					Integer isolationLevel = definition.getIsolationLevel();
					/*
					weblogic.transaction.Transaction wtx = (weblogic.transaction.Transaction) tx;
					wtx.setProperty(ISOLATION_LEVEL_KEY, isolationLevel);
					*/
					Assert.state(this.setPropertyMethod != null, "WebLogic JTA API not initialized");
					// 通过属性设置方法进行事务隔离级别设置
					this.setPropertyMethod.invoke(tx, ISOLATION_LEVEL_KEY, isolationLevel);
				}
				catch (InvocationTargetException ex) {
					throw new TransactionSystemException(
							"WebLogic's Transaction.setProperty(String, Serializable) method failed", ex.getTargetException());
				}
				catch (Exception ex) {
					throw new TransactionSystemException(
							"Could not invoke WebLogic's Transaction.setProperty(String, Serializable) method", ex);
				}
			}
		}
		else {
			// 应用事务隔离级别
			applyIsolationLevel(txObject, definition.getIsolationLevel());
		}
	}

	@Override
	protected void doJtaResume(@Nullable JtaTransactionObject txObject, Object suspendedTransaction)
			throws InvalidTransactionException, SystemException {

		try {
			// 获取事务管理器, 通过事务管理器进行恢复操作
			obtainTransactionManager().resume((Transaction) suspendedTransaction);
		}
		catch (InvalidTransactionException ex) {
			// 判断 weblogic事务管理器是否可用
			if (!this.weblogicTransactionManagerAvailable) {
				throw ex;
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Standard JTA resume threw InvalidTransactionException: " + ex.getMessage() +
						" - trying WebLogic JTA forceResume");
			}
			/*
			weblogic.transaction.TransactionManager wtm =
					(weblogic.transaction.TransactionManager) getTransactionManager();
			wtm.forceResume(suspendedTransaction);
			*/
			try {
				Assert.state(this.forceResumeMethod != null, "WebLogic JTA API not initialized");
				// 强制执行
				this.forceResumeMethod.invoke(getTransactionManager(), suspendedTransaction);
			}
			catch (InvocationTargetException ex2) {
				throw new TransactionSystemException(
						"WebLogic's TransactionManager.forceResume(Transaction) method failed", ex2.getTargetException());
			}
			catch (Exception ex2) {
				throw new TransactionSystemException(
						"Could not access WebLogic's TransactionManager.forceResume(Transaction) method", ex2);
			}
		}
	}

	@Override
	public Transaction createTransaction(@Nullable String name, int timeout) throws NotSupportedException, SystemException {
		// weblogic事务是否可以使用 名称是否为空
		if (this.weblogicUserTransactionAvailable && name != null) {
			try {
				if (timeout >= 0) {
					Assert.state(this.beginWithNameAndTimeoutMethod != null, "WebLogic JTA API not initialized");
					this.beginWithNameAndTimeoutMethod.invoke(getUserTransaction(), name, timeout);
				}
				else {
					Assert.state(this.beginWithNameMethod != null, "WebLogic JTA API not initialized");
					this.beginWithNameMethod.invoke(getUserTransaction(), name);
				}
			}
			catch (InvocationTargetException ex) {
				if (ex.getTargetException() instanceof NotSupportedException) {
					throw (NotSupportedException) ex.getTargetException();
				}
				else if (ex.getTargetException() instanceof SystemException) {
					throw (SystemException) ex.getTargetException();
				}
				else if (ex.getTargetException() instanceof RuntimeException) {
					throw (RuntimeException) ex.getTargetException();
				}
				else {
					throw new SystemException(
							"WebLogic's begin() method failed with an unexpected error: " + ex.getTargetException());
				}
			}
			catch (Exception ex) {
				throw new SystemException("Could not invoke WebLogic's UserTransaction.begin() method: " + ex);
			}
			return new ManagedTransactionAdapter(obtainTransactionManager());
		}

		else {
			// No name specified - standard JTA is sufficient.
			return super.createTransaction(name, timeout);
		}
	}

}
