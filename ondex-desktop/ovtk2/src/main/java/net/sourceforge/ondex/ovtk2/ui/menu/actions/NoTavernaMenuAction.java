package net.sourceforge.ondex.ovtk2.ui.menu.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;

/**
 * Listens to action events specific to the help menu.
 * 
 * @author taubertj
 * 
 */
public class NoTavernaMenuAction implements ActionListener {

	Icon icon = new ImageIcon("config/toolbarButtonGraphics/taverna/taverna.jpeg");

	@Override
	public void actionPerformed(ActionEvent ae) {

		String cmd = ae.getActionCommand();
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();

		// simple about message
		if (cmd.equals("TavernaMissing")) {
			JOptionPane.showInternalMessageDialog(desktop.getDesktopPane(), Config.language.getProperty("Taverna.Missing.Text"), Config.language.getProperty("Taverna.Missing.Title"), JOptionPane.PLAIN_MESSAGE, icon);
		}

	}
}
