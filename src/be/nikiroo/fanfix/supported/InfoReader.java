package be.nikiroo.fanfix.supported;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.Config;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.streams.MarkableFileInputStream;

// not complete: no "description" tag
public class InfoReader {
	static protected BasicSupportHelper bsHelper = new BasicSupportHelper();
	// static protected BasicSupportImages bsImages = new BasicSupportImages();
	// static protected BasicSupportPara bsPara = new BasicSupportPara(new
	// BasicSupportHelper(), new BasicSupportImages());

	public static MetaData readMeta(File infoFile, boolean withCover)
			throws IOException {
		if (infoFile == null) {
			throw new IOException("File is null");
		}

		if (infoFile.exists()) {
			InputStream in = new MarkableFileInputStream(infoFile);
			try {
				MetaData meta = createMeta(infoFile.toURI().toURL(), in,
						withCover);

				// Some old .info files were using UUID for URL...
				if (!hasIt(meta.getUrl()) && meta.getUuid() != null
						&& (meta.getUuid().startsWith("http://")
								|| meta.getUuid().startsWith("https://"))) {
					meta.setUrl(meta.getUuid());
				}

				// Some old .info files don't have those now required fields...
				// So we check if we can find the info in another way (many
				// formats have a copy of the original text file)
				if (!hasIt(meta.getTitle(), meta.getAuthor(), meta.getDate(),
						meta.getUrl())) {

					// TODO: not nice, would be better to do it properly...

					String base = infoFile.getPath();
					if (base.endsWith(".info")) {
						base = base.substring(0,
								base.length() - ".info".length());
					}
					File textFile = new File(base);
					if (!textFile.exists()) {
						textFile = new File(base + ".txt");
					}
					if (!textFile.exists()) {
						textFile = new File(base + ".text");
					}

					if (textFile.exists()) {
						final URL source = textFile.toURI().toURL();
						final MetaData[] superMetaA = new MetaData[1];
						@SuppressWarnings("unused")
						Text unused = new Text() {
							private boolean loaded = loadDocument();

							@Override
							public SupportType getType() {
								return SupportType.TEXT;
							}

							protected boolean loadDocument()
									throws IOException {
								loadDocument(source);
								superMetaA[0] = getMeta();
								return true;
							}

							@Override
							protected Image getCover(File sourceFile) {
								return null;
							}
						};

						MetaData superMeta = superMetaA[0];
						if (!hasIt(meta.getTitle())) {
							meta.setTitle(superMeta.getTitle());
						}
						if (!hasIt(meta.getAuthor())) {
							meta.setAuthor(superMeta.getAuthor());
						}
						if (!hasIt(meta.getDate())) {
							meta.setDate(superMeta.getDate());
						}
						if (!hasIt(meta.getUrl())) {
							meta.setUrl(superMeta.getUrl());
						}
					}
				}

				return meta;
			} finally {
				in.close();
			}
		}

		throw new FileNotFoundException(
				"File given as argument does not exists: "
						+ infoFile.getAbsolutePath());
	}

	/**
	 * Check if we have non-empty values for all the given {@link String}s.
	 * 
	 * @param values
	 *            the values to check
	 * 
	 * @return TRUE if none of them was NULL or empty
	 */
	static private boolean hasIt(String... values) {
		for (String value : values) {
			if (value == null || value.trim().isEmpty()) {
				return false;
			}
		}

		return true;
	}

	private static MetaData createMeta(URL sourceInfoFile, InputStream in,
			boolean withCover) throws IOException {
		MetaData meta = new MetaData();

		meta.setTitle(getInfoTag(in, "TITLE"));
		meta.setAuthor(getInfoTag(in, "AUTHOR"));
		meta.setDate(bsHelper.formatDate(getInfoTag(in, "DATE")));
		meta.setTags(getInfoTagList(in, "TAGS", ","));
		meta.setSource(getInfoTag(in, "SOURCE"));
		meta.setUrl(getInfoTag(in, "URL"));
		meta.setPublisher(getInfoTag(in, "PUBLISHER"));
		meta.setUuid(getInfoTag(in, "UUID"));
		meta.setLuid(getInfoTag(in, "LUID"));
		meta.setLang(getInfoTag(in, "LANG"));
		meta.setSubject(getInfoTag(in, "SUBJECT"));
		meta.setType(getInfoTag(in, "TYPE"));
		meta.setImageDocument(getInfoTagBoolean(in, "IMAGES_DOCUMENT", false));
		if (withCover) {
			String infoTag = getInfoTag(in, "COVER");
			if (infoTag != null && !infoTag.trim().isEmpty()) {
				meta.setCover(bsHelper.getImage(null, sourceInfoFile, infoTag));
			}
			if (meta.getCover() == null) {
				// Second chance: try to check for a cover next to the info file
				meta.setCover(getCoverByName(sourceInfoFile));
			}
		}
		try {
			meta.setWords(Long.parseLong(getInfoTag(in, "WORDCOUNT")));
		} catch (NumberFormatException e) {
			meta.setWords(0);
		}
		meta.setCreationDate(
				bsHelper.formatDate(getInfoTag(in, "CREATION_DATE")));
		meta.setFakeCover(Boolean.parseBoolean(getInfoTag(in, "FAKE_COVER")));

		if (withCover && meta.getCover() == null) {
			meta.setCover(bsHelper.getDefaultCover(meta.getSubject()));
		}

		return meta;
	}

