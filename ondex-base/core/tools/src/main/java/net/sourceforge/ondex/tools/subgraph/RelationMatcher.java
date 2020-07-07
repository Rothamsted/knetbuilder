package net.sourceforge.ondex.tools.subgraph;

import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * 
 * @author lysenkoa
 *
 */
public class RelationMatcher {
	private final Map<Object, RelationAttributeChecker> conditions = new LinkedHashMap<Object, RelationAttributeChecker>();
	private final ONDEXGraph graph;
	
	public RelationMatcher(ONDEXGraph graph,  AttributePrototype [] aps){
		this(graph);
		addConditions(aps);
	}
	
	public RelationMatcher(ONDEXGraph graph){
		super();
		this.graph = graph;
	}
	
	public void addConditions(AttributePrototype ... aps){
		for(AttributePrototype ap:aps){
			conditions.put(ap, RelationAttributeChecker.getConceptChecker(ap, graph));	
		}
	}
	
	public void addConditions(RelationAttributeChecker ... chks){
		for(RelationAttributeChecker chk:chks){
			conditions.put(chk, chk);	
		}
	}
	
	public void removeConditions(AttributePrototype ... aps){
		for(AttributePrototype ap:aps){
			conditions.remove(ap);	
		}	
	}
	
	public void removeConditions(RelationAttributeChecker ... chks){
		for(RelationAttributeChecker chk:chks){
			conditions.remove(chk);	
		}	
	}
	
	public BitSet getMatchingIdSet(Set<ONDEXRelation> ov) throws NullValueException, EmptyStringException, AccessDeniedException{
		BitSet result = new BitSet();
		 for(ONDEXRelation relation : ov){
			 boolean addToset = true;
			 for(RelationAttributeChecker chk :conditions.values()){
				 if(!chk.check(relation)){
					 addToset = false;
					 break;
				 }
			 }
			 if(addToset)result.set(relation.getId());
		 }
		 return result;		
	}
	
	public Set<ONDEXRelation> getMatchingRelationSet(Set<ONDEXRelation> ov) throws NullValueException, EmptyStringException, AccessDeniedException{
		return BitSetFunctions.create(graph, ONDEXRelation.class, getMatchingIdSet(ov));
	}
	
	public void filter(Set<ONDEXRelation> ov) throws NullValueException, EmptyStringException, AccessDeniedException{
		 for(ONDEXRelation relation : ov){
			 boolean addToset = true;
			 for(RelationAttributeChecker chk :conditions.values()){
				 if(!chk.check(relation)){
					 addToset = false;
					 break;
				 }
			 }
			 if(addToset)graph.deleteRelation(relation.getId());
		 }
	}
}
