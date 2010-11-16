package net.databinder.components;

import java.util.Iterator;

import org.apache.wicket.markup.repeater.Item;

/** Removes the given item from its collection. */
public class RemoveRepeaterItemButton<T> extends RepeaterItemButton<T> {
  private static final long serialVersionUID = 1L;

  public RemoveRepeaterItemButton(String id, Item<T> item) {
      super(id, item, getTrashImage());
   }

   @Override
   public void onSubmit() {
      Iterator<Item<T>> i = getView().getItems();
      if (i != null) {
        while (i.hasNext()) {
          // depends on correct equals()!
          if (i.next().equals(item.getModelObject())) {
            getView().modelChanging();
            i.remove();
            getView().modelChanged();
            break;
          }
        }
      }
   }

}
