package net.databinder.components.hib;

import net.databinder.hib.Databinder;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;

/**
 * Boomarkable link with object identifier set automatically from the attached object.
 * @author Nathan Hamblen
 */
public class PageSourceLink<T> extends BookmarkablePageLink<T> {
  private static final long serialVersionUID = 1L;

	private String idParameter;
	/**
	 * Construct a link to a page using the model object's Hibernate identifier set to the
	 * "id" parameter.
	 * @see #setParameters()
	 * @param id component id
	 * @param pageClass target page class
	 * @param model model to bind to component
	 */
	public <C extends Page> PageSourceLink(String id, Class<C> pageClass, IModel<T> model) {
		this(id, pageClass, model, null);
	}
	/**
	 * Construct a link to a page using the model object's Hibernate identifier set to the
	 * given identifier parameter.
	 * @param id component id
	 * @param pageClass target page class
	 * @param model model to bind to component
	 * @param idParameter name for identifying parameter
	 */
	public <C extends Page> PageSourceLink(String id, Class<C> pageClass, IModel<T> model, String idParameter) {
		super(id, pageClass);
		setModel(model);
		this.idParameter = idParameter == null ? "id" : idParameter;
	}
	/**
	 * @return true if pointing to a different page class than the current and a different model object
	 */
	@Override
	public boolean isEnabled() {
		Page thisPage = getPage();
		if (!getPageClass().isInstance(thisPage))
			return true;
		Object o = getPage().getDefaultModelObject();
		return o == null || !o.equals(getModelObject());
	}
	/** Calls setParameters() */
	@Override
	protected void onBeforeRender() {
		super.onBeforeRender();
		setParameters();
	}
	/** Sets the id parameter to  the identifier given by Hibernate*/
	protected void setParameters() {
		setParameter(idParameter,
				Databinder.getHibernateSession().getIdentifier(getModelObject()).toString());
	}
}
