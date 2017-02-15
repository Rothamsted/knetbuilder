package net.sourceforge.ondex.parser.drastic;

import java.util.ArrayList;
import java.util.HashMap;

import net.sourceforge.ondex.core.ONDEXConcept;

public class RelationLogger {
	
    HashMap<ONDEXConcept,ArrayList<ONDEXConcept>> relations = new HashMap<ONDEXConcept,ArrayList<ONDEXConcept>>();

	/*
	 * check if there is already a relation between concept a and b
	 */
	public boolean checkInRelation(ONDEXConcept a, ONDEXConcept b) {
		
		if (relations.containsKey(a)) {
			if (relations.get(a).contains(b)) {
				return false;
			} else {
				relations.get(a).add(b);
				return true;
			}
		} else {
			ArrayList<ONDEXConcept> tmp = new ArrayList<ONDEXConcept>();
			tmp.add(b);
			relations.put(a,tmp);
			return true;
		}
		
	}
	
}
