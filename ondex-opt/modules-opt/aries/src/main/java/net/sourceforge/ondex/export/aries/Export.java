package net.sourceforge.ondex.export.aries;

import java.util.Set;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.export.ONDEXExport;

public class Export extends ONDEXExport {

	@Override
	public String getId() {
		return "aries";
	}

	@Override
	public String getName() {
		return "Aries Export";
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

		// get all concepts from graph
		Set<ONDEXConcept> concepts = graph.getConcepts();
		System.out.println(concepts);
		for (ONDEXConcept c : concepts) {
			System.out.println(c);
			
			// print all concept names
			for (ConceptName cn : c.getConceptNames()) {
				System.out.println(cn.getName());
			}

			// get all relations of concept
			System.out.println("Neighbours:");
			Set<ONDEXRelation> relations = graph.getRelationsOfConcept(c);
			for (ONDEXRelation r : relations) {
				if (r.getFromConcept().equals(c)) {
					System.out.println(r.getToConcept());
				} else {
					System.out.println(r.getFromConcept());
				}
			}
		}

		// get only concepts of concept class
		ConceptClass conceptClass = graph.getMetaData().getConceptClass(
				"Protein");
		System.out.println(conceptClass);
		AttributeName an = graph.getMetaData().getAttributeName("AA");
		concepts = graph.getConceptsOfConceptClass(conceptClass);
		for (ONDEXConcept c : concepts) {
			// might be null if no attribute
			Attribute attr = c.getAttribute(an);
			if (attr != null)
				System.out.println(attr.getValue());
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
