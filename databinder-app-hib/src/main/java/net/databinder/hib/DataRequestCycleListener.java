/*
 * Databinder: a simple bridge from Wicket to Hibernate
 * Copyright (C) 2006  Nathan Hamblen nathan@technically.us
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/*
 * Note: this class contains code adapted from wicket-contrib-database.
 */

package net.databinder.hib;

import java.util.HashSet;

import org.apache.wicket.request.cycle.RequestCycle;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.ManagedSessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Opens Hibernate sessions and transactions as required and closes them at a request's
 * end. Uncomitted transactions are rolled back. Uses keyed Hibernate session factories from
 * Databinder service.</p>
 * @see Databinder
 * @author Nathan Hamblen
 */
public class DataRequestCycleListener implements net.databinder.models.DataRequestCycleListener {

	/** Keys for session factories that have been opened for this request */
	protected HashSet<Object> keys = new HashSet<Object>();

	private static final Logger log = LoggerFactory.getLogger(DataRequestCycleListener.class);

	/** Roll back active transactions and close session. */
	protected void closeSession(Object key) {
		Session sess = Databinder.getHibernateSession(key);

		if (sess.isOpen())
			try {
				if (sess.getTransaction().isActive()) {
					log.debug("Rolling back uncomitted transaction.");
					sess.getTransaction().rollback();
				}
			} finally {
				sess.close();
			}
	}

	/**
	 * Called by DataStaticService when a session is needed and does not already exist.
	 * Opens a new thread-bound Hibernate session.
	 */
	public void dataSessionRequested(Object key) {
		openHibernateSession(key);
	}

	/**
	 * Open a session and begin a transaction for the keyed session factory.
	 * @param key object, or null for the default factory
	 * @return newly opened session
	 */
	protected org.hibernate.classic.Session openHibernateSession(Object key) {
		org.hibernate.classic.Session sess = Databinder.getHibernateSessionFactory(key).openSession();
		sess.beginTransaction();
		ManagedSessionContext.bind(sess);
		keys.add(key);
		return sess;
	}

	/**
	 * Closes all Hibernate sessions opened for this request. If a transaction has
	 * not been committed, it will be rolled back before closing the session.
	 * @see net.databinder.components.hib.DataForm#onSubmit()
	 */
	public void onEndRequest(RequestCycle cycle) {
		for (Object key : keys) {
			SessionFactory sf = Databinder.getHibernateSessionFactory(key);
			if (ManagedSessionContext.hasBind(sf)) {
				closeSession(key);
				ManagedSessionContext.unbind(sf);
			}
		}
	}

	/**
	 * Closes and reopens sessions for this request cycle. Unrelated models may try to load
	 * themselves after this point.
	 */
	public void onException(RequestCycle cycle, Exception e) {
		onEndRequest(cycle);
		onBeginRequest(cycle);
	}

  public void onBeginRequest(RequestCycle cycle)
  {
    cycle.setMetaData(Databinder.HIBERNATE_CYCLE_LISTENER, this);
  }

  public void onDetach(RequestCycle cycle)
  {
  }

}
