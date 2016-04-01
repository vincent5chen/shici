package com.itranswarp.shici.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class ImageUtil {

	public static BufferedImage loadImage(byte[] data) throws IOException {
		BufferedImage img = null;
		try (InputStream input = new ByteArrayInputStream(data)) {
			img = ImageIO.read(input);
		}
		return img;
	}

	/**
	 * Scale image to fit height, keep aspect ratio. No transform if the source
	 * image is smaller.
	 * 
	 * @param source
	 *            Source image.
	 * @param targetHeight
	 *            Height in pixel.
	 * @return New image that fit the height.
	 */
	public static BufferedImage scaleToHeight(BufferedImage source, int targetHeight) {
		return scaleToHeight(source, targetHeight, false);
	}

	/**
	 * Scale image to fit height, keep aspect ratio. No transform if the source
	 * image is smaller and force is set to false.
	 * 
	 * @param source
	 *            Source image.
	 * @param targetHeight
	 *            Height in pixel.
	 * @param force
	 *            Force to fit the height even if the source image is smaller.
	 * @return New image that fit the height.
	 */
	public static BufferedImage scaleToHeight(BufferedImage source, int targetHeight, boolean force) {
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		if (sourceHeight == targetHeight) {
			return source;
		}
		if (sourceHeight < targetHeight && !force) {
			return source;
		}
		int targetWidth = (int) ((float) sourceWidth * targetHeight / sourceHeight);
		BufferedImage target = new BufferedImage(targetWidth, targetHeight, source.getType());
		Image scaleImage = source.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
		Graphics2D g = target.createGraphics();
		g.drawImage(scaleImage, 0, 0, targetWidth, targetHeight, null);
		g.dispose();
		return target;
	}

	/**
	 * Scale image to fit width, keep aspect ratio. No transform if the source
	 * image is smaller.
	 * 
	 * @param source
	 *            Source image.
	 * @param targetWidth
	 *            Width in pixel.
	 * @return New image that fit the width.
	 */
	public static BufferedImage scaleToWidth(BufferedImage source, int targetWidth) {
		return scaleToWidth(source, targetWidth, false);
	}

	/**
	 * Scale image to fit width, keep aspect ratio. No transform if the source
	 * image is smaller and force is set to false.
	 * 
	 * @param source
	 *            Source image.
	 * @param targetWidth
	 *            Width in pixel.
	 * @param force
	 *            Force to fit the width even if the source image is smaller.
	 * @return New image that fit the width.
	 */
	public static BufferedImage scaleToWidth(BufferedImage source, int targetWidth, boolean force) {
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		if (sourceWidth == targetWidth) {
			return source;
		}
		if (sourceWidth < targetWidth && !force) {
			return source;
		}
		int targetHeight = (int) ((float) sourceHeight * targetWidth / sourceWidth);
		BufferedImage target = new BufferedImage(targetWidth, targetHeight, source.getType());
		Image scaleImage = source.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
		Graphics2D g = target.createGraphics();
		g.drawImage(scaleImage, 0, 0, targetWidth, targetHeight, null);
		g.dispose();
		return target;
	}

	public static BufferedImage scaleToSize(BufferedImage source, int targetWidth, int targetHeight) {
		return scaleToSize(source, targetWidth, targetHeight, false);
	}

	public static BufferedImage scaleToSize(BufferedImage source, int targetWidth, int targetHeight,
			boolean forceScale) {
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		if (sourceWidth == targetWidth && sourceHeight == targetHeight) {
			return source;
		}
		Image scaled = null;
		if (forceScale) {
			scaled = source.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
		} else {
			if (sourceWidth * targetHeight > sourceHeight * targetWidth) {
				targetHeight = (int) ((float) sourceHeight * targetWidth / sourceWidth);
				scaled = source.getScaledInstance(-1, targetHeight, Image.SCALE_SMOOTH);
			} else if (sourceWidth * targetHeight < sourceHeight * targetWidth) {
				targetWidth = (int) ((float) sourceWidth * targetHeight / sourceHeight);
				scaled = source.getScaledInstance(targetWidth, -1, Image.SCALE_SMOOTH);
			} else {
				scaled = source.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
			}
		}
		BufferedImage target = new BufferedImage(targetWidth, targetHeight, source.getType());
		Graphics2D g = target.createGraphics();
		g.drawImage(scaled, 0, 0, targetWidth, targetHeight, null);
		g.dispose();
		return target;
	}

	/**
	 * Scale the image with original aspect ratio, then cut to fit the target
	 * size if necessary.
	 * 
	 * @param source
	 *            Source image.
	 * @param targetWidth
	 *            Width in pixel.
	 * @param targetHeight
	 *            Height in pixel.
	 * @return The image with exactly the size.
	 */
	public static BufferedImage fitToSize(BufferedImage source, int targetWidth, int targetHeight) {
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		if (sourceWidth == targetWidth && sourceHeight == targetHeight) {
			return source;
		}
		// scale:
		int sx1 = 0;
		int sy1 = 0;
		int sx2 = sourceWidth;
		int sy2 = sourceHeight;
		if (sourceWidth * targetHeight < sourceHeight * targetWidth) {
			// cut height:
			int newHeight = (int) ((float) sourceWidth * targetHeight / targetWidth);
			sy1 = (sourceHeight - newHeight) / 2;
			sy2 = sy1 + newHeight;
		} else if (sourceWidth * targetHeight > sourceHeight * targetWidth) {
			// cut width:
			int newWidth = (int) ((float) sourceHeight * targetWidth / targetHeight);
			sx1 = (sourceWidth - newWidth) / 2;
			sx2 = sx1 + newWidth;
		}
		BufferedImage target = new BufferedImage(targetWidth, targetHeight, source.getType());
		Graphics2D g = target.createGraphics();
		g.drawImage(source, 0, 0, targetWidth, targetHeight, sx1, sy1, sx2, sy2, null);
		g.dispose();
		return target;
	}

	/**
	 * Save image to JPEG data.
	 * 
	 * @param image
	 *            Source image.
	 * @return JPEG data as bytes.
	 * @throws IOException
	 */
	public static byte[] toJpeg(BufferedImage image) throws IOException {
		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			ImageIO.write(image, "jpg", output);
			return output.toByteArray();
		}
	}
}
