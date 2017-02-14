package net.sourceforge.ondex.parser.kegg56.util;

import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.core.base.RelationKeyImpl;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.DeserialisationFailedEvent;
import net.sourceforge.ondex.event.type.SerialisationFailedEvent;

import java.io.*;

public class BerkeleyRelationKey extends RelationKeyImpl {

	public BerkeleyRelationKey(long sid, Integer fromConcept,
			Integer toConcept, String ofType) {
		super(sid, fromConcept, toConcept, ofType);
	}

	public BerkeleyRelationKey(long sid, int fromConceptID, int toConceptID,
			String ofTypeId) {
		super(sid, fromConceptID, toConceptID, ofTypeId);
	}

	public byte[] serialise() {

		try {
			// create a byte output stream
			ByteArrayOutputStream baos = new ByteArrayOutputStream(100);

			// object output stream for serialisation
			DataOutputStream dos = new DataOutputStream(
					new BufferedOutputStream(baos));

			// serialise class content
			dos.writeLong(getSID());
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

			BerkeleyRelationKey key = new BerkeleyRelationKey(sid,
					fromConceptID, toConceptID, ofTypeId);

			return key;
		} catch (IOException ioe) {
			ONDEXEventHandler.getEventHandlerForSID(aog.getSID())
					.fireEventOccurred(
							new DeserialisationFailedEvent(ioe.getMessage(),
									"[BerkeleyRelationKey - deserialise]"));
			return null;
		}
	}
}
