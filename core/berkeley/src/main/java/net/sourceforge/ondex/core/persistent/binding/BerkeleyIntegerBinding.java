package net.sourceforge.ondex.core.persistent.binding;

import java.io.IOException;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * Serialises and deserialises a Integer.
 *
 * @author taubertj
 */
public class BerkeleyIntegerBinding extends TupleBinding<Integer> {

    @Override
    public Integer entryToObject(TupleInput ti) {
        Integer key = ti.readInt();
        try {
            ti.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return key;
    }

    @Override
    public void objectToEntry(Integer o, TupleOutput to) {
        to.writeInt(o);
    }
}
