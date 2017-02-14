package net.sourceforge.ondex.programcalls;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.programcalls.exceptions.AlgorithmNotSupportedException;
import net.sourceforge.ondex.programcalls.exceptions.MissingFileException;

import java.util.Collection;
import java.util.Set;

/**
 * 
 * @author hindlem
 *
 */
public interface BLASTAlignmentProgram {

	public static String ALGO_PSIBLAST = "PSI-BLAST";
	public static String ALGO_TBLASTX = "tBLASTx";
	public static String ALGO_TBLASTN = "tBLASTn";
	public static String ALGO_BLASTP = "BLASTp";
	public static String ALGO_BLASTN = "BLASTn";
	public static String ALGO_BLASTX = "BLASTx";
	
/**
 * 
 * @param s the current session
 * @param og the current ONDEX graph
 * @param from the query concepts
 * @param to the target concepts
 * @param algorithmType see getSupportedAlgorithms() and static definitions of algoritms for implementing class
 * @throws AlgorithmNotSupportedException algorithmType provided is not valid for this implementation
 * @throws AlgorithmNotSupportedException algorithmType provided is not valid for this implementation
 * @throws Exception 
 */
	Collection<Match> query(
			ONDEXGraph og, 
			Set<ONDEXConcept> from,
			Set<ONDEXConcept> to, 
			String algorithmType) throws Exception;
	
	String[] getSupportedAlgorithms();
}
