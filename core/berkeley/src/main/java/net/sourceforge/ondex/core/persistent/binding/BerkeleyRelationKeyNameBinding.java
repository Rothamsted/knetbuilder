package net.sourceforge.ondex.core.persistent.binding;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import net.sourceforge.ondex.core.persistent.BerkeleyEnv;
import net.sourceforge.ondex.core.persistent.BerkeleyRelationKey;
import net.sourceforge.ondex.core.persistent.BerkeleyRelationKeyName;

import java.io.IOException;

/**
 * Serialises and deserialises a BerkeleyRelationKeyName.
 *
 * @author taubertj
 */
public class BerkeleyRelationKeyNameBinding extends
        TupleBinding<BerkeleyRelationKeyName>
{

    private final BerkeleyEnv berkeley;

    public BerkeleyRelationKeyNameBinding(BerkeleyEnv berkeley) {
        this.berkeley = berkeley;
    }

    @Override
    public BerkeleyRelationKeyName entryToObject(TupleInput ti) {
        String name = ti.readString();
        byte[] keyBuf = new byte[ti.available()];
        ti.readFast(keyBuf);
        BerkeleyRelationKey key = BerkeleyRelationKey.deserialise(berkeley
                .getAbstractONDEXGraph(), keyBuf);
        try {
            ti.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (key == null)
            throw new NullPointerException(
                    "BerkeleyRelationKeyNameBinding key is null ");
        if (name == null)
            throw new NullPointerException(
                    "BerkeleyRelationKeyNameBinding name is null ");
        return new BerkeleyRelationKeyName(key, name);
    }

    @Override
    public void objectToEntry(BerkeleyRelationKeyName o, TupleOutput to) {
        to.writeString(o.getName());
        to.write(o.getKey().serialise());
    }
}
