package net.sourceforge.ondex.core.persistent.binding;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import net.sourceforge.ondex.core.persistent.BerkeleyEnv;
import net.sourceforge.ondex.core.persistent.BerkeleyRelationKey;
import net.sourceforge.ondex.core.persistent.BerkeleyRelationKeyName;

/**
 * SecondaryKeyCreator for a BerkeleyRelationKeyName.
 *
 * @author taubertj
 */
public class BerkeleyRelationKeyNameKeyCreator implements
        SecondaryKeyCreator
{
    private BerkeleyEnv berkeleyEnv;

    public BerkeleyRelationKeyNameKeyCreator(BerkeleyEnv berkeleyEnv)
    {
        this.berkeleyEnv = berkeleyEnv;
    }

    public boolean createSecondaryKey(SecondaryDatabase secDb,
                                      DatabaseEntry keyEntry, DatabaseEntry dataEntry,
                                      DatabaseEntry resultEntry) throws DatabaseException
    {
        if (resultEntry == null)
            throw new NullPointerException("resultEntry is null");
        if (keyEntry == null)
            throw new NullPointerException("keyEntry is null");
        if (dataEntry == null)
            throw new NullPointerException("dataEntry is null");
        if (secDb == null)
            throw new NullPointerException("secDb is null");

        BerkeleyRelationKeyName brkn = berkeleyEnv.convert(keyEntry, BerkeleyRelationKeyName.class);
        BerkeleyRelationKey key = brkn.getKey();
        if (key == null)
            throw new NullPointerException("key is null");

        ((BerkeleyRelationKeyBinding) berkeleyEnv.getBinding(BerkeleyRelationKey.class)).objectToEntry(key, resultEntry);
        return true;
    }
}
