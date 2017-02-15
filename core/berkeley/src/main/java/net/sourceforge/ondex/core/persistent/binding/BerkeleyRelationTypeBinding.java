package net.sourceforge.ondex.core.persistent.binding;

import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.core.persistent.BerkeleyEnv;
import net.sourceforge.ondex.core.persistent.BerkeleyRelationType;
import net.sourceforge.ondex.core.persistent.Persistence;

/**
 * Serialises and deserialises a RelationType.
 *
 * @author taubertj
 */
public class BerkeleyRelationTypeBinding extends
        AbstractTupleBinding<BerkeleyRelationType>
{
    public BerkeleyRelationTypeBinding(BerkeleyEnv berkeley) {
        super(berkeley, new Persistence.Deserializer<net.sourceforge.ondex.core.persistent.BerkeleyRelationType>()
        {
            @Override
            public BerkeleyRelationType deserialise(AbstractONDEXGraph graph, byte[] buf)
            {
                return BerkeleyRelationType.deserialise(graph, buf);
            }
        });
    }
}
