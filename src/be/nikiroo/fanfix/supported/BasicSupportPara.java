package be.nikiroo.fanfix.supported;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.Config;
import be.nikiroo.fanfix.bundles.StringId;
import be.nikiroo.fanfix.data.Chapter;
import be.nikiroo.fanfix.data.Paragraph;
import be.nikiroo.fanfix.data.Paragraph.ParagraphType;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.Progress;
import be.nikiroo.utils.StringUtils;

/**
 * Helper class for {@link BasicSupport}, mostly dedicated to {@link Paragraph}
 * and text formating for the {@link BasicSupport} class.
 * 
 * @author niki
 */
public class BasicSupportPara {
	// quote chars
	private static char openQuote = Instance.getInstance().getTrans().getCharacter(StringId.OPEN_SINGLE_QUOTE);
	private static char closeQuote = Instance.getInstance().getTrans().getCharacter(StringId.CLOSE_SINGLE_QUOTE);
	private static char openDoubleQuote = Instance.getInstance().getTrans().getCharacter(StringId.OPEN_DOUBLE_QUOTE);
	private static char closeDoubleQuote = Instance.getInstance().getTrans().getCharacter(StringId.CLOSE_DOUBLE_QUOTE);

	// used by this class:
	BasicSupportHelper bsHelper;
	BasicSupportImages bsImages;
	
	public BasicSupportPara(BasicSupportHelper bsHelper, BasicSupportImages bsImages) {
		this.bsHelper = bsHelper;
		this.bsImages = bsImages;
	}
	
	/**
	 * Create a {@link Chapter} object from the given information, formatting
	 * the content as it should be.
	 * 
	 * @param support
	 *            the linked {@link BasicSupport}
	 * @param source
	 *            the source of the story (for image lookup in the same path if
	 *            the source is a file, can be NULL)
	 * @param number
	 *            the chapter number
	 * @param name
	 *            the chapter name
	 * @param content
	 *            the chapter content
	 * @param pg
	 *            the optional progress reporter
	 * @param html
	 *            TRUE if the input content is in HTML mode
	 * 
	 * @return the {@link Chapter}
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public Chapter makeChapter(BasicSupport support, URL source,
			int number, String name, String content, boolean html, Progress pg)
			throws IOException {
		// Chapter name: process it correctly, then remove the possible
		// redundant "Chapter x: " in front of it, or "-" (as in
		// "Chapter 5: - Fun!" after the ": " was automatically added)
		String chapterName = processPara(name, false)
				.getContent().trim();
		for (String lang : Instance.getInstance().getConfig().getList(Config.CONF_CHAPTER)) {
			String chapterWord = Instance.getInstance().getConfig().getStringX(Config.CONF_CHAPTER, lang);
			if (chapterName.startsWith(chapterWord)) {
				chapterName = chapterName.substring(chapterWord.length())
						.trim();
				break;
			}
		}

		if (chapterName.startsWith(Integer.toString(number))) {
			chapterName = chapterName.substring(
					Integer.toString(number).length()).trim();
		}

		while (chapterName.startsWith(":") || chapterName.startsWith("-")) {
			chapterName = chapterName.substring(1).trim();
		}
		//

		Chapter chap = new Chapter(number, chapterName);

		if (content != null) {
			List<Paragraph> paras = makeParagraphs(support, source, content,
					html, pg);
			long words = 0;
			for (Paragraph para : paras) {
				words += para.getWords();
			}
			chap.setParagraphs(paras);
			chap.setWords(words);
		}

		return chap;
	}

	/**
	 * Check quotes for bad format (i.e., quotes with normal paragraphs inside)
	 * and requotify them (i.e., separate them into QUOTE paragraphs and other
	 * paragraphs (quotes or not)).
	 * 
	 * @param para
	 *            the paragraph to requotify (not necessarily a quote)
	 * @param html
	 *            TRUE if the input content is in HTML mode
	 * 
	 * @return the correctly (or so we hope) quotified paragraphs
	 */
	protected List<Paragraph> requotify(Paragraph para, boolean html) {
		List<Paragraph> newParas = new ArrayList<Paragraph>();

		if (para.getType() == ParagraphType.QUOTE
				&& para.getContent().length() > 2) {
			String line = para.getContent();
			boolean singleQ = line.startsWith("" + openQuote);
			boolean doubleQ = line.startsWith("" + openDoubleQuote);

			// Do not try when more than one quote at a time
			// (some stories are not easily readable if we do)
			if (singleQ
					&& line.indexOf(closeQuote, 1) < line
							.lastIndexOf(closeQuote)) {
				newParas.add(para);
				return newParas;
			}
			if (doubleQ
					&& line.indexOf(closeDoubleQuote, 1) < line
							.lastIndexOf(closeDoubleQuote)) {
				newParas.add(para);
				return newParas;
			}
			//

			if (!singleQ && !doubleQ) {
				line = openDoubleQuote + line + closeDoubleQuote;
				newParas.add(new Paragraph(ParagraphType.QUOTE, line, para
						.getWords()));
			} else {
				char open = singleQ ? openQuote : openDoubleQuote;
				char close = singleQ ? closeQuote : closeDoubleQuote;

				int posDot = -1;
				boolean inQuote = false;
				int i = 0;
				for (char car : line.toCharArray()) {
					if (car == open) {
						inQuote = true;
					} else if (car == close) {
						inQuote = false;
					} else if (car == '.' && !inQuote) {
						posDot = i;
						break;
					}
					i++;
				}

				if (posDot >= 0) {
					String rest = line.substring(posDot + 1).trim();
					line = line.substring(0, posDot + 1).trim();
					long words = 1;
					for (char car : line.toCharArray()) {
						if (car == ' ') {
							words++;
						}
					}
					newParas.add(new Paragraph(ParagraphType.QUOTE, line, words));
					if (!rest.isEmpty()) {
						newParas.addAll(requotify(processPara(rest, html), html));
					}
				} else {
					newParas.add(para);
				}
			}
		} else {
			newParas.add(para);
		}

		return newParas;
	}

