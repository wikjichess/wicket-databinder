/*
 * Databinder: a simple bridge from Wicket to Hibernate
 * Copyright (C) 2006  Nathan Hamblen nathan@technically.us
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
package net.databinder.web;

import javax.servlet.http.Cookie;

import org.apache.wicket.request.http.WebResponse;

/**
 * Creates web response objects that do not rewrite URLs for cookieless support. Buffered or
 * basic responses are created according to the application configuration. This factory is
 * used by DataApplicationBase when cookieless support is off, but may also be used independently.
 * @see net.databinder.DataApplicationBase
 * @author Nathan Hamblen
 */
public class NorewriteWebResponse extends WebResponse {

  private final WebResponse response;

  public NorewriteWebResponse(WebResponse response)
  {
    this.response = response;
  }

  @Override
  public void write(CharSequence sequence)
  {
    response.write(sequence);
  }

  @Override
  public void addCookie(Cookie cookie)
  {
    response.addCookie(cookie);
  }

  @Override
  public void write(byte[] array)
  {
    response.write(array);
  }

  @Override
  public void clearCookie(Cookie cookie)
  {
    response.clearCookie(cookie);
  }

  @Override
  public void setHeader(String name, String value)
  {
    response.setHeader(name, value);
  }

  @Override
  public String encodeURL(CharSequence url)
  {
    return url.toString();
  }

  @Override
  public void setDateHeader(String name, long date)
  {
    response.setDateHeader(name, date);
  }

  @Override
  public void setContentLength(long length)
  {
    response.setContentLength(length);
  }

  @Override
  public void setContentType(String mimeType)
  {
    response.setContentType(mimeType);
  }

  @Override
  public void setStatus(int sc)
  {
    response.setStatus(sc);
  }

  @Override
  public void sendError(int sc, String msg)
  {
    response.sendError(sc, msg);
  }

  @Override
  public void sendRedirect(String url)
  {
    response.sendRedirect(url);
  }

  @Override
  public boolean isRedirect()
  {
    return response.isRedirect();
  }

  @Override
  public void flush()
  {
    response.flush();
  }

}
