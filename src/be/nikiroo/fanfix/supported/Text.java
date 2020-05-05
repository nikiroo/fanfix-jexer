package be.nikiroo.fanfix.supported;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import org.jsoup.nodes.Document;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.Config;
import be.nikiroo.fanfix.data.Chapter;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.data.Paragraph;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.ImageUtils;
import be.nikiroo.utils.Progress;
import be.nikiroo.utils.streams.MarkableFileInputStream;

/**
 * Support class for local stories encoded in textual format, with a few rules:
 * <ul>
 * <li>The title must be on the first line</li>
 * <li>The author (preceded by nothing, "by " or "©") must be on the second
 * line, possibly with the publication date in parenthesis (i.e., "
 * <tt>By Unknown (3rd October 1998)</tt>")</li>
 * <li>Chapters must be declared with "<tt>Chapter x</tt>" or "
 * <tt>Chapter x: NAME OF THE CHAPTER</tt>", where "<tt>x</tt>" is the chapter
 * number</li>
 * <li>A description of the story must be given as chapter number 0</li>
 * <li>A cover may be present, with the same filename but a PNG, JPEG or JPG
 * extension</li>
 * </ul>
 * 
 * @author niki
 */
class Text extends BasicSupport {
	private File sourceFile;
	private InputStream in;

	protected File getSourceFile() {
		return sourceFile;
	}

	protected InputStream getInput() {
		if (in != null) {
			try {
				in.reset();
			} catch (IOException e) {
				Instance.getInstance().getTraceHandler().error(new IOException("Cannot reset the Text stream", e));
			}

			return in;
		}

		return null;
	}

	@Override
	protected boolean isHtml() {
		return false;
	}

	@Override
	protected Document loadDocument(URL source) throws IOException {
		try {
			sourceFile = new File(source.toURI());
			in = new MarkableFileInputStream(sourceFile);
		} catch (URISyntaxException e) {
			throw new IOException("Cannot load the text document: " + source);
		}

		return null;
	}

	@Override
	protected MetaData getMeta() throws IOException {
		MetaData meta = new MetaData();

		meta.setTitle(getTitle());
		meta.setAuthor(getAuthor());
		meta.setDate(bsHelper.formatDate(getDate()));
		meta.setTags(new ArrayList<String>());
		meta.setSource(getType().getSourceName());
		meta.setUrl(getSourceFile().toURI().toURL().toString());
		meta.setPublisher("");
		meta.setUuid(getSourceFile().toString());
		meta.setLuid("");
		meta.setLang(getLang()); // default is EN
		meta.setSubject(getSourceFile().getParentFile().getName());
		meta.setType(getType().toString());
		meta.setImageDocument(false);
		meta.setCover(getCover(getSourceFile()));
		
		return meta;
	}

	private String getLang() {
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(getInput(), "UTF-8");
		scan.useDelimiter("\\n");
		scan.next(); // Title
		scan.next(); // Author (Date)
		String chapter0 = scan.next(); // empty or Chapter 0
		while (chapter0.isEmpty()) {
			chapter0 = scan.next();
		}

		String lang = detectChapter(chapter0, 0);
		if (lang == null) {
			// No description??
			lang = detectChapter(chapter0, 1);
		}

		if (lang == null) {
			lang = "en";
		} else {
			lang = lang.toLowerCase();
		}

		return lang;
	}

	private String getTitle() {
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(getInput(), "UTF-8");
		scan.useDelimiter("\\n");
		return scan.next();
	}

	private String getAuthor() {
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(getInput(), "UTF-8");
		scan.useDelimiter("\\n");
		scan.next();
		String authorDate = scan.next();

		String author = authorDate;
		int pos = authorDate.indexOf('(');
		if (pos >= 0) {
			author = authorDate.substring(0, pos);
		}

		return bsHelper.fixAuthor(author);
	}

	private String getDate() {
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(getInput(), "UTF-8");
		scan.useDelimiter("\\n");
		scan.next();
		String authorDate = scan.next();

		String date = "";
		int pos = authorDate.indexOf('(');
		if (pos >= 0) {
			date = authorDate.substring(pos + 1).trim();
			pos = date.lastIndexOf(')');
			if (pos >= 0) {
				date = date.substring(0, pos).trim();
			}
		}

		return date;
	}

	@Override
	protected String getDesc() throws IOException {
		String content = getChapterContent(null, 0, null).trim();
		if (!content.isEmpty()) {
			Chapter desc = bsPara.makeChapter(this, null, 0, "Description",
					content, isHtml(), null);
			StringBuilder builder = new StringBuilder();
			for (Paragraph para : desc) {
				if (builder.length() > 0) {
					builder.append("\n");
				}
				builder.append(para.getContent());
			}
		}

		return content;
	}

