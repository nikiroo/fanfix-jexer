package be.nikiroo.utils.ui;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.ImageUtils;
import be.nikiroo.utils.StringUtils;

/**
 * This class offer some utilities based around images and uses java.awt.
 * 
 * @author niki
 */
public class ImageUtilsAwt extends ImageUtils {
	/**
	 * A rotation to perform on an image.
	 * 
	 * @author niki
	 */
	public enum Rotation {
		/** No rotation */
		NONE,
		/** Rotate the image to the right */
		RIGHT,
		/** Rotate the image to the left */
		LEFT,
		/** Rotate the image by 180° */
		UTURN
	}

	@Override
	protected boolean check() {
		// Will not work if ImageIO is not available
		ImageIO.getCacheDirectory();
		return true;
	}

	@Override
	public void saveAsImage(Image img, File target, String format)
			throws IOException {
		try {
			BufferedImage image = fromImage(img);

			boolean ok = false;
			try {

				ok = ImageIO.write(image, format, target);
			} catch (IOException e) {
				ok = false;
			}

			// Some formats are not reliable
			// Second chance: PNG
			if (!ok && !format.equals("png")) {
				try {
					ok = ImageIO.write(image, "png", target);
				} catch (IllegalArgumentException e) {
					throw e;
				} catch (Exception e) {
					throw new IOException("Undocumented exception occured, "
							+ "converting to IOException", e);
				}
			}

			if (!ok) {
				throw new IOException(
						"Cannot find a writer for this image and format: "
								+ format);
			}
		} catch (IOException e) {
			throw new IOException("Cannot write image to " + target, e);
		}
	}

	/**
	 * Convert the given {@link Image} into a {@link BufferedImage} object,
	 * respecting the EXIF transformations if any.
	 * 
	 * @param img
	 *            the {@link Image}
	 * 
	 * @return the {@link Image} object
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	public static BufferedImage fromImage(Image img) throws IOException {
		return fromImage(img, Rotation.NONE);
	}

	/**
	 * Convert the given {@link Image} into a {@link BufferedImage} object,
	 * respecting the EXIF transformations if any.
	 * 
	 * @param img
	 *            the {@link Image}
	 * @param rotation
	 *            the rotation to apply, if any (can be null, same as
	 *            {@link Rotation#NONE})
	 * 
	 * @return the {@link Image} object
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	public static BufferedImage fromImage(Image img, Rotation rotation)
			throws IOException {
		InputStream in = img.newInputStream();
		BufferedImage image;
		try {
			int orientation;
			try {
				orientation = getExifTransorm(in);
			} catch (Exception e) {
				// no EXIF transform, ok
				orientation = -1;
			}

			in.reset();

			try {
				image = ImageIO.read(in);
			} catch (IllegalArgumentException e) {
				throw e;
			} catch (Exception e) {
				throw new IOException("Undocumented exception occured, "
						+ "converting to IOException", e);
			}

			if (image == null) {
				String extra = "";
				if (img.getSize() <= 2048) {
					try {
						byte[] data = null;
						InputStream inData = img.newInputStream();
						try {
							data = IOUtils.toByteArray(inData);
						} finally {
							inData.close();
						}
						extra = ", content: " + new String(data, "UTF-8");
					} catch (Exception e) {
						extra = ", content unavailable";
					}
				}
				String ssize = StringUtils.formatNumber(img.getSize());
				throw new IOException(
						"Failed to convert input to image, size was: " + ssize
								+ extra);
			}

			// Note: this code has been found on Internet;
			// thank you anonymous coder.
			int width = image.getWidth();
			int height = image.getHeight();
			AffineTransform affineTransform = new AffineTransform();

			switch (orientation) {
			case 1:
				affineTransform = null;
				break;
			case 2: // Flip X
				affineTransform.scale(-1.0, 1.0);
				affineTransform.translate(-width, 0);
				break;
			case 3: // PI rotation
				affineTransform.translate(width, height);
				affineTransform.rotate(Math.PI);
				break;
			case 4: // Flip Y
				affineTransform.scale(1.0, -1.0);
				affineTransform.translate(0, -height);
				break;
			case 5: // - PI/2 and Flip X
				affineTransform.rotate(-Math.PI / 2);
				affineTransform.scale(-1.0, 1.0);
				break;
			case 6: // -PI/2 and -width
				affineTransform.translate(height, 0);
				affineTransform.rotate(Math.PI / 2);
				break;
			case 7: // PI/2 and Flip
				affineTransform.scale(-1.0, 1.0);
				affineTransform.translate(-height, 0);
				affineTransform.translate(0, width);
				affineTransform.rotate(3 * Math.PI / 2);
				break;
			case 8: // PI / 2
				affineTransform.translate(0, width);
				affineTransform.rotate(3 * Math.PI / 2);
				break;
			default:
				affineTransform = null;
				break;
			}

			if (rotation == null)
				rotation = Rotation.NONE;

			switch (rotation) {
			case RIGHT:
				if (affineTransform == null) {
					affineTransform = new AffineTransform();
				}
				affineTransform.translate(height, 0);
				affineTransform.rotate(Math.PI / 2);

				int tmp = width;
				width = height;
				height = tmp;

				break;
			case LEFT:
				if (affineTransform == null) {
					affineTransform = new AffineTransform();
				}
				affineTransform.translate(0, width);
				affineTransform.rotate(3 * Math.PI / 2);

				int temp = width;
				width = height;
				height = temp;

				break;
			case UTURN:
				if (affineTransform == null) {
					affineTransform = new AffineTransform();
				}
				affineTransform.translate(width, height);
				affineTransform.rotate(Math.PI);
				break;
			default:
				break;
			}

			if (affineTransform != null) {
				AffineTransformOp affineTransformOp = new AffineTransformOp(
						affineTransform, AffineTransformOp.TYPE_BILINEAR);

				BufferedImage transformedImage = new BufferedImage(width,
						height, image.getType());
				transformedImage = affineTransformOp.filter(image,
						transformedImage);

				image = transformedImage;
			}
			//
		} finally {
			in.close();
		}

		return image;
	}

	/**
	 * Resize the given image.
	 * 
	 * @param areaSize
	 *            the base size of the target dimension for snap sizes
	 * @param image
	 *            the image to resize
	 * @param zoom
	 *            the zoom factor or -1 for snap size
	 * @param zoomSnapWidth
	 *            if snap size, TRUE to snap to width (and FALSE, snap to
	 *            height)
	 * 
	 * @return a new, resized image
	 */
	public static BufferedImage scaleImage(Dimension areaSize,
			BufferedImage image, double zoom, boolean zoomSnapWidth) {
		Integer scaledSize[] = scaleSize(areaSize.width, areaSize.height,
				image.getWidth(), image.getHeight(), zoom, zoomSnapWidth);
		int width = scaledSize[0];
		int height = scaledSize[1];
		BufferedImage resizedImage = new BufferedImage(width, height,
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = resizedImage.createGraphics();
		try {
			g.drawImage(image, 0, 0, width, height, null);
		} finally {
			g.dispose();
		}

		return resizedImage;
	}
}
