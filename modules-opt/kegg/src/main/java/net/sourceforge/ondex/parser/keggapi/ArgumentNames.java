package net.sourceforge.ondex.parser.keggapi;

/**
 * Arguments used by the keggapi parser
 * 
 * @author taubertj
 *
 */
public interface ArgumentNames {

	public static final String ORG_ARG = "Org";
	
	public static final String ORG_ARG_DESC = "Kegg Organism code (usually 3 letters), see http://www.genome.jp/kegg/catalog/org_list.html";
	
	public static final String MAP_ARG = "Map";
	
	public static final String MAP_ARG_DESC = "Kegg Pathway Map number (5 digits, e.g. 00010), see http://www.genome.jp/kegg-bin/show_organism?menu_type=pathway_maps&org=rno";
	
}
