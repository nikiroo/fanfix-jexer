package be.nikiroo.utils.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import be.nikiroo.utils.StringUtils;
import be.nikiroo.utils.StringUtils.Alignment;

/**
 * Text justification (left, right, center, justify).
 * 
 * @author niki
 */
public class justify {
	/**
	 * Syntax: $0 ([left|right|center|justify]) (max width)
	 * <p>
	 * <ul>
	 * <li>mode: left, right, center or full justification (defaults to left)</li>
	 * <li>max width: the maximum width of a line, or "" for "no maximum"
	 * (defaults to "no maximum")</li>
	 * </ul>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int width = -1;
		StringUtils.Alignment align = Alignment.LEFT;

		if (args.length >= 1) {
			align = Alignment.valueOf(args[0].toUpperCase());
		}
		if (args.length >= 2) {
			width = Integer.parseInt(args[1]);
		}

		Scanner scan = new Scanner(System.in);
		scan.useDelimiter("\r\n|[\r\n]");
		try {
			List<String> lines = new ArrayList<String>();
			while (scan.hasNext()) {
				lines.add(scan.next());
			}

			for (String line : StringUtils.justifyText(lines, width, align)) {
				System.out.println(line);
			}
		} finally {
			scan.close();
		}
	}
}
