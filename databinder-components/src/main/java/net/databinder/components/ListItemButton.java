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
package net.databinder.components;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.resource.ResourceReference;

/** Base class for buttons that act on list items (move, delete). */
public abstract class ListItemButton<T> extends BaseItemButton {
  private static final long serialVersionUID = 1L;

  ListItem<T> item;

	public ListItemButton(String id, ListItem<T> item, ResourceReference image) {
		super(id, image);
		this.item = item;
	}

	@SuppressWarnings("unchecked")
  protected ListView<T> getListView() {
		return (ListView<T>) item.getParent();
	}

}
