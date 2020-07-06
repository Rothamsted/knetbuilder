package net.sourceforge.ondex.parser.gaf.transformer;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.parser.gaf.ArgumentNames;
import net.sourceforge.ondex.parser.gaf.MetaData;
import net.sourceforge.ondex.parser.gaf.sink.AnnotationLine;

/**
 * @author keywan
 */
public class GAFTransformer {
	
	private ONDEXGraph graph;
	private ONDEXGraphMetaData md;

    private HashMap<String, Integer> parsed;
    private Map<String, String> ccMap;

    private AttributeName attEvidence;
    private AttributeName attTAXID;
    private EvidenceType etIMPD;
    private DataSource dataSourceTAIR;
    private DataSource dataSourceUNIPROTKB;
    
	private ConceptClass ccPub;
	private ConceptClass ccOntTermsParent;
	private DataSource dsAccNLM;
	
	private RelationType rt_pub_in;
	
    private final Pattern atgPattern = Pattern.compile("AT[C|M|0-9]G[0-9]+([.][0-9]+)?", Pattern.CASE_INSENSITIVE);
    private final Pattern uniprotCheck = Pattern.compile("(^[A-N,R-Z][0-9][A-Z][A-Z,0-9][A-Z,0-9][0-9]$)|(^[O,P,Q][0-9][A-Z,0-9][A-Z,0-9][A-Z,0-9][0-9]$)");
	


