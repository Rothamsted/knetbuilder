package net.sourceforge.ondex.core.persistent.binding;

import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.core.persistent.BerkeleyConceptClass;
import net.sourceforge.ondex.core.persistent.BerkeleyEnv;
import net.sourceforge.ondex.core.persistent.Persistence;

/**
 * Serialises and deserialises a ConceptClass.
 *
 * @author taubertj
 */
public class BerkeleyConceptClassBinding extends
        AbstractTupleBinding<BerkeleyConceptClass>
{

    public BerkeleyConceptClassBinding(BerkeleyEnv berkeley) {
        super(berkeley, new Persistence.Deserializer<net.sourceforge.ondex.core.persistent.BerkeleyConceptClass>()
        {
            @Override
            public BerkeleyConceptClass deserialise(AbstractONDEXGraph graph, byte[] buf)
            {
                return BerkeleyConceptClass.deserialise(graph, buf);
            }
        });
    }
}
