/*
 * Created on 16-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.ko;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.kegg56.MetaData;
import net.sourceforge.ondex.parser.kegg56.Parser;
import net.sourceforge.ondex.parser.kegg56.sink.Concept;
import net.sourceforge.ondex.parser.kegg56.sink.ConceptAcc;
import net.sourceforge.ondex.parser.kegg56.sink.ConceptWriter;
import net.sourceforge.ondex.parser.kegg56.sink.Relation;

/**
 * Adds relations between KEGG ortholog groups, there members and the EC number.
 * 
 * @author taubertj
 */
public class KoRelationMerger {

	/**
	 * Create relations of type m_isp and is_a
	 * 
	 * @param ko2Genes
	 *            KO id to all genes of KO
	 * @param koAccessionToKoConcept
	 *            KO accession to KO concept
	 * @throws MetaDataMissingException
	 * @throws InconsistencyException
	 */
	public void mergeAndWrite(Map<String, Set<String>> ko2Genes,
			Map<String, Concept> koAccessionToKoConcept)
			throws MetaDataMissingException, InconsistencyException {

		ConceptWriter cw = Parser.getConceptWriter();

		Set<String> notFound = new HashSet<String>();

		// sort KO names
		for (String ko : ko2Genes.keySet()) {
			// get set of genes for KO sorted by gene name
			for (String gene : ko2Genes.get(ko)) {
				// go through all genes
				gene = gene + "_GE";
				if (!cw.conceptParserIDIsWritten(gene)) {
					// notFound.add(gene);
				} else {
					// this relation always exists
					Relation relation_gene = new Relation(gene, ko,
							MetaData.RT_MEMBER_PART_OF);
					Parser.getUtil().writeRelation(relation_gene);
				}
			}

			// link EC terms to KOs
			Concept koconcept = koAccessionToKoConcept.get(ko.substring(3));
			for (ConceptAcc acc : koconcept.getConceptAccs()) {
				if (acc.getElement_of().equals(MetaData.CV_EC)) {
					String ecname = acc.getConcept_accession();
					if (!cw.conceptParserIDIsWritten(ecname)) {
						notFound.add(ecname);
						//System.out.println("Not found: " + ecname);
					} else {
						// this relation always exists
						Relation relation_ec = new Relation(ko, ecname,
								MetaData.RT_CATALYSEING_CLASS);
						Parser.getUtil().writeRelation(relation_ec);
					}
				}
			}
		}

		System.err.println("Not found ECs for KO relations: " + notFound);
	}
}
