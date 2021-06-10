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

import javax.persistence.EntityManager;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.springframework.lang.Nullable;
import org.springframework.orm.jpa.EntityManagerHolder;

/**
 * Resource holder wrapping a Hibernate {@link Session} (plus an optional {@link Transaction}).
 * {@link HibernateTransactionManager} binds instances of this class to the thread,
 * for a given {@link org.hibernate.SessionFactory}. Extends {@link EntityManagerHolder}
 * as of 5.1, automatically exposing an {@code EntityManager} handle on Hibernate 5.2+.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 4.2
 * @see HibernateTransactionManager
 * @see SessionFactoryUtils
 */
public class SessionHolder extends EntityManagerHolder {
	/**
	 * session 对象
	 */
	private final Session session;

	/**
	 * 事务对象
	 */
	@Nullable
	private Transaction transaction;

	/**
	 * 刷新方式
	 */
	@Nullable
	private FlushMode previousFlushMode;


	public SessionHolder(Session session) {
		// Check below is always true against Hibernate >= 5.2 but not against 5.0/5.1 at runtime
		super(session instanceof EntityManager ? session : null);
		this.session = session;
	}


	public Session getSession() {
		return this.session;
	}

	@Nullable
	public Transaction getTransaction() {
		return this.transaction;
	}

	public void setTransaction(@Nullable Transaction transaction) {
		this.transaction = transaction;
		setTransactionActive(transaction != null);
	}

	@Nullable
	public FlushMode getPreviousFlushMode() {
		return this.previousFlushMode;
	}

	public void setPreviousFlushMode(@Nullable FlushMode previousFlushMode) {
		this.previousFlushMode = previousFlushMode;
	}

	@Override
	public void clear() {
		super.clear();
		this.transaction = null;
		this.previousFlushMode = null;
	}

}
