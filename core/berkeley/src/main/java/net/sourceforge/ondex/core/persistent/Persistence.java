package net.sourceforge.ondex.core.persistent;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.exception.type.RetrievalException;

/**
 * Common utilities for working with persistence.
 * 
 * @author Matthew Pocock
 */
public final class Persistence {
	protected Persistence() {
	}

	public static abstract class MetaDataFactory<BMD extends MetaData, MD extends MetaData> {
		final String topic;

		protected MetaDataFactory(String topic) {
			this.topic = topic;
		}

		final String getTopic() {
			return topic;
		}

		abstract BMD create(long sid, String id, String fullname,
				String description);

		abstract BMD convert(MD md);
	}

	public interface Deserializer<MD> {
		public MD deserialise(AbstractONDEXGraph graph, byte[] buf);
	}

	/**
	 * Returns a new created object of this class from a byte array.
	 * 
	 * @param aog
	 *            AbstractONDEXGraph
	 * @param array
	 *            byte[]
	 * @return EvidenceType
	 */
	public static <BMD extends MetaData, MD extends MetaData> BMD deserialise(
			MetaDataFactory<BMD, MD> mdFact, byte[] array) {
		
		try {
			// byte input stream for byte array
			ByteArrayInputStream bais = new ByteArrayInputStream(array);

			// object input stream for deserialisation
			DataInputStream dis = new DataInputStream(new BufferedInputStream(
					bais));

			// get serialised class content
			long sid = dis.readLong();
			String id = dis.readUTF();
			String fullname = dis.readUTF();
			String description = dis.readUTF();

			// close streams
			dis.close();
			bais.close();

			// try to prevent memory leaks
			dis = null;
			bais = null;

			// create new instance of this class
			return mdFact.create(sid, id, fullname, description);
		} catch (IOException ioe) {
			throw new RetrievalException(ioe.getMessage());
		}
	}
}
