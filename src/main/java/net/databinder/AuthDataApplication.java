package net.databinder;

import org.hibernate.cfg.AnnotationConfiguration;

import net.databinder.components.DataSignInPage;
import net.databinder.data.IUser;
import net.databinder.data.User;
import wicket.Component;
import wicket.RestartResponseAtInterceptPageException;
import wicket.Session;
import wicket.authorization.IUnauthorizedComponentInstantiationListener;
import wicket.authorization.UnauthorizedInstantiationException;
import wicket.authorization.strategies.role.IRoleCheckingStrategy;
import wicket.authorization.strategies.role.RoleAuthorizationStrategy;
import wicket.authorization.strategies.role.Roles;
import wicket.markup.html.WebPage;

public abstract class AuthDataApplication extends DataApplication implements IUnauthorizedComponentInstantiationListener, IRoleCheckingStrategy {

	@Override
	protected void init() {
		super.init();
		getSecuritySettings().setAuthorizationStrategy(new RoleAuthorizationStrategy(this));
		getSecuritySettings().setUnauthorizedComponentInstantiationListener(this);
	}
	
	@Override
	protected AuthDataSession newDataSession() {
		return new AuthDataSession(this);
	}
	
	@Override
	protected void configureHibernate(AnnotationConfiguration config) {
		super.configureHibernate(config);
		config.addAnnotatedClass(getUserClass());
	}
	
	public void onUnauthorizedInstantiation(Component component) {
		if (((AuthDataSession)Session.get()).isSignedIn()) {
			throw new UnauthorizedInstantiationException(component.getClass());
		}
		else {
			throw new RestartResponseAtInterceptPageException(getSignInPageClass());
		}	
	}
	
	public final boolean hasAnyRole(Roles roles) {
		return ((AuthDataSession)Session.get()).hasAnyRole(roles);
	}
	
	public Class< ? extends IUser> getUserClass() {
		return User.class;
	}

	protected Class< ? extends WebPage> getSignInPageClass() {
		return DataSignInPage.class;
	}
}
