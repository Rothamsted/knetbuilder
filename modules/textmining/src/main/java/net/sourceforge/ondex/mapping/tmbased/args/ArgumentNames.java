package net.sourceforge.ondex.mapping.tmbased.args;

public final class ArgumentNames {

	public final static String CONCEPTCLASS_ARG = "ConceptClass";
	public final static String CONCEPTCLASS_DESC = "The conceptclass (terminology) that should be mapped to publications using text mining mapping methods";
	
	public final static String SEARCH_STRATEGY_ARG = "Search";
	public final static String SEARCH_STRATEGY_DESC = "How do you like to search: Exact, And, Fuzzy or Proximity";
	
	public final static String PREFERRED_NAMES_ARG = "OnlyPreferredNames";
	public final static String PREFERRED_NAMES_ARG_DESC = "Set true to consider only preferred names for text mining.";

	public final static String USE_FULLTEXT_ARG = "UseFullText";
	public final static String USE_FULLTEXT_ARG_DESC = "Set true to include the full text if it is available on a publication.";

	public final static String FILTER_ARG = "Filter";
	public final static String FILTER_DESC = "Which mappings should be filtered out: lowscore, maxspecificity, besthits";
	
}
