/*
 * This file was taken from:
 * Jexer - Java Text User Interface
 *
 * The MIT License (MIT)
 *
 * Copyright (C) 2017 Kevin Lamonte
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * @author Kevin Lamonte [kevin.lamonte@gmail.com]
 * @version 1
 * 
 * I added some changes to integrate it here.
 * @author Niki
 */
package be.nikiroo.utils;

import java.util.LinkedList;
import java.util.List;

/**
 * StringJustifier contains methods to convert one or more long lines of strings
 * into justified text paragraphs.
 */
class StringJustifier {
	/**
	 * Process the given text into a list of left-justified lines of a given
	 * max-width.
	 * 
	 * @param data
	 *            the text to justify
	 * @param width
	 *            the maximum width of a line
	 * 
	 * @return the list of justified lines
	 */
	static List<String> left(final String data, final int width) {
		return left(data, width, false);
	}

	/**
	 * Right-justify a string into a list of lines.
	 * 
	 * @param str
	 *            the string
	 * @param n
	 *            the maximum number of characters in a line
	 * @return the list of lines
	 */
	static List<String> right(final String str, final int n) {
		List<String> result = new LinkedList<String>();

		/*
		 * Same as left(), but preceed each line with spaces to make it n chars
		 * long.
		 */
		List<String> lines = left(str, n);
		for (String line : lines) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < n - line.length(); i++) {
				sb.append(' ');
			}
			sb.append(line);
			result.add(sb.toString());
		}

		return result;
	}

	/**
	 * Center a string into a list of lines.
	 * 
	 * @param str
	 *            the string
	 * @param n
	 *            the maximum number of characters in a line
	 * @return the list of lines
	 */
	static List<String> center(final String str, final int n) {
		List<String> result = new LinkedList<String>();

		/*
		 * Same as left(), but preceed/succeed each line with spaces to make it
		 * n chars long.
		 */
		List<String> lines = left(str, n);
		for (String line : lines) {
			StringBuilder sb = new StringBuilder();
			int l = (n - line.length()) / 2;
			int r = n - line.length() - l;
			for (int i = 0; i < l; i++) {
				sb.append(' ');
			}
			sb.append(line);
			for (int i = 0; i < r; i++) {
				sb.append(' ');
			}
			result.add(sb.toString());
		}

		return result;
	}

	/**
	 * Fully-justify a string into a list of lines.
	 * 
	 * @param str
	 *            the string
	 * @param n
	 *            the maximum number of characters in a line
	 * @return the list of lines
	 */
	static List<String> full(final String str, final int n) {
		List<String> result = new LinkedList<String>();

		/*
		 * Same as left(true), but insert spaces between words to make each line
		 * n chars long. The "algorithm" here is pretty dumb: it performs a
		 * split on space and then re-inserts multiples of n between words.
		 */
		List<String> lines = left(str, n, true);
		for (int lineI = 0; lineI < lines.size() - 1; lineI++) {
			String line = lines.get(lineI);
			String[] words = line.split(" ");
			if (words.length > 1) {
				int charCount = 0;
				for (int i = 0; i < words.length; i++) {
					charCount += words[i].length();
				}
				int spaceCount = n - charCount;
				int q = spaceCount / (words.length - 1);
				int r = spaceCount % (words.length - 1);
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < words.length - 1; i++) {
					sb.append(words[i]);
					for (int j = 0; j < q; j++) {
						sb.append(' ');
					}
					if (r > 0) {
						sb.append(' ');
						r--;
					}
				}
				for (int j = 0; j < r; j++) {
					sb.append(' ');
				}
				sb.append(words[words.length - 1]);
				result.add(sb.toString());
			} else {
				result.add(line);
			}
		}
		if (lines.size() > 0) {
			result.add(lines.get(lines.size() - 1));
		}

		return result;
	}

	/**
	 * Process the given text into a list of left-justified lines of a given
	 * max-width.
	 * 
	 * @param data
	 *            the text to justify
	 * @param width
	 *            the maximum width of a line
	 * @param minTwoWords
	 *            use 2 words per line minimum if the text allows it
	 * 
	 * @return the list of justified lines
	 */
	static private List<String> left(final String data, final int width,
			boolean minTwoWords) {
		List<String> lines = new LinkedList<String>();

		for (String dataLine : data.split("\n")) {
			String line = rightTrim(dataLine.replace("\t", "    "));

			if (width > 0 && line.length() > width) {
				while (line.length() > 0) {
					int i = Math.min(line.length(), width - 1); // -1 for "-"

					boolean needDash = true;
					// find the best space if any and if needed
					int prevSpace = 0;
					if (i < line.length()) {
						prevSpace = -1;
						int space = line.indexOf(' ');
						int numOfSpaces = 0;

						while (space > -1 && space <= i) {
							prevSpace = space;
							space = line.indexOf(' ', space + 1);
							numOfSpaces++;
						}

						if (prevSpace > 0 && (!minTwoWords || numOfSpaces >= 2)) {
							i = prevSpace;
							needDash = false;
						}
					}
					//

					// no dash before space/dash
					if ((i + 1) < line.length()) {
						char car = line.charAt(i);
						char nextCar = line.charAt(i + 1);
						if (car == ' ' || car == '-' || nextCar == ' ') {
							needDash = false;
						} else if (i > 0) {
							char prevCar = line.charAt(i - 1);
							if (prevCar == ' ' || prevCar == '-') {
								needDash = false;
								i--;
							}
						}
					}

					// if the space freed by the removed dash allows it, or if
					// it is the last char, add the next char
					if (!needDash || i >= line.length() - 1) {
						int checkI = Math.min(i + 1, line.length());
						if (checkI == i || checkI <= width) {
							needDash = false;
							i = checkI;
						}
					}

					// no dash before parenthesis (but cannot add one more
					// after)
					if ((i + 1) < line.length()) {
						char nextCar = line.charAt(i + 1);
						if (nextCar == '(' || nextCar == ')') {
							needDash = false;
						}
					}

					if (needDash) {
						lines.add(rightTrim(line.substring(0, i)) + "-");
					} else {
						lines.add(rightTrim(line.substring(0, i)));
					}

					// full trim (remove spaces when cutting)
					line = line.substring(i).trim();
				}
			} else {
				lines.add(line);
			}
		}

		return lines;
	}

	/**
	 * Trim the given {@link String} on the right only.
	 * 
	 * @param data
	 *            the source {@link String}
	 * @return the right-trimmed String or Empty if it was NULL
	 */
	static private String rightTrim(String data) {
		if (data == null)
			return "";

		return ("|" + data).trim().substring(1);
	}
}
