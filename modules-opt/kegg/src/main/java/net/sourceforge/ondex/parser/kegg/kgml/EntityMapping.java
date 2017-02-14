package net.sourceforge.ondex.parser.kegg.kgml;

import java.util.HashMap;
import java.util.Map;

public class EntityMapping {

	public static Map<String, String> type2ConceptClass;

	public static Map<String, Integer> type2Shape;

	/**
	 * Returns mapping of KEGG entry type to ONDEX ConceptClass.
	 * 
	 * @param type
	 *            the KEGG entry type
	 * @return the ONDEX ConceptClass
	 */
	public static String getConceptClassMapping(String type) {

		// massage string here
		if (type != null) {
			type = type.trim();
			type = type.toUpperCase();
		}

		// initialise mapping table
		if (type2ConceptClass == null) {
			type2ConceptClass = new HashMap<String, String>(10);
			type2ConceptClass.put("COMPOUND", "Comp");
			type2ConceptClass.put("ENZYME", "Enzyme");
			type2ConceptClass.put("GENE", "Gene");
			type2ConceptClass.put("MAP", "Path");
			type2ConceptClass.put("ORTHOLOG", "KO");
			type2ConceptClass.put("OTHER", "Thing");
			type2ConceptClass.put("GROUP", "KO");
			type2ConceptClass.put("REACTION", "Reaction");
		}
		return type2ConceptClass.get(type);
	}

	/**
	 * Returns mapping of KEGG graphical type to ONDEX shape ID.
	 * 
	 * @param type
	 *            the KEGG graphical type
	 * @return the ONDEX shape ID
	 */
	public static Integer getGraphicsTypeMapping(String type) {

		// massage string here
		if (type != null) {
			type = type.trim();
			type = type.toUpperCase();
		}

		// initialise mapping table
		if (type2Shape == null) {
			type2Shape = new HashMap<String, Integer>(10);
			type2Shape.put("RECTANGLE", 1);
			type2Shape.put("ROUNDRECTANGLE", 2);
			type2Shape.put("CIRCLE", 9);
			type2Shape.put("LINE", -1); // indicates non ONDEX shape
		}
		return type2Shape.get(type);
	}

}
