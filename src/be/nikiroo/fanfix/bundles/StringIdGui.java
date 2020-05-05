package be.nikiroo.fanfix.bundles;

import java.io.IOException;
import java.io.Writer;

import be.nikiroo.utils.resources.Bundle;
import be.nikiroo.utils.resources.Meta;
import be.nikiroo.utils.resources.Meta.Format;

/**
 * The {@link Enum} representing textual information to be translated to the
 * user as a key.
 * 
 * Note that each key that should be translated <b>must</b> be annotated with a
 * {@link Meta} annotation.
 * 
 * @author niki
 */
@SuppressWarnings("javadoc")
public enum StringIdGui {
	/**
	 * A special key used for technical reasons only, without annotations so it
	 * is not visible in <tt>.properties</tt> files.
	 * <p>
	 * Use it when you need NO translation.
	 */
	NULL, //
	/**
	 * A special key used for technical reasons only, without annotations so it
	 * is not visible in <tt>.properties</tt> files.
	 * <p>
	 * Use it when you need a real translation but still don't have a key.
	 */
	DUMMY, //
	@Meta(def = "Fanfix %s", format = Format.STRING, description = "the title of the main window of Fanfix, the library", info = "%s = current Fanfix version")
	// The titles/subtitles:
	TITLE_LIBRARY, //
	@Meta(def = "Fanfix %s", format = Format.STRING, description = "the title of the main window of Fanfix, the library, when the library has a name (i.e., is not local)", info = "%s = current Fanfix version, %s = library name")
	TITLE_LIBRARY_WITH_NAME, //
	@Meta(def = "Fanfix Configuration", format = Format.STRING, description = "the title of the configuration window of Fanfix, also the name of the menu button")
	TITLE_CONFIG, //
	@Meta(def = "This is where you configure the options of the program.", format = Format.STRING, description = "the subtitle of the configuration window of Fanfix")
	SUBTITLE_CONFIG, //
	@Meta(def = "UI Configuration", format = Format.STRING, description = "the title of the UI configuration window of Fanfix, also the name of the menu button")
	TITLE_CONFIG_UI, //
	@Meta(def = "This is where you configure the graphical appearence of the program.", format = Format.STRING, description = "the subtitle of the UI configuration window of Fanfix")
	SUBTITLE_CONFIG_UI, //
	@Meta(def = "Save", format = Format.STRING, description = "the title of the 'save to/export to' window of Fanfix")
	TITLE_SAVE, //
	@Meta(def = "Moving story", format = Format.STRING, description = "the title of the 'move to' window of Fanfix")
	TITLE_MOVE_TO, //
	@Meta(def = "Move to:", format = Format.STRING, description = "the subtitle of the 'move to' window of Fanfix")
	SUBTITLE_MOVE_TO, //
	@Meta(def = "Delete story", format = Format.STRING, description = "the title of the 'delete' window of Fanfix")
	TITLE_DELETE, //
	@Meta(def = "Delete %s: %s", format = Format.STRING, description = "the subtitle of the 'delete' window of Fanfix", info = "%s = LUID of the story, %s = title of the story")
	SUBTITLE_DELETE, //
	@Meta(def = "Library error", format = Format.STRING, description = "the title of the 'library error' dialogue")
	TITLE_ERROR_LIBRARY, //
	@Meta(def = "Importing from URL", format = Format.STRING, description = "the title of the 'import URL' dialogue")
	TITLE_IMPORT_URL, //
	@Meta(def = "URL of the story to import:", format = Format.STRING, description = "the subtitle of the 'import URL' dialogue")
	SUBTITLE_IMPORT_URL, //
	@Meta(def = "Error", format = Format.STRING, description = "the title of general error dialogues")
	TITLE_ERROR, //
	@Meta(def = "%s: %s", format = Format.STRING, description = "the title of a story for the properties dialogue, the viewers...", info = "%s = LUID of the story, %s = title of the story")
	TITLE_STORY, //

	//

