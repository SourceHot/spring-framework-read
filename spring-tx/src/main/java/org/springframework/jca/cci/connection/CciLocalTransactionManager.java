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

package org.springframework.jca.cci.connection;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.spi.LocalTransactionException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.transaction.PlatformTransactionManager} implementation
 * that manages local transactions for a single CCI ConnectionFactory.
 * Binds a CCI Connection from the specified ConnectionFactory to the thread,
 * potentially allowing for one thread-bound Connection per ConnectionFactory.
 *
 * <p>Application code is required to retrieve the CCI Connection via
 * {@link ConnectionFactoryUtils#getConnection(ConnectionFactory)} instead of a standard
 * Java EE-style {@link ConnectionFactory#getConnection()} call. Spring classes such as
 * {@link org.springframework.jca.cci.core.CciTemplate} use this strategy implicitly.
 * If not used in combination with this transaction manager, the
 * {@link ConnectionFactoryUtils} lookup strategy behaves exactly like the native
 * DataSource lookup; it can thus be used in a portable fashion.
 *
 * <p>Alternatively, you can allow application code to work with the standard
 * Java EE lookup pattern {@link ConnectionFactory#getConnection()}, for example
 * for legacy code that is not aware of Spring at all. In that case, define a
 * {@link TransactionAwareConnectionFactoryProxy} for your target ConnectionFactory,
 * which will automatically participate in Spring-managed transactions.
 *
 * @author Thierry Templier
 * @author Juergen Hoeller
 * @since 1.2
 * @see ConnectionFactoryUtils#getConnection(javax.resource.cci.ConnectionFactory)
 * @see ConnectionFactoryUtils#releaseConnection
 * @see TransactionAwareConnectionFactoryProxy
 * @see org.springframework.jca.cci.core.CciTemplate
 */
@SuppressWarnings("serial")
public class CciLocalTransactionManager extends AbstractPlatformTransactionManager
		implements ResourceTransactionManager, InitializingBean {
	/**
	 * 链接工厂
	 */
	@Nullable
	private ConnectionFactory connectionFactory;


	/**
	 * Create a new CciLocalTransactionManager instance.
	 * A ConnectionFactory has to be set to be able to use it.
	 * @see #setConnectionFactory
	 */
	public CciLocalTransactionManager() {
	}

	/**
	 * Create a new CciLocalTransactionManager instance.
	 * @param connectionFactory the CCI ConnectionFactory to manage local transactions for
	 */
	public CciLocalTransactionManager(ConnectionFactory connectionFactory) {
		setConnectionFactory(connectionFactory);
		afterPropertiesSet();
	}


	/**
	 * Set the CCI ConnectionFactory that this instance should manage local
	 * transactions for.
	 */
	public void setConnectionFactory(@Nullable ConnectionFactory cf) {
		if (cf instanceof TransactionAwareConnectionFactoryProxy) {
			// If we got a TransactionAwareConnectionFactoryProxy, we need to perform transactions
			// for its underlying target ConnectionFactory, else JMS access code won't see
			// properly exposed transactions (i.e. transactions for the target ConnectionFactory).
			this.connectionFactory = ((TransactionAwareConnectionFactoryProxy) cf).getTargetConnectionFactory();
		}
		else {
			this.connectionFactory = cf;
		}
	}

	/**
	 * Return the CCI ConnectionFactory that this instance manages local
	 * transactions for.
	 */
	@Nullable
	public ConnectionFactory getConnectionFactory() {
		return this.connectionFactory;
	}

	private ConnectionFactory obtainConnectionFactory() {
		ConnectionFactory connectionFactory = getConnectionFactory();
		Assert.state(connectionFactory != null, "No ConnectionFactory set");
		return connectionFactory;
	}

	@Override
	public void afterPropertiesSet() {
		if (getConnectionFactory() == null) {
			throw new IllegalArgumentException("Property 'connectionFactory' is required");
		}
	}


	@Override
	public Object getResourceFactory() {
		return obtainConnectionFactory();
	}

	@Override
	protected Object doGetTransaction() {
		// 创建 CCI 的本地事务对象
		CciLocalTransactionObject txObject = new CciLocalTransactionObject();
		// 获取链接持有对象
		ConnectionHolder conHolder =
				(ConnectionHolder) TransactionSynchronizationManager.getResource(obtainConnectionFactory());
		// 设置链接持有对象
		txObject.setConnectionHolder(conHolder);
		// 返回
		return txObject;
	}

	@Override
	protected boolean isExistingTransaction(Object transaction) {
		CciLocalTransactionObject txObject = (CciLocalTransactionObject) transaction;
		// Consider a pre-bound connection as transaction.
		return txObject.hasConnectionHolder();
	}

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		// 事务对象类型转换
		CciLocalTransactionObject txObject = (CciLocalTransactionObject) transaction;
		// 获取链接工厂
		ConnectionFactory connectionFactory = obtainConnectionFactory();
		// 链接对象
		Connection con = null;

		try {
			// 通过链接工厂创建链接
			con = connectionFactory.getConnection();
			if (logger.isDebugEnabled()) {
				logger.debug("Acquired Connection [" + con + "] for local CCI transaction");
			}

			// 创建链接持有器
			ConnectionHolder connectionHolder = new ConnectionHolder(con);
			// 设置事务同步标记为true
			connectionHolder.setSynchronizedWithTransaction(true);
			// 通过链接对象获取本地事务并开始事务
			con.getLocalTransaction().begin();
			// 获取超时时间
			int timeout = determineTimeout(definition);
			if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
				connectionHolder.setTimeoutInSeconds(timeout);
			}

			// 事务对象设置链接持有者
			txObject.setConnectionHolder(connectionHolder);
			// 进行链接工厂和链接持有器的绑定
			TransactionSynchronizationManager.bindResource(connectionFactory, connectionHolder);
		}
		catch (NotSupportedException ex) {
			// 释放链接
			ConnectionFactoryUtils.releaseConnection(con, connectionFactory);
			throw new CannotCreateTransactionException("CCI Connection does not support local transactions", ex);
		}
		catch (LocalTransactionException ex) {
			ConnectionFactoryUtils.releaseConnection(con, connectionFactory);
			throw new CannotCreateTransactionException("Could not begin local CCI transaction", ex);
		}
		catch (Throwable ex) {
			ConnectionFactoryUtils.releaseConnection(con, connectionFactory);
			throw new TransactionSystemException("Unexpected failure on begin of CCI local transaction", ex);
		}
	}

	@Override
	protected Object doSuspend(Object transaction) {
		// 将参数事务对象进行类型转换，转换成CciLocalTransactionObject类型。
		CciLocalTransactionObject txObject = (CciLocalTransactionObject) transaction;
		// 将链接持有器设置为null
		txObject.setConnectionHolder(null);
		// 解绑资源
		return TransactionSynchronizationManager.unbindResource(obtainConnectionFactory());
	}

	@Override
	protected void doResume(@Nullable Object transaction, Object suspendedResources) {
		// 资源对象转换成链接持有器
		ConnectionHolder conHolder = (ConnectionHolder) suspendedResources;
		// 链接工厂与链接持有器进行绑定
		TransactionSynchronizationManager.bindResource(obtainConnectionFactory(), conHolder);
	}

	protected boolean isRollbackOnly(Object transaction) throws TransactionException {
		CciLocalTransactionObject txObject = (CciLocalTransactionObject) transaction;
		return txObject.getConnectionHolder().isRollbackOnly();
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) {
		// 从事务状态对象中获取事务对象并强制转换为CciLocalTransactionObject类型
		CciLocalTransactionObject txObject = (CciLocalTransactionObject) status.getTransaction();
		// 通过事务对象获取链接持有器再获取链接对象
		Connection con = txObject.getConnectionHolder().getConnection();
		if (status.isDebug()) {
			logger.debug("Committing CCI local transaction on Connection [" + con + "]");
		}
		try {
			// 通过链接对象获取本地事务进行事务提交
			con.getLocalTransaction().commit();
		}
		catch (LocalTransactionException ex) {
			throw new TransactionSystemException("Could not commit CCI local transaction", ex);
		}
		catch (ResourceException ex) {
			throw new TransactionSystemException("Unexpected failure on commit of CCI local transaction", ex);
		}
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) {
		// 从事务状态对象中获取事务对象并强制转换为CciLocalTransactionObject类型
		CciLocalTransactionObject txObject = (CciLocalTransactionObject) status.getTransaction();
		// 通过事务对象获取链接持有器再获取链接对象
		Connection con = txObject.getConnectionHolder().getConnection();
		if (status.isDebug()) {
			logger.debug("Rolling back CCI local transaction on Connection [" + con + "]");
		}
		try {
			// 通过链接对象获取本地事务进行事务回滚操作
			con.getLocalTransaction().rollback();
		}
		catch (LocalTransactionException ex) {
			throw new TransactionSystemException("Could not roll back CCI local transaction", ex);
		}
		catch (ResourceException ex) {
			throw new TransactionSystemException("Unexpected failure on rollback of CCI local transaction", ex);
		}
	}

	@Override
	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		// 从事务状态对象中获取事务对象
		CciLocalTransactionObject txObject = (CciLocalTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Setting CCI local transaction [" + txObject.getConnectionHolder().getConnection() +
					"] rollback-only");
		}
		// 通过事务对象获取链接持有器,通过链接持有器进行仅回滚标记的设置
		txObject.getConnectionHolder().setRollbackOnly();
	}

	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		// 将参数事务对象进行类型转换，转换成CciLocalTransactionObject类型。
		CciLocalTransactionObject txObject = (CciLocalTransactionObject) transaction;
		// 获取链接工厂
		ConnectionFactory connectionFactory = obtainConnectionFactory();

		// Remove the connection holder from the thread.
		// 解绑链接工厂对应的资源
		TransactionSynchronizationManager.unbindResource(connectionFactory);
		// 获取事务对象中的链接持有器进行清除方法的调用
		txObject.getConnectionHolder().clear();

		// 从事务对象中获取链接持有器再获取链接对象
		Connection con = txObject.getConnectionHolder().getConnection();
		if (logger.isDebugEnabled()) {
			logger.debug("Releasing CCI Connection [" + con + "] after transaction");
		}
		// 链接对象和链接工厂的关系解绑
		ConnectionFactoryUtils.releaseConnection(con, connectionFactory);
	}


	/**
	 * CCI local transaction object, representing a ConnectionHolder.
	 * Used as transaction object by CciLocalTransactionManager.
	 * @see ConnectionHolder
	 */
	private static class CciLocalTransactionObject {

		@Nullable
		private ConnectionHolder connectionHolder;

		public void setConnectionHolder(@Nullable ConnectionHolder connectionHolder) {
			this.connectionHolder = connectionHolder;
		}

		public ConnectionHolder getConnectionHolder() {
			Assert.state(this.connectionHolder != null, "No ConnectionHolder available");
			return this.connectionHolder;
		}

		public boolean hasConnectionHolder() {
			return (this.connectionHolder != null);
		}
	}

}
