package net.sourceforge.ondex.ovtk2.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;

public class OVTKSplashScreen extends JFrame {

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

	private SplashPanel splashpanel;

	private JLabel label;

	private JFrame instance;

	// ####CONSTRUCTOR####

	/**
	 * private constructor will be called with start()
	 */
	private OVTKSplashScreen(final Monitorable process) {
		super("OVTK2");
		this.process = process;
		this.instance = this;

		progressbar = new JProgressBar(process.getMinProgress(), process.getMaxProgress());
		progressbar.setIndeterminate(process.isIndeterminate());

		setupGUI();

		startFeeder();
	}

	/**
	 * sets up the content and features of the dialog window
	 */
	private void setupGUI() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setUndecorated(true);

		JPanel mainContainer = new JPanel(new BorderLayout());
		mainContainer.setBorder(BorderFactory.createLineBorder(Color.black, 3));
		mainContainer.setBackground(Color.white);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mainContainer, BorderLayout.CENTER);

		// picture panel
		splashpanel = new SplashPanel();
		mainContainer.add(splashpanel, BorderLayout.CENTER);

		label = new JLabel("   loading...");
		JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 0, 0));
		bottomPanel.setBackground(Color.white);
		bottomPanel.add(label);
		bottomPanel.add(progressbar);
		mainContainer.add(bottomPanel, BorderLayout.SOUTH);

		// adjust window size
		pack();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int w = screen.width;
		int h = screen.height;
		int w_self = getWidth();
		int h_self = getHeight();
		this.setBounds((w - w_self) / 2, (h - h_self) / 2, w_self, h_self);

		setVisible(true);
	}

	// /**
	// * sets up the content and features of the dialog window
	// */
	// private void setupGUI() {
	// setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	//
	// getContentPane().setLayout(new
	// BoxLayout(getContentPane(),BoxLayout.PAGE_AXIS));
	//
	// //ten pixel space at the top
	// getContentPane().add(Box.createRigidArea(new Dimension(10,10)));
	//
	// //message panel
	// JPanel p = new JPanel();
	// p.setLayout(new BorderLayout());
	// p.add(Box.createRigidArea(new Dimension(10,10)),BorderLayout.EAST);
	// p.add(note,BorderLayout.WEST);
	// p.add(Box.createRigidArea(new Dimension(10,10)),BorderLayout.CENTER);
	// getContentPane().add(p);
	//
	// //ten pixel space
	// getContentPane().add(Box.createRigidArea(new Dimension(10,10)));
	//
	// //progressbar
	// p = new JPanel();
	// p.setLayout(new BorderLayout());
	// p.add(Box.createRigidArea(new Dimension(10,10)),BorderLayout.EAST);
	// p.add(Box.createRigidArea(new Dimension(10,10)),BorderLayout.WEST);
	// p.add(progressbar,BorderLayout.CENTER);
	// getContentPane().add(p);
	//
	// //ten pixel space
	// getContentPane().add(Box.createRigidArea(new Dimension(10,10)));
	//
	// //adjust window size
	// pack();
	// setSize(400, getHeight());
	// Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	// int w = screen.width;
	// int h = screen.height;
	// int w_self = getWidth();
	// int h_self = getHeight();
	// this.setBounds((w-w_self)/2, (h-h_self)/2, w_self, h_self);
	//
	// setVisible(true);
	// }

	// ####METHODS####

	/**
	 * starts the feeder thread.
	 */
	private void startFeeder() {
		// feeds the monitor continuously with the information it gets from the
		// process
		Thread feederThread = new Thread(getTitle() + " progess feeder") {
			public void run() {
				while (!process.getState().equals(Monitorable.STATE_TERMINAL)) {
					if (!process.isIndeterminate()) {
						progressbar.setMinimum(process.getMinProgress());
						progressbar.setMaximum(process.getMaxProgress());
						progressbar.setValue(process.getProgress());
					}
					label.setText("   " + process.getState());
					// make sure the splash screen stays in front
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
					}
				}
				setVisible(false);
				dispose();
			}
		};
		feederThread.start();
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
	 * starts the process monitor.
	 * 
	 * @param process
	 *            the process to be monitored.
	 */
	public static void start(final Monitorable process) {
		new OVTKSplashScreen(process);
	}

	private class SplashPanel extends JPanel {

		BufferedImage img;

		SplashPanel() {
			getImage();
			setupGUI();
		}

		private void getImage() {
			String s = File.separator;
			File imgFile = new File(Config.ovtkDir + s + "themes" + s + "default" + s + "images" + s + "logo2" + ".png");
			try {
				img = ImageIO.read(imgFile);
			} catch (IOException e) {
				try {
					throw new Exception("failed to read :" + imgFile, e);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}

		private void setupGUI() {
			if (img != null) {
				Dimension d = new Dimension(img.getWidth(), img.getHeight());
				setMinimumSize(d);
				setMaximumSize(d);
				setSize(d);
				setPreferredSize(d);
			}

		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 5138052729693336611L;

		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2 = (Graphics2D) g;
			if (img != null) {
				g2.drawImage(img, 0, 0, null);
			}
		}

	}

}