	@Meta(def = "A new version of the program is available at %s", format = Format.STRING, description = "HTML text used to notify of a new version", info = "%s = url link in HTML")
	NEW_VERSION_AVAILABLE, //
	@Meta(def = "Updates available", format = Format.STRING, description = "text used as title for the update dialogue")
	NEW_VERSION_TITLE, //
	@Meta(def = "Version %s", format = Format.STRING, description = "HTML text used to specify a newer version title and number, used for each version newer than the current one", info = "%s = the newer version number")
	NEW_VERSION_VERSION, //
	@Meta(def = "%s words", format = Format.STRING, description = "show the number of words of a book", info = "%s = the number")
	BOOK_COUNT_WORDS, //
	@Meta(def = "%s images", format = Format.STRING, description = "show the number of images of a book", info = "%s = the number")
	BOOK_COUNT_IMAGES, //
	@Meta(def = "%s stories", format = Format.STRING, description = "show the number of stories of a meta-book (a book representing allthe types/sources or all the authors present)", info = "%s = the number")
	BOOK_COUNT_STORIES, //

	// Menu (and popup) items:

	@Meta(def = "File", format = Format.STRING, description = "the file menu")
	MENU_FILE, //
	@Meta(def = "Exit", format = Format.STRING, description = "the file/exit menu button")
	MENU_FILE_EXIT, //
	@Meta(def = "Import File...", format = Format.STRING, description = "the file/import_file menu button")
	MENU_FILE_IMPORT_FILE, //
	@Meta(def = "Import URL...", format = Format.STRING, description = "the file/import_url menu button")
	MENU_FILE_IMPORT_URL, //
	@Meta(def = "Save as...", format = Format.STRING, description = "the file/export menu button")
	MENU_FILE_EXPORT, //
	@Meta(def = "Move to", format = Format.STRING, description = "the file/move to menu button")
	MENU_FILE_MOVE_TO, //
	@Meta(def = "Set author", format = Format.STRING, description = "the file/set author menu button")
	MENU_FILE_SET_AUTHOR, //
	@Meta(def = "New source...", format = Format.STRING, description = "the file/move to/new type-source menu button, that will trigger a dialogue to create a new type/source")
	MENU_FILE_MOVE_TO_NEW_TYPE, //
	@Meta(def = "New author...", format = Format.STRING, description = "the file/move to/new author menu button, that will trigger a dialogue to create a new author")
	MENU_FILE_MOVE_TO_NEW_AUTHOR, //
	@Meta(def = "Rename...", format = Format.STRING, description = "the file/rename menu item, that will trigger a dialogue to ask for a new title for the story")
	MENU_FILE_RENAME, //
	@Meta(def = "Properties", format = Format.STRING, description = "the file/Properties menu item, that will trigger a dialogue to show the properties of the story")
	MENU_FILE_PROPERTIES, //
	@Meta(def = "Open", format = Format.STRING, description = "the file/open menu item, that will open the story or fake-story (an author or a source/type)")
	MENU_FILE_OPEN, //
	@Meta(def = "Edit", format = Format.STRING, description = "the edit menu")
	MENU_EDIT, //
	@Meta(def = "Prefetch to cache", format = Format.STRING, description = "the edit/send to cache menu button, to download the story into the cache if not already done")
	MENU_EDIT_DOWNLOAD_TO_CACHE, //
	@Meta(def = "Clear cache", format = Format.STRING, description = "the clear cache menu button, to clear the cache for a single book")
	MENU_EDIT_CLEAR_CACHE, //
	@Meta(def = "Redownload", format = Format.STRING, description = "the edit/redownload menu button, to download the latest version of the book")
	MENU_EDIT_REDOWNLOAD, //
	@Meta(def = "Delete", format = Format.STRING, description = "the edit/delete menu button")
	MENU_EDIT_DELETE, //
	@Meta(def = "Set as cover for source", format = Format.STRING, description = "the edit/Set as cover for source menu button")
	MENU_EDIT_SET_COVER_FOR_SOURCE, //
	@Meta(def = "Set as cover for author", format = Format.STRING, description = "the edit/Set as cover for author menu button")
	MENU_EDIT_SET_COVER_FOR_AUTHOR, //
	@Meta(def = "Search", format = Format.STRING, description = "the search menu to open the earch stories on one of the searchable websites")
	MENU_SEARCH,
	@Meta(def = "View", format = Format.STRING, description = "the view menu")
	MENU_VIEW, //
	@Meta(def = "Word count", format = Format.STRING, description = "the view/word_count menu button, to show the word/image/story count as secondary info")
	MENU_VIEW_WCOUNT, //
	@Meta(def = "Author", format = Format.STRING, description = "the view/author menu button, to show the author as secondary info")
	MENU_VIEW_AUTHOR, //
	@Meta(def = "Sources", format = Format.STRING, description = "the sources menu, to select the books from a specific source; also used as a title for the source books")
	MENU_SOURCES, //
	@Meta(def = "Authors", format = Format.STRING, description = "the authors menu, to select the books of a specific author; also used as a title for the author books")
	MENU_AUTHORS, //
	@Meta(def = "Options", format = Format.STRING, description = "the options menu, to configure Fanfix from the GUI")
	MENU_OPTIONS, //
	@Meta(def = "All", format = Format.STRING, description = "a special menu button to select all the sources/types or authors, by group (one book = one group)")
	MENU_XXX_ALL_GROUPED, //
	@Meta(def = "Listing", format = Format.STRING, description = "a special menu button to select all the sources/types or authors, in a listing (all the included books are listed, grouped by source/type or author)")
	MENU_XXX_ALL_LISTING, //
	@Meta(def = "[unknown]", format = Format.STRING, description = "a special menu button to select the books without author")
	MENU_AUTHORS_UNKNOWN, //

