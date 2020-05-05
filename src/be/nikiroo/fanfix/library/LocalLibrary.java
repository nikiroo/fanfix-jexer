package be.nikiroo.fanfix.library;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.Config;
import be.nikiroo.fanfix.bundles.ConfigBundle;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.fanfix.output.BasicOutput;
import be.nikiroo.fanfix.output.BasicOutput.OutputType;
import be.nikiroo.fanfix.output.InfoCover;
import be.nikiroo.fanfix.supported.InfoReader;
import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.Progress;
import be.nikiroo.utils.StringUtils;

/**
 * This {@link BasicLibrary} will store the stories locally on disk.
 * 
 * @author niki
 */
public class LocalLibrary extends BasicLibrary {
	private int lastId;
	private Object lock = new Object();
	private Map<MetaData, File[]> stories; // Files: [ infoFile, TargetFile ]
	private Map<String, Image> sourceCovers;
	private Map<String, Image> authorCovers;

	private File baseDir;
	private OutputType text;
	private OutputType image;

	/**
	 * Create a new {@link LocalLibrary} with the given back-end directory.
	 * 
	 * @param baseDir
	 *            the directory where to find the {@link Story} objects
	 * @param config
	 *            the configuration used to know which kind of default
	 *            {@link OutputType} to use for images and non-images stories
	 */
	public LocalLibrary(File baseDir, ConfigBundle config) {
		this(baseDir, //
				config.getString(Config.FILE_FORMAT_NON_IMAGES_DOCUMENT_TYPE),
				config.getString(Config.FILE_FORMAT_IMAGES_DOCUMENT_TYPE),
				false);
	}

	/**
	 * Create a new {@link LocalLibrary} with the given back-end directory.
	 * 
	 * @param baseDir
	 *            the directory where to find the {@link Story} objects
	 * @param text
	 *            the {@link OutputType} to use for non-image documents
	 * @param image
	 *            the {@link OutputType} to use for image documents
	 * @param defaultIsHtml
	 *            if the given text or image is invalid, use HTML by default (if
	 *            not, it will be INFO_TEXT/CBZ by default)
	 */
	public LocalLibrary(File baseDir, String text, String image,
			boolean defaultIsHtml) {
		this(baseDir,
				OutputType.valueOfAllOkUC(text,
						defaultIsHtml ? OutputType.HTML : OutputType.INFO_TEXT),
				OutputType.valueOfAllOkUC(image,
						defaultIsHtml ? OutputType.HTML : OutputType.CBZ));
	}

	/**
	 * Create a new {@link LocalLibrary} with the given back-end directory.
	 * 
	 * @param baseDir
	 *            the directory where to find the {@link Story} objects
	 * @param text
	 *            the {@link OutputType} to use for non-image documents
	 * @param image
	 *            the {@link OutputType} to use for image documents
	 */
	public LocalLibrary(File baseDir, OutputType text, OutputType image) {
		this.baseDir = baseDir;
		this.text = text;
		this.image = image;

		this.lastId = 0;
		this.stories = null;
		this.sourceCovers = null;

		baseDir.mkdirs();
	}

	@Override
	protected List<MetaData> getMetas(Progress pg) {
		return new ArrayList<MetaData>(getStories(pg).keySet());
	}

	@Override
	public File getFile(String luid, Progress pg) throws IOException {
		Instance.getInstance().getTraceHandler().trace(
				this.getClass().getSimpleName() + ": get file for " + luid);

		File file = null;
		String mess = "no file found for ";

		MetaData meta = getInfo(luid);
		if (meta != null) {
			File[] files = getStories(pg).get(meta);
			if (files != null) {
				mess = "file retrieved for ";
				file = files[1];
			}
		}

		Instance.getInstance().getTraceHandler()
				.trace(this.getClass().getSimpleName() + ": " + mess + luid
						+ " (" + meta.getTitle() + ")");

		return file;
	}

