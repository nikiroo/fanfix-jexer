package be.nikiroo.utils.resources;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * This class manages a translation-dedicated Bundle.
 * <p>
 * Two special cases are handled for the used enum:
 * <ul>
 * <li>NULL will always will return an empty {@link String}</li>
 * <li>DUMMY will return "[DUMMY]" (maybe with a suffix and/or "NOUTF")</li>
 * </ul>
 * 
 * @param <E>
 *            the enum to use to get values out of this class
 * 
 * @author niki
 */
public class TransBundle<E extends Enum<E>> extends Bundle<E> {
	private boolean utf = true;
	private Locale locale;
	private boolean defaultLocale = false;

	/**
	 * Create a translation service with the default language.
	 * 
	 * @param type
	 *            a runtime instance of the class of E
	 * @param name
	 *            the name of the {@link Bundles}
	 */
	public TransBundle(Class<E> type, Enum<?> name) {
		this(type, name, (Locale) null);
	}

	/**
	 * Create a translation service for the given language (will fall back to
	 * the default one i not found).
	 * 
	 * @param type
	 *            a runtime instance of the class of E
	 * @param name
	 *            the name of the {@link Bundles}
	 * @param language
	 *            the language to use, can be NULL for default
	 */
	public TransBundle(Class<E> type, Enum<?> name, String language) {
		super(type, name, null);
		setLocale(language);
	}

	/**
	 * Create a translation service for the given language (will fall back to
	 * the default one i not found).
	 * 
	 * @param type
	 *            a runtime instance of the class of E
	 * @param name
	 *            the name of the {@link Bundles}
	 * @param language
	 *            the language to use, can be NULL for default
	 */
	public TransBundle(Class<E> type, Enum<?> name, Locale language) {
		super(type, name, null);
		setLocale(language);
	}

	/**
	 * Translate the given id into user text.
	 * 
	 * @param stringId
	 *            the ID to translate
	 * @param values
	 *            the values to insert instead of the place holders in the
	 *            translation
	 * 
	 * @return the translated text with the given value where required or NULL
	 *         if not found (not present in the resource file)
	 */
	public String getString(E stringId, Object... values) {
		return getStringX(stringId, "", values);
	}

	/**
	 * Translate the given id into user text.
	 * 
	 * @param stringId
	 *            the ID to translate
	 * @param values
	 *            the values to insert instead of the place holders in the
	 *            translation
	 * 
	 * @return the translated text with the given value where required or NULL
	 *         if not found (not present in the resource file)
	 */
	public String getStringNOUTF(E stringId, Object... values) {
		return getStringX(stringId, "NOUTF", values);
	}

	/**
	 * Translate the given id suffixed with the runtime value "_suffix" (that
	 * is, "_" and suffix) into user text.
	 * 
	 * @param stringId
	 *            the ID to translate
	 * @param values
	 *            the values to insert instead of the place holders in the
	 *            translation
	 * @param suffix
	 *            the runtime suffix
	 * 
	 * @return the translated text with the given value where required or NULL
	 *         if not found (not present in the resource file)
	 */
	public String getStringX(E stringId, String suffix, Object... values) {
		E id = stringId;
		String result = "";

		String key = id.name()
				+ ((suffix == null || suffix.isEmpty()) ? "" : "_"
						+ suffix.toUpperCase());

		if (!isUnicode()) {
			if (containsKey(key + "_NOUTF")) {
				key += "_NOUTF";
			}
		}

		if ("NULL".equals(id.name().toUpperCase())) {
			result = "";
		} else if ("DUMMY".equals(id.name().toUpperCase())) {
			result = "[" + key.toLowerCase() + "]";
		} else if (containsKey(key)) {
			result = getString(key, null);
			if (result == null) {
				result = getMetaDef(id.name());
			}
		} else {
			result = null;
		}

		if (values != null && values.length > 0 && result != null) {
			return String.format(locale, result, values);
		}

		return result;
	}

	/**
	 * Check if unicode characters should be used.
	 * 
	 * @return TRUE to allow unicode
	 */
	public boolean isUnicode() {
		return utf;
	}

	/**
	 * Allow or disallow unicode characters in the program.
	 * 
	 * @param utf
	 *            TRUE to allow unuciode, FALSE to only allow ASCII characters
	 */
	public void setUnicode(boolean utf) {
		this.utf = utf;
	}

	/**
	 * Return all the languages known by the program for this bundle.
	 * 
	 * @return the known language codes
	 */
	public List<String> getKnownLanguages() {
		return getKnownLanguages(keyType);
	}

	/**
	 * The current language (which can be the default one, but NOT NULL).
	 * 
	 * @return the language, not NULL
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * The current language (which can be the default one, but NOT NULL).
	 * 
	 * @return the language, not NULL, in a display format (fr-BE, en-GB, es,
	 *         de...)
	 */
	public String getLocaleString() {
		String lang = locale.getLanguage();
		String country = locale.getCountry();
		if (country != null && !country.isEmpty()) {
			return lang + "-" + country;
		}
		return lang;
	}

