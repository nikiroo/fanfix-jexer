package be.nikiroo.fanfix_jexer.reader;

import java.io.Reader;

import be.nikiroo.fanfix.Instance;
import be.nikiroo.fanfix.reader.BasicReader;
import jexer.TApplication;
import jexer.TApplication.BackendType;

/**
 * This {@link Reader}is based upon the TUI widget library 'jexer'
 * (https://github.com/klamonte/jexer/) and offer, as its name suggest, a Text
 * User Interface.
 * <p>
 * It is expected to be on par with the GUI version.
 * 
 * @author niki
 */
public class TuiReader extends BasicReader {
	/**
	 * Will detect the backend to use.
	 * <p>
	 * Swing is the default backend on Windows and MacOS while evreything else
	 * will use XTERM unless explicitly overridden by <tt>jexer.Swing</tt> =
	 * <tt>true</tt> or <tt>false</tt>.
	 * 
	 * @return the backend to use
	 */
	private static BackendType guessBackendType() {
		// TODO: allow a config option to force one or the other?
		TApplication.BackendType backendType = TApplication.BackendType.XTERM;
		if (System.getProperty("os.name").startsWith("Windows")) {
			backendType = TApplication.BackendType.SWING;
		}

		if (System.getProperty("os.name").startsWith("Mac")) {
			backendType = TApplication.BackendType.SWING;
		}

		if (System.getProperty("jexer.Swing") != null) {
			if (System.getProperty("jexer.Swing", "false").equals("true")) {
				backendType = TApplication.BackendType.SWING;
			} else {
				backendType = TApplication.BackendType.XTERM;
			}
		}

		return backendType;
	}

	public void start(String source) {
		try {
			TuiReaderApplication app = new TuiReaderApplication(this, source,
					guessBackendType());
			app.run();
		} catch (Exception e) {
			Instance.getInstance().getTraceHandler().error(e);
		}
	}
}
