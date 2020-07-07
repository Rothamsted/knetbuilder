package net.sourceforge.ondex.ovtk2.util;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

public class LauncherFrame implements RegisteredFrame, WindowListener {

	JFrame frame;

	String group, name;

	public LauncherFrame(JFrame frame, String group, String name) {
		frame.addWindowListener(this);
		this.frame = frame;
		this.group = group;
		this.name = name;
		WindowRegister.getInstance().registerInternalFrame(this);
	}

	public JFrame getJFrame() {
		return frame;
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void windowActivated(WindowEvent e) {

	}

	@Override
	public void windowClosed(WindowEvent e) {
		WindowRegister.getInstance().deregisterInternalFrame(this);
	}

	@Override
	public void windowClosing(WindowEvent e) {

	}

	@Override
	public void windowDeactivated(WindowEvent e) {

	}

	@Override
	public void windowDeiconified(WindowEvent e) {

	}

	@Override
	public void windowIconified(WindowEvent e) {

	}

	@Override
	public void windowOpened(WindowEvent e) {

	}
}
