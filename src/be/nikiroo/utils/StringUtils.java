package be.nikiroo.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.unbescape.html.HtmlEscape;
import org.unbescape.html.HtmlEscapeLevel;
import org.unbescape.html.HtmlEscapeType;

import be.nikiroo.utils.streams.Base64InputStream;
import be.nikiroo.utils.streams.Base64OutputStream;

/**
 * This class offer some utilities based around {@link String}s.
 * 
 * @author niki
 */
public class StringUtils {
	/**
	 * This enum type will decide the alignment of a {@link String} when padding
	 * or justification is applied (if there is enough horizontal space for it
	 * to be aligned).
	 */
	public enum Alignment {
		/** Aligned at left. */
		LEFT,
		/** Centered. */
		CENTER,
		/** Aligned at right. */
		RIGHT,
		/** Full justified (to both left and right). */
		JUSTIFY,

		// Old Deprecated values:

		/** DEPRECATED: please use LEFT. */
		@Deprecated
		Beginning,
		/** DEPRECATED: please use CENTER. */
		@Deprecated
		Center,
		/** DEPRECATED: please use RIGHT. */
		@Deprecated
		End;

		/**
		 * Return the non-deprecated version of this enum if needed (or return
		 * self if not).
		 * 
		 * @return the non-deprecated value
		 */
		Alignment undeprecate() {
			if (this == Beginning)
				return LEFT;
			if (this == Center)
				return CENTER;
			if (this == End)
				return RIGHT;
			return this;
		}
	}

	static private Pattern marks = getMarks();

	/**
	 * Fix the size of the given {@link String} either with space-padding or by
	 * shortening it.
	 * 
	 * @param text
	 *            the {@link String} to fix
	 * @param width
	 *            the size of the resulting {@link String} or -1 for a noop
	 * 
	 * @return the resulting {@link String} of size <i>size</i>
	 */
	static public String padString(String text, int width) {
		return padString(text, width, true, null);
	}

	/**
	 * Fix the size of the given {@link String} either with space-padding or by
	 * optionally shortening it.
	 * 
	 * @param text
	 *            the {@link String} to fix
	 * @param width
	 *            the size of the resulting {@link String} if the text fits or
	 *            if cut is TRUE or -1 for a noop
	 * @param cut
	 *            cut the {@link String} shorter if needed
	 * @param align
	 *            align the {@link String} in this position if we have enough
	 *            space (default is Alignment.Beginning)
	 * 
	 * @return the resulting {@link String} of size <i>size</i> minimum
	 */
	static public String padString(String text, int width, boolean cut,
			Alignment align) {

		if (align == null) {
			align = Alignment.LEFT;
		}

		align = align.undeprecate();

		if (width >= 0) {
			if (text == null)
				text = "";

			int diff = width - text.length();

			if (diff < 0) {
				if (cut)
					text = text.substring(0, width);
			} else if (diff > 0) {
				if (diff < 2 && align != Alignment.RIGHT)
					align = Alignment.LEFT;

				switch (align) {
				case RIGHT:
					text = new String(new char[diff]).replace('\0', ' ') + text;
					break;
				case CENTER:
					int pad1 = (diff) / 2;
					int pad2 = (diff + 1) / 2;
					text = new String(new char[pad1]).replace('\0', ' ') + text
							+ new String(new char[pad2]).replace('\0', ' ');
					break;
				case LEFT:
				default:
					text = text + new String(new char[diff]).replace('\0', ' ');
					break;
				}
			}
		}

		return text;
	}

	/**
	 * Justify a text into width-sized (at the maximum) lines and return all the
	 * lines concatenated into a single '\\n'-separated line of text.
	 * 
	 * @param text
	 *            the {@link String} to justify
	 * @param width
	 *            the maximum size of the resulting lines
	 * 
	 * @return a list of justified text lines concatenated into a single
	 *         '\\n'-separated line of text
	 */
	static public String justifyTexts(String text, int width) {
		StringBuilder builder = new StringBuilder();
		for (String line : justifyText(text, width, null)) {
			if (builder.length() > 0) {
				builder.append('\n');
			}
			builder.append(line);
		}

		return builder.toString();
	}

