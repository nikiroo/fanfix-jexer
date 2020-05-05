package be.nikiroo.utils.test_code;

import java.io.File;
import java.io.IOException;

import be.nikiroo.utils.TempFiles;
import be.nikiroo.utils.test.TestCase;
import be.nikiroo.utils.test.TestLauncher;

class TempFilesTest extends TestLauncher {
	public TempFilesTest(String[] args) {
		super("TempFiles", args);

		addTest(new TestCase("Name is correct") {
			@Override
			public void test() throws Exception {
				RootTempFiles files = new RootTempFiles("testy");
				try {
					assertEquals("The root was not created", true, files
							.getRoot().exists());
					assertEquals(
							"The root is not prefixed with the expected name",
							true, files.getRoot().getName().startsWith("testy"));

				} finally {
					files.close();
				}
			}
		});

		addTest(new TestCase("Clean after itself no use") {
			@Override
			public void test() throws Exception {
				RootTempFiles files = new RootTempFiles("testy2");
				try {
					assertEquals("The root was not created", true, files
							.getRoot().exists());
				} finally {
					files.close();
					assertEquals("The root was not deleted", false, files
							.getRoot().exists());
				}
			}
		});

		addTest(new TestCase("Clean after itself after usage") {
			@Override
			public void test() throws Exception {
				RootTempFiles files = new RootTempFiles("testy3");
				try {
					assertEquals("The root was not created", true, files
							.getRoot().exists());
					files.createTempFile("test");
				} finally {
					files.close();
					assertEquals("The root was not deleted", false, files
							.getRoot().exists());
					assertEquals("The main root in /tmp was not deleted",
							false, files.getRoot().getParentFile().exists());
				}
			}
		});

		addTest(new TestCase("Temporary directories") {
			@Override
			public void test() throws Exception {
				RootTempFiles files = new RootTempFiles("testy4");
				File file = null;
				try {
					File dir = files.createTempDir("test");
					file = new File(dir, "fanfan");
					file.createNewFile();
					assertEquals(
							"Cannot create a file in a temporary directory",
							true, file.exists());
				} finally {
					files.close();
					if (file != null) {
						assertEquals(
								"A file created in a temporary directory should be deleted on close",
								false, file.exists());
					}
					assertEquals("The root was not deleted", false, files
							.getRoot().exists());
				}
			}
		});
	}

	private class RootTempFiles extends TempFiles {
		private File root = null;

		public RootTempFiles(String name) throws IOException {
			super(name);
		}

		public File getRoot() {
			if (root != null)
				return root;
			return super.root;
		}

		@Override
		public synchronized void close() throws IOException {
			root = super.root;
			super.close();
		}
	}
}
