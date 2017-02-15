package net.sourceforge.ondex.parser.biocyc.handler;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.parser.biocyc.MetaData;
import net.sourceforge.ondex.parser.biocyc.Parser;

import org.biopax.paxtools.model.level2.Level2Element;
import org.biopax.paxtools.model.level2.XReferrable;
import org.biopax.paxtools.model.level2.complex;
import org.biopax.paxtools.model.level2.entity;
import org.biopax.paxtools.model.level2.publicationXref;
import org.biopax.paxtools.model.level2.relationshipXref;
import org.biopax.paxtools.model.level2.sequenceEntity;
import org.biopax.paxtools.model.level2.unificationXref;
import org.biopax.paxtools.model.level2.xref;

/**
 * Implements shared methods for manipulation of concepts and relations.
 * 
 * @author taubertj
 * 
 */
public abstract class DefaultHandler implements MetaData {

	private static final boolean DEBUG = false;

	/*
	 * mapping from xref data source to ONDEX DataSource
	 */
	public static final Map<String, String> cvMap = new Hashtable<String, String>();

	/*
	 * using RDF id to identify concepts previously created
	 */
	protected static final Map<String, ONDEXConcept> rdf2Concept = new Hashtable<String, ONDEXConcept>();

	/*
	 * reference back to ONDEXGraph
	 */
	protected ONDEXGraph graph;

	/**
	 * Adds preferred name and list of synonyms to the concept.
	 * 
	 * @param c
	 *            ONDEXConcept to modify
	 * @param e
	 *            BioPAX entity
	 */
	public void addConceptNames(ONDEXConcept c, entity e) {

		// add preferred concept name
		String name = e.getNAME();
		if (name != null && name.length() > 0) {
			// split for possible synonyms
			String[] split = null;
			if (name.contains("||"))
				split = name.split("\\|\\|");
			else if (name.contains("//"))
				split = name.split("\\/\\/");
			if (split != null) {
				// add first name as preferred
				int i = 0;
				for (String s : split) {
					if (s.trim().length() > 0) {
						c.createConceptName(s, i == 0);
						i++;
					}
				}
			} else {
				// just one preferred name
				c.createConceptName(name, true);
			}
		}

		// add list of synonyms
		for (String synonym : e.getSYNONYMS()) {
			// prevent concept name duplication
			if (synonym.trim().length() > 0 && !synonym.equals(name))
				c.createConceptName(synonym.trim(), false);
		}
	}

	/**
	 * Adds a description as the concatenation of the comments to a concept.
	 * 
	 * @param c
	 *            ONDEXConcept to modify
	 * @param e
	 *            BioPAX Level2Element
	 */
	public void addDescription(ONDEXConcept c, Level2Element e) {
		// construct description as concatenation
		StringBuffer comment = new StringBuffer();
		for (String part : e.getCOMMENT()) {
			comment.append(part);
			comment.append("\n");
		}
		c.setDescription(comment.toString());
	}

	/**
	 * Adds a list of concept accessions to a concept.
	 * 
	 * @param c
	 *            ONDEXConcept to modify
	 * @param x
	 *            BioPAX XReferrable
	 */
	public void addConceptAccessions(ONDEXConcept c, XReferrable x)
			throws Exception {
		// turn xrefs into accessions
		for (xref xref : x.getXREF()) {

			// unificationXref are always non-ambiguous
			if (xref instanceof unificationXref) {
				addUnificationXref(c, (unificationXref) xref);
			}

			// publicationXref are added as publication concepts
			else if (xref instanceof publicationXref) {
				addPublicationXref(c, (publicationXref) xref);
			}

			// some other ambiguous relation ship
			else if (xref instanceof relationshipXref) {
				addRelationshipXref(c, (relationshipXref) xref);
			}

			// report error
			else
				System.err.println("Unknown type of xref: " + xref.getRDFId());
		}
	}

	/**
	 * Add a possible TAXID to a concept.
	 * 
	 * @param c
	 *            ONDEXConcept to modify
	 * @param s
	 *            BioPAX sequenceEntity
	 * @throws Exception
	 */
	public void addOrganism(ONDEXConcept c, sequenceEntity s) throws Exception {
		AttributeName an = graph.getMetaData().getAttributeName(anTAXID);
		if (an == null)
			throw new AttributeNameMissingException(anTAXID + " is missing.");

		// organism is known
		if (s.getORGANISM() != null) {
			unificationXref xref = s.getORGANISM().getTAXON_XREF();
			if (xref != null) {
				c.createAttribute(an, xref.getID(), false);
			} else if (Parser.taxidToUse != null
					&& Parser.taxidToUse.length() > 0) {
				// fall back to default TAXID
				c.createAttribute(an, Parser.taxidToUse, false);
			}
		}
	}

