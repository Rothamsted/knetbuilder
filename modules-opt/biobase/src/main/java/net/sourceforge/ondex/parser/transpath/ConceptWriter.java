package net.sourceforge.ondex.parser.transpath;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;

/**
 * Provides the interface to the AbstractONDEXGraph
 * for writing concepts and relations.
 * 
 * @author taubertj
 *
 */
class ConceptWriter {
	
	// tp accession prepender
	private static String TPACC = "TP_"; 
	
	// current AbstractONDEXGraph
	private ONDEXGraph aog;
	
	// ONDEX metadata
	private DataSource dataSourceTP;
	private DataSource dataSourcePMID;
	private EvidenceType evi;
	private ConceptClass ccGene;
	private ConceptClass ccReact;
	private ConceptClass ccPublication;
	private AttributeName taxAttr;
	private AttributeName subusAttr;
	private AttributeName aaAttr;
	private AttributeName naAttr;
	
	private RelationType en_by;
	private RelationType ca_by;
	private RelationType in_by;
	private RelationType si_to;
	private RelationType cs_by;
	private RelationType pd_by;
	private RelationType pub_in;
	
	// mapping gene id to ONDEXConcept
	private HashMap<String,ONDEXConcept> genesWritten = new HashMap<String,ONDEXConcept>();
	
	// mapping molecule id to ONDEXConcept
	private HashMap<String,ONDEXConcept> moleculesWritten = new HashMap<String,ONDEXConcept>();
	
	// mapping publications to ONDEXConcept
	private HashMap<String,ONDEXConcept> publicationsWritten = new HashMap<String,ONDEXConcept>();
	
	/**
	 * Constructor for a given AbstractONDEXGraph
	 * 
	 * @param aog - AbstractONDEXGraph
	 */
	protected ConceptWriter(ONDEXGraph aog) {
		this.aog = aog;
		
		String methodName = "ConceptWriter(AbstractONDEXGraph aog)";
		
		// get all the ONDEX metadata straight
		
		// CVs
		dataSourceTP = aog.getMetaData().getDataSource("TP");
		if (dataSourceTP == null) Parser.propagateEventOccurred(new DataSourceMissingEvent("TransPath DataSource is null", methodName)); 
		
		dataSourcePMID = aog.getMetaData().getDataSource("NLM");
		if (dataSourcePMID == null) Parser.propagateEventOccurred(new DataSourceMissingEvent("TransPath PMID is null", methodName)); 
		
		// EvidenceTypes
		evi = aog.getMetaData().getEvidenceType("IMPD");
		if (evi == null) Parser.propagateEventOccurred(new EvidenceTypeMissingEvent("TransPath evi is null", methodName)); 
		
		// ConceptClasses
		ccGene = aog.getMetaData().getConceptClass("Gene");
		if (ccGene == null) Parser.propagateEventOccurred(new ConceptClassMissingEvent("TransPath ccgene is null", methodName)); 
		
		ccPublication = aog.getMetaData().getConceptClass("Publication");
		if (ccPublication == null) Parser.propagateEventOccurred(new ConceptClassMissingEvent("TransPath ccpublication is null", methodName)); 
		
		ccReact = aog.getMetaData().getConceptClass("Reaction");
		if (ccReact == null) Parser.propagateEventOccurred(new ConceptClassMissingEvent("ConceptClass ccreact is null", methodName)); 
		
		// AttributeNames
		taxAttr = aog.getMetaData().getAttributeName("TAXID");
		if (taxAttr == null) Parser.propagateEventOccurred(new AttributeNameMissingEvent("TransPath taxAttr is null", methodName)); 
		
		subusAttr = aog.getMetaData().getAttributeName("SUBUS");
		if (subusAttr == null) Parser.propagateEventOccurred(new AttributeNameMissingEvent("TransPath subusAttr is null", methodName)); 
		
		aaAttr = aog.getMetaData().getAttributeName("AA");
		if (aaAttr == null) Parser.propagateEventOccurred(new AttributeNameMissingEvent("TransPath aaAttr is null", methodName)); 
		
		naAttr = aog.getMetaData().getAttributeName("NA");
		if (naAttr == null) Parser.propagateEventOccurred(new AttributeNameMissingEvent("TransPath naAttr is null", methodName)); 
		
		
		// RelationTypes		
		en_by = aog.getMetaData().getRelationType("en_by");
		if (en_by == null) en_by = getRelationType("en_by");
		
		ca_by = aog.getMetaData().getRelationType("ca_by");
		if (ca_by == null) ca_by = getRelationType("ca_by");
		
		in_by = aog.getMetaData().getRelationType("in_by");
		if (in_by == null) in_by = getRelationType("in_by");
		
		si_to = aog.getMetaData().getRelationType("situated_to");
		if (si_to == null) si_to = getRelationType("situated_to");
		
		cs_by = aog.getMetaData().getRelationType("cs_by");
		if (cs_by == null) cs_by = getRelationType("cs_by");
		
		pd_by = aog.getMetaData().getRelationType("pd_by");
		if (pd_by == null) pd_by = getRelationType("pd_by");
		
		pub_in = aog.getMetaData().getRelationType("pub_in");
		if (pub_in == null) pub_in = getRelationType("pub_in");
		
		if (en_by == null) Parser.propagateEventOccurred(new RelationTypeMissingEvent("RelationType en_by is null", methodName)); 
		if (ca_by == null) Parser.propagateEventOccurred(new RelationTypeMissingEvent("RelationType ca_by is null", methodName)); 
		if (in_by == null) Parser.propagateEventOccurred(new RelationTypeMissingEvent("RelationType in_by is null", methodName)); 
		if (si_to == null) Parser.propagateEventOccurred(new RelationTypeMissingEvent("RelationType si_to is null", methodName)); 
		if (cs_by == null) Parser.propagateEventOccurred(new RelationTypeMissingEvent("RelationType cs_by is null", methodName)); 
		if (pd_by == null) Parser.propagateEventOccurred(new RelationTypeMissingEvent("RelationType pd_by is null", methodName)); 
	}
	
