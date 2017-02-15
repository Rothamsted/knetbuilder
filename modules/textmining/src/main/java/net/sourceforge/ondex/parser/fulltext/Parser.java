package net.sourceforge.ondex.parser.fulltext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;

import org.apache.log4j.Level;

/**
 * beta version of a full text (corpus) parser
 * 
 * parses text files labeled with
 * [PMID]
 * [Title]
 * [Abstract]
 * [Full Text]
 * 
 * @author keywan
 *
 */
public class Parser extends ONDEXParser {
	
	private DataSource dataSourceNLM;
	private DataSource dataSourceCorpus;
	private ConceptClass ccPublication;
	private EvidenceType etIMPD;
	private AttributeName attTitle;
	private AttributeName attAbstract;
	private AttributeName attFullText;
	

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		FileArgumentDefinition inputDir = new FileArgumentDefinition(
				FileArgumentDefinition.INPUT_DIR,
				"directory with generif files", true, true, true, false);
		
		return new ArgumentDefinition<?>[] {inputDir};
	}

	@Override
	public String getId() {
		return "fulltext";
	}

	@Override
	public String getName() {
		return "Full Text";
	}

	@Override
	public String getVersion() {
		return "06_2010";
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	@Override
	public void start() throws Exception {
		
		ONDEXGraphMetaData md = graph.getMetaData();
		ccPublication = md.getConceptClass("Publication");
		dataSourceNLM = md.getDataSource("NLM");
		dataSourceCorpus = md.getDataSource("Corpus");
		etIMPD = md.getEvidenceType("IMPD");
		attTitle = md.getAttributeName("AbstractHeader");
		attAbstract = md.getAttributeName("Abstract");
		attFullText = md.getAttributeName("FullText");
		
        GeneralOutputEvent so = new GeneralOutputEvent("Starting FullText parsing...", "Fulltext - start()");
        so.setLog4jLevel(Level.INFO);
        fireEventOccurred(so);

        // get importdata dir
        File dir = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_DIR));
        
        for(File file : dir.listFiles()){

        	createOndexConcept(file);
        }
        
	}
	
	private ONDEXConcept createOndexConcept(File file) throws IOException {
		
		System.out.println(file.getAbsolutePath());
		
		ONDEXConcept concept = graph.getFactory().createConcept(file.getName(), "", "", dataSourceCorpus, ccPublication, etIMPD);
		
		BufferedReader in = new BufferedReader(new FileReader(file));
		
		String pubMedId = null;
		StringBuffer sbTitle = new StringBuffer();
		StringBuffer sbAbstract = new StringBuffer();
		StringBuffer sbFullText = new StringBuffer();
		
		boolean isPMID = false;
		boolean isTitle = false;
		boolean isAbstract = false;
		boolean isFullText = false;
    	
		String inputline = null;

		while ((inputline = in.readLine())!= null) {
			
			if(inputline.equals(""))
				continue;

			if(inputline.startsWith("[PMID]")){
				System.out.print("PMID: ");
				isPMID = true;
			}
			else if (inputline.startsWith("[Title]")) {
				System.out.print("Title: ");
				isTitle = true;
				isPMID = false;
			}
			else if(inputline.startsWith("[Abstract]")){
				isAbstract = true;
				isTitle = false;
			}
			else if(inputline.startsWith("[Full Text]")){
				isFullText = true;
				isAbstract = false;
			}else{
				
				if(isPMID){
					System.out.println(inputline);
					pubMedId = inputline;
					
				}else if(isTitle){
					System.out.println(inputline);
					sbTitle.append(inputline);
					
				}else if(isAbstract){
					sbAbstract.append(inputline);
					
				}else if(isFullText){
					if(!inputline.endsWith(" "))
						inputline = inputline+" ";
					sbFullText.append(inputline);
				}
			}
		}
		
		if(pubMedId != null)
			concept.createConceptAccession(pubMedId, dataSourceNLM, false);
		
		concept.createAttribute(attTitle, sbTitle.toString(), true);
		concept.createAttribute(attAbstract, sbAbstract.toString(), true);
		concept.createAttribute(attFullText, sbFullText.toString(), true);
		
		return concept;
	}

}
