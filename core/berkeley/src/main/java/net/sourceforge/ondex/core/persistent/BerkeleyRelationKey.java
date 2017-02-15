package net.sourceforge.ondex.core.persistent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.core.base.RelationKeyImpl;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.SerialisationFailedEvent;
import net.sourceforge.ondex.exception.type.StorageException;

public class BerkeleyRelationKey extends RelationKeyImpl implements
		BerkeleySerializable {

	/**
	 * Returns a new created object of this class from a byte array.
	 * 
	 * @param aog
	 *            AbstractONDEXGraph
	 * @param array
	 *            byte[]
	 * @return BerkeleyRelationKey
	 */
	public static BerkeleyRelationKey deserialise(AbstractONDEXGraph aog,
			byte[] array) {

		try {
			// byte input stream for byte array
			ByteArrayInputStream bais = new ByteArrayInputStream(array);

			// object input stream for deserialisation
			DataInputStream dis = new DataInputStream(new BufferedInputStream(
					bais));
			// get serialised class content

			long sid = dis.readLong();
			int fromConceptID = dis.readInt();
			int toConceptID = dis.readInt();
			String ofTypeId = dis.readUTF();

			dis.close();
			bais.close();

			return new BerkeleyRelationKey(sid, fromConceptID, toConceptID,
					ofTypeId);
		} catch (IOException ioe) {
			throw new StorageException(ioe.getMessage());
		}
	}

	public BerkeleyRelationKey(long sid, int fromConcept, int toConcept,
			String ofType) {
		super(sid, fromConcept, toConcept, ofType);
	}

	@Override
	public byte[] serialise() {
		return serialise(getSID());
	}

	@Override
	public byte[] serialise(long sid) {

		try {
			// create a byte output stream
			ByteArrayOutputStream baos = new ByteArrayOutputStream(100);

			// object output stream for serialisation
			DataOutputStream dos = new DataOutputStream(
					new BufferedOutputStream(baos));

			// serialise class content
			dos.writeLong(sid);
			dos.writeInt(getFromID());
			dos.writeInt(getToID());
			dos.writeUTF(getRtId());
			dos.flush();

			byte[] retVal = baos.toByteArray();

			// make sure streams are closed
			dos.close();
			baos.close();

			// return byte array
			return retVal;
		} catch (IOException ioe) {
			ONDEXEventHandler.getEventHandlerForSID(getSID())
					.fireEventOccurred(
							new SerialisationFailedEvent(ioe.getMessage(),
									"[BerkeleyRelationKey - serialise]"));
			return null;
		}
	}
}
