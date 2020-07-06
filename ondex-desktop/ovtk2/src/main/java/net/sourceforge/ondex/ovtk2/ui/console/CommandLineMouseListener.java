package net.sourceforge.ondex.ovtk2.ui.console;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;

import net.sourceforge.ondex.ovtk2.ui.RegisteredJInternalFrame;

/**
 * 
 * @author lysenkoa
 * 
 */
public class CommandLineMouseListener implements MouseListener {

	private JDesktopPane desktop;
	private JScrollPane frame;

	/**
	 * 
	 * @param desktop
	 *            the ovt2 desktop pane
	 * @param frame
	 *            the command line frame
	 */
	public CommandLineMouseListener(JDesktopPane desktop, JScrollPane frame) {
		this.desktop = desktop;
		this.frame = frame;
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() > 1) {
			JScrollPane pane = (JScrollPane) ((Component) e.getSource()).getParent().getParent();
			Component temp = pane.getParent().getParent().getParent().getParent();
			if (temp instanceof JInternalFrame) {
				cmdToBar(pane, (JInternalFrame) temp);
			} else {
				cmdToFrame(pane);
			}
		}
	}

	public void cmdToBar(JScrollPane scrollPane, JInternalFrame cmdFrame) {
		desktop.remove(cmdFrame);
		cmdFrame.dispose();
		scrollPane.getViewport().setMaximumSize(new Dimension(frame.getSize().width, 35));
		scrollPane.getViewport().setPreferredSize(new Dimension(frame.getSize().width, 35));
		frame.add(scrollPane, BorderLayout.PAGE_END);
		desktop.updateUI();
	}

	public void cmdToFrame(JScrollPane scrollingArea) {
		frame.remove(scrollingArea);
		JInternalFrame cmdFrame = new RegisteredJInternalFrame("Scripting", "Console", "Command console", true, false, false, true);
		cmdFrame.add(scrollingArea);
		cmdFrame.setVisible(true);
		cmdFrame.pack();
		int height = frame.getSize().height / 4;
		int width = 1000;
		int yPosition = frame.getSize().height - height;
		if (height < 68) {
			height = 68;
			yPosition = 0;
		}
		if (width > frame.getSize().width) {
			width = frame.getSize().width;
		}
		scrollingArea.getViewport().setPreferredSize(new Dimension(width, height));
		cmdFrame.setSize(width, height);
		cmdFrame.setLocation(-4, yPosition - 87);
		desktop.add(cmdFrame);
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent arg0) {
	}

	public void mouseReleased(MouseEvent arg0) {
	}
}