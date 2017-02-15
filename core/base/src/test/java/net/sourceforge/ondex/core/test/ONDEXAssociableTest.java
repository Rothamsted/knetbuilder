/**
 * 
 */
package net.sourceforge.ondex.core.test;

import junit.framework.TestCase;
import net.sourceforge.ondex.core.ONDEXGraph;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for ONDEXAssociable interface.
 * 
 * @author taubertj
 *
 */
public abstract class ONDEXAssociableTest extends TestCase {

	public abstract ONDEXGraph initialize(String name) throws Exception;
	
	private ONDEXGraph og;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// setup graph
		og = initialize(this.getName());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link net.sourceforge.ondex.core.ONDEXAssociable#getSID()}.
	 */
	@Test
	public final void testGetSID() {
		assertNotNull(og.getSID());
	}

}
