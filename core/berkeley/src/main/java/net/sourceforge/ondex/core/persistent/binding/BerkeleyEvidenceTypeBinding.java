package net.sourceforge.ondex.core.persistent.binding;

import net.sourceforge.ondex.core.base.AbstractONDEXGraph;
import net.sourceforge.ondex.core.persistent.BerkeleyEnv;
import net.sourceforge.ondex.core.persistent.BerkeleyEvidenceType;
import net.sourceforge.ondex.core.persistent.Persistence;

/**
 * Serialises and deserialises a EvidenceType.
 *
 * @author taubertj
 */
public class BerkeleyEvidenceTypeBinding extends
        AbstractTupleBinding<BerkeleyEvidenceType>
{
    public BerkeleyEvidenceTypeBinding(BerkeleyEnv berkeley) {
        super(berkeley, new Persistence.Deserializer<net.sourceforge.ondex.core.persistent.BerkeleyEvidenceType>()
        {
            @Override
            public BerkeleyEvidenceType deserialise(AbstractONDEXGraph graph, byte[] buf)
            {
                return Persistence.deserialise(BerkeleyEvidenceType.FACTORY, buf);
            }
        });
    }
}
