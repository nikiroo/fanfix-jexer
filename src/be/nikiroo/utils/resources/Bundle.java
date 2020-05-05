package be.nikiroo.utils.resources;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import be.nikiroo.utils.resources.Meta.Format;

/**
 * This class encapsulate a {@link ResourceBundle} in UTF-8. It allows to
 * retrieve values associated to an enumeration, and allows some additional
 * methods.
 * <p>
 * It also sports a writable change map, and you can save back the
 * {@link Bundle} to file with {@link Bundle#updateFile(String)}.
 * 
 * @param <E>
 *            the enum to use to get values out of this class
 * 
 * @author niki
 */

public class Bundle<E extends Enum<E>> {
	/** The type of E. */
	protected Class<E> type;
	/**
	 * The {@link Enum} associated to this {@link Bundle} (all the keys used in
	 * this {@link Bundle} will be of this type).
	 */
	protected Enum<?> keyType;

	private TransBundle<E> descriptionBundle;

	/** R/O map */
	private Map<String, String> map;
	/** R/W map */
	private Map<String, String> changeMap;

	/**
	 * Create a new {@link Bundles} of the given name.
	 * 
	 * @param type
	 *            a runtime instance of the class of E
	 * @param name
	 *            the name of the {@link Bundles}
	 * @param descriptionBundle
	 *            the description {@link TransBundle}, that is, a
	 *            {@link TransBundle} dedicated to the description of the values
	 *            of the given {@link Bundle} (can be NULL)
	 */
	protected Bundle(Class<E> type, Enum<?> name,
			TransBundle<E> descriptionBundle) {
		this.type = type;
		this.keyType = name;
		this.descriptionBundle = descriptionBundle;

		this.map = new HashMap<String, String>();
		this.changeMap = new HashMap<String, String>();
		setBundle(name, Locale.getDefault(), false);
	}

	/**
	 * Check if the setting is set into this {@link Bundle}.
	 * 
	 * @param id
	 *            the id of the setting to check
	 * @param includeDefaultValue
	 *            TRUE to only return false when the setting is not set AND
	 *            there is no default value
	 * 
	 * @return TRUE if the setting is set
	 */
	public boolean isSet(E id, boolean includeDefaultValue) {
		return isSet(id.name(), includeDefaultValue);
	}

