package net.sourceforge.ondex.parser.fasta;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * FASTA FILE parser
 *
 * @author berendh
 */
public class ReadFastaFiles {


    public static void parseFastaFile(ONDEXGraph graph, String file, WriteFastaFile write) throws Exception {
        parseFastaFile(graph, new File(file), write);
    }

    /**
     * Reads a fasta File into separate fasta sequence blocks
     *
     * @param graph
     * @param file
     * @throws Exception
     */
    public static void parseFastaFile(ONDEXGraph graph, File file, WriteFastaFile write) throws Exception {
        BufferedReader input = null;

        if (!file.isFile() || !file.exists()) {
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new DataFileMissingEvent("FASTA file missing :" + file, Parser.getCurrentMethodName()));
            return;
        } else if (!file.canRead()) {
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new DataFileMissingEvent("FASTA file has no read permissions :" + file, Parser.getCurrentMethodName()));
            return;
        }

        if (file.getName().endsWith(".gz")) {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(
                    file));
            input = new BufferedReader(new InputStreamReader(gzip));
        } else {
            input = new BufferedReader(new FileReader(file));
        }

        String inputLine = input.readLine();
        FastaBlock currentFastaSeq = null;

        while (inputLine != null) {
            if (inputLine.startsWith(">")) {
                if (currentFastaSeq != null) {
                    try {
                        write.parseFastaBlock(graph, currentFastaSeq);
                    } catch (WriteFastaFile.FormatFileException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }

                currentFastaSeq = new FastaBlock();
                currentFastaSeq.setHeader(inputLine.substring(1)); //skip the start char.
            } else if (inputLine.length() >= 1) {
                currentFastaSeq.addSequence(inputLine);
            }

            inputLine = input.readLine();
        }

        if (currentFastaSeq != null) {
            try {
                write.parseFastaBlock(graph, currentFastaSeq);
            } catch (WriteFastaFile.FormatFileException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}