package net.sourceforge.ondex.ovtk2.ui;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import net.sourceforge.ondex.ovtk2.util.RegisteredFrame;
import net.sourceforge.ondex.ovtk2.util.WindowRegister;

/**
 * A registered JInternalFrame that will be displayed in the top menu
 * 
 * @author hindlem
 * 
 */
public class RegisteredJInternalFrame extends JInternalFrame implements RegisteredFrame, InternalFrameListener {

	private static final long serialVersionUID = 1L;

	private String group;

	/**
	 * Creates a <code>JInternalFrame</code> with the specified title,
	 * resizability, closability, maximizability, and iconifiability. All
	 * <code>JInternalFrame</code> constructors use this one.
	 * 
	 * @param name
	 *            the name of this frame
	 * @param group
	 *            the group this frame belongs to
	 * @param title
	 *            the <code>String</code> to display in the title bar
	 * @param resizable
	 *            if <code>true</code>, the internal frame can be resized
	 * @param closable
	 *            if <code>true</code>, the internal frame can be closed
	 * @param maximizable
	 *            if <code>true</code>, the internal frame can be maximized
	 * @param iconifiable
	 *            if <code>true</code>, the internal frame can be iconified
	 */
	public RegisteredJInternalFrame(String name, String group, String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
		super(title, resizable, closable, maximizable, iconifiable);
		super.setName(name);
		this.group = group;
		WindowRegister.getInstance().registerInternalFrame(this);
		this.addInternalFrameListener(this);
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public void setName(String s) {
		super.setName(s);
		WindowRegister.getInstance().update();
	}

	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
		WindowRegister.getInstance().deregisterInternalFrame(this);
	}

	@Override
	public void internalFrameClosing(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameIconified(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameOpened(InternalFrameEvent e) {

	}

}