	// Progress names
	@Meta(def = "Reload books", format = Format.STRING, description = "progress bar caption for the 'reload books' step of all outOfUi operations")
	PROGRESS_OUT_OF_UI_RELOAD_BOOKS, //
	@Meta(def = "Change the source of the book to %s", format = Format.STRING, description = "progress bar caption for the 'change source' step of the ReDownload operation", info = "%s = new source name")
	PROGRESS_CHANGE_SOURCE, //

	// Error messages
	@Meta(def = "An error occured when contacting the library", format = Format.STRING, description = "default description if the error is not known")
	ERROR_LIB_STATUS, //
	@Meta(def = "You are not allowed to access this library", format = Format.STRING, description = "library access not allowed")
	ERROR_LIB_STATUS_UNAUTHORIZED, //
	@Meta(def = "Library not valid", format = Format.STRING, description = "the library is invalid (not correctly set up)")
	ERROR_LIB_STATUS_INVALID, //
	@Meta(def = "Library currently unavailable", format = Format.STRING, description = "the library is out of commission")
	ERROR_LIB_STATUS_UNAVAILABLE, //
	@Meta(def = "Cannot open the selected book", format = Format.STRING, description = "cannot open the book, internal or external viewer")
	ERROR_CANNOT_OPEN, //
	@Meta(def = "URL not supported: %s", format = Format.STRING, description = "URL is not supported by Fanfix", info = "%s = URL")
	ERROR_URL_NOT_SUPPORTED, //
	@Meta(def = "Failed to import %s:\n%s", format = Format.STRING, description = "cannot import the URL", info = "%s = URL, %s = reasons")
	ERROR_URL_IMPORT_FAILED,

	// Others
	@Meta(def = "&nbsp;&nbsp;<B>Chapitre <SPAN COLOR='#444466'>%d</SPAN>&nbsp;/&nbsp;%d</B>", format = Format.STRING, description = "(html) the chapter progression value used on the viewers", info = "%d = chapter number, %d = total chapters")
	CHAPTER_HTML_UNNAMED, //
	@Meta(def = "&nbsp;&nbsp;<B>Chapitre <SPAN COLOR='#444466'>%d</SPAN>&nbsp;/&nbsp;%d</B>: %s", format = Format.STRING, description = "(html) the chapter progression value used on the viewers", info = "%d = chapter number, %d = total chapters, %s = chapter name")
	CHAPTER_HTML_NAMED, //
	@Meta(def = "Image %d / %d", format = Format.STRING, description = "(NO html) the chapter progression value used on the viewers", info = "%d = current image number, %d = total images")
	IMAGE_PROGRESSION, //
	
	;

	/**
	 * Write the header found in the configuration <tt>.properties</tt> file of
	 * this {@link Bundle}.
	 * 
	 * @param writer
	 *            the {@link Writer} to write the header in
	 * @param name
	 *            the file name
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	static public void writeHeader(Writer writer, String name)
			throws IOException {
		writer.write("# " + name + " translation file (UTF-8)\n");
		writer.write("# \n");
		writer.write("# Note that any key can be doubled with a _NOUTF suffix\n");
		writer.write("# to use when the NOUTF env variable is set to 1\n");
		writer.write("# \n");
		writer.write("# Also, the comments always refer to the key below them.\n");
		writer.write("# \n");
	}
}
