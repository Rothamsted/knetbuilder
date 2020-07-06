package net.sourceforge.ondex.parser.ecocyc.parse.transformers;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.parser.ecocyc.MetaData;
import net.sourceforge.ondex.parser.ecocyc.Parser;
import net.sourceforge.ondex.parser.ecocyc.objects.AbstractNode;
import net.sourceforge.ondex.parser.ecocyc.objects.Publication;
import net.sourceforge.ondex.tools.functions.StandardFunctions;
/**
 * Transforms a net.sourceforge.ondex.parser.ecocyc.sink.Publication to a Concept.
 * @author peschr
 */
public class PublicationTransformer extends AbstractTransformer{
	private DataSource dataSourceNLM = null;
	private AttributeName attTitle = null;
	
	public PublicationTransformer(Parser parser) {
		super(parser);
		attTitle = graph.getMetaData().getAttributeName(MetaData.ATR_PUBLICATION_TITLE);
		if (attTitle == null) {
			Parser.propagateEventOccurred(new RelationTypeMissingEvent(
					MetaData.ATR_PUBLICATION_TITLE, Parser
							.getCurrentMethodName()));
		}
		dataSourceNLM = graph.getMetaData().getDataSource( MetaData.CV_PubMedID);
		if (dataSourceNLM == null) {
			Parser.propagateEventOccurred(new RelationTypeMissingEvent(
					MetaData.CV_PubMedID, Parser
							.getCurrentMethodName()));
		}
	}

	@Override
	public void nodeToConcept(AbstractNode node) {
		Publication pub = (Publication) node;
		if(StandardFunctions.nullChecker(pub.getUniqueId(), dataSourceMetaC, ccPublication, etIMPD))
			return;
		ONDEXConcept concept = this.graph.getFactory().createConcept(pub.getUniqueId(), dataSourceMetaC, ccPublication, etIMPD);
		
		if (pub.getPubMedId() != 0){
			concept.createConceptAccession(new String(pub.getPubMedId() + ""), dataSourceNLM, false);
		}
		if (pub.getTitle() != null &&  !pub.getTitle().equals("")){
			concept.createAttribute(attTitle, pub.getTitle(), false);
		}
		pub.setConcept(concept);
	}

	@Override
	public void pointerToRelation(AbstractNode node) {

	}

}
