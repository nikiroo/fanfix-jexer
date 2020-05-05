package be.nikiroo.utils.resources;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to give some information about the translation keys, so the
 * translation .properties file can be created programmatically.
 * 
 * @author niki
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Meta {
	/**
	 * The format of an item (the values it is expected to be of).
	 * <p>
	 * Note that the INI file can contain arbitrary data, but it is expected to
	 * be valid.
	 * 
	 * @author niki
	 */
	public enum Format {
		/** An integer value, can be negative. */
		INT,
		/** true or false. */
		BOOLEAN,
		/** Any text String. */
		STRING,
		/** A password field. */
		PASSWORD,
		/** A colour (either by name or #rrggbb or #aarrggbb). */
		COLOR,
		/** A locale code (e.g., fr-BE, en-GB, es...). */
		LOCALE,
		/** A path to a file. */
		FILE,
		/** A path to a directory. */
		DIRECTORY,
		/** A fixed list of values (see {@link Meta#list()} for the values). */
		FIXED_LIST,
		/**
		 * A fixed list of values (see {@link Meta#list()} for the values) OR a
		 * custom String value (basically, a {@link Format#FIXED_LIST} with an
		 * option to enter a not accounted for value).
		 */
		COMBO_LIST,
	}

	/**
	 * A description for this item: what it is or does, how to explain that item
	 * to the user including what can be used here (i.e., %s = file name, %d =
	 * file size...).
	 * <p>
	 * For group, the first line ('\\n'-separated) will be used as a title while
	 * the rest will be the description.
	 * 
	 * @return what it is
	 */
	String description() default "";
	
	/**
	 * This item should be hidden from the user (she will still be able to
	 * modify it if she opens the file manually).
	 * <p>
	 * Defaults to FALSE (visible).
	 * 
	 * @return TRUE if it should stay hidden
	 */
	boolean hidden() default false;

	/**
	 * This item is only used as a group, not as an option.
	 * <p>
	 * For instance, you could have LANGUAGE_CODE as a group for which you won't
	 * use the value in the program, and LANGUAGE_CODE_FR, LANGUAGE_CODE_EN
	 * inside for which the value must be set.
	 * 
	 * @return TRUE if it is a group
	 */
	boolean group() default false;

	/**
	 * What format should/must this key be in.
	 * 
	 * @return the format it is in
	 */
	Format format() default Format.STRING;

	/**
	 * The list of fixed values this item can be (either for
	 * {@link Format#FIXED_LIST} or {@link Format#COMBO_LIST}).
	 * 
	 * @return the list of values
	 */
	String[] list() default {};

	/**
	 * This item can be left unspecified.
	 * 
	 * @return TRUE if it can
	 */
	boolean nullable() default true;

	/**
	 * The default value of this item.
	 * 
	 * @return the value
	 */
	String def() default "";

	/**
	 * This item is a comma-separated list of values instead of a single value.
	 * <p>
	 * The list items are separated by a comma, each surrounded by
	 * double-quotes, with backslashes and double-quotes escaped by a backslash.
	 * <p>
	 * Example: <tt>"un", "deux"</tt>
	 * 
	 * @return TRUE if it is
	 */
	boolean array() default false;

	/**
	 * @deprecated add the info into the description, as only the description
	 *             will be translated.
	 */
	@Deprecated
	String info() default "";
}
