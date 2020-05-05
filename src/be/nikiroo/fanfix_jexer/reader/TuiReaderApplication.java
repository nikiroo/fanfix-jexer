package be.nikiroo.fanfix_jexer.reader;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;

import jexer.TApplication;
import jexer.TCommand;
import jexer.TKeypress;
import jexer.TMessageBox;
import jexer.TMessageBox.Result;
import jexer.TMessageBox.Type;
import jexer.TStatusBar;
import jexer.TWidget;
import jexer.TWindow;
import jexer.event.TCommandEvent;
import jexer.event.TMenuEvent;
import jexer.menu.TMenu;
import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.data.MetaData;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.fanfix.library.BasicLibrary;
import be.nikiroo.fanfix.reader.BasicReader;
import be.nikiroo.fanfix.reader.Reader;
import be.nikiroo.fanfix.supported.SupportType;
import be.nikiroo.fanfix_jexer.reader.TuiReaderMainWindow.Mode;
import be.nikiroo.utils.Progress;

/**
 * Manages the TUI general mode and links and manages the {@link TWindow}s.
 * <p>
 * It will also enclose a {@link Reader} and simply handle the reading part
 * differently (it will create the required sub-windows and display them).
 * 
 * @author niki
 */
class TuiReaderApplication extends TApplication implements Reader {
	public static final int MENU_FILE_OPEN = 1025;
	public static final int MENU_FILE_IMPORT_URL = 1026;
	public static final int MENU_FILE_IMPORT_FILE = 1027;
	public static final int MENU_FILE_EXPORT = 1028;
	public static final int MENU_FILE_DELETE = 1029;
	public static final int MENU_FILE_LIBRARY = 1030;
	public static final int MENU_FILE_EXIT = 1031;
	//
	public static final int MENU_OPT_FANFIX = 1032;
	public static final int MENU_OPT_TUI = 1033;
	

	public static final TCommand CMD_EXIT = new TCommand(MENU_FILE_EXIT) {
	};

	private Reader reader;
	private TuiReaderMainWindow main;

	// start reading if meta present
	public TuiReaderApplication(Reader reader, BackendType backend)
			throws Exception {
		super(backend);
		init(reader);

		if (getMeta() != null) {
			read(false);
		}
	}

	public TuiReaderApplication(Reader reader, String source,
			TApplication.BackendType backend) throws Exception {
		super(backend);
		init(reader);
		
		showMain();
		main.setMode(Mode.SOURCE, source);
	}

	@Override
	public void read(boolean sync) throws IOException {
		read(getStory(null), sync);
	}

	@Override
	public MetaData getMeta() {
		return reader.getMeta();
	}

	@Override
	public Story getStory(Progress pg) throws IOException {
		return reader.getStory(pg);
	}

	@Override
	public BasicLibrary getLibrary() {
		return reader.getLibrary();
	}

	@Override
	public void setLibrary(BasicLibrary lib) {
		reader.setLibrary(lib);
	}

	@Override
	public void setMeta(MetaData meta) throws IOException {
		reader.setMeta(meta);
	}

	@Override
	public void setMeta(String luid) throws IOException {
		reader.setMeta(luid);
	}

	@Override
	public void setMeta(URL source, Progress pg) throws IOException {
		reader.setMeta(source, pg);
	}

	@Override
	public void browse(String source) {
		try {
			reader.browse(source);
		} catch (IOException e) {
			Instance.getInstance().getTraceHandler().error(e);
		}
	}

	@Override
	public int getChapter() {
		return reader.getChapter();
	}

	@Override
	public void setChapter(int chapter) {
		reader.setChapter(chapter);
	}

	@Override
	public void search(boolean sync) throws IOException {
		reader.search(sync);
	}

	@Override
	public void search(SupportType searchOn, String keywords, int page,
			int item, boolean sync) throws IOException {
		reader.search(searchOn, keywords, page, item, sync);
	}

	@Override
	public void searchTag(SupportType searchOn, int page, int item,
			boolean sync, Integer... tags) throws IOException {
		reader.searchTag(searchOn, page, item, sync, tags);
	}

