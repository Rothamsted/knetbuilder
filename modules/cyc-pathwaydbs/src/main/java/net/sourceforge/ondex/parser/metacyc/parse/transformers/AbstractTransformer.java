package net.sourceforge.ondex.parser.metacyc.parse.transformers;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.parser.metacyc.MetaData;
import net.sourceforge.ondex.parser.metacyc.Parser;
import net.sourceforge.ondex.parser.metacyc.objects.AbstractNode;
import net.sourceforge.ondex.parser.metacyc.objects.DBName;
import net.sourceforge.ondex.parser.metacyc.objects.Publication;
import net.sourceforge.ondex.tools.functions.StandardFunctions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Similar to the ConceptWriter in the other packages. Provides a basic
 * functionality to transform an AbstractNode object to an ONDEXConcept with
 * the outging relationships.
 *
 * @author peschr
 *
 */
public abstract class AbstractTransformer {
	private Parser parser;

	protected DataSource dataSourceMetaC = null;

	protected DataSource dataSourceNCBIGene = null;

	protected DataSource dataSourceRefSeq = null;

	protected DataSource dataSourceUniProt = null;

	protected DataSource dataSourcePir = null;

	protected DataSource dataSourceEcoCyc = null;

	protected DataSource dataSourceCAS = null;

	protected EvidenceType etIMPD = null;

	protected ONDEXGraph graph = null;

	protected ConceptClass ccPublication = null;

	protected DataSource dataSourcePubMed = null;

	protected RelationType rtPublishedIn = null;

	public AbstractTransformer(Parser parser) {

		this.parser = parser;
		graph = parser.getGraph();

		dataSourceMetaC = graph.getMetaData().getDataSource(MetaData.CV_MetaC);
		if (dataSourceMetaC == null) {
			Parser.propagateEventOccurred(new DataSourceMissingEvent(MetaData.CV_MetaC, Parser.getCurrentMethodName()));
		}

		etIMPD = graph.getMetaData().getEvidenceType(MetaData.IMPD);
		if (etIMPD == null) {
			Parser.propagateEventOccurred(new EvidenceTypeMissingEvent(MetaData.IMPD, Parser.getCurrentMethodName()));
		}

		ccPublication = graph.getMetaData().getConceptClass(
				MetaData.CC_Publication);
		if (ccPublication == null) {
			Parser.propagateEventOccurred(new ConceptClassMissingEvent(MetaData.CC_Publication, Parser.getCurrentMethodName()));
		}

		rtPublishedIn = graph.getMetaData().getRelationType(
				MetaData.RT_PUBLISHED_IN);
		if (rtPublishedIn == null) {
			Parser.propagateEventOccurred(new RelationTypeMissingEvent(MetaData.RT_PUBLISHED_IN, Parser.getCurrentMethodName()));
		}

		dataSourceNCBIGene = graph.getMetaData().getDataSource(MetaData.CV_NCBIGene);
		if (dataSourceNCBIGene == null) {
			Parser.propagateEventOccurred(new DataSourceMissingEvent(MetaData.CV_NCBIGene, Parser.getCurrentMethodName()));
		}

		dataSourceEcoCyc = graph.getMetaData().getDataSource(MetaData.CV_ECOCYC);
		if (dataSourceEcoCyc == null) {
			Parser.propagateEventOccurred(new DataSourceMissingEvent(MetaData.CV_ECOCYC, Parser.getCurrentMethodName()));
		}

		dataSourceCAS = graph.getMetaData().getDataSource(MetaData.CV_CAS);
		if (dataSourceCAS == null) {
			Parser.propagateEventOccurred(new DataSourceMissingEvent(MetaData.CV_CAS, Parser.getCurrentMethodName()));
		}

		dataSourceRefSeq = graph.getMetaData().getDataSource(MetaData.CV_REFSEQGene);
		if (dataSourceRefSeq == null) {
			Parser.propagateEventOccurred(new DataSourceMissingEvent(MetaData.CV_REFSEQGene, Parser.getCurrentMethodName()));
		}

		dataSourceUniProt = graph.getMetaData().getDataSource(MetaData.CV_UniProt);
		if (dataSourceUniProt == null) {
			Parser.propagateEventOccurred(new DataSourceMissingEvent(MetaData.CV_UniProt, Parser.getCurrentMethodName()));
		}

		dataSourcePir = graph.getMetaData().getDataSource(MetaData.CV_PIR);
		if (dataSourcePir == null) {
			Parser.propagateEventOccurred(new DataSourceMissingEvent(MetaData.CV_PIR, Parser.getCurrentMethodName()));
		}

	}

