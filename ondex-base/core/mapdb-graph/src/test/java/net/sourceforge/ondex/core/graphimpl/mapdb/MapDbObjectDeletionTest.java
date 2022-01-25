package net.sourceforge.ondex.core.graphimpl.mapdb;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>20 Jan 2022</dd></dl>
 *
 */
public class MapDbObjectDeletionTest
{
	@Test
	public void testObjectDeletion ()
	{
		DB db = DBMaker
			.fileDB ( "target/test-mapdb-deletion.db" )
			.fileDeleteAfterClose()
			.make();

		Set<String> set = db.hashSet ( "testSet", Serializer.STRING ).createOrOpen ();
		set.add ( "test1" );
		
		Assert.assertTrue ( "Value not stored!", set.contains ( "test1" ) );
		
		db.delete ( "testSet" );
		set = db.hashSet ( "testSet", Serializer.STRING ).createOrOpen ();
		Assert.assertFalse ( "Value not stored!", set.contains ( "test1" ) );

	}
}
