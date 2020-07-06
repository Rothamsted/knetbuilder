package net.sourceforge.ondex.core.memory;

import org.junit.Test;

import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.core.test.AbstractONDEXGraphTest;

/**
 * Tests memory implementation of AbstractONDEXGraph.
 * 
 * @author taubertj
 *
 */
public class MemoryONDEXGraphTest extends AbstractONDEXGraphTest {

	@Override
	protected AbstractONDEXGraph initialize(String name) {
		return new MemoryONDEXGraph(name);
	}

	@Test
	public void testWorkflow() {
		//TODO: add tests
	}
}
