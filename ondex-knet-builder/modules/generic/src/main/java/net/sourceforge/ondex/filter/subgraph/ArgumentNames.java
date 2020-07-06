package net.sourceforge.ondex.filter.subgraph;

public interface ArgumentNames {

	static final String ROOT_ARG = "ConceptID";
	static final String ROOT_ARG_DESC = "The root concept to start at.";
	
	static final String FIRST_CC_ARG = "FirstConceptClass";
	static final String FIRST_CC_ARG_DESC = "ConceptClass limitation for depth 1.";
	
	static final String FIRST_RT_ARG = "FirstRelationType";
	static final String FIRST_RT_ARG_DESC = "RelationType limitation for depth 1.";
	
	static final String FIRST_RD_ARG = "FirstRelationDirection";
	static final String FIRST_RD_ARG_DESC = "Direction of relation for depth 1, one of [both, incoming, outgoing].";
	
	static final String SECOND_CC_ARG = "SecondConceptClass";
	static final String SECOND_CC_ARG_DESC = "ConceptClass limitation for depth 2.";
	
	static final String SECOND_RT_ARG = "SecondRelationType";
	static final String SECOND_RT_ARG_DESC = "RelationType limitation for depth 2.";
	
	static final String SECOND_RD_ARG = "SecondRelationDirection";
	static final String SECOND_RD_ARG_DESC = "Direction of relation for depth 2, one of [both, incoming, outgoing].";
	
	static final String THIRD_CC_ARG = "ThirdConceptClass";
	static final String THIRD_CC_ARG_DESC = "ConceptClass limitation for depth 3.";
	
	static final String THIRD_RT_ARG = "ThirdRelationType";
	static final String THIRD_RT_ARG_DESC = "RelationType limitation for depth 3.";
	
	static final String THIRD_RD_ARG = "ThirdRelationDirection";
	static final String THIRD_RD_ARG_DESC = "Direction of relation for depth 3, one of [both, incoming, outgoing].";
	
	static final String FORTH_CC_ARG = "ForthConceptClass";
	static final String FORTH_CC_ARG_DESC = "ConceptClass limitation for depth 4.";
	
	static final String FORTH_RT_ARG = "ForthRelationType";
	static final String FORTH_RT_ARG_DESC = "RelationType limitation for depth 4.";
	
	static final String FORTH_RD_ARG = "ForthRelationDirection";
	static final String FORTH_RD_ARG_DESC = "Direction of relation for depth 4, one of [both, incoming, outgoing].";
	
	static final String FIFTH_CC_ARG = "FifthConceptClass";
	static final String FIFTH_CC_ARG_DESC = "ConceptClass limitation for depth 5.";
	
	static final String FIFTH_RT_ARG = "FifthRelationType";
	static final String FIFTH_RT_ARG_DESC = "RelationType limitation for depth 5.";
	
	static final String FIFTH_RD_ARG = "FifthRelationDirection";
	static final String FIFTH_RD_ARG_DESC = "Direction of relation for depth 5, one of [both, incoming, outgoing].";


}
