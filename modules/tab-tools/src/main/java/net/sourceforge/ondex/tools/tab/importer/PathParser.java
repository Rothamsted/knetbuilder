package net.sourceforge.ondex.tools.tab.importer;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.tools.subgraph.AttributePrototype;
import net.sourceforge.ondex.tools.subgraph.Subgraph;

/**
 * 
 * @author lysenkoa
 *
 */
public class PathParser {
	public static final String MERGE_ACC = "MERGE_ON_ACCESSIONS";
	public static final String MERGE_NAME = "MERGE_ON_NAMES";
	public static final String MERGE_GDS = "MERGE_ON_GDS";
	public static final Collection<String> validFlags = Arrays.asList(new String[]{MERGE_ACC, MERGE_NAME, MERGE_GDS});
	private ONDEXGraph graph;
	private final List<ConceptPrototype> cps = new LinkedList<ConceptPrototype>();	
	private final Map<RelationPrototype, ConceptPrototype[]> rps = new LinkedHashMap<RelationPrototype, ConceptPrototype[]>();
	
	private Set<ONDEXConcept> concepts = null;
	private Set<ONDEXRelation> relations = null;

	private DataReader dataReader = null;
	private Set<String> processingFlags = new HashSet<String>();
	
	public void setProcessingOptions(String ... options){
		processingFlags.clear();
		for(String option:options){
			if(validFlags.contains(option)){
				processingFlags.add(option);
			}
		}
	}
	
	public void clearProcessingOptions(String ... options){
		processingFlags.clear();	
	}
	
	public PathParser(ONDEXGraph  graph, DelimitedReader dataReader) {
		this(graph);
		this.dataReader = dataReader;

	}
	
	public PathParser(ONDEXGraph  graph) {
		this.graph = graph;
		processingFlags.add(MERGE_ACC);
		this.concepts = new HashSet<ONDEXConcept>();
		this.relations = new HashSet<ONDEXRelation>();
	}

	public ConceptPrototype newConceptPrototype(AttributePrototype ... args) throws NullValueException, EmptyStringException{
		ConceptPrototype cp = new ConceptPrototype(graph, args);
		cps.add(cp);
		return cp;
	}
	
	public RelationPrototype newRelationPrototype(ConceptPrototype source, ConceptPrototype target, AttributePrototype ... args) throws NullValueException, EmptyStringException{
		RelationPrototype rp = new RelationPrototype(graph, args);
		rps.put(rp, new ConceptPrototype[]{source, target});
		return rp;
	}
	
	public Subgraph parse() throws Exception{
		System.err.println("Parsing stated "+now("dd.MM.yyyy G 'at' HH:mm:ss z"));
		if(dataReader == null)
			throw new Exception("No input to parse was specified.");
		if(!dataReader.isOpen())
			dataReader.reset();
		while(dataReader.hasNext()){
			parse(dataReader.readLine());
		}
		System.err.println("Parsing finished at "+now("dd.MM.yyyy G 'at' HH:mm:ss z"));
		return finishParsing();
	}
	
	public void parse(String [] vector){
		for(ConceptPrototype cp :cps){
			cp.parse(vector);
			ONDEXConcept c = cp.getValue();
			if(c != null)
				concepts.add(c);
		}
		for(Entry<RelationPrototype, ConceptPrototype[]> ent:rps.entrySet()){
			ent.getKey().parse(ent.getValue()[0], ent.getValue()[1], vector);
			ONDEXRelation r = ent.getKey().getValue();
			if(r != null)
				relations.add(r);
		}	
	}
	
	public Subgraph spawnSubgraph(){
		Subgraph result = new Subgraph(concepts, relations, graph);
		concepts = new HashSet<ONDEXConcept>();
		relations = new HashSet<ONDEXRelation>();
		return result;
	}
	
	/*public Subgraph finishParsing(){
		Subgraph result = new Subgraph(concepts, relations, graph);
		if(processingFlags.contains(MERGE_ACC)){
			System.err.println("Collapsing redundant entries based on accessions start at "+now("dd.MM.yyyy G 'at' HH:mm:ss z"));
			result.mergeConcepts(new AttributePrototype(DefConst.DEFACC, 0, 0, 0));
			System.err.println("Finished at "+now("dd.MM.yyyy G 'at' HH:mm:ss z"));
		}
		if(processingFlags.contains(MERGE_NAME)){
			System.err.println("Collapsing redundant entries based on names start at "+now("dd.MM.yyyy G 'at' HH:mm:ss z"));
			result.mergeConcepts(new AttributePrototype(DefConst.DEFNAME, 0, 0));	
			System.err.println("Finished at "+now("dd.MM.yyyy G 'at' HH:mm:ss z"));
		}
		if(processingFlags.contains(MERGE_GDS)){
			System.err.println("Collapsing redundant entries based on gds start at "+now("dd.MM.yyyy G 'at' HH:mm:ss z"));
			result.mergeConcepts(new AttributePrototype(DefConst.DEFATTR, 0, 0, 0));	
			System.err.println("Finished at "+now("dd.MM.yyyy G 'at' HH:mm:ss z"));
		}
		concepts = null;
		relations = null;
		return result;	
	}*/
	
	public Subgraph finishParsing(){
		Subgraph result = new Subgraph(concepts, relations, graph);
		if(processingFlags.contains(MERGE_ACC)){
			System.err.println("Collapsing redundant entries based on accessions start at "+now("dd.MM.yyyy G 'at' HH:mm:ss z"));
			result.mapOnAccessions();
			System.err.println("Finished at "+now("dd.MM.yyyy G 'at' HH:mm:ss z"));
		}
		if(processingFlags.contains(MERGE_NAME)){
			System.err.println("Collapsing redundant entries based on names start at "+now("dd.MM.yyyy G 'at' HH:mm:ss z"));
			result.mapOnNames();
			System.err.println("Finished at "+now("dd.MM.yyyy G 'at' HH:mm:ss z"));
		}
		if(processingFlags.contains(MERGE_GDS)){
			System.err.println("Collapsing redundant entries based on gds start at "+now("dd.MM.yyyy G 'at' HH:mm:ss z"));
			result.mapOnAttribute();
			System.err.println("Finished at "+now("dd.MM.yyyy G 'at' HH:mm:ss z"));
		}
		concepts = null;
		relations = null;
		return result;	
	}

	public void setGraph(ONDEXGraph graph) throws Exception{
		if(graph == null){
			throw new Exception("The graph reference is invalid!");
		}
		this.graph = graph;
	}
	
	public void parseSimple() throws Exception{
		if(dataReader == null)
			throw new Exception("No input to parse was specified.");
		while(dataReader.hasNext()){
			String [] input = dataReader.readLine();
			for(ConceptPrototype cp :cps){
				cp.parse(input);	
			}
			for(Entry<RelationPrototype, ConceptPrototype[]> ent:rps.entrySet()){
				ent.getKey().parse(ent.getValue()[0], ent.getValue()[1], input);	
			}
		}
	}
	
	public PathParser(DataReader dataReader, ONDEXGraph  graph) {
		this(graph);
		this.dataReader = dataReader;
		processingFlags.add(MERGE_ACC);
	}
	
	private static String now(String dateFormat) {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
	    return sdf.format(cal.getTime());
	}
}
