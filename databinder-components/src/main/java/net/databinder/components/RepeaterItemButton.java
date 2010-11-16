package net.databinder.components;

import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Base class for Item classes in Wicket's repeater package.
 */
public abstract class RepeaterItemButton<T> extends BaseItemButton
{
  private static final long serialVersionUID = 1L;

  Item<T> item;

  public RepeaterItemButton(String id, Item<T> item, ResourceReference image) {
    super(id, image);
    this.item = item;
  }

  @SuppressWarnings("unchecked")
  protected RefreshingView<T> getView() {
    return (RefreshingView<T>) item.getParent();
  }

}
