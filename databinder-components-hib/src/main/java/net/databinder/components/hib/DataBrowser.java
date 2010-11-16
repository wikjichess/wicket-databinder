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

import java.util.ArrayList;
import java.util.List;

import net.databinder.hib.Databinder;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.AccessDeniedPage;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Page containing a QueryPanel for browsing data and testing Hibernate queries. This
 * page is not bookmarkable, so that it will not be inadverdantly
 * available from the classpath. To access the page it must be subclassed or manually
 * linked. DataApplication.BmarkDataBrowser is a subclass that requires that the
 * running application be assignable to DataApplication and return true for
 * isDataBrowserAllowed(). To use this page from an application that does not extend
 * DataApplication, make a bookmarkable subclass and call super(true),
 * or link to the class with PageLink.
 * @author Nathan Hamblen
 */
public class DataBrowser<T> extends WebPage {
  private static final long serialVersionUID = 1L;

  private static final ResourceReference CSS =
    new PackageResourceReference(DataBrowser.class, "DataBrowser.css");

	public DataBrowser(boolean allowAccess) {
		if (allowAccess) {
			add(new QueryPanel("queryPanel"));

			add(new ListView<T>("entities", new LoadableDetachableModel<List<T>>() {
			  private static final long serialVersionUID = DataBrowser.serialVersionUID;

				@SuppressWarnings("unchecked")
				@Override
				protected List<T> load() {
					return new ArrayList<T>(
							Databinder.getHibernateSessionFactory().getAllClassMetadata().keySet());
				}
			}) {
        private static final long serialVersionUID = DataBrowser.serialVersionUID;

        @Override
				protected void populateItem(ListItem<T> item) {
					item.add(new Label("name", item.getModel()));
				}
			});

		} else setResponsePage(AccessDeniedPage.class);
	}

  @Override
  public void renderHead(IHeaderResponse response)
  {
    super.renderHead(response);
    response.renderCSSReference(CSS);
  }


}