package net.sourceforge.ondex;

public class OndexMiniMain {

	public static void main(String[] args) {
        System.setProperty("plugin.scan.lib", Boolean.TRUE.toString());
        net.sourceforge.ondex.WorkflowMain.main(args);
    }
	
}
