package be.nikiroo.fanfix.data;

import java.io.Serializable;

import be.nikiroo.utils.Image;

/**
 * A paragraph in a chapter of the story.
 * 
 * @author niki
 */
public class Paragraph implements Cloneable, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * A paragraph type, that will dictate how the paragraph will be handled.
	 * 
	 * @author niki
	 */
	public enum ParagraphType {
		/** Normal paragraph (text) */
		NORMAL,
		/** Blank line */
		BLANK,
		/** A Break paragraph, i.e.: HR (Horizontal Line) or '* * *' or whatever */
		BREAK,
		/** Quotation (dialogue) */
		QUOTE,
		/** An image (no text) */
		IMAGE, ;

		/**
		 * This paragraph type is of a text kind (quote or not).
		 * 
		 * @param allowEmpty
		 *            allow empty text as text, too (blanks, breaks...)
		 * @return TRUE if it is
		 */
		public boolean isText(boolean allowEmpty) {
			return (this == NORMAL || this == QUOTE)
					|| (allowEmpty && (this == BLANK || this == BREAK));
		}
	}

	private ParagraphType type;
	private String content;
	private Image contentImage;
	private long words;

	/**
	 * Empty constructor, not to use.
	 */
	@SuppressWarnings("unused")
	private Paragraph() {
		// for serialisation purposes
	}

	/**
	 * Create a new {@link Paragraph} with the given image.
	 * 
	 * @param contentImage
	 *            the image
	 */
	public Paragraph(Image contentImage) {
		this(ParagraphType.IMAGE, null, 1);
		this.contentImage = contentImage;
	}

	/**
	 * Create a new {@link Paragraph} with the given values.
	 * 
	 * @param type
	 *            the {@link ParagraphType}
	 * @param content
	 *            the content of this paragraph
	 * @param words
	 *            the number of words (or images)
	 */
	public Paragraph(ParagraphType type, String content, long words) {
		this.type = type;
		this.content = content;
		this.words = words;
	}

	/**
	 * The {@link ParagraphType}.
	 * 
	 * @return the type
	 */
	public ParagraphType getType() {
		return type;
	}

	/**
	 * The {@link ParagraphType}.
	 * 
	 * @param type
	 *            the type to set
	 */
	public void setType(ParagraphType type) {
		this.type = type;
	}

	/**
	 * The content of this {@link Paragraph} if it is not an image.
	 * 
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * The content of this {@link Paragraph}.
	 * 
	 * @param content
	 *            the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * The content of this {@link Paragraph} if it is an image.
	 * 
	 * @return the content
	 */
	public Image getContentImage() {
		return contentImage;
	}

	/**
	 * The number of words (or images) in this {@link Paragraph}.
	 * 
	 * @return the number of words
	 */
	public long getWords() {
		return words;
	}

	/**
	 * The number of words (or images) in this {@link Paragraph}.
	 * 
	 * @param words
	 *            the number of words to set
	 */
	public void setWords(long words) {
		this.words = words;
	}

	/**
	 * Display a DEBUG {@link String} representation of this object.
	 */
	@Override
	public String toString() {
		return String.format("%s: [%s]", "" + type, content == null ? "N/A"
				: content);
	}

	@Override
	public Paragraph clone() {
		Paragraph para = null;
		try {
			para = (Paragraph) super.clone();
		} catch (CloneNotSupportedException e) {
			// Did the clones rebel?
			System.err.println(e);
		}

		return para;
	}
}
