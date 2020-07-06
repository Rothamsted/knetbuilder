package net.sourceforge.ondex.tools.functions;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
/**
 * THis class contains a library of methods to simplify construction of
 * view according to the extended set of conditions.
 * 
 * @author lysenkoa
 *
 */
public class ViewConstruction {
	
	/**
	 * Constructs the view containing all relations associated with particular 
	 * concept class, using the string ids to define the class
	 * @param graph - graph to operate on 
	 * determine which relation  to include in the view.
	 * 
	 * @return - resulting view
	 */
	public static Set<ONDEXRelation> getRelationsOfConceptClasses(ONDEXGraph graph, List<String> ccs){
		Set<ONDEXRelation> result = BitSetFunctions.create(graph, ONDEXRelation.class, new BitSet());
		ONDEXGraphMetaData meta = graph.getMetaData();
		for(String cc : ccs){
			ConceptClass ct = meta.getConceptClass(cc);
			if(ct != null){
				result.addAll(graph.getRelationsOfConceptClass(ct));
			}
		}
		return result;
	}

	/**
	 * Return all of the concept of concept classes with provided ids in the graph
	 * @param graph - Ondex graph
	 * @param strIds - id of concept classes
	 * 
	 * @return view
	 */
	public static final Set<ONDEXConcept> getConceptsOfTypes(ONDEXGraph graph,Collection<String> strIds){
		Set<ONDEXConcept> result = BitSetFunctions.create(graph, ONDEXConcept.class, new BitSet());
		ConceptClass [] ccs = ControledVocabularyHelper.convertConceptClasses(graph, strIds);
		
		for(ConceptClass cc :ccs){
			result.addAll(graph.getConceptsOfConceptClass(cc));
		}
		return result;
		
	}
	
	/**
	 * Return all of the relations of relation types with provided ids in the graph
	 * @param graph - Ondex graph
	 * @param strIds - id of relation types
	 * 
	 * @return view
	 */
	public static final Set<ONDEXRelation> getRelationsOfTypes(ONDEXGraph graph,Collection<String> strIds){
		Set<ONDEXRelation> result = BitSetFunctions.create(graph, ONDEXRelation.class, new BitSet());
		RelationType [] rts = ControledVocabularyHelper.convertRelationTypes(graph, strIds);
		for(RelationType rt : rts){
			BitSetFunctions.or(result, graph.getRelationsOfRelationType(rt));	
		}
		return result;
		
	}
}
