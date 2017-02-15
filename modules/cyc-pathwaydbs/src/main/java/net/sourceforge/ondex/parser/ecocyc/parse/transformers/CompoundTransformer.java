package net.sourceforge.ondex.parser.ecocyc.parse.transformers;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.parser.ecocyc.MetaData;
import net.sourceforge.ondex.parser.ecocyc.Parser;
import net.sourceforge.ondex.parser.ecocyc.objects.AbstractNode;
import net.sourceforge.ondex.parser.ecocyc.objects.Compound;

import java.util.HashSet;
import java.util.regex.Pattern;
/**
 * Transforms a net.sourceforge.ondex.parser.ecocyc.sink.Compound to a ONDEXConcept.
 * @author peschr
 */
public class CompoundTransformer extends AbstractTransformer{
	private ConceptClass ccComp;
	private AttributeName attMolWeight = null;
	private RelationType rtCofectorOf = null;
	protected DataSource dataSourceSmiles = null;
	
	public CompoundTransformer(Parser parser) {
		super(parser);
		ccComp = graph.getMetaData().getConceptClass(MetaData.CC_COMPOUND);
		if (ccComp == null) {
			Parser.propagateEventOccurred(new ConceptClassMissingEvent(MetaData.CC_COMPOUND, Parser.getCurrentMethodName()));
		}
		
		rtCofectorOf = graph.getMetaData().getRelationType(MetaData.RT_COFACTORS_BY);
		if (rtCofectorOf == null) {
			Parser.propagateEventOccurred(new RelationTypeMissingEvent(MetaData.RT_COFACTORS_BY, Parser.getCurrentMethodName()));
		}
		
		attMolWeight = graph.getMetaData().getAttributeName(MetaData.ATR_MOLWEIGHT);
		if (attMolWeight == null) {
			Parser.propagateEventOccurred(new AttributeNameMissingEvent(MetaData.ATR_MOLWEIGHT, Parser.getCurrentMethodName()));
		}
		
		dataSourceSmiles = graph.getMetaData().getDataSource(MetaData.CV_SMILES);
		if (dataSourceSmiles == null) {
			Parser.propagateEventOccurred(new DataSourceMissingEvent(MetaData.CV_SMILES, Parser.getCurrentMethodName()));
		}
	}
	
	private static Pattern htmlTags = Pattern.compile("<.*?>");
	
	/**
	 * creates and adds the molweight, systematicname and SMILES to the concept
	 */
	@Override
	public void nodeToConcept(AbstractNode node) {
		Compound compound = (Compound) node;
		ONDEXConcept concept = graph.getFactory().createConcept(compound.getUniqueId(), dataSourceMetaC,
				ccComp, etIMPD);
		compound.setConcept(concept);	
		if ( compound.getMolweight() != null){
			concept.createAttribute(attMolWeight, compound.getMolweight(), false);
		}
		if ( compound.getSystematicName() != null){
			
			String cleanedName = htmlTags.matcher(compound.getSystematicName()).replaceAll("");
			if (compound.getSystematicName().length() != cleanedName.length()) {
				concept.createConceptName(compound.getSystematicName(), false);
				concept.createConceptName(cleanedName, true);
			} else {
				concept.createConceptName(compound.getSystematicName(), true);
			}
		}
		if (compound.getSmiles() != null){
			concept.createConceptAccession(compound.getSmiles(), dataSourceSmiles, false);
		}
	}
	@Override
	public void pointerToRelation(AbstractNode node) {
		HashSet<ONDEXRelation> relations = new HashSet<ONDEXRelation>();
		Compound compound = (Compound) node;
        for (AbstractNode abstractNode : compound.getCofactorOf()) {
            // Compound (cofactor of)=> Enzyme
            relations.add(graph.getFactory().createRelation(abstractNode.getConcept(), compound.getConcept(), rtCofectorOf, etIMPD));
        }
		for( ONDEXRelation relation: relations ){
			for (ONDEXConcept c : node.getConcept().getTags()){
				relation.addTag(c);
			}
		}/*
		for( AbstractNode n: compound.getCofactorOf()){
			Set<ONDEXConcept> toConcepts = node.getConcept().getContext(s);
			while( toConcepts.hasNext() ){
				ONDEXConcept toContext = toConcepts.next();
				if ( !n.getConcept().getContext(s).contains(toContext) )
					n.getConcept().addContext(toContext);
			}
		}*/
		
	}
}
