/*
 * Databinder: a simple bridge from Wicket to Hibernate
 * Copyright (C) 2006  Nathan Hamblen nathan@technically.us

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

package net.databinder.models.hib;

import java.util.List;

import net.databinder.hib.Databinder;

import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * Model for a List generated by a Hibernate query. This read-only model can be used
 * to fill ListModel and PropertyListModel components with rows from a database.
 * @author Nathan Hamblen
 */
public class HibernateListModel<T> extends LoadableDetachableModel<List<T>> {
  private static final long serialVersionUID = 1L;

  private QueryBuilder queryBuilder;
	private Class<T> objectClass;
	private CriteriaBuilder criteriaBuilder;

	private Object factoryKey;

	/**
	 * Contructor for a simple query.
	 * @param queryString query with no parameters
	 */
	public HibernateListModel(String queryString) {
		this(new QueryBinderBuilder(queryString));
	}

	/**
	 * Contructor for a simple query.
	 * @param queryString query with no parameters
	 * @param cacheable sets query to cacheable if true
	 */
	public HibernateListModel(String queryString, final boolean cacheable) {
		this(queryString, new QueryBinder() {
      private static final long serialVersionUID = HibernateListModel.serialVersionUID;

      public void bind(Query query) {
				query.setCacheable(cacheable);
			}
		});
	}

	/**
	 * Constructor for a parameterized query.
	 * @param queryString Query with parameters
	 * @param queryBinder object that binds the query parameters
	 */
	public HibernateListModel(String queryString, QueryBinder queryBinder) {
		this(new QueryBinderBuilder(queryString, queryBinder));
	}

	/**
	 * Constructor for a list of all results in class. While this query will be too open for
	 * most applications, it can useful in early development.
	 * @param objectClass class objects to return
	 */
	public HibernateListModel(Class<T> objectClass) {
		this.objectClass = objectClass;
	}

	/**
	 * Constructor for a list of results in class matching a built criteria.
	 * @param objectClass class for root criteria
	 * @param criteriaBuilder builder to apply criteria restrictions
	 */
	public HibernateListModel(Class<T> objectClass, CriteriaBuilder criteriaBuilder) {
		this.objectClass = objectClass;
		this.criteriaBuilder = criteriaBuilder;
	}

	/**
	 * Constructor for a custom query that is built by the calling application.
	 * @param queryBuilder builder to create and bind query object
	 */
	public HibernateListModel(QueryBuilder queryBuilder) {
		this.queryBuilder = queryBuilder;
	}

	/** @return session factory key, or null for the default factory */
	public Object getFactoryKey() {
		return factoryKey;
	}

	/**
	 * Set a factory key other than the default (null).
	 * @param key session factory key
	 * @return this, for chaining
	 */
	public HibernateListModel<T> setFactoryKey(Object key) {
		this.factoryKey = key;
		return this;
	}

	/**
	 * Load the object List through Hibernate, binding query parameters if available.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected List<T> load() {
		Session session = Databinder.getHibernateSession(factoryKey);
		if (queryBuilder != null) {
			return queryBuilder.build(session).list();
		}
		Criteria criteria = session.createCriteria(objectClass);
		if (criteriaBuilder != null)
			criteriaBuilder.build(criteria);
		return criteria.list();
	}
}
