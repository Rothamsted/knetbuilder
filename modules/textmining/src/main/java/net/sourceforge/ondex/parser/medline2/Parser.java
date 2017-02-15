package net.sourceforge.ondex.parser.medline2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.DataURL;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.medline2.sink.Abstract;
import net.sourceforge.ondex.parser.medline2.transformer.Transformer;
import net.sourceforge.ondex.parser.medline2.xml.XMLParser;

/**
 * @author keywan
 */
@Status(description = "Parses PubMed/Medline XML files or uses EFetch web-service to load publications into Ondex. Tested June 2011 (Keywan Hassani-Pak)", status = StatusType.STABLE)
@Authors(authors = {"Keywan Hassani-Pak"}, emails = {"keywan at users.sourceforge.net"})
@DatabaseTarget(name = "MEDLINE", description = "Biomedical Literature", version = "MEDLINE 2011", url = "http://www.ncbi.nlm.nih.gov/pubmed/")
@DataURL(name = "MEDLINE XML",
        description = "Any MEDLINE XML file. Use either PubMed to search for keywords and save results to XML or use Parser's integrated web-service functionality to fetch cited publications online.",
        urls = {"http://www.ncbi.nlm.nih.gov/pubmed?term=hassani-pak",
                "http://www.ncbi.nlm.nih.gov/pubmed?term=Arabidopsis"})
@Custodians(custodians = {"Keywan Hassani-pak"}, emails = {"keywan at users.sourceforge.net"})
public class Parser extends ONDEXParser {

	private static Parser instance;
	
	public static String EFETCH_WS = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&retmode=xml&id=";
	// changed from http to https to avoid DOCTYPE error

    public Parser() {
        instance = this;
    }

    public String getName() {
        return new String("Medline/PubMed");
    }

    public String getVersion() {
        return new String("20.06.2011");
    }

    public String getId() {
        return "medline";
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition[]{
        		 new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE, "Input file in PubMed/MEDLINE XML format.", false, true, false, false),
        		 new BooleanArgumentDefinition(ArgumentNames.IMP_CITED_PUB_ARG, ArgumentNames.IMP_CITED_PUB_DESC, false, false),
        		 new FileArgumentDefinition(ArgumentNames.PMID_FILE_ARG, ArgumentNames.PMID_FILE_ARG_DESC, false, true, false, false),
               
        };
    }

    public static void propagateEventOccurred(EventType et) {
        if (instance != null)
            instance.fireEventOccurred(et);
    }

    @Override
    public String[] requiresValidators() {
        return null;
    }

    public void start() throws InvalidPluginArgumentException {
        GeneralOutputEvent so = new GeneralOutputEvent("Starting PubMed/MEDLINE parser...", Parser.getCurrentMethodName());
        fireEventOccurred(so);
        
        XMLParser xmlParser = new XMLParser();
        Transformer writer = new Transformer(graph);
        Set<Abstract> abs = new HashSet<Abstract>();
        

        try {
            
            //Input: Directory with PubMed XML files
        	if(args.getUniqueValue(FileArgumentDefinition.INPUT_FILE) != null){
	            File file = new File((String) args
	                    .getUniqueValue(FileArgumentDefinition.INPUT_FILE));
	            
	            fireEventOccurred(new GeneralOutputEvent(
	                    "Start parsing file "+file.getAbsolutePath(), Parser.getCurrentMethodName()));

	            abs.addAll(xmlParser.parseMedlineXML(file));
	            
	            fireEventOccurred(new GeneralOutputEvent(
	                    "Number of publications found: "+abs.size(), Parser.getCurrentMethodName()));
        	}
            
    		
    		//Input: File with integer pmids
    		if(args.getUniqueValue(ArgumentNames.PMID_FILE_ARG) != null){
    			File idFile = new File((String) args.getUniqueValue(ArgumentNames.PMID_FILE_ARG));
    			HashSet<String> pmidSet = new HashSet<String>();
    			fireEventOccurred(new GeneralOutputEvent("Retrieving PMIDs from " +
    					"file: "+idFile.getAbsolutePath(), Parser.getCurrentMethodName()));
                
				BufferedReader in = new BufferedReader(new FileReader(idFile));
				String inputLine = null;
				while ((inputLine = in.readLine()) != null) {
					inputLine=inputLine.trim();
					if (!inputLine.equals("")) {
						//check if more than one value separated by ;
						String[] ids = inputLine.split(" ");
						for (String id : ids)  {
							pmidSet.add(id.trim());
						}
					}
				}
				abs.addAll(xmlParser.parsePummedID(pmidSet));
				in.close();
				
	            fireEventOccurred(new GeneralOutputEvent(
	                    "Number of publications found: "+pmidSet.size(), Parser.getCurrentMethodName()));
    			
    		}

    		//Input: Import cited publications
    		Boolean isImpOnlyCitedPub = (Boolean) args.getUniqueValue(ArgumentNames.IMP_CITED_PUB_ARG);

            if (isImpOnlyCitedPub) {
            	fireEventOccurred(new GeneralOutputEvent("Retrieving PMIDs " +
            			"cited in the Ondex graph...", Parser.getCurrentMethodName()));
                HashSet<String> pmidSet = new HashSet<String>();
                ConceptClass ccPub = graph.getMetaData().getConceptClass(MetaData.CC_PUBLICATION);
                for (ONDEXConcept c : graph.getConceptsOfConceptClass(ccPub)) {
                    for (ConceptAccession accession : c.getConceptAccessions()) {
                        if (accession.getElementOf().getId().equalsIgnoreCase(MetaData.CV_NLM)) {
                            pmidSet.add(accession.getAccession());
                        }
                    }
                }
                //System.out.println("pmidSet: " + pmidSet.iterator().next());
                abs.addAll(xmlParser.parsePummedID(pmidSet));
	            fireEventOccurred(new GeneralOutputEvent(
	                    "Number of publications found: "+pmidSet.size(), Parser.getCurrentMethodName()));
            }
            
        	fireEventOccurred(new GeneralOutputEvent("Adding " + abs.size() +
        			" unique publication concepts to Ondex graph...", Parser.getCurrentMethodName()));
            writer.writeConcepts(abs);
            
            xmlParser = null;
            writer.finalize();
            writer = null;
            
            fireEventOccurred(new GeneralOutputEvent("Finished PubMed/MEDLINE parsing" +
            		" with success!", Parser.getCurrentMethodName()));
            
        } catch (Exception e) {
        	fireEventOccurred(new GeneralOutputEvent("Error: PubMed/MEDLINE import did not" +
        			" finish! " + e.getMessage(), Parser.getCurrentMethodName()));
            e.printStackTrace();
        }
    }
    


    /**
     * Convenience method for outputting the current method name in a dynamic way
     *
     * @return the calling method name
     */
    public static String getCurrentMethodName() {
        Exception e = new Exception();
        StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
        String name = trace.getMethodName();
        String className = trace.getClassName();
        int line = trace.getLineNumber();
        return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line + "]";
    }

}