    public GAFTransformer(ONDEXGraph graph, ONDEXPluginArguments pa) throws InvalidPluginArgumentException {
        
    	this.graph = graph;
    	this.md = graph.getMetaData();
    	
    	ccOntTermsParent = md.getConceptClass(MetaData.CC_ONTOLOGY_TERMS);
    	ccPub = md.getConceptClass(MetaData.PUBLICATION);
        dataSourceTAIR = md.getDataSource(MetaData.TAIR);
        dataSourceUNIPROTKB = md.getDataSource(MetaData.UNIPROTKB);
        dsAccNLM = md.getDataSource(MetaData.NLM);
        etIMPD = md.getEvidenceType(MetaData.IMPD);
        attTAXID = md.getAttributeName(MetaData.taxID);
        attEvidence = md.getAttributeName(MetaData.attEVIDENCE);
        rt_pub_in = md.getRelationType(MetaData.RT_PUB_IN);
    	
        parsed = new HashMap<String, Integer>();
        ccMap = new HashMap<String, String>();
        ccMap.put("P", MetaData.BioProc); 
        ccMap.put("F", MetaData.MolFunc);
        ccMap.put("C", MetaData.CelComp);
        ccMap.put("S", MetaData.ccStructure);
        ccMap.put("A", MetaData.ccStructure);
        ccMap.put("G", MetaData.ccGrowthDev);
        ccMap.put("T", MetaData.ccTraitOnt);
        
        //if mapping file specified
		if(pa.getUniqueValue(ArgumentNames.TRANSLATION_ARG) != null){
			File file = new File((String) pa.getUniqueValue(ArgumentNames.TRANSLATION_ARG));
			try {
				BufferedReader input = new BufferedReader(new FileReader(file));
				String inputLine;
		        while ((inputLine = input.readLine()) != null) {
		        	String[] col = inputLine.split("\t");
		        	//DbObjType	OndexConceptClass	
		        	ccMap.put(col[0].trim(), col[1].trim());
		        }
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

        }
		//else use a default minimum mapping set
		else{
			ccMap.put("gene", MetaData.gene);
        	ccMap.put("rna", MetaData.rna);
        	ccMap.put("protein", MetaData.protein);
		}
        
    }

    
    public void parseGAFLine(AnnotationLine annotation) throws InvalidPluginArgumentException {
    	
        String dataSourceName = getDataSource(annotation.getDatabase(), annotation.getDBObjectID());
        String ontology = getDataSourceAcc(annotation.getAspect());
        String ontAspect = annotation.getAspect();
        String ontAcc = annotation.getGOID();
        String objType = annotation.getDBObjectType();
        String objID = annotation.getDBObjectID();
        String objSymbol = annotation.getDBObjectSymbol();
        String objName = annotation.getDBObjectName();
        String objFrom = annotation.getWithFrom();
        HashSet<String> annoReferences = annotation.getDBReferences();
        HashSet<String> objSynonyms = annotation.getDBObjectSynonyms();
        ArrayList<String> objTaxonomy = annotation.getTaxons();
        String eviCodeName = annotation.getEvidenceCode();
        String rtName = getRelationType(annotation.getAspect(), annotation.getQualifier());
        
        DataSource dsAnnotation;
		if (md.checkDataSource(dataSourceName)) 
        	dsAnnotation = md.getDataSource(dataSourceName);
        else
        	dsAnnotation = md.createDataSource(dataSourceName, dataSourceName, "");
        
        DataSource dsAccOntology;
		if(md.checkDataSource(ontology))
        	dsAccOntology = md.getDataSource(ontology);
        else
        	dsAccOntology = md.createDataSource(ontology, ontology, "");
        
        EvidenceType etCode;
		if(md.checkEvidenceType(eviCodeName))
        	etCode = md.getEvidenceType(eviCodeName);
        else
        	etCode = md.createEvidenceType(eviCodeName, eviCodeName, "");
		
    	RelationType rt;
    	if(md.checkRelationType(rtName))
    		rt = md.getRelationType(rtName);
    	else
    		rt = md.getRelationType(MetaData.rtIS_RELATED_TO);
    	
        ConceptClass ccObjType;
        if(md.checkConceptClass(ccMap.get(objType)))
        	ccObjType = md.getConceptClass(ccMap.get(objType));
        else
        	ccObjType = md.getFactory().createConceptClass(objType, objType);
        
        ConceptClass ccOntology;
        if(md.checkConceptClass(ccMap.get(ontAspect)))
        	ccOntology = md.getConceptClass(ccMap.get(ontAspect));
        else
        	ccOntology = md.getFactory().createConceptClass(ontAspect, ontAspect, ccOntTermsParent);

        
        // create ontology concept
        if (!parsed.containsKey(ontAcc)) {
        	ONDEXConcept c = graph.getFactory().createConcept(ontAcc, dsAnnotation, ccOntology, etIMPD);
            c.createConceptName(ontAcc, false);
            c.createConceptAccession(ontAcc, dsAccOntology, false);
            parsed.put(ontAcc, c.getId());
        } 
            
        // create object concept (e.g. gene, protein, rna)
    	if (!parsed.containsKey(objID)) {
    		ONDEXConcept c = graph.getFactory().createConcept(objID, dsAnnotation, ccObjType, etIMPD);
    		c.createConceptAccession(objID, dsAnnotation, false);
    		
    		if (objSymbol != null) {
        		if(c.getConceptName(objSymbol) == null){
        			c.createConceptName(objSymbol, true);
        		}
    		}
    		
        	// create more names
        	if(objName != null){
        		if(c.getConceptName(objName) == null){
        			c.createConceptName(objName, true);
        		}	
        	}
        	
        	// create name synonyms
        	if (objSynonyms != null) {
        		for (String syn : objSynonyms) {
        			syn = syn.trim();
        			if (syn.length() > 0 && !syn.equalsIgnoreCase(objID)) {

        				if (c.getConceptName(syn) == null){
        					c.createConceptName(syn, false);
        				}
        			}
        		}
        	}
        	
        	//handle taxonomy information
        	if (objTaxonomy != null){
        		String taxon = objTaxonomy.get(0).split(":")[1];
        		if (c.getAttribute(attTAXID) == null) {
        			c.createAttribute(attTAXID, taxon, false);
        		}
        	}
        	
        	//handle objFrom
        	if(objFrom != null){
        		//not used so far
        	}
    		
    		parsed.put(objID, c.getId());
    	} 
    	
    	
    	
    	// handle publications
    	ONDEXConcept pubConcept = null;
    	if (annoReferences != null){
    		
    		for (String dbRef : annoReferences) {
    			if (!dbRef.contains("PMID:")) {
    				continue;
    			}
    			// dbRef is a PubMed ID
    			if (!parsed.containsKey(dbRef)) {
    				ONDEXConcept c = graph.getFactory().createConcept(dbRef, dsAnnotation, ccPub, etIMPD);

    				if (c.getConceptName(dbRef) == null){
    					c.createConceptName(dbRef, true);
    				}	
    				
    				String acc = dbRef.split(":")[1];
    				c.createConceptAccession(acc, dsAccNLM, false);
    				
    				parsed.put(dbRef, c.getId());
    			} 
    			
    			pubConcept = graph.getConcept(parsed.get(dbRef));
    		}
    	}
    	
    	
    	ONDEXConcept dbObjectConcept = graph.getConcept(parsed.get(objID));
		//find external accessions in annotation line
    	//this check is done for each line as the same objID can have different accessions
		checkForAccessions(annotation, dbObjectConcept);

		ONDEXConcept ontologyConcept = graph.getConcept(parsed.get(ontAcc));
    	ONDEXRelation rel = graph.getRelation(dbObjectConcept, ontologyConcept, rt);
    	
    	if (rel == null) {
    		rel = graph.getFactory().createRelation(dbObjectConcept, ontologyConcept, rt, etCode);

    	}
    	
    	if(!rel.getEvidence().contains(etCode)){
    		rel.addEvidenceType(etCode);
    	}

    	if(pubConcept != null){
    		// dbObjectConcept.addTag(pubConcept);
    		// ontologyConcept.addTag(pubConcept);
    		// rel.addTag(pubConcept);
        	
        	if (graph.getRelation(dbObjectConcept, ontologyConcept, rt_pub_in) == null) {
        		graph.getFactory().createRelation(dbObjectConcept, pubConcept, rt_pub_in, etIMPD);

        	}
        	
    	}

    	if (annoReferences != null) {
    		if(rel.getAttribute(attEvidence) == null){
    			rel.createAttribute(attEvidence, annoReferences, false);
    		}else{
    			HashSet<String> val = (HashSet<String>) rel.getAttribute(attEvidence).getValue();
    			val.addAll(annoReferences);
    			rel.getAttribute(attEvidence).setValue(val);
    		}
    	}
    	
    }

    /**
     * check all elements of an annotation line for external accessions
     * Supports: TAIR, UNIPROT accessions
     * 
     * @param gaf
     * @param dbObjConcept
     */
    private void checkForAccessions(AnnotationLine gaf, ONDEXConcept dbObjConcept) {
    	
    	String symbol = gaf.getDBObjectSymbol();
    	String name = gaf.getDBObjectName();
    	HashSet<String> synonyms = gaf.getDBObjectSynonyms();
    	String objAcc = gaf.getDBObjectID();
    	String objFrom = gaf.getWithFrom();
    	    	
    	HashSet<String> queries = new HashSet<String>();
    	if(symbol != null)
    		queries.add(symbol);
    	if(name != null)
    		queries.add(name);
    	if(synonyms != null)
    		queries.addAll(synonyms);
    	if(objAcc != null)
    		queries.add(objAcc);
// this field contains accessions of other genes that were used to infer the
// annotation from and should not be used as the genes own accessions.
//    	if(objFrom != null)
//    		queries.add(objFrom);
    	
    	for(String query : queries){
	        //hack to get tair accessions from description
	        Matcher matchTAIR = atgPattern.matcher(query);
	        if (matchTAIR.find()) {
	            String acc = matchTAIR.group();
	            //make protein accessions ambiguous, gene accessions non-ambiguous for gene concepts
	            if (!(acc.contains("."))) {

	            	if (dbObjConcept.getConceptAccession(acc, dataSourceTAIR) == null){
	            		dbObjConcept.createConceptAccession(acc, dataSourceTAIR, false);
	            	}
	            }
            
	           
	        }
	        
	        Matcher matchUniProt = uniprotCheck.matcher(query);
    		if(matchUniProt.find()){
    			String acc = matchUniProt.group();
    			if(dbObjConcept.getConceptAccession(acc, dataSourceUNIPROTKB) == null)
    				dbObjConcept.createConceptAccession(acc, dataSourceUNIPROTKB, false);
    		}
    	}
    	
    }

    private String getDataSourceAcc(String ontology){
    	String accDataSource;
    	//Plant Ontology
    	if(ontology.equals("S") || ontology.equals("G") || ontology.equals("A")){
    		accDataSource = MetaData.CVPO;
    	}
    	//Trait Ontology
    	else if(ontology.equals("T")){
    		accDataSource = MetaData.CVTO;
    	}
    	//Gene Ontology
    	else{
    		accDataSource = MetaData.CVGO;
    	}
    	
    	return accDataSource;
    }

    private String getRelationType(String ontology, String qualifier) {

        String rt;

        if (ontology.equals("F"))
            if (!qualifier.contains("NOT"))
                rt = MetaData.hasFunction;
            else
            	rt = MetaData.hasFunctionNOT;
        else if (ontology.equals("P"))
            if (!qualifier.contains("NOT"))
            	rt = MetaData.hasParticipant;
            else
                rt = MetaData.hasParticipantNOT;
        else if (ontology.equals("C"))
            if (!qualifier.contains("NOT"))
                rt = MetaData.locatedIn;
            else
                rt = MetaData.locatedInNOT;
        else if (ontology.equals("S"))
        	rt = MetaData.rtExpressedIn;
        else if (ontology.equals("A"))
        	rt = MetaData.rtExpressedIn;
        else if (ontology.equals("G"))
        	rt = MetaData.rtExpressedDuring;
        else if (ontology.equals("T"))
        	rt = MetaData.rtHasPhenotype;        	
        else
            rt = MetaData.rtIS_RELATED_TO;

        return rt;
    }

    
    private String getDataSource(String dbName, String id){
        String dataSource = dbName;
    	if (dbName.equalsIgnoreCase("uniprot") ||
                dbName.equalsIgnoreCase("uniprotkb") ||
                dbName.equalsIgnoreCase("uprot") ||
                dbName.equalsIgnoreCase("uprotkb")) {
    		dataSource = MetaData.UNIPROTKB;
        } else if (dbName.equalsIgnoreCase(MetaData.UNIPROTKB)) {
            dbName = MetaData.UNIPROTKB;
        } else if (dbName.equalsIgnoreCase("REFSEQ")) { // validate, NC_NP or
            // NC_NM
            if (id.toUpperCase().startsWith("NC_")) {
            	dataSource = MetaData.ncNM;
            } else if (id.toUpperCase().startsWith("NP_")) {
            	dataSource = MetaData.ncNP;
            }
        } else if (dbName.equalsIgnoreCase(MetaData.VEGA)) {
        	dataSource = MetaData.VEGA;
        } else if (dbName.equalsIgnoreCase(MetaData.HINVDB)) {
        	dataSource = MetaData.HINVDB;
        } else if (dbName.equalsIgnoreCase("H-invDB")) {
        	dataSource = MetaData.HINVDB;
        } else if (dbName.equalsIgnoreCase("TAIR")) {
        	dataSource = MetaData.TAIR;
        } else if (dbName.equalsIgnoreCase("GRAMENE")) {
        	dataSource = MetaData.GRAMENE;
        } else if (dbName.equalsIgnoreCase("UniProtKB/Swiss-Prot")) {
        	dataSource = MetaData.UNIPROTKB;
        } else if (dbName.equalsIgnoreCase("TIGR")) {
        	dataSource = MetaData.TIGR;
        } else if (dbName.startsWith("GR_")) {
        	dataSource = MetaData.GRAMENE;
        } else if (dbName.equalsIgnoreCase(MetaData.SGN)) {
        	dataSource = MetaData.SGN;
        }
    	
    	return dataSource;

    }

}
