package net.sourceforge.ondex.core.persistent.binding;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import net.sourceforge.ondex.core.persistent.BerkeleyIntegerName;

import java.io.IOException;

/**
 * Serialises and deserialises a BerkeleyIntegerName.
 *
 * @author taubertj
 */
public class BerkeleyIntegerNameBinding extends
        TupleBinding<BerkeleyIntegerName>
{

    @Override
    public BerkeleyIntegerName entryToObject(TupleInput ti) {
        int key = ti.readInt();
        String name = ti.readString().intern();
        try {
            ti.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new BerkeleyIntegerName(key, name);
    }

    @Override
    public void objectToEntry(BerkeleyIntegerName o, TupleOutput to) {
        to.writeInt(o.getKey());
        to.writeString(o.getName());
    }
}
