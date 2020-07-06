package net.sourceforge.ondex.transformer.nameregex;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

/**
 * Modifies concept names using a given regular expression with replacement and
 * optionally copies result as a concept accession.
 * 
 * @author taubertj
 * 
 */
public class Transformer extends ONDEXTransformer implements ArgumentNames {

	@Override
	public String getId() {
		return "nameregex";
	}

	@Override
	public String getName() {
		return "Name RegEx";
	}

	@Override
	public String getVersion() {
		return "04.01.2013";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {

		return new ArgumentDefinition<?>[] {
				new StringArgumentDefinition(CC_ARG, CC_ARG_DESC, false, null,
						true),
				new StringArgumentDefinition(DS_ARG, DS_ARG_DESC, true, null,
						true),
				new StringArgumentDefinition(REGEX_ARG, REGEX_ARG_DESC, true,
						null, false),
				new StringArgumentDefinition(REPLACE_ARG, REPLACE_ARG_DESC,
						false, "", false),
				new StringArgumentDefinition(COPYAS_ARG, COPYAS_ARG_DESC,
						false, "", false) };
	}

	@Override
	public void start() throws Exception {

		Set<ONDEXConcept> concepts = new HashSet<ONDEXConcept>();

		for (String dsId : args.getObjectValueList(DS_ARG, String.class)) {
			if (dsId.trim().length() > 0) {
				DataSource ds = graph.getMetaData().getDataSource(dsId);
				if (ds == null) {
					fireEventOccurred(new DataSourceMissingEvent("DataSource "
							+ dsId + " not found.", getCurrentMethodName()));
				} else {
					// keep only concepts of data source
					concepts.addAll(graph.getConceptsOfDataSource(ds));
					fireEventOccurred(new GeneralOutputEvent(
							"Adding DataSource " + dsId, getCurrentMethodName()));
				}
			}
		}

		Set<ONDEXConcept> retain = new HashSet<ONDEXConcept>();
		for (String ccId : args.getObjectValueList(CC_ARG, String.class)) {
			if (ccId.trim().length() > 0) {
				ConceptClass cc = graph.getMetaData().getConceptClass(ccId);
				if (cc == null) {
					fireEventOccurred(new ConceptClassMissingEvent(
							"ConceptClass " + ccId + " not found.",
							getCurrentMethodName()));
				} else {
					// only keep concepts of concept class
					retain.addAll(graph.getConceptsOfConceptClass(cc));
					fireEventOccurred(new GeneralOutputEvent(
							"Adding ConceptClass " + ccId,
							getCurrentMethodName()));
				}
			}
		}

		// intersection between data source union and concept class union
		if (retain.size() > 0)
			concepts.retainAll(retain);

		// get data source
		String dsID = (String) args.getUniqueValue(COPYAS_ARG);
		DataSource ds = null;
		if (dsID != null && dsID.trim().length() > 0) {
			ds = graph.getMetaData().getDataSource(dsID);
			if (ds == null)
				ds = graph.getMetaData().getFactory().createDataSource(dsID);
		}

		// get regex
		String regex = (String) args.getUniqueValue(REGEX_ARG);
		String replace = (String) args.getUniqueValue(REPLACE_ARG);
		if (replace == null)
			replace = "";

		// process all concepts
		for (ONDEXConcept c : concepts) {
			ConceptName[] array = c.getConceptNames().toArray(
					new ConceptName[0]);
			for (ConceptName cn : array) {
				String oldname = cn.getName();
				boolean preferred = cn.isPreferred();
				// delete old one and create new one
				c.deleteConceptName(oldname);
				String name = oldname.replaceAll(regex, replace).trim();
				// check if new name is not empty
				if (name.length() > 0) {
					if (c.getConceptName(name) != null) {
						c.getConceptName(name).setPreferred(preferred);
					} else {
						c.createConceptName(name, preferred);
					}
					// if data source is specified create concept accession
					if (ds != null && oldname.matches(regex)
							&& c.getConceptAccession(name, ds) == null) {
						c.createConceptAccession(name, ds, false);
					}
				} else {
					// revert back if new name would be empty
					c.createConceptName(oldname, preferred);
				}
			}
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
