package be.nikiroo.utils.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link TestLauncher} starts a series of {@link TestCase}s and displays the
 * result to the user.
 * 
 * @author niki
 */
public class TestLauncher {
	/**
	 * {@link Exception} happening during the setup process.
	 * 
	 * @author niki
	 */
	private class SetupException extends Exception {
		private static final long serialVersionUID = 1L;

		public SetupException(Throwable e) {
			super(e);
		}
	}

	/**
	 * {@link Exception} happening during the tear-down process.
	 * 
	 * @author niki
	 */
	private class TearDownException extends Exception {
		private static final long serialVersionUID = 1L;

		public TearDownException(Throwable e) {
			super(e);
		}
	}

	private List<TestLauncher> series;
	private List<TestCase> tests;
	private TestLauncher parent;

	private int columns;
	private String okString;
	private String koString;
	private String name;
	private boolean cont;

	protected int executed;
	protected int total;

	private int currentSeries = 0;
	private boolean details = false;

	/**
	 * Create a new {@link TestLauncher} with default parameters.
	 * 
	 * @param name
	 *            the test suite name
	 * @param args
	 *            the arguments to configure the number of columns and the ok/ko
	 *            {@link String}s
	 */
	public TestLauncher(String name, String[] args) {
		this.name = name;

		int cols = 80;
		if (args != null && args.length >= 1) {
			try {
				cols = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.err.println("Test configuration: given number "
						+ "of columns is not parseable: " + args[0]);
			}
		}

		setColumns(cols);

		String okString = "[ ok ]";
		String koString = "[ !! ]";
		if (args != null && args.length >= 3) {
			okString = args[1];
			koString = args[2];
		}

		setOkString(okString);
		setKoString(koString);

		series = new ArrayList<TestLauncher>();
		tests = new ArrayList<TestCase>();
		cont = true;
	}

	/**
	 * Display the details of the errors
	 * 
	 * @return TRUE to display them, false to simply mark the test as failed
	 */
	public boolean isDetails() {
		if (parent != null) {
			return parent.isDetails();
		}

		return details;
	}

	/**
	 * Display the details of the errors
	 * 
	 * @param details
	 *            TRUE to display them, false to simply mark the test as failed
	 */
	public void setDetails(boolean details) {
		if (parent != null) {
			parent.setDetails(details);
		}

		this.details = details;
	}

	/**
	 * Called before actually starting the tests themselves.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	protected void start() throws Exception {
	}

	/**
	 * Called when the tests are passed (or failed to do so).
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	protected void stop() throws Exception {
	}

	protected void addTest(TestCase test) {
		tests.add(test);
	}

	protected void addSeries(TestLauncher series) {
		this.series.add(series);
		series.parent = this;
	}

	/**
	 * Launch the series of {@link TestCase}s and the {@link TestCase}s.
	 * 
	 * @return the number of errors
	 */
	public int launch() {
		return launch(0);
	}

	/**
	 * Launch the series of {@link TestCase}s and the {@link TestCase}s.
	 * 
	 * @param depth
	 *            the level at which is the launcher (0 = main launcher)
	 * 
	 * @return the number of errors
	 */
	public int launch(int depth) {
		int errors = 0;
		executed = 0;
		total = tests.size();

		print(depth);

		try {
			start();

			errors += launchTests(depth);
			if (tests.size() > 0 && depth == 0) {
				System.out.println("");
			}

			currentSeries = 0;
			for (TestLauncher serie : series) {
				errors += serie.launch(depth + 1);
				executed += serie.executed;
				total += serie.total;
				currentSeries++;
			}
		} catch (Exception e) {
			print(depth, "__start");
			print(depth, e);
		} finally {
			try {
				stop();
			} catch (Exception e) {
				print(depth, "__stop");
				print(depth, e);
			}
		}

		print(depth, executed, errors, total);

		return errors;
	}

	/**
	 * Launch the {@link TestCase}s.
	 * 
	 * @param depth
	 *            the level at which is the launcher (0 = main launcher)
	 * 
	 * @return the number of errors
	 */
	protected int launchTests(int depth) {
		int errors = 0;
		for (TestCase test : tests) {
			print(depth, test.getName());

			Throwable ex = null;
			try {
				try {
					test.setUp();
				} catch (Throwable e) {
					throw new SetupException(e);
				}
				test.test();
				try {
					test.tearDown();
				} catch (Throwable e) {
					throw new TearDownException(e);
				}
			} catch (Throwable e) {
				ex = e;
			}

			if (ex != null) {
				errors++;
			}

			print(depth, ex);

			executed++;

			if (ex != null && !cont) {
				break;
			}
		}

		return errors;
	}