	@Override
	public Image getCover(String luid) throws IOException {
		MetaData meta = getInfo(luid);
		if (meta != null) {
			if (meta.getCover() != null) {
				return meta.getCover();
			}

			File[] files = getStories(null).get(meta);
			if (files != null) {
				File infoFile = files[0];

				try {
					meta = InfoReader.readMeta(infoFile, true);
					return meta.getCover();
				} catch (IOException e) {
					Instance.getInstance().getTraceHandler().error(e);
				}
			}
		}

		return null;
	}

	@Override
	protected void updateInfo(MetaData meta) {
		invalidateInfo();
	}

	@Override
	protected void invalidateInfo(String luid) {
		synchronized (lock) {
			stories = null;
			sourceCovers = null;
		}
	}

	@Override
	protected int getNextId() {
		getStories(null); // make sure lastId is set

		synchronized (lock) {
			return ++lastId;
		}
	}

	@Override
	protected void doDelete(String luid) throws IOException {
		for (File file : getRelatedFiles(luid)) {
			// TODO: throw an IOException if we cannot delete the files?
			IOUtils.deltree(file);
			file.getParentFile().delete();
		}
	}

	@Override
	protected Story doSave(Story story, Progress pg) throws IOException {
		MetaData meta = story.getMeta();

		File expectedTarget = getExpectedFile(meta);
		expectedTarget.getParentFile().mkdirs();

		BasicOutput it = BasicOutput.getOutput(getOutputType(meta), true, true);
		it.process(story, expectedTarget.getPath(), pg);

		return story;
	}

	@Override
	protected synchronized void saveMeta(MetaData meta, Progress pg)
			throws IOException {
		File newDir = getExpectedDir(meta.getSource());
		if (!newDir.exists()) {
			newDir.mkdirs();
		}

		List<File> relatedFiles = getRelatedFiles(meta.getLuid());
		for (File relatedFile : relatedFiles) {
			// TODO: this is not safe at all.
			// We should copy all the files THEN delete them
			// Maybe also adding some rollback cleanup if possible
			if (relatedFile.getName().endsWith(".info")) {
				try {
					String name = relatedFile.getName().replaceFirst("\\.info$",
							"");
					relatedFile.delete();
					InfoCover.writeInfo(newDir, name, meta);
					relatedFile.getParentFile().delete();
				} catch (IOException e) {
					Instance.getInstance().getTraceHandler().error(e);
				}
			} else {
				relatedFile.renameTo(new File(newDir, relatedFile.getName()));
				relatedFile.getParentFile().delete();
			}
		}

		updateInfo(meta);
	}