	/**
	 * Open the given {@link Story} for reading. This may or may not start an
	 * external program to read said {@link Story}.
	 * 
	 * @param story
	 *            the {@link Story} to read
	 * @param sync
	 *            execute the process synchronously (wait until it is terminated
	 *            before returning)
	 * 
	 * @throws IOException
	 *             in case of I/O errors
	 */
	public void read(Story story, boolean sync) throws IOException {
		if (story == null) {
			throw new IOException("No story to read");
		}

		// TODO: open in editor + external option
		if (!story.getMeta().isImageDocument()) {
			TWindow window = new TuiReaderStoryWindow(this, story, getChapter());
			window.maximize();
		} else {
			try {
				openExternal(getLibrary(), story.getMeta().getLuid(), sync);
			} catch (IOException e) {
				messageBox("Error when trying to open the story",
						e.getMessage(), TMessageBox.Type.OK);
			}
		}
	}

	/**
	 * Set the default status bar when this window appear.
	 * <p>
	 * Some shortcuts are always visible, and will be put here.
	 * <p>
	 * Note that shortcuts placed this way on menu won't work unless the menu
	 * also implement them.
	 * 
	 * @param window
	 *            the new window or menu on screen
	 * @param description
	 *            the description to show on the status ba
	 */
	public TStatusBar setStatusBar(TWindow window, String description) {
		TStatusBar statusBar = window.newStatusBar(description);
		statusBar.addShortcutKeypress(TKeypress.kbF10, CMD_EXIT, "Exit");
		return statusBar;

	}

	private void showMain() {
		if (main != null && main.isVisible()) {
			main.activate();
		} else {
			if (main != null) {
				main.close();
			}
			main = new TuiReaderMainWindow(this);
			main.maximize();
		}
	}

	private void init(Reader reader) {
		this.reader = reader;

		// TODO: traces/errors?
		Instance.getInstance().setTraceHandler(null);

		// Add the menus TODO: i18n
		TMenu fileMenu = addMenu("&File");
		fileMenu.addItem(MENU_FILE_OPEN, "&Open...");
		fileMenu.addItem(MENU_FILE_EXPORT, "&Save as...");
		fileMenu.addItem(MENU_FILE_DELETE, "&Delete...");
		// TODO: Move to...
		fileMenu.addSeparator();
		fileMenu.addItem(MENU_FILE_IMPORT_URL, "Import &URL...");
		fileMenu.addItem(MENU_FILE_IMPORT_FILE, "Import &file...");
		fileMenu.addSeparator();
		fileMenu.addItem(MENU_FILE_LIBRARY, "Lib&rary");
		fileMenu.addSeparator();
		fileMenu.addItem(MENU_FILE_EXIT, "E&xit");
		
		TMenu OptionsMenu = addMenu("&Options");
		OptionsMenu.addItem(MENU_OPT_FANFIX, "&Fanfix Configuration");
		OptionsMenu.addItem(MENU_OPT_TUI, "&UI Configuration");

		setStatusBar(fileMenu, "File-management "
				+ "commands (Open, Save, Print, etc.)");
		

		// TODO: Edit: re-download, delete

		//

		addWindowMenu();

		getBackend().setTitle("Fanfix");
	}

	@Override
	protected boolean onCommand(TCommandEvent command) {
		if (command.getCmd().equals(TuiReaderMainWindow.CMD_SEARCH)) {
			messageBox("title", "caption");
			return true;
		}
		return super.onCommand(command);
	}

