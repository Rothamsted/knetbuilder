package net.sourceforge.ondex.mapping.sequence2pfam.method;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.programcalls.HMMMatch;
/**
 * Interface for different family to protein matching options
 * @author peschr
 *
 */
public interface IMethod {
	/**
	 * Runs the command returned by <code>getCommandName<code> and returns all valid hits
	 * @return Collection<Result> - the results
	 * @throws IOException
	 * @throws Exception
	 */
	public Collection<HMMMatch> execute() throws IOException, Exception;
	/**
	 * Returns the attributes used to run the choosen mapping method
	 * @return String - command
	 */
	public String[] getCommandArgments();
	/**
	 * Seaches the lucene environment for a concept whith given Accession/Name (depends on the method)
	 * @param lenv - Lucene environment
	 * @param result - the result which should be seached in the lucene environment
	 * @return Set<ONDEXConcept> list of Concepts
	 */
	public Set<ONDEXConcept> searchMatchingConceptsInLuceneEnvironment(LuceneEnv lenv, HMMMatch result);

	/**
	 * Returns the evidence type for the used method
	 * @return EvidenceType - the type
	 */
	public EvidenceType getEvidenceType();
}
