package net.sourceforge.ondex.core.memory;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.test.AbstractRelationTest;
import net.sourceforge.ondex.logging.ONDEXLogger;

/**
 * 
 * @author hindlem
 *
 */
public class MemoryRelationTest extends AbstractRelationTest{

	@Override
	public void commit() {
		//do nothing
	}

	@Override
	public ONDEXGraph initialize(String name) throws Exception {
		return new MemoryONDEXGraph(name,new ONDEXLogger());
	}

}
