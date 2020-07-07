package net.sourceforge.ondex.mapping.inferGoTerm;

/**
 * @author hindlem
 *         Created 15-Apr-2010 12:11:50
 */
public class MetaData {

    // concept classes:
    public static String BioProc = "BioProc";
    public static String MolFunc = "MolFunc";
    public static String CelComp = "CelComp";

    public final static String enzyme = "Enzyme";
    public static String tf = "TF";
    public static String gene = "Gene";
    public static String rna = "RNA";
    public static String protein = "Protein";
    public static String proteinComplex = "Protcmplx";

    public final static String hasFunction = "has_function";//molecular function "MolFunc"
    public final static String hasParticipant = "has_participant";//biological process "BioProc"
    public final static String locatedIn = "located_in";//cellular component "CelComp"

    public final static String hasFunctionNOT = "not_function";
    public final static String hasParticipantNOT = "participates_not";
    public final static String locatedInNOT = "not_located_in";


    final static String ATT_DATASOURCE = "DataSource";
}
