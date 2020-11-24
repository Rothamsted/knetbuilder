package net.sourceforge.ondex.tools.tab.importer;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.ONDEXGraphOperations;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.tools.subgraph.AttributePrototype;
import net.sourceforge.ondex.tools.subgraph.Subgraph;

/**
 * 
 * @author lysenkoa
 *
 */
public class PathParser
{
	public static final String MERGE_ACC = "MERGE_ON_ACCESSIONS";
	public static final String MERGE_NAME = "MERGE_ON_NAMES";
	public static final String MERGE_GDS = "MERGE_ON_GDS";
	/**
	 * If given, concepts having none of PID, an accession, or a name aren't ignored.
	 */
	public static final String NULL_CONCEPTS = "IGNORE_NULL_CONCEPTS";
	
	public static final Set<String> validFlags = Set.of ( MERGE_ACC, MERGE_NAME, MERGE_GDS, NULL_CONCEPTS );
	private ONDEXGraph graph;
	private final List<ConceptPrototype> cps = new LinkedList<> ();	
	private final Map<RelationPrototype, ConceptPrototype[]> rps = new LinkedHashMap<> ();
	
	private Set<ONDEXConcept> concepts = null;
	private Set<ONDEXRelation> relations = null;

	private DataReader dataReader = null;
	private Set<String> processingFlags = new HashSet<>();
	
  private Logger log = Logger.getLogger ( this.getClass() );
	
	
	public void setProcessingOptions(String ... options)
	{
		processingFlags.clear();
		for(String option: options )
			if ( validFlags.contains(option) ) processingFlags.add(option);
	}
	
	public void clearProcessingOptions ( String ... options )
	{
		processingFlags.clear();	
	}
	
	public PathParser(ONDEXGraph graph, DataReader dataReader) 
	{
		this ( graph );
		this.dataReader = dataReader;
	}
	
	
	public PathParser(ONDEXGraph  graph)
	{
		this.graph = graph;
		processingFlags.add ( MERGE_ACC );
		this.concepts = new HashSet<> ();
		this.relations = new HashSet<> ();
	}

	public ConceptPrototype newConceptPrototype ( AttributePrototype ... args ) throws NullValueException, EmptyStringException
	{
		ConceptPrototype cp = new ConceptPrototype ( graph, args );
		cps.add ( cp );
		return cp;
	}
	
	public RelationPrototype newRelationPrototype (
		ConceptPrototype source, ConceptPrototype target, AttributePrototype... args 
	) throws NullValueException, EmptyStringException
	{
		RelationPrototype rp = new RelationPrototype ( graph, args );
		rps.put ( rp, new ConceptPrototype[] { source, target } );
		return rp;
	}
	
	public Subgraph parse() throws Exception
	{
		log.info ( "Parsing started" );
		if ( dataReader == null ) throw new Exception ( "No input to parse was specified." );
		if ( !dataReader.isOpen () ) dataReader.reset ();
		
		for ( String[] row: (Iterable<String[]>) () -> dataReader ) 
			parse ( row );
		
		log.info ( "Parsing finished" );
		return finishParsing ();
	}
	
	protected void parse ( String [] row )
	{
		for ( ConceptPrototype conceptProto : cps )
		{
			conceptProto.parse ( row );
			ONDEXConcept c = conceptProto.getValue ();
			if ( c != null ) concepts.add ( c );
		}
		
		for ( Entry<RelationPrototype, ConceptPrototype[]> relMapping : rps.entrySet () )
		{
			var relProto = relMapping.getKey ();
			var conceptProtos = relMapping.getValue ();
			var fromProto = conceptProtos [ 0 ];
			var toProto = conceptProtos [ 1 ];
			
			relProto.parse ( fromProto, toProto, row );
			ONDEXRelation rel = relProto.getValue ();
			if ( rel != null ) relations.add ( rel );
		}
	}
	
	
	protected Subgraph finishParsing()
	{
		if ( !processingFlags.contains ( NULL_CONCEPTS ) )
		{
			log.info ( "Cleaning null concepts" );
			ONDEXGraphOperations.removeEmptyConcepts ( graph );
		}
		
		Subgraph result = new Subgraph ( concepts, relations, graph );
		if ( processingFlags.contains ( MERGE_ACC ) )
		{
			log.info ( "Collapsing redundant entries based on accessions start" );
			result.mapOnAccessions ();
			log.info ( "Accession-based collapsing finished" );
		}
		if ( processingFlags.contains ( MERGE_NAME ) )
		{
			log.info ( "Collapsing redundant entries based on names" );
			result.mapOnNames ();
			log.info ( "Name-based collapsing finished" );
		}
		if ( processingFlags.contains ( MERGE_GDS ) )
		{
			log.info ( "Collapsing redundant entries based on attributes" );
			result.mapOnAttribute ();
			log.info ( "Attribute-based collapsing finished" );
		}
		concepts = null;
		relations = null;
		return result;
	}

	public void setGraph(ONDEXGraph graph) throws Exception
	{
		if(graph == null) throw new Exception("The graph reference is invalid!");
		this.graph = graph;
	}
	
// TODO: it's a variant of parse without parameters (ie, parse all).
// Doesn't seem in use. Remove.
//	public void parseSimple() throws Exception{
//		if(dataReader == null)
//			throw new Exception("No input to parse was specified.");
//		while(dataReader.hasNext()){
//			String [] input = dataReader.readLine();
//			for(ConceptPrototype cp :cps){
//				cp.parse(input);	
//			}
//			for(Entry<RelationPrototype, ConceptPrototype[]> ent:rps.entrySet()){
//				ent.getKey().parse(ent.getValue()[0], ent.getValue()[1], input);	
//			}
//		}
//	}
}
