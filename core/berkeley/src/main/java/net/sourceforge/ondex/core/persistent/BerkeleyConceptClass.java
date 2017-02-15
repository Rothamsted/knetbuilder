package net.sourceforge.ondex.core.persistent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.base.ConceptClassImpl;
import net.sourceforge.ondex.core.util.UpdateListener;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.RetrievalException;
import net.sourceforge.ondex.exception.type.StorageException;

public class BerkeleyConceptClass extends ConceptClassImpl implements
		Updatable, BerkeleySerializable {

	private UpdateListener l;

	/**
	 * Constructor calls to parent class constructor.
	 * 
	 * @param sid
	 * @param id
	 * @param fullname
	 * @param description
	 * @param specialisationOf
	 */
	protected BerkeleyConceptClass(long sid, String id, String fullname,
			String description, ConceptClass specialisationOf) {
		super(sid, id, fullname, description, specialisationOf);
	}

	/**
	 * Cast a given ConceptClass into a BerkeleyConceptClass.
	 * 
	 * @param old
	 *            ConceptClass
	 * @return BerkeleyConceptClass
	 */
	protected static BerkeleyConceptClass convert(ConceptClass old) {
		BerkeleyConceptClass nu = new BerkeleyConceptClass(old.getSID(),
				old.getId(), old.getFullname(), old.getDescription(),
				old.getSpecialisationOf());
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
	public void setFullname(String s) {
		super.setFullname(s);
		fireUpdateEvent();
	}

	@Override
	public void setDescription(String s) {
		super.setDescription(s);
		fireUpdateEvent();
	}

	@Override
	public void setSpecialisationOf(ConceptClass specialisationOf)
			throws AccessDeniedException {
		super.setSpecialisationOf(specialisationOf);
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

			// write serialisation of superclass
			byte[] array = SerialiseMetaDataFactory.serialise(sid, this);
			dos.writeInt(array.length);
			dos.write(array);

			// serialise class content
			if (specialisationOf != null)
				dos.writeUTF(specialisationOf.getId());
			else
				dos.writeUTF("");
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
	 * @return ConceptClass
	 */
	public static BerkeleyConceptClass deserialise(ONDEXGraph graph,
			byte[] array) {

		BerkeleyConceptClass cc = null;

		try {

			// byte input stream for byte array
			ByteArrayInputStream bais = new ByteArrayInputStream(array);

			// object input stream for deserialisation
			DataInputStream dis = new DataInputStream(new BufferedInputStream(
					bais));

			// parse superclass fields first
			int length = dis.readInt();
			byte[] sarray = new byte[length];
			dis.read(sarray);

			DataInputStream sdis = new DataInputStream(new BufferedInputStream(
					new ByteArrayInputStream(sarray)));

			// get serialised super class content
			long sid = sdis.readLong();
			String id = sdis.readUTF();
			String fullname = sdis.readUTF();
			String description = sdis.readUTF();

			sdis.close();
			sdis = null;
			sarray = null;

			// get serialised class content
			String specialisationOfId = dis.readUTF();

			// close streams
			dis.close();
			bais.close();

			// try to prevent memory leaks
			dis = null;
			bais = null;

			// retrieve meta data element
			ConceptClass specialisationOf = null;
			if (specialisationOfId.trim().length() > 0)
				specialisationOf = graph.getMetaData().getConceptClass(
						specialisationOfId);

			// create new instance of this class
			cc = new BerkeleyConceptClass(sid, id, fullname, description,
					specialisationOf);
		} catch (IOException ioe) {
			throw new RetrievalException(ioe.getMessage());
		}

		// return new conceptclass or null
		return cc;
	}
	
}
