package net.sourceforge.ondex.export.cyjsJson;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.GraphLabelsUtils;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.export.ONDEXExport;

/**
 * Builds JSON files to be used with CytoscapeJS using data from ONDEX concepts, relations and other 
 * important Item Info. The concepts and relations' information used for generating the Network Graph is 
 * stored in a 'graphJSON' object & all the metadata is stored in an 'allGraphData' object, both of 
 * which are generated timestamped 'result' JSON file.
 * @author Ajit Singh
 * @version 14/10/2015
 */
@SuppressWarnings("unchecked")
@Status(description = "stable", status = StatusType.STABLE)
@Authors(authors = { "Ajit Singh" }, emails = { "ajit.singh at rothamsted.ac.uk" })
@Custodians(custodians = { "Ajit Singh" }, emails = { "ajit.singh at rothamsted.ac.uk" })
public class Export extends ONDEXExport {

    private FileWriter graphFileWriter= null;
    
    // current version of the cytoscapeJS JSON Exporter Plugin.
    public static final String version = "1.0";

    // line delimiter
    private final String newline= "\n";

    /**
     * Builds a file with Ondex Graph data exported to JSON variables for concepts (nodes), relations (edges)
     * and other Item Info.
     * 
     * @throws IOException
     * @throws InvalidPluginArgumentException
     */
	@Override
	public void start () throws IOException, InvalidPluginArgumentException {

		setOptionalArguments ();

		fireEventOccurred (new GeneralOutputEvent ( "Ready to Export.", "[Export - start]" ) );

		// A jsimple-json object (JSONObject) for the node, edge data for cytoscapeJS.
		JSONObject graphJson = new JSONObject ();

		/*
		 * Another jsimple-json object (JSONObject) to store all graph data (including
		 * metadata, concepts & relations' information).
		 */
		JSONObject allDataJson = new JSONObject ();

		// JSONArray objects to store all the ceoncepts (nodes) & relations (edges) to.
		JSONArray conceptNodes = new JSONArray ();
		JSONArray relationEdges = new JSONArray ();

		// Set output File location for writing network graph JSON data & other graph
		// metadata to.
		graphFileWriter = getOutputFileForGraphJson ();

		try {
			// Retrieving all the concepts & relations from the graph (the ONDEXGraph
			// object).
			if (graph != null) {
				concepts = graph.getConcepts ();
				relations = graph.getRelations ();
			}

			// Generate all graph metadata in JSON format.
			JSONObject allGraphDataJson = getJsonMetadata ();
			allDataJson.put ( JSONAttributeNames.ONDEXMETADATA, allGraphDataJson );

			// Generate necessary node(s) data in JSON format for CytoscapeJS graph.
			graphJson = getNodesJsonData ( graphJson, conceptNodes, relations );

			// Generate necessary edge(s) data in JSON format for CytoscapeJS graph.
			graphJson = getEdgesJsonData ( graphJson, relationEdges );

			// Write graph JSON & metadata JSON to output file.
			writeJSONToFile ( graphJson, allDataJson, graphFileWriter );
		} catch ( Exception ex ) {
			throw new IOException ( "Failed to write Attribute values", ex );
		}

		System.out.println ( "JSON export completed..." );
		fireEventOccurred (new GeneralOutputEvent ( "Finished JSON Export.", "[Export - start]" ) );
	}

	/**
	 * Builds a Document according to specifications in the ExportArguments
	 */
	private void setOptionalArguments() throws IOException, InvalidPluginArgumentException {

		if (graph == null && (concepts == null || relations == null))
		    throw new NullPointerException("Error: Ondex graph not set for export");

	}

    /**
     * @return String (Plugin ID)
     */
    @Override
    public String getId() {
     return "cyjs_json";
    }

    /**
     * @return String (Plugin name)
     */
    @Override
    public String getName() {
     return "CytoscapeJS JSON Export";
    }

    /**
     * @return String (Plugin version)
     */
    @Override
    public String getVersion() {
     return "07/04/2015";
    }

