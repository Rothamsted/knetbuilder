/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.nclondexexpression.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.exception.type.ParsingFailedException;

/**
 *
 * @author jweile
 */
public class DefaultPathTemplates {

    private static final String MAPPING_FILE_NAME = "ondex_mapping.tsv";

    private ONDEXGraph graph;

    private Map<String,PathTemplate> templates = new HashMap<String, PathTemplate>();

    public DefaultPathTemplates(ONDEXGraph graph) throws ParsingFailedException {
        this.graph = graph;
        read();
    }

    public Set<String> getTemplateKeys() {
        return Collections.unmodifiableSet(templates.keySet());
    }

    public PathTemplate getTemplate(String key) {
        return templates.get(key);
    }

    private void read() throws ParsingFailedException{
        InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream(MAPPING_FILE_NAME);

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line; int lineNum = 0;
            while ((line = reader.readLine())!= null) {
                lineNum++;

                if (line.length() == 0) {
                    continue;
                }

                String[] cols = line.split("\t");

                if (cols.length != 2) {
                    throw new ParsingFailedException("Unreadable line in internal " +
                            "mapping file: "+lineNum);
                }

                try {
                    PathTemplate p = new PathTemplate(cols[1], graph);
                    templates.put(cols[0],p);
                } catch (MalformedPathException ex) {
                    throw new ParsingFailedException("Unreadable path descriptor " +
                            "in internal mapping file: "+lineNum+"\n"+ex.getMessage());
                }
            }
        } catch (IOException ioe) {
            throw new ParsingFailedException("Unable to read internal mapping file!");
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(DefaultPathTemplates.class.getName()).log(Level.SEVERE,
                        "unable to close file handle to interal mapping file", ex);
            }
        }

    }
}
