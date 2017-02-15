package net.sourceforge.ondex.core.persistent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.base.ConceptAttribute;
import net.sourceforge.ondex.core.util.UpdateListener;
import net.sourceforge.ondex.exception.type.RetrievalException;
import net.sourceforge.ondex.exception.type.StorageException;
import net.sourceforge.ondex.marshal.Marshaller;

public class BerkeleyConceptAttribute extends ConceptAttribute implements
		Updatable, BerkeleySerializable {

	private UpdateListener l;

	/**
	 * Constructor calls to parent class constructor.
	 * 
	 * @param sid
	 * @param relationId
	 * @param attributeName
	 * @param value
	 * @param doIndex
	 */
	BerkeleyConceptAttribute(long sid, int cid, AttributeName anName,
			Object value, boolean doIndex) {
		super(sid, cid, anName, value, doIndex);
	}

	/**
	 * Cast a given Attribute into a BerkeleyConceptAttribute.
	 * 
	 * @param old
	 *            Attribute
	 * @return BerkeleyConceptAttribute
	 */
	protected static BerkeleyConceptAttribute convert(Attribute old) {
		BerkeleyConceptAttribute nu = new BerkeleyConceptAttribute(
				old.getSID(), old.getOwnerId(), old.getOfType(),
				old.getValue(), old.isDoIndex());
		return nu;
	}

	@Override
	public void fireUpdateEvent() {
		if (l != null)
			l.performUpdate(this);
	}

	@Override
	public void setUpdateListener(UpdateListener l) {
		this.l = l;
	}

	/**
	 * Returns a new created object of this class from a byte array.
	 * 
	 * @param graph
	 *            ONDEXGraph
	 * @param array
	 *            byte[]
	 * @return BerkeleyConceptAttribute
	 */
	public static BerkeleyConceptAttribute deserialise(ONDEXGraph graph,
			byte[] array) {

		BerkeleyConceptAttribute attribute = null;

		try {

			// byte input stream for byte array
			ByteArrayInputStream bais = new ByteArrayInputStream(array);

			// object input stream for deserialisation
			DataInputStream dis = new DataInputStream(new BufferedInputStream(
					bais));

			long sid = dis.readLong();
			int cid = dis.readInt();
			String attributeNameId = dis.readUTF();
			String valueXML = dis.readUTF();
			boolean doIndex = dis.readBoolean();

			// close streams
			dis.close();
			bais.close();

			// try to prevent memory leaks
			dis = null;
			bais = null;

			// retrieve meta data element
			AttributeName attributeName = graph.getMetaData().getAttributeName(
					attributeNameId);

			// create new instance of this class
			attribute = new BerkeleyConceptAttribute(sid, cid, attributeName,
					Marshaller.getMarshaller().fromXML(valueXML), doIndex);

		} catch (IOException ioe) {
			throw new RetrievalException(ioe.getMessage());
		}

		// return new attribute or null
		return attribute;
	}

	/**
	 * Returns a byte array serialisation of this class.
	 * 
	 * @return byte[]
	 */
	@Override
	public byte[] serialise() {
		return serialise(getSID());
	}

	/**
	 * Returns a byte array serialisation of this class.
	 * 
	 * @return byte[]
	 */
	@Override
	public byte[] serialise(long sid) {
		try {
			// create a byte output stream
			ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);

			// object output stream for serialisation
			DataOutputStream dos = new DataOutputStream(
					new BufferedOutputStream(baos));

			dos.writeLong(sid);
			dos.writeInt(ownerID);
			dos.writeUTF(attrname.getId());
			dos.writeUTF(Marshaller.getMarshaller().toXML(getValue()));
			dos.writeBoolean(doIndex);
			dos.flush();

			byte[] retVal = baos.toByteArray();

			// make sure streams are closed
			dos.close();
			baos.close();

			// try to prevent memory leaks
			dos = null;
			baos = null;

			return retVal;
		} catch (IOException ioe) {
			throw new StorageException(ioe);
		}
	}

	@Override
	public void setValue(Object o) {
		super.setValue(o);
		fireUpdateEvent();
	}

	@Override
	public void setDoIndex(boolean b) {
		super.setDoIndex(b);
		fireUpdateEvent();
	}

}