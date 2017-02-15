package net.sourceforge.ondex.core.persistent;

import java.io.File;
import java.io.IOException;

import org.junit.After;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.test.AbstractRelationTest;
import net.sourceforge.ondex.logging.ONDEXLogger;
import net.sourceforge.ondex.tools.DirUtils;

/**
 * 
 * @author hindlem
 *
 */
public class BerkeleyRelationTest extends AbstractRelationTest{

	private BerkeleyEnv env;

	private String path;

	@Override
	public ONDEXGraph initialize(String name) throws IOException {
		
		path = Config.ondexDir + File.separator + "dbs"
			+ File.separator + name;
				
		File dir = new File(path);
		if (dir.exists())
			DirUtils.deleteTree(dir);
		dir.deleteOnExit();

		dir.mkdir();
		assertTrue(dir.getAbsolutePath(), dir.canRead());
		assertTrue(dir.getAbsolutePath(), dir.canWrite());

		env = new BerkeleyEnv(path, name, new ONDEXLogger());
		return env.getAbstractONDEXGraph();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		env.cleanup();
		env = null;
		DirUtils.deleteTree(path);
	}

	@Override
	public void commit() {
		env.commit();
	}
	
}
