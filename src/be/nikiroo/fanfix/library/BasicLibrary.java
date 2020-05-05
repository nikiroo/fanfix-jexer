package be.nikiroo.fanfix.library;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.fanfix.output.BasicOutput;
import be.nikiroo.fanfix.output.BasicOutput.OutputType;
import be.nikiroo.fanfix.supported.BasicSupport;
import be.nikiroo.fanfix.supported.SupportType;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.Progress;
import be.nikiroo.utils.StringUtils;

/**
 * Manage a library of Stories: import, export, list, modify.
 * <p>
 * Each {@link Story} object will be associated with a (local to the library)
 * unique ID, the LUID, which will be used to identify the {@link Story}.
 * <p>
 * Most of the {@link BasicLibrary} functions work on a partial (cover
 * <b>MAY</b> not be included) {@link MetaData} object.
 * 
 * @author niki
 */
abstract public class BasicLibrary {
	/**
	 * A {@link BasicLibrary} status.
	 * 
	 * @author niki
	 */
	public enum Status {
		/** The library is ready and r/w. */
		READ_WRITE,
		/** The library is ready, but read-only. */
		READ_ONLY,
		/** The library is invalid (not correctly set up). */
		INVALID,
		/** You are not allowed to access this library. */
		UNAUTHORIZED,
		/** The library is currently out of commission. */
		UNAVAILABLE;

		/**
		 * The library is available (you can query it).
		 * <p>
		 * It does <b>not</b> specify if it is read-only or not.
		 * 
		 * @return TRUE if it is
		 */
		public boolean isReady() {
			return (this == READ_WRITE || this == READ_ONLY);
		}

		/**
		 * This library can be modified (= you are allowed to modify it).
		 * 
		 * @return TRUE if it is
		 */
		public boolean isWritable() {
			return (this == READ_WRITE);
		}
	}

	/**
	 * Return a name for this library (the UI may display this).
	 * <p>
	 * Must not be NULL.
	 * 
	 * @return the name, or an empty {@link String} if none
	 */
	public String getLibraryName() {
		return "";
	}

	/**
	 * The library status.
	 * 
	 * @return the current status
	 */
	public Status getStatus() {
		return Status.READ_WRITE;
	}

	/**
	 * Retrieve the main {@link File} corresponding to the given {@link Story},
	 * which can be passed to an external reader or instance.
	 * <p>
	 * Do <b>NOT</b> alter this file.
	 * 
	 * @param luid
	 *            the Library UID of the story, can be NULL
	 * @param pg
	 *            the optional {@link Progress}
	 * 
	 * @return the corresponding {@link Story}
	 * 
	 * @throws IOException
	 *             in case of IOException
	 */
	public abstract File getFile(String luid, Progress pg) throws IOException;

	/**
	 * Return the cover image associated to this story.
	 * 
	 * @param luid
	 *            the Library UID of the story
	 * 
	 * @return the cover image
	 * 
	 * @throws IOException
	 *             in case of IOException
	 */
	public abstract Image getCover(String luid) throws IOException;

	// TODO: ensure it is the main used interface
	public MetaResultList getList(Progress pg) throws IOException {
		return new MetaResultList(getMetas(pg));
	}

	// TODO: make something for (normal and custom) not-story covers

	/**
	 * Return the cover image associated to this source.
	 * <p>
	 * By default, return the custom cover if any, and if not, return the cover
	 * of the first story with this source.
	 * 
	 * @param source
	 *            the source
	 * 
	 * @return the cover image or NULL
	 * 
	 * @throws IOException
	 *             in case of IOException
	 */
	public Image getSourceCover(String source) throws IOException {
		Image custom = getCustomSourceCover(source);
		if (custom != null) {
			return custom;
		}

		List<MetaData> metas = getList().filter(source, null, null);
		if (metas.size() > 0) {
			return getCover(metas.get(0).getLuid());
		}

		return null;
	}

	/**
	 * Return the cover image associated to this author.
	 * <p>
	 * By default, return the custom cover if any, and if not, return the cover
	 * of the first story with this author.
	 * 
	 * @param author
	 *            the author
	 * 
	 * @return the cover image or NULL
	 * 
	 * @throws IOException
	 *             in case of IOException
	 */
	public Image getAuthorCover(String author) throws IOException {
		Image custom = getCustomAuthorCover(author);
		if (custom != null) {
			return custom;
		}

		List<MetaData> metas = getList().filter(null, author, null);
		if (metas.size() > 0) {
			return getCover(metas.get(0).getLuid());
		}

		return null;
	}

