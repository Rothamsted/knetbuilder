package net.sourceforge.ondex.algorithm.graphquery.flatfile;

import static uk.ac.ebi.utils.exceptions.ExceptionUtils.buildEx;
import static uk.ac.ebi.utils.exceptions.ExceptionUtils.throwEx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import guru.nidi.graphviz.attribute.Arrow;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.RankDir;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import net.sourceforge.ondex.algorithm.graphquery.DirectedEdgeTransition;
import net.sourceforge.ondex.algorithm.graphquery.DirectedEdgeTransition.EdgeTreatment;
import net.sourceforge.ondex.algorithm.graphquery.State;
import net.sourceforge.ondex.algorithm.graphquery.StateMachine;
import net.sourceforge.ondex.algorithm.graphquery.Transition;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.InvalidFileException;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.StateMachineInvalidException;
import net.sourceforge.ondex.core.ONDEXGraph;
import uk.ac.ebi.utils.exceptions.ExceptionUtils;
import uk.ac.ebi.utils.exceptions.UncheckedFileNotFoundException;
import uk.ac.ebi.utils.exceptions.UnexpectedValueException;

/**
 * Converts a semantic motif file (parsed via {@link StateMachineFlatFileParser2}) to the 
 * <a href = "https://en.wikipedia.org/wiki/DOT_(graph_description_language)">DOT format</a>, 
 * useful for debugging and inspection.
 * 
 * <p>TODO: write unit tests.</p>
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Mar 2019</dd></dl>
 *
 */
public class StateMachineDotExporter
{
	private StateMachine stateMachine;
  private BiMap<Integer, State> stateIndex;
  private Map<State, MutableNode> states2DotNodes = new HashMap<> ();  	  

  public StateMachineDotExporter ( String smPath, ONDEXGraph metadataGraph ) 
  {
  	this ( new File ( smPath ), metadataGraph );
  }
  

  public StateMachineDotExporter ( File smFile, ONDEXGraph metadataGraph )
  {  	
  	try {
			this.init ( new BufferedReader ( new FileReader ( smFile ) ), metadataGraph );
		}
		catch ( FileNotFoundException ex )
		{
			throwEx ( 
				UncheckedFileNotFoundException.class, ex, 
				"State machine file <%s> not found",
				smFile.getAbsolutePath ()
			);
		}
  	catch ( RuntimeException ex )
  	{
			throwEx (
				RuntimeException.class, ex,
				"Error while parsing the state machine definition <%s>: %s",
				smFile.getAbsoluteFile (),
				ex.getMessage () 
			);
  	}
  }


  /**
   * The {@link StateMachineFlatFileParser2} uses metadata definitions in metadataGraph.
   */
  public StateMachineDotExporter ( Reader smReader, ONDEXGraph metadataGraph )
  {
  	this.init ( smReader, metadataGraph );
  }

  /**
   * This is to be used with the output of {@link StateMachineFlatFileParser2}. 
   */
	public StateMachineDotExporter ( StateMachine sm, BiMap<Integer, State> stateIndex )
	{
		this.init ( sm, stateIndex );
	}
  
  /** See {@link #StateMachineDotExporter(Reader, ONDEXGraph)} */
  private void init ( Reader smReader, ONDEXGraph metadataGraph )
  {
  	try 
  	{
    	StateMachineFlatFileParser2 smParser = new StateMachineFlatFileParser2 ();
			smParser.parseReader ( smReader, metadataGraph );
			this.init ( smParser.getStateMachine (), smParser.getStateIndex () );
		}
		catch ( InvalidFileException | StateMachineInvalidException ex )
		{
			throwEx (
				UnexpectedValueException.class, ex,
				"Error while parsing a state machine definition: %s",
				ex.getMessage () 
			);
		}
		catch ( IOException ex )
		{
			throwEx (
				UncheckedIOException.class, ex,
				"Error while parsing a state machine definition: %s",
				ex.getMessage () 
			);
		}
  }
  	
	/** 
	 * @see #StateMachineDotExporter(StateMachine, BiMap) 
	 */
	private void init ( StateMachine sm, BiMap<Integer, State> stateIndex )
	{
		this.stateMachine = sm;
		this.stateIndex = stateIndex;
	}
	
	public MutableGraph getGraph ()
	{
		MutableGraph graph = getGraph ( false );
		MutableGraph digraph = getGraph ( true );
		
		digraph.add ( graph );
		
		return graph;
	}
	
