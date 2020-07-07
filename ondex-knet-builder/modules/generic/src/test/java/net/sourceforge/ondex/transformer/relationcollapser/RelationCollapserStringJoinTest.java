package net.sourceforge.ondex.transformer.relationcollapser;

import static org.apache.commons.lang3.StringUtils.repeat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.logging.ONDEXLogger;

/**
 *
 * Tests {@link ClusterCollapser#joinStrings} and {@link ClusterCollapser#joinPIDs}. 
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>16 Jun 2020</dd></dl>
 *
 */
public class RelationCollapserStringJoinTest
{
	private ONDEXGraph graph = new MemoryONDEXGraph ( "default" );
	private ONDEXGraphMetaData meta = graph.getMetaData ();
	
	private DataSource ds = meta.createDataSource ( "ds0", "Data Source A", "" );
	private EvidenceType ev = meta.createEvidenceType ( "ev0", "Evidence 0", "" );
	private Collection<EvidenceType> evs = Arrays.asList ( ev );

	private ConceptClass cc = meta.createConceptClass ( "A", "Class A", "", null );
	private RelationType joinme = meta.createRelationType ( "joinme", "Join Me", "", "", false, false, false, false, null );

	private Transformer collapser = new Transformer ();
	
	
	@Before
	public void init () throws Exception
	{
		collapser.setONDEXGraph ( graph );
		
		ONDEXPluginArguments args = new ONDEXPluginArguments ( collapser.getArgumentDefinitions () );
		args.addOption ( ArgumentNames.RELATION_TYPE_ARG, joinme.getId () );
		collapser.setArguments ( args );

		collapser.addONDEXListener ( new ONDEXLogger () );

	}
	
	@Test
	public void testJoinEmptyString () throws Exception
	{		
		ONDEXConcept c1 = graph.createConcept ( "c1", "Ann 1", "", ds, cc, evs );
		ONDEXConcept c2 = graph.createConcept ( "c2", "", "Descr 2", ds, cc, evs );
		
		graph.createRelation ( c1, c2, joinme, evs );

		collapser.start ();
		
		Set<ONDEXConcept> jcs = graph.getConcepts ();
		Assert.assertEquals ( "Joined concept count is wrong!", 1, jcs.size () );
		
		ONDEXConcept c = jcs.iterator ().next ();
		
		String pid = c.getPID ();
		Assert.assertTrue ( 
			"Joined concept has wrong PID!", 
			(c1.getPID () + ';' + c2.getPID () ).equals ( pid ) 
			|| (c2.getPID () + ';' + c1.getPID () ).equals ( pid ) 
		);
		
		String ann = c.getAnnotation ();
		Assert.assertTrue ( "Joined concept's annotation is wrong!", 
			c1.getAnnotation ().equals ( ann ) || c2.getAnnotation ().equals ( ann ) 
		);
		
		String descr = c.getDescription ();
		Assert.assertTrue ( "Joined concept's annotation is wrong!", 
			c1.getDescription ().equals ( descr ) || c2.getDescription ().equals ( descr ) 
		);
	}
	
	
	@Test
	public void testJoinPIDs () throws Exception
	{		
		ONDEXConcept c1 = graph.createConcept ( repeat ( "c1", 200 ), "Ann 1", "Descr 1", ds, cc, evs );
		ONDEXConcept c2 = graph.createConcept ( repeat ( "c2", 200 ), "Ann 2", "Descr 2", ds, cc, evs );
		
		graph.createRelation ( c1, c2, joinme, evs );

		collapser.start ();
		
		Set<ONDEXConcept> jcs = graph.getConcepts ();
		Assert.assertEquals ( "Joined concept count is wrong!", 1, jcs.size () );
		
		ONDEXConcept c = jcs.iterator ().next ();
		
		String pid = c.getPID ();
		int usi = pid.lastIndexOf ( '_' );
		Assert.assertTrue ( "Joined concept has wrong PID!", usi > 0 && usi < pid.length () ); 
	}	
}