	/**
	 * Return the custom cover image associated to this source.
	 * <p>
	 * By default, return NULL.
	 * 
	 * @param source
	 *            the source to look for
	 * 
	 * @return the custom cover or NULL if none
	 * 
	 * @throws IOException
	 *             in case of IOException
	 */
	@SuppressWarnings("unused")
	public Image getCustomSourceCover(String source) throws IOException {
		return null;
	}

	/**
	 * Return the custom cover image associated to this author.
	 * <p>
	 * By default, return NULL.
	 * 
	 * @param author
	 *            the author to look for
	 * 
	 * @return the custom cover or NULL if none
	 * 
	 * @throws IOException
	 *             in case of IOException
	 */
	@SuppressWarnings("unused")
	public Image getCustomAuthorCover(String author) throws IOException {
		return null;
	}

	/**
	 * Set the source cover to the given story cover.
	 * 
	 * @param source
	 *            the source to change
	 * @param luid
	 *            the story LUID
	 * 
	 * @throws IOException
	 *             in case of IOException
	 */
	public abstract void setSourceCover(String source, String luid)
			throws IOException;

	/**
	 * Set the author cover to the given story cover.
	 * 
	 * @param author
	 *            the author to change
	 * @param luid
	 *            the story LUID
	 * 
	 * @throws IOException
	 *             in case of IOException
	 */
	public abstract void setAuthorCover(String author, String luid)
			throws IOException;

	/**
	 * Return the list of stories (represented by their {@link MetaData}, which
	 * <b>MAY</b> not have the cover included).
	 * <p>
	 * The returned list <b>MUST</b> be a copy, not the original one.
	 * 
	 * @param pg
	 *            the optional {@link Progress}
	 * 
	 * @return the list (can be empty but not NULL)
	 * 
	 * @throws IOException
	 *             in case of IOException
	 */
	protected abstract List<MetaData> getMetas(Progress pg) throws IOException;

	/**
	 * Invalidate the {@link Story} cache (when the content should be re-read
	 * because it was changed).
	 */
	protected void invalidateInfo() {
		invalidateInfo(null);
	}

	/**
	 * Invalidate the {@link Story} cache (when the content is removed).
	 * <p>
	 * All the cache can be deleted if NULL is passed as meta.
	 * 
	 * @param luid
	 *            the LUID of the {@link Story} to clear from the cache, or NULL
	 *            for all stories
	 */
	protected abstract void invalidateInfo(String luid);

	/**
	 * Invalidate the {@link Story} cache (when the content has changed, but we
	 * already have it) with the new given meta.
	 * 
	 * @param meta
	 *            the {@link Story} to clear from the cache
	 * 
	 * @throws IOException
	 *             in case of IOException
	 */
	protected abstract void updateInfo(MetaData meta) throws IOException;

	/**
	 * Return the next LUID that can be used.
	 * 
	 * @return the next luid
	 */
	protected abstract int getNextId();

	/**
	 * Delete the target {@link Story}.
	 * 
	 * @param luid
	 *            the LUID of the {@link Story}
	 * 
	 * @throws IOException
	 *             in case of I/O error or if the {@link Story} wa not found
	 */
	protected abstract void doDelete(String luid) throws IOException;

	/**
	 * Actually save the story to the back-end.
	 * 
	 * @param story
	 *            the {@link Story} to save
	 * @param pg
	 *            the optional {@link Progress}
	 * 
	 * @return the saved {@link Story} (which may have changed, especially
	 *         regarding the {@link MetaData})
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	protected abstract Story doSave(Story story, Progress pg)
			throws IOException;

	/**
	 * Refresh the {@link BasicLibrary}, that is, make sure all metas are
	 * loaded.
	 * 
	 * @param pg
	 *            the optional progress reporter
	 */
	public void refresh(Progress pg) {
		try {
			getMetas(pg);
		} catch (IOException e) {
			// We will let it fail later
		}
	}

	/**
	 * Check if the {@link Story} denoted by this Library UID is present in the
	 * cache (if we have no cache, we default to </tt>true</tt>).
	 * 
	 * @param luid
	 *            the Library UID
	 * 
	 * @return TRUE if it is
	 */
	public boolean isCached(@SuppressWarnings("unused") String luid) {
		// By default, everything is cached
		return true;
	}

