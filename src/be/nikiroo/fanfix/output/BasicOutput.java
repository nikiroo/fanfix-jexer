package be.nikiroo.fanfix.output;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.bundles.StringId;
import be.nikiroo.fanfix.data.Chapter;
import be.nikiroo.fanfix.data.Paragraph;
import be.nikiroo.fanfix.data.Paragraph.ParagraphType;
import be.nikiroo.fanfix.data.Story;
import be.nikiroo.utils.Progress;
import be.nikiroo.utils.Version;

/**
 * This class is the base class used by the other output classes. It can be used
 * outside of this package, and have static method that you can use to get
 * access to the correct support class.
 * 
 * @author niki
 */
public abstract class BasicOutput {
	/**
	 * The supported output types for which we can get a {@link BasicOutput}
	 * object.
	 * 
	 * @author niki
	 */
	public enum OutputType {
		/** EPUB files created with this program */
		EPUB,
		/** Pure text file with some rules */
		TEXT,
		/** TEXT but with associated .info file */
		INFO_TEXT,
		/** DEBUG output to console */
		SYSOUT,
		/** ZIP with (PNG) images */
		CBZ,
		/** LaTeX file with "book" template */
		LATEX,
		/** HTML files in a dedicated directory */
		HTML,

		;

		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}

		/**
		 * A description of this output type.
		 * 
		 * @param longDesc
		 *            TRUE for the long description, FALSE for the short one
		 * 
		 * @return the description
		 */
		public String getDesc(boolean longDesc) {
			StringId id = longDesc ? StringId.OUTPUT_DESC
					: StringId.OUTPUT_DESC_SHORT;

			String desc = Instance.getInstance().getTrans().getStringX(id, this.name());

			if (desc == null) {
				desc = Instance.getInstance().getTrans().getString(id, this.toString());
			}

			if (desc == null || desc.isEmpty()) {
				desc = this.toString();
			}

			return desc;
		}

		/**
		 * The default extension to add to the output files.
		 * 
		 * @param readerTarget
		 *            TRUE to point to the main {@link Story} entry point for a
		 *            reader (for instance, the main entry point if this
		 *            {@link Story} is in a directory bundle), FALSE to point to
		 *            the main file even if it is a directory for instance
		 * 
		 * @return the extension
		 */
		public String getDefaultExtension(boolean readerTarget) {
			BasicOutput output = BasicOutput.getOutput(this, false, false);
			if (output != null) {
				return output.getDefaultExtension(readerTarget);
			}

			return null;
		}

		/**
		 * Call {@link OutputType#valueOf(String)} after conversion to upper
		 * case.
		 * 
		 * @param typeName
		 *            the possible type name
		 * 
		 * @return NULL or the type
		 */
		public static OutputType valueOfUC(String typeName) {
			return OutputType.valueOf(typeName == null ? null : typeName
					.toUpperCase());
		}

		/**
		 * Call {@link OutputType#valueOf(String)} after conversion to upper
		 * case but return def for NULL and empty instead of raising an
		 * exception.
		 * 
		 * @param typeName
		 *            the possible type name
		 * @param def
		 *            the default value
		 * 
		 * @return NULL or the type
		 */
		public static OutputType valueOfNullOkUC(String typeName, OutputType def) {
			if (typeName == null || typeName.isEmpty()) {
				return def;
			}

			return OutputType.valueOfUC(typeName);
		}