	/**
	 * Process a {@link Paragraph} from a raw line of text.
	 * <p>
	 * Will also fix quotes and HTML encoding if needed.
	 * 
	 * @param line
	 *            the raw line
	 * @param html
	 *            TRUE if the input content is in HTML mode
	 * 
	 * @return the processed {@link Paragraph}
	 */
	protected Paragraph processPara(String line, boolean html) {
		if (html) {
			line = StringUtils.unhtml(line).trim();
		}
		boolean space = true;
		boolean brk = true;
		boolean quote = false;
		boolean tentativeCloseQuote = false;
		char prev = '\0';
		int dashCount = 0;
		long words = 1;

		StringBuilder builder = new StringBuilder();
		for (char car : line.toCharArray()) {
			if (car != '-') {
				if (dashCount > 0) {
					// dash, ndash and mdash: - – —
					// currently: always use mdash
					builder.append(dashCount == 1 ? '-' : '—');
				}
				dashCount = 0;
			}

			if (tentativeCloseQuote) {
				tentativeCloseQuote = false;
				if (Character.isLetterOrDigit(car)) {
					builder.append("'");
				} else {
					// handle double-single quotes as double quotes
					if (prev == car) {
						builder.append(closeDoubleQuote);
						continue;
					}

					builder.append(closeQuote);
				}
			}

			switch (car) {
			case ' ': // note: unbreakable space
			case ' ':
			case '\t':
			case '\n': // just in case
			case '\r': // just in case
				if (builder.length() > 0
						&& builder.charAt(builder.length() - 1) != ' ') {
					words++;
				}
				builder.append(' ');
				break;

			case '\'':
				if (space || (brk && quote)) {
					quote = true;
					// handle double-single quotes as double quotes
					if (prev == car) {
						builder.deleteCharAt(builder.length() - 1);
						builder.append(openDoubleQuote);
					} else {
						builder.append(openQuote);
					}
				} else if (prev == ' ' || prev == car) {
					// handle double-single quotes as double quotes
					if (prev == car) {
						builder.deleteCharAt(builder.length() - 1);
						builder.append(openDoubleQuote);
					} else {
						builder.append(openQuote);
					}
				} else {
					// it is a quote ("I'm off") or a 'quote' ("This
					// 'good' restaurant"...)
					tentativeCloseQuote = true;
				}
				break;

			case '"':
				if (space || (brk && quote)) {
					quote = true;
					builder.append(openDoubleQuote);
				} else if (prev == ' ') {
					builder.append(openDoubleQuote);
				} else {
					builder.append(closeDoubleQuote);
				}
				break;

			case '-':
				if (space) {
					quote = true;
				} else {
					dashCount++;
				}
				space = false;
				break;

			case '*':
			case '~':
			case '/':
			case '\\':
			case '<':
			case '>':
			case '=':
			case '+':
			case '_':
			case '–':
			case '—':
				space = false;
				builder.append(car);
				break;

			case '‘':
			case '`':
			case '‹':
			case '﹁':
			case '〈':
			case '「':
				if (space || (brk && quote)) {
					quote = true;
					builder.append(openQuote);
				} else {
					// handle double-single quotes as double quotes
					if (prev == car) {
						builder.deleteCharAt(builder.length() - 1);
						builder.append(openDoubleQuote);
					} else {
						builder.append(openQuote);
					}
				}
				space = false;
				brk = false;
				break;

			case '’':
			case '›':
			case '﹂':
			case '〉':
			case '」':
				space = false;
				brk = false;
				// handle double-single quotes as double quotes
				if (prev == car) {
					builder.deleteCharAt(builder.length() - 1);
					builder.append(closeDoubleQuote);
				} else {
					builder.append(closeQuote);
				}
				break;

			case '«':
			case '“':
			case '﹃':
			case '《':
			case '『':
				if (space || (brk && quote)) {
					quote = true;
					builder.append(openDoubleQuote);
				} else {
					builder.append(openDoubleQuote);
				}
				space = false;
				brk = false;
				break;

			case '»':
			case '”':
			case '﹄':
			case '》':
			case '』':
				space = false;
				brk = false;
				builder.append(closeDoubleQuote);
				break;

			default:
				space = false;
				brk = false;
				builder.append(car);
				break;
			}

			prev = car;
		}

		if (tentativeCloseQuote) {
			tentativeCloseQuote = false;
			builder.append(closeQuote);
		}

		line = builder.toString().trim();

		ParagraphType type = ParagraphType.NORMAL;
		if (space) {
			type = ParagraphType.BLANK;
		} else if (brk) {
			type = ParagraphType.BREAK;
		} else if (quote) {
			type = ParagraphType.QUOTE;
		}

		return new Paragraph(type, line, words);
	}

