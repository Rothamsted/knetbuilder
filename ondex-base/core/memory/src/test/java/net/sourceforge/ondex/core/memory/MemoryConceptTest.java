package net.sourceforge.ondex.core.memory;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.test.AbstractConceptTest;
import net.sourceforge.ondex.logging.ONDEXLogger;

/**
 * 
 * @author hindlem
 *
 */
public class MemoryConceptTest extends AbstractConceptTest {

	@Override
	public void commit() {
		//do nothing
	}

	@Override
	public ONDEXGraph initialize(String name) throws Exception {
		return new MemoryONDEXGraph(name,new ONDEXLogger());
	}

}
