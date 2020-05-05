package be.nikiroo.fanfix.reader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.UiConfig;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.fanfix.library.BasicLibrary;
import be.nikiroo.utils.StringUtils;

/**
 * The class that handles the different {@link Story} readers you can use.
 * 
 * @author niki
 */
public abstract class BasicReader {
	/**
	 * Return an {@link URL} from this {@link String}, be it a file path or an
	 * actual {@link URL}.
	 * 
	 * @param sourceString
	 *            the source
	 * 
	 * @return the corresponding {@link URL}
	 * 
	 * @throws MalformedURLException
	 *             if this is neither a file nor a conventional {@link URL}
	 */
	public static URL getUrl(String sourceString) throws MalformedURLException {
		if (sourceString == null || sourceString.isEmpty()) {
			throw new MalformedURLException("Empty url");
		}

		URL source = null;
		try {
			source = new URL(sourceString);
		} catch (MalformedURLException e) {
			File sourceFile = new File(sourceString);
			source = sourceFile.toURI().toURL();
		}

		return source;
	}

	/**
	 * Describe a {@link Story} from its {@link MetaData} and return a list of
	 * title/value that represent this {@link Story}.
	 * 
	 * @param meta
	 *            the {@link MetaData} to represent
	 * 
	 * @return the information
	 */
	public static Map<String, String> getMetaDesc(MetaData meta) {
		Map<String, String> metaDesc = new TreeMap<String, String>();

		// TODO: i18n

		StringBuilder tags = new StringBuilder();
		for (String tag : meta.getTags()) {
			if (tags.length() > 0) {
				tags.append(", ");
			}
			tags.append(tag);
		}

		// TODO: i18n
		metaDesc.put("Author", meta.getAuthor());
		metaDesc.put("Publication date", formatDate(meta.getDate()));
		metaDesc.put("Published on", meta.getPublisher());
		metaDesc.put("URL", meta.getUrl());
		String count = "";
		if (meta.getWords() > 0) {
			count = StringUtils.formatNumber(meta.getWords());
		}
		if (meta.isImageDocument()) {
			metaDesc.put("Number of images", count);
		} else {
			metaDesc.put("Number of words", count);
		}
		metaDesc.put("Source", meta.getSource());
		metaDesc.put("Subject", meta.getSubject());
		metaDesc.put("Language", meta.getLang());
		metaDesc.put("Tags", tags.toString());

		return metaDesc;
	}

	/**
	 * Open the {@link Story} with an external reader (the program will be
	 * passed the main file associated with this {@link Story}).
	 * 
	 * @param lib
	 *            the {@link BasicLibrary} to select the {@link Story} from
	 * @param luid
	 *            the {@link Story} LUID
	 * @param sync
	 *            execute the process synchronously (wait until it is terminated
	 *            before returning)
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public void openExternal(BasicLibrary lib, String luid, boolean sync)
			throws IOException {
		MetaData meta = lib.getInfo(luid);
		File target = lib.getFile(luid, null);

		openExternal(meta, target, sync);
	}

	/**
	 * Open the {@link Story} with an external reader (the program will be
	 * passed the given target file).
	 * 
	 * @param meta
	 *            the {@link Story} to load
	 * @param target
	 *            the target {@link File}
	 * @param sync
	 *            execute the process synchronously (wait until it is terminated
	 *            before returning)
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	protected void openExternal(MetaData meta, File target, boolean sync)
			throws IOException {
		String program = null;
		if (meta.isImageDocument()) {
			program = Instance.getInstance().getUiConfig().getString(UiConfig.IMAGES_DOCUMENT_READER);
		} else {
			program = Instance.getInstance().getUiConfig().getString(UiConfig.NON_IMAGES_DOCUMENT_READER);
		}

		if (program != null && program.trim().isEmpty()) {
			program = null;
		}

		start(target, program, sync);
	}

	/**
	 * Start a file and open it with the given program if given or the first
	 * default system starter we can find.
	 * 
	 * @param target
	 *            the target to open
	 * @param program
	 *            the program to use or NULL for the default system starter
	 * @param sync
	 *            execute the process synchronously (wait until it is terminated
	 *            before returning)
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	protected void start(File target, String program, boolean sync)
			throws IOException {

		Process proc = null;
		if (program == null) {
			boolean ok = false;
			for (String starter : new String[] { "xdg-open", "open", "see",
					"start", "run" }) {
				try {
					Instance.getInstance().getTraceHandler().trace("starting external program");
					proc = Runtime.getRuntime().exec(new String[] { starter, target.getAbsolutePath() });
					ok = true;
					break;
				} catch (IOException e) {
				}
			}
			if (!ok) {
				throw new IOException("Cannot find a program to start the file");
			}
		} else {
			Instance.getInstance().getTraceHandler().trace("starting external program");
			proc = Runtime.getRuntime().exec(
					new String[] { program, target.getAbsolutePath() });
		}

		if (proc != null && sync) {
			try {
				proc.waitFor();
			} catch (InterruptedException e) {
			}
		}
	}

	static private String formatDate(String date) {
		long ms = 0;

		if (date != null && !date.isEmpty()) {
			try {
				ms = StringUtils.toTime(date);
			} catch (ParseException e) {
			}

			if (ms <= 0) {
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ssSSS");
				try {
					ms = sdf.parse(date).getTime();
				} catch (ParseException e) {
				}
			}

			if (ms > 0) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				return sdf.format(new Date(ms));
			}
		}

		if (date == null) {
			date = "";
		}

		// :(
		return date;
	}
}
