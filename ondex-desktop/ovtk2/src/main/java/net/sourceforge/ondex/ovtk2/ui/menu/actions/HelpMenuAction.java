package net.sourceforge.ondex.ovtk2.ui.menu.actions;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop.Position;
import net.sourceforge.ondex.ovtk2.ui.dialog.ErrorReportingDialog;
import net.sourceforge.ondex.ovtk2.ui.dialog.WelcomeDialog;
import net.sourceforge.ondex.ovtk2.util.DesktopUtils;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;

/**
 * Listens to action events specific to the help menu.
 * 
 * @author taubertj
 * 
 */
public class HelpMenuAction implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent ae) {

		String cmd = ae.getActionCommand();
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();

		// simple about message
		if (cmd.equals("about")) {
			JOptionPane.showInternalMessageDialog(desktop.getDesktopPane(), Config.language.getProperty("Dialog.About.Text"), Config.language.getProperty("Dialog.About.Title"), JOptionPane.PLAIN_MESSAGE, new ImageIcon("config/toolbarButtonGraphics/general/About24.gif"));
		}

		// version information
		else if (cmd.equals("version")) {
			StringBuilder b = new StringBuilder(Config.language.getProperty("Dialog.Version.Text"));
			b.append("\n");

			String build = DesktopUtils.extractBuildNumber();
			if (build != null) {
				b.append("Build: " + build + "\n");
			} else {
				b.append("Build: N/A\n");
			}

			String arch = System.getProperty("os.arch");
			String osname = System.getProperty("os.name");
			String osversion = System.getProperty("os.version");

			b.append("System: " + arch + " " + osname + " v" + osversion + "\n");

			long maxMem = Runtime.getRuntime().maxMemory() / (1024 * 1024);
			long totalMem = Runtime.getRuntime().totalMemory() / (1024 * 1024);
			long freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024);
			b.append("Memory: current=" + totalMem + "MB unclaimed=" + freeMem + "MB max=" + maxMem + "MB\n");

			File file = new File(Config.ovtkDir);
			long totalSpace = file.getTotalSpace() / (1024 * 1024);
			long freeSpace = file.getFreeSpace() / (1024 * 1024);
			b.append("Disk: total=" + totalSpace + "MB free=" + freeSpace + "MB\n");

			JOptionPane.showInternalMessageDialog(desktop.getDesktopPane(), b.toString(), Config.language.getProperty("Dialog.Version.Title"), JOptionPane.PLAIN_MESSAGE, new ImageIcon("config/toolbarButtonGraphics/general/About24.gif"));
		}

		// open tutorial
		else if (cmd.equals("tutorial")) {

			// where is file?
			File tutorialLocation = new File(Config.docuDir + "/tutorial/" + Config.config.getProperty("Program.Language") + "/" + Config.config.getProperty("Tutorial.HTML"));
			if (tutorialLocation.exists()) {
				URL tutorialURL = null;

				// try opening URL in system browser
				try {
					tutorialURL = tutorialLocation.toURI().toURL();
					Desktop.getDesktop().browse(tutorialURL.toURI());
				} catch (Exception e) {
					ErrorDialog.show(e);
				}
			} else {
				JOptionPane.showInternalMessageDialog(desktop.getDesktopPane(), Config.language.getProperty("Dialog.Tutorial.NotFoundText"), Config.language.getProperty("Dialog.Tutorial.Title"), JOptionPane.ERROR_MESSAGE);
			}
		}

		// show welcome message again
		else if (cmd.equals("welcome")) {
			WelcomeDialog welcome = WelcomeDialog.getInstance(desktop);
			if (!welcome.isVisible())
				welcome.setVisible(true);
			if (welcome.isIcon())
				try {
					welcome.setIcon(false);
				} catch (PropertyVetoException e) {
					ErrorDialog.show(e);
				}
			welcome.toFront();
		}

		// show error dialog
		else if (cmd.equals("error")) {
			ErrorReportingDialog error = new ErrorReportingDialog();
			OVTK2Desktop.getInstance().display(error, Position.centered);
		}
	}
}