	/**
	 * Justify a text into width-sized (at the maximum) lines.
	 * 
	 * @param text
	 *            the {@link String} to justify
	 * @param width
	 *            the maximum size of the resulting lines
	 * 
	 * @return a list of justified text lines
	 */
	static public List<String> justifyText(String text, int width) {
		return justifyText(text, width, null);
	}

	/**
	 * Justify a text into width-sized (at the maximum) lines.
	 * 
	 * @param text
	 *            the {@link String} to justify
	 * @param width
	 *            the maximum size of the resulting lines
	 * @param align
	 *            align the lines in this position (default is
	 *            Alignment.Beginning)
	 * 
	 * @return a list of justified text lines
	 */
	static public List<String> justifyText(String text, int width,
			Alignment align) {
		if (align == null) {
			align = Alignment.LEFT;
		}

		align = align.undeprecate();

		switch (align) {
		case CENTER:
			return StringJustifier.center(text, width);
		case RIGHT:
			return StringJustifier.right(text, width);
		case JUSTIFY:
			return StringJustifier.full(text, width);
		case LEFT:
		default:
			return StringJustifier.left(text, width);
		}
	}

	/**
	 * Justify a text into width-sized (at the maximum) lines.
	 * 
	 * @param text
	 *            the {@link String} to justify
	 * @param width
	 *            the maximum size of the resulting lines
	 * 
	 * @return a list of justified text lines
	 */
	static public List<String> justifyText(List<String> text, int width) {
		return justifyText(text, width, null);
	}

	/**
	 * Justify a text into width-sized (at the maximum) lines.
	 * 
	 * @param text
	 *            the {@link String} to justify
	 * @param width
	 *            the maximum size of the resulting lines
	 * @param align
	 *            align the lines in this position (default is
	 *            Alignment.Beginning)
	 * 
	 * @return a list of justified text lines
	 */
	static public List<String> justifyText(List<String> text, int width,
			Alignment align) {
		List<String> result = new ArrayList<String>();

		// Content <-> Bullet spacing (null = no spacing)
		List<Entry<String, String>> lines = new ArrayList<Entry<String, String>>();
		StringBuilder previous = null;
		StringBuilder tmp = new StringBuilder();
		String previousItemBulletSpacing = null;
		String itemBulletSpacing = null;
		for (String inputLine : text) {
			boolean previousLineComplete = true;

			String current = inputLine.replace("\t", "    ");
			itemBulletSpacing = getItemSpacing(current);
			boolean bullet = isItemLine(current);
			if ((previousItemBulletSpacing == null || itemBulletSpacing
					.length() <= previousItemBulletSpacing.length()) && !bullet) {
				itemBulletSpacing = null;
			}

			if (itemBulletSpacing != null) {
				current = current.trim();
				if (!current.isEmpty() && bullet) {
					current = current.substring(1);
				}
				current = current.trim();
				previousLineComplete = bullet;
			} else {
				tmp.setLength(0);
				for (String word : current.split(" ")) {
					if (word.isEmpty()) {
						continue;
					}

					if (tmp.length() > 0) {
						tmp.append(' ');
					}
					tmp.append(word.trim());
				}
				current = tmp.toString();

				previousLineComplete = current.isEmpty()
						|| previousItemBulletSpacing != null
						|| (previous != null && isFullLine(previous))
						|| isHrLine(current) || isHrLine(previous);
			}

			if (previous == null) {
				previous = new StringBuilder();
			} else {
				if (previousLineComplete) {
					lines.add(new AbstractMap.SimpleEntry<String, String>(
							previous.toString(), previousItemBulletSpacing));
					previous.setLength(0);
					previousItemBulletSpacing = itemBulletSpacing;
				} else {
					previous.append(' ');
				}
			}

			previous.append(current);

		}

		if (previous != null) {
			lines.add(new AbstractMap.SimpleEntry<String, String>(previous
					.toString(), previousItemBulletSpacing));
		}

		for (Entry<String, String> line : lines) {
			String content = line.getKey();
			String spacing = line.getValue();

			String bullet = "- ";
			if (spacing == null) {
				bullet = "";
				spacing = "";
			}

			if (spacing.length() > width + 3) {
				spacing = "";
			}

			for (String subline : StringUtils.justifyText(content, width
					- (spacing.length() + bullet.length()), align)) {
				result.add(spacing + bullet + subline);
				if (!bullet.isEmpty()) {
					bullet = "  ";
				}
			}
		}

		return result;
	}

