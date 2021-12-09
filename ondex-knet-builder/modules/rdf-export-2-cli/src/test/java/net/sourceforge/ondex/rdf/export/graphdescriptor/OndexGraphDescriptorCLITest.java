package net.sourceforge.ondex.rdf.export.graphdescriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import net.sourceforge.ondex.parser.oxl.Parser;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>9 Dec 2021</dd></dl>
 *
 */
public class OndexGraphDescriptorCLITest
{
	@Test
	public void testCLI () throws Exception
	{
		String mavenBuildPath = System.getProperty ( "maven.buildDirectory", "target" ) + '/';
		
		String examplesDir = mavenBuildPath + "dependency/rdf-export-2-plugin/examples/";
		
		var oxlPath = examplesDir + "text_mining.oxl";
		var xpath = mavenBuildPath + "/graph-descriptor-test.ttl";
		var oxlOutPath = mavenBuildPath + "/graph-descriptor-test.oxl";
		
		int exitCode = OndexGraphDescriptorCLI.invoke (
			"--config", examplesDir + "descriptor.properties",
			"--template", examplesDir + "descriptor-template.ttl",
			"-x", xpath,
			oxlPath,
			oxlOutPath
		);
		
		assertEquals ( "Wrong CLI exit code!", 0, exitCode );
		assertTrue ( "No output OXL!", new File ( oxlOutPath ).exists () );
		assertTrue ( "No Exported RDF!", new File ( xpath ).exists () );

		// Verify it was written in the out.
		var graph = Parser.loadOXL ( oxlOutPath );
		OndexGraphDescriptorTool descriptorTool = new OndexGraphDescriptorTool.Builder ()
			.setGraph ( graph )
			.build ();
		
		assertNotNull ( "No descriptor in the output!", descriptorTool.getDescriptor () );
		assertNotNull ( "No dataset object from the output!", descriptorTool.getDescriptorDataset () );
	}
}
