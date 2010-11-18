/*
 * Databinder: a simple bridge from Wicket to Hibernate
 * Copyright (C) 2008  Nathan Hamblen nathan@technically.us
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.databinder.auth;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import net.databinder.auth.data.DataUser;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.cookies.CookieDefaults;
import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.util.time.Duration;

/**
 * Base class for Databinder implementations providing an implementation for
 * authentication cookies and current user lookup.
 */
public abstract class AuthDataSessionBase<T extends DataUser> extends WebSession implements AuthSession<T> {
  private static final long serialVersionUID = 1L;

  private static final String CHARACTER_ENCODING = "UTF-8";

  private final CookieUtils cookieUtils;

  /** Effective signed in state. */
	private IModel<T> userModel;

	/**
	 * Initialize new session.
	 * @see WebApplication
	 */
	public AuthDataSessionBase(Request request) {
		super(request);
		final CookieDefaults defaults = new CookieDefaults();
		defaults.setMaxAge((int) getSignInCookieMaxAge().seconds());
    cookieUtils = new CookieUtils(defaults);
	}

	@SuppressWarnings("unchecked")
  protected AuthApplication<T> getApp() {
		return (AuthApplication<T>) Application.get();
	}

	@SuppressWarnings("unchecked")
  public static <Y extends DataUser> AuthDataSessionBase<Y> get() {
		return (AuthDataSessionBase<Y>) WebSession.get();
	}

	/**
	 * @return DataUser object for current user, or null if none signed in.
	 */
	public T getUser() {
		if  (isSignedIn()) {
			return getUserModel().getObject();
		}
		return null;
	}

	public IModel<T> getUserModel() {
		return userModel;
	}

	/**
	 * @return model for current user
	 */
	public abstract IModel<T> createUserModel(T user);

	/**
	 * @return length of time sign-in cookie should persist, defined here as one month
	 */
	protected Duration getSignInCookieMaxAge() {
		return Duration.days(31);
	}

	/**
	 * Determine if user is signed in, or can be via cookie.
	 * @return true if signed in or cookie sign in is possible and successful
	 */
	public boolean isSignedIn() {
		if (userModel == null)
			cookieSignIn();
		return userModel != null;
	}

	/**
	 * @return true if signed in, false if credentials incorrect
	 */
	public boolean signIn(String username, String password) {
		return signIn(username, password, false);
	}

	/**
	 * @param setCookie if true, sets cookie to remember user
	 * @return true if signed in, false if credentials incorrect
	 */
	public boolean signIn(final String username, final String password, boolean setCookie) {
		clearUser();
		T potential = getUser(username);
		if (potential != null && (potential).getPassword().matches(password))
			signIn(potential, setCookie);

		return userModel != null;
	}

	/**
	 * Sign in a user whose credentials have been validated elsewhere. The user object must exist,
	 * and already have been saved, in the current request's Hibernate session.
	 * @param user validated and persisted user, must be in current Hibernate session
	 * @param setCookie if true, sets cookie to remember user
	 */
	public void signIn(T user, boolean setCookie) {
		userModel = createUserModel(user);
		if (setCookie)
			setCookie();
	}

	/**
	 * Attempts cookie sign in, which will set usename field but not user.
	 * @return true if signed in, false if credentials incorrect or unavailable
	 */
	protected boolean cookieSignIn() {
		String userCookie = cookieUtils.load(getUserCookieName()),
			token = cookieUtils.load(getAuthCookieName());

		if (userCookie != null && token != null) {
			T potential;
			try {
				potential = getUser(URLDecoder.decode(userCookie, CHARACTER_ENCODING));
			} catch (UnsupportedEncodingException e) {
				throw new WicketRuntimeException(e);
			}
			if (potential != null && potential instanceof DataUser) {
				String correctToken = getApp().getToken(potential);
				if (correctToken.equals(token))
					signIn(potential, false);
			}
		}
		return userModel != null;
	}

	/**
	 * Looks for a persisted DataUser object matching the given username. Uses the user class
	 * and criteria builder returned from the application subclass implementing AuthApplication.
	 * @param username
	 * @return user object from persistent storage
	 * @see AuthApplication
	 */
	protected T getUser(final String username) {
		return getApp().getUser(username);
	}

	public static String getUserCookieName() {
		return Application.get().getClass().getSimpleName() + "_USER";
	}

	public static String getAuthCookieName() {
		return Application.get().getClass().getSimpleName() + "_AUTH";
	}

	/**
	 * Sets cookie to remember the currently signed-in user. Sets max age to
	 * value from getSignInCookieMaxAge().
	 * @see AuthDataSessionBase#getSignInCookieMaxAge()
	 */
	protected void setCookie() {
		if (userModel == null)
			throw new WicketRuntimeException("User must be signed in when calling this method");

		T cookieUser = getUser();

		try {
		  cookieUtils.save(getUserCookieName(),
					URLEncoder.encode(cookieUser.getUsername(), CHARACTER_ENCODING));
		  cookieUtils.save(getAuthCookieName(), getApp().getToken(cookieUser));
		} catch (UnsupportedEncodingException e) {
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * Detach userModel manually, as it isnt' attached to any component.
	 */
	@Override
	protected void detach() {
		if (userModel != null)
			userModel.detach();
	}

	/** Nullifies userModela nd clears authentication cookies. */
	protected void clearUser() {
		userModel = null;
		cookieUtils.remove(getUserCookieName());
		cookieUtils.remove(getAuthCookieName());
  }

  /** Signs out and invalidates session. */
	public void signOut() {
	  clearUser();
		getSessionStore().invalidate(RequestCycle.get().getRequest());
	}

}
