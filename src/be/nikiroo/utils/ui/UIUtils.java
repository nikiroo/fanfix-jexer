package be.nikiroo.utils.ui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Some Java Swing utilities.
 * 
 * @author niki
 */
public class UIUtils {
	/**
	 * Set a fake "native look &amp; feel" for the application if possible
	 * (check for the one currently in use, then try GTK).
	 * <p>
	 * <b>Must</b> be called prior to any GUI work.
	 */
	static public void setLookAndFeel() {
		// native look & feel
		try {
			String noLF = "javax.swing.plaf.metal.MetalLookAndFeel";
			String lf = UIManager.getSystemLookAndFeelClassName();
			if (lf.equals(noLF))
				lf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
			UIManager.setLookAndFeel(lf);
		} catch (InstantiationException e) {
		} catch (ClassNotFoundException e) {
		} catch (UnsupportedLookAndFeelException e) {
		} catch (IllegalAccessException e) {
		}
	}

	/**
	 * Draw a 3D-looking ellipse at the given location, if the given
	 * {@link Graphics} object is compatible (with {@link Graphics2D}); draw a
	 * simple ellipse if not.
	 * 
	 * @param g
	 *            the {@link Graphics} to draw on
	 * @param color
	 *            the base colour
	 * @param x
	 *            the X coordinate
	 * @param y
	 *            the Y coordinate
	 * @param width
	 *            the width radius
	 * @param height
	 *            the height radius
	 */
	static public void drawEllipse3D(Graphics g, Color color, int x, int y,
			int width, int height) {
		drawEllipse3D(g, color, x, y, width, height, true);
	}

	/**
	 * Draw a 3D-looking ellipse at the given location, if the given
	 * {@link Graphics} object is compatible (with {@link Graphics2D}); draw a
	 * simple ellipse if not.
	 * 
	 * @param g
	 *            the {@link Graphics} to draw on
	 * @param color
	 *            the base colour
	 * @param x
	 *            the X coordinate
	 * @param y
	 *            the Y coordinate
	 * @param width
	 *            the width radius
	 * @param height
	 *            the height radius
	 * @param fill
	 *            fill the content of the ellipse
	 */
	static public void drawEllipse3D(Graphics g, Color color, int x, int y,
			int width, int height, boolean fill) {
		if (g instanceof Graphics2D) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			// Retains the previous state
			Paint oldPaint = g2.getPaint();

			// Base shape
			g2.setColor(color);
			if (fill) {
				g2.fillOval(x, y, width, height);
			} else {
				g2.drawOval(x, y, width, height);
			}

			// Compute dark/bright colours
			Paint p = null;
			Color dark = color.darker().darker();
			Color bright = color.brighter().brighter();
			Color darkEnd = new Color(dark.getRed(), dark.getGreen(),
					dark.getBlue(), 0);
			Color darkPartial = new Color(dark.getRed(), dark.getGreen(),
					dark.getBlue(), 64);
			Color brightEnd = new Color(bright.getRed(), bright.getGreen(),
					bright.getBlue(), 0);

			// Adds shadows at the bottom left
			p = new GradientPaint(0, height, dark, width, 0, darkEnd);
			g2.setPaint(p);
			if (fill) {
				g2.fillOval(x, y, width, height);
			} else {
				g2.drawOval(x, y, width, height);
			}
			// Adds highlights at the top right
			p = new GradientPaint(width, 0, bright, 0, height, brightEnd);
			g2.setPaint(p);
			if (fill) {
				g2.fillOval(x, y, width, height);
			} else {
				g2.drawOval(x, y, width, height);
			}

			// Darken the edges
			p = new RadialGradientPaint(x + width / 2f, y + height / 2f,
					Math.min(width / 2f, height / 2f), new float[] { 0f, 1f },
					new Color[] { darkEnd, darkPartial },
					RadialGradientPaint.CycleMethod.NO_CYCLE);
			g2.setPaint(p);
			if (fill) {
				g2.fillOval(x, y, width, height);
			} else {
				g2.drawOval(x, y, width, height);
			}

			// Adds inner highlight at the top right
			p = new RadialGradientPaint(x + 3f * width / 4f, y + height / 4f,
					Math.min(width / 4f, height / 4f),
					new float[] { 0.0f, 0.8f },
					new Color[] { bright, brightEnd },
					RadialGradientPaint.CycleMethod.NO_CYCLE);
			g2.setPaint(p);
			if (fill) {
				g2.fillOval(x * 2, y, width, height);
			} else {
				g2.drawOval(x * 2, y, width, height);
			}

			// Reset original paint
			g2.setPaint(oldPaint);
		} else {
			g.setColor(color);
			if (fill) {
				g.fillOval(x, y, width, height);
			} else {
				g.drawOval(x, y, width, height);
			}
		}
	}

	/**
	 * Add a {@link JScrollPane} around the given panel and use a sensible (for
	 * me) increment for the mouse wheel.
	 * 
	 * @param pane
	 *            the panel to wrap in a {@link JScrollPane}
	 * @param allowHorizontal
	 *            allow horizontal scrolling (not always desired)
	 * 
	 * @return the {@link JScrollPane}
	 */
	static public JScrollPane scroll(JComponent pane, boolean allowHorizontal) {
		return scroll(pane, allowHorizontal, true);
	}

	/**
	 * Add a {@link JScrollPane} around the given panel and use a sensible (for
	 * me) increment for the mouse wheel.
	 * 
	 * @param pane
	 *            the panel to wrap in a {@link JScrollPane}
	 * @param allowHorizontal
	 *            allow horizontal scrolling (not always desired)
	 * @param allowVertical
	 *            allow vertical scrolling (usually yes, but sometimes you only
	 *            want horizontal)
	 * 
	 * @return the {@link JScrollPane}
	 */
	static public JScrollPane scroll(JComponent pane, boolean allowHorizontal,
			boolean allowVertical) {
		JScrollPane scroll = new JScrollPane(pane);

		scroll.getVerticalScrollBar().setUnitIncrement(16);
		scroll.getHorizontalScrollBar().setUnitIncrement(16);

		if (!allowHorizontal) {
			scroll.setHorizontalScrollBarPolicy(
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		if (!allowVertical) {
			scroll.setVerticalScrollBarPolicy(
					JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		}

		return scroll;
	}
}
