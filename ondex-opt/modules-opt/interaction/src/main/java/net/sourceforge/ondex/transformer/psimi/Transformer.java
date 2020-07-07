package net.sourceforge.ondex.transformer.psimi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.RelationTypeMissingException;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

/**
 * Hard coded transformation from PSI-MI parser output into more interaction
 * network compatible representation.
 * 
 * @author taubertj
 * 
 */
public class Transformer extends ONDEXTransformer {

	@Override
	public String getId() {
		return "psimitrans";
	}

	@Override
	public String getName() {
		return "PSI-MI Transformer";
	}

	@Override
	public String getVersion() {
		return "30/03/11";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[] {
				new StringArgumentDefinition("DataSource",
						"The DataSource to use in evidence triple.", false,
						null, false),
				new StringArgumentDefinition("Organism",
						"The organism to use in evidence triple.", false, null,
						false) };
	}

	@Override
	public void start() throws Exception {

		// get all concept classes under consideration
		ConceptClass ccPolyPeptide = graph.getMetaData().getConceptClass(
				"Polypeptide");
		if (ccPolyPeptide == null) {
			throw new ConceptClassMissingException(
					"ConceptClass Polypeptide is missing.");
		}

		ConceptClass ccExperiment = graph.getMetaData().getConceptClass(
				"Experiment");
		if (ccExperiment == null) {
			throw new ConceptClassMissingException(
					"ConceptClass Experiment is missing.");
		}

		ConceptClass ccMI0914 = graph.getMetaData().getConceptClass("MI:0914");
		if (ccMI0914 == null) {
			throw new ConceptClassMissingException(
					"ConceptClass MI:0914 is missing.");
		}

		// get all relation types under consideration
		RelationType rtParticipatesIn = graph.getMetaData().getRelationType(
				"participates_in");
		if (rtParticipatesIn == null) {
			throw new RelationTypeMissingException(
					"RelationType participates_in is missing.");
		}

		RelationType rtObservedIn = graph.getMetaData().getRelationType(
				"observed_in");
		if (rtObservedIn == null) {
			throw new RelationTypeMissingException(
					"RelationType observed_in is missing.");
		}

		// new relation type
		RelationType rtInteracts = graph.getMetaData().getRelationType("it_wi");
		if (rtInteracts == null) {
			rtInteracts = graph.getMetaData().getFactory()
					.createRelationType("it_wi");
		}

		AttributeName anTaxid = graph.getMetaData().getAttributeName("TAXID");
		if (anTaxid == null) {
			throw new AttributeNameMissingException(
					"AttributeName TAXID is missing.");
		}

		// override things in evidence triple
		String dataSource = (String) args.getUniqueValue("DataSource");
		String organism = (String) args.getUniqueValue("Organism");

		// start with interaction concepts
		Set<ONDEXConcept> concepts = new HashSet<ONDEXConcept>();
		for (ONDEXConcept c : graph.getConcepts()) {
			// concept class hierarchy has to exist
			if (c.inheritedFrom(ccMI0914))
				concepts.add(c);
		}

		// consider each pair of polypeptide
		for (ONDEXConcept from : graph.getConceptsOfConceptClass(ccPolyPeptide)) {
			for (ONDEXConcept to : graph
					.getConceptsOfConceptClass(ccPolyPeptide)) {
				if (!from.equals(to)) {
					// accumulate all participants and experiments first
					Set<ONDEXConcept> interactions = new HashSet<ONDEXConcept>();
					Set<ONDEXConcept> experiments = new HashSet<ONDEXConcept>();

					// search for all relations to interaction
					for (ONDEXRelation r : graph.getRelationsOfConcept(from)) {
						if (r.getOfType().isAssignableTo(rtParticipatesIn)) {

							// found interaction of this concept
							ONDEXConcept interaction = r.getToConcept();
							for (ONDEXRelation interactionRel : graph
									.getRelationsOfConcept(interaction)) {
								if (interactionRel.getOfType().isAssignableTo(
										rtParticipatesIn)
										&& interactionRel.getFromConcept()
												.equals(to)) {
									// found interaction between from and to
									interactions.add(interaction);
								}
							}
						}
					}

					// continue if there are no interactions
					if (interactions.size() == 0)
						continue;
					
					// get experiments for interactions
					for (ONDEXConcept interaction : interactions) {
						for (ONDEXRelation interactionRel : graph
								.getRelationsOfConcept(interaction)) {
							if (interactionRel.getOfType().equals(rtObservedIn)) {
								// get experiments of interaction
								experiments.add(interactionRel.getToConcept());
							}
						}
					}

					// collect all evidences from experiments
					Set<EvidenceType> evidences = new HashSet<EvidenceType>();
					Set<String> triples = new HashSet<String>();
					Map<String, Integer> expCount = new HashMap<String, Integer>();
					for (ONDEXConcept experiment : experiments) {
						evidences.addAll(experiment.getEvidence());
						DataSource ds = experiment.getElementOf();
						Attribute taxId = experiment.getAttribute(anTaxid);
						if (taxId != null || organism != null) {
							for (EvidenceType et : experiment.getEvidence()) {
								// ignore non-specific IMPD
								if (!et.getId().equals("IMPD")) {
									// get name of evidence type to concatenate
									String etName = et.getFullname();
									if (etName == null || etName.length() == 0)
										etName = et.getId();
									// get name of data source to concatenate
									String dsName = ds.getFullname();
									if (dsName == null || dsName.length() == 0)
										dsName = ds.getId();
									if (dataSource != null)
										dsName = dataSource;
									// get value of taxId to concatenate
									String id = null;
									if (organism != null)
										id = organism;
									else
										id = taxId.getValue().toString();
									String triple = etName + "/" + dsName + "/"
											+ id;
									triples.add(triple);
									if (!expCount.containsKey(dsName))
										expCount.put(dsName, Integer.valueOf(1));
									else
										expCount.put(dsName,
												expCount.get(dsName) + 1);
								}
							}
						}
					}

					// create interaction
					ONDEXRelation r = graph.createRelation(from, to,
							rtInteracts, evidences);

					// add evidence triples to relation
					for (String s : triples) {
						AttributeName anEvidence = null;
						int i = 0;
						do {
							String name = "evidence";
							if (i > 0) {
								name = name + "_" + i;
							}
							anEvidence = graph.getMetaData().getAttributeName(
									name);
							if (anEvidence == null) {
								anEvidence = graph
										.getMetaData()
										.getFactory()
										.createAttributeName(name, String.class);
							}
							i++;
						} while (r.getAttribute(anEvidence) != null);
						r.createAttribute(anEvidence, s, false);
					}

					// add experiment count
					for (String dsName : expCount.keySet()) {
						String name = "experimentCount"
								+ dsName.replaceAll("\\s", "_");
						AttributeName anCount = graph.getMetaData()
								.getAttributeName(name);
						if (anCount == null)
							anCount = graph.getMetaData().getFactory()
									.createAttributeName(name, Integer.class);
						r.createAttribute(anCount, experiments.size(), false);
					}
				}
			}
		}

		// prepare for concept removal
		concepts = BitSetFunctions.copy(graph.getConcepts());
		concepts.removeAll(graph.getConceptsOfConceptClass(ccPolyPeptide));
		ONDEXConcept[] toDelete = concepts.toArray(new ONDEXConcept[0]);
		for (ONDEXConcept c : toDelete) {
			graph.deleteConcept(c.getId());
		}

		// remove unconnected polypeptides
		concepts = graph.getConcepts();
		toDelete = concepts.toArray(new ONDEXConcept[0]);
		for (ONDEXConcept c : toDelete) {
			if (graph.getRelationsOfConcept(c).size() == 0)
				graph.deleteConcept(c.getId());
		}
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

}