	/**
	 * Sanitise the given input to make it more Terminal-friendly by removing
	 * combining characters.
	 * 
	 * @param input
	 *            the input to sanitise
	 * @param allowUnicode
	 *            allow Unicode or only allow ASCII Latin characters
	 * 
	 * @return the sanitised {@link String}
	 */
	static public String sanitize(String input, boolean allowUnicode) {
		return sanitize(input, allowUnicode, !allowUnicode);
	}

	/**
	 * Sanitise the given input to make it more Terminal-friendly by removing
	 * combining characters.
	 * 
	 * @param input
	 *            the input to sanitise
	 * @param allowUnicode
	 *            allow Unicode or only allow ASCII Latin characters
	 * @param removeAllAccents
	 *            TRUE to replace all accentuated characters by their non
	 *            accentuated counter-parts
	 * 
	 * @return the sanitised {@link String}
	 */
	static public String sanitize(String input, boolean allowUnicode,
			boolean removeAllAccents) {

		if (removeAllAccents) {
			input = Normalizer.normalize(input, Form.NFKD);
			if (marks != null) {
				input = marks.matcher(input).replaceAll("");
			}
		}

		input = Normalizer.normalize(input, Form.NFKC);

		if (!allowUnicode) {
			StringBuilder builder = new StringBuilder();
			for (int index = 0; index < input.length(); index++) {
				char car = input.charAt(index);
				// displayable chars in ASCII are in the range 32<->255,
				// except DEL (127)
				if (car >= 32 && car <= 255 && car != 127) {
					builder.append(car);
				}
			}
			input = builder.toString();
		}

		return input;
	}