	/**
	 * Return the cover image if it is next to the source file.
	 * 
	 * @param sourceInfoFile
	 *            the source file
	 * 
	 * @return the cover if present, NULL if not
	 */
	public static Image getCoverByName(URL sourceInfoFile) {
		Image cover = null;

		File basefile = new File(sourceInfoFile.getFile());

		String ext = "." + Instance.getInstance().getConfig()
				.getString(Config.FILE_FORMAT_IMAGE_FORMAT_COVER).toLowerCase();

		// Without removing ext
		cover = bsHelper.getImage(null, sourceInfoFile,
				basefile.getAbsolutePath() + ext);

		// Try without ext
		String name = basefile.getName();
		int pos = name.lastIndexOf(".");
		if (cover == null && pos > 0) {
			name = name.substring(0, pos);
			basefile = new File(basefile.getParent(), name);

			cover = bsHelper.getImage(null, sourceInfoFile,
					basefile.getAbsolutePath() + ext);
		}

		return cover;
	}

	private static boolean getInfoTagBoolean(InputStream in, String key,
			boolean def) throws IOException {
		Boolean value = getInfoTagBoolean(in, key);
		return value == null ? def : value;
	}

	private static Boolean getInfoTagBoolean(InputStream in, String key)
			throws IOException {
		String value = getInfoTag(in, key);
		if (value != null && !value.trim().isEmpty()) {
			value = value.toLowerCase().trim();
			return value.equals("1") || value.equals("on")
					|| value.equals("true") || value.equals("yes");
		}

		return null;
	}

	private static List<String> getInfoTagList(InputStream in, String key,
			String separator) throws IOException {
		List<String> list = new ArrayList<String>();
		String tt = getInfoTag(in, key);
		if (tt != null) {
			for (String tag : tt.split(separator)) {
				list.add(tag.trim());
			}
		}

		return list;
	}

	/**
	 * Return the value of the given tag in the <tt>.info</tt> file if present.
	 * 
	 * @param key
	 *            the tag key
	 * 
	 * @return the value or NULL
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	private static String getInfoTag(InputStream in, String key)
			throws IOException {
		key = "^" + key + "=";

		if (in != null) {
			in.reset();
			String value = getLine(in, key, 0);
			if (value != null && !value.isEmpty()) {
				value = value.trim().substring(key.length() - 1).trim();
				if (value.length() > 1 && //
						(value.startsWith("'") && value.endsWith("'")
								|| value.startsWith("\"")
										&& value.endsWith("\""))) {
					value = value.substring(1, value.length() - 1).trim();
				}

				// Some old files ended up with TITLE="'xxxxx'"
				if ("^TITLE=".equals(key)) {
					if (value.startsWith("'") && value.endsWith("'")
							&& value.length() > 1) {
						value = value.substring(1, value.length() - 1).trim();
					}
				}

				return value;
			}
		}

		return null;
	}

	/**
	 * Return the first line from the given input which correspond to the given
	 * selectors.
	 * 
	 * @param in
	 *            the input
	 * @param needle
	 *            a string that must be found inside the target line (also
	 *            supports "^" at start to say "only if it starts with" the
	 *            needle)
	 * @param relativeLine
	 *            the line to return based upon the target line position (-1 =
	 *            the line before, 0 = the target line...)
	 * 
	 * @return the line
	 */
	static private String getLine(InputStream in, String needle,
			int relativeLine) {
		return getLine(in, needle, relativeLine, true);
	}

	/**
	 * Return a line from the given input which correspond to the given
	 * selectors.
	 * 
	 * @param in
	 *            the input
	 * @param needle
	 *            a string that must be found inside the target line (also
	 *            supports "^" at start to say "only if it starts with" the
	 *            needle)
	 * @param relativeLine
	 *            the line to return based upon the target line position (-1 =
	 *            the line before, 0 = the target line...)
	 * @param first
	 *            takes the first result (as opposed to the last one, which will
	 *            also always spend the input)
	 * 
	 * @return the line
	 */
	static private String getLine(InputStream in, String needle,
			int relativeLine, boolean first) {
		String rep = null;

		List<String> lines = new ArrayList<String>();
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(in, "UTF-8");
		int index = -1;
		scan.useDelimiter("\\n");
		while (scan.hasNext()) {
			lines.add(scan.next());

			if (index == -1) {
				if (needle.startsWith("^")) {
					if (lines.get(lines.size() - 1)
							.startsWith(needle.substring(1))) {
						index = lines.size() - 1;
					}

				} else {
					if (lines.get(lines.size() - 1).contains(needle)) {
						index = lines.size() - 1;
					}
				}
			}

			if (index >= 0 && index + relativeLine < lines.size()) {
				rep = lines.get(index + relativeLine);
				if (first) {
					break;
				}
			}
		}

		return rep;
	}
}
