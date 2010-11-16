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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;

/**
 * Panel for a comma-separated list of tag strings displayed in a text field or text area. The
 * backing model for this component must be a Collection of String tags. Tags are converted to
 * lower case before insertion to the set.
 */
public class TagField extends Panel {
  private static final long serialVersionUID = 1L;

  /** Generates text field */
	public TagField(String id) {
		this(id, null);
	}
	/** Generates text field */
	public TagField(String id, IModel<? extends Collection<String>> model) {
		this(id, model, false);
	}
	/** @param textarea true to generate a text area */
	public TagField(String id, boolean textarea) {
		this(id, null, textarea);
	}
	/** @param textarea true to generate a text area */
	public TagField(String id, IModel<? extends Collection<String>> model, boolean textarea) {
		super(id, model);
		IModel<String> tagModel = new IModel<String>() {
      private static final long serialVersionUID = TagField.serialVersionUID;

      public void detach() {}
			@SuppressWarnings("unchecked")
			public String getObject() {
			  Collection<String> tags = (Collection<String>) TagField.this.getDefaultModelObject();
				if (tags == null) return null;
				return Strings.join(", ",  tags.toArray(new String[tags.size()]));
			}
			public void setObject(String object) {
				if (object == null)
					TagField.this.setDefaultModelObject(new HashSet<String>());
				else {
					String value = object.toLowerCase();
					String[] tagstrs = value.split(" *, *,* *"); // also consumes empty ' ,  ,' tags
					TagField.this.setDefaultModelObject(new HashSet<String>(Arrays.asList(tagstrs)));
				}
			}
		};
		add(new TextField<String>("field", tagModel).setVisible(!textarea));
		add(new TextArea<String>("area", tagModel).setVisible(textarea));
	}
}
