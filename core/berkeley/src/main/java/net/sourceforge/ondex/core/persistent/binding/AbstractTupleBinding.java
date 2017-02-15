package net.sourceforge.ondex.core.persistent.binding;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import net.sourceforge.ondex.core.persistent.BerkeleyEnv;
import net.sourceforge.ondex.core.persistent.BerkeleySerializable;
import net.sourceforge.ondex.core.persistent.Persistence;

import java.io.IOException;

public class AbstractTupleBinding<T extends BerkeleySerializable> extends
        TupleBinding<T>
{
    protected final BerkeleyEnv berkeley;
    private final Persistence.Deserializer<T> deserializer;

    public AbstractTupleBinding(BerkeleyEnv berkeley, Persistence.Deserializer<T> deserializer)
    {
        this.berkeley = berkeley;
        this.deserializer = deserializer;
    }

    @Override
    public final T entryToObject(TupleInput ti)
    {
        byte[] buf = new byte[ti.available()];
        ti.readFast(buf);
        T t = deserializer.deserialise(berkeley.getAbstractONDEXGraph(), buf);
        try {
            ti.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return t;
    }

    @Override
    public final void objectToEntry(T o, TupleOutput to)
    {
        to.writeFast(o.serialise());
    }
}