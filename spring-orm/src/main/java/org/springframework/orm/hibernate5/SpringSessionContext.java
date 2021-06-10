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

package org.springframework.orm.hibernate5;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.context.spi.CurrentSessionContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;

import org.springframework.lang.Nullable;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Implementation of Hibernate 3.1's {@link CurrentSessionContext} interface
 * that delegates to Spring's {@link SessionFactoryUtils} for providing a
 * Spring-managed current {@link Session}.
 *
 * <p>This CurrentSessionContext implementation can also be specified in custom
 * SessionFactory setup through the "hibernate.current_session_context_class"
 * property, with the fully qualified name of this class as value.
 *
 * @author Juergen Hoeller
 * @since 4.2
 */
@SuppressWarnings("serial")
public class SpringSessionContext implements CurrentSessionContext {

	/**
	 * session 工厂
	 */
	private final SessionFactoryImplementor sessionFactory;

	/**
	 * 事务管理器
	 */
	@Nullable
	private TransactionManager transactionManager;

	/**
	 * session 上下文对象
	 */
	@Nullable
	private CurrentSessionContext jtaSessionContext;


	/**
	 * Create a new SpringSessionContext for the given Hibernate SessionFactory.
	 * @param sessionFactory the SessionFactory to provide current Sessions for
	 */
	public SpringSessionContext(SessionFactoryImplementor sessionFactory) {
		this.sessionFactory = sessionFactory;
		try {
			JtaPlatform jtaPlatform = sessionFactory.getServiceRegistry().getService(JtaPlatform.class);
			this.transactionManager = jtaPlatform.retrieveTransactionManager();
			if (this.transactionManager != null) {
				this.jtaSessionContext = new SpringJtaSessionContext(sessionFactory);
			}
		}
		catch (Exception ex) {
			LogFactory.getLog(SpringSessionContext.class).warn(
					"Could not introspect Hibernate JtaPlatform for SpringJtaSessionContext", ex);
		}
	}


	/**
	 * Retrieve the Spring-managed Session for the current thread, if any.
	 */
	@Override
	@SuppressWarnings("deprecation")
	public Session currentSession() throws HibernateException {
		// 获取session工厂对应的资源对象
		Object value = TransactionSynchronizationManager.getResource(this.sessionFactory);
		// 资源对象如果是 Session接口直接返回
		if (value instanceof Session) {
			return (Session) value;
		}
		// 资源对象如果是 session持有器
		else if (value instanceof SessionHolder) {
			// HibernateTransactionManager
			// 类型转换
			SessionHolder sessionHolder = (SessionHolder) value;
			// 从session持有器中获取session对象
			Session session = sessionHolder.getSession();
			// 是否是与事务同步的
			// 是否处于活跃状态
			if (!sessionHolder.isSynchronizedWithTransaction() &&
					TransactionSynchronizationManager.isSynchronizationActive()) {
				// 事务管理器进行TransactionSynchronization对象注册.
				TransactionSynchronizationManager.registerSynchronization(
						new SpringSessionSynchronization(sessionHolder, this.sessionFactory, false));
				// 设置与事务同步标记为true
				sessionHolder.setSynchronizedWithTransaction(true);
				// Switch to FlushMode.AUTO, as we have to assume a thread-bound Session
				// with FlushMode.MANUAL, which needs to allow flushing within the transaction.
				// 获取session对应的刷新方式
				FlushMode flushMode = SessionFactoryUtils.getFlushMode(session);
				if (flushMode.equals(FlushMode.MANUAL) &&
						!TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
					// 设置刷新方式
					session.setFlushMode(FlushMode.AUTO);
					sessionHolder.setPreviousFlushMode(flushMode);
				}
			}
			return session;
		}
		// 资源对象如果是EntityManagerHolder
		else if (value instanceof EntityManagerHolder) {
			// JpaTransactionManager
			return ((EntityManagerHolder) value).getEntityManager().unwrap(Session.class);
		}


		// 事务管理器不为空 并且session上下文不为空
		if (this.transactionManager != null && this.jtaSessionContext != null) {
			try {
				// 事务管理器状态是否和STATUS_ACTIVE相同
				if (this.transactionManager.getStatus() == Status.STATUS_ACTIVE) {
					// 从session上下文中获取session对象
					Session session = this.jtaSessionContext.currentSession();
					// 事务是否处于活跃状态
					if (TransactionSynchronizationManager.isSynchronizationActive()) {
						// 进行TransactionSynchronization注册
						TransactionSynchronizationManager.registerSynchronization(
								new SpringFlushSynchronization(session));
					}
					return session;
				}
			}
			catch (SystemException ex) {
				throw new HibernateException("JTA TransactionManager found but status check failed", ex);
			}
		}

		// 判断事务是否处于活跃状态
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			// 从session工厂中开启session
			Session session = this.sessionFactory.openSession();
			// 判断当前事务是否只读
			if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
				// 设置刷新方式
				session.setFlushMode(FlushMode.MANUAL);
			}
			// 创建session持有器
			SessionHolder sessionHolder = new SessionHolder(session);
			// 注册TransactionSynchronization
			TransactionSynchronizationManager.registerSynchronization(
					new SpringSessionSynchronization(sessionHolder, this.sessionFactory, true));
			// 资源绑定
			TransactionSynchronizationManager.bindResource(this.sessionFactory, sessionHolder);
			// 设置与事务同步标记为true
			sessionHolder.setSynchronizedWithTransaction(true);
			return session;
		}
		else {
			throw new HibernateException("Could not obtain transaction-synchronized Session for current thread");
		}
	}

}
