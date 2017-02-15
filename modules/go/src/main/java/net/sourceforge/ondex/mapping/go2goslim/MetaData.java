package net.sourceforge.ondex.mapping.go2goslim;

/**
 * 
 * @author hindlem
 *
 */
public interface MetaData {
	
	// concept classes:
	public static String BioProc = "BioProc";
	public static String MolFunc = "MolFunc";
	public static String CelComp = "CelComp";
	
	//Relation Types
	public final static String hasFunction = "has_function";//molecular function
	public final static String hasParticipant = "has_participant";//biological process
	public final static String locatedIn = "located_in";//cellular component
	
	public static String notLocatedIn = "not_function";
	public static String notParticipant = "not_participant";
	public static String notFunction = "not_located_in";
	
	// cv
	public static String cvGO = "GO";
	public static String cvGOSLIM = "GOSLIM";

	//relation types
	public static String is_a = "is_a";

	public static String is_p = "part_of";

}