	/**
	 * Yields one of the needed graphs, either the directed graph (getDirectedTransitions==true), or 
	 * undirected. This is used by {@link #getGraph()}, to report both undirected and directed transitions.
	 */
	private MutableGraph getGraph ( boolean getDirectedTransitions )
	{
		MutableGraph graph = Factory.mutGraph ()
			// it needs to be directed even when the edge has no arrow, it's the only way to have a hybrid graph
			.setDirected ( true ) 
			.graphAttrs ().add ( RankDir.LEFT_TO_RIGHT );
				
		// All the nodes and then all the edges
		//
		this.stateMachine.getAllStates ().forEach ( 
			s -> getNode ( s, graph )
		);
		
		
		// Edges
		//
		
		// Use this intermediate, to put loops together, in a single transition
		Table<MutableNode, MutableNode, String> normalizedEdges = HashBasedTable.create ();
		// Go through transitions in a given order, needed to keep test results predictable
		List<Transition> transitions = new ArrayList<> ( this.stateMachine.getAllTransitions () );
		Collections.sort ( 
			transitions, 
			(t1, t2) -> t1.getValidRelationType ().getId ().compareTo ( t2.getValidRelationType ().getId () ) 
		);

		transitions.forEach ( t -> 
		{
			EdgeTreatment tdir = t instanceof DirectedEdgeTransition ? ((DirectedEdgeTransition) t).getTreatment () : null;
			if ( EdgeTreatment.BACKWARD.equals ( tdir ) ) ExceptionUtils.throwEx (
				UnsupportedOperationException.class, "The state machine dotter doesn't support backward transtions yet"
			);
			// The directionality of the transition must match the one currently being considered
			if ( getDirectedTransitions ^ EdgeTreatment.FORWARD.equals ( tdir ) ) return;
			
			MutableNode srcDot = this.states2DotNodes.get ( this.stateMachine.getTransitionSource ( t ) );
			MutableNode dstDot = this.states2DotNodes.get ( this.stateMachine.getTransitionTarget ( t ) );
		
			// link-type (<=maxLen) (omitted if no maxLen is specified)  
			String newLabel = t.getValidRelationType ().getId ();
			if ( t.getMaxLength () != Integer.MAX_VALUE ) newLabel += "(<=" + t.getMaxLength () + ")";

			String oldLabel = normalizedEdges.get ( srcDot, dstDot );
			if ( oldLabel != null ) newLabel = oldLabel + "\n" + newLabel;
			
			normalizedEdges.put ( srcDot, dstDot, newLabel );
		});
		
		int [] ctr = new int [] { 0 }; // to colour them alternatively
		normalizedEdges.cellSet ().forEach ( tuple ->
		{ 
			MutableNode srcDot = tuple.getRowKey ();
			MutableNode dstDot = tuple.getColumnKey ();
			
			// link-type (<=maxLen) (omitted if no maxLen is specified)  
			String elabel = tuple.getValue ();
			
			srcDot.addLink ( 
				Factory.to ( dstDot )
					.add ( Label.of ( elabel ) )
					.add ( ctr [ 0 ]++ % 2 == 0 ? Color.BLACK : Color.BLUE )
					// We process directed and undirected edges separately
					.add ( getDirectedTransitions ? Arrow.NORMAL : Arrow.NONE )
			);
		});
		
		return graph;
	}
	
	/**
	 * Creates a new DOT node or get a tracked one (already created). 
	 */
	private MutableNode getNode ( State s, MutableGraph dotGraph )
	{
		try
		{
			MutableNode node = this.states2DotNodes.get ( s );
			if ( node != null ) return node;
			
			// Type(idx)
			String nlabel = s.getValidConceptClass ().getId () + "(" + this.stateIndex.inverse ().get ( s ) + ")";
			node = Factory.mutNode ( nlabel );
			
			
			// Start/end nodes rendered differently
			//
			
			if ( stateMachine.getStart ().equals ( s ) )
				node.add ( Shape.DOUBLE_CIRCLE ).add ( Color.RED );
			
			if ( stateMachine.isFinish ( s ) )
				node.add ( Shape.DOUBLE_OCTAGON ).add ( Color.DARKGREEN );
			
			this.states2DotNodes.put ( s, node );
			dotGraph.add ( node );
			return node;
		}
		catch ( StateMachineInvalidException ex )
		{
			throw buildEx ( 
				IllegalStateException.class,
				ex,
				"Internal error while DOT-rendering a state machine: %s",
				ex.getMessage ()
			);
		}
	}

}
