package net.sourceforge.ondex.mapping.external2go;

/**
 * Static class to give a overview of the MetaData requirements
 *
 * @author peschr
 */
public interface MetaData {
    //cv
    public static String CV_GO = "GO";
    //relationType
    public static String RT_EQU = "equ";

    public static String RT_MSP = "member_of";
    //evidenceType
    public static String EV_EXTERNAL2GO = "EXTERNAL2GO";

    public static String CC_BioProc = "BioProc";
    public static String CC_CelComp = "CelComp";
    public static String CC_MolFunc = "MolFunc";
    public static String CC_Thing = "Thing";

    //Relation Types
    public final static String RT_hasFunction = "has_function";//molecular function
    public final static String RT_participatesIn = "participates_in";//biological process
    public final static String RT_locatedIn = "located_in";//cellular component

    final static String ATT_DATASOURCE = "DataSource";

}
