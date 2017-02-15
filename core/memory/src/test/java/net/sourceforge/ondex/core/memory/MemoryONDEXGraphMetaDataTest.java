package net.sourceforge.ondex.core.memory;

import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.core.test.AbstractONDEXGraphMetaDataTest;

/**
 * Tests memory implementation of AbstractONDEXGraph.
 * 
 * @author taubertj
 *
 */
public class MemoryONDEXGraphMetaDataTest extends AbstractONDEXGraphMetaDataTest {

	@Override
	protected AbstractONDEXGraph initialize(String name) {
		return new MemoryONDEXGraph(name);
	}

	@Override
	protected void finalize() {
		// nothing to do
	}

	public void testWorkflow() {
		//TODO: add tests
	}
}
