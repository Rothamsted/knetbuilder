package net.sourceforge.ondex.ovtk2.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;

/**
 * This is the OVTK's ProgressMonitor class. It can automatically monitor a
 * running process that has implemented the interface "Monitorable". For easier
 * use it also provides several convenience methods to cope with race
 * conditions. Whenever the actual "Monitorable" instance is not accessible
 * before the worker thread has been started, the waitForInitialization() method
 * can be called in the gui thread. In the meantime the worker thread can take
 * its time to initialize everything and then call the
 * notifyInitializationDone() method before starting the real computation.
 * 
 * Consider the following example with a global variable
 * <code> monitorableProcess </code>:
 * 
 * <code> 
 * 
 * monitorableProcess = new ExampleProcess();
 * 
 * Thread worker = new Thread("worker") {
 * 		public void run() {
 * 			monitorableProcess.doInitStuff();
 * 			OVTKProgressMonitor.notifyInitializationDone(monitorableProcess);
 * 			monitorableProcess.doWork();   //this method releases the semaphore when the monitorableProcess initialized
 * 		}
 * };
 * worker.start();
 * 
 * OVTKProgressMonitor.waitForInitialization(monitorableProcess);
 * OVTKProgressMonitor.start("My Process", monitorableProcess);
 * </code>
 * 
 * 
 * If however the Monitorable instance is ready from the start, anyway, then you
 * might just as well just start the worker thread and call the start() method
 * without worrying about sync.
 * 
 * Example:
 * 
 * <code>
 * final Monitorable monitorableProcess = new MyMonitorableProcess();
 * Thread t = new Thread("worker") {
 * 		public void run() {
 * 			monitorableProcess.doWork();
 * 		}
 * };
 * t.start();
 * 
 * OVTKProgressMonitor.start("My Process", monitorableProcess);
 * </code>
 * 
 * @author Jochen Weile, B.Sc.
 * 
 */
public class OVTKProgressMonitor extends JDialog implements ActionListener {

	// ####FIELDS####

	/**
	 * generated
	 */
	private static final long serialVersionUID = 4688250806269698801L;

	/**
	 * the supervised monitorable process.
	 */
	private Monitorable process;

	/**
	 * the progress bar, which is displayed in the middle of the dialog window.
	 */
	private JProgressBar progressbar;

	/**
	 * whether the user has canceled the process.
	 */
	private boolean canceled = false;

	/**
	 * describes the current state of the process.
	 */
	private JLabel note;

	// ####CONSTRUCTOR####

	/**
	 * private constructor will be called with start()
	 */
	private OVTKProgressMonitor(Frame main, String title, final Monitorable process) {
		super(main, title);
		this.process = process;

		progressbar = new JProgressBar(process.getMinProgress(), process.getMaxProgress());
		progressbar.setIndeterminate(process.isIndeterminate());

		note = new JLabel("   idle");

		setupGUI();

		startFeeder();
	}

	/**
	 * sets up the content and features of the dialog window
	 */
	private void setupGUI() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

		getContentPane().add(Box.createRigidArea(new Dimension(10, 10)));

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(Box.createRigidArea(new Dimension(10, 10)), BorderLayout.EAST);
		p.add(note, BorderLayout.WEST);
		p.add(Box.createRigidArea(new Dimension(10, 10)), BorderLayout.CENTER);
		getContentPane().add(p);

		getContentPane().add(Box.createRigidArea(new Dimension(10, 10)));

		p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(Box.createRigidArea(new Dimension(10, 10)), BorderLayout.EAST);
		p.add(Box.createRigidArea(new Dimension(10, 10)), BorderLayout.WEST);
		p.add(progressbar, BorderLayout.CENTER);
		getContentPane().add(p);

		getContentPane().add(Box.createRigidArea(new Dimension(10, 10)));

		JButton b = new JButton("Cancel");
		b.setActionCommand("cancel");
		b.addActionListener(this);
		b.setEnabled(process.isAbortable());

		p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.CENTER));
		p.add(b);
		getContentPane().add(p);
		getContentPane().add(Box.createRigidArea(new Dimension(10, 10)));

		pack();
		setSize(400, getHeight());
		int x = getParent().getX();
		int y = getParent().getY();
		int w = getParent().getWidth();
		int h = getParent().getHeight();
		int w_self = getWidth();
		int h_self = getHeight();
		this.setBounds(x + (w - w_self) / 2, y + (h - h_self) / 2, w_self, h_self);

		setVisible(true);
	}

	// ####METHODS####

	/**
	 * starts the feeder thread.
	 */
	private void startFeeder() {
		// feeds the monitor continuously with the information it gets from the
		// process
		Thread feederthread = new Thread(getTitle() + " progess feeder") {
			public void run() {
				// re-use this as "   " + progress.getState() was accounting for
				// 10~20% of cpu
				StringBuilder sb = new StringBuilder();

				while (!process.getState().equals(Monitorable.STATE_TERMINAL)) {
					if (!process.isIndeterminate()) {
						progressbar.setMinimum(process.getMinProgress());
						progressbar.setMaximum(process.getMaxProgress());
						progressbar.setValue(process.getProgress());
					}
					sb.append("   ").append(process.getState());
					note.setText(sb.toString());
					sb.setLength(0);

					if (canceled || (process.getProgress() >= process.getMaxProgress()) || (process.getUncaughtException() != null)) {
						process.setCancelled(canceled);
						break;
					}
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
					}
				}
				setVisible(false);
				if (process.getUncaughtException() != null) {
					Throwable exc = process.getUncaughtException();
					exc.printStackTrace();
					ErrorDialog.show(exc);
				}
				dispose();
			}
		};
		feederthread.start();
	}

	/**
	 * basically calls wait in a synchronzied environment on the monitorable
	 * process.
	 * 
	 * @param m
	 *            the process.
	 */
	public static void waitForInitialization(Monitorable m) {
		synchronized (m) {
			try {
				m.wait();
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * basically calles notifyAll in a synchronized environment on the
	 * monitorable process.
	 * 
	 * @param m
	 *            the process.
	 */
	public static void notifyInitializationDone(Monitorable m) {
		synchronized (m) {
			m.notifyAll();
		}
	}

	/**
	 * Method for compatibility with previous versions
	 * 
	 * @param title
	 * @param process
	 */
	public static void start(String title, final Monitorable process) {
		// running in Desktop version
		if (OVTK2Desktop.getDesktopResources().getSelectedViewer() instanceof OVTK2Viewer)
			start(OVTK2Desktop.getInstance().getMainFrame(), title, process);
		else
			start(null, title, process);
	}

	/**
	 * starts the process monitor.
	 * 
	 * @param main
	 *            the main frame the display progress monitor at
	 * @param title
	 *            the name of the process that is monitored.
	 * @param process
	 *            the process to be monitored.
	 */
	public static void start(Frame main, String title, final Monitorable process) {
		new OVTKProgressMonitor(main, title, process);
	}

	/**
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("cancel"))
			canceled = true;
	}

}
