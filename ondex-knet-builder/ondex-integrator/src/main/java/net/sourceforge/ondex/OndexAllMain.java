package net.sourceforge.ondex;

public class OndexAllMain {
    public static void main(String[] args) {
        System.setProperty("plugin.scan.lib", Boolean.TRUE.toString());
        WorkflowMain.main(args);
    }

}
