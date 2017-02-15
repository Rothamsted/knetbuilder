package net.sourceforge.ondex.tools;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for deleting directories.
 * 
 * @author taubertj
 * 
 */
public abstract class DirUtils {

    /**
     * Waits for 10 times the multiple
     * @param multiple How many time 10 to wait 
     * @param errorMessage Message to include if there is an interrupt
     * @throws IOException Wrapped Interrupt Exception
     */
    private static void wait10(int multiple, String errorMessage) throws IOException{
        synchronized (errorMessage) {
            try {
                errorMessage.wait(10 * multiple);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
                throw new IOException(errorMessage);
            }
        }
    }

    /**
     * Delete given directory by File.
     * 
     * @param path
     *            File
     * @throws IOException
     */
    public static void deleteTree(File path) throws IOException {
        if (!path.exists()){
            return;
        }
        File[] files = path.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory())
                    deleteTree(f);
                else {
                    int i = 0;
                    while (f.exists() && !f.delete()) {
                        if (i == 20) {
                        	throw new IOException("Unable to delete File (after 20 tries) " + f.getAbsolutePath());
                        } else {
                            i++;
                            wait10(i, "Unable to delete File (InterruptedException) " + f.getAbsolutePath());
                        }
                    }
                }
            }
        }
        int i = 0;
        while (!path.delete()){
            if (i == 20) {
            } else {
                i++;
                wait10(i, "Unable to make delete " + path.getAbsolutePath());
            }            
        }
        path = null;
    }

    /**
     * Makes a new directory if required at the location of this file.
     * 
     * Will also make any parent directories.
     * Then checks that the directory is read and writable.
     * 
     * @param dir Directory to be created.
     * @throws IOException IO or wrapper interrupt exception.
     */
    public static void makeDirs(File dir) throws IOException{
        int i = 0;
        if (!dir.exists()){
            while (!dir.mkdirs()){
                if (i == 20) {
                    throw new IOException("Unable to make dir (after 20 tries) " + dir.getAbsolutePath());
                } else {
                    i++;
                    wait10(i, "Unable to make dir " + dir.getAbsolutePath());
                }
            }
        }
        if (!dir.exists()){
           throw new IOException("Make dir failed " + dir.getAbsolutePath());
        }
        i = 0;
        while (!dir.setReadable(true)){
            if (i == 200) {
                throw new IOException("Unable to make dir readable (after 20 tries) " + dir.getAbsolutePath());
            } else {
                i++;
                wait10(10, "Unable to make dir readable " + dir.getAbsolutePath());
            }
        }
        i=0;
        while (!dir.setWritable(true)){
            if (!dir.exists()){
               throw new IOException("Dir disappeared while making writeable " + dir.getAbsolutePath());
             }
            if (i == 20) {
                throw new IOException("Unable to make dir writeable (after 20 tries) " + dir.getAbsolutePath());
            } else {
                i++;
                wait10(i, "Unable to make dir writeable " + dir.getAbsolutePath());
            }
        }
    }
    
    /**
     * Delete given directory by name.
     * 
     * @param path
     *            String
     * @throws IOException
     */
    public static void deleteTree(String path) throws IOException {
        File f = new File(path);
        DirUtils.deleteTree(f);
        f = null;
    }

}
