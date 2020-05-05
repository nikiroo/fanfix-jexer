package be.nikiroo.utils.main;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.Image;
import be.nikiroo.utils.ui.ImageTextAwt;
import be.nikiroo.utils.ui.ImageTextAwt.Mode;
import be.nikiroo.utils.ui.ImageUtilsAwt;

/**
 * Image to ASCII conversion.
 * 
 * @author niki
 */
public class img2aa {
	/**
	 * Syntax: (--mode=MODE) (--width=WIDTH) (--height=HEIGHT) (--size=SIZE)
	 * (--output=OUTPUT) (--invert) (--help)
	 * <p>
	 * See "--help".
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Dimension size = null;
		Mode mode = null;
		boolean invert = false;
		List<String> inputs = new ArrayList<String>();
		File output = null;

		String lastArg = "";
		try {
			int height = -1;
			int width = -1;

			for (String arg : args) {
				lastArg = arg;

				if (arg.startsWith("--mode=")) {
					mode = Mode.valueOf(arg.substring("--mode=".length()));
				} else if (arg.startsWith("--width=")) {
					width = Integer
							.parseInt(arg.substring("--width=".length()));
				} else if (arg.startsWith("--height=")) {
					height = Integer.parseInt(arg.substring("--height="
							.length()));
				} else if (arg.startsWith("--size=")) {
					String content = arg.substring("--size=".length()).replace(
							"X", "x");
					width = Integer.parseInt(content.split("x")[0]);
					height = Integer.parseInt(content.split("x")[1]);
				} else if (arg.startsWith("--ouput=")) {
					if (!arg.equals("--output=-")) {
						output = new File(arg.substring("--output=".length()));
					}
				} else if (arg.equals("--invert")) {
					invert = true;
				} else if (arg.equals("--help")) {
					System.out
							.println("Syntax: (--mode=MODE) (--width=WIDTH) (--height=HEIGHT) (--size=SIZE) (--output=OUTPUT) (--invert) (--help)");
					System.out.println("\t --help: will show this screen");
					System.out
							.println("\t --invert: will invert the 'colours'");
					System.out
							.println("\t --mode: will select the rendering mode (default: ASCII):");
					System.out
							.println("\t\t ASCII: ASCI output mode, that is, characters \" .-+=o8#\"");
					System.out
							.println("\t\t DITHERING: Use 5 different \"colours\" which are actually"
									+ "\n\t\t Unicode characters \" ░▒▓█\"");
					System.out
							.println("\t\t DOUBLE_RESOLUTION: Use \"block\" Unicode characters up to quarter"
									+ "\n\t\t blocks, thus in effect doubling the resolution both in vertical"
									+ "\n\t\t and horizontal space."
									+ "\n\t\t Note that since 2 characters next to each other are square,"
									+ "\n\t\t 4 blocks per 2 blocks for w/h resolution.");
					System.out
							.println("\t\t DOUBLE_DITHERING: Use characters from both DOUBLE_RESOLUTION"
									+ "\n\t\t and DITHERING");
					return;
				} else {
					inputs.add(arg);
				}
			}

			size = new Dimension(width, height);
			if (inputs.size() == 0) {
				inputs.add("-"); // by default, stdin
			}
		} catch (Exception e) {
			System.err.println("Syntax error: \"" + lastArg + "\" is invalid");
			System.exit(1);
		}

		try {
			if (mode == null) {
				mode = Mode.ASCII;
			}

			for (String input : inputs) {
				InputStream in = null;

				try {
					if (input.equals("-")) {
						in = System.in;
					} else {
						in = new FileInputStream(input);
					}
					BufferedImage image = ImageUtilsAwt
							.fromImage(new Image(in));
					ImageTextAwt img = new ImageTextAwt(image, size, mode,
							invert);
					if (output == null) {
						System.out.println(img.getText());
					} else {
						IOUtils.writeSmallFile(output, img.getText());
					}
				} finally {
					if (!input.equals("-")) {
						in.close();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}
}
