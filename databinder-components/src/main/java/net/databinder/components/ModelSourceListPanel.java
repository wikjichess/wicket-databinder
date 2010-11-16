package net.databinder.components;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

/**
 * Displays a list of links to set the model of a target component. The panel renders to an
 * unordered list of class <tt>source-list</tt>.
 * @author Nathan Hamblen
 */
public class ModelSourceListPanel<T> extends SourceListPanel<T> {
  private static final long serialVersionUID = 1L;

  private Component target;
	/**
	 * Creates list panel.
	 * @param id component id
	 * @param target sets model of this component
	 * @param bodyProperty object property for link body text
	 * @param listModel list of entities to render
	 */
	public ModelSourceListPanel(String id, Component target, String bodyProperty, IModel<List<T>> listModel ) {
		super(id, bodyProperty, listModel);
		this.target = target;
	}
	/** Called from super-class to construct source links. Note: subclasses my override
	 * to add attribute modifiers to the ModelSourceLink object constructed here, for example. */
	@Override
	protected Link<T> sourceLink(String id, IModel<T> model) {
		return new ModelSourceLink<T>("link", target, model) {
      private static final long serialVersionUID = ModelSourceListPanel.serialVersionUID;

      @Override
			public void onClick() {
				ModelSourceListPanel.this.onClick(this);
				super.onClick();
			}
		};
	}

	/**
	 * Called before the default ModelSourceLink's onClick. Base impl does nothing.
	 * @param link component that was clicked
	 */
	protected void onClick(ModelSourceLink<T> link) { }
}