	/**
	 * Clear the {@link Story} from the cache, if needed.
	 * <p>
	 * The next time we try to retrieve the {@link Story}, it may be required to
	 * cache it again.
	 * 
	 * @param luid
	 *            the story to clear
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	@SuppressWarnings("unused")
	public void clearFromCache(String luid) throws IOException {
		// By default, this is a noop.
	}

	/**
	 * @deprecated please use {@link BasicLibrary#getList()} and
	 *             {@link MetaResultList#getSources()} instead.
	 */
	@Deprecated
	public List<String> getSources() throws IOException {
		return getList().getSources();
	}

	/**
	 * @deprecated please use {@link BasicLibrary#getList()} and
	 *             {@link MetaResultList#getSourcesGrouped()} instead.
	 */
	@Deprecated
	public Map<String, List<String>> getSourcesGrouped() throws IOException {
		return getList().getSourcesGrouped();
	}

	/**
	 * @deprecated please use {@link BasicLibrary#getList()} and
	 *             {@link MetaResultList#getAuthors()} instead.
	 */
	@Deprecated
	public List<String> getAuthors() throws IOException {
		return getList().getAuthors();
	}

	/**
	 * @deprecated please use {@link BasicLibrary#getList()} and
	 *             {@link MetaResultList#getAuthorsGrouped()} instead.
	 */
	public Map<String, List<String>> getAuthorsGrouped() throws IOException {
		return getList().getAuthorsGrouped();
	}

	/**
	 * List all the stories in the {@link BasicLibrary}.
	 * <p>
	 * Cover images <b>MAYBE</b> not included.
	 * 
	 * @return the stories
	 * 
	 * @throws IOException
	 *             in case of IOException
	 */
	public MetaResultList getList() throws IOException {
		return getList(null);
	}

	/**
	 * Retrieve a {@link MetaData} corresponding to the given {@link Story},
	 * cover image <b>MAY</b> not be included.
	 * 
	 * @param luid
	 *            the Library UID of the story, can be NULL
	 * 
	 * @return the corresponding {@link Story} or NULL if not found
	 * 
	 * @throws IOException
	 *             in case of IOException
	 */
	public MetaData getInfo(String luid) throws IOException {
		if (luid != null) {
			for (MetaData meta : getMetas(null)) {
				if (luid.equals(meta.getLuid())) {
					return meta;
				}
			}
		}

		return null;
	}

	/**
	 * Retrieve a specific {@link Story}.
	 * 
	 * @param luid
	 *            the Library UID of the story
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @return the corresponding {@link Story} or NULL if not found
	 * 
	 * @throws IOException
	 *             in case of IOException
	 */
	public Story getStory(String luid, Progress pg) throws IOException {
		Progress pgMetas = new Progress();
		Progress pgStory = new Progress();
		if (pg != null) {
			pg.setMinMax(0, 100);
			pg.addProgress(pgMetas, 10);
			pg.addProgress(pgStory, 90);
		}

		MetaData meta = null;
		for (MetaData oneMeta : getMetas(pgMetas)) {
			if (oneMeta.getLuid().equals(luid)) {
				meta = oneMeta;
				break;
			}
		}

		pgMetas.done();

		Story story = getStory(luid, meta, pgStory);
		pgStory.done();

		return story;
	}

	/**
	 * Retrieve a specific {@link Story}.
	 * 
	 * @param luid
	 *            the LUID of the story
	 * @param meta
	 *            the meta of the story
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @return the corresponding {@link Story} or NULL if not found
	 * 
	 * @throws IOException
	 *             in case of IOException
	 */
	public synchronized Story getStory(String luid, MetaData meta, Progress pg)
			throws IOException {

		if (pg == null) {
			pg = new Progress();
		}

		Progress pgGet = new Progress();
		Progress pgProcess = new Progress();

		pg.setMinMax(0, 2);
		pg.addProgress(pgGet, 1);
		pg.addProgress(pgProcess, 1);

		Story story = null;
		File file = null;

		if (luid != null && meta != null) {
			file = getFile(luid, pgGet);
		}

		pgGet.done();
		try {
			if (file != null) {
				SupportType type = SupportType.valueOfAllOkUC(meta.getType());
				if (type == null) {
					throw new IOException("Unknown type: " + meta.getType());
				}

				URL url = file.toURI().toURL();
				story = BasicSupport.getSupport(type, url) //
						.process(pgProcess);

				// Because we do not want to clear the meta cache:
				meta.setCover(story.getMeta().getCover());
				meta.setResume(story.getMeta().getResume());
				story.setMeta(meta);
			}
		} catch (IOException e) {
			// We should not have not-supported files in the library
			Instance.getInstance().getTraceHandler()
					.error(new IOException(String.format(
							"Cannot load file of type '%s' from library: %s",
							meta.getType(), file), e));
		} finally {
			pgProcess.done();
			pg.done();
		}

		return story;
	}

