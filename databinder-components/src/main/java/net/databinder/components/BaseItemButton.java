package net.databinder.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.ImageButton;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Base class for item buttons, whether ListItem or repeater Item.
 */
public abstract class BaseItemButton extends ImageButton {
  private static final long serialVersionUID = 1L;

  public BaseItemButton(String id, ResourceReference image) {
		super(id, image);
		add(new AttributeModifier("class", true, new AbstractReadOnlyModel<String>() {
      private static final long serialVersionUID = BaseItemButton.serialVersionUID;

      @Override
			public String getObject() {
				return isEnabled() ? null : "disabled-image";
			}
		}));
	}

	protected static ResourceReference getTrashImage() {
		return new PackageResourceReference(BaseItemButton.class, "image/trash.png");
	}
}