	/**
	 * Convert between the time in milliseconds to a {@link String} in a "fixed"
	 * way (to exchange data over the wire, for instance).
	 * <p>
	 * Precise to the second.
	 * 
	 * @param time
	 *            the specified number of milliseconds since the standard base
	 *            time known as "the epoch", namely January 1, 1970, 00:00:00
	 *            GMT
	 * 
	 * @return the time as a {@link String}
	 */
	static public String fromTime(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date(time));
	}

	/**
	 * Convert between the time as a {@link String} to milliseconds in a "fixed"
	 * way (to exchange data over the wire, for instance).
	 * <p>
	 * Precise to the second.
	 * 
	 * @param displayTime
	 *            the time as a {@link String}
	 * 
	 * @return the number of milliseconds since the standard base time known as
	 *         "the epoch", namely January 1, 1970, 00:00:00 GMT, or -1 in case
	 *         of error
	 * 
	 * @throws ParseException
	 *             in case of parse error
	 */
	static public long toTime(String displayTime) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.parse(displayTime).getTime();
	}

	/**
	 * Return a hash of the given {@link String}.
	 * 
	 * @param input
	 *            the input data
	 * 
	 * @return the hash
	 */
	static public String getMd5Hash(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(getBytes(input));
			byte byteData[] = md.digest();

			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				String hex = Integer.toHexString(0xff & byteData[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}

			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			return input;
		}
	}

	/**
	 * Remove the HTML content from the given input, and un-html-ize the rest.
	 * 
	 * @param html
	 *            the HTML-encoded content
	 * 
	 * @return the HTML-free equivalent content
	 */
	public static String unhtml(String html) {
		StringBuilder builder = new StringBuilder();

		int inTag = 0;
		for (char car : html.toCharArray()) {
			if (car == '<') {
				inTag++;
			} else if (car == '>') {
				inTag--;
			} else if (inTag <= 0) {
				builder.append(car);
			}
		}

		char nbsp = ' '; // non-breakable space (a special char)
		char space = ' ';
		return HtmlEscape.unescapeHtml(builder.toString()).replace(nbsp, space);
	}

	/**
	 * Escape the given {@link String} so it can be used in XML, as content.
	 * 
	 * @param input
	 *            the input {@link String}
	 * 
	 * @return the escaped {@link String}
	 */
	public static String xmlEscape(String input) {
		if (input == null) {
			return "";
		}

		return HtmlEscape.escapeHtml(input,
				HtmlEscapeType.HTML4_NAMED_REFERENCES_DEFAULT_TO_HEXA,
				HtmlEscapeLevel.LEVEL_1_ONLY_MARKUP_SIGNIFICANT);
	}

	/**
	 * Escape the given {@link String} so it can be used in XML, as text content
	 * inside double-quotes.
	 * 
	 * @param input
	 *            the input {@link String}
	 * 
	 * @return the escaped {@link String}
	 */
	public static String xmlEscapeQuote(String input) {
		if (input == null) {
			return "";
		}

		return HtmlEscape.escapeHtml(input,
				HtmlEscapeType.HTML4_NAMED_REFERENCES_DEFAULT_TO_HEXA,
				HtmlEscapeLevel.LEVEL_1_ONLY_MARKUP_SIGNIFICANT);
	}

	/**
	 * Zip the data and then encode it into Base64.
	 * 
	 * @param data
	 *            the data
	 * 
	 * @return the Base64 zipped version
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static String zip64(String data) throws IOException {
		try {
			return zip64(getBytes(data));
		} catch (UnsupportedEncodingException e) {
			// All conforming JVM are required to support UTF-8
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Zip the data and then encode it into Base64.
	 * 
	 * @param data
	 *            the data
	 * 
	 * @return the Base64 zipped version
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static String zip64(byte[] data) throws IOException {
		// 1. compress
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			OutputStream out = new GZIPOutputStream(bout);
			try {
				out.write(data);
			} finally {
				out.close();
			}
		} finally {
			data = bout.toByteArray();
			bout.close();
		}

		// 2. base64
		InputStream in = new ByteArrayInputStream(data);
		try {
			in = new Base64InputStream(in, true);
			return new String(IOUtils.toByteArray(in), "UTF-8");
		} finally {
			in.close();
		}
	}

	/**
	 * Unconvert from Base64 then unzip the content, which is assumed to be a
	 * String.
	 * 
	 * @param data
	 *            the data in Base64 format
	 * 
	 * @return the raw data
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static String unzip64s(String data) throws IOException {
		return new String(unzip64(data), "UTF-8");
	}

	/**
	 * Unconvert from Base64 then unzip the content.
	 * 
	 * @param data
	 *            the data in Base64 format
	 * 
	 * @return the raw data
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static byte[] unzip64(String data) throws IOException {
		InputStream in = new Base64InputStream(new ByteArrayInputStream(
				getBytes(data)), false);
		try {
			in = new GZIPInputStream(in);
			return IOUtils.toByteArray(in);
		} finally {
			in.close();
		}
	}

	/**
	 * Convert the given data to Base64 format.
	 * 
	 * @param data
	 *            the data to convert
	 * 
	 * @return the Base64 {@link String} representation of the data
	 * 
	 * @throws IOException
	 *             in case of I/O errors
	 */
	public static String base64(String data) throws IOException {
		return base64(getBytes(data));
	}

	/**
	 * Convert the given data to Base64 format.
	 * 
	 * @param data
	 *            the data to convert
	 * 
	 * @return the Base64 {@link String} representation of the data
	 * 
	 * @throws IOException
	 *             in case of I/O errors
	 */
	public static String base64(byte[] data) throws IOException {
		Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(
				data), true);
		try {
			return new String(IOUtils.toByteArray(in), "UTF-8");
		} finally {
			in.close();
		}
	}

	/**
	 * Unconvert the given data from Base64 format back to a raw array of bytes.
	 * 
	 * @param data
	 *            the data to unconvert
	 * 
	 * @return the raw data represented by the given Base64 {@link String},
	 * 
	 * @throws IOException
	 *             in case of I/O errors
	 */
	public static byte[] unbase64(String data) throws IOException {
		Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(
				getBytes(data)), false);
		try {
			return IOUtils.toByteArray(in);
		} finally {
			in.close();
		}
	}

	/**
	 * Unonvert the given data from Base64 format back to a {@link String}.
	 * 
	 * @param data
	 *            the data to unconvert
	 * 
	 * @return the {@link String} represented by the given Base64 {@link String}
	 * 
	 * @throws IOException
	 *             in case of I/O errors
	 */
	public static String unbase64s(String data) throws IOException {
		return new String(unbase64(data), "UTF-8");
	}

	/**
	 * Return a display {@link String} for the given value, which can be
	 * suffixed with "k" or "M" depending upon the number, if it is big enough.
	 * <p>
	 * <p>
	 * Examples:
	 * <ul>
	 * <li><tt>8 765</tt> becomes "8 k"</li>
	 * <li><tt>998 765</tt> becomes "998 k"</li>
	 * <li><tt>12 987 364</tt> becomes "12 M"</li>
	 * <li><tt>5 534 333 221</tt> becomes "5 G"</li>
	 * </ul>
	 * 
	 * @param value
	 *            the value to convert
	 * 
	 * @return the display value
	 */
	public static String formatNumber(long value) {
		return formatNumber(value, 0);
	}

	/**
	 * Return a display {@link String} for the given value, which can be
	 * suffixed with "k" or "M" depending upon the number, if it is big enough.
	 * <p>
	 * Examples (assuming decimalPositions = 1):
	 * <ul>
	 * <li><tt>8 765</tt> becomes "8.7 k"</li>
	 * <li><tt>998 765</tt> becomes "998.7 k"</li>
	 * <li><tt>12 987 364</tt> becomes "12.9 M"</li>
	 * <li><tt>5 534 333 221</tt> becomes "5.5 G"</li>
	 * </ul>
	 * 
	 * @param value
	 *            the value to convert
	 * @param decimalPositions
	 *            the number of decimal positions to keep
	 * 
	 * @return the display value
	 */
	public static String formatNumber(long value, int decimalPositions) {
		long userValue = value;
		String suffix = " ";
		long mult = 1;

		if (value >= 1000000000l) {
			mult = 1000000000l;
			userValue = value / 1000000000l;
			suffix = " G";
		} else if (value >= 1000000l) {
			mult = 1000000l;
			userValue = value / 1000000l;
			suffix = " M";
		} else if (value >= 1000l) {
			mult = 1000l;
			userValue = value / 1000l;
			suffix = " k";
		}

		String deci = "";
		if (decimalPositions > 0) {
			deci = Long.toString(value % mult);
			int size = Long.toString(mult).length() - 1;
			while (deci.length() < size) {
				deci = "0" + deci;
			}

			deci = deci.substring(0, Math.min(decimalPositions, deci.length()));
			while (deci.length() < decimalPositions) {
				deci += "0";
			}

			deci = "." + deci;
		}

		return Long.toString(userValue) + deci + suffix;
	}

	/**
	 * The reverse operation to {@link StringUtils#formatNumber(long)}: it will
	 * read a "display" number that can contain a "M" or "k" suffix and return
	 * the full value.
	 * <p>
	 * Of course, the conversion to and from display form is lossy (example:
	 * <tt>6870</tt> to "6.5k" to <tt>6500</tt>).
	 * 
	 * @param value
	 *            the value in display form with possible "M" and "k" suffixes,
	 *            can be NULL
	 * 
	 * @return the value as a number, or 0 if not possible to convert
	 */
	public static long toNumber(String value) {
		return toNumber(value, 0l);
	}

	/**
	 * The reverse operation to {@link StringUtils#formatNumber(long)}: it will
	 * read a "display" number that can contain a "M" or "k" suffix and return
	 * the full value.
	 * <p>
	 * Of course, the conversion to and from display form is lossy (example:
	 * <tt>6870</tt> to "6.5k" to <tt>6500</tt>).
	 * 
	 * @param value
	 *            the value in display form with possible "M" and "k" suffixes,
	 *            can be NULL
	 * @param def
	 *            the default value if it is not possible to convert the given
	 *            value to a number
	 * 
	 * @return the value as a number, or 0 if not possible to convert
	 */
	public static long toNumber(String value, long def) {
		long count = def;
		if (value != null) {
			value = value.trim().toLowerCase();
			try {
				long mult = 1;
				if (value.endsWith("g")) {
					value = value.substring(0, value.length() - 1).trim();
					mult = 1000000000;
				} else if (value.endsWith("m")) {
					value = value.substring(0, value.length() - 1).trim();
					mult = 1000000;
				} else if (value.endsWith("k")) {
					value = value.substring(0, value.length() - 1).trim();
					mult = 1000;
				}

				long deci = 0;
				if (value.contains(".")) {
					String[] tab = value.split("\\.");
					if (tab.length != 2) {
						throw new NumberFormatException(value);
					}
					double decimal = Double.parseDouble("0."
							+ tab[tab.length - 1]);
					deci = ((long) (mult * decimal));
					value = tab[0];
				}
				count = mult * Long.parseLong(value) + deci;
			} catch (Exception e) {
			}
		}

		return count;
	}

	/**
	 * Return the bytes array representation of the given {@link String} in
	 * UTF-8.
	 * 
	 * @param str
	 *            the {@link String} to transform into bytes
	 * @return the content in bytes
	 */
	static public byte[] getBytes(String str) {
		try {
			return str.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// All conforming JVM must support UTF-8
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * The "remove accents" pattern.
	 * 
	 * @return the pattern, or NULL if a problem happens
	 */
	private static Pattern getMarks() {
		try {
			return Pattern
					.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
		} catch (Exception e) {
			// Can fail on Android...
			return null;
		}
	}

	//
	// justify List<String> related:
	//

	/**
	 * Check if this line ends as a complete line (ends with a "." or similar).
	 * <p>
	 * Note that we consider an empty line as full, and a line ending with
	 * spaces as not complete.
	 * 
	 * @param line
	 *            the line to check
	 * 
	 * @return TRUE if it does
	 */
	static private boolean isFullLine(StringBuilder line) {
		if (line.length() == 0) {
			return true;
		}

		char lastCar = line.charAt(line.length() - 1);
		switch (lastCar) {
		case '.': // points
		case '?':
		case '!':

		case '\'': // quotes
		case '‘':
		case '’':

		case '"': // double quotes
		case '”':
		case '“':
		case '»':
		case '«':
			return true;
		default:
			return false;
		}
	}

	/**
	 * Check if this line represent an item in a list or description (i.e.,
	 * check that the first non-space char is "-").
	 * 
	 * @param line
	 *            the line to check
	 * 
	 * @return TRUE if it is
	 */
	static private boolean isItemLine(String line) {
		String spacing = getItemSpacing(line);
		return spacing != null && !spacing.isEmpty()
				&& line.charAt(spacing.length()) == '-';
	}

	/**
	 * Return all the spaces that start this line (or Empty if none).
	 * 
	 * @param line
	 *            the line to get the starting spaces from
	 * 
	 * @return the left spacing
	 */
	static private String getItemSpacing(String line) {
		int i;
		for (i = 0; i < line.length(); i++) {
			if (line.charAt(i) != ' ') {
				return line.substring(0, i);
			}
		}

		return "";
	}

	/**
	 * This line is an horizontal spacer line.
	 * 
	 * @param line
	 *            the line to test
	 * 
	 * @return TRUE if it is
	 */
	static private boolean isHrLine(CharSequence line) {
		int count = 0;
		if (line != null) {
			for (int i = 0; i < line.length(); i++) {
				char car = line.charAt(i);
				if (car == ' ' || car == '\t' || car == '*' || car == '-'
						|| car == '_' || car == '~' || car == '=' || car == '/'
						|| car == '\\') {
					count++;
				} else {
					return false;
				}
			}
		}

		return count > 2;
	}

	// Deprecated functions, please do not use //

	/**
	 * @deprecated please use {@link StringUtils#zip64(byte[])} or
	 *             {@link StringUtils#base64(byte[])} instead.
	 * 
	 * @param data
	 *            the data to encode
	 * @param zip
	 *            TRUE to zip it before Base64 encoding it, FALSE for Base64
	 *            encoding only
	 * 
	 * @return the encoded data
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	@Deprecated
	public static String base64(String data, boolean zip) throws IOException {
		return base64(getBytes(data), zip);
	}

	/**
	 * @deprecated please use {@link StringUtils#zip64(String)} or
	 *             {@link StringUtils#base64(String)} instead.
	 * 
	 * @param data
	 *            the data to encode
	 * @param zip
	 *            TRUE to zip it before Base64 encoding it, FALSE for Base64
	 *            encoding only
	 * 
	 * @return the encoded data
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	@Deprecated
	public static String base64(byte[] data, boolean zip) throws IOException {
		if (zip) {
			return zip64(data);
		}

		Base64InputStream b64 = new Base64InputStream(new ByteArrayInputStream(
				data), true);
		try {
			return IOUtils.readSmallStream(b64);
		} finally {
			b64.close();
		}
	}

	/**
	 * @deprecated please use {@link Base64OutputStream} and
	 *             {@link GZIPOutputStream} instead.
	 * 
	 * @param breakLines
	 *            NOT USED ANYMORE, it is always considered FALSE now
	 */
	@Deprecated
	public static OutputStream base64(OutputStream data, boolean zip,
			boolean breakLines) throws IOException {
		OutputStream out = new Base64OutputStream(data);
		if (zip) {
			out = new java.util.zip.GZIPOutputStream(out);
		}

		return out;
	}

	/**
	 * Unconvert the given data from Base64 format back to a raw array of bytes.
	 * <p>
	 * Will automatically detect zipped data and also uncompress it before
	 * returning, unless ZIP is false.
	 * 
	 * @deprecated DO NOT USE ANYMORE (bad perf, will be dropped)
	 * 
	 * @param data
	 *            the data to unconvert
	 * @param zip
	 *            TRUE to also uncompress the data from a GZIP format
	 *            automatically; if set to FALSE, zipped data can be returned
	 * 
	 * @return the raw data represented by the given Base64 {@link String},
	 *         optionally compressed with GZIP
	 * 
	 * @throws IOException
	 *             in case of I/O errors
	 */
	@Deprecated
	public static byte[] unbase64(String data, boolean zip) throws IOException {
		byte[] buffer = unbase64(data);
		if (!zip) {
			return buffer;
		}

		try {
			GZIPInputStream zipped = new GZIPInputStream(
					new ByteArrayInputStream(buffer));
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				try {
					IOUtils.write(zipped, out);
					return out.toByteArray();
				} finally {
					out.close();
				}
			} finally {
				zipped.close();
			}
		} catch (Exception e) {
			return buffer;
		}
	}

	/**
	 * Unconvert the given data from Base64 format back to a raw array of bytes.
	 * <p>
	 * Will automatically detect zipped data and also uncompress it before
	 * returning, unless ZIP is false.
	 * 
	 * @deprecated DO NOT USE ANYMORE (bad perf, will be dropped)
	 * 
	 * @param data
	 *            the data to unconvert
	 * @param zip
	 *            TRUE to also uncompress the data from a GZIP format
	 *            automatically; if set to FALSE, zipped data can be returned
	 * 
	 * @return the raw data represented by the given Base64 {@link String},
	 *         optionally compressed with GZIP
	 * 
	 * @throws IOException
	 *             in case of I/O errors
	 */
	@Deprecated
	public static InputStream unbase64(InputStream data, boolean zip)
			throws IOException {
		return new ByteArrayInputStream(unbase64(IOUtils.readSmallStream(data),
				zip));
	}

	/**
	 * @deprecated DO NOT USE ANYMORE (bad perf, will be dropped)
	 */
	@Deprecated
	public static byte[] unbase64(byte[] data, int offset, int count,
			boolean zip) throws IOException {
		byte[] dataPart = Arrays.copyOfRange(data, offset, offset + count);
		return unbase64(new String(dataPart, "UTF-8"), zip);
	}

	/**
	 * @deprecated DO NOT USE ANYMORE (bad perf, will be dropped)
	 */
	@Deprecated
	public static String unbase64s(String data, boolean zip) throws IOException {
		return new String(unbase64(data, zip), "UTF-8");
	}

	/**
	 * @deprecated DO NOT USE ANYMORE (bad perf, will be dropped)
	 */
	@Deprecated
	public static String unbase64s(byte[] data, int offset, int count,
			boolean zip) throws IOException {
		return new String(unbase64(data, offset, count, zip), "UTF-8");
	}
}
