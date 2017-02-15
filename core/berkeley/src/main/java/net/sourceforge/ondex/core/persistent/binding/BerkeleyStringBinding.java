package net.sourceforge.ondex.core.persistent.binding;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import java.io.IOException;

/**
 * Serialises and deserialises a String.
 *
 * @author taubertj
 */
public class BerkeleyStringBinding extends TupleBinding<String>
{

    @Override
    public String entryToObject(TupleInput ti) {
        String key = ti.readString().intern();
        try {
            ti.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return key;
    }

    @Override
    public void objectToEntry(String o, TupleOutput to) {
        to.writeString(o);
    }
}