	/**
	 * Add a possible TAXID to a concept.
	 * 
	 * @param c
	 *            ONDEXConcept to modify
	 * @param cmplx
	 *            BioPAX complex
	 * @throws Exception
	 */
	public void addOrganism(ONDEXConcept c, complex cmplx) throws Exception {
		AttributeName an = graph.getMetaData().getAttributeName(anTAXID);
		if (an == null)
			throw new AttributeNameMissingException(anTAXID + " is missing.");

		// organism is known
		if (cmplx.getORGANISM() != null) {
			unificationXref xref = cmplx.getORGANISM().getTAXON_XREF();
			if (xref != null) {
				c.createAttribute(an, xref.getID(), false);
			} else if (Parser.taxidToUse != null
					&& Parser.taxidToUse.length() > 0) {
				// fall back to default TAXID
				c.createAttribute(an, Parser.taxidToUse, false);
			}
		}
	}

	/**
	 * A unification reference is a non-ambiguous accession.
	 * 
	 * @param c
	 *            ONDEXConcept with unification reference
	 * @param uni
	 *            unificationXref used
	 */
	private void addUnificationXref(ONDEXConcept c, unificationXref uni)
			throws Exception {

		// sanity checks
		if (uni.getID() == null || uni.getID().isEmpty()) {
			System.err.println(uni.getRDFId() + " is empty unificationXref.");
			return;
		}

		// get current data source
		DataSource elementOf = findDataSource(uni.getDB());
		
		if(elementOf.getId().equalsIgnoreCase("unknown")){
			System.err.println("DataSource is UNKNOWN");
			
		}
		
		
		if ((DEBUG || c.getConceptAccession(uni.getID(), elementOf) == null) && !(elementOf.getId().equalsIgnoreCase("unknown"))){
				c.createConceptAccession(uni.getID(), elementOf, false);
		}	
	}

	/**
	 * For publications create a separate concept and relationship.
	 * 
	 * @param c
	 *            ONDEXConcept with publication
	 * @param pub
	 *            publicationXref used
	 * @throws Exception
	 */
	private void addPublicationXref(ONDEXConcept c, publicationXref pub)
			throws Exception {

		// publication gets default data source
		DataSource elementOf = graph.getMetaData()
				.getDataSource(Parser.cvToUse);
		if (elementOf == null)
			throw new DataSourceMissingException(Parser.cvToUse
					+ " is missing.");

		// reused for relation
		EvidenceType evidence = graph.getMetaData().getEvidenceType(etIMPD);
		if (evidence == null)
			throw new EvidenceTypeMissingException(etIMPD + " is missing.");

		// get corresponding concept
		String rdfid = pub.getRDFId();
		if (!rdf2Concept.containsKey(rdfid)) {

			// create publication concept
			ConceptClass ofType = graph.getMetaData().getConceptClass(
					ccPublication);
			if (ofType == null)
				throw new ConceptClassMissingException(ccPublication
						+ " is missing.");

			// add concept to global map
			ONDEXConcept pubC = graph.getFactory().createConcept(rdfid,
					elementOf, ofType, evidence);
			rdf2Concept.put(rdfid, pubC);

			// PubMed id is non-ambiguous for a publication
			if (pub.getID() != null)
				pubC.createConceptAccession(pub.getID(), elementOf, false);

			// add year of publication
			if (pub.getYEAR() > 0) {
				AttributeName yearAN = graph.getMetaData().getAttributeName(
						anYEAR);
				if (yearAN == null)
					throw new AttributeNameMissingException(anYEAR
							+ " is missing.");
				pubC.createAttribute(yearAN, pub.getYEAR(), false);
			}

			// add journal reference
			AttributeName journalAN = graph.getMetaData().getAttributeName(
					anJOURNAL);
			if (journalAN == null)
				throw new AttributeNameMissingException(anJOURNAL
						+ " is missing.");
			StringBuffer source = new StringBuffer();
			for (String s : pub.getSOURCE()) {
				source.append(s);
				source.append("\n");
			}
			if (source.length() > 0) {
				pubC.createAttribute(journalAN, source.toString(), true);
				pubC.createConceptName(source.toString(), true);
			}

			// add title of publication
			if (pub.getTITLE() != null) {
				AttributeName titleAN = graph.getMetaData().getAttributeName(
						anTITLE);
				if (titleAN == null)
					throw new AttributeNameMissingException(anTITLE
							+ " is missing.");
				pubC.createAttribute(titleAN, pub.getTITLE(), true);
				pubC.createConceptName(pub.getTITLE(), false);
			}

			// add authors as list
			AttributeName authorsAN = graph.getMetaData().getAttributeName(
					anAUTHORS);
			if (authorsAN == null)
				throw new AttributeNameMissingException(anAUTHORS
						+ " is missing.");
			StringBuffer authors = new StringBuffer();
			for (String s : pub.getAUTHORS()) {
				authors.append(s);
				authors.append("; ");
			}
			if (authors.length() > 0)
				pubC.createAttribute(authorsAN, authors.toString(), true);

			// add possible URL of publication, only first one
			if (pub.getURL() != null) {
				AttributeName urlAN = graph.getMetaData().getAttributeName(
						anURL);
				if (urlAN == null)
					throw new AttributeNameMissingException(anURL
							+ " is missing.");
				Iterator<String> it = pub.getURL().iterator();
				if (it.hasNext())
					pubC.createAttribute(urlAN, it.next(), false);
			}
		}

		// create relation between concept and publication
		RelationType ofType = graph.getMetaData()
				.getRelationType(rtPublishedIn);

		ONDEXConcept pubC = rdf2Concept.get(rdfid);
		graph.getFactory().createRelation(c, pubC, ofType, evidence);
	}

