package net.sourceforge.ondex;

import net.sourceforge.ondex.workflow2.gui.Main;

public class OndexLauncherAll {

	public static void main(String[] args) {
		System.setProperty("plugin.scan.lib", Boolean.TRUE.toString());
		Main.main(args);
	}

}
