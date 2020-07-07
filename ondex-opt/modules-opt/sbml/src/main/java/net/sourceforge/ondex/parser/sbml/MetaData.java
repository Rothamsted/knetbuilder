package net.sourceforge.ondex.parser.sbml;

/**
 * Contains meta data used in parser.
 * 
 * @author taubertj
 * @version 22.11.2011
 */
public interface MetaData {

	public static final String CV_SBML = "SBML";

	public static final String ET_SBML = "SBML_import";

	public static final String AN_SBO = "SBO";

	public static final String RT_CONSUMED_BY = "cs_by";

	public static final String RT_PRODUCED_BY = "pd_by";

	public static final String RT_REGULATED_BY = "rg_by";

	public static final String RT_TRANSFORMATION_OF = "transformation_of";

	public static final String CC_REACTION = "Reaction";

	public static final String CC_COMPOUND = "Comp";

	public static final String CC_CELCOMP = "CelComp";

}
