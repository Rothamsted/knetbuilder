package net.sourceforge.ondex.parser.wordnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.config.ValidatorRegistry;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;

import org.apache.log4j.Level;

public class Writer {

	ONDEXGraph graph;

	ONDEXParser Parser;

	ONDEXPluginArguments pa;

	HashSet<String> relationSet = new HashSet<String>();

	public Writer(ONDEXGraph gr, ONDEXPluginArguments p, ONDEXParser parser) {
		graph = gr;
		pa = p;
		this.Parser = parser;
	}

	public void write(ArrayList<Entity> input_entries) throws Exception {

		DataSource dataSource = null;
		EvidenceType et = null;
		ConceptClass cc = null;

		try {
			dataSource = graph.getMetaData().getDataSource(
					input_entries.get(0).getElement_of());
		} catch (Exception e) {
			DataSourceMissingEvent so = new DataSourceMissingEvent(input_entries.get(0)
					.getElement_of()
					+ " missing.", "[Writer - write]");
			Parser.fireEventOccurred(so);
            throw e;
		}
		try {
			et = graph.getMetaData().getEvidenceType(
					input_entries.get(0).getEvidence());
		} catch (Exception e) {
			EvidenceTypeMissingEvent so = new EvidenceTypeMissingEvent(
					input_entries.get(0).getEvidence() + " missing.",
					"[Writer - write]");
			Parser.fireEventOccurred(so);
            throw e;
		}
		try {
			// Large assumption that all concepts in the list will have the
			// same concept Class.
			String conceptClass = input_entries.get(0).getOf_type();
			cc = graph.getMetaData().getConceptClass(conceptClass);
		} catch (Exception e) {

			ConceptClassMissingEvent so = new ConceptClassMissingEvent(
					input_entries.get(0).getOf_type() + " missing.",
					"[Writer - write]");
			Parser.fireEventOccurred(so);
            throw e;
		}

		HashMap<String, Integer> idMapping = new HashMap<String, Integer>();

		AbstractONDEXValidator validator = ValidatorRegistry.validators
				.get("cvregex");

		if (dataSource != null && et != null && cc != null) {

			GeneralOutputEvent so = new GeneralOutputEvent("Write Concepts..",
					"[Writer - write]");
			so.setLog4jLevel(Level.INFO);
			Parser.fireEventOccurred(so);

			for (int i = 0; i < input_entries.size(); i++) {
				Entity entry = input_entries.get(i);

				// fix to get the correct et cc and cv if they differ in the
				// list of entities
				cc = getConceptClass(cc, entry);
				et = getEvidenceType(et, entry);
				dataSource = getDataSource(dataSource, entry);

				// write the CONCEPT
				String cID = entry.getId();
				String annotation = entry.getDescription();

				ONDEXConcept c = graph.getFactory().createConcept(cID,
						annotation, annotation, dataSource, cc, et);
				idMapping.put(cID, c.getId());

				// write the CONCEPT_NAME

				// preferred name
				String name = entry.getName();
				if (name != null && !name.equals("")) {
					c.createConceptName(name, true);
				}

				// synonyms
				ArrayList<String> synonyms = entry.getSynonyms();
				Set<String> names = new TreeSet<String>();
				names.add(name);

				for (int j = 0; j < synonyms.size(); j++) {
					name = synonyms.get(j);
					if (!names.contains(name)) {
						names.add(name);
						c.createConceptName(name, false);
					}
				}

				// write the CONCEPT_ACC
				ArrayList<Accession> accessions = entry.getAccessions();
				Set<String> accs = new TreeSet<String>();

				for (int j = 0; j < accessions.size(); j++) {

					String acc = accessions.get(j).getAccession();

					if (!accs.contains(acc)) {

						accs.add(acc);

						String cvname = (String) validator.validate(acc);

						if (cvname != null) {
							DataSource accdataSource = graph.getMetaData().getDataSource(cvname);

							if (accdataSource == null) {

								Parser
										.fireEventOccurred(new DataSourceMissingEvent(
												"Missing " + accdataSource,
												"[Writer - write]"));
							} else {

								boolean amb = accessions.get(j).getAmbiguous();

								c.createConceptAccession(acc, accdataSource, amb);
							}
						} else {
							Parser.fireEventOccurred(new DataSourceMissingEvent(
									"Missing " + cvname, "[Writer - write]"));
						}
					}
				}

			}

			so = new GeneralOutputEvent("Successfully written "
					+ input_entries.size() + " concepts.", "[Writer - write]");
			so.setLog4jLevel(Level.INFO);
			Parser.fireEventOccurred(so);

			so = new GeneralOutputEvent("Write Relations..", "[Writer - write]");
			so.setLog4jLevel(Level.INFO);
			Parser.fireEventOccurred(so);

			int rel = 0;

			for (int i = 0; i < input_entries.size(); i++) {

				Entity entry = input_entries.get(i);
				Iterator<Relation> relations = entry.getRelations();

				while (relations.hasNext()) {

					Relation rela = relations.next();

					String from_id = rela.getFrom();
					String to_id = rela.getTo();
					String t_set = rela.getRelationType();

					Integer fromId = idMapping.get(from_id);
					Integer toId = idMapping.get(to_id);

					if (fromId != null && toId != null) {

						ONDEXConcept from = graph.getConcept(fromId);
						ONDEXConcept to = graph.getConcept(toId);
						RelationType rtset = graph.getMetaData()
								.getRelationType(t_set);

						String key = from_id + to_id + t_set;
						if (!relationSet.contains(key)) {

							graph.getFactory().createRelation(from, to, rtset,
									et);
							relationSet.add(key);
							rel++;
						}
					} else {
						InconsistencyEvent ge = new InconsistencyEvent(
								"Relation error: from concept or to concept is missing.",
								"[Writer - write]");
						Parser.fireEventOccurred(ge);
					}
				}

			}
			so = new GeneralOutputEvent("Successfully written " + rel
					+ " relations.", "[Writer - write]");
			so.setLog4jLevel(Level.INFO);
			Parser.fireEventOccurred(so);
		} else {
			InconsistencyEvent ge = new InconsistencyEvent(
					"cv==null || et==null || cc==null", "[Writer - write]");
			Parser.fireEventOccurred(ge);
		}

	}

