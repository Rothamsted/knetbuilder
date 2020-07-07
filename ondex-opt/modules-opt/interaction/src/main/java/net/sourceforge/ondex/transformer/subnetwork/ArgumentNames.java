package net.sourceforge.ondex.transformer.subnetwork;
/**
 * 
 * @author lysenkoa
 *
 */
public interface ArgumentNames {
	public static final String DATA_ATT_ARG = "starts_with";
	public static final String DATA_ATT_ARG_DESC = "Only gds arguments whose names starts with this substing will be used for the analysis";
	public static final String MARKER_ARG = "marker";
	public static final String MARKER_ARG_DESC = "Arguemt name ending that is used to identify classes of observations";
	public static final String INFO_ARG ="r";
	public static final String INFO_ARG_DESC = "Minimum information increase that the candidate node must bring to the network";
	public static final String DISTANCE_ARG ="d";
	public static final String DISTANCE_ARG_DESC = "Neighbour distance cutoff";
	public static final String GROUP_NAME_ARG = "Subnetwork_name";
	public static final String GROUP_NAME_ARG_DESC = "Argument name that will be used to identify subnetworks";
	public static final String P1_ARG ="p1";
	public static final String P1_ARG_DESC = "Probability in null distribution where gene expression vectors are randomly shuffled, the same search persidure, new set of subnetworks is drawn and scored(100 trials).";
	public static final String P2_ARG ="p2";
	public static final String P2_ARG_DESC = "Probability in local null distribution - 100 random networks intialised with the same seed.";
	public static final String P3_ARG ="p3";
	public static final String P3_ARG_DESC = "Probability in  null distribution where categories are randomly asssigned, but the subnetworks are the same as the real ones. Each subnetwork's score is compared to the pulled distribution from the scores of this subnetwork across all 20,000 trials.";

}
