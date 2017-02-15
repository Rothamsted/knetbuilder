package net.sourceforge.ondex.filter.attributevalue;

/**
 * Contains static String content for arguments.
 * 
 * @version 16.04.2008
 * @author taubertj
 * 
 */
public interface ArgumentNames extends
		net.sourceforge.ondex.filter.ArgumentNames {

	public static final String ATTRNAME_ARG = "AttributeName";
	public static final String ATTRNAME_ARG_DESC = "AttributeName to filter out.";

	public static final String VALUE_ARG = "AttributeValue";
	public static final String VALUE_ARG_DESC = "A value which will be matched against the Attributess.";
	
	public static final String IGNORE_ARG = "IgnoreValue";
	public static final String IGNORE_ARG_DESC = "Removes concepts/relations that dont have the AttributeName.";
	
	public static final String INCLUDING_ARG = "Including";
	public static final String INCLUDING_ARG_DESC = "If true keep only concepts/relations that have Attribute value or dont have the Attribute at all. If false remove concepts that have Attribute value.";

	public static final String CC_ARG = "ConceptClass";
	public static final String CC_ARG_DESC = "ConceptClass to filter within (only concepts within this class will be removed): default = all";

	public static final String RTS_ARG = "RelationType";
	public static final String RTS_ARG_DESC = "RelationType to filter within (only relations connected with this type will be removed): default = all";

	public static final String OPERATOR_ARG = "Operator";
	public static final String OPERATOR_ARG_DESC = "A operator that will be used in conjuction with the gds value to determine if the object should be kept or not e.g. (>,<,=,<=,>=) NB mathmatical operators that are not \"=\" are only evaluated on Number Objects, all else will be false. i.e. \"a\" >= \"b\" is false but \"a\" >= \"a\" is true";
	
	public static final String MODULUS_ARG = "Modulus";
	public static final String MODULUS_ARG_DESC = "Treat values as modulus in comparison";
}
