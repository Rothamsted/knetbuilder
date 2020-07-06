package net.sourceforge.ondex.parser.poplar_chip;

public class ArgumentNames {
	public static final String TOPNHITS = "TopNHit";
	public static final String TOPNHITS_DESC = "Top N BLAST hits to take from the database";
	
	public static final String EVAL_OR_SCORE = "EvalOrScore";
	public static final String EVAL_OR_SCORE_DESC = "By e-value (true) or bit score (false)";
	
	public static final String EVALCUTOFF = "EvalCutOff";
	public static final String EVALCUTOFF_DESC = "e-value cut off";
	
	public static final String SCORECUTOFF = "ScoreCutOff";
	public static final String SCORECUTOFF_DESC = "bit score cut off";
	
	public static final String DBPARAM = "DbParam";
	public static final String DBPARAM_DESC = "Database parameters, i.e. REFSEQ or UNIPROT_TrEMBL or TAIR or UNIPROT_Swiss-Prot";
}
