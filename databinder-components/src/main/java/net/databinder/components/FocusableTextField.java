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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavascriptResourceReference;

/**
 * TextField that can be told to focus itself on the next request.  Works in conjunction with
 * the onload handler.
 */
public class FocusableTextField<T> extends TextField<T> {
  private static final long serialVersionUID = 1L;

  private static final JavascriptResourceReference JAVASCRIPT_REFERENCE =
    new JavascriptResourceReference(FocusableTextField.class, "FocusableTextField.js");


  private boolean wantsFocus = false;

	/**
	 * 	@param id Wicket id
	 * @param model text field model
	 */
	public FocusableTextField(String id, IModel<T> model) {
		super (id, model);
	}

	/**
	 * @param id Wicket id
	 */
	public FocusableTextField(String id) {
		this(id, (IModel<T>) null);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.renderJavascriptReference(JAVASCRIPT_REFERENCE);
		response.renderOnLoadJavascript("initFocusableTextField();");
	}

	/**
	 * Request focus on next rendering.
	 */
	public void requestFocus() {
		wantsFocus = true;
	}

	/** Adds flagging id attribute if focus has been requested. */
	@Override
	protected void onComponentTag(ComponentTag tag) {
		if (wantsFocus) {
			tag.put("id", "focusMe");
			wantsFocus = false;
		}
		super.onComponentTag(tag);
	}
}