package net.sourceforge.ondex.mapping.interolog;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.mapping.ArgumentNames;
import net.sourceforge.ondex.mapping.ONDEXMapping;

/**
 * Implements a Interolog mapping to infer interactions from other organisms
 * based on ortholog and paralog relations.
 * 
 * @author taubertj
 * @version 24.02.2011
 */
@Authors(authors = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Custodians(custodians = { "Jochen Weile" }, emails = { "jweile at users.sourceforge.net" })
public class Mapping extends ONDEXMapping implements MetaData, ArgumentNames {

	@Override
	public String getId() {
		return "interolog";
	}

	@Override
	public String getName() {
		return "Interolog Mapping";
	}

	@Override
	public String getVersion() {
		return "29.03.2011";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {

		// these are evaluated in the evaluateConcept in the super
		StringArgumentDefinition gdsEquals = new StringArgumentDefinition(
				ATTRIBUTE_EQUALS_ARG, ATTRIBUTE_EQUALS_ARG_DESC, false, null,
				false);
		return new ArgumentDefinition<?>[] { gdsEquals };
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	@Override
	public void start() throws Exception {

		// optional argument
		AttributeName anEQUAL = null;
		if (args.getUniqueValue(ATTRIBUTE_EQUALS_ARG) != null) {
			anEQUAL = graph.getMetaData().getAttributeName(
					(String) args.getUniqueValue(ATTRIBUTE_EQUALS_ARG));
			if (anEQUAL == null) {
				fireEventOccurred(new AttributeNameMissingEvent(
						"AttributeName "
								+ args.getUniqueValue(ATTRIBUTE_EQUALS_ARG)
								+ " missing. Cannot continue.",
						"[Mapping - start]"));
				return;
			}
		}

		// should exist
		RelationType ortho = graph.getMetaData().getRelationType(rtOrtholog);
		if (ortho == null) {
			fireEventOccurred(new RelationTypeMissingEvent("Relation type "
					+ rtOrtholog + " missing. Cannot continue.",
					"[Mapping - start]"));
			return;
		}

		// should exist
		RelationType para = graph.getMetaData().getRelationType(rtParalog);
		if (para == null) {
			fireEventOccurred(new RelationTypeMissingEvent("Relation type "
					+ rtParalog + " missing. Cannot continue.",
					"[Mapping - start]"));
			return;
		}

		// should exist
		RelationType it_wi = graph.getMetaData().getRelationType(rtInteracts);
		if (it_wi == null) {
			fireEventOccurred(new RelationTypeMissingEvent("Relation type "
					+ rtInteracts + " missing. Cannot continue.",
					"[Mapping - start]"));
			return;
		}

		// this relation type could be a new one
		RelationType interolog = graph.getMetaData().getRelationType(
				rtInterolog);
		if (interolog == null) {
			interolog = graph.getMetaData().getFactory()
					.createRelationType(rtInterolog);
		}

		// possible new evidence type
		EvidenceType evidence = graph.getMetaData()
				.getEvidenceType(etInterolog);
		if (evidence == null) {
			evidence = graph.getMetaData().getFactory()
					.createEvidenceType(etInterolog);
		}

		// start by getting all existing interacts relations
		for (ONDEXRelation relIt_wi : graph.getRelationsOfRelationType(it_wi)) {

			// get from and to concept of it_wi
			ONDEXConcept fromIt_wi = relIt_wi.getFromConcept();
			ONDEXConcept toIt_wi = relIt_wi.getToConcept();

			// gather all ortho relations of from concept
			Set<ONDEXConcept> fromOrtho = new HashSet<ONDEXConcept>();
			for (ONDEXRelation r : graph.getRelationsOfConcept(fromIt_wi)) {
				if (r.getOfType().equals(ortho)) {
					// add concept at other end of ortho relation
					if (r.getFromConcept().equals(fromIt_wi))
						fromOrtho.add(r.getToConcept());
					else
						fromOrtho.add(r.getFromConcept());
				}
			}

			// gather all ortho relations of to concept
			Set<ONDEXConcept> toOrtho = new HashSet<ONDEXConcept>();
			for (ONDEXRelation r : graph.getRelationsOfConcept(toIt_wi)) {
				if (r.getOfType().equals(ortho)) {
					// add concept at other end of ortho relation
					if (r.getFromConcept().equals(toIt_wi))
						toOrtho.add(r.getToConcept());
					else
						toOrtho.add(r.getFromConcept());
				}
			}

			// add para relations of ortho concept
			Set<ONDEXConcept> fromPara = new HashSet<ONDEXConcept>();
			for (ONDEXConcept c : fromOrtho) {
				// look for additional para relations
				for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
					if (r.getOfType().equals(para)) {
						// add concept at other end of para relation
						if (r.getToConcept().equals(c))
							fromPara.add(r.getFromConcept());
						else
							fromPara.add(r.getToConcept());
					}
				}
			}
			fromOrtho.addAll(fromPara);

			// add para relations of ortho concept
			Set<ONDEXConcept> toPara = new HashSet<ONDEXConcept>();
			for (ONDEXConcept c : toOrtho) {
				// look for additional para relations
				for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
					if (r.getOfType().equals(para)) {
						// add concept at other end of para relation
						if (r.getToConcept().equals(c))
							toPara.add(r.getFromConcept());
						else
							toPara.add(r.getToConcept());
					}
				}
			}
			toOrtho.addAll(toPara);

			int nonMapping = 0;

			// create new relations between all members of fromOrthoPara and
			// toOrthoPara sets
			for (ONDEXConcept from : fromOrtho) {
				Attribute fromAttribute = null;
				if (anEQUAL != null) {
					fromAttribute = from.getAttribute(anEQUAL);
				}

				for (ONDEXConcept to : toOrtho) {
					Attribute toAttribute = null;
					if (anEQUAL != null) {
						toAttribute = to.getAttribute(anEQUAL);
					}

					boolean map = true;
					if (anEQUAL != null) {
						if (fromAttribute == null || toAttribute == null) {
							map = false;
							nonMapping++;
						} else {
							Object o1 = fromAttribute.getValue();
							Object o2 = toAttribute.getValue();
							if (!o1.equals(o2)) {
								map = false;
								nonMapping++;
							}
						}
					}

					// new interolog relation
					if (map) {
						ONDEXRelation r;
						if (graph.getRelation(from, to, interolog) == null) {
							r = graph.getFactory().createRelation(from, to,
									interolog, evidence);
						} else {
							r = graph.getRelation(from, to, interolog);
						}
						// possibly add some Attributes here
						for (Attribute attrIt_wi : relIt_wi.getAttributes()) {
							if (r.getAttribute(attrIt_wi.getOfType()) == null) {
								r.createAttribute(attrIt_wi.getOfType(),
										attrIt_wi.getValue(), false);
							} else {
								// add as additional attribute name
								AttributeName an = null;
								int i = 1;
								do {
									String name = attrIt_wi.getOfType().getId();
									if (name.indexOf("_") > 0)
										name = name.substring(0,
												name.indexOf("_"));
									name = name + "_" + i;
									an = graph.getMetaData().getAttributeName(
											name);
									if (an == null) {
										an = graph
												.getMetaData()
												.getFactory()
												.createAttributeName(name,
														String.class);
									}
									i++;
								} while (r.getAttribute(an) != null);
								r.createAttribute(an, attrIt_wi.getValue(),
										false);
							}
						}
					}
				}
			}

			fireEventOccurred(new GeneralOutputEvent(
					"Interologs missed because of non matching or non existing attributs: "
							+ nonMapping, "[Mapping - start]"));
		}
	}
}
