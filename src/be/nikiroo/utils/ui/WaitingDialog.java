package be.nikiroo.utils.ui;

import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import be.nikiroo.utils.Progress;
import be.nikiroo.utils.Progress.ProgressListener;

/**
 * A small waiting dialog that will show only if more than X milliseconds passed
 * before we dismiss it.
 * 
 * @author niki
 */
public class WaitingDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private boolean waitScreen;
	private Object waitLock = new Object();

	private Progress pg;
	private ProgressListener pgl;

	/**
	 * Create a new {@link WaitingDialog}.
	 * 
	 * @param parent
	 *            the parent/owner of this {@link WaitingDialog}
	 * @param delayMs
	 *            the delay after which to show the dialog if it is still not
	 *            dismiss (see {@link WaitingDialog#dismiss()})
	 */
	public WaitingDialog(Window parent, long delayMs) {
		this(parent, delayMs, null, null);
	}

	/**
	 * Create a new {@link WaitingDialog}.
	 * 
	 * @param parent
	 *            the parent/owner of this {@link WaitingDialog}
	 * @param delayMs
	 *            the delay after which to show the dialog if it is still not
	 *            dismiss (see {@link WaitingDialog#dismiss()})
	 * @param pg
	 *            the {@link Progress} to listen on -- when it is
	 *            {@link Progress#done()}, this {@link WaitingDialog} will
	 *            automatically be dismissed as if
	 *            {@link WaitingDialog#dismiss()} was called
	 */
	public WaitingDialog(Window parent, long delayMs, Progress pg) {
		this(parent, delayMs, pg, null);
	}

	/**
	 * Create a new {@link WaitingDialog}.
	 * 
	 * @param parent
	 *            the parent/owner of this {@link WaitingDialog}
	 * @param delayMs
	 *            the delay after which to show the dialog if it is still not
	 *            dismiss (see {@link WaitingDialog#dismiss()})
	 * @param waitingText
	 *            a waiting text to display (note: you may want to subclass it
	 *            for nicer UI)
	 */
	public WaitingDialog(Window parent, long delayMs, String waitingText) {
		this(parent, delayMs, null, waitingText);
	}

	/**
	 * Create a new {@link WaitingDialog}.
	 * 
	 * @param parent
	 *            the parent/owner of this {@link WaitingDialog}
	 * @param delayMs
	 *            the delay after which to show the dialog if it is still not
	 *            dismiss (see {@link WaitingDialog#dismiss()})
	 * @param pg
	 *            the {@link Progress} to listen on -- when it is
	 *            {@link Progress#done()}, this {@link WaitingDialog} will
	 *            automatically be dismissed as if
	 *            {@link WaitingDialog#dismiss()} was called
	 * @param waitingText
	 *            a waiting text to display (note: you may want to subclass it
	 *            for nicer UI)
	 */
	public WaitingDialog(Window parent, long delayMs, Progress pg,
			String waitingText) {
		super(parent);

		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		this.pg = pg;

		if (waitingText != null) {
			JLabel waitingTextLabel = new JLabel(waitingText);
			this.add(waitingTextLabel);
			waitingTextLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
			this.pack();
		}

		if (pg != null) {
			pgl = new ProgressListener() {
				@Override
				public void progress(Progress progress, String name) {
					if (WaitingDialog.this.pg.isDone()) {
						// Must be done out of this thread (cannot remove a pgl
						// from a running pgl)
						new SwingWorker<Void, Void>() {
							@Override
							protected Void doInBackground() throws Exception {
								return null;
							}

							@Override
							public void done() {
								dismiss();
							}
						}.execute();
					}
				}
			};

			pg.addProgressListener(pgl);

			if (pg.isDone()) {
				dismiss();
				return;
			}
		}

		final long delay = delayMs;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
				}

				synchronized (waitLock) {
					if (!waitScreen) {
						waitScreen = true;
						setVisible(true);
					}
				}
			}
		}).start();
	}

	/**
	 * Notify this {@link WaitingDialog} that the job is done, and dismiss it if
	 * it was already showing on screen (or never show it if it was not).
	 * <p>
	 * Will also dispose the {@link WaitingDialog}.
	 */
	public void dismiss() {
		synchronized (waitLock) {
			if (waitScreen) {
				setVisible(false);
			}
			waitScreen = true;
		}

		if (pg != null) {
			pg.removeProgressListener(pgl);
		}

		dispose();
	}
}
