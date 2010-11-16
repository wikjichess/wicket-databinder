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
package net.databinder.components.hib;

import java.io.Serializable;

import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IChainingModel;

/**
 * Panel subclass to be tied to a single persistent object. Similar to
 * DataForm, this component wraps its model in a BoundCompoundProperty model
 * and provides access methods to the nested model.
 * @see DataForm
 * @author Nathan Hamblen
 */
public class DataPanel<T> extends Panel {
  private static final long serialVersionUID = 1L;

	/**
	 * Create panel with an existing persistent object model.
	 * @param id Wicket id
	 * @param model to be wrapped in a BoundCompoundPropertyModel
	 */
	public DataPanel(String id, HibernateObjectModel<T> model) {
		super(id, new CompoundPropertyModel<T>(model));
	}

	/**
	 * Instantiates this panel with a persistent object of the given class and id.
	 * @param id Wicket id
	 * @param modelClass for the persistent object
	 * @param persistentObjectId id of the persistent object
	 */
	public DataPanel(String id, Class<T> modelClass, Serializable persistentObjectId) {
		super(id, new CompoundPropertyModel<T>(new HibernateObjectModel<T>(modelClass, persistentObjectId)));
	}

	/**
	 * Create panel without a model. Use this constructor only if the intended model
	 * is not available at construction time of DataPanel subclass.
	 * @param id Wicket id
	 * @see DataPanel#setPersistentObject(Object object)
	 */
	protected DataPanel(String id) {
		super(id);
	}

	/**
	 * @return the nested model, casted to the expected IModel subclass.
	 */
	@SuppressWarnings("unchecked")
	protected HibernateObjectModel<T> getPersistentObjectModel() {
		try {
			return (HibernateObjectModel<T>) ((IChainingModel<T>)getDefaultModel()).getChainedModel();
		} catch (ClassCastException c) {
			throw new RuntimeException("DataPanel's nested model was not a HibernateObjectModel", c);
		}
	}

	/**
	 * Change the persistent model object of this panel.
	 * @param object  to attach to this panel
	 * @return this panel, for chaining
	 */
	public DataPanel<T> setPersistentObject(T object) {
		getPersistentObjectModel().setObject(object);
		return this;
	}

	/**
	 * Set the persistant object model. Use this when a DataPanel subclass has
	 * been constructed without a model.
	 * @param model to back this panel
	 * @return this panel, for chaining
	 */
	protected DataPanel<T> setPersistentObjectModel(HibernateObjectModel<T> model) {
		setDefaultModel(new CompoundPropertyModel<T>(model));
		return this;
	}
}