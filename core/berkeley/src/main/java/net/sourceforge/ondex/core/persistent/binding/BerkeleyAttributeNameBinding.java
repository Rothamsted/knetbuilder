package net.sourceforge.ondex.core.persistent.binding;

import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.core.persistent.BerkeleyAttributeName;
import net.sourceforge.ondex.core.persistent.BerkeleyEnv;
import net.sourceforge.ondex.core.persistent.Persistence;

/**
 * Serialises and deserialises a AttributeName.
 *
 * @author taubertj
 */
public class BerkeleyAttributeNameBinding extends AbstractTupleBinding<BerkeleyAttributeName>
{
    public BerkeleyAttributeNameBinding(BerkeleyEnv berkeley)
    {
        super(berkeley, new Persistence.Deserializer<net.sourceforge.ondex.core.persistent.BerkeleyAttributeName>()
        {
            @Override
            public BerkeleyAttributeName deserialise(AbstractONDEXGraph graph, byte[] buf)
            {
                return BerkeleyAttributeName.deserialise(graph, buf);
            }
        });
    }
}