	/**
	 * Create a RelationType, if missing in metadata.
	 * 
	 * @param name - Name of RelationType to be contained
	 * @return RelationType
	 */
	private RelationType getRelationType(String name) {
		RelationType rt = aog.getMetaData().getRelationType(name);
		if (rt != null) {
			return aog.getMetaData().getFactory().createRelationType(rt.getId(),rt);
		} else {
			Parser.propagateEventOccurred(
					new RelationTypeMissingEvent("Missing RelationType: "+name, "getRelationType(String name)"));
		}
		return null;
	}
	
	private static final Pattern notDNAChecker = Pattern.compile("[^A|T|G|C|U]");
	
	/**
	 * Write a given Gene to ONDEX.
	 * 
	 * @param g - Gene
	 */
	protected void createGene(Gene g) {
		
		String methodName = "createGene(Gene g)";
		
		// create concept for gene
		ONDEXConcept c = aog.getFactory().createConcept(TPACC+g.getAccession(),dataSourceTP,ccGene,evi);
		genesWritten.put(g.getAccession(),c);
				
		// create preffered name
		if (g.getName() != null) {
			c.createConceptName(g.getName(),true);
		}
		
		// add more concept names
		Iterator<String> itS = g.getSynonyms();
		while (itS.hasNext()) {
			String synonym = itS.next();
			if (!synonym.equalsIgnoreCase(g.getName())) {
				c.createConceptName(synonym,false);
			}
		}
		
		// set taxid as Attribute
		if (g.getSpecies() != null && g.getSpecies().length() > 0) {
			c.createAttribute(taxAttr,g.getSpecies(),true);
		}
		
		// add concept accession for TP
		if (g.getAccession() != null) {
			c.createConceptAccession(g.getAccession(),dataSourceTP, false);
		}
		
		// add more TP concept accessions
		Iterator<String> itAA = g.getAccessionAlternatives();
		while (itAA.hasNext()) {
			c.createConceptAccession(itAA.next(),dataSourceTP, false);
		}
		
		// add accessions to other CVs
		Iterator<DBlink> itDBL = g.getDatabaseLinks();
		while (itDBL.hasNext()) {
			DBlink link = itDBL.next();
			if (link.getAcc() != null) {
				DataSource dataSource = aog.getMetaData().getDataSource(link.getCv());
				if (dataSource == null) {
					Parser.propagateEventOccurred(new DataSourceMissingEvent("DataSource "+link.getCv()+" not detected", methodName));
				} else {
					c.createConceptAccession(link.getAcc(),dataSource, false);
				}
			}
		}
		
		// add associated publications
		Iterator<Publication> it = g.getPublications();
		while (it.hasNext()) {
			Publication pub = it.next();
			
			if (pub.getPmid() != null 
					&& pub.getTitle() != null 
					&& pub.getAuthors() != null 
					&& pub.getSource() != null) {
					
				ONDEXConcept pubC = publicationsWritten.get(pub.getPmid());
				
				if (pubC == null) {
					pubC = aog.getFactory().createConcept(TPACC+pub.getPmid(),
							pub.getAuthors()+" "+pub.getSource(),dataSourceTP,ccPublication,evi);
					pubC.createConceptAccession(pub.getPmid(), dataSourcePMID, false);
					pubC.createConceptName(pub.getTitle(), true);
					publicationsWritten.put(pub.getPmid(),pubC);
				} 
				aog.getFactory().createRelation(c, pubC, pub_in, evi);
			
			} else {
				Parser.propagateEventOccurred(new DataFileErrorEvent(
						"Something missing in Publication: "
						+pub.getPmid()+" | source: "
						+pub.getSource()+" | title: "
						+pub.getTitle()+" | authors: "
						+pub.getAuthors(), methodName));
			}
		}
	}
	
