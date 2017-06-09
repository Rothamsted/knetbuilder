package net.sourceforge.ondex.core.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;

/**
 * <p>A wrapper for {@link ONDEXGraph} that has the main scope of avoiding to create duplicates when creating new
 * ONDEX entities. Methods are similar to createXXX methods in the {@link ONDEXGraph} interface, each method
 * calls the corresponding underlining creation method, but only if the required object hasn't been created yet.</p>
 * 
 * <p>Clearly, this is based on an internal static set of caches, that can be on a per-graph basis 
 * (see {@link #getInstance(ONDEXGraph)}).</p>
 * 
 * <p>Note that this class would normally be a <a href = "https://en.wikipedia.org/wiki/Decorator_pattern">decorator</a>, 
 * but we prefer not to implement this way here (for the time being), for it would require too much review of 
 * existing code.</p> 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Apr 2017</dd></dl>
 *
 */
public class CachedGraphWrapper
{
	private ONDEXGraph graph; 
	private Table<Class<Object>, String, Object> cache = HashBasedTable.create ();
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	private static Map<ONDEXGraph, CachedGraphWrapper> instances = new HashMap<> ();
	
	public CachedGraphWrapper ( ONDEXGraph graph )
	{
		this.graph = graph;
	}

	/**
	 * We recommend to use this to get a wrapper that caches this graph.
	 *  
	 * @param graph
	 */
	public static CachedGraphWrapper getInstance ( ONDEXGraph graph ) 
	{
		return instances.computeIfAbsent ( graph, g -> new CachedGraphWrapper ( g ) );
	}
	
	
	public ConceptClass getConceptClass ( String id, String fullName, String description, ConceptClass specialisationOf )
	{
		return this.cacheGet ( 
			ConceptClass.class, id, 
			() -> this.graph.getMetaData ().createConceptClass ( id, fullName, description, specialisationOf )
		);
	}

	
	public ONDEXConcept getConcept (
		String id, String annotation, String description, DataSource ds, ConceptClass conceptClass, EvidenceType evidence
	)
	{
		return this.cacheGet ( 
			ONDEXConcept.class, id, 
			() -> this.graph.getFactory ().createConcept ( id, annotation, description, ds, conceptClass, evidence )
		);
	}
	
	
	public RelationType getRelationType ( 
		String id, boolean isAntisymmetric, boolean isReflexive, boolean isSymmetric, boolean isTransitive 
	)
	{
		return this.cacheGet ( 
			RelationType.class, id, 
			() -> this.graph.getMetaData ().getFactory ().createRelationType (
				id, isAntisymmetric, isReflexive, isSymmetric, isTransitive 
			)
		);
	}

	public RelationType getRelationType ( RelationTypePrototype proto )
	{
		try 
		{
			// Let's see if it has a parent
			if ( proto.getSpecialisationOf () == null )
			{
				// Or a prototype to build it. In case of loops, this will lead to stack overflow
				RelationTypePrototype parentProto = proto.getParentPrototype ();
				if ( parentProto != null )
						proto.setSpecialisationOf ( this.getRelationType ( parentProto ) );
			}
			
			return this.cacheGet ( 
				RelationType.class, proto.getId (), 
				() -> this.graph.getMetaData ().getFactory ().createRelationType ( 
					proto.getId (), proto.getFullname (), proto.getDescription (), 
					proto.isAntisymmetric (), proto.isReflexive (), proto.isSymmetric (), proto.isTransitiv (), 
					proto.getSpecialisationOf () )
			);
		}
		catch ( StackOverflowError ex ) 
		{
			log.error ( "Stackoverflow error while creating relation '{}'. Do you have circular references?", proto.getId () );
			throw ex;
		}
	}
	
	
	public ONDEXRelation getRelation ( ONDEXConcept from, ONDEXConcept to, RelationType type, EvidenceType evidence )
	{
		String id = from.getPID () + to.getPID () + type.getId () + evidence.getId ();
		return this.cacheGet ( 
				ONDEXRelation.class, id, 
			() -> this.graph.getFactory ().createRelation ( from, to, type, evidence )
		);
	}
	
	public EvidenceType getEvidenceType ( String id, String fullName, String description )
	{
		return this.cacheGet ( 
			EvidenceType.class, id, 
			() -> this.graph.getMetaData ().createEvidenceType ( id, fullName, description ) 
		);
	}
	
	public EvidenceType getEvidenceType ( EvidenceTypePrototype proto )
	{
		return this.getEvidenceType ( proto.getId (), proto.getFullName (), proto.getDescription () );
	}
	
	
	public DataSource getDataSource ( String id, String fullName, String description )
	{
		return this.cacheGet ( 
			DataSource.class, id, 
			() -> this.graph.getMetaData ().createDataSource ( id, fullName, description ) 
		);
	}

	public DataSource getDataSource ( DataSourcePrototype proto )
	{
		return this.getDataSource ( proto.getId (), proto.getFullName (), proto.getDescription () );
	}
	
	@SuppressWarnings ( "unchecked" )
	private <V> V cacheGet ( Class<? super V> type, String key, Supplier<V> newValueGenerator )
	{
		V result = (V) this.cache.get ( type, key );
		if ( result != null ) return result;
		
		this.cache.put ( (Class<Object>) type, key, result = newValueGenerator.get () );
		return result;
	}

}
