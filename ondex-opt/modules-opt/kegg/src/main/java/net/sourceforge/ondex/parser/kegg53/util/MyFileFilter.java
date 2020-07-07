/*
 * Created on 20-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg53.util;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author taubertj
 */
public class MyFileFilter implements FileFilter {

    private Pattern p;

    public MyFileFilter(String ext) {
        p = Pattern.compile(".+\\." + ext + "$");
    }

    public boolean accept(File pathname) {
        if (pathname.isDirectory() && pathname.canRead()) {
            return true;
        }
        if (pathname.isFile() && pathname.canRead()) {
            Matcher m = p.matcher(pathname.getName());
            return m.matches();
        }
        return false;
    }
}