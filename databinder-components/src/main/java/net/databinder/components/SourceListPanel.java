package net.databinder.components;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Displays a list of links to set the model of a target component or page. The panel renders to an
 * unordered list of class <tt>source-list</tt>.
 * @author Nathan Hamblen
 */
public abstract class SourceListPanel<T> extends Panel {
  private static final long serialVersionUID = 1L;

  /**
	 * @param id panel id
	 * @param bodyProperty property to display as link body
	 * @param listModel must  the list from which the model objects will be drawn
	 */
	public SourceListPanel(String id, final String bodyProperty, IModel<List<T>> listModel) {
		super(id);
		add(new PropertyListView<T>("list", listModel) {
      private static final long serialVersionUID = SourceListPanel.serialVersionUID;

      @Override
      protected void populateItem(final ListItem<T> item) {
				Link<T> link = sourceLink("link", item.getModel());
				Label title = new Label("title", Model.of(bodyProperty));
				item.add(link.add(title));
			}
		});
	}
	/** Supply a source link for the model and id */
	protected abstract Link<T> sourceLink(String id, IModel<T> model);
}
