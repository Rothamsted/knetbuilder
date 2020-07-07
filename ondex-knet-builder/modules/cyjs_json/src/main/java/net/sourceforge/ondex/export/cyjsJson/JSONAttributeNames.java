package net.sourceforge.ondex.export.cyjsJson;

/**
 * Static String content for JSON Attribute names, used when retrieving values of attributes from the 
 * ONDEXGraph object and writing them as attribute-value pairs in the JSON object.
 * @author Ajit Singh
 * @version 08/07/15
 */
public final class JSONAttributeNames {

	// part of metadata.
	public final static String GRAPHNAME = "graphName";

	// Exporter Plugin version number
	public final static String VERSION = "version";

	// part of metadata.
	public final static String NUMBERCONCEPTS = "numberOfConcepts";

	// part of metadata.
	public final static String NUMBERRELATIONS = "numberOfRelations";

	// part of metadata.
	public final static String ONDEXMETADATA = "ondexmetadata";

	public final static String CONCEPTS = "concepts";

	public final static String CONCEPT = "concept";

	public final static String ID = "id";

	public final static String VALUE = "value";

	public final static String PID = "pid";

	public final static String ANNOTATION = "annotation";

	// contained in ONDEXMETADATA
	public final static String DESCRIPTION = "description";

	public final static String ELEMENTOF = "elementOf";

	public final static String FULLNAME = "fullname";

	public final static String OFTYPE = "ofType";

	public final static String EVIDENCES = "evidences";

	public final static String EVIDENCE = "evidence";

	public final static String CONAMES = "conames";

	public final static String CONCEPTNAME = "concept_name";

	public final static String NAME = "name";

	public final static String ISPREFERRED = "isPreferred";

	public final static String COACCESSIONS = "coaccessions";

	public final static String CONCEPTACCESSION = "concept_accession";

	public final static String ACCESSION = "accession";

	public final static String AMBIGUOUS = "ambiguous";

	// CONCEPT GDSs
	public final static String COATTRIBUTE= "attribute"; // "cogds";

	public final static String CONCEPTATTRIBUTES= "attributes"; // "concept_gds";

	public final static String ATTRIBUTENAME = "attrname";

	// contained in ONDEXMETADATA
	public final static String UNITS = "units";

	// unit of the object
	public final static String UNIT = "unit";

	public final static String DATATYPE = "datatype";

	public final static String SPECIALISATIONOF = "specialisationOf";

	public static final String JAVA_CLASS = "java_class";

	public final static String DOINDEX = "doindex";

	// new contexts list design
	public final static String CONTEXTS = "contexts";

	// context list
	public final static String CONTEXT = "context";

	// contained in metadata.
	public final static String RELATIONS = "relations";

	public final static String RELATION = "relation";

	public final static String FROMCONCEPT = "fromConcept";

	public final static String TOCONCEPT = "toConcept";

	public final static String INVERSENAME = "inverseName";

	public final static String ISANTISYMMETRIC = "isAntisymmetric";

	public final static String ISREFLEXTIVE = "isReflexive";

	public final static String ISSYMMETRIC = "isSymmetric";

	public final static String ISTRANSITIVE = "isTransitive";

	// RELATION GDSs
	public final static String RELGDS = "attributes"; // "relgds";

	public final static String RELATIONGDS = "attribute"; // "relation_gds";

/*	// contained in metadata.
	public final static String GRAPHANNOTATIONS = "graphAnnotations";

	// contained in metadata.
	public final static String GRAPHANNOTATION = "graphAnnotation";

	// contained in metadata.
	public final static String GRAPHANNOTATIONKEY = "key";

	public final static String LITERAL = "literal";

	public final static String ONDEXDATASEQ = "ondexdataseq";

	// Data Sources; contained in ONDEXMETADATA
	public final static String CVS = "cvs";

	public final static String CV = "cv";

	// contained in ONDEXMETADATA
	public final static String CONCEPTCLASSES = "conceptclasses";

	public final static String CC = "cc";

	// contained in ONDEXMETADATA
	public final static String ATTRIBUTENAMES = "attrnames";

	// contained in ONDEXMETADATA
	public final static String RELATIONTYPES = "relationtypes";

	public final static String RELATIONTYPE = "relation_type";
*/
}
