package net.sourceforge.ondex.mini.test;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author brandizi
 * <dl><dt>Date:</dt><dd>18 Sep 2017</dd></dl>
 *
 */
public class MiniInvokerTest
{
	@Test @Ignore ( "Not a real test, this is tested in other modules anyway (e.g., textmining)" )
	public void testBasics ()
	{
		new MiniInvoker ().invoke ( 
			"/Users/brandizi/Documents/Work/RRes/tasks/text_mining_review/textmining_wf/tm-workflow.xml"
		);
	}
}