	protected Image getCover(File sourceFile) {
		String path = sourceFile.getName();

		for (String ext : new String[] { ".txt", ".text", ".story" }) {
			if (path.endsWith(ext)) {
				path = path.substring(0, path.length() - ext.length());
			}
		}

		Image cover = bsImages.getImage(this, sourceFile.getParentFile(), path);
		if (cover != null) {
			try {
				File tmp = Instance.getInstance().getTempFiles().createTempFile("test_cover_image");
				ImageUtils.getInstance().saveAsImage(cover, tmp, "png");
				tmp.delete();
			} catch (IOException e) {
				cover = null;
			}
		}

		return cover;
	}

	@Override
	protected List<Entry<String, URL>> getChapters(Progress pg)
			throws IOException {
		List<Entry<String, URL>> chaps = new ArrayList<Entry<String, URL>>();
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(getInput(), "UTF-8");
		scan.useDelimiter("\\n");
		boolean prevLineEmpty = false;
		while (scan.hasNext()) {
			String line = scan.next();
			if (prevLineEmpty && detectChapter(line, chaps.size() + 1) != null) {
				String chapName = Integer.toString(chaps.size() + 1);
				int pos = line.indexOf(':');
				if (pos >= 0 && pos + 1 < line.length()) {
					chapName = line.substring(pos + 1).trim();
				}

				chaps.add(new AbstractMap.SimpleEntry<String, URL>(//
						chapName, //
						getSourceFile().toURI().toURL()));
			}

			prevLineEmpty = line.trim().isEmpty();
		}

		return chaps;
	}

	@Override
	protected String getChapterContent(URL source, int number, Progress pg)
			throws IOException {
		StringBuilder builder = new StringBuilder();
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(getInput(), "UTF-8");
		scan.useDelimiter("\\n");
		boolean inChap = false;
		while (scan.hasNext()) {
			String line = scan.next();
			if (!inChap && detectChapter(line, number) != null) {
				inChap = true;
			} else if (detectChapter(line, number + 1) != null) {
				break;
			} else if (inChap) {
				builder.append(line);
				builder.append("\n");
			}
		}

		return builder.toString();
	}

	@Override
	protected void close() {
		InputStream in = getInput();
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				Instance.getInstance().getTraceHandler()
						.error(new IOException("Cannot close the text source file input", e));
			}
		}

		super.close();
	}

	@Override
	protected boolean supports(URL url) {
		return supports(url, false);
	}

	/**
	 * Check if we supports this {@link URL}, that is, if the info file can be
	 * found OR not found.
	 * <p>
	 * It must also be a file, not another kind of URL.
	 * 
	 * @param url
	 *            the {@link URL} to check
	 * @param info
	 *            TRUE to require the info file, FALSE to forbid the info file
	 * 
	 * @return TRUE if it is supported
	 */
	protected boolean supports(URL url, boolean info) {
		if (!"file".equals(url.getProtocol())) {
			return false;
		}

		boolean infoPresent = false;
		File file;
		try {
			file = new File(url.toURI());
			file = assureNoTxt(file);
			file = new File(file.getPath() + ".info");
		} catch (URISyntaxException e) {
			Instance.getInstance().getTraceHandler().error(e);
			file = null;
		}

		infoPresent = (file != null && file.exists());

		return infoPresent == info;
	}

	/**
	 * Remove the ".txt" (or ".text") extension if it is present.
	 * 
	 * @param file
	 *            the file to process
	 * 
	 * @return the same file or a copy of it without the ".txt" extension if it
	 *         was present
	 */
	protected File assureNoTxt(File file) {
		for (String ext : new String[] { ".txt", ".text" }) {
			if (file.getName().endsWith(ext)) {
				file = new File(file.getPath().substring(0,
						file.getPath().length() - ext.length()));
			}
		}

		return file;
	}

	/**
	 * Check if the given line looks like the given starting chapter in a
	 * supported language, and return the language if it does (or NULL if not).
	 * 
	 * @param line
	 *            the line to check
	 * @param number
	 *            the specific chapter number to check for
	 * 
	 * @return the language or NULL
	 */
	static private String detectChapter(String line, int number) {
		line = line.toUpperCase();
		for (String lang : Instance.getInstance().getConfig().getList(Config.CONF_CHAPTER)) {
			String chapter = Instance.getInstance().getConfig().getStringX(Config.CONF_CHAPTER, lang);
			if (chapter != null && !chapter.isEmpty()) {
				chapter = chapter.toUpperCase() + " ";
				if (line.startsWith(chapter)) {
					// We want "[CHAPTER] [number]: [name]", with ": [name]"
					// optional
					String test = line.substring(chapter.length()).trim();

					String possibleNum = test.trim();
					if (possibleNum.indexOf(':') > 0) {
						possibleNum = possibleNum.substring(0,
								possibleNum.indexOf(':')).trim();
					}

					if (test.startsWith(Integer.toString(number))) {
						test = test
								.substring(Integer.toString(number).length())
								.trim();
						if (test.isEmpty() || test.startsWith(":")) {
							return lang;
						}
					}
				}
			}
		}

		return null;
	}
}