	/**
	 * Convert the given content into {@link Paragraph}s.
	 * 
	 * @param support
	 *            the linked {@link BasicSupport} (can be NULL), used to
	 *            download optional image content in []
	 * @param source
	 *            the source URL of the story (for image lookup in the same path
	 *            if the source is a file, can be NULL)
	 * @param content
	 *            the textual content
	 * @param html
	 *            TRUE if the input content is in HTML mode
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @return the {@link Paragraph}s
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	protected List<Paragraph> makeParagraphs(BasicSupport support,
			URL source, String content, boolean html, Progress pg)
			throws IOException {
		if (pg == null) {
			pg = new Progress();
		}

		if (html) {
			// Special <HR> processing:
			content = content.replaceAll("(<hr [^>]*>)|(<hr/>)|(<hr>)",
					"<br/>* * *<br/>");
		}

		List<Paragraph> paras = new ArrayList<Paragraph>();

		if (content != null && !content.trim().isEmpty()) {
			if (html) {
				String[] tab = content.split("(<p>|</p>|<br>|<br/>)");
				pg.setMinMax(0, tab.length);
				int i = 1;
				for (String line : tab) {
					if (line.startsWith("[") && line.endsWith("]")) {
						pg.setName("Extracting image " + i);
					}
					paras.add(makeParagraph(support, source, line.trim(), html));
					pg.setProgress(i++);
				}
			} else {
				List<String> lines = new ArrayList<String>();
				BufferedReader buff = null;
				try {
					buff = new BufferedReader(
							new InputStreamReader(new ByteArrayInputStream(
									content.getBytes("UTF-8")), "UTF-8"));
					for (String line = buff.readLine(); line != null; line = buff
							.readLine()) {
						lines.add(line.trim());
					}
				} finally {
					if (buff != null) {
						buff.close();
					}
				}

				pg.setMinMax(0, lines.size());
				int i = 0;
				for (String line : lines) {
					if (line.startsWith("[") && line.endsWith("]")) {
						pg.setName("Extracting image " + i);
					}
					paras.add(makeParagraph(support, source, line, html));
					pg.setProgress(i++);
				}
			}

			pg.done();
			pg.setName(null);

			// Check quotes for "bad" format
			List<Paragraph> newParas = new ArrayList<Paragraph>();
			for (Paragraph para : paras) {
				newParas.addAll(requotify(para, html));
			}
			paras = newParas;

			// Remove double blanks/brks
			fixBlanksBreaks(paras);
		}

		return paras;
	}

	/**
	 * Convert the given line into a single {@link Paragraph}.
	 * 
	 * @param support
	 *            the linked {@link BasicSupport} (can be NULL), used to
	 *            download optional image content in []
	 * @param source
	 *            the source URL of the story (for image lookup in the same path
	 *            if the source is a file, can be NULL)
	 * @param line
	 *            the textual content of the paragraph
	 * @param html
	 *            TRUE if the input content is in HTML mode
	 * 
	 * @return the {@link Paragraph}
	 */
	protected Paragraph makeParagraph(BasicSupport support, URL source,
			String line, boolean html) {
		Image image = null;
		if (line.startsWith("[") && line.endsWith("]")) {
			image = bsHelper.getImage(support, source, line
					.substring(1, line.length() - 1).trim());
		}

		if (image != null) {
			return new Paragraph(image);
		}

		return processPara(line, html);
	}

