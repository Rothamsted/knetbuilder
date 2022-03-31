package net.sourceforge.ondex.core.test;

import java.util.ServiceLoader;

import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * 
 * A generic test graph provider, which feed all the tests needing one. This is for organising tests about 
 * ONDEX graph interfaces in this way: 
 * 
 * <ul>
 * 	<li>
 * 		A new implementation (see the memory module) should add an concrete implementation of this method in the
 * 		test/java directory, and, following the Java SPI specifications, mention this implementation in 
 * 		test/resources/META-INF/services/net.sourceforge.ondex.core.test.TestGraphProvider.
 * 
 * 		As per SPI, this will ensure that the {@link #getInstance()} method below uses the local provider to 
 *    create a new {@link ONDEXGraph} during tests.
 *  </li>
 *  <li>
 *  	There should be an abstract test class in the hereby base module, let's say AbstractMyTest, which should have 
 *  	tests that <b>do not depend</b> on any particular implementation of {@link ONDEXGraph} (or related interfaces).
 *  	These methods should use {@link #getInstance()} and {@link #createGraph(String)}. Since the binding is based 
 *  	on SPI and at runtime, they will remain implementation-independent. 
 *  </li>
 *  <li>
 *  	For each new implementation of graph interfaces (eg, memory), there should be a concrete implementation of 
 *    the abstract test class (eg, MemMyTest). Typically, this will be empty (everything is already in place, including
 *    the dynamic binding above) and will only serve as a marker for JUnit to know there is a new test class to run.
 *  </li>
 * </ul>
 * 
 * TODO: Note that classes like {@link AbstractConceptTest} are more redundant than the pattern described above, since 
 * they were re-adapted to it many years after the first writing and need more cleaning.
 * 
 * TODO: maybe use Spring in place of SPI?
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>31 Mar 2022</dd></dl>
 *
 */
public abstract class TestGraphProvider
{
	private static TestGraphProvider instance;
	
	public static synchronized TestGraphProvider getInstance ()
	{
		if ( instance != null ) return instance;
		
		var loader = ServiceLoader.load ( TestGraphProvider.class ).iterator ();
		if ( !loader.hasNext () ) throw new IllegalStateException ( 
			"You must configure the SPI service for TestGraphProvider to use ondex graph tests"
		);
		instance = loader.next ();
		if ( loader.hasNext () ) throw new IllegalStateException ( 
			"multiple SPI configuration met for TestGraphProvider, this is likely wrong"
		);
		return instance;
	}
	
	public abstract ONDEXGraph createGraph ( String name );
	
	/**
	 * Defaults to "default". Should be fine in most cases.
	 */
	public ONDEXGraph createGraph () {
		return createGraph ( "default" );
	}
}
