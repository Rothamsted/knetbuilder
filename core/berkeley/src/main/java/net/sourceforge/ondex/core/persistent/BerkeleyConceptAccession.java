package net.sourceforge.ondex.core.persistent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.base.ConceptAccessionImpl;
import net.sourceforge.ondex.core.util.UpdateListener;
import net.sourceforge.ondex.exception.type.RetrievalException;
import net.sourceforge.ondex.exception.type.StorageException;

public class BerkeleyConceptAccession extends ConceptAccessionImpl implements
		Updatable, BerkeleySerializable {

	private UpdateListener l;

	/**
	 * Constructor calls to parent class constructor.
	 * 
	 * @param sid
	 * @param conceptId
	 * @param accession
	 * @param elementOf
	 * @param ambiguous
	 */
	protected BerkeleyConceptAccession(long sid, int conceptId,
			String accession, DataSource elementOf, boolean ambiguous) {
		super(sid, conceptId, accession, elementOf, ambiguous);
	}

	/**
	 * Cast a given ConceptAccession into a BerkeleyConceptAccession.
	 * 
	 * @param old
	 *            ConceptAccession
	 * @return BerkeleyConceptAccession
	 */
	protected static BerkeleyConceptAccession convert(ConceptAccession old) {
		BerkeleyConceptAccession nu = new BerkeleyConceptAccession(
				old.getSID(), old.getOwnerId(), old.getAccession(),
				old.getElementOf(), old.isAmbiguous());
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

	@Override
	public void setAmbiguous(boolean b) {
		super.setAmbiguous(b);
		fireUpdateEvent();
	}

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
			ByteArrayOutputStream baos = new ByteArrayOutputStream(100);

			// object output stream for serialisation
			DataOutputStream dos = new DataOutputStream(
					new BufferedOutputStream(baos));

			dos.writeLong(sid);
			dos.writeInt(conceptId);
			dos.writeUTF(accession);
			dos.writeUTF(elementOf.getId());
			dos.writeBoolean(ambiguous);
			dos.flush();

			byte[] retVal = baos.toByteArray();

			// make sure streams are closed
			dos.close();
			baos.close();

			return retVal;
		} catch (IOException ioe) {
			throw new StorageException(ioe.getMessage());
		}
	}

	/**
	 * Returns a new created object of this class from a byte array.
	 * 
	 * @param graph
	 *            ONDEXGraph
	 * @param array
	 *            byte[]
	 * @return ConceptAccession
	 */
	public static BerkeleyConceptAccession deserialise(ONDEXGraph graph,
			byte[] array) {

		BerkeleyConceptAccession ca = null;

		try {

			// byte input stream for byte array
			ByteArrayInputStream bais = new ByteArrayInputStream(array);

			// object input stream for deserialisation
			DataInputStream dis = new DataInputStream(new BufferedInputStream(
					bais));

			// get serialised class content
			long sid = dis.readLong();
			int cid = dis.readInt();
			String accession = dis.readUTF();
			String elementOfId = dis.readUTF();
			boolean ambiguous = dis.readBoolean();

			// close streams
			dis.close();
			bais.close();

			// try to prevent memory leaks
			dis = null;
			bais = null;

			// retrieve meta data element
			DataSource elementOf = graph.getMetaData().getDataSource(elementOfId);

			// create new instance of this class
			ca = new BerkeleyConceptAccession(sid, cid, accession, elementOf,
					ambiguous);
		} catch (IOException ioe) {
			throw new RetrievalException(ioe.getMessage());
		}

		// return new concept accession or null
		return ca;
	}

}
