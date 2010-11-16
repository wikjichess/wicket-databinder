package net.databinder.components;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

/**
 * Links to set the model of a target component.
 * @author Nathan Hamblen
 */
public class ModelSourceLink<T> extends Link<T> {
  private static final long serialVersionUID = 1L;

  private Component target;
	/**
	 * Construct link to set model of component.
	 * @param id component id
	 * @param target set model of this component
	 * @param model the model to set
	 */
	public ModelSourceLink(String id, Component target, IModel<T> model) {
		super(id, model);
		this.target = target;
	}
	/** return false when model is already set to target and is visible */
	@Override
	public boolean isEnabled() {
		if (!target.isVisible())
			return true;

		return !getModelObject().equals(target.getDefaultModelObject());
	}
	/**
	 * Set model of target, and set target to visible in case it has hide before use behavior.
	 */
	@Override
	public void onClick() {
		target.setDefaultModelObject(getModelObject());
		target.setVisible(true);
	}
}