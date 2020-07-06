package net.sourceforge.ondex.filter.conceptclassneighbours;

/**
 * 
 * @author hindlem
 *
 *
 */
public interface ArgumentNames {
	
	static final String DEPTH_ARG = "Depth";
	static final String DEPTH_ARG_DESC = "The Depth (distance from seed in relations) to apply the filter to";
	
	static final String SEEDCONCEPTS_ARG = "ConceptClass";
	static final String SEEDCONCEPTS_ARG_DESC = "The Concepts of ConceptClass seed the algorithm with";
	
	static final String CONCEPTCLASS_RESTRIC_ARG = "ConceptClassAtDepth";
	static final String CONCEPTCLASS_RESTRIC_ARG_DESC = "Restricts Concept Class at a given depth define by an array of pairs (depth1,cca;depth2,ccb...) when no concept class at a depth is specified the default is any allowed (depth refers to the relations from the seed and the concept tested is the leading connection to the edge)";

	static final String CONCEPTCV_RESTRIC_ARG = "ConceptCVAtDepth";
	static final String CONCEPTCV_RESTRIC_ARG_DESC = "Restricts Concept DataSource at a given depth define by an array of pairs (depth1,cva;depth2,cvb...) when no concept cv at a depth is specified the default is any allowed (depth refers to the relations from the seed and the concept tested is the leading connection to the edge)";

	static final String RELATIONTYPE_RESTRIC_ARG = "RelationTypeAtDepth";
	static final String RELATIONTYPE_RESTRIC_ARG_DESC = "Restricts RelationType at a given depth define by an array of pairs (depth1,rta;depth2,rtb...) when no relation type at a depth is specified the default is any allowed. A valid relation will contain the relation in its relation type set. Where possible define the RelationType, this will be faster.";

	static final String RELATIONTYPESET_RESTRIC_ARG = "RelationTypeAtDepth";
	static final String RELATIONTYPESET_RESTRIC_ARG_DESC = "Restricts RelationType at a given depth define by an array of pairs (depth1,rtsa;depth2,rtsb...) when no concept class at a depth is specified the default is any allowed";
	
}
