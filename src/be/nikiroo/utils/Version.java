package be.nikiroo.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class describe a program {@link Version}.
 * 
 * @author niki
 */
public class Version implements Comparable<Version> {
	private String version;
	private int major;
	private int minor;
	private int patch;
	private String tag;
	private int tagVersion;

	/**
	 * Create a new, empty {@link Version}.
	 * 
	 */
	public Version() {
	}

	/**
	 * Create a new {@link Version} with the given values.
	 * 
	 * @param major
	 *            the major version
	 * @param minor
	 *            the minor version
	 * @param patch
	 *            the patch version
	 */
	public Version(int major, int minor, int patch) {
		this(major, minor, patch, null, -1);
	}

	/**
	 * Create a new {@link Version} with the given values.
	 * 
	 * @param major
	 *            the major version
	 * @param minor
	 *            the minor version
	 * @param patch
	 *            the patch version
	 * @param tag
	 *            a tag name for this version
	 */
	public Version(int major, int minor, int patch, String tag) {
		this(major, minor, patch, tag, -1);
	}

	/**
	 * Create a new {@link Version} with the given values.
	 * 
	 * @param major
	 *            the major version
	 * @param minor
	 *            the minor version
	 * @param patch
	 *            the patch version the patch version
	 * @param tag
	 *            a tag name for this version
	 * @param tagVersion
	 *            the version of the tagged version
	 */
	public Version(int major, int minor, int patch, String tag, int tagVersion) {
		if (tagVersion >= 0 && tag == null) {
			throw new java.lang.IllegalArgumentException(
					"A tag version cannot be used without a tag");
		}

		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.tag = tag;
		this.tagVersion = tagVersion;

		this.version = generateVersion();
	}

	/**
	 * Create a new {@link Version} with the given value, which must be in the
	 * form <tt>MAJOR.MINOR.PATCH(-TAG(TAG_VERSION))</tt>.
	 * 
	 * @param version
	 *            the version (<tt>MAJOR.MINOR.PATCH</tt>,
	 *            <tt>MAJOR.MINOR.PATCH-TAG</tt> or
	 *            <tt>MAJOR.MINOR.PATCH-TAGVERSIONTAG</tt>)
	 */
	public Version(String version) {
		try {
			String[] tab = version.split("\\.");
			this.major = Integer.parseInt(tab[0].trim());
			this.minor = Integer.parseInt(tab[1].trim());
			if (tab[2].contains("-")) {
				int posInVersion = version.indexOf('.');
				posInVersion = version.indexOf('.', posInVersion + 1);
				String rest = version.substring(posInVersion + 1);

				int posInRest = rest.indexOf('-');
				this.patch = Integer.parseInt(rest.substring(0, posInRest)
						.trim());

				posInVersion = version.indexOf('-');
				this.tag = version.substring(posInVersion + 1).trim();
				this.tagVersion = -1;

				StringBuilder str = new StringBuilder();
				while (!tag.isEmpty() && tag.charAt(tag.length() - 1) >= '0'
						&& tag.charAt(tag.length() - 1) <= '9') {
					str.insert(0, tag.charAt(tag.length() - 1));
					tag = tag.substring(0, tag.length() - 1);
				}

				if (str.length() > 0) {
					this.tagVersion = Integer.parseInt(str.toString());
				}
			} else {
				this.patch = Integer.parseInt(tab[2].trim());
				this.tag = null;
				this.tagVersion = -1;
			}

			this.version = generateVersion();
		} catch (Exception e) {
			this.major = 0;
			this.minor = 0;
			this.patch = 0;
			this.tag = null;
			this.tagVersion = -1;
			this.version = null;
		}
	}

	/**
	 * The 'major' version.
	 * <p>
	 * This version should only change when API-incompatible changes are made to
	 * the program.
	 * 
	 * @return the major version
	 */
	public int getMajor() {
		return major;
	}

	/**
	 * The 'minor' version.
	 * <p>
	 * This version should only change when new, backwards-compatible
	 * functionality has been added to the program.
	 * 
	 * @return the minor version
	 */
	public int getMinor() {
		return minor;
	}

	/**
	 * The 'patch' version.
	 * <p>
	 * This version should change when backwards-compatible bugfixes have been
	 * added to the program.
	 * 
	 * @return the patch version
	 */
	public int getPatch() {
		return patch;
	}

