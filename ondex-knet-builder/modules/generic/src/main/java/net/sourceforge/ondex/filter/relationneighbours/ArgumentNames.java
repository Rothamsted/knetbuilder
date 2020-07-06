package net.sourceforge.ondex.filter.relationneighbours;

/**
 * 
 * @author hindlem
 *
 *
 */
public interface ArgumentNames {
	
	static final String DEPTH_ARG = "Depth";
	static final String DEPTH_ARG_DESC = "The Depth (distance from seed in relations) to apply the filter to";
	
	static final String SEEDCONCEPT_ARG = "ConceptID";
	static final String SEEDCONCEPT_ARG_DESC = "The Concept ID to seed the algorithm with";
	
	static final String CONCEPTCLASS_RESTRIC_ARG = "ConceptClassAtDepth";
	static final String CONCEPTCLASS_RESTRIC_ARG_DESC = "Restricts Concept Class at a given depth define by an array of pairs (depth1,cca;depth2,ccb...) when no concept class at a depth is specified the default is any allowed (depth refers to the relations from the seed and the concept tested is the leading connection to the edge)";

	static final String CONCEPTCV_RESTRIC_ARG = "ConceptCVAtDepth";
	static final String CONCEPTCV_RESTRIC_ARG_DESC = "Restricts Concept DataSource at a given depth define by an array of pairs (depth1,cva;depth2,cvb...) when no concept cv at a depth is specified the default is any allowed (depth refers to the relations from the seed and the concept tested is the leading connection to the edge)";

	static final String RELATIONTYPE_RESTRIC_ARG = "RelationTypeAtDepth";
	static final String RELATIONTYPE_RESTRIC_ARG_DESC = "Restricts RelationType at a given depth define by an array of pairs (depth1,rta;depth2,rtb...) when no relation type at a depth is specified the default is any allowed. A valid relation will contain the relation in its relation type set. Where possible define the RelationType, this will be faster.";

	static final String CONCEPTACC_ARG_DESC = "Concept accession to apply relation neighbours search to all matching concepts";
	static final String CONCEPTACC_ARG = "ConceptAccession";
	
	static final String CONCEPTACC_CV_ARG_DESC = "DataSource to look for Concept accession within";
	static final String CONCEPTACC_CV_ARG = "ConceptAccessionCV";
	
	static final String CONCEPTNAME_ARG_DESC = "Concept name to apply relation neighbours search to all matching concepts";
	static final String CONCEPTNAME_ARG = "ConceptName";
	
	static final String CONCEPTCLASS_ARG_DESC = "ConceptClass to look for Concepts within if accession or name has been specified";
	static final String CONCEPTCLASS_ARG = "ConceptClass";
	
}
