package net.sourceforge.ondex.filter.allpairs;

/**
 * @author Jochen Weile, B.Sc.
 *
 */
public interface ArgumentNames {
	
	static final String WEIGHTATTRIBUTENAME_ARG = "AttributeWeight";
	static final String WEIGHTATTRIBUTENAME_ARG_DESC = "The name of the Attribute type to be used as edge weights";
	
	static final String ONLYDIRECTED_ARG = "OnlyDirectedEdges";
	static final String ONLYDIRECTED_ARG_DESC = "Follow edges only according to their direction?";
	
	static final String INVERSE_WEIGHT_ARG = "InverseWeight";
	static final String INVERSE_WEIGHT_ARG_DESC = "Takes the inverse of the Attribute value as the weight. This is for probabilities or scores.";

	
}
