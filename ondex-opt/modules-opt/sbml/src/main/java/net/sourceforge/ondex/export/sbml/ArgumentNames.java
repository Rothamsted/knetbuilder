package net.sourceforge.ondex.export.sbml;

public interface ArgumentNames {

	public final static String CONFIG_FILE_ARG = "ConfigFile";
	
	//Sorry wiki syntax, but it is quite clear.
	public final static String CONFIG_FILE_ARG_DESC = "Configuration file specifies converison options that allow to alter the default convertion options to make ONDEX data represetntion compatable with SBML representation.\n"
			+"By default (when no parameters are supplied) concepts are converted to species, relations converted to reactions. This behaviour can be overriden by listing the concept/relation types that should be treated differently in the approprieate sections of the configuration file. The possible options are:\n"
	    		+"**Concept to reaction** - coverts a concept to reaction instead of species (reactions are concepts in ONDEX representation)\n**Relation to regulatory arc** - creates a regulatory arc from the source concept to the reaction for all realtions of this type.\n"
	    		+"**Reverse relation logic** - reverses the process regulatory effect e.g. from \"substrate //is produced by// a reaction\" to \"reaction //produces// a substrate\". In effect it provides a way to make direction of the relations match the direction of real-world processes they represent.\n" 
	    		+"**Convert relation/concept to annotation** - not implemented. Will provide a way of collapsing \"information about\" concepts, like paper references to the annotation tag of the SBML entity.\n\n";

	public final static String EXPORT_AS_ZIP_FILE = "GUNZip";
	public final static String EXPORT_AS_ZIP_FILE_DESC = "When this option is set the file wil be exported as a zip file, this is the preffered option as it reduces disc space requirements by a lot.";
	
	
}
