package net.sourceforge.ondex.core.persistent.binding;

import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.core.persistent.BerkeleyDataSource;
import net.sourceforge.ondex.core.persistent.BerkeleyEnv;
import net.sourceforge.ondex.core.persistent.Persistence;

/**
 * Serialises and deserialises a DataSource.
 *
 * @author taubertj
 */
public class BerkeleyDataSourceBinding extends AbstractTupleBinding<BerkeleyDataSource>
{
    public BerkeleyDataSourceBinding(BerkeleyEnv berkeley)
    {
        super(berkeley, new Persistence.Deserializer<net.sourceforge.ondex.core.persistent.BerkeleyDataSource>()
        {
            @Override
            public BerkeleyDataSource deserialise(AbstractONDEXGraph graph, byte[] buf)
            {
                return Persistence.deserialise(BerkeleyDataSource.FACTORY, buf);
            }
        });
    }
}