	/**
	 * Any other relationships with entry of other data sources, marked as
	 * ambiguous.
	 * 
	 * @param c
	 *            ONDEXConcept with relationship reference
	 * @param rel
	 *            relationshipXref used
	 */
	private void addRelationshipXref(ONDEXConcept c, relationshipXref rel)
			throws Exception {

		// sanity checks
		if (rel.getID() == null || rel.getID().isEmpty()) {
			System.err.println(rel.getRDFId() + " is empty relationshipXref.");
			return;
		}
		DataSource elementOf = findDataSource(rel.getDB());
		if ((DEBUG || c.getConceptAccession(rel.getID(), elementOf) == null) && !(elementOf.getId().equalsIgnoreCase("unknown"))){
			c.createConceptAccession(rel.getID(), elementOf, false);
		}	
	}
		// get current data source
		
		//if (DEBUG || c.getConceptAccession(rel.getID(), elementOf) == null)
			//c.createConceptAccession(rel.getID(), elementOf, false);
	//}

	/**
	 * Returns a DataSource, either the specified one derived from a mapping
	 * table or "unknown".
	 * 
	 * @param cv
	 *            name of DataSource to find
	 * @return DataSource found or "unknown"
	 * @throws Exception
	 */
	private DataSource findDataSource(String cv) throws Exception {
		// sanity checks
		if (cv == null) {
			cv = "unknown";
		}

		// retrieve right DataSource
		DataSource elementOf = null;
		if (!cvMap.containsKey(cv)) {
			System.err.println("Missing DataSource mapping: " + cv);
			cv = "unknown";
		}
		elementOf = graph.getMetaData().getDataSource(cvMap.get(cv));
		if (elementOf == null)
			throw new DataSourceMissingException(cv + " is missing.");

		return elementOf;
	}

	/**
	 * Tries to infer the associated data source of an entity.
	 * 
	 * @param e
	 *            entity to get data source for
	 * @return ONDEX DataSource
	 */
	public DataSource getDataSource(entity e) throws Exception {
		// count only first data source if exits
		String id = Parser.cvToUse;
		if (!e.getDATA_SOURCE().isEmpty())
			id = e.getDATA_SOURCE().iterator().next().getRDFId();
		if (!cvMap.containsKey(id))
			cvMap.put(id, Parser.cvToUse);
		DataSource elementOf = graph.getMetaData().getDataSource(cvMap.get(id));
		if (elementOf == null)
			throw new DataSourceMissingException(cvMap.get(id) + " is missing.");
		return elementOf;
	}

}