		/**
		 * Call {@link OutputType#valueOf(String)} after conversion to upper
		 * case but return def in case of error instead of raising an exception.
		 * 
		 * @param typeName
		 *            the possible type name
		 * @param def
		 *            the default value
		 * 
		 * @return NULL or the type
		 */
		public static OutputType valueOfAllOkUC(String typeName, OutputType def) {
			try {
				return OutputType.valueOfUC(typeName);
			} catch (Exception e) {
				return def;
			}
		}
	}

	/** The creator name (this program, by me!) */
	static protected final String EPUB_CREATOR = "Fanfix "
			+ Version.getCurrentVersion() + " (by Niki)";

	/** The current best name for an image */
	private String imageName;
	private File targetDir;
	private String targetName;
	private OutputType type;
	private boolean writeCover;
	private boolean writeInfo;
	private Progress storyPg;
	private Progress chapPg;

	/**
	 * Process the {@link Story} into the given target.
	 * 
	 * @param story
	 *            the {@link Story} to export
	 * @param target
	 *            the target where to save to (will not necessary be taken as is
	 *            by the processor, for instance an extension can be added)
	 * @param pg
	 *            the optional progress reporter
	 * 
	 * @return the actual main target saved, which can be slightly different
	 *         that the input one
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public File process(Story story, String target, Progress pg)
			throws IOException {
		storyPg = pg;

		File targetDir = null;
		String targetName = null;
		if (target != null) {
			target = new File(target).getAbsolutePath();
			targetDir = new File(target).getParentFile();
			targetName = new File(target).getName();

			String ext = getDefaultExtension(false);
			if (ext != null && !ext.isEmpty()) {
				if (targetName.toLowerCase().endsWith(ext)) {
					targetName = targetName.substring(0, targetName.length()
							- ext.length());
				}
			}
		}

		return process(story, targetDir, targetName);
	}

	/**
	 * Process the {@link Story} into the given target.
	 * <p>
	 * This method is expected to be overridden in most cases.
	 * 
	 * @param story
	 *            the {@link Story} to export
	 * @param targetDir
	 *            the target dir where to save to
	 * @param targetName
	 *            the target filename (will not necessary be taken as is by the
	 *            processor, for instance an extension can be added)
	 * 
	 * 
	 * @return the actual main target saved, which can be slightly different
	 *         that the input one
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	protected File process(Story story, File targetDir, String targetName)
			throws IOException {
		this.targetDir = targetDir;
		this.targetName = targetName;

		writeStory(story);

		return null;
	}

	/**
	 * The output type.
	 * 
	 * @return the type
	 */
	public OutputType getType() {
		return type;
	}

	/**
	 * Enable the creation of a .info file next to the resulting processed file.
	 * 
	 * @return TRUE to enable it
	 */
	protected boolean isWriteInfo() {
		return writeInfo;
	}

	/**
	 * Enable the creation of a cover file next to the resulting processed file
	 * if possible.
	 * 
	 * @return TRUE to enable it
	 */
	protected boolean isWriteCover() {
		return writeCover;
	}

	/**
	 * The output type.
	 * 
	 * @param type
	 *            the new type
	 * @param writeCover
	 *            TRUE to enable the creation of a cover if possible
	 * @param writeInfo
	 *            TRUE to enable the creation of a .info file
	 * 
	 * @return this
	 */
	protected BasicOutput setType(OutputType type, boolean writeInfo,
			boolean writeCover) {
		this.type = type;
		this.writeInfo = writeInfo;
		this.writeCover = writeCover;

		return this;
	}

	/**
	 * The default extension to add to the output files.
	 * 
	 * @param readerTarget
	 *            TRUE to point to the main {@link Story} entry point for a
	 *            reader (for instance, the main entry point if this
	 *            {@link Story} is in a directory bundle), FALSE to point to the
	 *            main file even if it is a directory for instance
	 * 
	 * @return the extension
	 */
	public String getDefaultExtension(
			@SuppressWarnings("unused") boolean readerTarget) {
		return "";
	}

	@SuppressWarnings("unused")
	protected void writeStoryHeader(Story story) throws IOException {
	}

	@SuppressWarnings("unused")
	protected void writeChapterHeader(Chapter chap) throws IOException {
	}

	@SuppressWarnings("unused")
	protected void writeParagraphHeader(Paragraph para) throws IOException {
	}

	@SuppressWarnings("unused")
	protected void writeStoryFooter(Story story) throws IOException {
	}

	@SuppressWarnings("unused")
	protected void writeChapterFooter(Chapter chap) throws IOException {
	}

	@SuppressWarnings("unused")
	protected void writeParagraphFooter(Paragraph para) throws IOException {
	}

	protected void writeStory(Story story) throws IOException {
		if (storyPg == null) {
			storyPg = new Progress(0, story.getChapters().size() + 2);
		} else {
			storyPg.setMinMax(0, story.getChapters().size() + 2);
		}

		String chapterNameNum = String.format("%03d", 0);
		String paragraphNumber = String.format("%04d", 0);
		imageName = paragraphNumber + "_" + chapterNameNum;

		if (story.getMeta() != null) {
			story.getMeta().setType("" + getType());
		}

		if (isWriteCover()) {
			InfoCover.writeCover(targetDir, targetName, story.getMeta());
		}
		if (isWriteInfo()) {
			InfoCover.writeInfo(targetDir, targetName, story.getMeta());
		}

		storyPg.setProgress(1);

		List<Progress> chapPgs = new ArrayList<Progress>(story.getChapters()
				.size());
		for (Chapter chap : story) {
			chapPg = new Progress(0, chap.getParagraphs().size());
			storyPg.addProgress(chapPg, 1);
			chapPgs.add(chapPg);
			chapPg = null;
		}

		writeStoryHeader(story);
		for (int i = 0; i < story.getChapters().size(); i++) {
			chapPg = chapPgs.get(i);
			writeChapter(story.getChapters().get(i));
			chapPg.setProgress(chapPg.getMax());
			chapPg = null;
		}
		writeStoryFooter(story);

		storyPg.setProgress(storyPg.getMax());
		storyPg = null;
	}

	protected void writeChapter(Chapter chap) throws IOException {
		String chapterNameNum;
		if (chap.getName() == null || chap.getName().isEmpty()) {
			chapterNameNum = String.format("%03d", chap.getNumber());
		} else {
			chapterNameNum = String.format("%03d", chap.getNumber()) + "_"
					+ chap.getName().replace(" ", "_");
		}

		int num = 0;
		String paragraphNumber = String.format("%04d", num++);
		imageName = chapterNameNum + "_" + paragraphNumber;

		writeChapterHeader(chap);
		int i = 1;
		for (Paragraph para : chap) {
			paragraphNumber = String.format("%04d", num++);
			imageName = chapterNameNum + "_" + paragraphNumber;
			writeParagraph(para);
			if (chapPg != null) {
				chapPg.setProgress(i++);
			}
		}
		writeChapterFooter(chap);
	}

	protected void writeParagraph(Paragraph para) throws IOException {
		writeParagraphHeader(para);
		writeTextLine(para.getType(), para.getContent());
		writeParagraphFooter(para);
	}

	@SuppressWarnings("unused")
	protected void writeTextLine(ParagraphType type, String line)
			throws IOException {
	}

	/**
	 * Return the current best guess for an image name, based upon the current
	 * {@link Chapter} and {@link Paragraph}.
	 * 
	 * @param prefix
	 *            add the original target name as a prefix
	 * 
	 * @return the guessed name
	 */
	protected String getCurrentImageBestName(boolean prefix) {
		if (prefix) {
			return targetName + "_" + imageName;
		}

		return imageName;
	}

	/**
	 * Return the given word or sentence as <b>bold</b>.
	 * 
	 * @param word
	 *            the input
	 * 
	 * @return the bold output
	 */
	protected String enbold(String word) {
		return word;
	}

	/**
	 * Return the given word or sentence as <i>italic</i>.
	 * 
	 * @param word
	 *            the input
	 * 
	 * @return the italic output
	 */
	protected String italize(String word) {
		return word;
	}

	/**
	 * Decorate the given text with <b>bold</b> and <i>italic</i> words,
	 * according to {@link BasicOutput#enbold(String)} and
	 * {@link BasicOutput#italize(String)}.
	 * 
	 * @param text
	 *            the input
	 * 
	 * @return the decorated output
	 */
	protected String decorateText(String text) {
		StringBuilder builder = new StringBuilder();

		int bold = -1;
		int italic = -1;
		char prev = '\0';
		for (char car : text.toCharArray()) {
			switch (car) {
			case '*':
				if (bold >= 0 && prev != ' ') {
					String data = builder.substring(bold);
					builder.setLength(bold);
					builder.append(enbold(data));
					bold = -1;
				} else if (bold < 0
						&& (prev == ' ' || prev == '\0' || prev == '\n')) {
					bold = builder.length();
				} else {
					builder.append(car);
				}

				break;
			case '_':
				if (italic >= 0 && prev != ' ') {
					String data = builder.substring(italic);
					builder.setLength(italic);
					builder.append(enbold(data));
					italic = -1;
				} else if (italic < 0
						&& (prev == ' ' || prev == '\0' || prev == '\n')) {
					italic = builder.length();
				} else {
					builder.append(car);
				}

				break;
			default:
				builder.append(car);
				break;
			}

			prev = car;
		}

		if (bold >= 0) {
			builder.insert(bold, '*');
		}

		if (italic >= 0) {
			builder.insert(italic, '_');
		}

		return builder.toString();
	}

	/**
	 * Return a {@link BasicOutput} object compatible with the given
	 * {@link OutputType}.
	 * 
	 * @param type
	 *            the type
	 * @param writeCover
	 *            TRUE to enable the creation of a cover if possible to be saved
	 *            next to the main target file
	 * @param writeInfo
	 *            TRUE to enable the creation of a .info file to be saved next
	 *            to the main target file
	 * 
	 * @return the {@link BasicOutput}
	 */
	public static BasicOutput getOutput(OutputType type, boolean writeInfo,
			boolean writeCover) {
		if (type != null) {
			switch (type) {
			case EPUB:
				return new Epub().setType(type, writeInfo, writeCover);
			case TEXT:
				return new Text().setType(type, writeInfo, true);
			case INFO_TEXT:
				return new InfoText().setType(type, true, true);
			case SYSOUT:
				return new Sysout().setType(type, false, false);
			case CBZ:
				return new Cbz().setType(type, writeInfo, writeCover);
			case LATEX:
				return new LaTeX().setType(type, writeInfo, writeCover);
			case HTML:
				return new Html().setType(type, writeInfo, writeCover);
			}
		}

		return null;
	}
}
