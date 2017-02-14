package net.sourceforge.ondex.parser.gramene.proteins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.parser.gramene.Parser;

/**
 * DEFINITION FOR gene_product_seq
 * 0 `gene_product_seq_id` int(11) NOT NULL default '0',
 * 1 `gene_product_id` int(11) NOT NULL default '0',
 * 2 `seq_id` int(11) NOT NULL default '0',
 * 3 `is_primary_seq` int(11) NOT NULL default '0',
 * 
 * DEFINITION FOR seq
 * 0 `id` int(11) NOT NULL default '0',
 * 1 `display_id` varchar(64) NOT NULL default '',
 * 2 `description` varchar(255) default NULL,
 * 3 `seq` text NOT NULL,
 * 4 `seq_len` int(11) default NULL,
 * 5 `md5checksum` varchar(32) default NULL,
 * 6 `moltype` varchar(25) default NULL,
 * 7 `timestamp` int(11) default NULL,
 * 
 * Appends sequence data from gene_product_seq and seq tables to existing protein objects
 * 
 * @author hindlem
 *
 */
public class ProteinSequenceParser {

	private HashMap<Integer, Integer> proteinIDToCID;
	private ONDEXGraph og;

/**
 * 
 * @param proteinIDToCID table of internal gramene ids to concept id names
 * @param s current session
 * @param og ondex graph
 */
	public ProteinSequenceParser(HashMap<Integer,Integer> proteinIDToCID, ONDEXGraph og) {
		this.proteinIDToCID = proteinIDToCID;	
		this.og = og;
	}
	
	/**
	 * 
	 * @param geneProductSeqFileName gene_product_seq.txt file name
	 * @param seqFileName seq.txt file name
	 */
	public void parseSequences(String geneProductSeqFileName, String seqFileName) {
		
		AttributeName aaAttr = og.getMetaData().getAttributeName("AA");
		if (aaAttr == null) Parser.propagateEventOccurred(new AttributeNameMissingEvent("TransFac aaAttr is null", "")); 

		
		HashMap<Integer,Integer> seqIdToProteinId = new HashMap<Integer,Integer>();
		Pattern tabPattern = Pattern.compile("\t");
		
		try {
			BufferedReader input = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(geneProductSeqFileName),"UTF8"));

			while (input.ready()) {
				String inputLine = input.readLine();

				String[] columns = tabPattern.split(inputLine);
				if (columns.length < 3) { 
					continue;
				}

				int proteinId = Integer.parseInt(columns[1]);
				int seqId = Integer.parseInt(columns[2]);
				Integer primarySeq = 1;
				if (columns.length >= 4) {
					primarySeq = Integer.parseInt(columns[3]);
				}
				
				if (primarySeq == 1) {
					seqIdToProteinId.put(seqId, proteinId);
				}
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(seqIdToProteinId.size()+" proteins have sequences");
		int i = 0;
		
		try {
			BufferedReader input = new BufferedReader(new FileReader(seqFileName));
			
			while (input.ready()) {
				String inputLine = input.readLine().trim(); //read ahead

				//get lines untill we have a whole line
				String[] columns = tabPattern.split(inputLine);
				while (columns.length < 8) { 
					inputLine = inputLine + input.readLine().trim(); //append next line
					columns = tabPattern.split(inputLine);
				}

				int seqId = Integer.parseInt(columns[0].trim());
				String currentSequence = columns[3].trim();
				
				if (currentSequence.length() == 0) {
					System.err.println("NO SEQ detected in line "+inputLine);
					continue;
				}
				

				Integer proteinId = seqIdToProteinId.get(seqId);
				if (proteinId != null) {
					Integer cid = proteinIDToCID.get(proteinId);
					ONDEXConcept protein = og.getConcept(cid);
					protein.createAttribute(aaAttr, currentSequence, false);
					i++;
				} else {
					System.err.println("protein is null "+seqId);
				}

			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.err.println("Sequences added to proteins :"+i);
	}
	
	public static void main(String[] sedw) {
		String dir = "D:/workspace/backend/data/importdata/gramene2/protein";
		new ProteinSequenceParser(null, null)
		.parseSequences(dir+File.separator+"gene_product_seq.txt",
				dir+File.separator+"seq.txt");
	}
}
