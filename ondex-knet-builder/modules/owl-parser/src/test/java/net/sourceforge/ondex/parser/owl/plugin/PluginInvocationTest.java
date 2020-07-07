package net.sourceforge.ondex.parser.owl.plugin;

import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;

/**
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Mar 2019</dd></dl>
 *
 */
public class PluginInvocationTest
{
	@Test
	public void testOwlPluginInvocation () throws Exception
	{
		String testPath = "target/test-classes/";
		
		OWLParser plugin = new OWLParser ();
		
		ONDEXGraph graph = new MemoryONDEXGraph ( "test" );
		plugin.setONDEXGraph ( graph );

		ArgumentDefinition<?> argDefs[] = plugin.getArgumentDefinitions ();
		ONDEXPluginArguments args = new ONDEXPluginArguments ( argDefs );
		args.addOption ( argDefs [ 0 ].getName (), testPath + "go_tests_common.owl" );
		args.addOption ( argDefs [ 0 ].getName (), testPath + "go_relations_test.owl" );
		args.addOption ( argDefs [ 1 ].getName (), testPath + "go_relations_test_cfg.xml" );
		plugin.setArguments ( args );

		plugin.start ();
		Assert.assertTrue ( "Parsed graph has 0 GO concepts!", graph.getConcepts ().size () > 0 );
		Assert.assertTrue ( "Parsed graph has 0 GO relations!", graph.getRelations ().size () > 0 );
	}
}
