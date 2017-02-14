package net.sourceforge.ondex.parser.brendasbml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.DataFileMissingEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.parser.ONDEXParser;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.xml.stax.SBMLReader;

@DatabaseTarget(name = "BRENDA", description = "Brenda enzymatics database", version = "BRENDA release online since 2nd July 2010", url = "http://www.brenda-enzymes.info/")
@Custodians(custodians = {"Jan Taubert"}, emails = {"jan.taubert at rothamsted.ac.uk"})
@Status(description = "tested on 28/07/2010", status = StatusType.STABLE) 
public class Parser extends ONDEXParser
{

	// associates a species id with a concept
	Map<String, ONDEXConcept> concepts = new HashMap<String, ONDEXConcept>();

	// global evidence type
	EvidenceType evidence = null;

	// data source is Brenda
	DataSource dataSourceBrenda = null;

	// data source for links to EC
	DataSource dataSourceEC = null;

	// class for reaction concepts
	ConceptClass ccReaction = null;

	// class for compound concepts
	ConceptClass ccComp = null;

	// class for enzyme concepts
	ConceptClass ccEnzyme = null;

	// type of consumed relation
	RelationType rtConsumed = null;

	// type of produced relation
	RelationType rtProduced = null;

	// type of catalyzed relation
	RelationType rtCatalyzed = null;

	// type of co-factored relation
	RelationType rtCofactored = null;

	// type of inhibited relation
	RelationType rtInhibited = null;

	// attribute of KI values
	AttributeName atKI = null;

	// attribute of KM values
	AttributeName atKM = null;

