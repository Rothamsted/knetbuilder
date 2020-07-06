package net.sourceforge.ondex.export.oxl;

/**
 * Contains static String content for XML tag names in oxl parser.
 * 
 * @author taubertj
 * 
 */
public final class XMLTagNames {

	// OXL version number
	public final static String VERSION = "version";

	// info type with general statistics and information about the graph
	public final static String INFO = "info";

	// part of info type
	public final static String NUMBERCONCEPTS = "numberOfConcepts";

	// part of info type
	public final static String NUMBERRELATIONS = "numberOfRelations";

	// part of info type
	public final static String GRAPHNAME = "graphName";

	// part of info type
	public final static String GRAPHANNOTATIONS = "graphAnnotations";

	// part of info type
	public final static String GRAPHANNOTATION = "graphAnnotation";

	// part of info type
	public final static String GRAPHANNOTATIONKEY = "key";

	public final static String ID = "id";

	public final static String ID_REF = "idRef";

	public final static String FULLNAME = "fullname";

	public final static String DESCRIPTION = "description";

	public final static String ANNOTATION = "annotation";

	public final static String SPECIALISATIONOF = "specialisationOf";

	public final static String ELEMENTOF = "elementOf";

	public final static String OFTYPE = "ofType";

	public final static String CONCEPTACCESSION = "concept_accession";

	public final static String ACCESSION = "accession";

	public final static String AMBIGUOUS = "ambiguous";

	public final static String VALUE = "value";

	public final static String LITERAL = "literal";

	public final static String DOINDEX = "doindex";

	public final static String EVIDENCE = "evidence";

	public final static String INVERSENAME = "inverseName";

	public final static String ISANTISYMMETRIC = "isAntisymmetric";

	public final static String ISREFLEXTIVE = "isReflexive";

	public final static String ISSYMMETRIC = "isSymmetric";

	public final static String ISTRANSITIVE = "isTransitive";

	public final static String ONDEXDATASEQ = "ondexdataseq";

	// contained within ONDEXDATASEQ tag
	public final static String CONCEPTS = "concepts";

	// contained within ONDEXDATASEQ tag
	public final static String RELATIONS = "relations";

	// CONCEPT GDSs
	public final static String COGDS = "cogds";

	// RELATION GDSs
	public final static String RELGDS = "relgds";

	// context list
	public final static String CONTEXT = "context";

	// new contexts list design
	public final static String CONTEXTS = "contexts";

	public final static String COACCESSIONS = "coaccessions";

	public final static String CONAMES = "conames";

	public final static String FROMCONCEPT = "fromConcept";

	public final static String TOCONCEPT = "toConcept";

	public final static String QUALIFIER = "qualifier";

	public final static String ONDEXMETADATA = "ondexmetadata";

	// contained in ONDEXMETADATA
	public final static String CVS = "cvs";

	// contained in ONDEXMETADATA
	public final static String UNITS = "units";

	// contained in ONDEXMETADATA
	public final static String ATTRIBUTENAMES = "attrnames";

	// contained in ONDEXMETADATA
	public final static String EVIDENCES = "evidences";

	// contained in ONDEXMETADATA
	public final static String CONCEPTCLASSES = "conceptclasses";

	// contained in ONDEXMETADATA
	public final static String RELATIONTYPES = "relationtypes";

	public final static String PID = "pid";

	public final static String CONCEPT = "concept";

	public final static String RELATION = "relation";

	public final static String CC = "cc";

	public final static String CV = "cv";

	public final static String CONCEPTGDS = "concept_gds";

	public final static String RELATIONGDS = "relation_gds";

	public final static String RELATIONTYPE = "relation_type";

	// unit of the object
	public final static String UNIT = "unit";

	public final static String ATTRIBUTENAME = "attrname";

	public final static String CONCEPTNAME = "concept_name";

	public final static String NAME = "name";

	public final static String ISPREFERRED = "isPreferred";

	public final static String DATATYPE = "datatype";

	public static final String JAVA_CLASS = "java_class";
}