	private ConceptClass getConceptClass(ConceptClass old, Entity currentEntity) throws Exception {
		ConceptClass cc = old;
		try {
			String conceptClassName = currentEntity.getOf_type();
			cc = graph.getMetaData().getConceptClass(conceptClassName);
		} catch (Exception e) {

			ConceptClassMissingEvent so = new ConceptClassMissingEvent(
					currentEntity.getOf_type() + " missing.",
					"[Writer - getConceptClass]");
			Parser.fireEventOccurred(so);
            throw e;
		}

		return cc;
	}

	private DataSource getDataSource(DataSource old, Entity currentEntity) throws Exception {
		DataSource dataSource = old;
		try {
			String cvName = currentEntity.getElement_of();
			dataSource = graph.getMetaData().getDataSource(cvName);
		} catch (Exception e) {

			DataSourceMissingEvent so = new DataSourceMissingEvent(currentEntity
					.getElement_of()
					+ " missing.", "[Writer - getCV]");
			Parser.fireEventOccurred(so);
            throw e;
		}

		return dataSource;
	}

	private EvidenceType getEvidenceType(EvidenceType old, Entity currentEntity) throws Exception {
		EvidenceType et = old;
		try {
			String etName = currentEntity.getEvidence();
			et = graph.getMetaData().getEvidenceType(etName);
		} catch (Exception e) {

			EvidenceTypeMissingEvent so = new EvidenceTypeMissingEvent(
					currentEntity.getOf_type() + " missing.",
					"[Writer - getEvidenceType]");
			Parser.fireEventOccurred(so);
            throw e;
		}

		return et;
	}

}