	/**
	 * Write a given Molecule to ONDEX.
	 * 
	 * @param m - Molecule
	 */
	protected void createMolecule(Molecule m) {
		
		String methodName = "createMolecule(Molecule m)";
		
		// get ConceptClass for current type
		ConceptClass cc = aog.getMetaData().getConceptClass(m.getType());
		
		String desc = "";
		if (m.getDescription() != null)
			desc = m.getDescription();
		
		// create concept for Molecule
		ONDEXConcept c = aog.getFactory().createConcept(TPACC+m.getAccession(),desc,"",dataSourceTP,cc,evi);
		moleculesWritten.put(m.getAccession(),c);
		
		// create prefferred name
		if (m.getName() != null) {
			c.createConceptName(m.getName(),true);
		}
		
		// add more concept names
		Iterator<String> itSyn = m.getSynonyms();
		while (itSyn.hasNext()) {
			String synonym = itSyn.next();
			if (!synonym.equalsIgnoreCase(m.getName())) {
				c.createConceptName(synonym,false);
			}
		}
		
		// set taxid
		if (m.getSpecies() != null && m.getSpecies().length() > 0) {
			c.createAttribute(taxAttr,m.getSpecies(),true);
		}
		
		// add TP accessions and alternatives
		if (m.getAccession() != null) {
			c.createConceptAccession(m.getAccession(),dataSourceTP, false);
		}
		
		// add more TP concept accessions
		Iterator<String> itAA = m.getAccessionAlternatives();
		while (itAA.hasNext()) {
			c.createConceptAccession(itAA.next(),dataSourceTP, false); 
		}
		
		// add accessions to other CVs
		Iterator<DBlink> itDBL = m.getDatabaseLinks();
		while (itDBL.hasNext()) {
			DBlink link = itDBL.next();
			if (link.getAcc() != null) {
				DataSource dataSource = aog.getMetaData().getDataSource(link.getCv());
				if (dataSource == null) {
					Parser.propagateEventOccurred(new DataSourceMissingEvent("DataSource "+link.getCv()+" not detected", methodName));
				} else {
					c.createConceptAccession(link.getAcc(),dataSource, false);
				}
			}
		}
		
		// add associated publications
		Iterator<Publication> it = m.getPublications();
		while (it.hasNext()) {
			Publication pub = it.next();
			
			if (pub.getPmid() != null 
					&& pub.getTitle() != null 
					&& pub.getAuthors() != null 
					&& pub.getSource() != null) {
					
				ONDEXConcept pubC = publicationsWritten.get(pub.getPmid());
				
				if (pubC == null) {
					pubC = aog.getFactory().createConcept(TPACC+pub.getPmid(),
							pub.getAuthors()+" "+pub.getSource(),dataSourceTP,ccPublication,evi);
					pubC.createConceptAccession(pub.getPmid(), dataSourcePMID, false);
					pubC.createConceptName(pub.getTitle(), true);
					publicationsWritten.put(pub.getPmid(),pubC);
				} 
				aog.getFactory().createRelation(c, pubC, pub_in, evi);
			
			} else {
				Parser.propagateEventOccurred(new DataFileErrorEvent(
						"Something missing in Publication: "
						+pub.getPmid()+" | source: "
						+pub.getSource()+" | title: "
						+pub.getTitle()+" | authors: "
						+pub.getAuthors(), methodName));
			}
		}
		
		// create subunits as Attribute		
		StringBuilder subunits = new StringBuilder();
		Iterator<String> itSubUn = m.getSubunits();
		while(itSubUn.hasNext()) { 
			subunits.append(itSubUn.next()+",");
		}
		if (subunits.length() > 0) {
			subunits.deleteCharAt(subunits.length()-1); // delete final comma
			c.createAttribute(subusAttr,TPACC + subunits.toString(),true);
		}
		subunits = null;
		
		// add sequence
		if (m.getSequence() != null) {
			if (notDNAChecker.matcher(m.getSequence()).find()) {
				c.createAttribute(aaAttr,m.getSequence(),false);
			} else {
				c.createAttribute(naAttr,m.getSequence(),false);
			}
		}
		
		// create relation to encoding gene
		if (m.getEncodingGene() != null) {
			aog.getFactory().createRelation(c,genesWritten.get(m.getEncodingGene()),en_by,evi);
		}
	}
	
