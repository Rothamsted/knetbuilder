package net.sourceforge.ondex.filter.shortestpath;

/**
 * @author Jochen Weile
 *
 */
public interface ArgumentNames {
	
	static final String SEEDCONCEPT_ARG = "StartConceptID";
	static final String SEEDCONCEPT_ARG_DESC = "The Concept ID that starts the path";
	
//	static final String CONCEPTCLASS_ARG = "ConceptClass";
//	static final String CONCEPTCLASS_ARG_DESC = "The ConceptClass that terminates the path";
	
	static final String USEWEIGHTS_ARG = "UseWeights";
	static final String USEWEIGHTS_ARG_DESC = "Use attribute values as edge weights?";
	
	static final String WEIGHTATTRIBUTENAME_ARG = "Attributeeight";
	static final String WEIGHTATTRIBUTENAME_ARG_DESC = "The name of the Attribute type to be used as edge weights";
	
	static final String ONLYDIRECTED_ARG = "OnlyDirectedEdges";
	static final String ONLYDIRECTED_ARG_DESC = "Follow edges only according to their direction?";
	
	static final String INVERSE_WEIGHT_ARG = "InverseWeight";
	static final String INVERSE_WEIGHT_ARG_DESC = "Takes the inverse of the Attribute value as the weight. This is for probabilities or scores.";

}