	@Override
	protected boolean onMenu(TMenuEvent menu) {
		// TODO: i18n
		switch (menu.getId()) {
		case MENU_FILE_EXIT:
			close(this);
			return true;
		case MENU_FILE_OPEN:
			String openfile = null;
			try {
				openfile = fileOpenBox(".");
				reader.setMeta(BasicReader.getUrl(openfile), null);
				read(false);
			} catch (IOException e) {
				// TODO: i18n
				error("Fail to open file"
						+ (openfile == null ? "" : ": " + openfile),
						"Import error", e);
			}

			return true;
		case MENU_FILE_DELETE:
			String luid = null;
			String story = null;
			MetaData meta = null;
			if (main != null) {
				meta = main.getSelectedMeta();
			}
			if (meta != null) {
				luid = meta.getLuid();
				story = luid + ": " + meta.getTitle();
			}

			// TODO: i18n
			TMessageBox mbox = messageBox("Delete story", "Delete story \""
					+ story + "\"", Type.OKCANCEL);
			if (mbox.getResult() == Result.OK) {
				try {
					reader.getLibrary().delete(luid);
					if (main != null) {
						main.refreshStories();
					}
				} catch (IOException e) {
					// TODO: i18n
					error("Fail to delete the story: \"" + story + "\"",
							"Error", e);
				}
			}

			return true;
		case MENU_FILE_IMPORT_URL:
			String clipboard = "";
			try {
				clipboard = ("" + Toolkit.getDefaultToolkit()
						.getSystemClipboard().getData(DataFlavor.stringFlavor))
						.trim();
			} catch (Exception e) {
				// No data will be handled
			}

			if (clipboard == null || !clipboard.startsWith("http")) {
				clipboard = "";
			}

			String url = inputBox("Import story", "URL to import", clipboard)
					.getText();

			try {
				if (!imprt(url)) {
					// TODO: i18n
					error("URK not supported: " + url, "Import error");
				}
			} catch (IOException e) {
				// TODO: i18n
				error("Fail to import URL: " + url, "Import error", e);
			}

			return true;
		case MENU_FILE_IMPORT_FILE:
			String filename = null;
			try {
				filename = fileOpenBox(".");
				if (!imprt(filename)) {
					// TODO: i18n
					error("File not supported: " + filename, "Import error");
				}
			} catch (IOException e) {
				// TODO: i18n
				error("Fail to import file"
						+ (filename == null ? "" : ": " + filename),
						"Import error", e);
			}
			return true;
		case MENU_FILE_LIBRARY:
			showMain();
			return true;
			
		case MENU_OPT_FANFIX:
			new TuiReaderOptionWindow(this, false).maximize();
			return true;
		
		case MENU_OPT_TUI:
			new TuiReaderOptionWindow(this, true).maximize();
			return true;
			
		}

		return super.onMenu(menu);
	}

	/**
	 * Import the given url.
	 * <p>
	 * Can fail if the host is not supported.
	 * 
	 * @param url
	 * 
	 * @return TRUE in case of success, FALSE if the host is not supported
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	private boolean imprt(String url) throws IOException {
		try {
			reader.getLibrary().imprt(BasicReader.getUrl(url), null);
			main.refreshStories();
			return true;
		} catch (UnknownHostException e) {
			return false;
		}
	}

	@Override
	public void openExternal(BasicLibrary lib, String luid, boolean sync)
			throws IOException {
		reader.openExternal(lib, luid, sync);
	}

	/**
	 * Display an error message and log it.
	 * 
	 * @param message
	 *            the message
	 * @param title
	 *            the title of the error message
	 */
	private void error(String message, String title) {
		error(message, title, null);
	}

	/**
	 * Display an error message and log it, including the linked
	 * {@link Exception}.
	 * 
	 * @param message
	 *            the message
	 * @param title
	 *            the title of the error message
	 * @param e
	 *            the exception to log if any (can be NULL)
	 */
	private void error(String message, String title, Exception e) {
		Instance.getInstance().getTraceHandler().error(title + ": " + message);
		if (e != null) {
			Instance.getInstance().getTraceHandler().error(e);
		}

		if (e != null) {
			messageBox(title, message //
					+ "\n" + e.getMessage());
		} else {
			messageBox(title, message);
		}
	}

	/**
	 * Ask the user and, if confirmed, close the {@link TApplication} this
	 * {@link TWidget} is running on.
	 * <p>
	 * This should result in the program terminating.
	 * 
	 * @param widget
	 *            the {@link TWidget}
	 */
	static public void close(TWidget widget) {
		close(widget.getApplication());
	}

	/**
	 * Ask the user and, if confirmed, close the {@link TApplication}.
	 * <p>
	 * This should result in the program terminating.
	 * 
	 * @param app
	 *            the {@link TApplication}
	 */
	static void close(TApplication app) {
		// TODO: i18n
		if (app.messageBox("Confirmation", "(TODO: i18n) Exit application?",
				TMessageBox.Type.YESNO).getResult() == TMessageBox.Result.YES) {
			app.exit();
		}
	}
}