	/**
	 * Fix the {@link ParagraphType#BLANK}s and {@link ParagraphType#BREAK}s of
	 * those {@link Paragraph}s.
	 * <p>
	 * The resulting list will not contain a starting or trailing blank/break
	 * nor 2 blanks or breaks following each other.
	 * 
	 * @param paras
	 *            the list of {@link Paragraph}s to fix
	 */
	protected void fixBlanksBreaks(List<Paragraph> paras) {
		boolean space = false;
		boolean brk = true;
		for (int i = 0; i < paras.size(); i++) {
			Paragraph para = paras.get(i);
			boolean thisSpace = para.getType() == ParagraphType.BLANK;
			boolean thisBrk = para.getType() == ParagraphType.BREAK;

			if (i > 0 && space && thisBrk) {
				paras.remove(i - 1);
				i--;
			} else if ((space || brk) && (thisSpace || thisBrk)) {
				paras.remove(i);
				i--;
			}

			space = thisSpace;
			brk = thisBrk;
		}

		// Remove blank/brk at start
		if (paras.size() > 0
				&& (paras.get(0).getType() == ParagraphType.BLANK || paras.get(
						0).getType() == ParagraphType.BREAK)) {
			paras.remove(0);
		}

		// Remove blank/brk at end
		int last = paras.size() - 1;
		if (paras.size() > 0
				&& (paras.get(last).getType() == ParagraphType.BLANK || paras
						.get(last).getType() == ParagraphType.BREAK)) {
			paras.remove(last);
		}
	}
}
