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

package net.databinder.hib.conv;

import net.databinder.hib.DataRequestCycleListener;
import net.databinder.hib.Databinder;
import net.databinder.hib.conv.components.IConversationPage;

import org.apache.wicket.Page;
import org.apache.wicket.request.cycle.RequestCycle;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.classic.Session;
import org.hibernate.context.ManagedSessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supports extended Hibernate sessions for long conversations. This is useful for a page or
 * a series of pages where changes are made to an entity that can not be immediately
 * committed. Using a "conversation" session, HibernateObjectModels are used normally, but
 * until the session is flushed the changes are not made to persistent storage.
 * @author Nathan Hamblen
 */
public class DataConversationRequestCycleListener extends DataRequestCycleListener {
	private static final Logger log = LoggerFactory.getLogger(DataConversationRequestCycleListener.class);

	/**
	 * Called by DataStaticService when a session is needed and does not already exist.
	 * Determines current page and retrieves its associated conversation session if
	 * appropriate. Does nothing if current page is not yet available.
	 * @param key factory key object, or null for the default factory
	 */
	@Override
  public void dataSessionRequested(Object key) {
	  RequestCycle cycle = RequestCycle.get();
		Page page = cycle.getResponsePage();
		if (page == null)
			page = cycle.getRequest().getPage();

		if (page == null) {
			Class<?> pageClass = cycle.getResponsePageClass();
			if (pageClass != null) {
				openHibernateSession(key);
				// set to manual if we are going to a conv. page
				if (IConversationPage.class.isAssignableFrom(pageClass))
					Databinder.getHibernateSession(key).setFlushMode(FlushMode.MANUAL);
			}
			return;
		}

		// if continuing a conversation page
		if (page instanceof  IConversationPage) {
			// look for existing session
			IConversationPage convPage = (IConversationPage) page;
			org.hibernate.classic.Session sess = convPage.getConversationSession(key);

			// if usable session exists, try to open txn, bind, and return
			if (sess != null && sess.isOpen()) {
				try {
					sess.beginTransaction();
					ManagedSessionContext.bind(sess);
					keys.add(key);
					return;
				} catch (HibernateException e) {
					log.warn("Existing session exception on beginTransation, opening new", e);
				}
			}
			// else start new one and set in page
			sess = openHibernateSession(key);
			sess.setFlushMode(FlushMode.MANUAL);
			((IConversationPage)page).setConversationSession(key, sess);
			return;
		}
		// start new standard session
		openHibernateSession(key);
	}

	/**
	 * Inspects responding page to determine if current Hibernate session should be closed
	 * or left open and stored in the page.
	 */
	@Override
  public void onEndRequest(RequestCycle cycle) {
		for (Object key : keys) {
			if (!ManagedSessionContext.hasBind(Databinder.getHibernateSessionFactory(key)))
				return;
			org.hibernate.classic.Session sess = Databinder.getHibernateSession(key);
			boolean transactionComitted = false;
			if (sess.getTransaction().isActive())
				sess.getTransaction().rollback();
			else
				transactionComitted = true;

			Page page = cycle.getResponsePage() ;

			if (page != null) {
				// check for current conversational session
				if (page instanceof IConversationPage) {
					IConversationPage convPage = (IConversationPage)page;
					// close if not dirty contains no changes
					if (transactionComitted && !sess.isDirty()) {
						sess.close();
						sess = null;
					}
					convPage.setConversationSession(key, sess);
				} else
					sess.close();
			}
			ManagedSessionContext.unbind(Databinder.getHibernateSessionFactory(key));
		}
	}

	/**
	 * Closes and reopens Hibernate session for this Web session. Unrelated models may try to load
	 * themselves after this point.
	 */
	@Override
  public void onException(RequestCycle cycle, Exception e) {
		for (Object key : keys) {
			if (Databinder.hasBoundSession(key)) {
				Session sess = Databinder.getHibernateSession(key);
				try {
					if (sess.getTransaction().isActive())
						sess.getTransaction().rollback();
				} finally {
					sess.close();
					ManagedSessionContext.unbind(Databinder.getHibernateSessionFactory(key));
				}
			}
			openHibernateSession(key);
		}
	}

}