	// attribute of TN values
	AttributeName atTN = null;

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[] { new FileArgumentDefinition(
				FileArgumentDefinition.INPUT_FILE,
				FileArgumentDefinition.INPUT_FILE_DESC, true, true, false,
				false) };
	}

	@Override
	public String getId() {
		return "brendasbml";
	}

	@Override
	public String getName() {
		return "Brenda specific SBML Parser";
	}

	@Override
	public String getVersion() {
		return "15/06/2010";
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	@Override
	public void start() throws Exception {

		// get file to parse from arguments
		String filename = (String) args
				.getUniqueValue(FileArgumentDefinition.INPUT_FILE);

		// check file exists
		File file = new File(filename);
		if (!file.exists() && !file.canRead()) {
			fireEventOccurred(new DataFileMissingEvent(filename
					+ " was not found or cannot be read.", "[Parser - start]"));
			return;
		}

		evidence = graph.getMetaData().getEvidenceType("IMPD");
		if (evidence == null) {
			fireEventOccurred(new EvidenceTypeMissingEvent("IMPD missing",
					"[Parser - start]"));
			return;
		}

		dataSourceBrenda = graph.getMetaData().getDataSource("BRENDA");
		if (dataSourceBrenda == null) {
			fireEventOccurred(new DataSourceMissingEvent("BRENDA missing",
					"[Parser - start]"));
			return;
		}

		dataSourceEC = graph.getMetaData().getDataSource("EC");
		if (dataSourceEC == null) {
			fireEventOccurred(new DataSourceMissingEvent("EC missing",
					"[Parser - start]"));
			return;
		}

		ccReaction = graph.getMetaData().getConceptClass("Reaction");
		if (ccReaction == null) {
			fireEventOccurred(new ConceptClassMissingEvent("Reaction missing",
					"[Parser - start]"));
			return;
		}

		ccComp = graph.getMetaData().getConceptClass("Comp");
		if (ccComp == null) {
			fireEventOccurred(new ConceptClassMissingEvent("Comp missing",
					"[Parser - start]"));
			return;
		}

		ccEnzyme = graph.getMetaData().getConceptClass("Enzyme");
		if (ccEnzyme == null) {
			fireEventOccurred(new ConceptClassMissingEvent("Enzyme missing",
					"[Parser - start]"));
			return;
		}

		rtConsumed = graph.getMetaData().getRelationType("cs_by");
		if (rtConsumed == null) {
			fireEventOccurred(new RelationTypeMissingEvent("cs_by missing",
					"[Parser - start]"));
			return;
		}

		rtProduced = graph.getMetaData().getRelationType("pd_by");
		if (rtConsumed == null) {
			fireEventOccurred(new RelationTypeMissingEvent("pd_by missing",
					"[Parser - start]"));
			return;
		}

		rtCatalyzed = graph.getMetaData().getRelationType("ca_by");
		if (rtConsumed == null) {
			fireEventOccurred(new RelationTypeMissingEvent("ca_by missing",
					"[Parser - start]"));
			return;
		}

		rtCofactored = graph.getMetaData().getRelationType("co_by");
		if (rtCofactored == null) {
			fireEventOccurred(new RelationTypeMissingEvent("co_by missing",
					"[Parser - start]"));
			return;
		}

		rtInhibited = graph.getMetaData().getRelationType("in_by");
		if (rtInhibited == null) {
			fireEventOccurred(new RelationTypeMissingEvent("in_by missing",
					"[Parser - start]"));
			return;
		}

		atKI = graph.getMetaData().getAttributeName("KI");
		if (atKI == null) {
			fireEventOccurred(new AttributeNameMissingEvent("KI missing",
					"[Parser - start]"));
			return;
		}

		atKM = graph.getMetaData().getAttributeName("KM");
		if (atKM == null) {
			fireEventOccurred(new AttributeNameMissingEvent("KM missing",
					"[Parser - start]"));
			return;
		}

		atTN = graph.getMetaData().getAttributeName("KKAT");
		if (atTN == null) {
			fireEventOccurred(new AttributeNameMissingEvent("KKAT missing",
					"[Parser - start]"));
			return;
		}

		// read in SBML file
		SBMLDocument doc = SBMLReader.readSBML(filename);
		fireEventOccurred(new GeneralOutputEvent("Finished reading model.",
				"[Parser - start]"));

		// get model for file
		Model model = doc.getModel();

		// add species as concepts
		processSpecies(model.getListOfSpecies());
		fireEventOccurred(new GeneralOutputEvent("Finished parsing species.",
				"[Parser - start]"));

		// add reactions as relations
		processReactions(model.getListOfReactions());
		fireEventOccurred(new GeneralOutputEvent("Finished parsing reactions.",
				"[Parser - start]"));
	}

	private void addKinetics(Reaction r, ONDEXRelation relation, String id) {

		Map<AttributeName, Double> values = new HashMap<AttributeName, Double>();

		// process kinetic law parameters
		for (Parameter p : r.getKineticLaw().getListOfParameters()) {
			String pid = p.getId();
			boolean relevant = false;

			// check if parameter is linked with current species
			String specId = id.substring(id.indexOf("_"));
			if (pid.contains(specId)) {
				relevant = true;
			}
			// System.out.println(specId + " " + pid + " " + relevant);

			// add KI value as Attribute
			if (pid.startsWith("Ki_Value_")) {
				if (relevant) {
					if (values.containsKey(atKI)) {
						double d = values.get(atKI).doubleValue();
						if (d < p.getValue())
							values.put(atKI, Double.valueOf(p.getValue()));
					} else {
						values.put(atKI, Double.valueOf(p.getValue()));
					}
				}
			}

			// add KM value as Attribute
			else if (pid.startsWith("Km_Value_")) {
				if (relevant) {
					if (values.containsKey(atKM)) {
						double d = values.get(atKM).doubleValue();
						if (d < p.getValue())
							values.put(atKM, Double.valueOf(p.getValue()));
					} else {
						values.put(atKM, Double.valueOf(p.getValue()));
					}
				}
			}

			else if (pid.startsWith("pH_Optimum_KM_")) {

			}

			else if (pid.startsWith("Temperature_Optimum_KM_")) {

			}

			// add TN value as Attribute (KKAT)
			else if (pid.startsWith("Turnover_Number_")) {
				if (relevant) {
					if (values.containsKey(atTN)) {
						double d = values.get(atTN).doubleValue();
						if (d < p.getValue())
							values.put(atTN, Double.valueOf(p.getValue()));
					} else {
						values.put(atTN, Double.valueOf(p.getValue()));
					}
				}
			}

			else if (pid.startsWith("pH_Optimum_TN_")) {

			}

			else if (pid.startsWith("Temperature_Optimum_TN_")) {

			}

			// unknown / not yet implemented
			else {
				fireEventOccurred(new InconsistencyEvent(
						"unknown parameter type found in " + pid,
						"[Parser - addKinetics]"));
			}
		}

		// finally add found Attribute to relation
		for (AttributeName an : values.keySet()) {
			relation.createAttribute(an, values.get(an), false);
		}
	}

	private void addCompounds(Reaction r, SpeciesReference sp,
			ONDEXConcept reaction, RelationType rt) {

		// process all lists of reactants and products
		String id = sp.getSpecies();

		// this is compound which is consumed or produced by reaction
		ONDEXConcept fromConcept = concepts.get(id);
		if (fromConcept == null) {
			fireEventOccurred(new InconsistencyEvent(
					"fromConcept is empty for " + id,
					"[Parser - addSubstrates]"));
			return;
		}

		// create relation and add kinetics
		ONDEXRelation relation = graph.getFactory().createRelation(fromConcept,
				reaction, rt, evidence);
		addKinetics(r, relation, id);
	}

	private void addModifiers(Reaction r, ModifierSpeciesReference sp,
			ONDEXConcept reaction) {
		String id = sp.getSpecies();

		// this is either an enzyme or an compound
		ONDEXConcept toConcept = concepts.get(id);
		if (toConcept == null) {
			fireEventOccurred(new InconsistencyEvent("toConcept is empty for "
					+ id, "[Parser - processReactions]"));
			return;
		}

		ONDEXRelation relation = null;

		// a reaction is catalyzed by an enzyme
		if (id.startsWith("enz_")) {
			relation = graph.getFactory().createRelation(reaction, toConcept,
					rtCatalyzed, evidence);
		}

		// a reaction can be cofactored by an compound
		else if (id.startsWith("cof_")) {
			relation = graph.getFactory().createRelation(reaction, toConcept,
					rtCofactored, evidence);
		}

		// a reaction can be inhibited by an compound
		else if (id.startsWith("inh_")) {
			relation = graph.getFactory().createRelation(reaction, toConcept,
					rtInhibited, evidence);
		}

		// unknown type?
		else {
			fireEventOccurred(new InconsistencyEvent("unknown type found in "
					+ id, "[Parser - processReactions]"));
		}

		// add kinetics to relation
		if (relation != null) {
			addKinetics(r, relation, id);
		}
	}

	private void processReactions(ListOf<Reaction> list) {

		AttributeName atReversible = graph.getMetaData().getAttributeName(
				"reversible");
		if (atReversible == null) {
			atReversible = graph.getMetaData().getFactory()
					.createAttributeName("reversible", Boolean.class);
		}

		// process all reactions
		for (Reaction r : list) {

			// create concept for reaction itself
			String id = r.getId();
			ONDEXConcept reaction = graph.getFactory().createConcept(id,
					dataSourceBrenda, ccReaction, evidence);
			String name = r.getName();
			reaction.createConceptName(name, true);

			// add reversebility of reaction as Attribute
			reaction.createAttribute(atReversible, r.isReversible(), false);

			// add relations to substrates of reaction
			for (SpeciesReference sp : r.getListOfReactants()) {
				addCompounds(r, sp, reaction, rtConsumed);
			}

			// add relations to products of reaction
			for (SpeciesReference sp : r.getListOfProducts()) {
				addCompounds(r, sp, reaction, rtProduced);
			}

			// add relations of reaction modifiers
			for (ModifierSpeciesReference sp : r.getListOfModifiers()) {
				addModifiers(r, sp, reaction);
			}
		}
	}

	private void processSpecies(ListOf<Species> list) {

		// process all species
		for (Species s : list) {
			String id = s.getId();
			ONDEXConcept concept = null;
			// treat enzymes differently
			if (id.startsWith("enz_")) {
				concept = graph.getFactory().createConcept(id, dataSourceBrenda,
						ccEnzyme, evidence);
				// EC number is an ambiguous accession for an enzyme
				String ec = id.substring(7);
				ec = ec.replaceAll("_", ".");
				concept.createConceptAccession(ec, dataSourceEC, true);
			} else {
				concept = graph.getFactory().createConcept(id, dataSourceBrenda,
						ccComp, evidence);
			}
			// add preferred concept name
			String name = s.getName();
			concept.createConceptName(name, true);
			concepts.put(id, concept);
		}
	}

}