	/**
	 * Import the {@link Story} at the given {@link URL} into the
	 * {@link BasicLibrary}.
	 * 
	 * @param url
	 *            the {@link URL} to import
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @return the imported Story {@link MetaData}
	 * 
	 * @throws UnknownHostException
	 *             if the host is not supported
	 * @throws IOException
	 *             in case of I/O error
	 */
	public MetaData imprt(URL url, Progress pg) throws IOException {
		if (pg == null)
			pg = new Progress();

		pg.setMinMax(0, 1000);
		Progress pgProcess = new Progress();
		Progress pgSave = new Progress();
		pg.addProgress(pgProcess, 800);
		pg.addProgress(pgSave, 200);

		BasicSupport support = BasicSupport.getSupport(url);
		if (support == null) {
			throw new UnknownHostException("" + url);
		}

		Story story = save(support.process(pgProcess), pgSave);
		pg.done();

		return story.getMeta();
	}

	/**
	 * Import the story from one library to another, and keep the same LUID.
	 * 
	 * @param other
	 *            the other library to import from
	 * @param luid
	 *            the Library UID
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public void imprt(BasicLibrary other, String luid, Progress pg)
			throws IOException {
		Progress pgGetStory = new Progress();
		Progress pgSave = new Progress();
		if (pg == null) {
			pg = new Progress();
		}

		pg.setMinMax(0, 2);
		pg.addProgress(pgGetStory, 1);
		pg.addProgress(pgSave, 1);

		Story story = other.getStory(luid, pgGetStory);
		if (story != null) {
			story = this.save(story, luid, pgSave);
			pg.done();
		} else {
			pg.done();
			throw new IOException("Cannot find story in Library: " + luid);
		}
	}

	/**
	 * Export the {@link Story} to the given target in the given format.
	 * 
	 * @param luid
	 *            the {@link Story} ID
	 * @param type
	 *            the {@link OutputType} to transform it to
	 * @param target
	 *            the target to save to
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @return the saved resource (the main saved {@link File})
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public File export(String luid, OutputType type, String target, Progress pg)
			throws IOException {
		Progress pgGetStory = new Progress();
		Progress pgOut = new Progress();
		if (pg != null) {
			pg.setMax(2);
			pg.addProgress(pgGetStory, 1);
			pg.addProgress(pgOut, 1);
		}

		BasicOutput out = BasicOutput.getOutput(type, false, false);
		if (out == null) {
			throw new IOException("Output type not supported: " + type);
		}

		Story story = getStory(luid, pgGetStory);
		if (story == null) {
			throw new IOException("Cannot find story to export: " + luid);
		}

		return out.process(story, target, pgOut);
	}

	/**
	 * Save a {@link Story} to the {@link BasicLibrary}.
	 * 
	 * @param story
	 *            the {@link Story} to save
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @return the same {@link Story}, whose LUID may have changed
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public Story save(Story story, Progress pg) throws IOException {
		return save(story, null, pg);
	}

	/**
	 * Save a {@link Story} to the {@link BasicLibrary} -- the LUID <b>must</b>
	 * be correct, or NULL to get the next free one.
	 * <p>
	 * Will override any previous {@link Story} with the same LUID.
	 * 
	 * @param story
	 *            the {@link Story} to save
	 * @param luid
	 *            the <b>correct</b> LUID or NULL to get the next free one
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @return the same {@link Story}, whose LUID may have changed
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public synchronized Story save(Story story, String luid, Progress pg)
			throws IOException {
		if (pg == null) {
			pg = new Progress();
		}

		Instance.getInstance().getTraceHandler().trace(
				this.getClass().getSimpleName() + ": saving story " + luid);

		// Do not change the original metadata, but change the original story
		MetaData meta = story.getMeta().clone();
		story.setMeta(meta);

		pg.setName("Saving story");

		if (luid == null || luid.isEmpty()) {
			meta.setLuid(String.format("%03d", getNextId()));
		} else {
			meta.setLuid(luid);
		}

		if (luid != null && getInfo(luid) != null) {
			delete(luid);
		}

		story = doSave(story, pg);

		updateInfo(story.getMeta());

		Instance.getInstance().getTraceHandler()
				.trace(this.getClass().getSimpleName() + ": story saved ("
						+ luid + ")");

		pg.setName(meta.getTitle());
		pg.done();
		return story;
	}

	/**
	 * Delete the given {@link Story} from this {@link BasicLibrary}.
	 * 
	 * @param luid
	 *            the LUID of the target {@link Story}
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public synchronized void delete(String luid) throws IOException {
		Instance.getInstance().getTraceHandler().trace(
				this.getClass().getSimpleName() + ": deleting story " + luid);

		doDelete(luid);
		invalidateInfo(luid);

		Instance.getInstance().getTraceHandler()
				.trace(this.getClass().getSimpleName() + ": story deleted ("
						+ luid + ")");
	}

	/**
	 * Change the type (source) of the given {@link Story}.
	 * 
	 * @param luid
	 *            the {@link Story} LUID
	 * @param newSource
	 *            the new source
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @throws IOException
	 *             in case of I/O error or if the {@link Story} was not found
	 */
	public synchronized void changeSource(String luid, String newSource,
			Progress pg) throws IOException {
		MetaData meta = getInfo(luid);
		if (meta == null) {
			throw new IOException("Story not found: " + luid);
		}

		changeSTA(luid, newSource, meta.getTitle(), meta.getAuthor(), pg);
	}

