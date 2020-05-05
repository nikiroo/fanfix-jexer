package be.nikiroo.utils.test_code;

import be.nikiroo.utils.Version;
import be.nikiroo.utils.test.TestCase;
import be.nikiroo.utils.test.TestLauncher;

class VersionTest extends TestLauncher {
	public VersionTest(String[] args) {
		super("Version test", args);

		addTest(new TestCase("String <-> int") {
			@Override
			public void test() throws Exception {
				assertEquals("Cannot parse version 1.2.3 from int to String",
						"1.2.3", new Version(1, 2, 3).toString());
				assertEquals(
						"Cannot parse major version \"1.2.3\" from String to int",
						1, new Version("1.2.3").getMajor());
				assertEquals(
						"Cannot parse minor version \"1.2.3\" from String to int",
						2, new Version("1.2.3").getMinor());
				assertEquals(
						"Cannot parse patch version \"1.2.3\" from String to int",
						3, new Version("1.2.3").getPatch());
			}
		});

		addTest(new TestCase("Bad input") {
			@Override
			public void test() throws Exception {
				assertEquals(
						"Bad input should return an empty version",
						true,
						new Version(
								"Doors 98 SE Special Deluxe Edition Pro++ Not-Home")
								.isEmpty());

				assertEquals(
						"Bad input should return [unknown]",
						"[unknown]",
						new Version(
								"Doors 98 SE Special Deluxe Edition Pro++ Not-Home")
								.toString());
			}
		});

		addTest(new TestCase("Read current version") {
			@Override
			public void test() throws Exception {
				assertNotNull("The version should not be NULL (in any case!)",
						Version.getCurrentVersion());
				assertEquals("The current version should not be empty", false,
						Version.getCurrentVersion().isEmpty());
			}
		});

		addTest(new TestCase("Tag version") {
			@Override
			public void test() throws Exception {
				Version version = new Version("1.0.0-debian0");
				assertEquals("debian", version.getTag());
				assertEquals(0, version.getTagVersion());
				version = new Version("1.0.0-debian.0");
				assertEquals("debian.", version.getTag());
				assertEquals(0, version.getTagVersion());
				version = new Version("1.0.0-debian-0");
				assertEquals("debian-", version.getTag());
				assertEquals(0, version.getTagVersion());
				version = new Version("1.0.0-debian-12");
				assertEquals("debian-", version.getTag());
				assertEquals(12, version.getTagVersion());

				// tag with no tag version
				version = new Version("1.0.0-dev");
				assertEquals(1, version.getMajor());
				assertEquals(0, version.getMinor());
				assertEquals(0, version.getPatch());
				assertEquals("dev", version.getTag());
				assertEquals(-1, version.getTagVersion());
			}
		});

		addTest(new TestCase("Comparing versions") {
			@Override
			public void test() throws Exception {
				assertEquals(true,
						new Version(1, 1, 1).isNewerThan(new Version(1, 1, 0)));
				assertEquals(true,
						new Version(2, 0, 0).isNewerThan(new Version(1, 1, 1)));
				assertEquals(true,
						new Version(10, 7, 8).isNewerThan(new Version(9, 9, 9)));
				assertEquals(true,
						new Version(0, 0, 0).isOlderThan(new Version(0, 0, 1)));
				assertEquals(1,
						new Version(1, 1, 1).compareTo(new Version(0, 1, 1)));
				assertEquals(-1,
						new Version(0, 0, 1).compareTo(new Version(0, 1, 1)));
				assertEquals(0,
						new Version(0, 0, 1).compareTo(new Version(0, 0, 1)));
				assertEquals(true,
						new Version(0, 0, 1).equals(new Version(0, 0, 1)));
				assertEquals(false,
						new Version(0, 2, 1).equals(new Version(0, 0, 1)));

				assertEquals(true,
						new Version(1, 0, 1, "my.tag.", 2).equals(new Version(
								1, 0, 1, "my.tag.", 2)));
				assertEquals(false,
						new Version(1, 0, 1, "my.tag.", 2).equals(new Version(
								1, 0, 0, "my.tag.", 2)));
				assertEquals(false,
						new Version(1, 0, 1, "my.tag.", 2).equals(new Version(
								1, 0, 1, "not-my.tag.", 2)));
			}
		});

		addTest(new TestCase("toString") {
			@Override
			public void test() throws Exception {
				// Check leading 0s:
				Version version = new Version("01.002.4");
				assertEquals("Leading 0s not working", "1.2.4",
						version.toString());

				// Check spacing
				version = new Version("1 . 2.4 ");
				assertEquals("Additional spaces not working", "1.2.4",
						version.toString());

				String[] tests = new String[] { "1.0.0", "1.2.3", "1.0.0-dev",
						"1.1.2-niki0" };
				for (String test : tests) {
					version = new Version(test);
					assertEquals("toString and back conversion failed", test,
							version.toString());
				}
			}
		});
	}
}
