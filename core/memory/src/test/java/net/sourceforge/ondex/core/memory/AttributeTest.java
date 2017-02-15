package net.sourceforge.ondex.core.memory;

import junit.framework.TestCase;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

import org.junit.Test;

/**
 * @author hindlem
 *         Created 25-Apr-2010 16:01:21
 */
public class AttributeTest extends TestCase {

	@Test
    public void testEqualityAttribute() {
		
		ONDEXGraph og = new MemoryONDEXGraph("test");
		
		DataSource elementOf = og.getMetaData().getFactory().createDataSource("cv");
		ConceptClass ofType = og.getMetaData().getFactory().createConceptClass("cc");
		EvidenceType evidence = og.getMetaData().getFactory().createEvidenceType("et");
		
		ONDEXConcept c1 = og.getFactory().createConcept("c1", elementOf, ofType, evidence);
		ONDEXConcept c2 = og.getFactory().createConcept("c2", elementOf, ofType, evidence);
		
		AttributeName an1 = og.getMetaData().getFactory().createAttributeName("an1", String.class);
		AttributeName an2 = og.getMetaData().getFactory().createAttributeName("an2", Double.class);
		
		Attribute attribute1 = c1.createAttribute(an1, "test", false);
		Attribute attribute2 = c2.createAttribute(an1, "test", false);
		Attribute attribute3 = c2.createAttribute(an2, 1.0, false);
		Attribute attribute4 = c1.createAttribute(an2, 2.0, false);
		
		assertEquals("equals on Attribute", attribute1, attribute2);
		assertFalse("different attribute name", attribute2.equals(attribute3));
		assertFalse("different content", attribute3.equals(attribute4));
    }
}
