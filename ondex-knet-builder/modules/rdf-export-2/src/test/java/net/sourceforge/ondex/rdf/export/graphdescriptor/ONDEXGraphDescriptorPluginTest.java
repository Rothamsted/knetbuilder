package net.sourceforge.ondex.rdf.export.graphdescriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.oxl.Parser;
import net.sourceforge.ondex.utils.OndexPluginUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>27 Nov 2021</dd></dl>
 *
 */
public class ONDEXGraphDescriptorPluginTest
{
	@Test
	public void testBasics () throws IOException
	{
		String mavenPomDir = Path.of ( System.getProperty ( "maven.basedir", "." ) )
			.toRealPath ()
			.toString () + '/';
		
		String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + '/';
		
		var oxlPath = mavenPomDir + "src/main/assembly/resources/examples/text_mining.oxl";
		ONDEXGraph graph = Parser.loadOXL ( oxlPath );

		var xpath = mavenBuildPath + "/graph-descriptor-plugin-test.ttl";
		
		Map<String, Object> args = Map.of (
			"configurationPath", mavenPomDir + "src/main/assembly/resources/examples/descriptor.properties",
			"rdfTemplatePath", mavenPomDir + "src/main/assembly/resources/examples/descriptor-template.ttl",
			"oxlSourceURL", "file://" + oxlPath,
			"exportPath", xpath
		);
		
		OndexPluginUtils.runPlugin ( ONDEXGraphDescriptorPlugin.class, graph, args );
		
		var descritorTool = new OndexGraphDescriptorTool.Builder()
			.setGraph ( graph )
			.build ();

		assertNotNull ( "No descritor concept!", descritorTool.getDescriptorConcept () );
				
		var dataset = descritorTool.getDescriptorDataset ();
		assertNotNull ( "No descritor dataset!", descritorTool.getDescriptorConcept () );
		assertEquals ( "Bad dataset identifier!", "KnetMiner:Triticum_aestivum", dataset.get ( "identifier" ) );
		
		Map<String, Map<String, Object>> propVals = descritorTool.getDatasetAdditionalProperties ();
		int nconcepts = OndexGraphDescriptorTool.getPropertyValueAsInt ( propVals, "KnetMiner:Dataset:Concepts Number" );
		assertEquals ( "Wrong property value for concepts number", graph.getConcepts ().size (), nconcepts + 1 );
		
		assertNotNull ( "No OXL hash created!", (String) OndexGraphDescriptorTool.getPropertyValue ( propVals, "KnetMiner:Dataset:Source MD5" ) );
		
		assertTrue ( "Descriptor not exported!", new File ( xpath ).exists () );
	}
}
