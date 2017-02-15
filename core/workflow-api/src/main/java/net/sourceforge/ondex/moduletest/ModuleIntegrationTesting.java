/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sourceforge.ondex.moduletest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.log4j.Logger;

/**
 *
 * This class can be used for integration testing of a workflow module.
 *
 * To use it extend it as a testcase and use call the setup() method
 * as the <code>@Before</code>. During your test you can use the file download function
 * <code>downloadFile()</code> to get any web resources like database flatfiles and such.
 * then you can use the <code>runOndexMini()</code> method to run ondex mini on a workflow
 * file of your choice.
 * After you're done testing you should invoke the cleanup() method as the <code>@After</code>.
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class ModuleIntegrationTesting {

    /**
     * Logger
     */
    private Logger logger = Logger.getLogger(ModuleIntegrationTesting.class);

    /**
     * The current test directory
     */
    private File testDir;

    /**
     * The ondex mini main directory
     */
    private File ominiDir;


    /**
     * Use this method to get the ondex mini main directory.
     * @return
     */
    public File getOndexMiniDir() {
        return ominiDir;
    }



    /**
     * Sets up the testing environment. Downloads and extracts ondex-mini from
     * the nexus repository and installs the plugin jar file.
     *
     * @param moduleJarName the name of the module jar file (something like mymodule-0.6.0-SNAPSHOT-jar-with-dependencies.jar)
     */
    protected void setup(String moduleJarName) {

        //make test directory
        testDir = new File("integrationtest");

        //delete possible previous tests
        cleanup();
        testDir.mkdir();

        //download ondex-mini
        File ominiZip = new File(testDir,"omini.zip");
        try {
            URL ominiURL = new URL("http://ondex.rothamsted.ac.uk/nexus/"
                    + "service/local/artifact/maven/redirect?r=snapshots&"
                    + "g=net.sourceforge.ondex.apps&a=ondex-mini"
                    + "&v=0.6.0-SNAPSHOT&e=zip&c=packaged-distro");
            download(ominiURL,ominiZip);
        } catch (MalformedURLException e) {
            throw new RuntimeException("This is a bug in the integration test.",e);
        } catch (IOException e) {
            throw new RuntimeException("Unable to download ondex-mini.",e);
        }

        //unzip ondex-mini
        try {
            unzipArchive(ominiZip, testDir);
        } catch (IOException e) {
            throw new RuntimeException("Unable to extract ondex-mini.",e);
        }
        
        //copy module to plugins folder
        ominiDir = new File(testDir,"ondex-mini-0.6.0-SNAPSHOT");
        File pluginsDir = new File(ominiDir,"plugins");

        File mvntargetDir = new File("target");
        File moduleJar = new File(mvntargetDir,moduleJarName);
        try {
            copy(moduleJar, pluginsDir);
        } catch (IOException e) {
            throw new RuntimeException("Unable to copy module jar file.",e);
        }
        
        
    }

    /**
     * Use this method to run ondex-mini on a workflow file of your choice.
     * @param workflowFile the workflow file to execute.
     * @param workflowArguments additional arguments (velocity)
     */
    protected void runOndexMini(File workflowFile, String... workflowArguments) {

        //get the ondex-mini jar file
        File jarFile = new File(new File(ominiDir,"lib"),"ondex-mini-0.6.0-SNAPSHOT.jar");

        //get the data directory
        File dataDir = new File(ominiDir,"data");

        try {

            //ondex-mini has a stupid bug due to which it doesn't accept absolute
            //paths for workflow files, so we have to transform the workflow file
            //path into a relative path.
            String relativeWorkflowPath = getRelativePath(ominiDir, workflowFile);

            //create the argument vector
            List<String> execArgList = new ArrayList<String>(Arrays.asList(
                    new String[] {"java", "-Xmx1G", "-D"+dataDir.getPath(), "-jar",
                                jarFile.getCanonicalPath(), "-ubla", "-ptest",
                                "-w"+relativeWorkflowPath}));

            //add additional arguments
            for (String warg : workflowArguments) {
                execArgList.add("-P"+warg);
            }

            //transform argument list to array so we can feed it to the exec method
            String[] execArgs = execArgList.toArray(new String[execArgList.size()]);

            System.out.println("Executing: "+execArgs);

            //execute the java process with the ondex-mini directory as current working directory
            Process process = Runtime.getRuntime().exec(execArgs, null, ominiDir);

            //read stdout and stderr and forward them to our own stdout and stderr
            BufferedReader errorReader = new BufferedReader(
                                            new InputStreamReader(process.getErrorStream())
                                         );

            BufferedReader outReader = new BufferedReader(
                                            new InputStreamReader(process.getInputStream())
                                         );
            boolean moreOut = true;
            boolean moreErrors = true;
            while (moreErrors && moreOut) {
                String line;
                if (moreErrors) {
                    if ((line = errorReader.readLine()) == null) {
                        moreErrors = false;
                    } else {
                        System.err.println(line);
                    }
                }
                if (moreOut) {
                    if ((line = outReader.readLine()) == null) {
                        moreOut = false;
                    } else {
                        System.out.println(line);
                    }
                }
            }

            //wait until the process has terminated.
            process.waitFor();

            //close all streams
            errorReader.close();
            outReader.close();
            process.getOutputStream().close();

            System.out.println("Process terminated with exit value "+process.exitValue());

            //make sure the process terminated successfully
            assert(process.exitValue() == 0);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Creates a relative path from one directory to another.
     * @param from the originating file
     * @param to the target file
     * @return the relative path
     * @throws IOException
     */
    private String getRelativePath(File from, File to) throws IOException {

        String s = File.separator;

        String[] fromCP = from.getCanonicalPath().split(s);
        String[] toCP = to.getCanonicalPath().split(s);

        int lastCommonIndex = -1;
        for (int i = 0; fromCP[i].equals(toCP[i]); i++) {
            lastCommonIndex = i;
        }
        
        StringBuilder b = new StringBuilder();
        for (int i = fromCP.length -1; i > lastCommonIndex; i--) {
            b.append("..").append(s);
        }
        
        for (int i = lastCommonIndex+1; i < toCP.length; i++) {
            b.append(toCP[i]).append(s);
        }

        b.deleteCharAt(b.length()-1);

        return b.toString();
    }

    /**
     * Downloads a file and passes on the handle.
     * @param name name of the file
     * @param url URL of the file
     * @return the file
     */
    protected File downloadFile(String name, String url) {

        try {

            File file = new File(testDir, name);
            URL ominiURL = new URL(url);
            download(ominiURL,file);
            return file;

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }

    /**
     * Delete all files in the testing environment
     */
    private void cleanup() {
        if (testDir != null && testDir.exists()) {
            try {
                delete(testDir);
            } catch (IOException e) {
                throw new RuntimeException("Failed to cleanup test environment.",e);
            }
        }
    }

    /**
     * Recursively deletes the given directory and all its contents
     * @param f a file or directory to delete
     * @throws IOException
     */
    private void delete(File f) throws IOException {
      if (f.isDirectory()) {
        for (File c : f.listFiles())
          delete(c);
      }
      if (!f.delete())
        throw new IOException("Failed to delete file: " + f);
    }


    /**
     * Download file from given url to the given destination file
     * @param url the URL where the file originates
     * @param outFile the download destination file
     * @throws IOException if an IO error occurrs
     */
    private void download(URL url, File outFile) throws IOException {

        System.out.println("Trying to download: " + url);

        InputStream in = null;
        OutputStream out = null;

        try {
            URLConnection conn = url.openConnection();

            in = conn.getInputStream();
            out = new BufferedOutputStream(new FileOutputStream(outFile));

            byte[] buf = new byte[1024];
            int len;
            while((len = in.read(buf)) > -1) {
                out.write(buf, 0, len);
            }


        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("Unable to close stream.",e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error("Unable to close stream.",e);
                }
            }
        }

    }

    /**
     * Copy file to given directory
     * @param sourceFile file to copy
     * @param targetDir target directory
     * @throws IOException if an IO error occurrs.
     */
    private void copy(File sourceFile, File targetDir) throws IOException {

        System.out.println("Copying "+sourceFile.getCanonicalPath()+" -> "+
                targetDir.getCanonicalPath());

        File targetFile = new File(targetDir,sourceFile.getName());

        InputStream in = null;
        OutputStream out = null;

        try {
            
            in = new FileInputStream(sourceFile);
            out = new BufferedOutputStream(new FileOutputStream(targetFile));

            byte[] buf = new byte[1024];
            int len;
            while((len = in.read(buf)) > -1) {
                out.write(buf, 0, len);
            }
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("Unable to close stream.",e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error("Unable to close stream.",e);
                }
            }
        }
    }

    public void unzipArchive(File archive, File outputDir) throws IOException {
        System.out.println("Unzipping: "+archive.getCanonicalPath()+" to "+
                outputDir.getCanonicalPath());
        ZipFile zipfile = new ZipFile(archive);
        for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) e.nextElement();
            unzipEntry(zipfile, entry, outputDir);
        }
    }

    private void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException {

        System.out.println("Unzipping entry: "+entry.getName());

        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()){
            createDir(outputFile.getParentFile());
        }

        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            byte[] buf = new byte[1024];
            int len;
            while((len = inputStream.read(buf)) > -1) {
                outputStream.write(buf, 0, len);
            }
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }

    private void createDir(File dir) {
        System.out.println("Creating directory "+dir.getAbsolutePath());
        if(!dir.mkdirs()) throw new RuntimeException("Can not create dir "+dir);
    }

}
