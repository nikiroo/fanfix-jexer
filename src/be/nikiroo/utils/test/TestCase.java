package be.nikiroo.utils.test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import be.nikiroo.utils.IOUtils;

/**
 * A {@link TestCase} that can be run with {@link TestLauncher}.
 * 
 * @author niki
 */
abstract public class TestCase {
	/**
	 * The type of {@link Exception} used to signal a failed assertion or a
	 * force-fail.
	 * 
	 * @author niki
	 */
	class AssertException extends Exception {
		private static final long serialVersionUID = 1L;

		public AssertException(String reason, Exception source) {
			super(reason, source);
		}

		public AssertException(String reason) {
			super(reason);
		}
	}

	private String name;

	/**
	 * Create a new {@link TestCase}.
	 * 
	 * @param name
	 *            the test name
	 */
	public TestCase(String name) {
		this.name = name;
	}

	/**
	 * This constructor can be used if you require a no-param constructor. In
	 * this case, you are allowed to set the name manually via
	 * {@link TestCase#setName}.
	 */
	protected TestCase() {
		this("no name");
	}

	/**
	 * Setup the test (called before the test is run).
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void setUp() throws Exception {
	}

	/**
	 * Tear-down the test (called when the test has been ran).
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void tearDown() throws Exception {
	}

	/**
	 * The test name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * The test name.
	 * 
	 * @param name
	 *            the new name (internal use only)
	 * 
	 * @return this (so we can chain and so we can initialize it in a member
	 *         variable if this is an anonymous inner class)
	 */
	protected TestCase setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Actually do the test.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	abstract public void test() throws Exception;

	/**
	 * Force a failure.
	 * 
	 * @throws AssertException
	 *             every time
	 */
	public void fail() throws AssertException {
		fail(null);
	}

	/**
	 * Force a failure.
	 * 
	 * @param reason
	 *            the failure reason
	 * 
	 * @throws AssertException
	 *             every time
	 */
	public void fail(String reason) throws AssertException {
		fail(reason, null);
	}

	/**
	 * Force a failure.
	 * 
	 * @param reason
	 *            the failure reason
	 * @param e
	 *            the exception that caused the failure (can be NULL)
	 * 
	 * @throws AssertException
	 *             every time
	 */
	public void fail(String reason, Exception e) throws AssertException {
		throw new AssertException("Failed!" + //
				reason != null ? "\n" + reason : "", e);
	}

	/**
	 * Check that 2 {@link Object}s are equals.
	 * 
	 * @param expected
	 *            the expected value
	 * @param actual
	 *            the actual value
	 * 
	 * @throws AssertException
	 *             in case they differ
	 */
	public void assertEquals(Object expected, Object actual)
			throws AssertException {
		assertEquals(null, expected, actual);
	}

	/**
	 * Check that 2 {@link Object}s are equals.
	 * 
	 * @param errorMessage
	 *            the error message to display if they differ
	 * @param expected
	 *            the expected value
	 * @param actual
	 *            the actual value
	 * 
	 * @throws AssertException
	 *             in case they differ
	 */
	public void assertEquals(String errorMessage, Object expected, Object actual)
			throws AssertException {
		if ((expected == null && actual != null)
				|| (expected != null && !expected.equals(actual))) {
			if (errorMessage == null) {
				throw new AssertException(generateAssertMessage(expected,
						actual));
			}

			throw new AssertException(errorMessage, new AssertException(
					generateAssertMessage(expected, actual)));
		}
	}

	/**
	 * Check that 2 longs are equals.
	 * 
	 * @param expected
	 *            the expected value
	 * @param actual
	 *            the actual value
	 * 
	 * @throws AssertException
	 *             in case they differ
	 */
	public void assertEquals(long expected, long actual) throws AssertException {
		assertEquals(Long.valueOf(expected), Long.valueOf(actual));
	}

	/**
	 * Check that 2 longs are equals.
	 * 
	 * @param errorMessage
	 *            the error message to display if they differ
	 * @param expected
	 *            the expected value
	 * @param actual
	 *            the actual value
	 * 
	 * @throws AssertException
	 *             in case they differ
	 */
	public void assertEquals(String errorMessage, long expected, long actual)
			throws AssertException {
		assertEquals(errorMessage, Long.valueOf(expected), Long.valueOf(actual));
	}

