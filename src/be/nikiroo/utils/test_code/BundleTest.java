package be.nikiroo.utils.test_code;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.resources.Bundle;
import be.nikiroo.utils.resources.Bundles;
import be.nikiroo.utils.resources.Meta;
import be.nikiroo.utils.test.TestCase;
import be.nikiroo.utils.test.TestLauncher;

class BundleTest extends TestLauncher {
	private File tmp;
	private B b = new B();

	public BundleTest(String[] args) {
		this("Bundle test", args);
	}

	protected BundleTest(String name, String[] args) {
		super(name, args);

		for (TestCase test : getSimpleTests()) {
			addTest(test);
		}

		addSeries(new TestLauncher("After saving/reloading the resources", args) {
			{
				for (TestCase test : getSimpleTests()) {
					addTest(test);
				}
			}

			@Override
			protected void start() throws Exception {
				tmp = File.createTempFile("nikiroo-utils", ".test");
				tmp.delete();
				tmp.mkdir();
				b.updateFile(tmp.getAbsolutePath());
				Bundles.setDirectory(tmp.getAbsolutePath());
				b.reload(false);
			}

			@Override
			protected void stop() {
				IOUtils.deltree(tmp);
			}
		});

		addSeries(new TestLauncher("Read/Write support", args) {
			{
				addTest(new TestCase("Reload") {
					@Override
					public void test() throws Exception {
						String def = b.getString(E.ONE);
						String val = "Something";

						b.setString(E.ONE, val);
						b.updateFile();
						b.reload(true);

						assertEquals("We should have reset the bundle", def,
								b.getString(E.ONE));

						b.reload(false);

						assertEquals("We should have reloaded the same files",
								val, b.getString(E.ONE));

						// reset values for next tests
						b.reload(true);
						b.updateFile();
					}
				});

				addTest(new TestCase("Set/Get") {
					@Override
					public void test() throws Exception {
						String val = "Newp";
						b.setString(E.ONE, val);
						String setGet = b.getString(E.ONE);

						assertEquals(val, setGet);

						// reset values for next tests
						b.restoreSnapshot(null);
					}
				});

				addTest(new TestCase("Snapshots") {
					@Override
					public void test() throws Exception {
						String val = "Newp";
						String def = b.getString(E.ONE);

						b.setString(E.ONE, val);
						Object snap = b.takeSnapshot();

						b.restoreSnapshot(null);
						assertEquals(
								"restoreChanges(null) should clear the changes",
								def, b.getString(E.ONE));
						b.restoreSnapshot(snap);
						assertEquals(
								"restoreChanges(snapshot) should restore the changes",
								val, b.getString(E.ONE));

						// reset values for next tests
						b.restoreSnapshot(null);
					}
				});

				addTest(new TestCase("updateFile with changes") {
					@Override
					public void test() throws Exception {
						String val = "Go to disk! (UTF-8 test: 日本語)";

						String def = b.getString(E.ONE);
						b.setString(E.ONE, val);
						b.updateFile(tmp.getAbsolutePath());
						b.reload(false);

						assertEquals(val, b.getString(E.ONE));

						// reset values for next tests
						b.setString(E.ONE, def);
						b.updateFile(tmp.getAbsolutePath());
						b.reload(false);
					}
				});
			}

			@Override
			protected void start() throws Exception {
				tmp = File.createTempFile("nikiroo-utils", ".test");
				tmp.delete();
				tmp.mkdir();
				b.updateFile(tmp.getAbsolutePath());
				Bundles.setDirectory(tmp.getAbsolutePath());
				b.reload(false);
			}

			@Override
			protected void stop() {
				IOUtils.deltree(tmp);
			}
		});
	}

	private List<TestCase> getSimpleTests() {
		String pre = "";

		List<TestCase> list = new ArrayList<TestCase>();

		list.add(new TestCase(pre + "getString simple") {
			@Override
			public void test() throws Exception {
				assertEquals("un", b.getString(E.ONE));
			}
		});

		list.add(new TestCase(pre + "getStringX with null suffix") {
			@Override
			public void test() throws Exception {
				assertEquals("un", b.getStringX(E.ONE, null));
			}
		});

		list.add(new TestCase(pre + "getStringX with empty suffix") {
			@Override
			public void test() throws Exception {
				assertEquals(null, b.getStringX(E.ONE, ""));
			}
		});

		list.add(new TestCase(pre + "getStringX with existing suffix") {
			@Override
			public void test() throws Exception {
				assertEquals("un + suffix", b.getStringX(E.ONE, "suffix"));
			}
		});

		list.add(new TestCase(pre + "getStringX with not existing suffix") {
			@Override
			public void test() throws Exception {
				assertEquals(null, b.getStringX(E.ONE, "fake"));
			}
		});

		list.add(new TestCase(pre + "getString with UTF-8 content") {
			@Override
			public void test() throws Exception {
				assertEquals("日本語 Nihongo", b.getString(E.JAPANESE));
			}
		});

		return list;
	}

	/**
	 * {@link Bundle}.
	 * 
	 * @author niki
	 */
	private class B extends Bundle<E> {
		protected B() {
			super(E.class, N.bundle_test, null);
		}

		@Override
		// ...and make it public
		public Object takeSnapshot() {
			return super.takeSnapshot();
		}

		@Override
		// ...and make it public
		public void restoreSnapshot(Object snap) {
			super.restoreSnapshot(snap);
		}
	}

	/**
	 * Key enum for the {@link Bundle}.
	 * 
	 * @author niki
	 */
	private enum E {
		@Meta
		ONE, //
		@Meta
		ONE_SUFFIX, //
		@Meta
		TWO, //
		@Meta
		JAPANESE
	}

	/**
	 * Name enum for the {@link Bundle}.
	 * 
	 * @author niki
	 */
	private enum N {
		bundle_test
	}
}
