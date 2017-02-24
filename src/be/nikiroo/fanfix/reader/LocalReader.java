package be.nikiroo.fanfix.reader;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.Library;
import be.nikiroo.fanfix.bundles.UiConfig;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.fanfix.output.BasicOutput.OutputType;
import be.nikiroo.utils.Progress;

class LocalReader extends BasicReader {
	private Library lib;

	public LocalReader() throws IOException {
		File dir = Instance.getReaderDir();
		dir.mkdirs();
		if (!dir.exists()) {
			throw new IOException(
					"Cannote create cache directory for local reader: " + dir);
		}

		OutputType text = null;
		OutputType images = null;

		try {
			text = OutputType.valueOfNullOkUC(Instance.getUiConfig().getString(
					UiConfig.NON_IMAGES_DOCUMENT_TYPE));
			if (text == null) {
				text = OutputType.HTML;
			}

			images = OutputType.valueOfNullOkUC(Instance.getUiConfig()
					.getString(UiConfig.IMAGES_DOCUMENT_TYPE));
			if (images == null) {
				images = OutputType.CBZ;
			}
		} catch (Exception e) {
			UiConfig key = (text == null) ? UiConfig.NON_IMAGES_DOCUMENT_TYPE
					: UiConfig.IMAGES_DOCUMENT_TYPE;
			String value = Instance.getUiConfig().getString(key);

			throw new IOException(
					String.format(
							"The configuration option %s is not valid: %s",
							key, value), e);
		}

		lib = new Library(dir, text, images);
	}

	@Override
	public void read() throws IOException {
		if (getStory() == null) {
			throw new IOException("No story to read");
		}

		open(getStory().getMeta().getLuid(), null);
	}

	@Override
	public void read(int chapter) throws IOException {
		// TODO: show a special page?
		read();
	}

	/**
	 * Import the story into the local reader library, and keep the same LUID.
	 * 
	 * @param luid
	 *            the Library UID
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public void imprt(String luid, Progress pg) throws IOException {
		Progress pgGetStory = new Progress();
		Progress pgSave = new Progress();
		if (pg != null) {
			pg.setMax(2);
			pg.addProgress(pgGetStory, 1);
			pg.addProgress(pgSave, 1);
		}

		try {
			Story story = Instance.getLibrary().getStory(luid, pgGetStory);
			if (story != null) {
				story = lib.save(story, luid, pgSave);
			} else {
				throw new IOException("Cannot find story in Library: " + luid);
			}
		} catch (IOException e) {
			throw new IOException(
					"Cannot import story from library to LocalReader library: "
							+ luid, e);
		}
	}

	/**
	 * Get the target file related to this {@link Story}.
	 * 
	 * @param luid
	 *            the LUID of the {@link Story}
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @return the target file
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public File getTarget(String luid, Progress pg) throws IOException {
		File file = lib.getFile(luid);
		if (file == null) {
			imprt(luid, pg);
			file = lib.getFile(luid);
		}

		return file;
	}

	/**
	 * Check if the {@link Story} denoted by this Library UID is present in the
	 * {@link LocalReader} cache.
	 * 
	 * @param luid
	 *            the Library UID
	 * 
	 * @return TRUE if it is
	 */
	public boolean isCached(String luid) {
		return lib.getInfo(luid) != null;
	}

	@Override
	public void start(String type) {
		final String typeFinal = type;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				new LocalReaderFrame(LocalReader.this, typeFinal)
						.setVisible(true);
			}
		});
	}

	// refresh = delete from LocalReader cache (TODO: rename?)
	void refresh(String luid) {
		lib.delete(luid);
	}

	// delete from main library
	void delete(String luid) {
		lib.delete(luid);
		Instance.getLibrary().delete(luid);
	}

	// open the given book
	void open(String luid, Progress pg) throws IOException {
		MetaData meta = Instance.getLibrary().getInfo(luid);
		File target = getTarget(luid, pg);

		String program = null;
		if (meta.isImageDocument()) {
			program = Instance.getUiConfig().getString(
					UiConfig.IMAGES_DOCUMENT_READER);
		} else {
			program = Instance.getUiConfig().getString(
					UiConfig.NON_IMAGES_DOCUMENT_READER);
		}

		if (program != null && program.trim().isEmpty()) {
			program = null;
		}

		if (program == null) {
			try {
				Desktop.getDesktop().browse(target.toURI());
			} catch (UnsupportedOperationException e) {
				Runtime.getRuntime().exec(
						new String[] { "xdg-open", target.getAbsolutePath() });

			}
		} else {
			Runtime.getRuntime().exec(
					new String[] { program, target.getAbsolutePath() });

		}
	}
}
