package net.sourceforge.ondex.tools.oldfastafunctions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;

/**
 * FASTA FILE parser
 * 
 * @author berendh
 * 
 */
@Deprecated
public class ReadFastaFiles {

	/**
	 * Reads a fasta File into separate fasta sequence blocks
	 * 
	 * @param graph
	 * @param fileName
	 * @throws Exception
	 */
	public static void parseFastaFile(ONDEXGraph graph, String fileName, WriteFastaFile write) throws Exception {
		BufferedReader input = null;

		File file = new File(fileName);

		if (!file.isFile() || !file.exists()) {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new DataFileMissingEvent("FASTA file missing :"+file, "parseFastaFile(ONDEXGraph graph, String fileName, WriteFastaFile write)"));
			return;
		} else if (!file.canRead()) {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new DataFileMissingEvent("FASTA file has no read permissions :"+file, "parseFastaFile(ONDEXGraph graph, String fileName, WriteFastaFile write)"));
			return;
		}

		if (fileName.endsWith(".gz")) {
			GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(
					fileName));
			input = new BufferedReader(new InputStreamReader(gzip));
		} else {
			input = new BufferedReader(new FileReader(fileName));
		}

		String inputLine = input.readLine();
		FastaBlock currentFastaSeq = null;

		while (inputLine != null) {
			if (inputLine.charAt(0) == '>') {
				if(currentFastaSeq != null){
					write.parseFastaBlock(graph,currentFastaSeq);
				}

				currentFastaSeq = new FastaBlock();
				currentFastaSeq.setHeader(inputLine.substring(1)); //skip the start char.
			} else if (inputLine.length() >= 1)	{
				currentFastaSeq.addSequence(inputLine);
			}

			inputLine = input.readLine();
		}

		if(currentFastaSeq != null) {
			write.parseFastaBlock(graph,currentFastaSeq);
		}
	}
}