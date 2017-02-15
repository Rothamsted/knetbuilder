package net.sourceforge.ondex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.init.PluginRegistry;
import net.sourceforge.ondex.workflow.engine.BasicJobImpl;
import net.sourceforge.ondex.workflow.engine.ResourcePool;
import net.sourceforge.ondex.workflow.model.WorkflowDescription;
import net.sourceforge.ondex.workflow.model.WorkflowDescriptionIO;
import net.sourceforge.ondex.workflow.model.WorkflowTask;
import net.sourceforge.ondex.workflow.validation.ErrorReport;
import net.sourceforge.ondex.workflow.validation.PluginValidator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * The main entry point for running workflows
 *
 * @author hindlem
 */
public class WorkflowMain {
    private static final Logger LOG = Logger.getLogger(WorkflowMain.class);

    /**
     * The main entry point for running ONDEX workflows.
     *
     * @param args options -d -w -help -u -p (see help for details)
     */
    public static void main(String[] args) {

        Options options = new Options();
        options.addOption("d", "data", true, "ONDEX data directory can also be specified as a Systems property under \"ondex.dir\"");
        options.addOption("w", "workflow", true, "ONDEX workflow file the default is found under \"ondex.dir\"" + File.separator + "xml" + File.separator + "ONDEXParameters.xml");
        options.addOption("u", "username", true, "User name for the database Session");
        options.addOption("p", "password", true, "Password for the database Session");
        options.addOption("h", "help", false, "Usage help for this program");
        options.addOption("l", "libraries", true, "comma-separated list of identifiers for libraries which to load");
        options.addOption(OptionBuilder
                .withArgName("property=value")
                .hasArgs(2)
                .withValueSeparator()
                .withDescription("Any properties to pass on to the workflow")
                .create("P"));

        CommandLine cmd;

        try {
            CommandLineParser parser = new GnuParser();
            cmd = parser.parse(options, args);
        } catch (ParseException e1) {
            System.err.println("Problem processing command-line");
            e1.printStackTrace();
            printHelp(options);
            System.exit(1);
            return; // hack: java doesn't know about System.exit
        }

        if (cmd.getArgs().length != 0) {
            System.err.println("Unexpected arguments: " + cmd.getArgList());
            printHelp(options);
            System.exit(1);
        }

        if (cmd.hasOption("h")) {
            printHelp(options);
            System.exit(0);
            return; // hack: java doesn't know about System.exit
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

        Properties props = cmd.getOptionProperties("P");
        if(props == null) props = new Properties();
        LOG.debug("Properties: " + props);

        // done processing command-line options

        if (ondexDir == null) {
            if (Config.ondexDir != null) {
                ondexDir = Config.ondexDir;
            }
        } else {
            System.setProperty("ondex.dir", ondexDir);
        }

        if (ondexDir == null) {
            printHelp(options);
            System.exit(0);
        }

        if (ondexWorkflowFile == null) {
            ondexWorkflowFile = ondexDir + File.separator + "xml" + File.separator + "ONDEXParameters.xml";
        }

        LOG.info("Using data dir " + ondexDir);
        LOG.info("Using workflow file " + ondexWorkflowFile);

        Config.ondexDir = ondexDir;
        try {
            Config.loadConfig();
        } catch (IOException e) {
            System.err.println("Problems loading configuration. Attempting to continue anyway, but things may break!");
            System.err.println("Stack trace to follow");
            e.printStackTrace();
        }
       
        try {
        	
            PluginRegistry.init(true, Config.pluginDir, Config.libDir);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        File wfTmpFile = null; // declared here so that we have access to it in catch/finally blocks
        try {
            WorkflowAndPath wfap = workflowAndPath(ondexWorkflowFile);

            // variable substitution - initialise velocity
            Properties veProps = new Properties();
            veProps.put("runtime.references.strict", String.valueOf(true));
            veProps.put("file.resource.loader.path", wfap.getPath()); // handles workflow locations with leading ../
            VelocityEngine ve = new VelocityEngine(veProps);
            ve.init();

            VelocityContext ctxt = new VelocityContext(props);

            // variable substitution - process the workflow file
            Template wfTemplate = ve.getTemplate(wfap.getWorkflow());

            // the base of the temp file must not contain path seporators, so we strip off everything but the name
            wfTmpFile = File.createTempFile(new File(ondexWorkflowFile).getName(), "expanded");


            FileWriter fwTmp = new FileWriter(wfTmpFile);
            wfTemplate.merge(ctxt, fwTmp);
            fwTmp.close();
            LOG.debug("Expanded variables out to temporary file: " + wfTmpFile);

            // workflow enactment
            WorkflowDescription td = WorkflowDescriptionIO.readFile(wfTmpFile, PluginRegistry.getInstance());
            PluginValidator validator = new PluginValidator();
            int pos = 0;
            List<ErrorReport> errors = new ArrayList<ErrorReport>();
            for(WorkflowTask pc : td.getComponents()) {
                errors.addAll(validator.check(pc, ++pos));
            }
            if(!errors.isEmpty()) {
                System.err.println("There where " + errors.size() + " problems found when validating the workflow.");
                for(ErrorReport er : errors) {
                    System.err.println(er.getPluginName() + ": " + er.getPosition() + "#" + er.getPosition() + " " + er.getMessage());
                }
                System.exit(2);
            }
            BasicJobImpl job = new BasicJobImpl(new ResourcePool());
            td.toOndexJob(job);

            
            try {
                job.run();
            } finally {
                if (job != null && job.getErrorState()) {
                    throw job.getException();
                }
            }

            wfTmpFile.delete(); // clean this up on success, but on failure leave it lying about so we can look at it

        } catch (Exception e) {
            System.err.println("Error: Could not open and parse the workflow file. See stack trace for details.");
            System.err.println("Expanded workflow can be found at: " + wfTmpFile);
            e.printStackTrace();
            System.exit(2);
        }

    }

    private static void printHelp(Options options)
    {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ONDEX workflow", options, true);
        System.out.println("Exit codes:");
        System.out.println("  0: SUCCESS");
        System.out.println("  1: ARGUMENTS_FAIL");
        System.out.println("  2: WORKFLOW_FAIL");
    }

    private static WorkflowAndPath workflowAndPath(String wf)
    {
        return wfap(wf, "./");
    }

    private static WorkflowAndPath wfap(String wf, String p)
    {
        String pfx = "../";
        if(wf.startsWith(pfx))
        {
            return wfap(wf.substring(pfx.length()), pfx + p);
        }
        else
        {
            return new WorkflowAndPath(wf, p);
        }
    }

    private static class WorkflowAndPath
    {
        private final String workflow;
        private final String path;

        public WorkflowAndPath(String workflow, String path)
        {
            if(workflow == null) throw new NullPointerException();
            if(path == null) throw new NullPointerException();

            this.workflow = workflow;
            this.path = path;
        }

        public String getWorkflow()
        {
            return workflow;
        }

        public String getPath()
        {
            return path;
        }
    }
}