	@Override
	public Image getCustomSourceCover(String source) {
		synchronized (lock) {
			if (sourceCovers == null) {
				sourceCovers = new HashMap<String, Image>();
			}
		}

		synchronized (lock) {
			Image img = sourceCovers.get(source);
			if (img != null) {
				return img;
			}
		}

		File coverDir = getExpectedDir(source);
		if (coverDir.isDirectory()) {
			File cover = new File(coverDir, ".cover.png");
			if (cover.exists()) {
				InputStream in;
				try {
					in = new FileInputStream(cover);
					try {
						synchronized (lock) {
							sourceCovers.put(source, new Image(in));
						}
					} finally {
						in.close();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					Instance.getInstance().getTraceHandler()
							.error(new IOException(
									"Cannot load the existing custom source cover: "
											+ cover,
									e));
				}
			}
		}

		synchronized (lock) {
			return sourceCovers.get(source);
		}
	}

	@Override
	public Image getCustomAuthorCover(String author) {
		synchronized (lock) {
			if (authorCovers == null) {
				authorCovers = new HashMap<String, Image>();
			}
		}

		synchronized (lock) {
			Image img = authorCovers.get(author);
			if (img != null) {
				return img;
			}
		}

		File cover = getAuthorCoverFile(author);
		if (cover.exists()) {
			InputStream in;
			try {
				in = new FileInputStream(cover);
				try {
					synchronized (lock) {
						authorCovers.put(author, new Image(in));
					}
				} finally {
					in.close();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				Instance.getInstance().getTraceHandler()
						.error(new IOException(
								"Cannot load the existing custom author cover: "
										+ cover,
								e));
			}
		}

		synchronized (lock) {
			return authorCovers.get(author);
		}
	}

	@Override
	public void setSourceCover(String source, String luid) throws IOException {
		setSourceCover(source, getCover(luid));
	}

	@Override
	public void setAuthorCover(String author, String luid) throws IOException {
		setAuthorCover(author, getCover(luid));
	}

	/**
	 * Set the source cover to the given story cover.
	 * 
	 * @param source
	 *            the source to change
	 * @param coverImage
	 *            the cover image
	 */
	void setSourceCover(String source, Image coverImage) {
		File dir = getExpectedDir(source);
		dir.mkdirs();
		File cover = new File(dir, ".cover");
		try {
			Instance.getInstance().getCache().saveAsImage(coverImage, cover,
					true);
			synchronized (lock) {
				if (sourceCovers != null) {
					sourceCovers.put(source, coverImage);
				}
			}
		} catch (IOException e) {
			Instance.getInstance().getTraceHandler().error(e);
		}
	}

	/**
	 * Set the author cover to the given story cover.
	 * 
	 * @param author
	 *            the author to change
	 * @param coverImage
	 *            the cover image
	 */
	void setAuthorCover(String author, Image coverImage) {
		File cover = getAuthorCoverFile(author);
		cover.getParentFile().mkdirs();
		try {
			Instance.getInstance().getCache().saveAsImage(coverImage, cover,
					true);
			synchronized (lock) {
				if (authorCovers != null) {
					authorCovers.put(author, coverImage);
				}
			}
		} catch (IOException e) {
			Instance.getInstance().getTraceHandler().error(e);
		}
	}

	@Override
	public void imprt(BasicLibrary other, String luid, Progress pg)
			throws IOException {
		if (pg == null) {
			pg = new Progress();
		}

		// Check if we can simply copy the files instead of the whole process
		if (other instanceof LocalLibrary) {
			LocalLibrary otherLocalLibrary = (LocalLibrary) other;

			MetaData meta = otherLocalLibrary.getInfo(luid);
			String expectedType = ""
					+ (meta != null && meta.isImageDocument() ? image : text);
			if (meta != null && meta.getType().equals(expectedType)) {
				File from = otherLocalLibrary.getExpectedDir(meta.getSource());
				File to = this.getExpectedDir(meta.getSource());
				List<File> relatedFiles = otherLocalLibrary
						.getRelatedFiles(luid);
				if (!relatedFiles.isEmpty()) {
					pg.setMinMax(0, relatedFiles.size());
				}

				for (File relatedFile : relatedFiles) {
					File target = new File(relatedFile.getAbsolutePath()
							.replace(from.getAbsolutePath(),
									to.getAbsolutePath()));
					if (!relatedFile.equals(target)) {
						target.getParentFile().mkdirs();
						InputStream in = null;
						try {
							in = new FileInputStream(relatedFile);
							IOUtils.write(in, target);
						} catch (IOException e) {
							if (in != null) {
								try {
									in.close();
								} catch (Exception ee) {
								}
							}

							pg.done();
							throw e;
						}
					}

					pg.add(1);
				}

				invalidateInfo();
				pg.done();
				return;
			}
		}

		super.imprt(other, luid, pg);
	}

	/**
	 * Return the {@link OutputType} for this {@link Story}.
	 * 
	 * @param meta
	 *            the {@link Story} {@link MetaData}
	 * 
	 * @return the type
	 */
	private OutputType getOutputType(MetaData meta) {
		if (meta != null && meta.isImageDocument()) {
			return image;
		}

		return text;
	}

	/**
	 * Return the default {@link OutputType} for this kind of {@link Story}.
	 * 
	 * @param imageDocument
	 *            TRUE for images document, FALSE for text documents
	 * 
	 * @return the type
	 */
	public String getOutputType(boolean imageDocument) {
		if (imageDocument) {
			return image.toString();
		}

		return text.toString();
	}

	/**
	 * Get the target {@link File} related to the given <tt>.info</tt>
	 * {@link File} and {@link MetaData}.
	 * 
	 * @param meta
	 *            the meta
	 * @param infoFile
	 *            the <tt>.info</tt> {@link File}
	 * 
	 * @return the target {@link File}
	 */
	private File getTargetFile(MetaData meta, File infoFile) {
		// Replace .info with whatever is needed:
		String path = infoFile.getPath();
		path = path.substring(0, path.length() - ".info".length());
		String newExt = getOutputType(meta).getDefaultExtension(true);

		return new File(path + newExt);
	}

	/**
	 * The target (full path) where the {@link Story} related to this
	 * {@link MetaData} should be located on disk for a new {@link Story}.
	 * 
	 * @param key
	 *            the {@link Story} {@link MetaData}
	 * 
	 * @return the target
	 */
	private File getExpectedFile(MetaData key) {
		String title = key.getTitle();
		if (title == null) {
			title = "";
		}
		title = title.replaceAll("[^a-zA-Z0-9._+-]", "_");
		if (title.length() > 40) {
			title = title.substring(0, 40);
		}
		return new File(getExpectedDir(key.getSource()),
				key.getLuid() + "_" + title);
	}

	/**
	 * The directory (full path) where the new {@link Story} related to this
	 * {@link MetaData} should be located on disk.
	 * 
	 * @param source
	 *            the type (source)
	 * 
	 * @return the target directory
	 */
	private File getExpectedDir(String source) {
		String sanitizedSource = source.replaceAll("[^a-zA-Z0-9._+/-]", "_");

		while (sanitizedSource.startsWith("/")
				|| sanitizedSource.startsWith("_")) {
			if (sanitizedSource.length() > 1) {
				sanitizedSource = sanitizedSource.substring(1);
			} else {
				sanitizedSource = "";
			}
		}

		sanitizedSource = sanitizedSource.replace("/", File.separator);

		if (sanitizedSource.isEmpty()) {
			sanitizedSource = "_EMPTY";
		}

		return new File(baseDir, sanitizedSource);
	}

	/**
	 * Return the full path to the file to use for the custom cover of this
	 * author.
	 * <p>
	 * One or more of the parent directories <b>MAY</b> not exist.
	 * 
	 * @param author
	 *            the author
	 * 
	 * @return the custom cover file
	 */
	private File getAuthorCoverFile(String author) {
		File aDir = new File(baseDir, "_AUTHORS");
		String hash = StringUtils.getMd5Hash(author);
		String ext = Instance.getInstance().getConfig()
				.getString(Config.FILE_FORMAT_IMAGE_FORMAT_COVER);
		return new File(aDir, hash + "." + ext.toLowerCase());
	}

	/**
	 * Return the list of files/directories on disk for this {@link Story}.
	 * <p>
	 * If the {@link Story} is not found, and empty list is returned.
	 * 
	 * @param luid
	 *            the {@link Story} LUID
	 * 
	 * @return the list of {@link File}s
	 * 
	 * @throws IOException
	 *             if the {@link Story} was not found
	 */
	private List<File> getRelatedFiles(String luid) throws IOException {
		List<File> files = new ArrayList<File>();

		MetaData meta = getInfo(luid);
		if (meta == null) {
			throw new IOException("Story not found: " + luid);
		}

		File infoFile = getStories(null).get(meta)[0];
		File targetFile = getStories(null).get(meta)[1];

		files.add(infoFile);
		files.add(targetFile);

		String readerExt = getOutputType(meta).getDefaultExtension(true);
		String fileExt = getOutputType(meta).getDefaultExtension(false);

		String path = targetFile.getAbsolutePath();
		if (readerExt != null && !readerExt.equals(fileExt)) {
			path = path.substring(0, path.length() - readerExt.length())
					+ fileExt;
			File relatedFile = new File(path);

			if (relatedFile.exists()) {
				files.add(relatedFile);
			}
		}

		String coverExt = "." + Instance.getInstance().getConfig()
				.getString(Config.FILE_FORMAT_IMAGE_FORMAT_COVER).toLowerCase();
		File coverFile = new File(path + coverExt);
		if (!coverFile.exists()) {
			coverFile = new File(
					path.substring(0, path.length() - fileExt.length())
							+ coverExt);
		}

		if (coverFile.exists()) {
			files.add(coverFile);
		}

		return files;
	}

	/**
	 * Fill the list of stories by reading the content of the local directory
	 * {@link LocalLibrary#baseDir}.
	 * <p>
	 * Will use a cached list when possible (see
	 * {@link BasicLibrary#invalidateInfo()}).
	 * 
	 * @param pg
	 *            the optional {@link Progress}
	 * 
	 * @return the list of stories (for each item, the first {@link File} is the
	 *         info file, the second file is the target {@link File})
	 */
	private Map<MetaData, File[]> getStories(Progress pg) {
		if (pg == null) {
			pg = new Progress();
		} else {
			pg.setMinMax(0, 100);
		}

		Map<MetaData, File[]> stories = this.stories;
		if (stories == null) {
			stories = getStoriesDo(pg);
			synchronized (lock) {
				if (this.stories == null)
					this.stories = stories;
				else
					stories = this.stories;
			}
		}

		pg.done();
		return stories;

	}

	/**
	 * Actually do the work of {@link LocalLibrary#getStories(Progress)} (i.e.,
	 * do not retrieve the cache).
	 * 
	 * @param pg
	 *            the optional {@link Progress}
	 * 
	 * @return the list of stories (for each item, the first {@link File} is the
	 *         info file, the second file is the target {@link File})
	 */
	private synchronized Map<MetaData, File[]> getStoriesDo(Progress pg) {
		if (pg == null) {
			pg = new Progress();
		} else {
			pg.setMinMax(0, 100);
		}

		Map<MetaData, File[]> stories = new HashMap<MetaData, File[]>();

		File[] dirs = baseDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file != null && file.isDirectory();
			}
		});

