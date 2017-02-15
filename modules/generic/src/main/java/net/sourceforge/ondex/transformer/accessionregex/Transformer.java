package net.sourceforge.ondex.transformer.accessionregex;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

public class Transformer extends ONDEXTransformer implements ArgumentNames {

	@Override
	public String getId() {
		return "accessionregex";
	}

	@Override
	public String getName() {
		return "Accession RegEx";
	}

	@Override
	public String getVersion() {
		return "25.02.2011";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {

		return new ArgumentDefinition<?>[] {
				new StringArgumentDefinition(CC_ARG, CC_ARG_DESC, false, null,
						true),
				new StringArgumentDefinition(DS_ARG, DS_ARG_DESC, false, null,
						true),
				new StringArgumentDefinition(ACC_ARG, ACC_ARG_DESC, true, null,
						true),
				new StringArgumentDefinition(REGEX_ARG, REGEX_ARG_DESC, true,
						null, false),
				new StringArgumentDefinition(REPLACE_ARG, REPLACE_ARG_DESC,
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
							+ dsId + " not found.", "[Transformer - start]"));
				} else {
					// keep only concepts of data source
					concepts.addAll(graph.getConceptsOfDataSource(ds));
					fireEventOccurred(new GeneralOutputEvent(
							"Adding DataSource " + dsId,
							"[Transformer - start]"));
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
							"[Transformer - start]"));
				} else {
					// only keep concepts of concept class
					retain.addAll(graph.getConceptsOfConceptClass(cc));
					fireEventOccurred(new GeneralOutputEvent(
							"Adding ConceptClass " + ccId,
							"[Transformer - start]"));
				}
			}
		}

		// intersection between data source union and concept class union
		concepts.retainAll(retain);

		// collect accession elementOfs
		Set<DataSource> elementOfs = new HashSet<DataSource>();
		for (String typeId : args.getObjectValueList(ACC_ARG, String.class)) {
			DataSource elementOf = graph.getMetaData().getDataSource(typeId);
			if (elementOf == null) {
				fireEventOccurred(new DataSourceMissingEvent("DataSource "
						+ typeId + " not found.", "[Transformer - start]"));
				return;
			} else {
				elementOfs.add(elementOf);
				fireEventOccurred(new GeneralOutputEvent(
						"Adding Accession DataSource " + typeId,
						"[Transformer - start]"));
			}
		}

		String regex = (String) args.getUniqueValue(REGEX_ARG);
		String replace = (String) args.getUniqueValue(REPLACE_ARG);
		if (replace == null)
			replace = "";

		// process all concepts
		for (ONDEXConcept c : concepts) {
			ConceptAccession[] array = c.getConceptAccessions().toArray(
					new ConceptAccession[0]);
			for (ConceptAccession acc : array) {
				if (elementOfs.contains(acc.getElementOf())) {
					String accession = acc.getAccession();
					DataSource elementOf = acc.getElementOf();
					boolean ambiguous = acc.isAmbiguous();
					// delete old one and create new one
					c.deleteConceptAccession(accession, elementOf);
					accession = accession.replaceAll(regex, replace);
					c.createConceptAccession(accession, elementOf, ambiguous);
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
