/*
 * Created on 29.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.ondex.parser.kegg52.gene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import net.sourceforge.ondex.parser.kegg52.Parser;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

/**
 * @author Jan
 *         <p/>
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class AbreviationsParser {


    public static Map<String, String> parse(String pathToAbreviations) {
        Map<String, String> mapping = new HashMap<String, String>(100);

        Pattern colonSplit = Pattern.compile(":");

        File file = new File(pathToAbreviations);
        try {
            BufferedReader reader = null;

            if (file.exists()) {
                reader = new BufferedReader(new FileReader(file));
            } else {
                try {
                    if (!new File(Parser.pathToGenome).exists()) {
                        TarInputStream tis = new TarInputStream(new GZIPInputStream(new FileInputStream(Parser.pathToKegg + File.separator + "genes.tar.gz")));

                        TarEntry entry;
                        while ((entry = tis.getNextEntry()) != null) {
                            String name = entry.getName();
                            if (name.equalsIgnoreCase(file.getName())) {
                                reader = new BufferedReader(
                                        new InputStreamReader(tis));
                                break;
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            while (reader.ready()) {
                String line = reader.readLine();
                if (!(line.indexOf("#") > -1) && line.indexOf(":") > -1) {
                    String[] result = colonSplit.split(line);
                    File test = new File(file.getParent()
                            + System.getProperty("file.separator")
                            + result[0]);
                    if (test.canRead())
                        mapping.put(result[2], result[0]);
                }
            }
            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return mapping;
    }

}
