package net.sourceforge.ondex.tools.subgraph;

import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * 
 * @author lysenkoa
 *
 */
public class ConceptMatcher {
	private final Map<Object, ConceptAttributeChecker> conditions = new LinkedHashMap<Object, ConceptAttributeChecker>();
	private final ONDEXGraph graph;
	
	public ConceptMatcher(ONDEXGraph graph, AttributePrototype ... aps){
		this(graph);
		addConditions(aps);
	}
	
	public ConceptMatcher(ONDEXGraph graph){
		super();
		this.graph = graph;
	}
	
	public void addConditions(AttributePrototype ... aps){
		for(AttributePrototype ap:aps){
			conditions.put(ap, ConceptAttributeChecker.getConceptChecker(ap, graph));	
		}
	}
	
	public void addConditions(ConceptAttributeChecker ... chks){
		for(ConceptAttributeChecker chk:chks){
			conditions.put(chk, chk);	
		}
	}
	
	public void removeConditions(AttributePrototype ... aps){
		for(AttributePrototype ap:aps){
			conditions.remove(ap);	
		}	
	}
	
	public void removeConditions(ConceptAttributeChecker ... chks){
		for(ConceptAttributeChecker chk:chks){
			conditions.remove(chk);	
		}	
	}
	
	public BitSet getMatchingIdSet(Set<ONDEXConcept> ov) throws NullValueException, EmptyStringException, AccessDeniedException{
		BitSet result = new BitSet();
		 for(ONDEXConcept c : ov){
			 boolean addToset = true;
			 for(ConceptAttributeChecker chk :conditions.values()){
				 if(!chk.check(c)){
					 addToset = false;
					 break;
				 }
			 }
			 if(addToset)result.set(c.getId());
		 }
		 return result;		
	}
	
	public Set<ONDEXConcept> getMatchingConcpetSet(Set<ONDEXConcept> ov) throws NullValueException, EmptyStringException, AccessDeniedException{
		return BitSetFunctions.create(graph, ONDEXConcept.class, getMatchingIdSet(ov));
	}
	
	public void filter(Set<ONDEXConcept> ov) throws NullValueException, EmptyStringException, AccessDeniedException{
		 for(ONDEXConcept c : ov){
			 boolean addToset = true;
			 for(ConceptAttributeChecker chk :conditions.values()){
				 if(!chk.check(c)){
					 addToset = false;
					 break;
				 }
			 }
			 System.err.println("Deleted: "+c.getConceptName().getName());
			 if(addToset)graph.deleteConcept(c.getId());
		 }
	}
}
 