package be.nikiroo.utils.test_code;

import be.nikiroo.utils.Progress;
import be.nikiroo.utils.test.TestCase;
import be.nikiroo.utils.test.TestLauncher;

class ProgressTest extends TestLauncher {
	public ProgressTest(String[] args) {
		super("Progress reporting", args);

		addSeries(new TestLauncher("Simple progress", args) {
			{
				addTest(new TestCase("Relative values and direct values") {
					@Override
					public void test() throws Exception {
						Progress p = new Progress();
						assertEquals(0, p.getProgress());
						assertEquals(0, p.getRelativeProgress());
						p.setProgress(33);
						assertEquals(33, p.getProgress());
						assertEquals(0.33, p.getRelativeProgress());
						p.setMax(3);
						p.setProgress(1);
						assertEquals(1, p.getProgress());
						assertEquals(
								generateAssertMessage("0.33..",
										p.getRelativeProgress()), true,
								p.getRelativeProgress() >= 0.332);
						assertEquals(
								generateAssertMessage("0.33..",
										p.getRelativeProgress()), true,
								p.getRelativeProgress() <= 0.334);
					}
				});

				addTest(new TestCase("Listeners at first level") {
					int pg;

					@Override
					public void test() throws Exception {
						Progress p = new Progress();
						p.addProgressListener(new Progress.ProgressListener() {
							@Override
							public void progress(Progress progress, String name) {
								pg = progress.getProgress();
							}
						});

						p.setProgress(42);
						assertEquals(42, pg);
						p.setProgress(0);
						assertEquals(0, pg);
					}
				});
			}
		});

		addSeries(new TestLauncher("Progress with children", args) {
			{
				addTest(new TestCase("One child") {
					@Override
					public void test() throws Exception {
						Progress p = new Progress();
						Progress child = new Progress();

						p.addProgress(child, 100);

						child.setProgress(42);
						assertEquals(42, p.getProgress());
					}
				});

				addTest(new TestCase("Multiple children") {
					@Override
					public void test() throws Exception {
						Progress p = new Progress();
						Progress child1 = new Progress();
						Progress child2 = new Progress();
						Progress child3 = new Progress();

						p.addProgress(child1, 20);
						p.addProgress(child2, 60);
						p.addProgress(child3, 20);

						child1.setProgress(50);
						assertEquals(10, p.getProgress());
						child2.setProgress(100);
						assertEquals(70, p.getProgress());
						child3.setProgress(100);
						assertEquals(90, p.getProgress());
						child1.setProgress(100);
						assertEquals(100, p.getProgress());
					}
				});

				addTest(new TestCase("Listeners with children") {
					int pg;

					@Override
					public void test() throws Exception {
						final Progress p = new Progress();
						Progress child1 = new Progress();
						Progress child2 = new Progress();
						p.addProgress(child1, 50);
						p.addProgress(child2, 50);

						p.addProgressListener(new Progress.ProgressListener() {
							@Override
							public void progress(Progress progress, String name) {
								pg = p.getProgress();
							}
						});

						child1.setProgress(50);
						assertEquals(25, pg);
						child2.setProgress(100);
						assertEquals(75, pg);
						child1.setProgress(100);
						assertEquals(100, pg);
					}
				});

				addTest(new TestCase("Listeners with children, not 1-100") {
					int pg;

					@Override
					public void test() throws Exception {
						final Progress p = new Progress();
						p.setMax(1000);

						Progress child1 = new Progress();
						child1.setMax(2);

						Progress child2 = new Progress();
						p.addProgress(child1, 500);
						p.addProgress(child2, 500);

						p.addProgressListener(new Progress.ProgressListener() {
							@Override
							public void progress(Progress progress, String name) {
								pg = p.getProgress();
							}
						});

						child1.setProgress(1);
						assertEquals(250, pg);
						child2.setProgress(100);
						assertEquals(750, pg);
						child1.setProgress(2);
						assertEquals(1000, pg);
					}
				});

				addTest(new TestCase(
						"Listeners with children, not 1-100, local progress") {
					int pg;

					@Override
					public void test() throws Exception {
						final Progress p = new Progress();
						p.setMax(1000);

						Progress child1 = new Progress();
						child1.setMax(2);

						Progress child2 = new Progress();
						p.addProgress(child1, 400);
						p.addProgress(child2, 400);
						// 200 = local progress

						p.addProgressListener(new Progress.ProgressListener() {
							@Override
							public void progress(Progress progress, String name) {
								pg = p.getProgress();
							}
						});

						child1.setProgress(1);
						assertEquals(200, pg);
						child2.setProgress(100);
						assertEquals(600, pg);
						p.setProgress(100);
						assertEquals(700, pg);
						child1.setProgress(2);
						assertEquals(900, pg);
						p.setProgress(200);
						assertEquals(1000, pg);
					}
				});

				addTest(new TestCase("Listeners with 5+ children, 4+ depth") {
					int pg;

					@Override
					public void test() throws Exception {
						final Progress p = new Progress();
						Progress child1 = new Progress();
						Progress child2 = new Progress();
						p.addProgress(child1, 50);
						p.addProgress(child2, 50);
						Progress child11 = new Progress();
						child1.addProgress(child11, 100);
						Progress child111 = new Progress();
						child11.addProgress(child111, 100);
						Progress child1111 = new Progress();
						child111.addProgress(child1111, 20);
						Progress child1112 = new Progress();
						child111.addProgress(child1112, 20);
						Progress child1113 = new Progress();
						child111.addProgress(child1113, 20);
						Progress child1114 = new Progress();
						child111.addProgress(child1114, 20);
						Progress child1115 = new Progress();
						child111.addProgress(child1115, 20);

						p.addProgressListener(new Progress.ProgressListener() {
							@Override
							public void progress(Progress progress, String name) {
								pg = p.getProgress();
							}
						});

						child1111.setProgress(100);
						child1112.setProgress(50);
						child1113.setProgress(25);
						child1114.setProgress(25);
						child1115.setProgress(50);
						assertEquals(25, pg);
						child2.setProgress(100);
						assertEquals(75, pg);
						child1111.setProgress(100);
						child1112.setProgress(100);
						child1113.setProgress(100);
						child1114.setProgress(100);
						child1115.setProgress(100);
						assertEquals(100, pg);
					}
				});

				addTest(new TestCase("Listeners with children, multi-thread") {
					int pg;
					boolean decrease;
					Object lock1 = new Object();
					Object lock2 = new Object();
					int currentStep1;
					int currentStep2;

					@Override
					public void test() throws Exception {
						final Progress p = new Progress(0, 200);

						final Progress child1 = new Progress();
						final Progress child2 = new Progress();
						p.addProgress(child1, 100);
						p.addProgress(child2, 100);

						p.addProgressListener(new Progress.ProgressListener() {
							@Override
							public void progress(Progress progress, String name) {
								int now = p.getProgress();
								if (now < pg) {
									decrease = true;
								}
								pg = now;
							}
						});

						// Run 200 concurrent threads, 2 at a time allowed to
						// make progress (each on a different child)
						for (int i = 0; i <= 100; i++) {
							final int step = i;
							new Thread(new Runnable() {
								@Override
								public void run() {
									synchronized (lock1) {
										if (step > currentStep1) {
											currentStep1 = step;
											child1.setProgress(step);
										}
									}
								}
							}).start();

							new Thread(new Runnable() {
								@Override
								public void run() {
									synchronized (lock2) {
										if (step > currentStep2) {
											currentStep2 = step;
											child2.setProgress(step);
										}
									}
								}
							}).start();
						}

						int i;
						int timeout = 20; // in 1/10th of seconds
						for (i = 0; i < timeout
								&& (currentStep1 + currentStep2) < 200; i++) {
							Thread.sleep(100);
						}

						assertEquals("The test froze at step " + currentStep1
								+ " + " + currentStep2, true, i < timeout);
						assertEquals(
								"There should not have any decresing steps",
								decrease, false);
						assertEquals("The progress should have reached 200",
								200, p.getProgress());
						assertEquals(
								"The progress should have reached completion",
								true, p.isDone());
					}
				});
			}
		});
	}
}
