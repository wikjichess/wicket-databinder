/*
 * Databinder: a simple bridge from Wicket to Hibernate
 * Copyright (C) 2006	Nathan Hamblen nathan@technically.us
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA	02110-1301	USA
 */

package net.databinder.hib;

import java.util.HashMap;

import net.databinder.DataApplicationBase;
import net.databinder.components.hib.DataBrowser;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

/**
 * Optional Databinder base Application class for configuration and session management.
 * Supports multiple session factories with key objects.
 * @author Nathan Hamblen
 */
public abstract class DataApplication extends DataApplicationBase implements HibernateApplication {

	/** App-wide session factories */
	private HashMap<Object, SessionFactory> hibernateSessionFactories = new HashMap<Object, SessionFactory>();


	/**
	 * Initializes a default Hibernate session factory and mounts a page for
	 * the data browser. This is called automatically during start-up. Applications
	 * with one session factory will not normally need to override this method;
	 * see related methods to override specific tasks.
	 * @see #buildHibernateSessionFactory(Object) aoe
	 * @see #mountDataBrowser()
	 */
	@Override
  protected void dataInit() {
		buildHibernateSessionFactory(null);
		if (isDataBrowserAllowed())
			mountDataBrowser();
		getRequestCycleListeners().add(new DataRequestCycleListener());
	}

  /**
	 * Bookmarkable subclass of DataBrowser page. Access to the page is permitted
	 * only if the current application is assignable to DataApplication
	 * and returns true for isDataBrowserAllowed().
	 * @see DataBrowser
	 */
	public static class BmarkDataBrowser extends DataBrowser<Void> {
		public BmarkDataBrowser() {
			super(((DataApplication)Application.get()).isDataBrowserAllowed());
		}
	}

	/**
	 * Mounts Data Diver to /dbrowse. Override to mount elsewhere, or not mount at all.
	 * This method is only called if isDataBrowserAllowed() returns true in init().
	 */
	protected void mountDataBrowser() {
		mountPage("/dbrowse", BmarkDataBrowser.class);
	}

	/**
	 * Called by init to create Hibernate session factory and load a configuration. Passes
	 * an empty new AnnotationConfiguration to buildHibernateSessionFactory(key, config) by
	 * default. Override if creating a configuration externally.
	 * @param key session factory key; the default key is null
	 */
	public void buildHibernateSessionFactory(Object key) {
		buildHibernateSessionFactory(key, new AnnotationConfiguration());
	}

	/**
	 * Builds and	a session factory with the given configuration. Passes config
	 * through configureHibernate methods.
	 * @param key session factory key; the default key is null
	 * @param config annotation conifuration
	 * @see #configureHibernateEssentials(AnnotationConfiguration)
	 * @see #configureHibernate(AnnotationConfiguration, Object)
	 */
	final public void buildHibernateSessionFactory(Object key, AnnotationConfiguration config) {
		configureHibernateEssentials(config);
		configureHibernate(config, key);
		setHibernateSessionFactory(key, config.buildSessionFactory());
	}

	/**
	 * Configures the session factory associated with the key. The default implementation
	 * calls the configureHibernate(config) method and ignores the key.
	 * For applications with multiple session factories, override this method to
	 * perform key-specific configuration here instead.
	 * @param config configuration to update
	 * @param key object, or null for the default factory
	 */
	protected	void configureHibernate(AnnotationConfiguration config, Object key) {
		configureHibernate(config);
	}

	/**
	 * For Hibernate settings that should not normally be overriden by client
	 * applications. Specifically, this method sets Hibernate for a ManagedSessionContext,
	 * the session lookup method used by DataRequestCycle.
	 * @param config Hibernate configuration
	 */
	protected void configureHibernateEssentials(AnnotationConfiguration config) {
		config.setProperty("hibernate.current_session_context_class","managed");
	}

	/**
	 * Configures the default session factory; override to add annotated classes
	 * but don't forget to call this super-implementation if you want its defaults.
	 * When running in development the session factory is set for
	 * hbm2ddl auto-updating to create and add columns to tables
	 * as required. For deployment it is configured for C3P0 connection pooling.
	 * @param config used to build Hibernate session factory
	 */
	protected	void configureHibernate(AnnotationConfiguration config) {
			if (isDevelopment())
				config.setProperty("hibernate.hbm2ddl.auto", "update");
			else {
				config
					.setProperty("hibernate.connection.provider_class", "org.hibernate.connection.C3P0ConnectionProvider")
					.setProperty("hibernate.c3p0.max_size", "20")
					.setProperty("hibernate.c3p0.timeout","3000")
					.setProperty("hibernate.c3p0.idle_test_period", "300");
			}
	}

	/**
	 * @param key object, or null for the default factory
	 * @return the retained session factory
	 */
	public SessionFactory getHibernateSessionFactory(Object key) {
		SessionFactory sf = hibernateSessionFactories.get(key);
		if (sf == null)
			if (key == null)
				throw new WicketRuntimeException("The default Hibernate session factory has not been " +
						"initialized. This is normally done in DataApplication.init().");
			else
				throw new WicketRuntimeException("Session factory not found for key: " + key);
		return sf;
	}

	/**
	 * @param key object, or null for the default factory
	 * @param sf session factory to retain
	 */
	protected void setHibernateSessionFactory(Object key, SessionFactory sf) {
		hibernateSessionFactories.put(key, sf);
	}

	/**
	 * Returns true if development mode is enabled. Override for other behavior.
	 * @return true if the Data Browser page should be enabled
	 */
	protected boolean isDataBrowserAllowed() {
		return isDevelopment();
	}
}
