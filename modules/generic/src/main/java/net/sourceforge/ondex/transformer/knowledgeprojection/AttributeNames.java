package net.sourceforge.ondex.transformer.knowledgeprojection;

public interface AttributeNames {
	public static final String REMOVE_ARG = "Remove_transferred?";
	public static final String REMOVE_ARG_DESC = "If true will delete all planes leaving just the primary one, after the processing is complete";
	public static final String TRANSFERRING_RELATION_ARG = "MappingRelation";
	public static final String TRANSFERRING_RELATION_ARG_DESC = "The relation along wich the network structure is going to be projected";
	public static final String GDS_NAME_ARG = "Plane_defining_attribute";
	public static final String GDS_NAME_ARG_DESC = "The attribute that defines the planes";
	public static final String GDS_VALUE_ARG = "Target_plane_att_value";
	public static final String GDS_VALUE_ARG_DESC = "The value of Plane_defining_attribute that identifies the target plane, that the information will be projected to.";
}
