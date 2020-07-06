package net.sourceforge.ondex.parser.aracyc.parse.transformers;

import java.util.HashSet;
import java.util.Iterator;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.parser.aracyc.MetaData;
import net.sourceforge.ondex.parser.aracyc.Parser;
import net.sourceforge.ondex.parser.aracyc.objects.AbstractNode;
import net.sourceforge.ondex.parser.aracyc.objects.Enzyme;
import net.sourceforge.ondex.parser.aracyc.objects.Protein;
/**
 * Transforms a net.sourceforge.ondex.parser.aracyc2.sink.Protein to a Concept.
 * @author peschr
 */
public class ProteinTransformer extends AbstractTransformer{

	ConceptClass ccProtein = null;
	ConceptClass ccProteinComplex = null;

	RelationType rtIsPartOf = null;
	RelationType rtMemberIsPartOf = null;
	private AttributeName attTaxId = null;	

	public ProteinTransformer(Parser parser) {
		super(parser);
		try{
			rtMemberIsPartOf = graph.getMetaData().getRelationType(
					MetaData.RT_MEMBER_IS_PART_OF);
			if (rtMemberIsPartOf == null) {
				Parser.propagateEventOccurred(new RelationTypeMissingEvent(
						MetaData.RT_MEMBER_IS_PART_OF, Parser
								.getCurrentMethodName()));
			}
			rtIsPartOf = graph.getMetaData().getRelationType(
					MetaData.RT_IS_PART_OF);
			if (rtIsPartOf == null) {
				Parser.propagateEventOccurred(new RelationTypeMissingEvent(
						MetaData.RT_IS_PART_OF, Parser.getCurrentMethodName()));
			}
			ccProtein = graph.getMetaData().getConceptClass(MetaData.CC_Protein);
			if (ccProtein == null) {
				Parser.propagateEventOccurred(new ConceptClassMissingEvent(
						MetaData.CC_Protein, Parser.getCurrentMethodName()));
			}
			ccProteinComplex = graph.getMetaData().getConceptClass(
					MetaData.CC_PROTEIN_COMPLEX);
			if (ccProteinComplex == null) {
				Parser.propagateEventOccurred(new ConceptClassMissingEvent(
						MetaData.CC_PROTEIN_COMPLEX, Parser
								.getCurrentMethodName()));
			}
			attTaxId = graph.getMetaData().getAttributeName(
					MetaData.ATR_TAXID);
			if (attTaxId == null) {
				Parser.propagateEventOccurred(new AttributeNameMissingEvent(
						MetaData.ATR_TAXID, Parser.getCurrentMethodName()));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void nodeToConcept(AbstractNode node) {
		Protein protein = (Protein) node;
		ConceptClass cc = null;
		if (protein.isComplex())
			cc = ccProteinComplex;
		else
			cc = ccProtein;

		ONDEXConcept concept = graph.getFactory().createConcept(protein.getUniqueId(), dataSourceAraC,
				cc, etIMPD);
		
		concept.createAttribute( attTaxId, MetaData.TAXID, false);
		int monomerPos = protein.getUniqueId().indexOf("-MONOMER");
		if (monomerPos != -1){
			concept.createConceptAccession(protein.getUniqueId().subSequence(0, monomerPos).toString(), super.dataSourceTAIR, true);
			String nameWithoutMonomer = protein.getUniqueId().subSequence(0, monomerPos).toString();
			if ( !protein.getSynonym().contains(nameWithoutMonomer))
				concept.createConceptName(nameWithoutMonomer,false );
		}
		protein.setConcept(concept);
	}

	@Override
	public void pointerToRelation(AbstractNode node) {
		Protein protein = (Protein) node;
		HashSet<ONDEXConcept> nonRedundant = super.getNonRedundant(null, protein.getConcept());

		if( protein.getComponentOf().size() > 0){
			Iterator<Protein> proteins = protein.getComponentOf().iterator();
			//Protein (is part of)=> Protein
			while(proteins.hasNext()){
				Protein p = proteins.next();
				
				super.copyContext(p.getConcept(), nonRedundant);
				super.copyContext(graph.getFactory().createRelation(node.getConcept(), p.getConcept(), rtIsPartOf, etIMPD),
						nonRedundant);
			}
			//Protein (is memember of)=> Enzyme(Complex)
			Iterator<Enzyme> enzymes = protein.getIsMemberOf().iterator();
			while(enzymes.hasNext()){
				Enzyme e = enzymes.next();
				super.copyContext(e.getConcept(), nonRedundant);
				super.copyContext(
						graph.getFactory().createRelation(node.getConcept(), e.getConcept(), rtMemberIsPartOf, etIMPD),
						nonRedundant);
			}
		}
	}
}
