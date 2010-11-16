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

import java.util.Collections;
import java.util.List;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.resource.PackageResourceReference;

/** Move the given list item up in its list. */
public class MoveUpButton<T> extends ListItemButton<T> {
  private static final long serialVersionUID = 1L;

  public MoveUpButton(String id, ListItem<T> item) {
		super(id, item, new PackageResourceReference(MoveUpButton.class, "image/up-arrow.png"));
	}
	@Override
	public void onSubmit() {
	  final ListView<T> lv = getListView();
    final List<? extends T> list = lv.getList();
    int index = item.getIndex();
    if (index >= 1 && index < list.size())
    {
  		lv.modelChanging();
  		Collections.swap(lv.getList(), index, index - 1);
  		lv.modelChanged();
    }
	}
	@Override
	public boolean isEnabled() {
		return item.getIndex() > 0;
	}

}
