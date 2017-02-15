package net.sourceforge.ondex.workflow2.gui;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.init.PluginRegistry;
import org.apache.commons.cli.*;

import javax.swing.*;
import java.io.File;

public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("d", "data", true, "ONDEX data directory can also be specified as a Systems property under \"ondex.dir\"");
        options.addOption("z", "plugins", true, "Directory where the jar files for Ondex plugins were fortuitously deposited.");
        options.addOption("u", "username", true, "User name for the database Session");
        options.addOption("p", "password", true, "Password for the database Session");
        options.addOption("h", "help", false, "Usage help for this program");
        options.addOption("f", "lookandfeel", true, "Use this option to set Java look and feel rather than system one.");
        options.addOption("l", "libraries", true, "comma-separated list of identifiers for libraries which to load");

        CommandLine cmd = null;

        try {
            CommandLineParser parser = new GnuParser();
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ONDEX workflow", options, true);
            System.exit(0);
        }


        if (cmd.hasOption("f")) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
        } else {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
        }

        String ondexDir = null; //ondex data directory
        String ondexWorkflowFile = null; //ondex xml file that specifies the workflow to run
        String username = null;
        String password = null;

        if (cmd.hasOption('d')) {
            ondexDir = cmd.getOptionValue('d');
            System.setProperty("ondex.dir", ondexDir);
        }

        if (cmd.hasOption('w')) {
            ondexWorkflowFile = cmd.getOptionValue('w');
        }

        if (cmd.hasOption('u')) {
            username = cmd.getOptionValue('u');
        }

        if (cmd.hasOption('p')) {
            password = cmd.getOptionValue('p');
        }

        if (cmd.hasOption('l')) {
            System.out.print("Loading native libraries...");

            String idList = cmd.getOptionValue('l');
            String[] ids = idList.split(",");
            for (String id : ids) {
                try {
                    System.loadLibrary(id);
                } catch (UnsatisfiedLinkError e) {
                    System.out.println();
                    System.err.println("Failed to load native library:" + id);
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
            }
        }

        if (ondexDir == null) {
            if (net.sourceforge.ondex.config.Config.ondexDir != null) {
                ondexDir = net.sourceforge.ondex.config.Config.ondexDir;
            }
        } else {
            System.setProperty("ondex.dir", ondexDir);
        }

        if (ondexDir == null) {
            ondexDir = "/data";
        }

        if (ondexWorkflowFile == null) {
            ondexWorkflowFile = ondexDir + File.separator + "xml" + File.separator + "ONDEXParameters.xml";
        }
        System.out.println("Using data dir " + ondexDir);
        System.out.println("Using workflow file " + ondexWorkflowFile);

        Config.ondexDir = ondexDir;
        try {
            Config.loadConfig();
        }
        catch (Exception e) {
            System.err.println("Config file not found, no settings loaded.");
        }

        String pluginsDir = cmd.getOptionValue('z');
        if (pluginsDir == null) {
            pluginsDir = "/plugins";
        }

        try {
            PluginRegistry.init(true, pluginsDir);
            WorkflowTool wt = new WorkflowTool();
            wt.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
