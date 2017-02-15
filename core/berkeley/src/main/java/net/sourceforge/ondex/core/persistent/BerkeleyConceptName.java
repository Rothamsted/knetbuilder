package net.sourceforge.ondex.core.persistent;

import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.core.base.ConceptNameImpl;
import net.sourceforge.ondex.core.util.UpdateListener;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.DeserialisationFailedEvent;
import net.sourceforge.ondex.event.type.SerialisationFailedEvent;
import net.sourceforge.ondex.exception.type.RetrievalException;
import net.sourceforge.ondex.exception.type.StorageException;

import java.io.*;

public class BerkeleyConceptName extends ConceptNameImpl implements Updatable, BerkeleySerializable {

    private UpdateListener l;

    BerkeleyConceptName(long sid, int cid, String name, boolean isPreferred) {
        super(sid, cid, name, isPreferred);
    }

    public static BerkeleyConceptName convert(ConceptName old) {
        long sid = old.getSID();
        BerkeleyConceptName nu = new BerkeleyConceptName(sid, old.getOwnerId(), old.getName(), old.isPreferred());
        return nu;
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
    public byte[] serialise(long sid) {
        try {

            // create a byte output stream
            ByteArrayOutputStream baos = new ByteArrayOutputStream(100);

            // object output stream for serialisation
            DataOutputStream dos = new DataOutputStream(
                    new BufferedOutputStream(baos));

            dos.writeLong(sid);
            dos.writeInt(conceptId);
            dos.writeUTF(name);
            dos.writeBoolean(isPreferred);
            dos.flush();

            byte[] retVal = baos.toByteArray();

            // make sure streams are closed
            dos.close();
            baos.close();

            return retVal;
        } catch (IOException ioe) {
            ONDEXEventHandler.getEventHandlerForSID(getSID())
                    .fireEventOccurred(
                            new SerialisationFailedEvent(ioe.getMessage(),
                                    "[ConceptName - serialise]"));
            throw new StorageException(ioe.getMessage());
        }
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
    public void setPreferred(boolean b) {
        super.setPreferred(b);
        fireUpdateEvent();
    }

    /**
     * Returns a new created object of this class from a byte array.
     *
     * @param aog   AbstractONDEXGraph
     * @param array byte[]
     * @return ConceptName
     */
    public static BerkeleyConceptName deserialise(AbstractONDEXGraph aog, byte[] array) {

        BerkeleyConceptName cn = null;

        try {

            // byte input stream for byte array
            ByteArrayInputStream bais = new ByteArrayInputStream(array);

            // object input stream for deserialisation
            DataInputStream dis = new DataInputStream(new BufferedInputStream(
                    bais));

            // get serialised class content
//			int length = dis.readInt();
//			byte[] parray = new byte[length];
//			dis.read(parray);
//			Permissions perms = Permissions.deserialise(parray);
//			parray = null;

            long sid = dis.readLong();
            int cid = dis.readInt();
            String name = dis.readUTF();
            boolean isPreferred = dis.readBoolean();

            array = null;
            dis.close();
            dis = null;
            bais.close();
            bais = null;
            // create new instance of this class
            cn = new BerkeleyConceptName(sid, cid, name, isPreferred);
//			GlobalPermissions.getInstance(sid).deserializeGraphElement(AbstractConcept.class, eid, parray);

        }
        catch (IOException ioe) {
            ONDEXEventHandler.getEventHandlerForSID(aog.getSID()).fireEventOccurred(new DeserialisationFailedEvent(ioe
                    .getMessage(), "[ConceptName - deserialise]"));
            throw new RetrievalException(ioe.getMessage());
        }

        // return new conceptname or null
        return cn;

    }

}
