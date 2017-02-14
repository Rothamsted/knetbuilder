package net.sourceforge.ondex.filter.nohits;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

import java.util.Set;

/**
 * A Filter to remove all UniProt proteins that do not have any
 * sequence similarity to the other proteins
 * 
 * @author keywan
 *
 */
public class Filter extends ONDEXFilter
{
	
	private Set<ONDEXConcept> concepts;

	private Set<ONDEXRelation> relations;

	@Override
	public void copyResultsToNewGraph(ONDEXGraph exportGraph) {
		ONDEXGraphCloner graphCloner = new ONDEXGraphCloner(graph, exportGraph);
		for (ONDEXConcept c : concepts) {
			graphCloner.cloneConcept(c);
		}
		for (ONDEXRelation r : relations) {
			graphCloner.cloneRelation(r);
		}
	}

	@Override
	public Set<ONDEXConcept> getVisibleConcepts() {
		return BitSetFunctions.unmodifiableSet(concepts);
	}

	@Override
	public Set<ONDEXRelation> getVisibleRelations() {
		return BitSetFunctions.unmodifiableSet(relations);
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[0];
	}

	@Override
	public String getId() {
		return "nohits";
	}

	@Override
	public String getName() {
		return "NoHits Filter";
	}

	@Override
	public String getVersion() {
		return "05/02/2009";
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	@Override
	public void start() {
		
		ONDEXGraphMetaData md = graph.getMetaData();
		ConceptClass ccProt = md.getConceptClass(MetaData.CC_PROTEIN);
		DataSource dataSourceUniProtSP = md.getDataSource(MetaData.CV_UNIPROTKB_SP);
		DataSource dataSourceUniProtTR = md.getDataSource(MetaData.CV_UNIPROTKB_TR);

		Set<ONDEXConcept> v1 = BitSetFunctions.or(
				graph.getConceptsOfDataSource(dataSourceUniProtSP), graph.getConceptsOfDataSource(dataSourceUniProtTR));
		RelationType rtSimSeq = md.getRelationType(MetaData.RT_HSS);

		Set<ONDEXConcept> v2 = graph.getConceptsOfConceptClass(ccProt);
		Set<ONDEXConcept> protSet = BitSetFunctions.and(v1, v2);
		
		//delete UniProt proteins with no hits to Poplar
		int numRemovedProteins = 0;
		for (ONDEXConcept prot : protSet) {
			boolean hasHits = false;
			for (ONDEXRelation rel : graph.getRelationsOfConcept(prot)) {
				if(rel.getOfType().equals(rtSimSeq) &&
						(!rel.getFromConcept().getElementOf().equals(dataSourceUniProtSP) ||
								!rel.getFromConcept().getElementOf().equals(dataSourceUniProtTR))){
					hasHits = true;
					break;
				}
			}
			if(!hasHits){
				graph.deleteConcept(prot.getId());
				numRemovedProteins++;
			}
		}
		
		concepts = graph.getConcepts();
		relations = graph.getRelations();
		
		System.out.println("Removed UniProt proteins: "+numRemovedProteins);
	}
	
	
	class MetaData{
		public static final String RT_HSS = "h_s_s";
		public final static String CC_PROTEIN = "Protein";
		public final static String CV_UNIPROTKB_SP = "UNIPROTKB-SwissProt";
		public final static String CV_UNIPROTKB_TR = "UNIPROTKB-TrEMBL";
	}
	

}


