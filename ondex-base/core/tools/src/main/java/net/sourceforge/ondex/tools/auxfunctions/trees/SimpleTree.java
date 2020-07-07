package net.sourceforge.ondex.tools.auxfunctions.trees;

/**
 * 
 * Auxiliary functions for parsers
 * SimpleTree
 * 
 * Simple Tree structure for holding things 
 * 
 * Usage:
 * 
 * Creation:
 * SimpleTree st = new SimpleTree();
 * 
 * Adding contents to a node
 * st.addContents(Object c);
 * 
 * Get contents from a node:
 * st.getContents();
 * 
 * Adding a branch:
 * st.addBranch("name of branch");
 * 
 * Deleting a branch:
 * st.deleteBranch("name of branch");
 * 
 * Checking if a branch exists:
 * st.hasKey("name of branch"); 
 * 
 *   
 * @author sckuo
 * 
 */

import java.util.HashMap;
import java.util.Map;



public class SimpleTree {
	
	private Map<Object,SimpleTree> branches;
	private Object contents;
	
	public SimpleTree() {
		
		branches = new HashMap<Object, SimpleTree>();
		
	}
	
	public SimpleTree addBranch(Object a) {
		
		if (branches.containsKey(a)) {
			return branches.get(a);
		} else {
			SimpleTree l = new SimpleTree();
			branches.put(a, l);
			return l;
		}
		
	}
	
	public SimpleTree getBranch(Object a) {
		return branches.get(a);
	}
	
	public boolean hasKey(Object a) {
		return branches.containsKey(a);
	}
	
	public Object getContents() {
		return contents;
	}
	
	public void addContents(Object c) {
		this.contents = c;
	}
	
	public void deleteBranch(Object c) {
		branches.remove(c);
	}

}
