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

package net.databinder.converters;

import java.awt.Color;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.converter.AbstractConverter;
import org.apache.wicket.util.string.Strings;

/**
 * Convert between a HTML hex color string and java.awt.Color.
 * @author Nathan Hamblen
 * @see Color
 */
public class ColorConverter extends AbstractConverter {
  private static final long serialVersionUID = 1L;

  @Override
	protected Class<Color> getTargetType() {
		return Color.class;
	}

	public Color convertToObject(String str, Locale loc) {
		try {
			if (Strings.isEmpty(str)) return null;
			return Color.decode(str.toString());
		} catch (NumberFormatException e) {
			throw new ConversionException(e);
		}
	}

	@Override
	public String convertToString(Object o, Locale locale) {
		if (o == null) return null;
		return "#" + Integer.toHexString(((Color)o).getRGB()).substring(2);
	}

}