	/**
	 * Check that 2 booleans are equals.
	 * 
	 * @param expected
	 *            the expected value
	 * @param actual
	 *            the actual value
	 * 
	 * @throws AssertException
	 *             in case they differ
	 */
	public void assertEquals(boolean expected, boolean actual)
			throws AssertException {
		assertEquals(Boolean.valueOf(expected), Boolean.valueOf(actual));
	}

	/**
	 * Check that 2 booleans are equals.
	 * 
	 * @param errorMessage
	 *            the error message to display if they differ
	 * @param expected
	 *            the expected value
	 * @param actual
	 *            the actual value
	 * 
	 * @throws AssertException
	 *             in case they differ
	 */
	public void assertEquals(String errorMessage, boolean expected,
			boolean actual) throws AssertException {
		assertEquals(errorMessage, Boolean.valueOf(expected),
				Boolean.valueOf(actual));
	}

	/**
	 * Check that 2 doubles are equals.
	 * 
	 * @param expected
	 *            the expected value
	 * @param actual
	 *            the actual value
	 * 
	 * @throws AssertException
	 *             in case they differ
	 */
	public void assertEquals(double expected, double actual)
			throws AssertException {
		assertEquals(Double.valueOf(expected), Double.valueOf(actual));
	}

	/**
	 * Check that 2 doubles are equals.
	 * 
	 * @param errorMessage
	 *            the error message to display if they differ
	 * @param expected
	 *            the expected value
	 * @param actual
	 *            the actual value
	 * 
	 * @throws AssertException
	 *             in case they differ
	 */
	public void assertEquals(String errorMessage, double expected, double actual)
			throws AssertException {
		assertEquals(errorMessage, Double.valueOf(expected),
				Double.valueOf(actual));
	}

	/**
	 * Check that 2 {@link List}s are equals.
	 * 
	 * @param errorMessage
	 *            the error message to display if they differ
	 * @param expected
	 *            the expected value
	 * @param actual
	 *            the actual value
	 * 
	 * @throws AssertException
	 *             in case they differ
	 */
	public void assertEquals(List<?> expected, List<?> actual)
			throws AssertException {
		assertEquals("Assertion failed", expected, actual);
	}

	/**
	 * Check that 2 {@link List}s are equals.
	 * 
	 * @param errorMessage
	 *            the error message to display if they differ
	 * @param expected
	 *            the expected value
	 * @param actual
	 *            the actual value
	 * 
	 * @throws AssertException
	 *             in case they differ
	 */
	public void assertEquals(String errorMessage, List<?> expected,
			List<?> actual) throws AssertException {

		if (expected.size() != actual.size()) {
			assertEquals(errorMessage + ": not same number of items",
					list(expected), list(actual));
		}

		int size = expected.size();
		for (int i = 0; i < size; i++) {
			assertEquals(errorMessage + ": item " + i
					+ " (0-based) is not correct", expected.get(i),
					actual.get(i));
		}
	}

	/**
	 * Check that 2 {@link File}s are equals, by doing a line-by-line
	 * comparison.
	 * 
	 * @param expected
	 *            the expected value
	 * @param actual
	 *            the actual value
	 * @param errorMessage
	 *            the error message to display if they differ
	 * 
	 * @throws AssertException
	 *             in case they differ
	 */
	public void assertEquals(File expected, File actual) throws AssertException {
		assertEquals(generateAssertMessage(expected, actual), expected, actual);
	}

	/**
	 * Check that 2 {@link File}s are equals, by doing a line-by-line
	 * comparison.
	 * 
	 * @param errorMessage
	 *            the error message to display if they differ
	 * @param expected
	 *            the expected value
	 * @param actual
	 *            the actual value
	 * 
	 * @throws AssertException
	 *             in case they differ
	 */
	public void assertEquals(String errorMessage, File expected, File actual)
			throws AssertException {
		assertEquals(errorMessage, expected, actual, null);
	}

	/**
	 * Check that 2 {@link File}s are equals, by doing a line-by-line
	 * comparison.
	 * 
	 * @param errorMessage
	 *            the error message to display if they differ
	 * @param expected
	 *            the expected value
	 * @param actual
	 *            the actual value
	 * @param skipCompare
	 *            skip the lines starting with some values for the given files
	 *            (relative path from base directory in recursive mode)
	 * 
	 * @throws AssertException
	 *             in case they differ
	 */
	public void assertEquals(String errorMessage, File expected, File actual,
			Map<String, List<String>> skipCompare) throws AssertException {
		assertEquals(errorMessage, expected, actual, skipCompare, null);
	}