	/**
	 * Change the title (name) of the given {@link Story}.
	 * 
	 * @param luid
	 *            the {@link Story} LUID
	 * @param newTitle
	 *            the new title
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @throws IOException
	 *             in case of I/O error or if the {@link Story} was not found
	 */
	public synchronized void changeTitle(String luid, String newTitle,
			Progress pg) throws IOException {
		MetaData meta = getInfo(luid);
		if (meta == null) {
			throw new IOException("Story not found: " + luid);
		}

		changeSTA(luid, meta.getSource(), newTitle, meta.getAuthor(), pg);
	}

	/**
	 * Change the author of the given {@link Story}.
	 * 
	 * @param luid
	 *            the {@link Story} LUID
	 * @param newAuthor
	 *            the new author
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @throws IOException
	 *             in case of I/O error or if the {@link Story} was not found
	 */
	public synchronized void changeAuthor(String luid, String newAuthor,
			Progress pg) throws IOException {
		MetaData meta = getInfo(luid);
		if (meta == null) {
			throw new IOException("Story not found: " + luid);
		}

		changeSTA(luid, meta.getSource(), meta.getTitle(), newAuthor, pg);
	}

	/**
	 * Change the Source, Title and Author of the {@link Story} in one single
	 * go.
	 * 
	 * @param luid
	 *            the {@link Story} LUID
	 * @param newSource
	 *            the new source
	 * @param newTitle
	 *            the new title
	 * @param newAuthor
	 *            the new author
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @throws IOException
	 *             in case of I/O error or if the {@link Story} was not found
	 */
	protected synchronized void changeSTA(String luid, String newSource,
			String newTitle, String newAuthor, Progress pg) throws IOException {
		MetaData meta = getInfo(luid);
		if (meta == null) {
			throw new IOException("Story not found: " + luid);
		}

		meta.setSource(newSource);
		meta.setTitle(newTitle);
		meta.setAuthor(newAuthor);
		saveMeta(meta, pg);
	}

	/**
	 * Save back the current state of the {@link MetaData} (LUID <b>MUST NOT</b>
	 * change) for this {@link Story}.
	 * <p>
	 * By default, delete the old {@link Story} then recreate a new
	 * {@link Story}.
	 * <p>
	 * Note that this behaviour can lead to data loss in case of problems!
	 * 
	 * @param meta
	 *            the new {@link MetaData} (LUID <b>MUST NOT</b> change)
	 * @param pg
	 *            the optional {@link Progress}
	 * 
	 * @throws IOException
	 *             in case of I/O error or if the {@link Story} was not found
	 */
	protected synchronized void saveMeta(MetaData meta, Progress pg)
			throws IOException {
		if (pg == null) {
			pg = new Progress();
		}

		Progress pgGet = new Progress();
		Progress pgSet = new Progress();
		pg.addProgress(pgGet, 50);
		pg.addProgress(pgSet, 50);

		Story story = getStory(meta.getLuid(), pgGet);
		if (story == null) {
			throw new IOException("Story not found: " + meta.getLuid());
		}

		// TODO: this is not safe!
		delete(meta.getLuid());
		story.setMeta(meta);
		save(story, meta.getLuid(), pgSet);

		pg.done();
	}
}
