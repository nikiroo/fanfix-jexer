package be.nikiroo.fanfix_jexer;

import java.io.IOException;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.reader.Reader.ReaderType;
import be.nikiroo.fanfix_jexer.reader.TuiReader;

/**
 * The main class of the application, the launcher.
 * 
 * @author niki
 */
public class Main {
	/**
	 * The main entry point of the application.
	 * <p>
	 * If arguments are passed, everything will be passed to Fanfix CLI; if no
	 * argument are present, Fanfix-Swing proper will be launched.
	 * 
	 * @param args
	 *            the arguments (none, or will be passed to Fanfix)
	 */
	public static void main(String[] args) {
		// Defer to main application if parameters (we are only a UI)
		// (though we could handle some of the parameters in the future,
		// maybe importing via ImporterFrame? but that would require a
		// unique instance of the UI to be usable...)
		if (args != null && args.length > 0) {
			be.nikiroo.fanfix.Main.main(args);
			return;
		}

		Instance.init();

		TuiReader.setDefaultReaderType(ReaderType.TUI);
		new TuiReader().browse(null);
	}
}
