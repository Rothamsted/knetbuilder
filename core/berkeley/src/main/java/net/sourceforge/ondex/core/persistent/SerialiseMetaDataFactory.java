package net.sourceforge.ondex.core.persistent;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.SerialisationFailedEvent;
import net.sourceforge.ondex.exception.type.StorageException;

/**
 * @author hindlem Created 09-Apr-2010 16:09:36
 */
public class SerialiseMetaDataFactory {

	/**
	 * Returns a byte array serialisation of this class.
	 * 
	 * @param sid
	 *            override current sid
	 * @return byte[]
	 */
	public static byte[] serialise(long sid, MetaData md) {
		try {
			// create a byte output stream
			ByteArrayOutputStream baos = new ByteArrayOutputStream(100);

			// object output stream for serialisation
			DataOutputStream dos = new DataOutputStream(
					new BufferedOutputStream(baos));

			// serialise class content
			dos.writeLong(sid);
			dos.writeUTF(md.getId());
			dos.writeUTF(md.getFullname());
			dos.writeUTF(md.getDescription());
			dos.flush();

			byte[] retVal = baos.toByteArray();

			// make sure streams are closed
			dos.close();
			baos.close();

			return retVal;
		} catch (IOException ioe) {
			ONDEXEventHandler.getEventHandlerForSID(sid).fireEventOccurred(
					new SerialisationFailedEvent(ioe.getMessage(),
							"[SerialiseMetaDataFactory - serialise]"));
			throw new StorageException(ioe.getMessage());
		}
	}

}
