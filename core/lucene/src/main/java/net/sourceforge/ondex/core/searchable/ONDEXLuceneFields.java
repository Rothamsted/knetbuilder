package net.sourceforge.ondex.core.searchable;

/**
 * Static references for Fields used to build the Lucene index
 * @author hindlem
 *
 */
public interface ONDEXLuceneFields {

	//some static constants
	static final char DELIM = '_';
	
	static final char AMBIGUOUS = 'a';
	
	static final String SPACE = " ";
	
	// definition of all constants used
	static final String FROM_FIELD = "FromConcept";

	static final String TO_FIELD = "ToConcept";

	static final String OFTYPE_FIELD = "OfType";

	static final String RELATTRIBUTE_FIELD = "RelationAttribute";

	static final String CONATTRIBUTE_FIELD = "ConceptAttribute";

	static final String CONNAME_FIELD = "ConceptName";

	static final String CONACC_FIELD = "ConceptAccession";

	static final String DESC_FIELD = "Description";

	static final String ANNO_FIELD = "Annotation";

	static final String DataSource_FIELD = "DataSource";

	static final String CC_FIELD = "ConceptClass";

	static final String PID_FIELD = "PID";

	static final String CONID_FIELD = "ConceptID";

	static final String RELID_FIELD = "RelationID";
	
}
