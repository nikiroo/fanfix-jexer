package be.nikiroo.fanfix.reader;

import java.io.IOException;
import java.net.URL;

import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.fanfix.library.BasicLibrary;
import be.nikiroo.fanfix.supported.SupportType;
import be.nikiroo.utils.Progress;

/**
 * A {@link Reader} is a class that will handle {@link Story} reading and
 * browsing.
 * 
 * @author niki
 */
public interface Reader {
	/**
	 * A type of {@link BasicReader}.
	 * 
	 * @author niki
	 */
	public enum ReaderType {
		/** Simple reader that outputs everything on the console */
		CLI,
		/** Reader that starts local programs to handle the stories */
		GUI,
		/** A text (UTF-8) reader with menu and text windows */
		TUI,
		/** A GUI reader implemented with the Android framework */
		ANDROID,

		;

		/**
		 * Return the full class name of a type that implements said
		 * {@link ReaderType}.
		 * 
		 * @return the class name
		 */
		public String getTypeName() {
			String pkg = "be.nikiroo.fanfix.reader.";
			switch (this) {
			case CLI:
				return pkg + "cli.CliReader";
			case TUI:
				return pkg + "tui.TuiReader";
			case GUI:
				return pkg + "ui.GuiReader";
			case ANDROID:
				return pkg + "android.AndroidReader";
			}

			return null;
		}
	}

	/**
	 * Return the current target {@link MetaData}.
	 * 
	 * @return the meta
	 */
	public MetaData getMeta();

	/**
	 * Return the current {@link Story} as described by the current
	 * {@link MetaData}.
	 * 
	 * @param pg
	 *            the optional progress
	 * 
	 * @return the {@link Story}
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 * 
	 */
	public Story getStory(Progress pg) throws IOException;

	/**
	 * The {@link BasicLibrary} to load the stories from (by default, takes the
	 * default {@link BasicLibrary}).
	 * 
	 * @return the {@link BasicLibrary}
	 */
	public BasicLibrary getLibrary();

	/**
	 * Change the {@link BasicLibrary} that will be managed by this
	 * {@link BasicReader}.
	 * 
	 * @param lib
	 *            the new {@link BasicLibrary}
	 */
	public void setLibrary(BasicLibrary lib);

	/**
	 * Set a {@link Story} from the current {@link BasicLibrary} into the
	 * {@link Reader}.
	 * 
	 * @param luid
	 *            the {@link Story} ID
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public void setMeta(String luid) throws IOException;

	/**
	 * Set a {@link Story} from the current {@link BasicLibrary} into the
	 * {@link Reader}.
	 * 
	 * @param meta
	 *            the meta
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public void setMeta(MetaData meta) throws IOException;

	/**
	 * Set an external {@link Story} into this {@link Reader}.
	 * 
	 * @param source
	 *            the {@link Story} {@link URL}
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public void setMeta(URL source, Progress pg) throws IOException;

	/**
	 * Start the {@link Story} Reading.
	 * 
	 * @param sync
	 *            execute the process synchronously (wait until it is terminated
	 *            before returning)
	 * 
	 * @throws IOException
	 *             in case of I/O error or if the {@link Story} was not
	 *             previously set
	 */
	public void read(boolean sync) throws IOException;

	/**
	 * The selected chapter to start reading at (starting at 1, 0 = description,
	 * -1 = none).
	 * 
	 * @return the chapter, or -1 for "no chapter"
	 */
	public int getChapter();

	/**
	 * The selected chapter to start reading at (starting at 1, 0 = description,
	 * -1 = none).
	 * 
	 * @param chapter
	 *            the chapter, or -1 for "no chapter"
	 */
	public void setChapter(int chapter);

	/**
	 * Start the reader in browse mode for the given source (or pass NULL for
	 * all sources).
	 * <p>
	 * Note that this must be a <b>synchronous</b> action.
	 * 
	 * @param source
	 *            the type of {@link Story} to take into account, or NULL for
	 *            all
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public void browse(String source) throws IOException;

	/**
	 * Display all supports that allow search operations.
	 * 
	 * @param sync
	 *            execute the process synchronously (wait until it is terminated
	 *            before returning)
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public void search(boolean sync) throws IOException;

	/**
	 * Search for the given terms and find stories that correspond if possible.
	 * 
	 * @param searchOn
	 *            the website to search on
	 * @param keywords
	 *            the words to search for (cannot be NULL)
	 * @param page
	 *            the page of results to show (0 = request the maximum number of
	 *            pages, pages start at 1)
	 * @param item
	 *            the item to select (0 = do not select a specific item but show
	 *            all the page, items start at 1)
	 * @param sync
	 *            execute the process synchronously (wait until it is terminated
	 *            before returning)
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public void search(SupportType searchOn, String keywords, int page,
			int item, boolean sync) throws IOException;

	/**
	 * Search based upon a hierarchy of tags, or search for (sub)tags.
	 * <p>
	 * We use the tags <tt>DisplayName</tt>.
	 * <p>
	 * If no tag is given, the main tags will be shown.
	 * <p>
	 * If a non-leaf tag is given, the subtags will be shown.
	 * <p>
	 * If a leaf tag is given (or a full hierarchy ending with a leaf tag),
	 * stories will be shown.
	 * <p>
	 * You can select the story you want with the <tt>item</tt> number.
	 * 
	 * @param searchOn
	 *            the website to search on
	 * @param page
	 *            the page of results to show (0 = request the maximum number of
	 *            pages, pages <b>start at 1</b>)
	 * @param item
	 *            the item to select (0 = do not select a specific item but show
	 *            all the page, items <b>start at 1</b>)
	 * @param sync
	 *            execute the process synchronously (wait until it is terminated
	 *            before returning)
	 * @param tags
	 *            the tags indices to search for (this is a tag
	 *            <b>hierarchy</b>, <b>NOT</b> a multiple tags choice)
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public void searchTag(SupportType searchOn, int page, int item,
			boolean sync, Integer... tags) throws IOException;

	/**
	 * Open the {@link Story} with an external reader (the program should be
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
			throws IOException;
}
