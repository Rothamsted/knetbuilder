package net.sourceforge.ondex.rdf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * TODO: comment me!
 *
 * @author Marco Brandizi
 * <dl><dt>Date:</dt><dd>11 Mar 2024</dd></dl>
 *
 */
public class OndexRDFUtilsTest
{
	private static String NS = "http://foo.com/test/";
	
	@Test
	public void testIri ()
	{
		String cls = "TestConcept", acc = "A00";
		int id = 1;
		
		var iri = OndexRDFUtils.iri ( NS, cls, acc, id, false );
		assertEquals ( "iri() failed!", (NS + cls + "_" + acc).toLowerCase (), iri );
	}

	@Test
	public void testIriForceId ()
	{
		String cls = "TestConcept", acc = "A00";
		int id = 1;
		
		var iri = OndexRDFUtils.iri ( NS, cls, acc, id );
		assertEquals ( "iri() failed!", (NS + cls + "_" + acc + "_" + id).toLowerCase (), iri );
	}

	@Test
	public void testIriNoAcc ()
	{
		String cls = "TestConcept", acc = " ";
		int id = 1;
		
		var iri = OndexRDFUtils.iri ( NS, cls, acc, id );
		assertEquals ( "iri() failed!", (NS + cls + "_" + id).toLowerCase (), iri );
	}

	@Test
	public void testIriNoId ()
	{
		String cls = "TestConcept", acc = "A01";
		
		var iri = OndexRDFUtils.iri ( NS, cls, acc );
		assertEquals ( "iri() failed!", (NS + cls + "_" + acc).toLowerCase (), iri );
	}

	@Test ( expected = NullPointerException.class )
	public void testIriNoAccNoId ()
	{
		String cls = "TestConcept";
		
		OndexRDFUtils.iri ( NS, cls, null );
	}
	
	@Test
	public void testIriRepeatedClass ()
	{
		String cls = "FOO", accCode = "001", acc = "FOO:" + accCode;
		int id = 1;
		
		var iri = OndexRDFUtils.iri ( NS, cls, acc, id );
		assertEquals ( "iri() failed!", 
			(NS + cls + "_" + accCode + "_" + id).toLowerCase (),
			iri
		);
	}
		
}