	/**
	 * Initialise the translation mappings for the given language.
	 * 
	 * @param language
	 *            the language to initialise, in the form "en-GB" or "fr" for
	 *            instance
	 */
	private void setLocale(String language) {
		setLocale(getLocaleFor(language));
	}

	/**
	 * Initialise the translation mappings for the given language.
	 * 
	 * @param language
	 *            the language to initialise, or NULL for default
	 */
	private void setLocale(Locale language) {
		if (language != null) {
			defaultLocale = false;
			locale = language;
		} else {
			defaultLocale = true;
			locale = Locale.getDefault();
		}

		setBundle(keyType, locale, false);
	}

	@Override
	public void reload(boolean resetToDefault) {
		setBundle(keyType, locale, resetToDefault);
	}

	@Override
	public String getString(E id) {
		return getString(id, (Object[]) null);
	}

	/**
	 * Create/update the .properties files for each supported language and for
	 * the default language.
	 * <p>
	 * Note: this method is <b>NOT</b> thread-safe.
	 * 
	 * @param path
	 *            the path where the .properties files are
	 * 
	 * @throws IOException
	 *             in case of IO errors
	 */
	@Override
	public void updateFile(String path) throws IOException {
		String prev = locale.getLanguage();
		Object status = takeSnapshot();

		// default locale
		setLocale((Locale) null);
		if (prev.equals(Locale.getDefault().getLanguage())) {
			// restore snapshot if default locale = current locale
			restoreSnapshot(status);
		}
		super.updateFile(path);

		for (String lang : getKnownLanguages()) {
			setLocale(lang);
			if (lang.equals(prev)) {
				restoreSnapshot(status);
			}
			super.updateFile(path);
		}

		setLocale(prev);
		restoreSnapshot(status);
	}

	@Override
	protected File getUpdateFile(String path) {
		String code = locale.toString();
		File file = null;
		if (!defaultLocale && code.length() > 0) {
			file = new File(path, keyType.name() + "_" + code + ".properties");
		} else {
			// Default properties file:
			file = new File(path, keyType.name() + ".properties");
		}

		return file;
	}

	@Override
	protected void writeHeader(Writer writer) throws IOException {
		String code = locale.toString();
		String name = locale.getDisplayCountry(locale);

		if (name.length() == 0) {
			name = locale.getDisplayLanguage(locale);
		}

		if (name.length() == 0) {
			name = "default";
		}

		if (code.length() > 0) {
			name = name + " (" + code + ")";
		}

		name = (name + " " + getBundleDisplayName()).trim();

		writer.write("# " + name + " translation file (UTF-8)\n");
		writer.write("# \n");
		writer.write("# Note that any key can be doubled with a _NOUTF suffix\n");
		writer.write("# to use when the NOUTF env variable is set to 1\n");
		writer.write("# \n");
		writer.write("# Also, the comments always refer to the key below them.\n");
		writer.write("# \n");
	}

	@Override
	protected void writeValue(Writer writer, E id) throws IOException {
		super.writeValue(writer, id);

		String name = id.name() + "_NOUTF";
		if (containsKey(name)) {
			String value = getString(name, null);
			if (value == null) {
				value = getMetaDef(id.name());
			}
			boolean set = isSet(id, false);
			writeValue(writer, name, value, set);
		}
	}

	/**
	 * Return the {@link Locale} representing the given language.
	 * 
	 * @param language
	 *            the language to initialise, in the form "en-GB" or "fr" for
	 *            instance
	 * 
	 * @return the corresponding {@link Locale} or NULL if it is not known
	 */
	static private Locale getLocaleFor(String language) {
		Locale locale;

		if (language == null || language.trim().isEmpty()) {
			return null;
		}

		language = language.replaceAll("_", "-");
		String lang = language;
		String country = null;
		if (language.contains("-")) {
			lang = language.split("-")[0];
			country = language.split("-")[1];
		}

		if (country != null)
			locale = new Locale(lang, country);
		else
			locale = new Locale(lang);

		return locale;
	}

	/**
	 * Return all the languages known by the program.
	 * 
	 * @param name
	 *            the enumeration on which we translate
	 * 
	 * @return the known language codes
	 */
	static protected List<String> getKnownLanguages(Enum<?> name) {
		List<String> resources = new LinkedList<String>();

		String regex = ".*" + name.name() + "[_a-zA-Za]*\\.properties$";

		for (String res : TransBundle_ResourceList.getResources(Pattern
				.compile(regex))) {
			String resource = res;
			int index = resource.lastIndexOf('/');
			if (index >= 0 && index < (resource.length() - 1))
				resource = resource.substring(index + 1);
			if (resource.startsWith(name.name())) {
				resource = resource.substring(0, resource.length()
						- ".properties".length());
				resource = resource.substring(name.name().length());
				if (resource.startsWith("_")) {
					resource = resource.substring(1);
					resources.add(resource);
				}
			}
		}

		return resources;
	}
}
