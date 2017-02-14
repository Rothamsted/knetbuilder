package net.sourceforge.ondex.programcalls;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.programcalls.exceptions.AlgorithmNotSupportedException;

import java.io.File;
import java.util.Collection;
import java.util.Set;

public interface HMMAlignmentProgram {

	public static String ALGO_HMM_TO_AA = "hmm_vs_aa";
	public static String ALGO_HMM_TO_NA = "hmm_vs_nt";
	public static String ALGO_NA_TO_HMM = "nt_vs_hmm";
	public static String ALGO_AA_TO_HMM = "aa_vs_hmm";
	
	public static String ALGO_HMM_BUILD = "hmmbuild";
	
	/**
	 *
	 * @param og the current ONDEX graph
	 * @param hmm_domains hmm domain file
	 * @param to the target concepts
	 * @param algorithmType see getSupportedAlgorithms() and static definitions of algoritms for implementing class
	 * @throws AlgorithmNotSupportedException algorithmType provided is not valid for this implementation
	 * @throws AlgorithmNotSupportedException algorithmType provided is not valid for this implementation
	 * @throws Exception 
	 */
		Collection<HMMMatch> query(
				ONDEXGraph og, 
				File hmm_domains, 
				String hmmThreshold,
				Set<ONDEXConcept> to, 
				String algorithmType) throws Exception;
		
		String[] getSupportedAlgorithms();

}
