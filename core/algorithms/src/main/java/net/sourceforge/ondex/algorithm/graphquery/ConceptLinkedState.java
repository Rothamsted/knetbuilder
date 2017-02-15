package net.sourceforge.ondex.algorithm.graphquery;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * Where a State can only fulfilled by set conceptids
 * @author hindlem
 *
 */
public class ConceptLinkedState extends State{

	private int cid = -1;
	
	private static Collection<Integer> cidsSet = null;

	/**
	 * 
	 * @param cc the conceptClass of the
	 * @param cids
	 */
	public ConceptLinkedState(ConceptClass cc, int[] cids) {
		super(cc);
		if (cids.length > 1) {
			cidsSet = Collections.synchronizedCollection(new HashSet<Integer>());
			for(int cid: cids) {
				cidsSet.add(cid);
			}
		} else {
			if (cids.length > 0) 
				cid = cids[0];
		}
	}

	public boolean isValidConcept(ONDEXConcept c) {
		System.out.println("ISVALID");
		if (cidsSet == null|| cidsSet.size() == 0) {
			System.out.println("finalf__"+(c.getId() == cid)+" "+c.getId());
			
			return c.getId() == cid;
		} else {
			System.out.println("final__"+cidsSet.contains( c.getId() )+" "+c.getId());
			return cidsSet.contains( c.getId() );
		}
	}
	
}
