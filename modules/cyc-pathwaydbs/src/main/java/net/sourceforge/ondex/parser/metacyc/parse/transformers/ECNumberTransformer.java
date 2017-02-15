package net.sourceforge.ondex.parser.metacyc.parse.transformers;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.parser.metacyc.MetaData;
import net.sourceforge.ondex.parser.metacyc.Parser;
import net.sourceforge.ondex.parser.metacyc.objects.AbstractNode;
import net.sourceforge.ondex.parser.metacyc.objects.ECNumber;
/**
 * Transforms a net.sourceforge.ondex.parser.metacyc.sink.ECNumber to a Concept.
 * @author peschr
 */
public class ECNumberTransformer extends AbstractTransformer {
	private ConceptClass ccEC;
	private DataSource dataSourceEC;
	
	public ECNumberTransformer(Parser parser) {
		super(parser);
		ccEC = graph.getMetaData().getConceptClass(MetaData.CC_EC);
		if (ccEC == null) {
			Parser.propagateEventOccurred(new DataSourceMissingEvent(MetaData.CC_EC, Parser.getCurrentMethodName()));
		}
		dataSourceEC = graph.getMetaData().getDataSource(MetaData.CV_EC);
		if (dataSourceEC == null) {
			Parser.propagateEventOccurred(new DataSourceMissingEvent(MetaData.CV_EC, Parser.getCurrentMethodName()));
		}
	}

	@Override
	public void nodeToConcept(AbstractNode node) {
		ECNumber ecNumber = (ECNumber) node;
		ONDEXConcept concept = graph.getFactory().createConcept(ecNumber.getUniqueId(), dataSourceMetaC,
				ccEC, etIMPD);
		concept.createConceptAccession(ecNumber.getUniqueId(), dataSourceEC, false);
		ecNumber.setConcept(concept);	
	}

	@Override
	public void pointerToRelation(AbstractNode node) {
		//do nothing
	}

}
