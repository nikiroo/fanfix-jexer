package be.nikiroo.fanfix.bundles;

import java.io.IOException;
import java.io.Writer;

import be.nikiroo.utils.resources.Bundle;
import be.nikiroo.utils.resources.Meta;

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
public enum StringId {
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
	@Meta(info = "%s = supported input, %s = supported output", description = "help message for the syntax")
	HELP_SYNTAX, //
	@Meta(description = "syntax error message")
	ERR_SYNTAX, //
	@Meta(info = "%s = support name, %s = support desc", description = "an input or output support type description")
	ERR_SYNTAX_TYPE, //
	@Meta(info = "%s = input string", description = "Error when retrieving data")
	ERR_LOADING, //
	@Meta(info = "%s = save target", description = "Error when saving to given target")
	ERR_SAVING, //
	@Meta(info = "%s = bad output format", description = "Error when unknown output format")
	ERR_BAD_OUTPUT_TYPE, //
	@Meta(info = "%s = input string", description = "Error when converting input to URL/File")
	ERR_BAD_URL, //
	@Meta(info = "%s = input url", description = "URL/File not supported")
	ERR_NOT_SUPPORTED, //
	@Meta(info = "%s = cover URL", description = "Failed to download cover : %s")
	ERR_BS_NO_COVER, //
	@Meta(def = "`", info = "single char", description = "Canonical OPEN SINGLE QUOTE char (for instance: ‘)")
	OPEN_SINGLE_QUOTE, //
	@Meta(def = "‘", info = "single char", description = "Canonical CLOSE SINGLE QUOTE char (for instance: ’)")
	CLOSE_SINGLE_QUOTE, //
	@Meta(def = "“", info = "single char", description = "Canonical OPEN DOUBLE QUOTE char (for instance: “)")
	OPEN_DOUBLE_QUOTE, //
	@Meta(def = "”", info = "single char", description = "Canonical CLOSE DOUBLE QUOTE char (for instance: ”)")
	CLOSE_DOUBLE_QUOTE, //
	@Meta(def = "Description", description = "Name of the description fake chapter")
	DESCRIPTION, //
	@Meta(def = "Chapter %d: %s", info = "%d = number, %s = name", description = "Name of a chapter with a name")
	CHAPTER_NAMED, //
	@Meta(def = "Chapter %d", info = "%d = number, %s = name", description = "Name of a chapter without name")
	CHAPTER_UNNAMED, //
	@Meta(info = "%s = type", description = "Default description when the type is not known by i18n")
	INPUT_DESC, //
	@Meta(description = "Description of this input type")
	INPUT_DESC_EPUB, //
	@Meta(description = "Description of this input type")
	INPUT_DESC_TEXT, //
	@Meta(description = "Description of this input type")
	INPUT_DESC_INFO_TEXT, //
	@Meta(description = "Description of this input type")
	INPUT_DESC_FANFICTION, //
	@Meta(description = "Description of this input type")
	INPUT_DESC_FIMFICTION, //
	@Meta(description = "Description of this input type")
	INPUT_DESC_MANGAFOX, //
	@Meta(description = "Description of this input type")
	INPUT_DESC_E621, //
	@Meta(description = "Description of this input type")
	INPUT_DESC_E_HENTAI, //
	@Meta(description = "Description of this input type")
	INPUT_DESC_YIFFSTAR, //
	@Meta(description = "Description of this input type")
	INPUT_DESC_CBZ, //
	@Meta(description = "Description of this input type")
	INPUT_DESC_HTML, //
	@Meta(info = "%s = type", description = "Default description when the type is not known by i18n")
	OUTPUT_DESC, //
	@Meta(description = "Description of this output type")
	OUTPUT_DESC_EPUB, //
	@Meta(description = "Description of this output type")
	OUTPUT_DESC_TEXT, //
	@Meta(description = "Description of this output type")
	OUTPUT_DESC_INFO_TEXT, //
	@Meta(description = "Description of this output type")
	OUTPUT_DESC_CBZ, //
	@Meta(description = "Description of this output type")
	OUTPUT_DESC_HTML, //
	@Meta(description = "Description of this output type")
	OUTPUT_DESC_LATEX, //
	@Meta(description = "Description of this output type")
	OUTPUT_DESC_SYSOUT, //
	@Meta(group = true, info = "%s = type", description = "Default description when the type is not known by i18n")
	OUTPUT_DESC_SHORT, //
	@Meta(description = "Short description of this output type")
	OUTPUT_DESC_SHORT_EPUB, //
	@Meta(description = "Short description of this output type")
	OUTPUT_DESC_SHORT_TEXT, //
	@Meta(description = "Short description of this output type")
	OUTPUT_DESC_SHORT_INFO_TEXT, //
	@Meta(description = "Short description of this output type")
	OUTPUT_DESC_SHORT_CBZ, //
	@Meta(description = "Short description of this output type")
	OUTPUT_DESC_SHORT_LATEX, //
	@Meta(description = "Short description of this output type")
	OUTPUT_DESC_SHORT_SYSOUT, //
	@Meta(description = "Short description of this output type")
	OUTPUT_DESC_SHORT_HTML, //
	@Meta(info = "%s = the unknown 2-code language", description = "Error message for unknown 2-letter LaTeX language code")
	LATEX_LANG_UNKNOWN, //
	@Meta(def = "by", description = "'by' prefix before author name used to output the author, make sure it is covered by Config.BYS for input detection")
	BY, //

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