	private void assertEquals(String errorMessage, File expected, File actual,
			Map<String, List<String>> skipCompare, String removeFromName)
			throws AssertException {

		if (expected.isDirectory() || actual.isDirectory()) {
			assertEquals(errorMessage + ": type mismatch: expected a "
					+ (expected.isDirectory() ? "directory" : "file")
					+ ", received a "
					+ (actual.isDirectory() ? "directory" : "file"),
					expected.isDirectory(), actual.isDirectory());

			List<String> expectedFiles = Arrays.asList(expected.list());
			Collections.sort(expectedFiles);
			List<String> actualFiles = Arrays.asList(actual.list());
			Collections.sort(actualFiles);

			assertEquals(errorMessage, expectedFiles, actualFiles);
			for (int i = 0; i < actualFiles.size(); i++) {
				File expectedFile = new File(expected, expectedFiles.get(i));
				File actualFile = new File(actual, actualFiles.get(i));

				assertEquals(errorMessage, expectedFile, actualFile,
						skipCompare, expected.getAbsolutePath());
			}
		} else {
			try {
				List<String> expectedLines = Arrays.asList(IOUtils
						.readSmallFile(expected).split("\n"));
				List<String> resultLines = Arrays.asList(IOUtils.readSmallFile(
						actual).split("\n"));

				String name = expected.getAbsolutePath();
				if (removeFromName != null && name.startsWith(removeFromName)) {
					name = expected.getName()
							+ name.substring(removeFromName.length());
				}

				assertEquals(errorMessage + ": " + name
						+ ": the number of lines is not the same",
						expectedLines.size(), resultLines.size());

				for (int j = 0; j < expectedLines.size(); j++) {
					String expectedLine = expectedLines.get(j);
					String resultLine = resultLines.get(j);

					boolean skip = false;
					if (skipCompare != null) {
						for (Entry<String, List<String>> skipThose : skipCompare
								.entrySet()) {
							for (String skipStart : skipThose.getValue()) {
								if (name.endsWith(skipThose.getKey())
										&& expectedLine.startsWith(skipStart)
										&& resultLine.startsWith(skipStart)) {
									skip = true;
								}
							}
						}
					}

					if (skip) {
						continue;
					}

					assertEquals(errorMessage + ": line " + (j + 1)
							+ " is not the same in file " + name, expectedLine,
							resultLine);
				}
			} catch (Exception e) {
				throw new AssertException(errorMessage, e);
			}
		}
	}

	/**
	 * Check that given {@link Object} is not NULL.
	 * 
	 * @param errorMessage
	 *            the error message to display if it is NULL
	 * @param actual
	 *            the actual value
	 * 
	 * @throws AssertException
	 *             in case they differ
	 */
	public void assertNotNull(String errorMessage, Object actual)
			throws AssertException {
		if (actual == null) {
			String defaultReason = String.format("" //
					+ "Assertion failed!%n" //
					+ "Object should not have been NULL");

			if (errorMessage == null) {
				throw new AssertException(defaultReason);
			}

			throw new AssertException(errorMessage, new AssertException(
					defaultReason));
		}
	}

	/**
	 * Generate the default assert message for 2 different values that were
	 * supposed to be equals.
	 * 
	 * @param expected
	 *            the expected value
	 * @param actual
	 *            the actual value
	 * 
	 * @return the message
	 */
	public static String generateAssertMessage(Object expected, Object actual) {
		return String.format("" //
				+ "Assertion failed!%n" //
				+ "Expected value: [%s]%n" //
				+ "Actual value:   [%s]", expected, actual);
	}

	private static String list(List<?> items) {
		StringBuilder builder = new StringBuilder();
		for (Object item : items) {
			if (builder.length() == 0) {
				builder.append(items.size() + " item(s): ");
			} else {
				builder.append(", ");
			}

			builder.append("" + item);

			if (builder.length() > 60) {
				builder.setLength(57);
				builder.append("...");
				break;
			}
		}

		return builder.toString();
	}
}
