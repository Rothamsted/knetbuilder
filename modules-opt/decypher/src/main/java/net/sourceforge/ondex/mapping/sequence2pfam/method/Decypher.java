package net.sourceforge.ondex.mapping.sequence2pfam.method;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.core.searchable.LuceneQueryBuilder;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.mapping.sequence2pfam.MetaData;
import net.sourceforge.ondex.programcalls.HMMMatch;
import net.sourceforge.ondex.programcalls.decypher.DecypherAlignment;
import net.sourceforge.ondex.programcalls.exceptions.AlgorithmNotSupportedException;
import net.sourceforge.ondex.programcalls.exceptions.MissingFileException;
import org.apache.lucene.search.Query;

import java.io.File;
import java.util.Collection;
import java.util.Set;

/**
 * Decypher based mapping method
 * @author peschr
 *
 */
public class Decypher extends AbstractMethod {
	private static final String EV_HMMER = "HMMER";

	private EvidenceType hmmer;

	private ConceptClass ccPfam;

	private DataSource dataSourcePfam;

	public Decypher(String programDir, String pfamPfad, String tmpDir,
			String evalue, String bitscore, String hmmThreshold,
			ONDEXGraph graph, AttributeName seqAtt, ConceptClass conceptType) {
		super(programDir, pfamPfad, tmpDir, evalue, bitscore, hmmThreshold,
				graph, seqAtt, conceptType);
		this.hmmer = graph.getMetaData().getEvidenceType(EV_HMMER);
		this.ccPfam = graph.getMetaData().getConceptClass(
				MetaData.CC_Pfam);
		this.dataSourcePfam = graph.getMetaData().getDataSource(MetaData.CV_PFAM);
	}

	public Collection<HMMMatch> execute() throws Exception {
		String ondexDir = net.sourceforge.ondex.config.Config.ondexDir;
		try {
			
			float eValue = Float.parseFloat(getEvalue());
			int bitscore = Integer.parseInt(getBitscore());
			
			DecypherAlignment dal = new DecypherAlignment(ondexDir,
					super.getProgramDir(),
					eValue, //evalue
					bitscore, //bitscore
					50, //maxresults
					false);
			
			AttributeName att = super.getSeqAtt();
			
			String algo = null;
			
			if (att.getId().equalsIgnoreCase("AA")) {
				algo = DecypherAlignment.ALGO_AA_TO_HMM;
			}else if (att.getId().equalsIgnoreCase("NA")) {
				algo = DecypherAlignment.ALGO_NA_TO_HMM;
			} else {
				throw new RuntimeException("Unsupported Atribute Type:"+att.getId());
			}
			
			Set<ONDEXConcept> concepts = BitSetFunctions.copy(graph.getConceptsOfAttributeName(att));
			if (super.getConceptType() != null) {
				concepts.retainAll(graph.getConceptsOfConceptClass(super.getConceptType()));
			}
			
			return dal.query(graph, new File(super.getPfamPfad()), getHmmThreshold(), concepts, algo);
		} catch (MissingFileException e) {
			e.printStackTrace();
		} catch (AlgorithmNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * throws UnsupportedOperationException
	 */

	public String[] getCommandArgments() {
		throw new UnsupportedOperationException();
	}

	public EvidenceType getEvidenceType() {
		return this.hmmer;
	}

	public Set<ONDEXConcept> searchMatchingConceptsInLuceneEnvironment(
			LuceneEnv lenv, HMMMatch result) {
		String accession = result.getHmmAccession().trim();
		int version = accession.indexOf(".");
		if (version > -1) {
			accession = accession.substring(0, version);
		}
		Query query = LuceneQueryBuilder.searchConceptByConceptAccessionExact(dataSourcePfam,accession,ccPfam, true);
		return lenv.searchInConcepts(query);
	}

}
