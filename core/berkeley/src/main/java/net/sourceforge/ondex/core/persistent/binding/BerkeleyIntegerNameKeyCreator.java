package net.sourceforge.ondex.core.persistent.binding;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import net.sourceforge.ondex.core.persistent.BerkeleyEnv;
import net.sourceforge.ondex.core.persistent.BerkeleyIntegerName;

/**
 * SecondaryKeyCreator for a IntegerName.
 *
 * @author taubertj
 */
public class BerkeleyIntegerNameKeyCreator implements SecondaryKeyCreator
{
    private BerkeleyEnv berkeleyEnv;

    public BerkeleyIntegerNameKeyCreator(BerkeleyEnv berkeleyEnv)
    {
        this.berkeleyEnv = berkeleyEnv;
    }

    public boolean createSecondaryKey(SecondaryDatabase secDb,
                                      DatabaseEntry keyEntry, DatabaseEntry dataEntry,
                                      DatabaseEntry resultEntry) throws DatabaseException
    {

        BerkeleyIntegerName bin = berkeleyEnv.convert(keyEntry, BerkeleyIntegerName.class);
        ((BerkeleyIntegerBinding) berkeleyEnv.getBinding(Integer.class))
                .objectToEntry(bin.getKey(), resultEntry);
        return true;
    }
}
