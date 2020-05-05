package be.nikiroo.fanfix.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A chapter in the story (or the resume/description).
 * 
 * @author niki
 */
public class Chapter implements Iterable<Paragraph>, Cloneable, Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private int number;
	private List<Paragraph> paragraphs = new ArrayList<Paragraph>();
	private List<Paragraph> empty = new ArrayList<Paragraph>();
	private long words;

	/**
	 * Empty constructor, not to use.
	 */
	@SuppressWarnings("unused")
	private Chapter() {
		// for serialisation purposes
	}

	/**
	 * Create a new {@link Chapter} with the given information.
	 * 
	 * @param number
	 *            the chapter number, or 0 for the description/resume.
	 * @param name
	 *            the chapter name
	 */
	public Chapter(int number, String name) {
		this.number = number;
		this.name = name;
	}

	/**
	 * The chapter name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * The chapter name.
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * The chapter number, or 0 for the description/resume.
	 * 
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * The chapter number, or 0 for the description/resume.
	 * 
	 * @param number
	 *            the number to set
	 */
	public void setNumber(int number) {
		this.number = number;
	}

	/**
	 * The included paragraphs.
	 * 
	 * @return the paragraphs
	 */
	public List<Paragraph> getParagraphs() {
		return paragraphs;
	}

	/**
	 * The included paragraphs.
	 * 
	 * @param paragraphs
	 *            the paragraphs to set
	 */
	public void setParagraphs(List<Paragraph> paragraphs) {
		this.paragraphs = paragraphs;
	}

	/**
	 * Get an iterator on the {@link Paragraph}s.
	 */
	@Override
	public Iterator<Paragraph> iterator() {
		return paragraphs == null ? empty.iterator() : paragraphs.iterator();
	}

	/**
	 * The number of words (or images) in this {@link Chapter}.
	 * 
	 * @return the number of words
	 */
	public long getWords() {
		return words;
	}

	/**
	 * The number of words (or images) in this {@link Chapter}.
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
		return "Chapter " + number + ": " + name;
	}

	@Override
	public Chapter clone() {
		Chapter chap = null;
		try {
			chap = (Chapter) super.clone();
		} catch (CloneNotSupportedException e) {
			// Did the clones rebel?
			System.err.println(e);
		}

		if (paragraphs != null) {
			chap.paragraphs = new ArrayList<Paragraph>();
			for (Paragraph para : paragraphs) {
				chap.paragraphs.add(para.clone());
			}
		}

		return chap;
	}
}
