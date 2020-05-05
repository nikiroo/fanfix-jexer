package be.nikiroo.fanfix.bundles;

import be.nikiroo.utils.resources.Meta;
import be.nikiroo.utils.resources.Meta.Format;

/**
 * The configuration options.
 * 
 * @author niki
 */
@SuppressWarnings("javadoc")
public enum UiConfig {
	@Meta(description = "The directory where to store temporary files for the GUI reader; any relative path uses the applciation config directory as base, $HOME notation is supported, / is always accepted as directory separator",//
	format = Format.DIRECTORY, def = "tmp-reader/")
	CACHE_DIR_LOCAL_READER, //
	@Meta(description = "How to save the cached stories for the GUI Reader (non-images documents) -- those files will be sent to the reader",//
	format = Format.COMBO_LIST, list = { "INFO_TEXT", "EPUB", "HTML", "TEXT" }, def = "EPUB")
	GUI_NON_IMAGES_DOCUMENT_TYPE, //
	@Meta(description = "How to save the cached stories for the GUI Reader (images documents) -- those files will be sent to the reader",//
	format = Format.COMBO_LIST, list = { "CBZ", "HTML" }, def = "CBZ")
	GUI_IMAGES_DOCUMENT_TYPE, //
	@Meta(description = "Use the internal reader for images documents",//
	format = Format.BOOLEAN, def = "true")
	IMAGES_DOCUMENT_USE_INTERNAL_READER, //
	@Meta(description = "The external viewer for images documents (or empty to use the system default program for the given file type)",//
	format = Format.STRING)
	IMAGES_DOCUMENT_READER, //
	@Meta(description = "Use the internal reader for non-images documents",//
	format = Format.BOOLEAN, def = "true")
	NON_IMAGES_DOCUMENT_USE_INTERNAL_READER, //
	@Meta(description = "The external viewer for non-images documents (or empty to use the system default program for the given file type)",//
	format = Format.STRING)
	NON_IMAGES_DOCUMENT_READER, //
	@Meta(description = "The icon to use for the program",//
	format = Format.FIXED_LIST, def = "default", list = { "default", "alternative", "magic-book", "pony-book", "pony-library" })
	PROGRAM_ICON, //
	//
	// GUI settings (hidden in config)
	//
	@Meta(description = "Show the side panel by default",//
	hidden = true, format = Format.BOOLEAN, def = "true")
	SHOW_SIDE_PANEL, //
	@Meta(description = "Show the details panel by default",//
	hidden = true, format = Format.BOOLEAN, def = "true")
	SHOW_DETAILS_PANEL, //
	@Meta(description = "Show thumbnails by default in the books view",//
	hidden = true, format = Format.BOOLEAN, def = "false")
	SHOW_THUMBNAILS, //
	@Meta(description = "Show a words/images count instead of the author by default in the books view",//
	hidden = true, format = Format.BOOLEAN, def = "false")
	SHOW_WORDCOUNT, //
	//
	// Deprecated
	//
	@Meta(description = "The background colour of the library if you don't like the default system one",//
	hidden = true, format = Format.COLOR)
	@Deprecated
	BACKGROUND_COLOR, //
}