	/**
	 *
	 * @param node
	 */
	public final void pointerToRelationsCore(AbstractNode node) {
		Iterator<Publication> nodes = node.getPublications();
		while (nodes.hasNext()) {
			AbstractNode toNode = nodes.next();
			HashSet<ONDEXConcept> contexts = this.getNonRedundant(toNode.getConcept(),node.getConcept());
			this.copyContext(toNode.getConcept(), contexts);
			this.copyContext(
					graph.getFactory().createRelation(node.getConcept(), toNode.getConcept(),
							rtPublishedIn, etIMPD),contexts);
		}
		this.pointerToRelation(node);
	}

	/**
	 * translate a DBName object to a DataSource object
	 *
	 * @param dbName
	 * @return
	 * @throws NoSuchElementException
	 */
	private DataSource getElementOf(DBName dbName) throws NoSuchElementException {
		if (DBName.ENTREZ == dbName)
			return this.dataSourceNCBIGene;
		else if (DBName.NCBIGENE == dbName)
			return this.dataSourceNCBIGene;
		else if (DBName.REFSEQ == dbName)
			return this.dataSourceRefSeq;
		else if (DBName.UNIPROT == dbName)
			return this.dataSourceUniProt;
		else if (DBName.PIR == dbName)
			return this.dataSourcePir;
		else if (dbName.name().equals("ECOCYC")) {
			return this.dataSourceEcoCyc;
		} else if (dbName.name().equals("CAS")) {
			return this.dataSourceCAS;
		} else {
			throw new NoSuchElementException();
		}
	}

	private static Pattern htmlTags = Pattern.compile("<.*?>");

	/**
	 * adds common details to a concept. Such as uniqueId, commonname,
	 * accession-numbers, description and synonyms.
	 *
	 * @param concept
	 * @param node
	 */
	public final void addCommonDetailsToConcept(ONDEXConcept concept,
			AbstractNode node) {
		if (node.getCommonName() == null || node.getCommonName().length() == 0) {
			String cleanedName = htmlTags.matcher(node.getUniqueId()).replaceAll("");
			if (node.getUniqueId().length() != cleanedName.length()) {
				concept.createConceptName(node.getUniqueId(), false);
				concept.createConceptName(cleanedName, true);
			} else {
				if(StandardFunctions.nullChecker(node.getUniqueId()))
					return;
				concept.createConceptName(node.getUniqueId(), true);
			}
		} else {
			String cleanedName = htmlTags.matcher(node.getCommonName()).replaceAll("");
			if (node.getCommonName().length() != cleanedName.length()) {
				concept.createConceptName(node.getCommonName(), false);
				concept.createConceptName(cleanedName, true);
			} else {
				concept.createConceptName(node.getCommonName(), true);
			}
		}

		if (node.getDbLink() != null) {
			try {
				concept.createConceptAccession(node.getDbLink()
						.getAccession(), this.getElementOf(node.getDbLink()
								.getDbName()), false);
			} catch (NoSuchElementException e) {
				System.out.println("unknown database:" + node.getDbLink());
			}
		}
		concept.createConceptAccession(node.getUniqueId(), this.dataSourceMetaC,
				false);
		if (node.getComment() != null)
			concept.setDescription(node.getComment());
		if (node.getSynonym().size() > 0) {
			Iterator<String> i = node.getSynonym().iterator();
			while (i.hasNext()) {
				String name = i.next();
				if (!name.equals(node.getCommonName())
						&& !name.equals(node.getUniqueId()))

					if (concept.getConceptName(name) == null)
						concept.createConceptName(name,false);
			}
		}
	}

	public abstract void pointerToRelation(AbstractNode node);

	public abstract void nodeToConcept(AbstractNode node);

	public Parser getParser() {
		return parser;
	}
	/**
	 * Copies a context list from on concept to another concept
	 * @param acFrom
	 * @param acTo
	 */
	public void copyContext(ONDEXConcept acTo, Set<ONDEXConcept> acFrom){
        for (ONDEXConcept concept : acFrom) {
            if (!acTo.getTags().contains(concept)) {
                acTo.addTag(concept);
            }
        }
	}
	/**
	 * @param acTo
	 * @param acFrom
	 */
	public void copyContext(ONDEXRelation acTo, Set<ONDEXConcept> acFrom){
        for (ONDEXConcept concept : acFrom) {
            if (!acTo.getTags().contains(concept)) {
                acTo.addTag(concept);
            }
        }
	}

	public HashSet<ONDEXConcept> getNonRedundant(ONDEXConcept acTo, ONDEXConcept acFrom){
		HashSet<ONDEXConcept> nonRedundantConcepts = new HashSet<ONDEXConcept>();
		for (ONDEXConcept concept : acFrom.getTags()){
			if (acTo != null && !acTo.getTags().contains(concept))
				nonRedundantConcepts.add(concept);
			else if(acTo == null){
				nonRedundantConcepts.add(concept);
			}
		}
		return nonRedundantConcepts;
	}
}
