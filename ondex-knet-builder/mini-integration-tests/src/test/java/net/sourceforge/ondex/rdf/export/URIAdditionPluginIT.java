package net.sourceforge.ondex.rdf.export;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.function.Consumer;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.ONDEXGraphUtils;
import net.sourceforge.ondex.mini.test.MiniInvoker;
import net.sourceforge.ondex.parser.oxl.Parser;

/**
 * Test the invocation of the {@link URIAdditionPlugin} from workflow file definitions.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>1 Mar 2019</dd></dl>
 *
 */
public class URIAdditionPluginIT
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	@Test
	public void testIt ()
	{
		String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + "/";
		String wfPath = mavenBuildPath + "test-classes/uri-addition-test/";
		MiniInvoker invoker = new MiniInvoker ();
		invoker.setMiniStartDirPath ( mavenBuildPath + "ondex-mini" );
		invoker.invoke ( wfPath + "uri-addition-test_wf.xml" );
		
		// TODO: this is a very simple graph with just two nodes and one relation, we need
		// something with URI-related edge cases
		//
		ONDEXGraph graph = Parser.loadOXL ( mavenBuildPath + "uri-addition-test-result.oxl" );
	
		AttributeName iriAttribType = graph.getMetaData ().getAttributeName ( "iri" );
		assertNotNull ( "URI Attribute Type not stored!", iriAttribType );
				
		Consumer<Collection<? extends ONDEXEntity>> checkIris = entities -> entities.stream ()
		.peek ( 
			odx -> {
				String label = ONDEXGraphUtils.getString ( odx );
				assertNotNull ( "entity " + label + " has null 'iri'!", odx.getAttribute ( iriAttribType ) );
				assertNotNull ( "entity " + label + " has null 'iri' value!", odx.getAttribute ( iriAttribType ).getValue () );
			}
		)
	  .map ( odx -> (String) odx.getAttribute ( iriAttribType ).getValue () )
		.forEach ( iri -> 
			assertTrue ( "iri <" + iri + "> is wrong!", iri.startsWith ( "http://knetminer.org/data/rdf/resources/" ) ) 
		);
		 
		checkIris.accept ( graph.getConcepts () );
		checkIris.accept ( graph.getRelations () );
	}
}
