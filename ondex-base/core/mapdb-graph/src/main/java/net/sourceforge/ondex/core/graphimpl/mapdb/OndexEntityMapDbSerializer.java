package net.sourceforge.ondex.core.graphimpl.mapdb;

import java.io.IOException;

import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.elsa.ElsaSerializerPojo;
import org.mapdb.serializer.GroupSerializerObjectArray;

import net.sourceforge.ondex.core.ONDEXEntity;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Jan 2022</dd></dl>
 *
 */
abstract class OndexEntityMapDbSerializer<OE extends ONDEXEntity> extends GroupSerializerObjectArray<OE>
{	
	protected MapDbOndexGraph ondexGraph;
		
	private static final ElsaSerializerPojo ELSA_SER = new ElsaSerializerPojo ();

	static class ConceptSerializer extends OndexEntityMapDbSerializer<MapDbOndexConcept>
	{
		ConceptSerializer ( MapDbOndexGraph ondexGraph ) {
			super ( ondexGraph );
		}

		@Override
		void setGraph ( MapDbOndexConcept c ) {
			c.setGraph ( ondexGraph );
		}
	}
	
	static class RelationSerializer extends OndexEntityMapDbSerializer<MapDbOndexRelation>
	{
		RelationSerializer ( MapDbOndexGraph ondexGraph ) {
			super ( ondexGraph );
		}

		@Override
		void setGraph ( MapDbOndexRelation r ) {
			r.setGraph ( ondexGraph );
		}
	}
	
	
	OndexEntityMapDbSerializer ( MapDbOndexGraph ondexGraph )
	{
		super ();
		this.ondexGraph = ondexGraph;
	}

	@Override
	public void serialize ( DataOutput2 out, OE value ) throws IOException
	{
		ELSA_SER.serialize ( out, value );
	}
	
	
	@Override
	public OE deserialize ( DataInput2 input, int available ) throws IOException
	{
		OE result = ELSA_SER.deserialize ( input );
		this.setGraph ( result );
		return result;
	} 
	
	abstract void setGraph ( OE oe );
}
