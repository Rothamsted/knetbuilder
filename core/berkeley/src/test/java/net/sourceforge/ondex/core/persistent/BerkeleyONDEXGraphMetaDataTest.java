package net.sourceforge.ondex.core.persistent;

import java.io.File;
import java.io.IOException;

import org.junit.After;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.core.test.AbstractONDEXGraphMetaDataTest;
import net.sourceforge.ondex.logging.ONDEXLogger;
import net.sourceforge.ondex.tools.DirUtils;

/**
 * Test Berkeley implementation of AbstractONDEXGraph.
 * 
 * @author taubertj
 * 
 */
public class BerkeleyONDEXGraphMetaDataTest extends AbstractONDEXGraphMetaDataTest {

	BerkeleyEnv env;

	String name = "BerkeleyONDEXGraphMetaDataTest";

	String path = Config.ondexDir + File.separator + "dbs"
			+ File.separator + name;

	@Override
	protected synchronized AbstractONDEXGraph initialize(String name) throws IOException {
		File dir = new File(path);
                if (dir.exists()){
                    DirUtils.deleteTree(dir);
                }

                dir.deleteOnExit();
                DirUtils.makeDirs(dir);
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

}
