package be.nikiroo.utils.resources;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal class used to convert data to/from {@link String}s in the context of
 * {@link Bundle}s.
 * 
 * @author niki
 */
class BundleHelper {
	/**
	 * Convert the given {@link String} into a {@link Boolean} if it represents
	 * a {@link Boolean}, or NULL if it doesn't.
	 * <p>
	 * Note: null, "strange text", ""... will all be converted to NULL.
	 * 
	 * @param str
	 *            the input {@link String}
	 * @param item
	 *            the item number to use for an array of values, or -1 for
	 *            non-arrays
	 * 
	 * @return the converted {@link Boolean} or NULL
	 */
	static public Boolean parseBoolean(String str, int item) {
		str = getItem(str, item);
		if (str == null) {
			return null;
		}

		if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("on")
				|| str.equalsIgnoreCase("yes"))
			return true;
		if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("off")
				|| str.equalsIgnoreCase("no"))
			return false;

		return null;
	}

	/**
	 * Return a {@link String} representation of the given {@link Boolean}.
	 * 
	 * @param value
	 *            the input value
	 * 
	 * @return the raw {@link String} value that correspond to it
	 */
	static public String fromBoolean(boolean value) {
		return Boolean.toString(value);
	}

	/**
	 * Convert the given {@link String} into a {@link Integer} if it represents
	 * a {@link Integer}, or NULL if it doesn't.
	 * <p>
	 * Note: null, "strange text", ""... will all be converted to NULL.
	 * 
	 * @param str
	 *            the input {@link String}
	 * @param item
	 *            the item number to use for an array of values, or -1 for
	 *            non-arrays
	 * 
	 * @return the converted {@link Integer} or NULL
	 */
	static public Integer parseInteger(String str, int item) {
		str = getItem(str, item);
		if (str == null) {
			return null;
		}

		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
		}

		return null;
	}

	/**
	 * Return a {@link String} representation of the given {@link Integer}.
	 * 
	 * @param value
	 *            the input value
	 * 
	 * @return the raw {@link String} value that correspond to it
	 */
	static public String fromInteger(int value) {
		return Integer.toString(value);
	}

	/**
	 * Convert the given {@link String} into a {@link Character} if it
	 * represents a {@link Character}, or NULL if it doesn't.
	 * <p>
	 * Note: null, "strange text", ""... will all be converted to NULL
	 * (remember: any {@link String} whose length is not 1 is <b>not</b> a
	 * {@link Character}).
	 * 
	 * @param str
	 *            the input {@link String}
	 * @param item
	 *            the item number to use for an array of values, or -1 for
	 *            non-arrays
	 * 
	 * @return the converted {@link Character} or NULL
	 */
	static public Character parseCharacter(String str, int item) {
		str = getItem(str, item);
		if (str == null) {
			return null;
		}

		String s = str.trim();
		if (s.length() == 1) {
			return s.charAt(0);
		}

		return null;
	}

	/**
	 * Return a {@link String} representation of the given {@link Boolean}.
	 * 
	 * @param value
	 *            the input value
	 * 
	 * @return the raw {@link String} value that correspond to it
	 */
	static public String fromCharacter(char value) {
		return Character.toString(value);
	}

	/**
	 * Convert the given {@link String} into a colour (represented here as an
	 * {@link Integer}) if it represents a colour, or NULL if it doesn't.
	 * <p>
	 * The returned colour value is an ARGB value.
	 * 
	 * @param str
	 *            the input {@link String}
	 * @param item
	 *            the item number to use for an array of values, or -1 for
	 *            non-arrays
	 * 
	 * @return the converted colour as an {@link Integer} value or NULL
	 */
	static Integer parseColor(String str, int item) {
		str = getItem(str, item);
		if (str == null) {
			return null;
		}

		Integer rep = null;

		str = str.trim();
		int r = 0, g = 0, b = 0, a = -1;
		if (str.startsWith("#") && (str.length() == 7 || str.length() == 9)) {
			try {
				r = Integer.parseInt(str.substring(1, 3), 16);
				g = Integer.parseInt(str.substring(3, 5), 16);
				b = Integer.parseInt(str.substring(5, 7), 16);
				if (str.length() == 9) {
					a = Integer.parseInt(str.substring(7, 9), 16);
				} else {
					a = 255;
				}

			} catch (NumberFormatException e) {
				// no changes
			}
		}

		// Try by name if still not found
		if (a == -1) {
			if ("black".equalsIgnoreCase(str)) {
				a = 255;
				r = 0;
				g = 0;
				b = 0;
			} else if ("white".equalsIgnoreCase(str)) {
				a = 255;
				r = 255;
				g = 255;
				b = 255;
			} else if ("red".equalsIgnoreCase(str)) {
				a = 255;
				r = 255;
				g = 0;
				b = 0;
			} else if ("green".equalsIgnoreCase(str)) {
				a = 255;
				r = 0;
				g = 255;
				b = 0;
			} else if ("blue".equalsIgnoreCase(str)) {
				a = 255;
				r = 0;
				g = 0;
				b = 255;
			} else if ("grey".equalsIgnoreCase(str)
					|| "gray".equalsIgnoreCase(str)) {
				a = 255;
				r = 128;
				g = 128;
				b = 128;
			} else if ("cyan".equalsIgnoreCase(str)) {
				a = 255;
				r = 0;
				g = 255;
				b = 255;
			} else if ("magenta".equalsIgnoreCase(str)) {
				a = 255;
				r = 255;
				g = 0;
				b = 255;
			} else if ("yellow".equalsIgnoreCase(str)) {
				a = 255;
				r = 255;
				g = 255;
				b = 0;
			}
		}

		if (a != -1) {
			rep = ((a & 0xFF) << 24) //
					| ((r & 0xFF) << 16) //
					| ((g & 0xFF) << 8) //
					| ((b & 0xFF) << 0);
		}

		return rep;
	}

	/**
	 * Return a {@link String} representation of the given colour.
	 * <p>
	 * The colour value is interpreted as an ARGB value.
	 * 
	 * @param color
	 *            the ARGB colour value
	 * @return the raw {@link String} value that correspond to it
	 */
	static public String fromColor(int color) {
		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = (color >> 0) & 0xFF;

		String rs = Integer.toString(r, 16);
		String gs = Integer.toString(g, 16);
		String bs = Integer.toString(b, 16);
		String as = "";
		if (a < 255) {
			as = Integer.toString(a, 16);
		}

		return "#" + rs + gs + bs + as;
	}

	/**
	 * The size of this raw list (note than a NULL list is of size 0).
	 * 
	 * @param raw
	 *            the raw list
	 * 
	 * @return its size if it is a list (NULL is an empty list), -1 if it is not
	 *         a list
	 */
	static public int getListSize(String raw) {
		if (raw == null) {
			return 0;
		}

		List<String> list = parseList(raw, -1);
		if (list == null) {
			return -1;
		}

		return list.size();
	}

	/**
	 * Return a {@link String} representation of the given list of values.
	 * <p>
	 * The list of values is comma-separated and each value is surrounded by
	 * double-quotes; caret (^) and double-quotes (") are escaped by a caret.
	 * 
	 * @param str
	 *            the input value
	 * @param item
	 *            the item number to use for an array of values, or -1 for
	 *            non-arrays
	 * 
	 * @return the raw {@link String} value that correspond to it
	 */
	static public List<String> parseList(String str, int item) {
		if (str == null) {
			return null;
		}

		if (item >= 0) {
			str = getItem(str, item);
		}

		List<String> list = new ArrayList<String>();
		try {
			boolean inQuote = false;
			boolean prevIsBackSlash = false;
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < str.length(); i++) {
				char car = str.charAt(i);

				if (prevIsBackSlash) {
					// We don't process it here
					builder.append(car);
					prevIsBackSlash = false;
				} else {
					switch (car) {
					case '"':
						// We don't process it here
						builder.append(car);

						if (inQuote) {
							list.add(unescape(builder.toString()));
							builder.setLength(0);
						}

						inQuote = !inQuote;
						break;
					case '^':
						// We don't process it here
						builder.append(car);
						prevIsBackSlash = true;
						break;
					case ' ':
					case '\n':
					case '\r':
						if (inQuote) {
							builder.append(car);
						}
						break;

					case ',':
						if (!inQuote) {
							break;
						}
						// continue to default
					default:
						if (!inQuote) {
							// Bad format!
							return null;
						}

						builder.append(car);
						break;
					}
				}
			}

			if (inQuote || prevIsBackSlash) {
				// Bad format!
				return null;
			}

		} catch (Exception e) {
			return null;
		}

		return list;
	}

	/**
	 * Return a {@link String} representation of the given list of values.
	 * <p>
	 * NULL will be assimilated to an empty {@link String} if later non-null
	 * values exist, or just ignored if not.
	 * <p>
	 * Example:
	 * <ul>
	 * <li><tt>1</tt>,<tt>NULL</tt>, <tt>3</tt> will become <tt>1</tt>,
	 * <tt>""</tt>, <tt>3</tt></li>
	 * <li><tt>1</tt>,<tt>NULL</tt>, <tt>NULL</tt> will become <tt>1</tt></li>
	 * <li><tt>NULL</tt>, <tt>NULL</tt>, <tt>NULL</tt> will become an empty list
	 * </li>
	 * </ul>
	 * 
	 * @param list
	 *            the input value
	 * 
	 * @return the raw {@link String} value that correspond to it
	 */
	static public String fromList(List<String> list) {
		if (list == null) {
			list = new ArrayList<String>();
		}

		int last = list.size() - 1;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) != null) {
				last = i;
			}
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i <= last; i++) {
			String item = list.get(i);
			if (item == null) {
				item = "";
			}

			if (builder.length() > 0) {
				builder.append(", ");
			}
			builder.append(escape(item));
		}

		return builder.toString();
	}

	/**
	 * Return a {@link String} representation of the given list of values.
	 * <p>
	 * NULL will be assimilated to an empty {@link String} if later non-null
	 * values exist, or just ignored if not.
	 * <p>
	 * Example:
	 * <ul>
	 * <li><tt>1</tt>,<tt>NULL</tt>, <tt>3</tt> will become <tt>1</tt>,
	 * <tt>""</tt>, <tt>3</tt></li>
	 * <li><tt>1</tt>,<tt>NULL</tt>, <tt>NULL</tt> will become <tt>1</tt></li>
	 * <li><tt>NULL</tt>, <tt>NULL</tt>, <tt>NULL</tt> will become an empty list
	 * </li>
	 * </ul>
	 * 
	 * @param list
	 *            the input value
	 * @param value
	 *            the value to insert
	 * @param item
	 *            the position to insert it at
	 * 
	 * @return the raw {@link String} value that correspond to it
	 */
	static public String fromList(List<String> list, String value, int item) {
		if (list == null) {
			list = new ArrayList<String>();
		}

		while (item >= list.size()) {
			list.add(null);
		}
		list.set(item, value);

		return fromList(list);
	}

	/**
	 * Return a {@link String} representation of the given list of values.
	 * <p>
	 * NULL will be assimilated to an empty {@link String} if later non-null
	 * values exist, or just ignored if not.
	 * <p>
	 * Example:
	 * <ul>
	 * <li><tt>1</tt>,<tt>NULL</tt>, <tt>3</tt> will become <tt>1</tt>,
	 * <tt>""</tt>, <tt>3</tt></li>
	 * <li><tt>1</tt>,<tt>NULL</tt>, <tt>NULL</tt> will become <tt>1</tt></li>
	 * <li><tt>NULL</tt>, <tt>NULL</tt>, <tt>NULL</tt> will become an empty list
	 * </li>
	 * </ul>
	 * 
	 * @param list
	 *            the input value
	 * @param value
	 *            the value to insert
	 * @param item
	 *            the position to insert it at
	 * 
	 * @return the raw {@link String} value that correspond to it
	 */
	static public String fromList(String list, String value, int item) {
		return fromList(parseList(list, -1), value, item);
	}

	/**
	 * Escape the given value for list formating (no carets, no NEWLINES...).
	 * <p>
	 * You can unescape it with {@link BundleHelper#unescape(String)}
	 * 
	 * @param value
	 *            the value to escape
	 * 
	 * @return an escaped value that can unquoted by the reverse operation
	 *         {@link BundleHelper#unescape(String)}
	 */
	static public String escape(String value) {
		return '"' + value//
				.replace("^", "^^") //
				.replace("\"", "^\"") //
				.replace("\n", "^\n") //
				.replace("\r", "^\r") //
		+ '"';
	}

	/**
	 * Unescape the given value for list formating (change ^n into NEWLINE and
	 * so on).
	 * <p>
	 * You can escape it with {@link BundleHelper#escape(String)}
	 * 
	 * @param value
	 *            the value to escape
	 * 
	 * @return an unescaped value that can reverted by the reverse operation
	 *         {@link BundleHelper#escape(String)}, or NULL if it was badly
	 *         formated
	 */
	static public String unescape(String value) {
		if (value.length() < 2 || !value.startsWith("\"")
				|| !value.endsWith("\"")) {
			// Bad format
			return null;
		}

		value = value.substring(1, value.length() - 1);

		boolean prevIsBackslash = false;
		StringBuilder builder = new StringBuilder();
		for (char car : value.toCharArray()) {
			if (prevIsBackslash) {
				switch (car) {
				case 'n':
				case 'N':
					builder.append('\n');
					break;
				case 'r':
				case 'R':
					builder.append('\r');
					break;
				default: // includes ^ and "
					builder.append(car);
					break;
				}
				prevIsBackslash = false;
			} else {
				if (car == '^') {
					prevIsBackslash = true;
				} else {
					builder.append(car);
				}
			}
		}

		if (prevIsBackslash) {
			// Bad format
			return null;
		}

		return builder.toString();
	}

	/**
	 * Retrieve the specific item in the given value, assuming it is an array.
	 * 
	 * @param value
	 *            the value to look into
	 * @param item
	 *            the item number to get for an array of values, or -1 for
	 *            non-arrays (in that case, simply return the value as-is)
	 * 
	 * @return the value as-is for non arrays, the item <tt>item</tt> if found,
	 *         NULL if not
	 */
	static private String getItem(String value, int item) {
		if (item >= 0) {
			value = null;
			List<String> values = parseList(value, -1);
			if (values != null && item < values.size()) {
				value = values.get(item);
			}
		}

		return value;
	}
}
