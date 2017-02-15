package net.sourceforge.ondex.tools.tab.exporter;

import net.sourceforge.ondex.algorithm.pathmodel.Path;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.tools.tab.exporter.extractors.AttributeExtractor;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * A class for printing Ondex Routes to a file in tab deliminated form
 *
 * @author hindlem
 */
public class OndexPathPrinter {

    private AttributeExtractorModel attributes;
    private BufferedWriter bw;
    private ONDEXGraph graph;

    /**
     * @param attributes          the extractors to use at each depth to write data
     * @param file                the file to write to
     * @param depthToWriteHeaders the number of depths to write header information for (I the max length path)
     * @throws IOException file can not be opened
     */
    public OndexPathPrinter(AttributeExtractorModel attributes,
                            File file,
                            int depthToWriteHeaders,
                            ONDEXGraph graph, boolean exportToGzip) throws IOException {
        this.attributes = attributes;
        this.graph = graph;

        String fileName = file.getAbsolutePath();

        if (exportToGzip) {
            if (!fileName.endsWith(".gz")) {
                file = new File(fileName + ".gz");
            }
            bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file), 512 * 2)));
        } else {
            bw = new BufferedWriter(new FileWriter(file));
        }

        for (Integer i = 0; i < depthToWriteHeaders; i++) {
            for (String header : attributes.getHeader(i, depthToWriteHeaders)) {
                bw.append(header);
                bw.append('\t');
            }
        }

        bw.newLine();
        bw.flush();
    }

    /**
     * Prints the given route through the graph to the open file
     *
     * @param path the path throught the graph
     * @throws NullValueException
     * @throws AccessDeniedException
     * @throws IOException
     */
    public void printPath(Path path) throws NullValueException, AccessDeniedException, IOException {

        int level = 0;
        for (ONDEXEntity entity : path) {
            List<AttributeExtractor> lae = attributes.getAttributes(level, path);
            if (lae != null && lae.size() > 0) {
                for (AttributeExtractor ae : lae) {
                    String value = null;
                    try {
                        value = ae.getValue(entity);
                    } catch (InvalidOndexEntityException e) {
                        e.printStackTrace();
                    }
                    if (value == null) value = ""; //never insert "null"
                    bw.append(value);
                    bw.append('\t');
                }
            }
            level++;
        }
        bw.newLine();
    }

    public void close() throws IOException {
        bw.flush();
        bw.close();
    }
}
