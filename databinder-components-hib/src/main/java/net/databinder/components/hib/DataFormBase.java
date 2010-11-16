package net.databinder.components.hib;

import net.databinder.hib.Databinder;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;

/**
 * Base class for forms that commit in onSubmit(). This is extended by DataForm, and may be
 * extended directly by client forms when DataForm is not appropriate. Transactions
 * are committed only when no errors are displayed.
 * @author Nathan Hamblen
 */
public class DataFormBase<T> extends Form<T> {
  private static final long serialVersionUID = 1L;

	private Object factoryKey;
	public DataFormBase(String id) {
		super(id);
	}
	public DataFormBase(final String id, IModel<T> model)
	{
		super(id, model);
	}

	public Object getFactoryKey() {
		return factoryKey;
	}

	public DataFormBase<T> setFactoryKey(Object key) {
		this.factoryKey = key;
		return this;
	}

	protected Session getHibernateSession() {
		return Databinder.getHibernateSession(factoryKey);
	}

	/** Default implementation calls {@link #commitTransactionIfValid()}. */
	@Override
  protected void onSubmit() {
		commitTransactionIfValid();
	}

	/**
	 * Commit transaction if no errors are registered for any form component.
	 * @return true if transaction was committed
	 */
	protected boolean commitTransactionIfValid() {
		try {
			if (!hasError()) {
				Session session = Databinder.getHibernateSession(factoryKey);
				session.flush(); // needed for conv. sessions, harmless otherwise
				onBeforeCommit();
				session.getTransaction().commit();
				session.beginTransaction();
				return true;
			}
		} catch (StaleObjectStateException e) {
			error(getString("version.mismatch", null)); // report error
		}
		return false;
	}

	/** Called before committing a transaction by {@link #commitTransactionIfValid()}. */
	protected void onBeforeCommit() { };

}
