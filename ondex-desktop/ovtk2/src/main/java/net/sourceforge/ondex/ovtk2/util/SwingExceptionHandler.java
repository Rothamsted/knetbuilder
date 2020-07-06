package net.sourceforge.ondex.ovtk2.util;

import java.awt.Frame;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class SwingExceptionHandler implements Thread.UncaughtExceptionHandler {

	@Override
	public void uncaughtException(final Thread t, final Throwable e) {
		if (SwingUtilities.isEventDispatchThread()) {
			showMessage(t, e);
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						showMessage(t, e);
					}
				});
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			} catch (InvocationTargetException ite) {
				// not much more we can do here except log the exception
				ite.getCause().printStackTrace();
			}
		}
	}

	private String generateStackTrace(Throwable e) {
		StringWriter writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		e.printStackTrace(pw);
		pw.close();
		return writer.toString();
	}

	private void showMessage(Thread t, Throwable e) {
		String stackTrace = generateStackTrace(e);
		// show an error dialog
		JOptionPane.showMessageDialog(findActiveOrVisibleFrame(), stackTrace, "Exception Occurred in " + t, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * We look for an active frame and attach ourselves to that.
	 */
	private Frame findActiveOrVisibleFrame() {
		Frame[] frames = JFrame.getFrames();
		for (Frame frame : frames) {
			if (frame.isActive()) {
				return frame;
			}
		}
		for (Frame frame : frames) {
			if (frame.isVisible()) {
				return frame;
			}
		}
		return null;
	}
}
