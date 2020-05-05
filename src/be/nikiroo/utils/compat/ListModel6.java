package be.nikiroo.utils.compat;

import javax.swing.JList;

/**
 * Compatibility layer so I can at least get rid of the warnings of using
 * {@link JList} without a parameter (and still staying Java 1.6 compatible).
 * <p>
 * This class is merely a {@link javax.swing.ListModel} that you can parametrise
 * also in Java 1.6.
 * 
 * @author niki
 *
 * @param <E>
 *            the type to use
 */
@SuppressWarnings("rawtypes") // not compatible Java 1.6
public interface ListModel6<E> extends javax.swing.ListModel {
}
