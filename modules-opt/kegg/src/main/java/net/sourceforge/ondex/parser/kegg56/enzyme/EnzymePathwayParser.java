/*
 * Created on 16-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.enzyme;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;

import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.kegg56.MetaData;
import net.sourceforge.ondex.parser.kegg56.Parser;
import net.sourceforge.ondex.parser.kegg56.data.Entry;
import net.sourceforge.ondex.parser.kegg56.data.Pathway;
import net.sourceforge.ondex.parser.kegg56.sink.Concept;
import net.sourceforge.ondex.parser.kegg56.sink.ConceptAcc;
import net.sourceforge.ondex.parser.kegg56.sink.ConceptName;
import net.sourceforge.ondex.parser.kegg56.sink.Relation;
import net.sourceforge.ondex.parser.kegg56.util.DPLPersistantSet;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @author taubertj
 */
public class EnzymePathwayParser {

	private ArrayList<String> writtenIds = new ArrayList<String>();

	public void parseAndWrite(DPLPersistantSet<Pathway> pathways,
			DPLPersistantSet<Relation> relationsCache)
			throws MetaDataMissingException, InconsistencyException {

		final Pattern plusSplit = Pattern.compile("[+]");
		final Pattern spaceSplit = Pattern.compile(" ");

		EntityCursor<Pathway> cursor = pathways.getCursor();
		for (Pathway pathway : cursor) {
			for (Entry entry : pathway.getEntries().values()) {
				if (entry.getType().equalsIgnoreCase(MetaData.CC_ENZYME)) {
					String[] results = spaceSplit.split(entry.getName()
							.toUpperCase());
					for (String result : results) {

						// String[] ecs = FastSplit.fastSplit(result,
						// FastSplit.COLON);
						String[] plus = plusSplit.split(result);
						for (String idpart : plus) {
							String id = idpart.toUpperCase() + "_EN";

							// Enzyme for ec number
							Concept concept_enzyme = new Concept(id,
									MetaData.CV_KEGG, MetaData.CC_ENZYME);
							concept_enzyme
									.setDescription("Infered enzyme for EC in pathway");
							if (entry.getLink() != null)
								concept_enzyme.setUrl(entry.getLink());

							entry.getConceptIDs().add(concept_enzyme.getId());

							concept_enzyme.addContext(pathway.getId());

							if (!writtenIds.contains(concept_enzyme.getId())) {
								writtenIds.add(concept_enzyme.getId());
								Parser.getUtil().writeConcept(concept_enzyme);
							}

							// Introduce new EC class
							Concept ec = new Concept(idpart, MetaData.CV_KEGG,
									MetaData.CC_EC);
							if (!Parser.getConceptWriter()
									.conceptParserIDIsWritten(ec.getId())) {
								ec.setDescription("ec from entry name");

								// the first name is preferred
								ConceptName cn = new ConceptName(ec.getId(),
										true);
								ec.getConceptNames().add(cn);

								if (ec.getId().toUpperCase().startsWith("EC:")) {
									String ecNumber = ec.getId().replaceAll(
											"[^0-9|\\.|\\-]", "");
									ConceptAcc ca = new ConceptAcc(ecNumber,
											MetaData.CV_EC);
									ec.getConceptAccs().add(ca);
								}

								if (!writtenIds.contains(ec.getId())) {
									writtenIds.add(ec.getId());
									Parser.getUtil().writeConcept(ec, false);
								}
							}

							// relation between EC and enzyme
							Relation member_of = new Relation(concept_enzyme
									.getId(), ec.getId(),
									MetaData.RT_MEMBER_PART_OF);
							if (relationsCache.contains(member_of.pk))
								member_of = relationsCache.get(member_of.pk);
							member_of.addContext(pathway.getId());
							relationsCache.add(member_of);
						}
					}
				}
			}
			try {
				cursor.update(pathway);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		pathways.closeCursor(cursor);
		Parser.getUtil().writeRelations(relationsCache);

		writtenIds.clear();
	}
}