		if (dirs != null) {
			Progress pgDirs = new Progress(0, 100 * dirs.length);
			pg.addProgress(pgDirs, 100);

			for (File dir : dirs) {
				Progress pgFiles = new Progress();
				pgDirs.addProgress(pgFiles, 100);
				pgDirs.setName("Loading from: " + dir.getName());

				addToStories(stories, pgFiles, dir);

				pgFiles.setName(null);
			}

			pgDirs.setName("Loading directories");
		}

		pg.done();

		return stories;
	}

	private void addToStories(Map<MetaData, File[]> stories, Progress pgFiles,
			File dir) {
		File[] infoFilesAndSubdirs = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				boolean info = file != null && file.isFile()
						&& file.getPath().toLowerCase().endsWith(".info");
				boolean dir = file != null && file.isDirectory();
				boolean isExpandedHtml = new File(file, "index.html").isFile();
				return info || (dir && !isExpandedHtml);
			}
		});

		if (pgFiles != null) {
			pgFiles.setMinMax(0, infoFilesAndSubdirs.length);
		}

		for (File infoFileOrSubdir : infoFilesAndSubdirs) {
			if (infoFileOrSubdir.isDirectory()) {
				addToStories(stories, null, infoFileOrSubdir);
			} else {
				try {
					MetaData meta = InfoReader.readMeta(infoFileOrSubdir,
							false);
					try {
						int id = Integer.parseInt(meta.getLuid());
						if (id > lastId) {
							lastId = id;
						}

						stories.put(meta, new File[] { infoFileOrSubdir,
								getTargetFile(meta, infoFileOrSubdir) });
					} catch (Exception e) {
						// not normal!!
						throw new IOException("Cannot understand the LUID of "
								+ infoFileOrSubdir + ": " + meta.getLuid(), e);
					}
				} catch (IOException e) {
					// We should not have not-supported files in the
					// library
					Instance.getInstance().getTraceHandler().error(
							new IOException("Cannot load file from library: "
									+ infoFileOrSubdir, e));
				}
			}

			if (pgFiles != null) {
				pgFiles.add(1);
			}
		}
	}
}
