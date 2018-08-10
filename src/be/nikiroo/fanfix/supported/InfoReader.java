package be.nikiroo.fanfix.supported;

import java.io.File;
import java.io.FileInputStream;
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
import be.nikiroo.utils.MarkableFileInputStream;

// not complete: no "description" tag
public class InfoReader {
	public static MetaData readMeta(File infoFile, boolean withCover)
			throws IOException {
		if (infoFile == null) {
			throw new IOException("File is null");
		}

		if (infoFile.exists()) {
			InputStream in = new MarkableFileInputStream(new FileInputStream(
					infoFile));
			try {
				return createMeta(infoFile.toURI().toURL(), in, withCover);
			} finally {
				in.close();
			}
		}

		throw new FileNotFoundException(
				"File given as argument does not exists: "
						+ infoFile.getAbsolutePath());
	}

	private static MetaData createMeta(URL sourceInfoFile, InputStream in,
			boolean withCover) throws IOException {
		MetaData meta = new MetaData();

		meta.setTitle(getInfoTag(in, "TITLE"));
		meta.setAuthor(getInfoTag(in, "AUTHOR"));
		meta.setDate(getInfoTag(in, "DATE"));
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
				meta.setCover(BasicSupportHelper.getImage(null, sourceInfoFile,
						infoTag));
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
		meta.setCreationDate(getInfoTag(in, "CREATION_DATE"));
		meta.setFakeCover(Boolean.parseBoolean(getInfoTag(in, "FAKE_COVER")));

		if (withCover && meta.getCover() == null) {
			meta.setCover(BasicSupportHelper.getDefaultCover(meta.getSubject()));
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

		String ext = "."
				+ Instance.getConfig().getString(Config.IMAGE_FORMAT_COVER)
						.toLowerCase();

		// Without removing ext
		cover = BasicSupportHelper.getImage(null, sourceInfoFile,
				basefile.getAbsolutePath() + ext);

		// Try without ext
		String name = basefile.getName();
		int pos = name.lastIndexOf(".");
		if (cover == null && pos > 0) {
			name = name.substring(0, pos);
			basefile = new File(basefile.getParent(), name);

			cover = BasicSupportHelper.getImage(null, sourceInfoFile,
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
				if (value.startsWith("'") && value.endsWith("'")
						|| value.startsWith("\"") && value.endsWith("\"")) {
					value = value.substring(1, value.length() - 1).trim();
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
					if (lines.get(lines.size() - 1).startsWith(
							needle.substring(1))) {
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
