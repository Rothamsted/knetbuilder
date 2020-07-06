package net.sourceforge.ondex.transformer.graphpath;

import java.util.ArrayList;

public class GraphPath {
	
	private ArrayList<Integer> nodes;
	private ArrayList<Integer> edges;
	
	private int lengthlimit;
	
	public GraphPath(int limit) {
		
		this.nodes = new ArrayList<Integer>();
		this.edges = new ArrayList<Integer>();
		this.lengthlimit = limit;
	
	}
	
	public ArrayList<Integer> getNodes() {
		return nodes;
	}
	
	public ArrayList<Integer> getEdges() {
		return edges;
	}
	
	public void addNode(int nodeid) {
		
		nodes.add(nodeid);

	}
	
	public void addEdge(int edgeid) {
		
		edges.add(edgeid);	
		
	}
	
	public int getLastNode() {
		
		return nodes.get(nodes.size()-1);
		
	}
	
	public boolean containsEdge(int e) {
		return edges.contains(e);
	}
	
	public boolean containsNode(int e) {
		return nodes.contains(e);
	}
	
	public int getLimit() {
		return this.lengthlimit;
	}
	
	public GraphPath clone() {
	
		GraphPath b = new GraphPath(lengthlimit);
		
		for (int n : nodes) {
			b.addNode(n);
		}
		
		for (int e : edges) {
			b.addEdge(e);
		}
		
		return b;
	}
	
	public boolean atLimit() {
		return (nodes.size() >= lengthlimit);
	}
	
	public String toString() {
		
		StringBuilder s = new StringBuilder();
		for (int t = 0; t < nodes.size() - 1; t++) {
			s.append("N");
			s.append(nodes.get(t));
			s.append(" - ");
			s.append("R");
			s.append(edges.get(t));
			s.append(" - ");
		}
		
		s.append("N");
		s.append(nodes.get(nodes.size()-1));
		return s.toString();
		
		
	}
	

}