    /**
     * @return Argument Definitions for this Exporter Plugin (in an ArgumentDefinition[] array).
     */
    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
     return new ArgumentDefinition[] {
         new FileArgumentDefinition(FileArgumentDefinition.EXPORT_FILE, "JSON file to export to", 
             true, false, false, false)
     };
    }

    /**
     * No indexed graph required.
     * @return false
     */
    @Override
    public boolean requiresIndexedGraph() {
     return false;
    }

    /**
     * @return Validators String[] array.
     */
    @Override
    public String[] requiresValidators() {
     return new String[0];
    }

	/**
	 * Returns an Output Stream (i.e., output file location to write the JSON data
	 * out to).
	 * 
	 * @param name output json file name
	 * @return FileWriter object for destination (output) file.
	 */
	private FileWriter getOutputFileForGraphJson () {

		FileWriter fw = null;
		try {
			// Get output (export) file name from arguments.
			String outputFileName = ( ( String ) args.getUniqueValue(FileArgumentDefinition.EXPORT_FILE ) ).trim ();
			fw = new FileWriter ( outputFileName );
		} catch ( Exception e ) {
			e.printStackTrace ();
		}
		return fw;
	}

    /** 
     * Generate node(s) data in JSON format.
     * @param conceptNodes
     *            JSONArray object to store the required node information (in various individual JSONObjects).
     * @param graphJson
     *            JSONObject to put the JSONArray in.
     * @return A JSONObject containing information about all the nodes.
     */
    private JSONObject getNodesJsonData(JSONObject graphJson, JSONArray conceptNodes, Set<ONDEXRelation> allRelations) {
     /** Pass a Set containing all the Concepts that are used as source or target in a Relation. 
      * This is used to toggle the default visibility of a particular Concept. */
     Set<Integer> conceptsUsedInRelations= new HashSet<Integer>();
     for(ONDEXRelation rel : allRelations) {
         conceptsUsedInRelations.add(rel.getFromConcept().getId());
         conceptsUsedInRelations.add(rel.getToConcept().getId());
        }

     AddConceptNodeInfo anci= new AddConceptNodeInfo();
     for(ONDEXConcept con : concepts) {
         // generate, return & store concept/ node data.
         conceptNodes.add(anci.getNodeJson(con, conceptsUsedInRelations)); // add the returned node to the JSONArray.
        }
     System.out.println("\n");

     graphJson.put("nodes", conceptNodes); // add the "nodes" array to the JSON object.

     return graphJson;
    }

    /** 
     * Generate edge(s) data in JSON format.
     * @param relationEdges
     *            JSONArray object to store the required edge information (in various individual JSONObjects).
     * @param graphJson
     *            JSONObject to put the JSONArray in.
     * @return A JSONObject containing information about all the edges.
     */
    private JSONObject getEdgesJsonData(JSONObject graphJson, JSONArray relationEdges) {

     AddRelationEdgeInfo arei= new AddRelationEdgeInfo();
     for(ONDEXRelation rel : relations) {
         // generate, return & store relation/ edge data.
         relationEdges.add(arei.getEdgeJson(rel)); // add the returned edge to the JSONArray.
        }
     graphJson.put("edges", relationEdges); // add the "edges" array to the JSON object.

     return graphJson;
    }

	/**
	 * Generate metadata object in JSON format.
	 * 
	 * @return A JSONObject containing information about all concepts, relations &
	 *         metadata.
	 */
	private JSONObject getJsonMetadata () {
		JSONObject mdJson = new JSONObject ();

		// Write graph name in the metadata (allGraphDataJson) JSONObject.
		mdJson.put ( JSONAttributeNames.GRAPHNAME, graph.getName () ); // graph name

		// Write JSON Exporter Plugin version in the JSONObject.
		mdJson.put ( JSONAttributeNames.VERSION, version );

		// Write the number of concepts in the JSONObject.
		mdJson.put ( JSONAttributeNames.NUMBERCONCEPTS, concepts.size () );

		// Write the number of relations in the JSONObject.
		mdJson.put ( JSONAttributeNames.NUMBERRELATIONS, relations.size () );

		// Build JSON for all the Concepts.
		JSONArray conceptsJsonArray = new JSONArray ();
		for ( ONDEXConcept con : concepts ) {
			conceptsJsonArray.add ( buildConcept ( con ) );
		}
		mdJson.put ( JSONAttributeNames.CONCEPTS, conceptsJsonArray );

		// Build JSON for all the Relations.
		JSONArray relationsJsonArray = new JSONArray ();
		for ( ONDEXRelation rel : relations ) {
			relationsJsonArray.add ( buildRelation ( rel ) );
		}
		mdJson.put ( JSONAttributeNames.RELATIONS, relationsJsonArray );

		return mdJson;
	}

    /** 
     * Write to output file.
     * @param outputFile
     *            The output file that gets written out.
     * @param allDataJson
     *            JSONObject containing all the metadata, concepts & relations information.
     * @param graphJson
     *            JSONObject containing the node (concept) & edge (relation) information used by CytoscapeJS.
     */
	private void writeJSONToFile (JSONObject graphJson, JSONObject metadataJson, FileWriter outputFile ) {

		String graphData = graphJson.toString ();
		String allData = metadataJson.toString ();

		// Format the created JSON String objects.
		graphData = formatJson ( graphData );
		allData = formatJson ( allData );

		// Displaying contents of the generated JSON object.
		// System.out.println("graphJson= "+ graphData +"\n \n"+ "metadataJson= "+ allData);
		try {
			// Write the JSON object to be used for displaying the nodes & edges using
			// CytoscapeJS.
			outputFile.write ( "var graphJSON= " );
			outputFile.write ( /* graphJson.toString() */graphData ); // write necessary concepts & relations data to file
			outputFile.write ( ";" );

			// Write all graph data (including metadata, concepts & relations' information).
			outputFile.write ( newline );
			outputFile.write ( newline );
			outputFile.write ( "var allGraphData= " );
			outputFile.write ( /* allDataJson.toString() */allData ); // write all graph data to file
			outputFile.write ( ";" );

			outputFile.flush ();
			outputFile.close ();
		} catch ( IOException e ) {
			e.printStackTrace ();
		}

	}

	/**
	 * Generate metadata for ONDEXConcept.
	 * 
	 * @param con An ONDEXConcept (from the Ondex API).
	 * @return JSONObject JSONObject containing information about the Concept.
	 */
	private JSONObject buildConcept ( ONDEXConcept con ) {
		JSONObject conceptJson = new JSONObject ();

		conceptJson.put ( JSONAttributeNames.ID, String.valueOf ( con.getId () ) ); // concept ID.

		/*
		 * Concept Type (details returned in another JSON object). Now, uses "ofType" as
		 * key instead of "ConceptClasses" & "CC".
		 */
		String conceptType = buildConceptClass ( con.getOfType () );

		conceptJson.put ( JSONAttributeNames.VALUE, GraphLabelsUtils.getBestConceptLabel ( con ) ); // preferred concept name.
		conceptJson.put ( JSONAttributeNames.OFTYPE, conceptType );
		conceptJson.put ( JSONAttributeNames.PID, con.getPID () );
		conceptJson.put ( JSONAttributeNames.ANNOTATION, con.getAnnotation ().replaceAll ( "(\\r|\\n)", " " ) );
		conceptJson.put ( JSONAttributeNames.DESCRIPTION, con.getDescription ());

		/**
		 * Element Of (parent Data Source details returned in another JSON object). Now,
		 * uses "ElementOf" as key instead of "CVS" & "CV".
		 */
		conceptJson.put ( JSONAttributeNames.ELEMENTOF, buildDataSource ( con.getElementOf () ) );

		// Evidence Types.
		Set<EvidenceType> evidenceTypes = con.getEvidence ();
		JSONArray evidencesArray = new JSONArray (); // fetch array of concept attributes.
		for ( EvidenceType et : evidenceTypes ) {
			// return various JSON objects for each Evidence Type.
			evidencesArray.add ( buildEvidenceType ( et ) ); // add to evidences array.
		}
		conceptJson.put( JSONAttributeNames.EVIDENCES, evidencesArray/* evidencesJson */ );

		// Get all Concept Names (conames), whether preferred or not.
		Set<ConceptName> conames = con.getConceptNames ();
		JSONArray concept_names_Array = new JSONArray ();
		for ( ConceptName coname : conames ) {
			// return various JSON objects for each Concept Name.
			concept_names_Array.add ( buildConceptName ( coname ) );
		}
		conceptJson.put ( JSONAttributeNames.CONAMES, concept_names_Array/* conamesJson */ );

		// Concept Accessions.
		Set<ConceptAccession> accessions = con.getConceptAccessions ();
		JSONArray accessionsArray = new JSONArray (); // fetch array of concept attributes.
		for ( ConceptAccession accession : accessions ) {
			// return various JSON objects for each Concept Accession.
			accessionsArray.add ( buildConceptAccession ( accession ) ); // add to co-accessions array.
		}
		conceptJson.put ( JSONAttributeNames.COACCESSIONS, accessionsArray/* accessionsJson */ );

		/*
		 * Concept GDS (Attributes), Attribute Names, Units & SpecialisationOf. Now,
		 * uses "attributes" & "attribute" instead of using older terminology: "cogds" &
		 * "concept_gds". These are now used as Keys in addition to older terms like
		 * "attrnames" and "attrname".
		 */
		Set<Attribute> attributes = con.getAttributes (); // get all concept Attributes.
		JSONArray conAttributesArray = new JSONArray (); // fetch array of concept attributes.
		for ( Attribute attr : attributes ) {
			conAttributesArray.add ( buildAttribute ( attr ) ); // add to concept attributes array.
		}
		conceptJson.put( JSONAttributeNames.CONCEPTATTRIBUTES, conAttributesArray/* attrJson */ );

		// Contexts.
		Set<ONDEXConcept> contexts = con.getTags ();
		JSONObject contextsJson = new JSONObject ();
		for ( ONDEXConcept context : contexts ) {
			// return various JSON objects for each Context.
			contextsJson.put ( JSONAttributeNames.CONTEXT, buildContext ( context ) );
		}
		conceptJson.put( JSONAttributeNames.CONTEXTS, contextsJson );

		return conceptJson;
	}

    /**
     * Generate metadata for ONDEXRelation.
     * @param rel
     *            An ONDEXRelation (from the Ondex API).
     * @return JSONObject
     *            JSONObject containing information about the Relation.
     */
    private JSONObject buildRelation(ONDEXRelation rel) {
     JSONObject relationJson= new JSONObject();

     relationJson.put(JSONAttributeNames.ID, "e"+String.valueOf(rel.getId())); // relation ID.
     relationJson.put(JSONAttributeNames.FROMCONCEPT, String.valueOf(rel.getFromConcept().getId())); // relation source ID.
     relationJson.put(JSONAttributeNames.TOCONCEPT,String.valueOf(rel.getToConcept().getId())); // relation target ID.

     /* Relation Type (details returned in another JSON object). Now, uses "ofType" as key instead of 
      * "relationtypes" & "relation_type".
      */
     relationJson.put(JSONAttributeNames.OFTYPE, buildRelationType(rel.getOfType()));

     // Evidence Types.
     Set<EvidenceType> evidenceTypes= rel.getEvidence();
//     JSONObject evidencesJson= new JSONObject();
     JSONArray evidencesArray= new JSONArray(); // fetch array of concept attributes.
     for(EvidenceType et : evidenceTypes) {
         // return various JSON objects for each Evidence Type.
//         evidencesJson.put(JSONAttributeNames.EVIDENCE, buildEvidenceType(et));
         evidencesArray.add(buildEvidenceType(et)); // add to evidences array.
        }
     relationJson.put(JSONAttributeNames.EVIDENCES, evidencesArray/*evidencesJson*/);

     /** Relation GDS (Attributes), Attribute Names, Units & SpecialisationOf. Now, uses "attributes" & 
      * "attribute" instead of using older terminology: "relgds" & "relation_gds". These are now used as 
      * Keys in addition to older terms like "attrnames" and "attrname".
      */
     Set<Attribute> attributes= rel.getAttributes();
//     JSONObject attrJson= new JSONObject();
     JSONArray relAttributesArray= new JSONArray(); // fetch array of relation attributes.
     for(Attribute attr : attributes) {
//         attrJson.put(JSONAttributeNames.RELATIONGDS, buildAttribute(attr));
         relAttributesArray.add(buildAttribute(attr)); // add to relation attributes array.
        }
     relationJson.put(JSONAttributeNames.RELGDS, relAttributesArray/*attrJson*/);

     // Contexts.
     Set<ONDEXConcept> contexts= rel.getTags();
     JSONObject contextsJson= new JSONObject();
     for(ONDEXConcept context: contexts) {
         // return various JSON objects for each Context.
         contextsJson.put(JSONAttributeNames.CONTEXT, buildContext(context));
        }
     relationJson.put(JSONAttributeNames.CONTEXTS, contextsJson);

     return relationJson;
    }

    /**
     * Generate metadata for Data Source.
     * @param ds
     *            A Data Source (from the Ondex API).
     * @return String
     *            String containing full name or id.
     */
    private String/*JSONObject*/ buildDataSource(DataSource ds) {
     String elementOf;
/*     JSONObject dsJson= new JSONObject();
     dsJson.put(JSONAttributeNames.ID, ds.getId());
     dsJson.put(JSONAttributeNames.FULLNAME, ds.getFullname());
     dsJson.put(JSONAttributeNames.DESCRIPTION, ds.getDescription());
*/
     if(ds.getId().equals("")) {
        elementOf= ds.getFullname();
       }
     else {
        elementOf= ds.getId();
       }
     return elementOf/*dsJson*/;
    }

    /**
     * Generate metadata for Concept Class.
     * @param cc
     *            A Concept Class (from the Ondex API).
     * @return String
     *            String containing full name or id.
     */
    private String/*JSONObject*/ buildConceptClass(ConceptClass cc) {
     String ofType;
/*     JSONObject ccJson= new JSONObject();
     ccJson.put(JSONAttributeNames.ID, cc.getId());
     ccJson.put(JSONAttributeNames.FULLNAME, cc.getFullname());
     ccJson.put(JSONAttributeNames.DESCRIPTION, cc.getDescription());
     // ConceptClass SpecialisationOf (optional).
     ConceptClass spec= cc.getSpecialisationOf();
     if(spec != null) {
        ccJson.put(JSONAttributeNames.SPECIALISATIONOF, buildConceptClass(spec));
       }
*/
     if(cc.getFullname().equals("")) {
        ofType= cc.getId();
       }
     else {
        ofType= cc.getFullname();
       }
     return ofType/*ccJson*/;
    }

    /**
     * Generate metadata for Evidence Type.
     * @param et
     *            Evidence Type (from the Ondex API).
     * @return String
     *            String containing information about the Evidence Type.
     */
    private String/*JSONObject*/ buildEvidenceType(EvidenceType et) {
/*     JSONObject evidenceJson= new JSONObject();
     
     evidenceJson.put(JSONAttributeNames.ID, et.getId());
     evidenceJson.put(JSONAttributeNames.FULLNAME, et.getFullname());
     evidenceJson.put(JSONAttributeNames.DESCRIPTION, et.getDescription());
*/
     String evidenceName= et.getFullname().trim();
     if(evidenceName.equals("")) { 
        evidenceName= et.toString().trim();
       }

     return evidenceName/*evidenceJson*/;
    }

    /**
     * Generate metadata for Concept Name.
     * @param coname
     *            A Concept Name (from the Ondex API).
     * @return JSONObject
     *            JSONObject containing information about the Concept Name.
     */
    private JSONObject buildConceptName(ConceptName coname) {
     JSONObject conameJson= new JSONObject();
     
     conameJson.put(JSONAttributeNames.NAME, coname.getName());
     conameJson.put(JSONAttributeNames.ISPREFERRED, String.valueOf(coname.isPreferred()));

     return conameJson;
    }

    /**
     * Generate metadata for Concept Accession.
     * @param accession
     *            A Concept Accession (from the Ondex API).
     * @return JSONObject
     *            JSONObject containing information about the Concept Accession.
     */
    private JSONObject buildConceptAccession(ConceptAccession accession) {
     JSONObject accJson= new JSONObject();
     
     accJson.put(JSONAttributeNames.ACCESSION, accession.getAccession());
     // Element Of (return parent data source information).
     accJson.put(JSONAttributeNames.ELEMENTOF, buildDataSource(accession.getElementOf()));
//     accJson.put(JSONAttributeNames.AMBIGUOUS, String.valueOf(accession.isAmbiguous()));

     return accJson;
    }

    /**
     * Generate metadata for Attribute.
     * @param attr
     *            An Attribute (from the Ondex API).
     * @return JSONObject
     *            JSONObject containing information about the Attribute including its Attribute Name, 
     *            Unit(s) & SpecialisationOf.
     */
    private JSONObject buildAttribute(Attribute attr) {
     JSONObject attrJson= new JSONObject();

     attrJson.put(JSONAttributeNames.ATTRIBUTENAME, buildAttributeName(attr.getOfType()));

     // Attribute value (can be a String or any other data type or an Object or a Java class instance).
     attrJson.put(JSONAttributeNames.VALUE, attr.getValue().toString());
     /** ^^using String for now. */
     
//     attrJson.put(JSONAttributeNames.DOINDEX, String.valueOf(attr.isDoIndex()));
     return attrJson;
    }

    /**
     * Generate metadata for Attribute Name.
     * @param attr
     *            An AttributeName (from the Ondex API).
     * @return String
     *            String containing information about the Attribute.
     */
    private String/*JSONObject*/ buildAttributeName(AttributeName attrName) {
/*     JSONObject attrNameJson= new JSONObject();
     
     attrNameJson.put(JSONAttributeNames.ID, attrName.getId());
     attrNameJson.put(JSONAttributeNames.FULLNAME, attrName.getFullname());
     // Attribute Unit (optional).
     Unit attrUnit= attrName.getUnit();
     if(attrUnit != null) {
        attrNameJson.put(JSONAttributeNames.UNIT, buildUnit(attrUnit));
       }
     
     attrNameJson.put(JSONAttributeNames.DATATYPE, attrName.getDataTypeAsString());
     attrNameJson.put(JSONAttributeNames.DESCRIPTION, attrName.getDescription());
     // Attribute SpecialisationOf (optional).
     AttributeName spec= attrName.getSpecialisationOf();
     if(spec != null) {
        attrNameJson.put(JSONAttributeNames.SPECIALISATIONOF, buildAttributeName(spec));
       }
*/
     String attribute;
     if(attrName.getId().equals("")) {
        attribute= attrName.getFullname();
       }
     else {
        attribute= attrName.getId();
       }
     return attribute/*attrNameJson*/;
    }

   /**
     * Generate metadata for Context.
     * @param context
     *            A Context tag (from the Ondex API).
     * @return JSONObject
     *            JSONObject containing information about the Context tag.
     */
    private JSONObject buildContext(ONDEXConcept context) {
     JSONObject contextJson= new JSONObject();
     
     contextJson.put(JSONAttributeNames.ID, context.getId());

     return contextJson;
    }

    /**
     * Generate metadata for Relation Type.
     * @param rt
     *            Relation Type (from the Ondex API).
     * @return String
     *            String containing information about the Relation Type.
     */
    private String/*JSONObject*/ buildRelationType(RelationType rt) {
/*     JSONObject rtJson= new JSONObject();
     rtJson.put(JSONAttributeNames.ID, rt.getId());
     rtJson.put(JSONAttributeNames.FULLNAME, rt.getFullname());
     rtJson.put(JSONAttributeNames.INVERSENAME, rt.getInverseName());
     rtJson.put(JSONAttributeNames.ISANTISYMMETRIC, String.valueOf(rt.isAntisymmetric()));
     rtJson.put(JSONAttributeNames.ISREFLEXTIVE, String.valueOf(rt.isReflexive()));
     rtJson.put(JSONAttributeNames.ISTRANSITIVE, String.valueOf(rt.isTransitiv()));
     rtJson.put(JSONAttributeNames.ISSYMMETRIC, String.valueOf(rt.isSymmetric()));
     rtJson.put(JSONAttributeNames.DESCRIPTION, rt.getDescription());
     // RelationType SpecialisationOf (optional).
     RelationType spec= rt.getSpecialisationOf();
     if(spec != null) {
        rtJson.put(JSONAttributeNames.SPECIALISATIONOF, buildRelationType(spec));
       }
*/
     String relType;
     if(rt.getFullname().equals("")) {
        relType= rt.getId();
       }
     else {
       relType= rt.getFullname();
      }

     return relType/*rtJson*/;
    }

    /**
     * Format & return the data in json Objects
     * @param jsonData
     *            A String containing json data from the two json objects created from the ONDEXGraph.
     * @return String
     *            The formatted String containing json data.
     */
    private String formatJson(String jsonData) {
     jsonData= jsonData.trim();
     jsonData= jsonData.replace(",", ", ");
     jsonData= jsonData.replace(":", ": ");
     jsonData= jsonData.replace("{", "{ ");
     jsonData= jsonData.replace("}", "} ");
     jsonData= jsonData.trim();

     return jsonData;
    }
}