	/**
	 * Specify a custom number of columns to use for the display of messages.
	 * 
	 * @param columns
	 *            the number of columns
	 */
	public void setColumns(int columns) {
		this.columns = columns;
	}

	/**
	 * Continue to run the tests when an error is detected.
	 * 
	 * @param cont
	 *            yes or no
	 */
	public void setContinueAfterFail(boolean cont) {
		this.cont = cont;
	}

	/**
	 * Set a custom "[ ok ]" {@link String} when a test passed.
	 * 
	 * @param okString
	 *            the {@link String} to display at the end of a success
	 */
	public void setOkString(String okString) {
		this.okString = okString;
	}

	/**
	 * Set a custom "[ !! ]" {@link String} when a test failed.
	 * 
	 * @param koString
	 *            the {@link String} to display at the end of a failure
	 */
	public void setKoString(String koString) {
		this.koString = koString;
	}

	/**
	 * Print the test suite header.
	 * 
	 * @param depth
	 *            the level at which is the launcher (0 = main launcher)
	 */
	protected void print(int depth) {
		if (depth == 0) {
			System.out.println("[ Test suite: " + name + " ]");
			System.out.println("");
		} else {
			System.out.println(prefix(depth, false) + name + ":");
		}
	}

	/**
	 * Print the name of the {@link TestCase} we will start immediately after.
	 * 
	 * @param depth
	 *            the level at which is the launcher (0 = main launcher)
	 * @param name
	 *            the {@link TestCase} name
	 */
	protected void print(int depth, String name) {
		name = prefix(depth, false)
				+ (name == null ? "" : name).replace("\t", "    ");

		StringBuilder dots = new StringBuilder();
		while ((name.length() + dots.length()) < columns - 11) {
			dots.append('.');
		}

		System.out.print(name + dots.toString());
	}

	/**
	 * Print the result of the {@link TestCase} we just ran.
	 * 
	 * @param depth
	 *            the level at which is the launcher (0 = main launcher)
	 * @param error
	 *            the {@link Exception} it ran into if any
	 */
	private void print(int depth, Throwable error) {
		if (error != null) {
			System.out.println(" " + koString);
			if (isDetails()) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				error.printStackTrace(pw);
				String lines = sw.toString();
				for (String line : lines.split("\n")) {
					System.out.println(prefix(depth, false) + "\t\t" + line);
				}
			}
		} else {
			System.out.println(" " + okString);
		}
	}

	/**
	 * Print the total result for this test suite.
	 * 
	 * @param depth
	 *            the level at which is the launcher (0 = main launcher)
	 * @param executed
	 *            the number of tests actually ran
	 * @param errors
	 *            the number of errors encountered
	 * @param total
	 *            the total number of tests in the suite
	 */
	private void print(int depth, int executed, int errors, int total) {
		int ok = executed - errors;
		int pc = (int) ((100.0 * ok) / executed);
		if (pc == 0 && ok > 0) {
			pc = 1;
		}
		int pcTotal = (int) ((100.0 * ok) / total);
		if (pcTotal == 0 && ok > 0) {
			pcTotal = 1;
		}

		String resume = "Tests passed: " + ok + "/" + executed + " (" + pc
				+ "%) on a total of " + total + " (" + pcTotal + "% total)";
		if (depth == 0) {
			System.out.println(resume);
		} else {
			String arrow = "┗▶ ";
			System.out.println(prefix(depth, currentSeries == 0) + arrow
					+ resume);
			System.out.println(prefix(depth, currentSeries == 0));
		}
	}

	private int last = -1;

	/**
	 * Return the prefix to print before the current line.
	 * 
	 * @param depth
	 *            the current depth
	 * @param first
	 *            this line is the first of its tabulation level
	 * 
	 * @return the prefix
	 */
	private String prefix(int depth, boolean first) {
		String space = tabs(depth - 1);

		String line = "";
		if (depth > 0) {
			if (depth > 1) {
				if (depth != last && first) {
					line = "╻"; // first line
				} else {
					line = "┃"; // continuation
				}
			}

			space += line + tabs(1);
		}

		last = depth;
		return space;
	}

	/**
	 * Return the given number of space-converted tabs in a {@link String}.
	 * 
	 * @param depth
	 *            the number of tabs to return
	 * 
	 * @return the string
	 */
	private String tabs(int depth) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < depth; i++) {
			builder.append("    ");
		}
		return builder.toString();
	}
}