	protected void createReaction(Reaction r) {
		
		String methodName = "createReaction(Reaction r)";
		
		String desc = "";
		if (r.getDescription() != null)
			desc = r.getDescription();
		
		// create concept for reaction
		ONDEXConcept c = aog.getFactory().createConcept(TPACC+r.getAccession(),desc,"",dataSourceTP,ccReact,evi);
		
		// create prefferred name
		if (r.getName() != null) {
			c.createConceptName(r.getName(),true);
		}
		
		// add TP accessions and alternatives
		if (r.getAccession() != null) {
			c.createConceptAccession(r.getAccession(),dataSourceTP, false);
		}
		
		// add more TP concept accessions
		Iterator<String> itAA = r.getAccessionAlternatives();
		while (itAA.hasNext()) {
			c.createConceptAccession(itAA.next(),dataSourceTP, false); 
		}
		
		// add accessions to other CVs
		Iterator<DBlink> itDBL = r.getDatabaseLinks();
		while (itDBL.hasNext()) {
			DBlink link = itDBL.next();
			if (link.getAcc() != null) {
				DataSource dataSource = aog.getMetaData().getDataSource(link.getCv());
				if (dataSource == null) {
					Parser.propagateEventOccurred(new DataSourceMissingEvent("DataSource "+link.getCv()+" not detected", methodName));
				} else {
					c.createConceptAccession(link.getAcc(),dataSource, false);
				}
			}
		}
		
		// add associated publications
		Iterator<Publication> it = r.getPublications();
		while (it.hasNext()) {
			Publication pub = it.next();
			
			if (pub.getPmid() != null 
					&& pub.getTitle() != null 
					&& pub.getAuthors() != null 
					&& pub.getSource() != null) {
					
				ONDEXConcept pubC = publicationsWritten.get(pub.getPmid());
				
				if (pubC == null) {
					pubC = aog.getFactory().createConcept(TPACC+pub.getPmid(),
							pub.getAuthors()+" "+pub.getSource(),dataSourceTP,ccPublication,evi);
					pubC.createConceptAccession(pub.getPmid(), dataSourcePMID, false);
					pubC.createConceptName(pub.getTitle(), true);
					publicationsWritten.put(pub.getPmid(),pubC);
				} 
				aog.getFactory().createRelation(c, pubC, pub_in, evi);
			
			} else {
				Parser.propagateEventOccurred(new DataFileErrorEvent(
						"Something missing in Publication: "
						+pub.getPmid()+" | source: "
						+pub.getSource()+" | title: "
						+pub.getTitle()+" | authors: "
						+pub.getAuthors(),
						methodName));
			}
		}
		
		// definition of a reaction is {substrates,products}
		Iterator<String> itInMol = r.getInMolecules();
		while (itInMol.hasNext()) {
			String in = itInMol.next();
			if (in.startsWith("G")) {
				aog.getFactory().createRelation(genesWritten.get(in),c,si_to,evi);
			} else {
				aog.getFactory().createRelation(moleculesWritten.get(in),c,cs_by,evi);
			}
		}
			
		Iterator<String> outInMol = r.getOutMolecules();
		while (outInMol.hasNext()) {
			String out = outInMol.next();
			if (out.startsWith("G")) {
				aog.getFactory().createRelation(c,genesWritten.get(out),si_to,evi);
			} else {
				aog.getFactory().createRelation(moleculesWritten.get(out),c,pd_by,evi);
			}	
		}
				
		Iterator<String> itCat = r.getCatalysationMolecules();
		while (itCat.hasNext()) {
			String cat = itCat.next();
			aog.getFactory().createRelation(c,moleculesWritten.get(cat),ca_by,evi);	
		}
		
		Iterator<String> itInhibMol = r.getInhibitionMolecules();
		while (itInhibMol.hasNext()) {
			String inh = itInhibMol.next();
			aog.getFactory().createRelation(c,moleculesWritten.get(inh),in_by,evi);
		}
	}
}
