package be.nikiroo.fanfix.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The main data class, where the whole story resides.
 * 
 * @author niki
 */
public class Story implements Iterable<Chapter>, Cloneable, Serializable {
	private static final long serialVersionUID = 1L;
	
	private MetaData meta;
	private List<Chapter> chapters = new ArrayList<Chapter>();
	private List<Chapter> empty = new ArrayList<Chapter>();

	/**
	 * The metadata about this {@link Story}.
	 * 
	 * @return the meta
	 */
	public MetaData getMeta() {
		return meta;
	}

	/**
	 * The metadata about this {@link Story}.
	 * 
	 * @param meta
	 *            the meta to set
	 */
	public void setMeta(MetaData meta) {
		this.meta = meta;
	}

	/**
	 * The chapters of the story.
	 * 
	 * @return the chapters
	 */
	public List<Chapter> getChapters() {
		return chapters;
	}

	/**
	 * The chapters of the story.
	 * 
	 * @param chapters
	 *            the chapters to set
	 */
	public void setChapters(List<Chapter> chapters) {
		this.chapters = chapters;
	}

	/**
	 * Get an iterator on the {@link Chapter}s.
	 */
	@Override
	public Iterator<Chapter> iterator() {
		return chapters == null ? empty.iterator() : chapters.iterator();
	}

	/**
	 * Display a DEBUG {@link String} representation of this object.
	 * <p>
	 * This is not efficient, nor intended to be.
	 */
	@Override
	public String toString() {
		if (getMeta() != null)
			return "Story: [\n" + getMeta().toString() + "\n]";
		return "Story: [ no metadata found ]";
	}

	@Override
	public Story clone() {
		Story story = null;
		try {
			story = (Story) super.clone();
		} catch (CloneNotSupportedException e) {
			// Did the clones rebel?
			System.err.println(e);
		}

		if (meta != null) {
			story.meta = meta.clone();
		}

		if (chapters != null) {
			story.chapters = new ArrayList<Chapter>();
			for (Chapter chap : chapters) {
				story.chapters.add(chap.clone());
			}
		}

		return story;
	}
}
