package net.databinder.components.tree.hib;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;


/**
 * Expand all tree nodes.
 *
 * @author Thomas Kappler
 */
public class DataTreeExpandAllLink extends AjaxLink<Void> {
  private static final long serialVersionUID = 1L;

	private DataTree<?> tree;

	public DataTreeExpandAllLink(String id, DataTree<?> tree) {
		super(id);
		this.tree = tree;
	}

	@Override
	public void onClick(AjaxRequestTarget target) {
		tree.getTreeState().expandAll();
		tree.updateTree(target);
	}
}