package net.sourceforge.ondex.mapping.crossspecies;

/**
 * Provides methods for BLAST related tasks.
 * 
 * @author taubertj
 *
 */
public class BlastUtil implements MetaData {

	/**
	 * Returns BLAST type for a given sequence type.
	 * 
	 * @param sequenceTypes - String
	 * @return String
	 */
	public static String getFlavourOfBlast (String sequenceTypes){
		if (sequenceTypes.equals(ATT_AMINO_ACID_SEQ)){
			return "blastp";
		}
		return "blastn";
	}
}
