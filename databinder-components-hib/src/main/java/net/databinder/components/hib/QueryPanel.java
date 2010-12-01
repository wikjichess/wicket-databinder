/*
 * Databinder: a simple bridge from Wicket to Hibernate
 * Copyright (C) 2007  Nathan Hamblen nathan@technically.us
 * Copyright (C) 2007  xoocode.org project

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.databinder.hib.Databinder;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;
import org.hibernate.Query;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;

/**
 * A Panel used to display a textarea to enter an HQL query and execute it
 * against the current session of a {@link SessionFactory}.
 * <p>
 * The panel result is displayed in a data table, where columns are created
 * according to the query.
 * <p>
 * For instance, a query like:
 * <pre>
 * select job.name as name, job.id as id from JobModel job
 * </pre>
 * will result in two columns, 'name' and 'id', in the result data table.
 * <p>
 * If you run:
 * <pre>
 * from JobModel
 * </pre>
 * the columns in the result table will be the available properties of a
 * JobModel
 */
public class QueryPanel extends Panel {
	private static final long serialVersionUID = 1L;

	/**
	 * Bean used to store the query
	 */
	private QueryBean query = new QueryBean();
	/**
	 * Stores information about the query execution (executed query, time, ...)
	 */
	private String executionInfo;

	/**
	 * Constructs an {@link QueryPanel}
	 * @param id
	 * 			the panel identifier. Must not be null.
	 */
	public QueryPanel(String id) {
		super(id);

		final WebMarkupContainer resultsHolder = new WebMarkupContainer("resultsHolder");
		resultsHolder.add(new Label("executionInfo", new PropertyModel<String>(this, "executionInfo")));
		resultsHolder.add(getResultsTable());
		resultsHolder.setOutputMarkupId(true);
		add(resultsHolder);

		Form<QueryBean> form = new Form<QueryBean>("form", new CompoundPropertyModel<QueryBean>(query));
		form.setOutputMarkupId(true);
		form.add(new TextArea<String>("query"));
		form.add(new AjaxButton("submit", form) {
			private static final long serialVersionUID = 1L;

			@Override
      protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				if (resultsHolder.get("results") != null) {
					resultsHolder.remove("results");
				}
				try {
					resultsHolder.add(getResultsTable());
				} catch (QueryException e) {
					note(e);
				} catch (IllegalArgumentException e) {
					note(e);
				} catch (IllegalStateException e) {
					note(e);
				}
				target.add(resultsHolder);
			}

      @Override
      protected void onError(AjaxRequestTarget target, Form<?> form)
      {
      }

      private void note(Exception e) {
        resultsHolder.add(new Label("results",
            e.getClass().getSimpleName()+ ": " + e.getMessage()));
      }

		});
		add(form);
	}

	/**
	 * Creates a result table for the current query.
	 * @return a result table, or an empty label if there is no current query
	 */
	@SuppressWarnings("unchecked")
	private Component getResultsTable() {
		if (Strings.isEmpty(query.getQuery())) {
			return new Label("results", "");
		} else {
		  SortableDataProvider<Object[]> dataProvider = new SortableDataProvider<Object[]>() {
				private static final long serialVersionUID = 1L;

				@Override
        public void detach() {
				}

				public int size() {
					Session sess = Databinder.getHibernateSession();
					Query query = sess.createQuery(getQuery());
					return query.list().size();
				}

				public String getQuery() {
					return QueryPanel.this.query.getQuery();
				}

				public IModel<Object[]> model(Object[] object) {
					return new CompoundPropertyModel<Object[]>(new HibernateObjectModel<Object[]>(object));
				}

				public Iterator<Object[]> iterator(int first, int count) {
					Session sess =  Databinder.getHibernateSession();
					long start = System.nanoTime();
					try {
						Query q = sess.createQuery(getQuery());
						q.setFirstResult(first);
						q.setMaxResults(count);
						return q.iterate();
					} finally {
						float nanoTime = ((System.nanoTime()-start) / 1000) / 1000.0f;
						setExecutionInfo("query executed in "+nanoTime+" ms: "+getQuery());
					}
				}
			};
			IColumn<Object[]>[] columns;
			Session sess =  Databinder.getHibernateSession();
			Query q = sess.createQuery(query.getQuery());
			String[] aliases;
			Type[] returnTypes;
			try {
				aliases = q.getReturnAliases();
				returnTypes = q.getReturnTypes();
			} catch (NullPointerException e) { // thrown on updates
				return new Label("results", "");
			}

			if (returnTypes.length != 1) {
				columns = new IColumn[returnTypes.length];
				for (int i = 0; i < returnTypes.length; i++) {
					String alias = aliases == null || aliases.length <= i?returnTypes[i].getName():aliases[i];
					final int index = i;
					columns[i] = new AbstractColumn<Object[]>(Model.of(alias)) {
						private static final long serialVersionUID = 1L;

						public void populateItem(Item<ICellPopulator<Object[]>> cellItem, String componentId, IModel<Object[]> rowModel) {
							Object[] objects = rowModel.getObject();
							cellItem.add(new Label(componentId,
							    Model.of(objects[index]==null?"":objects[index].toString())));
						}
					};
				}
			} else {
				Type returnType = returnTypes[0];
				if (returnType.isEntityType()) {
					Class<?> clss = returnType.getReturnedClass();
					ClassMetadata metadata = Databinder.getHibernateSessionFactory().getClassMetadata(clss);
					List<IColumn<?>> cols = new ArrayList<IColumn<?>>();
					String idProp = metadata.getIdentifierPropertyName();
					cols.add(new PropertyColumn<Object>(Model.of(idProp), idProp));
					String[] properties = metadata.getPropertyNames();
					for (String prop : properties) {
						Type type = metadata.getPropertyType(prop);
						if (type.isCollectionType()) {
							// TODO: see if we could provide a link to the collection value
						} else {
							cols.add(new PropertyColumn<Object>(Model.of(prop), prop));
						}
					}
					columns = cols.toArray(new IColumn[cols.size()]);
				} else {
					String alias = aliases == null || aliases.length == 0?returnType.getName():aliases[0];
					columns = new IColumn[] {new AbstractColumn<Object>(Model.of(alias)) {
						private static final long serialVersionUID = 1L;

						public void populateItem(Item<ICellPopulator<Object>> cellItem, String componentId, IModel<Object> rowModel) {
							cellItem.add(new Label(componentId, rowModel));
						}
					}};
				}
			}
			DataTable<Object[]> dataTable = new DataTable<Object[]>("results", columns, dataProvider, 10);

	    dataTable.addTopToolbar(new HeadersToolbar("headersToolbar", dataTable, dataProvider));
			dataTable.addBottomToolbar(new NavigationToolbar("navigationToolbar", dataTable));
			dataTable.setOutputMarkupId(true);
			return dataTable;
		}
	}

	private static class QueryBean implements Serializable {
		private static final long serialVersionUID = 1L;
		private String query;

		public String getQuery() {
			return query;
		}

		@SuppressWarnings("unused")
    public void setQuery(String query) {
			this.query = query;
		}
	}

	public String getExecutionInfo() {
		return executionInfo;
	}

	public void setExecutionInfo(String executionInfo) {
		this.executionInfo = executionInfo;
	}

}
