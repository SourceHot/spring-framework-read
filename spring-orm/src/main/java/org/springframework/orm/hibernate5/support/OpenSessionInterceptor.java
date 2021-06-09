/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.orm.hibernate5.support;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.lang.Nullable;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * Simple AOP Alliance {@link MethodInterceptor} implementation that binds a new
 * Hibernate {@link Session} for each method invocation, if none bound before.
 *
 * <p>This is a simple Hibernate Session scoping interceptor along the lines of
 * {@link OpenSessionInViewInterceptor}, just for use with AOP setup instead of
 * MVC setup. It opens a new {@link Session} with flush mode "MANUAL" since the
 * Session is only meant for reading, except when participating in a transaction.
 *
 * @author Juergen Hoeller
 * @since 4.2
 * @see OpenSessionInViewInterceptor
 * @see OpenSessionInViewFilter
 * @see org.springframework.orm.hibernate5.HibernateTransactionManager
 * @see TransactionSynchronizationManager
 * @see SessionFactory#getCurrentSession()
 */
public class OpenSessionInterceptor implements MethodInterceptor, InitializingBean {

	@Nullable
	private SessionFactory sessionFactory;


	/**
	 * Set the Hibernate SessionFactory that should be used to create Hibernate Sessions.
	 */
	public void setSessionFactory(@Nullable SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Return the Hibernate SessionFactory that should be used to create Hibernate Sessions.
	 */
	@Nullable
	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}

	@Override
	public void afterPropertiesSet() {
		if (getSessionFactory() == null) {
			throw new IllegalArgumentException("Property 'sessionFactory' is required");
		}
	}


	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		// 获取 session 工厂
		SessionFactory sf = getSessionFactory();
		Assert.state(sf != null, "No SessionFactory set");

		// 判断 session 工厂是否存在对应资源
		// 不存在的情况下处理
		if (!TransactionSynchronizationManager.hasResource(sf)) {
			// New Session to be bound for the current method's scope...
			// 打开session
			Session session = openSession(sf);
			try {
				// 进行资源绑定
				TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
				// 执行方法
				return invocation.proceed();
			}
			finally {
				// 关闭session
				SessionFactoryUtils.closeSession(session);
				// 解绑资源
				TransactionSynchronizationManager.unbindResource(sf);
			}
		}
		// 存在的情况下处理
		else {
			// Pre-bound Session found -> simply proceed.
			// 方法处理
			return invocation.proceed();
		}
	}

	/**
	 * Open a Session for the given SessionFactory.
	 * <p>The default implementation delegates to the {@link SessionFactory#openSession}
	 * method and sets the {@link Session}'s flush mode to "MANUAL".
	 * @param sessionFactory the SessionFactory to use
	 * @return the Session to use
	 * @throws DataAccessResourceFailureException if the Session could not be created
	 * @since 5.0
	 * @see FlushMode#MANUAL
	 */
	@SuppressWarnings("deprecation")
	protected Session openSession(SessionFactory sessionFactory) throws DataAccessResourceFailureException {
		Session session = openSession();
		if (session == null) {
			try {
				session = sessionFactory.openSession();
				session.setFlushMode(FlushMode.MANUAL);
			}
			catch (HibernateException ex) {
				throw new DataAccessResourceFailureException("Could not open Hibernate Session", ex);
			}
		}
		return session;
	}

	/**
	 * Open a Session for the given SessionFactory.
	 * @deprecated as of 5.0, in favor of {@link #openSession(SessionFactory)}
	 */
	@Deprecated
	@Nullable
	protected Session openSession() throws DataAccessResourceFailureException {
		return null;
	}

}
