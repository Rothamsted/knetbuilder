package net.sourceforge.ondex;

import net.sourceforge.ondex.workflow.base.WorkflowMain;

public class OndexAllMain {
    public static void main(String[] args) {
        System.setProperty("plugin.scan.lib", Boolean.TRUE.toString());
        WorkflowMain.main(args);
    }

}