	/**
	 * A tag name for this version.
	 * 
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * The version of the tag, or -1 for no version.
	 * 
	 * @return the tag version
	 */
	public int getTagVersion() {
		return tagVersion;
	}

	/**
	 * Check if this {@link Version} is "empty" (i.e., the version was not
	 * parse-able or not given).
	 * 
	 * @return TRUE if it is empty
	 */
	public boolean isEmpty() {
		return version == null;
	}

	/**
	 * Check if we are more recent than the given {@link Version}.
	 * <p>
	 * Note that a tagged version is considered newer than a non-tagged version,
	 * but two tagged versions with different tags are not comparable.
	 * <p>
	 * Also, an empty version is always considered older.
	 * 
	 * @param o
	 *            the other {@link Version}
	 * @return TRUE if this {@link Version} is more recent than the given one
	 */
	public boolean isNewerThan(Version o) {
		if (isEmpty()) {
			return false;
		} else if (o.isEmpty()) {
			return true;
		}

		if (major > o.major) {
			return true;
		}

		if (major == o.major && minor > o.minor) {
			return true;
		}

		if (major == o.major && minor == o.minor && patch > o.patch) {
			return true;
		}

		// a tagged version is considered newer than a non-tagged one
		if (major == o.major && minor == o.minor && patch == o.patch
				&& tag != null && o.tag == null) {
			return true;
		}

		// 2 <> tagged versions are not comparable
		boolean sameTag = (tag == null && o.tag == null)
				|| (tag != null && tag.equals(o.tag));
		if (major == o.major && minor == o.minor && patch == o.patch && sameTag
				&& tagVersion > o.tagVersion) {
			return true;
		}

		return false;
	}

	/**
	 * Check if we are older than the given {@link Version}.
	 * <p>
	 * Note that a tagged version is considered newer than a non-tagged version,
	 * but two tagged versions with different tags are not comparable.
	 * <p>
	 * Also, an empty version is always considered older.
	 * 
	 * @param o
	 *            the other {@link Version}
	 * @return TRUE if this {@link Version} is older than the given one
	 */
	public boolean isOlderThan(Version o) {
		if (o.isEmpty()) {
			return false;
		} else if (isEmpty()) {
			return true;
		}

		// 2 <> tagged versions are not comparable
		boolean sameTag = (tag == null && o.tag == null)
				|| (tag != null && tag.equals(o.tag));
		if (major == o.major && minor == o.minor && patch == o.patch
				&& !sameTag) {
			return false;
		}

		return !equals(o) && !isNewerThan(o);
	}

	/**
	 * Return the version of the running program if it follows the VERSION
	 * convention (i.e., if it has a file called VERSION containing the version
	 * as a {@link String} in its binary root, and if this {@link String}
	 * follows the Major/Minor/Patch convention).
	 * <p>
	 * If it does not, return an empty {@link Version} object.
	 * 
	 * @return the {@link Version} of the program, or an empty {@link Version}
	 *         (does not return NULL)
	 */
	public static Version getCurrentVersion() {
		String version = null;

		InputStream in = IOUtils.openResource("VERSION");
		if (in != null) {
			try {
				ByteArrayOutputStream ba = new ByteArrayOutputStream();
				IOUtils.write(in, ba);
				in.close();

				version = ba.toString("UTF-8").trim();
			} catch (IOException e) {
			}
		}

		return new Version(version);
	}

	@Override
	public int compareTo(Version o) {
		if (equals(o)) {
			return 0;
		} else if (isNewerThan(o)) {
			return 1;
		} else {
			return -1;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Version) {
			Version o = (Version) obj;
			if (isEmpty()) {
				return o.isEmpty();
			}

			boolean sameTag = (tag == null && o.tag == null)
					|| (tag != null && tag.equals(o.tag));
			return o.major == major && o.minor == minor && o.patch == patch
					&& sameTag && o.tagVersion == tagVersion;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return version == null ? 0 : version.hashCode();
	}

	/**
	 * Return a user-readable form of this {@link Version}.
	 */
	@Override
	public String toString() {
		return version == null ? "[unknown]" : version;
	}

	/**
	 * Generate the clean version {@link String} from the current values.
	 * 
	 * @return the clean version string
	 */
	private String generateVersion() {
		String tagSuffix = "";
		if (tag != null) {
			tagSuffix = "-" + tag
					+ (tagVersion >= 0 ? Integer.toString(tagVersion) : "");
		}

		return String.format("%d.%d.%d%s", major, minor, patch, tagSuffix);
	}
}