	/**
	 * Check if the setting is set into this {@link Bundle}.
	 * 
	 * @param name
	 *            the id of the setting to check
	 * @param includeDefaultValue
	 *            TRUE to only return false when the setting is explicitly set
	 *            to NULL (and not just "no set") in the change maps
	 * 
	 * @return TRUE if the setting is set
	 */
	protected boolean isSet(String name, boolean includeDefaultValue) {
		if (getString(name, null) == null) {
			if (!includeDefaultValue || getString(name, "") == null) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Return the value associated to the given id as a {@link String}.
	 * 
	 * @param id
	 *            the id of the value to get
	 * 
	 * @return the associated value, or NULL if not found (not present in the
	 *         resource file)
	 */
	public String getString(E id) {
		return getString(id, null);
	}

	/**
	 * Return the value associated to the given id as a {@link String}.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * @param def
	 *            the default value when it is not present in the config file
	 * 
	 * @return the associated value, or NULL if not found (not present in the
	 *         resource file)
	 */
	public String getString(E id, String def) {
		return getString(id, def, -1);
	}

	/**
	 * Return the value associated to the given id as a {@link String}.
	 * <p>
	 * If no value is associated (or if it is empty!), take the default one if
	 * any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * @param def
	 *            the default value when it is not present in the config file
	 * @param item
	 *            the item number to get for an array of values, or -1 for
	 *            non-arrays
	 * 
	 * @return the associated value, or NULL if not found (not present in the
	 *         resource file)
	 */
	public String getString(E id, String def, int item) {
		String rep = getString(id.name(), null);
		if (rep == null) {
			rep = getMetaDef(id.name());
		}

		if (rep == null || rep.isEmpty()) {
			return def;
		}

		if (item >= 0) {
			List<String> values = BundleHelper.parseList(rep, item);
			if (values != null && item < values.size()) {
				return values.get(item);
			}

			return null;
		}

		return rep;
	}

	/**
	 * Set the value associated to the given id as a {@link String}.
	 * 
	 * @param id
	 *            the id of the value to set
	 * @param value
	 *            the value
	 * 
	 */
	public void setString(E id, String value) {
		setString(id.name(), value);
	}

	/**
	 * Set the value associated to the given id as a {@link String}.
	 * 
	 * @param id
	 *            the id of the value to set
	 * @param value
	 *            the value
	 * @param item
	 *            the item number to get for an array of values, or -1 for
	 *            non-arrays
	 * 
	 */
	public void setString(E id, String value, int item) {
		if (item < 0) {
			setString(id.name(), value);
		} else {
			List<String> values = getList(id);
			setString(id.name(), BundleHelper.fromList(values, value, item));
		}
	}

	/**
	 * Return the value associated to the given id as a {@link String} suffixed
	 * with the runtime value "_suffix" (that is, "_" and suffix).
	 * <p>
	 * Will only accept suffixes that form an existing id.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * @param suffix
	 *            the runtime suffix
	 * 
	 * @return the associated value, or NULL if not found (not present in the
	 *         resource file)
	 */
	public String getStringX(E id, String suffix) {
		return getStringX(id, suffix, null, -1);
	}

	/**
	 * Return the value associated to the given id as a {@link String} suffixed
	 * with the runtime value "_suffix" (that is, "_" and suffix).
	 * <p>
	 * Will only accept suffixes that form an existing id.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * @param suffix
	 *            the runtime suffix
	 * @param def
	 *            the default value when it is not present in the config file
	 * 
	 * @return the associated value, or NULL if not found (not present in the
	 *         resource file)
	 */
	public String getStringX(E id, String suffix, String def) {
		return getStringX(id, suffix, def, -1);
	}

	/**
	 * Return the value associated to the given id as a {@link String} suffixed
	 * with the runtime value "_suffix" (that is, "_" and suffix).
	 * <p>
	 * Will only accept suffixes that form an existing id.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * @param suffix
	 *            the runtime suffix
	 * @param def
	 *            the default value when it is not present in the config file
	 * @param item
	 *            the item number to get for an array of values, or -1 for
	 *            non-arrays
	 * 
	 * @return the associated value, or NULL if not found (not present in the
	 *         resource file)
	 */
	public String getStringX(E id, String suffix, String def, int item) {
		String key = id.name()
				+ (suffix == null ? "" : "_" + suffix.toUpperCase());

		try {
			id = Enum.valueOf(type, key);
			return getString(id, def, item);
		} catch (IllegalArgumentException e) {
		}

		return null;
	}

	/**
	 * Set the value associated to the given id as a {@link String} suffixed
	 * with the runtime value "_suffix" (that is, "_" and suffix).
	 * <p>
	 * Will only accept suffixes that form an existing id.
	 * 
	 * @param id
	 *            the id of the value to set
	 * @param suffix
	 *            the runtime suffix
	 * @param value
	 *            the value
	 */
	public void setStringX(E id, String suffix, String value) {
		setStringX(id, suffix, value, -1);
	}

	/**
	 * Set the value associated to the given id as a {@link String} suffixed
	 * with the runtime value "_suffix" (that is, "_" and suffix).
	 * <p>
	 * Will only accept suffixes that form an existing id.
	 * 
	 * @param id
	 *            the id of the value to set
	 * @param suffix
	 *            the runtime suffix
	 * @param value
	 *            the value
	 * @param item
	 *            the item number to get for an array of values, or -1 for
	 *            non-arrays
	 */
	public void setStringX(E id, String suffix, String value, int item) {
		String key = id.name()
				+ (suffix == null ? "" : "_" + suffix.toUpperCase());

		try {
			id = Enum.valueOf(type, key);
			setString(id, value, item);
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * Return the value associated to the given id as a {@link Boolean}.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * 
	 * @return the associated value
	 */
	public Boolean getBoolean(E id) {
		return BundleHelper.parseBoolean(getString(id), -1);
	}

	/**
	 * Return the value associated to the given id as a {@link Boolean}.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * @param def
	 *            the default value when it is not present in the config file or
	 *            if it is not a boolean value
	 * 
	 * @return the associated value
	 */
	public boolean getBoolean(E id, boolean def) {
		Boolean value = getBoolean(id);
		if (value != null) {
			return value;
		}

		return def;
	}

	/**
	 * Return the value associated to the given id as a {@link Boolean}.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * @param def
	 *            the default value when it is not present in the config file or
	 *            if it is not a boolean value
	 * @param item
	 *            the item number to get for an array of values, or -1 for
	 *            non-arrays
	 * 
	 * @return the associated value
	 */
	public Boolean getBoolean(E id, boolean def, int item) {
		String value = getString(id);
		if (value != null) {
			return BundleHelper.parseBoolean(value, item);
		}

		return def;
	}

	/**
	 * Set the value associated to the given id as a {@link Boolean}.
	 * 
	 * @param id
	 *            the id of the value to set
	 * @param value
	 *            the value
	 * 
	 */
	public void setBoolean(E id, boolean value) {
		setBoolean(id, value, -1);
	}

	/**
	 * Set the value associated to the given id as a {@link Boolean}.
	 * 
	 * @param id
	 *            the id of the value to set
	 * @param value
	 *            the value
	 * @param item
	 *            the item number to get for an array of values, or -1 for
	 *            non-arrays
	 * 
	 */
	public void setBoolean(E id, boolean value, int item) {
		setString(id, BundleHelper.fromBoolean(value), item);
	}

	/**
	 * Return the value associated to the given id as an {@link Integer}.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * 
	 * @return the associated value
	 */
	public Integer getInteger(E id) {
		String value = getString(id);
		if (value != null) {
			return BundleHelper.parseInteger(value, -1);
		}

		return null;
	}

	/**
	 * Return the value associated to the given id as an int.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * @param def
	 *            the default value when it is not present in the config file or
	 *            if it is not a int value
	 * 
	 * @return the associated value
	 */
	public int getInteger(E id, int def) {
		Integer value = getInteger(id);
		if (value != null) {
			return value;
		}

		return def;
	}

	/**
	 * Return the value associated to the given id as an int.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * @param def
	 *            the default value when it is not present in the config file or
	 *            if it is not a int value
	 * @param item
	 *            the item number to get for an array of values, or -1 for
	 *            non-arrays
	 * 
	 * @return the associated value
	 */
	public Integer getInteger(E id, int def, int item) {
		String value = getString(id);
		if (value != null) {
			return BundleHelper.parseInteger(value, item);
		}

		return def;
	}

	/**
	 * Set the value associated to the given id as a {@link Integer}.
	 * 
	 * @param id
	 *            the id of the value to set
	 * @param value
	 *            the value
	 * 
	 */
	public void setInteger(E id, int value) {
		setInteger(id, value, -1);
	}

	/**
	 * Set the value associated to the given id as a {@link Integer}.
	 * 
	 * @param id
	 *            the id of the value to set
	 * @param value
	 *            the value
	 * @param item
	 *            the item number to get for an array of values, or -1 for
	 *            non-arrays
	 * 
	 */
	public void setInteger(E id, int value, int item) {
		setString(id, BundleHelper.fromInteger(value), item);
	}

	/**
	 * Return the value associated to the given id as a {@link Character}.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * 
	 * @return the associated value
	 */
	public Character getCharacter(E id) {
		return BundleHelper.parseCharacter(getString(id), -1);
	}

	/**
	 * Return the value associated to the given id as a {@link Character}.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * @param def
	 *            the default value when it is not present in the config file or
	 *            if it is not a char value
	 * 
	 * @return the associated value
	 */
	public char getCharacter(E id, char def) {
		Character value = getCharacter(id);
		if (value != null) {
			return value;
		}

		return def;
	}

	/**
	 * Return the value associated to the given id as a {@link Character}.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * @param def
	 *            the default value when it is not present in the config file or
	 *            if it is not a char value
	 * @param item
	 *            the item number to get for an array of values, or -1 for
	 *            non-arrays
	 * 
	 * @return the associated value
	 */
	public Character getCharacter(E id, char def, int item) {
		String value = getString(id);
		if (value != null) {
			return BundleHelper.parseCharacter(value, item);
		}

		return def;
	}

	/**
	 * Set the value associated to the given id as a {@link Character}.
	 * 
	 * @param id
	 *            the id of the value to set
	 * @param value
	 *            the value
	 * 
	 */
	public void setCharacter(E id, char value) {
		setCharacter(id, value, -1);
	}

	/**
	 * Set the value associated to the given id as a {@link Character}.
	 * 
	 * @param id
	 *            the id of the value to set
	 * @param value
	 *            the value
	 * @param item
	 *            the item number to get for an array of values, or -1 for
	 *            non-arrays
	 * 
	 */
	public void setCharacter(E id, char value, int item) {
		setString(id, BundleHelper.fromCharacter(value), item);
	}

	/**
	 * Return the value associated to the given id as a colour if it is found
	 * and can be parsed.
	 * <p>
	 * The returned value is an ARGB value.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * 
	 * @return the associated value
	 */
	public Integer getColor(E id) {
		return BundleHelper.parseColor(getString(id), -1);
	}

	/**
	 * Return the value associated to the given id as a colour if it is found
	 * and can be parsed.
	 * <p>
	 * The returned value is an ARGB value.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * @param def
	 *            the default value when it is not present in the config file or
	 *            if it is not a char value
	 * 
	 * @return the associated value
	 */
	public int getColor(E id, int def) {
		Integer value = getColor(id);
		if (value != null) {
			return value;
		}

		return def;
	}

	/**
	 * Return the value associated to the given id as a colour if it is found
	 * and can be parsed.
	 * <p>
	 * The returned value is an ARGB value.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * @param def
	 *            the default value when it is not present in the config file or
	 *            if it is not a char value
	 * @param item
	 *            the item number to get for an array of values, or -1 for
	 *            non-arrays
	 * 
	 * @return the associated value
	 */
	public Integer getColor(E id, int def, int item) {
		String value = getString(id);
		if (value != null) {
			return BundleHelper.parseColor(value, item);
		}

		return def;
	}

	/**
	 * Set the value associated to the given id as a colour.
	 * <p>
	 * The value is a BGRA value.
	 * 
	 * @param id
	 *            the id of the value to set
	 * @param color
	 *            the new colour
	 */
	public void setColor(E id, Integer color) {
		setColor(id, color, -1);
	}

	/**
	 * Set the value associated to the given id as a Color.
	 * 
	 * @param id
	 *            the id of the value to set
	 * @param value
	 *            the value
	 * @param item
	 *            the item number to get for an array of values, or -1 for
	 *            non-arrays
	 * 
	 */
	public void setColor(E id, int value, int item) {
		setString(id, BundleHelper.fromColor(value), item);
	}

	/**
	 * Return the value associated to the given id as a list of values if it is
	 * found and can be parsed.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * 
	 * @return the associated list, empty if the value is empty, NULL if it is
	 *         not found or cannot be parsed as a list
	 */
	public List<String> getList(E id) {
		return BundleHelper.parseList(getString(id), -1);
	}

	/**
	 * Return the value associated to the given id as a list of values if it is
	 * found and can be parsed.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * @param def
	 *            the default value when it is not present in the config file or
	 *            if it is not a char value
	 * 
	 * @return the associated list, empty if the value is empty, NULL if it is
	 *         not found or cannot be parsed as a list
	 */
	public List<String> getList(E id, List<String> def) {
		List<String> value = getList(id);
		if (value != null) {
			return value;
		}

		return def;
	}

	/**
	 * Return the value associated to the given id as a list of values if it is
	 * found and can be parsed.
	 * <p>
	 * If no value is associated, take the default one if any.
	 * 
	 * @param id
	 *            the id of the value to get
	 * @param def
	 *            the default value when it is not present in the config file or
	 *            if it is not a char value
	 * @param item
	 *            the item number to get for an array of values, or -1 for
	 *            non-arrays
	 * 
	 * @return the associated list, empty if the value is empty, NULL if it is
	 *         not found or cannot be parsed as a list
	 */
	public List<String> getList(E id, List<String> def, int item) {
		String value = getString(id);
		if (value != null) {
			return BundleHelper.parseList(value, item);
		}

		return def;
	}

	/**
	 * Set the value associated to the given id as a list of values.
	 * 
	 * @param id
	 *            the id of the value to set
	 * @param list
	 *            the new list of values
	 */
	public void setList(E id, List<String> list) {
		setList(id, list, -1);
	}

	/**
	 * Set the value associated to the given id as a {@link List}.
	 * 
	 * @param id
	 *            the id of the value to set
	 * @param value
	 *            the value
	 * @param item
	 *            the item number to get for an array of values, or -1 for
	 *            non-arrays
	 * 
	 */
	public void setList(E id, List<String> value, int item) {
		setString(id, BundleHelper.fromList(value), item);
	}

	/**
	 * Create/update the .properties file.
	 * <p>
	 * Will use the most likely candidate as base if the file does not already
	 * exists and this resource is translatable (for instance, "en_US" will use
	 * "en" as a base if the resource is a translation file).
	 * <p>
	 * Will update the files in {@link Bundles#getDirectory()}; it <b>MUST</b>
	 * be set.
	 * 
	 * @throws IOException
	 *             in case of IO errors
	 */
	public void updateFile() throws IOException {
		updateFile(Bundles.getDirectory());
	}

	/**
	 * Create/update the .properties file.
	 * <p>
	 * Will use the most likely candidate as base if the file does not already
	 * exists and this resource is translatable (for instance, "en_US" will use
	 * "en" as a base if the resource is a translation file).
	 * 
	 * @param path
	 *            the path where the .properties files are, <b>MUST NOT</b> be
	 *            NULL
	 * 
	 * @throws IOException
	 *             in case of IO errors
	 */
	public void updateFile(String path) throws IOException {
		File file = getUpdateFile(path);

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(file), "UTF-8"));

		writeHeader(writer);
		writer.write("\n");
		writer.write("\n");

		for (Field field : type.getDeclaredFields()) {
			Meta meta = field.getAnnotation(Meta.class);
			if (meta != null) {
				E id = Enum.valueOf(type, field.getName());
				String info = getMetaInfo(meta);

				if (info != null) {
					writer.write(info);
					writer.write("\n");
				}

				writeValue(writer, id);
			}
		}

		writer.close();
	}

	/**
	 * Delete the .properties file.
	 * <p>
	 * Will use the most likely candidate as base if the file does not already
	 * exists and this resource is translatable (for instance, "en_US" will use
	 * "en" as a base if the resource is a translation file).
	 * <p>
	 * Will delete the files in {@link Bundles#getDirectory()}; it <b>MUST</b>
	 * be set.
	 * 
	 * @return TRUE if the file was deleted
	 */
	public boolean deleteFile() {
		return deleteFile(Bundles.getDirectory());
	}

	/**
	 * Delete the .properties file.
	 * <p>
	 * Will use the most likely candidate as base if the file does not already
	 * exists and this resource is translatable (for instance, "en_US" will use
	 * "en" as a base if the resource is a translation file).
	 * 
	 * @param path
	 *            the path where the .properties files are, <b>MUST NOT</b> be
	 *            NULL
	 * 
	 * @return TRUE if the file was deleted
	 */
	public boolean deleteFile(String path) {
		File file = getUpdateFile(path);
		return file.delete();
	}

	/**
	 * The description {@link TransBundle}, that is, a {@link TransBundle}
	 * dedicated to the description of the values of the given {@link Bundle}
	 * (can be NULL).
	 * 
	 * @return the description {@link TransBundle}
	 */
	public TransBundle<E> getDescriptionBundle() {
		return descriptionBundle;
	}

	/**
	 * Reload the {@link Bundle} data files.
	 * 
	 * @param resetToDefault
	 *            reset to the default configuration (do not look into the
	 *            possible user configuration files, only take the original
	 *            configuration)
	 */
	public void reload(boolean resetToDefault) {
		setBundle(keyType, Locale.getDefault(), resetToDefault);
	}

	/**
	 * Check if the internal map contains the given key.
	 * 
	 * @param key
	 *            the key to check for
	 * 
	 * @return true if it does
	 */
	protected boolean containsKey(String key) {
		return changeMap.containsKey(key) || map.containsKey(key);
	}

	/**
	 * The default {@link MetaInfo.def} value for the given enumeration name.
	 * 
	 * @param id
	 *            the enumeration name (the "id")
	 * 
	 * @return the def value in the {@link MetaInfo} or "" if none (never NULL)
	 */
	protected String getMetaDef(String id) {
		String rep = "";
		try {
			Meta meta = type.getDeclaredField(id).getAnnotation(Meta.class);
			rep = meta.def();
		} catch (NoSuchFieldException e) {
		} catch (SecurityException e) {
		}

		if (rep == null) {
			rep = "";
		}

		return rep;
	}

	/**
	 * Get the value for the given key if it exists in the internal map, or
	 * <tt>def</tt> if not.
	 * <p>
	 * DO NOT get the default meta value (MetaInfo.def()).
	 * 
	 * @param key
	 *            the key to check for
	 * @param def
	 *            the default value when it is not present in the internal map
	 * 
	 * @return the value, or <tt>def</tt> if not found
	 */
	protected String getString(String key, String def) {
		if (changeMap.containsKey(key)) {
			return changeMap.get(key);
		}

		if (map.containsKey(key)) {
			return map.get(key);
		}

		return def;
	}

	/**
	 * Set the value for this key, in the change map (it is kept in memory, not
	 * yet on disk).
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the associated value
	 */
	protected void setString(String key, String value) {
		changeMap.put(key, value == null ? null : value.trim());
	}

	/**
	 * Return formated, display-able information from the {@link Meta} field
	 * given. Each line will always starts with a "#" character.
	 * 
	 * @param meta
	 *            the {@link Meta} field
	 * 
	 * @return the information to display or NULL if none
	 */
	protected String getMetaInfo(Meta meta) {
		String desc = meta.description();
		boolean group = meta.group();
		Meta.Format format = meta.format();
		String[] list = meta.list();
		boolean nullable = meta.nullable();
		String def = meta.def();
		boolean array = meta.array();

		// Default, empty values -> NULL
		if (desc.length() + list.length + def.length() == 0 && !group
				&& nullable && format == Format.STRING) {
			return null;
		}

		StringBuilder builder = new StringBuilder();
		for (String line : desc.split("\n")) {
			builder.append("# ").append(line).append("\n");
		}

		if (group) {
			builder.append("# This item is used as a group, its content is not expected to be used.");
		} else {
			builder.append("# (FORMAT: ").append(format)
					.append(nullable ? "" : ", required");
			builder.append(") ");

			if (list.length > 0) {
				builder.append("\n# ALLOWED VALUES: ");
				boolean first = true;
				for (String value : list) {
					if (!first) {
						builder.append(", ");
					}
					builder.append(BundleHelper.escape(value));
					first = false;
				}
			}

			if (array) {
				builder.append("\n# (This item accepts a list of ^escaped comma-separated values)");
			}
		}

		return builder.toString();
	}

	/**
	 * The display name used in the <tt>.properties file</tt>.
	 * 
	 * @return the name
	 */
	protected String getBundleDisplayName() {
		return keyType.toString();
	}

	/**
	 * Write the header found in the configuration <tt>.properties</tt> file of
	 * this {@link Bundles}.
	 * 
	 * @param writer
	 *            the {@link Writer} to write the header in
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	protected void writeHeader(Writer writer) throws IOException {
		writer.write("# " + getBundleDisplayName() + "\n");
		writer.write("#\n");
	}

	/**
	 * Write the given data to the config file, i.e., "MY_ID = my_curent_value"
	 * followed by a new line.
	 * <p>
	 * Will prepend a # sign if the is is not set (see
	 * {@link Bundle#isSet(Enum, boolean)}).
	 * 
	 * @param writer
	 *            the {@link Writer} to write into
	 * @param id
	 *            the id to write
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	protected void writeValue(Writer writer, E id) throws IOException {
		boolean set = isSet(id, false);
		writeValue(writer, id.name(), getString(id), set);
	}

	/**
	 * Write the given data to the config file, i.e., "MY_ID = my_curent_value"
	 * followed by a new line.
	 * <p>
	 * Will prepend a # sign if the is is not set.
	 * 
	 * @param writer
	 *            the {@link Writer} to write into
	 * @param id
	 *            the id to write
	 * @param value
	 *            the id's value
	 * @param set
	 *            the value is set in this {@link Bundle}
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	protected void writeValue(Writer writer, String id, String value,
			boolean set) throws IOException {

		if (!set) {
			writer.write('#');
		}

		writer.write(id);
		writer.write(" = ");

		if (value == null) {
			value = "";
		}

		String[] lines = value.replaceAll("\t", "\\\\\\t").split("\n");
		for (int i = 0; i < lines.length; i++) {
			writer.write(lines[i]);
			if (i < lines.length - 1) {
				writer.write("\\n\\");
			}
			writer.write("\n");
		}
	}

	/**
	 * Return the source file for this {@link Bundles} from the given path.
	 * 
	 * @param path
	 *            the path where the .properties files are
	 * 
	 * @return the source {@link File}
	 */
	protected File getUpdateFile(String path) {
		return new File(path, keyType.name() + ".properties");
	}

	/**
	 * Change the currently used bundle, and reset all changes.
	 * 
	 * @param name
	 *            the name of the bundle to load
	 * @param locale
	 *            the {@link Locale} to use
	 * @param resetToDefault
	 *            reset to the default configuration (do not look into the
	 *            possible user configuration files, only take the original
	 *            configuration)
	 */
	protected void setBundle(Enum<?> name, Locale locale, boolean resetToDefault) {
		changeMap.clear();
		String dir = Bundles.getDirectory();
		String bname = type.getPackage().getName() + "." + name.name();

		boolean found = false;
		if (!resetToDefault && dir != null) {
			// Look into Bundles.getDirectory() for .properties files
			try {
				File file = getPropertyFile(dir, name.name(), locale);
				if (file != null) {
					Reader reader = new InputStreamReader(new FileInputStream(
							file), "UTF-8");
					resetMap(new PropertyResourceBundle(reader));
					found = true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (!found) {
			// Look into the package itself for resources
			try {
				resetMap(ResourceBundle
						.getBundle(bname, locale, type.getClassLoader(),
								new FixedResourceBundleControl()));
				found = true;
			} catch (MissingResourceException e) {
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (!found) {
			// We have no bundle for this Bundle
			System.err.println("No bundle found for: " + bname);
			resetMap(null);
		}
	}

	/**
	 * Reset the backing map to the content of the given bundle, or with NULL
	 * values if bundle is NULL.
	 * 
	 * @param bundle
	 *            the bundle to copy
	 */
	protected void resetMap(ResourceBundle bundle) {
		this.map.clear();
		for (Field field : type.getDeclaredFields()) {
			try {
				Meta meta = field.getAnnotation(Meta.class);
				if (meta != null) {
					E id = Enum.valueOf(type, field.getName());

					String value;
					if (bundle != null) {
						value = bundle.getString(id.name());
					} else {
						value = null;
					}

					this.map.put(id.name(), value == null ? null : value.trim());
				}
			} catch (MissingResourceException e) {
			}
		}
	}

	/**
	 * Take a snapshot of the changes in memory in this {@link Bundle} made by
	 * the "set" methods ( {@link Bundle#setString(Enum, String)}...) at the
	 * current time.
	 * 
	 * @return a snapshot to use with {@link Bundle#restoreSnapshot(Object)}
	 */
	public Object takeSnapshot() {
		return new HashMap<String, String>(changeMap);
	}

	/**
	 * Restore a snapshot taken with {@link Bundle}, or reset the current
	 * changes if the snapshot is NULL.
	 * 
	 * @param snap
	 *            the snapshot or NULL
	 */
	@SuppressWarnings("unchecked")
	public void restoreSnapshot(Object snap) {
		if (snap == null) {
			changeMap.clear();
		} else {
			if (snap instanceof Map) {
				changeMap = (Map<String, String>) snap;
			} else {
				throw new RuntimeException(
						"Restoring changes in a Bundle must be done on a changes snapshot, "
								+ "or NULL to discard current changes");
			}
		}
	}

	/**
	 * Return the resource file that is closer to the {@link Locale}.
	 * 
	 * @param dir
	 *            the directory to look into
	 * @param name
	 *            the file base name (without <tt>.properties</tt>)
	 * @param locale
	 *            the {@link Locale}
	 * 
	 * @return the closest match or NULL if none
	 */
	private File getPropertyFile(String dir, String name, Locale locale) {
		List<String> locales = new ArrayList<String>();
		if (locale != null) {
			String country = locale.getCountry() == null ? "" : locale
					.getCountry();
			String language = locale.getLanguage() == null ? "" : locale
					.getLanguage();
			if (!language.isEmpty() && !country.isEmpty()) {
				locales.add("_" + language + "-" + country);
			}
			if (!language.isEmpty()) {
				locales.add("_" + language);
			}
		}

		locales.add("");

		File file = null;
		for (String loc : locales) {
			file = new File(dir, name + loc + ".properties");
			if (file.exists()) {
				break;
			}

			file = null;
		}

		return file;
	}
}
