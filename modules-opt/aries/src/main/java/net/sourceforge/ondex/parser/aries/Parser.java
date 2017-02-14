package net.sourceforge.ondex.parser.aries;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.parser.ONDEXParser;

public class Parser extends ONDEXParser {

	@Override
	public String getId() {
		return "aries";
	}

	@Override
	public String getName() {
		return "Aries parser";
	}

	@Override
	public String getVersion() {
		return "14.08.2012";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[0];
	}

	@Override
	public void start() throws Exception {

		ONDEXGraphMetaData md = graph.getMetaData();
		
		// get meta dat for first concept
		ConceptClass ofType = md.getConceptClass("EC");
		DataSource elementOf = md.getDataSource("EC");
		EvidenceType evidence = md.getEvidenceType("IMPD");
		
		// create first concept
		ONDEXConcept c1 = graph.getFactory().createConcept("PID", elementOf, ofType, evidence);
		c1.createConceptAccession("1.1.1.1", elementOf, false);
		
		// get meta data for second concept
		ConceptClass conceptClass = md.getConceptClass("Protein");
		
		// create second concept
		ONDEXConcept c2 = graph.getFactory().createConcept("", elementOf, conceptClass, evidence);
		c2.createConceptName("A protein", true);
		
		// meta dat for attribute
		AttributeName an = md.getAttributeName("AA");
		c2.createAttribute(an, "ACGTACGTACGTATCAGACTGCATACGCTAACT", false);
		
		// get meta data for relation
		RelationType rt = md.getRelationType("cat_c");
		
		// create a new relation
		graph.getFactory().createRelation(c2, c1, rt, evidence);
		
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

}
