package net.sourceforge.ondex;

import net.sourceforge.ondex.workflow.base.WorkflowMain;

public class OndexMiniMain {

	public static void main(String[] args) {
        System.setProperty("plugin.scan.lib", Boolean.TRUE.toString());
        net.sourceforge.ondex.workflow.base.WorkflowMain.main(args);
    }
	
}
