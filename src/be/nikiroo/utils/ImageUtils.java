package be.nikiroo.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import be.nikiroo.utils.serial.SerialUtils;

/**
 * This class offer some utilities based around images.
 * 
 * @author niki
 */
public abstract class ImageUtils {
	private static ImageUtils instance = newObject();

	/**
	 * Get a (unique) instance of an {@link ImageUtils} compatible with your
	 * system.
	 * 
	 * @return an {@link ImageUtils}
	 */
	public static ImageUtils getInstance() {
		return instance;
	}

	/**
	 * Save the given resource as an image on disk using the given image format
	 * for content, or with "png" format if it fails.
	 * 
	 * @param img
	 *            the resource
	 * @param target
	 *            the target file
	 * @param format
	 *            the file format ("png", "jpeg", "bmp"...)
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public abstract void saveAsImage(Image img, File target, String format)
			throws IOException;

	/**
	 * Return the EXIF transformation flag of this image if any.
	 * 
	 * <p>
	 * Note: this code has been found on internet; thank you anonymous coder.
	 * </p>
	 * 
	 * @param in
	 *            the data {@link InputStream}
	 * 
	 * @return the transformation flag if any
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	protected static int getExifTransorm(InputStream in) throws IOException {
		int[] exif_data = new int[100];
		int set_flag = 0;
		int is_motorola = 0;

		/* Read File head, check for JPEG SOI + Exif APP1 */
		for (int i = 0; i < 4; i++)
			exif_data[i] = in.read();

		if (exif_data[0] != 0xFF || exif_data[1] != 0xD8
				|| exif_data[2] != 0xFF || exif_data[3] != 0xE1)
			return -2;

		/* Get the marker parameter length count */
		int length = (in.read() << 8 | in.read());

		/* Length includes itself, so must be at least 2 */
		/* Following Exif data length must be at least 6 */
		if (length < 8)
			return -1;
		length -= 8;
		/* Read Exif head, check for "Exif" */
		for (int i = 0; i < 6; i++)
			exif_data[i] = in.read();

		if (exif_data[0] != 0x45 || exif_data[1] != 0x78
				|| exif_data[2] != 0x69 || exif_data[3] != 0x66
				|| exif_data[4] != 0 || exif_data[5] != 0)
			return -1;

		/* Read Exif body */
		length = length > exif_data.length ? exif_data.length : length;
		for (int i = 0; i < length; i++)
			exif_data[i] = in.read();

		if (length < 12)
			return -1; /* Length of an IFD entry */

		/* Discover byte order */
		if (exif_data[0] == 0x49 && exif_data[1] == 0x49)
			is_motorola = 0;
		else if (exif_data[0] == 0x4D && exif_data[1] == 0x4D)
			is_motorola = 1;
		else
			return -1;

		/* Check Tag Mark */
		if (is_motorola == 1) {
			if (exif_data[2] != 0)
				return -1;
			if (exif_data[3] != 0x2A)
				return -1;
		} else {
			if (exif_data[3] != 0)
				return -1;
			if (exif_data[2] != 0x2A)
				return -1;
		}

		/* Get first IFD offset (offset to IFD0) */
		int offset;
		if (is_motorola == 1) {
			if (exif_data[4] != 0)
				return -1;
			if (exif_data[5] != 0)
				return -1;
			offset = exif_data[6];
			offset <<= 8;
			offset += exif_data[7];
		} else {
			if (exif_data[7] != 0)
				return -1;
			if (exif_data[6] != 0)
				return -1;
			offset = exif_data[5];
			offset <<= 8;
			offset += exif_data[4];
		}
		if (offset > length - 2)
			return -1; /* check end of data segment */

		/* Get the number of directory entries contained in this IFD */
		int number_of_tags;
		if (is_motorola == 1) {
			number_of_tags = exif_data[offset];
			number_of_tags <<= 8;
			number_of_tags += exif_data[offset + 1];
		} else {
			number_of_tags = exif_data[offset + 1];
			number_of_tags <<= 8;
			number_of_tags += exif_data[offset];
		}
		if (number_of_tags == 0)
			return -1;
		offset += 2;

		/* Search for Orientation Tag in IFD0 */
		for (;;) {
			if (offset > length - 12)
				return -1; /* check end of data segment */
			/* Get Tag number */
			int tagnum;
			if (is_motorola == 1) {
				tagnum = exif_data[offset];
				tagnum <<= 8;
				tagnum += exif_data[offset + 1];
			} else {
				tagnum = exif_data[offset + 1];
				tagnum <<= 8;
				tagnum += exif_data[offset];
			}
			if (tagnum == 0x0112)
				break; /* found Orientation Tag */
			if (--number_of_tags == 0)
				return -1;
			offset += 12;
		}

		/* Get the Orientation value */
		if (is_motorola == 1) {
			if (exif_data[offset + 8] != 0)
				return -1;
			set_flag = exif_data[offset + 9];
		} else {
			if (exif_data[offset + 9] != 0)
				return -1;
			set_flag = exif_data[offset + 8];
		}
		if (set_flag > 8)
			return -1;

		return set_flag;
	}

	/**
	 * Check that the class can operate (for instance, that all the required
	 * libraries or frameworks are present).
	 * 
	 * @return TRUE if it works
	 */
	abstract protected boolean check();

	/**
	 * Create a new {@link ImageUtils}.
	 * 
	 * @return the {@link ImageUtils}
	 */
	private static ImageUtils newObject() {
		for (String clazz : new String[] { "be.nikiroo.utils.ui.ImageUtilsAwt",
				"be.nikiroo.utils.android.ImageUtilsAndroid" }) {
			try {
				ImageUtils obj = (ImageUtils) SerialUtils.createObject(clazz);
				if (obj.check()) {
					return obj;
				}
			} catch (Throwable e) {
			}
		}

		return null;
	}
}
