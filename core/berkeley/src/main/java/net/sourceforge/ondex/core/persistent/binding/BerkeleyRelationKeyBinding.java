package net.sourceforge.ondex.core.persistent.binding;

import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.core.persistent.BerkeleyEnv;
import net.sourceforge.ondex.core.persistent.BerkeleyRelationKey;
import net.sourceforge.ondex.core.persistent.Persistence;

/**
 * Serialises and deserialises a RelationKey.
 *
 * @author taubertj
 */
public class BerkeleyRelationKeyBinding
        extends AbstractTupleBinding<BerkeleyRelationKey>
{
    public BerkeleyRelationKeyBinding(BerkeleyEnv berkeley) {
        super(berkeley, new Persistence.Deserializer<net.sourceforge.ondex.core.persistent.BerkeleyRelationKey>()
        {
            @Override
            public BerkeleyRelationKey deserialise(AbstractONDEXGraph graph, byte[] buf)
            {
                return BerkeleyRelationKey.deserialise(graph, buf);
            }
        });
    }
}
