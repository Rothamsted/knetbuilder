package net.sourceforge.ondex.mapping.sequence2pfam.method;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.core.searchable.LuceneQueryBuilder;
import net.sourceforge.ondex.mapping.sequence2pfam.MetaData;
import net.sourceforge.ondex.programcalls.HMMMatch;
import net.sourceforge.ondex.programcalls.SequenceType;

import org.apache.lucene.search.Query;

/**
 * BLAST based pfam mapping method
 * 
 * @author peschr
 * 
 */
public class Blast extends AbstractMethod {

	private static final String EV_BLAST = "BLAST";

	private ConceptClass ccPfam;
	private DataSource dataSourcePfam;
	private EvidenceType blast;

	public Blast(String blastDir, String pfamPfad, String tmpDir,
			String evalue, String bitscore, String hmmThreshold,
			ONDEXGraph graph, AttributeName seqAtt, ConceptClass conceptType) {
		super(blastDir, pfamPfad, tmpDir, evalue, bitscore, hmmThreshold,
				graph, seqAtt, conceptType);
		this.ccPfam = graph.getMetaData().getConceptClass(MetaData.CC_Pfam);
		this.dataSourcePfam = graph.getMetaData().getDataSource(
				MetaData.CV_PFAM);
		this.blast = graph.getMetaData().getEvidenceType(EV_BLAST);
	}

	public Set<HMMMatch> execute() throws Exception {
		Set<HMMMatch> resultList = new HashSet<HMMMatch>();
		Process blast = Runtime.getRuntime().exec(this.getCommandArgments());
		System.out.println(this.getCommandArgments()[0] + " "
				+ this.getCommandArgments()[1] + " "
				+ this.getCommandArgments()[2] + " "
				+ this.getCommandArgments()[3] + " "
				+ this.getCommandArgments()[4]);
		BufferedReader br = new BufferedReader(new InputStreamReader(
				blast.getInputStream()));
		String line = "";
		int id = -1;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("gnl|CDD|")) {
				if (id == -1)
					throw new Exception("no target accession");
				double evalue;
				try {
					evalue = Double.valueOf(line.substring(73));
				} catch (NumberFormatException e) {
					evalue = Double.valueOf(1 + "" + line.substring(73).trim());
				}
				HMMMatch result = new HMMMatch("PF" + line.substring(18, 23),
						id, -1, -1, evalue, -1, SequenceType.AA);
				resultList.add(result);
			} else if (line.startsWith("Query=")) {
				id = Integer.valueOf(line.substring(7).trim());
			}
		}
		blast.waitFor();
		return resultList;
	}

	public String[] getCommandArgments() {
		return new String[] { this.getProgramDir() + "blastp", "-db",
				getPfamPfad(), "-query", getTmpDir(), "-evalue", getEvalue() };
	}

	public Set<ONDEXConcept> searchMatchingConceptsInLuceneEnvironment(
			LuceneEnv lenv, HMMMatch result) {
		String accession = result.getHmmAccession();
		int version = accession.indexOf(".");
		if (version > -1) {
			accession = accession.substring(0, version);
		}
		Query query = LuceneQueryBuilder.searchConceptByConceptAccessionExact(
				dataSourcePfam, accession, ccPfam, true);
		return lenv.searchInConcepts(query);
	}

	public EvidenceType getEvidenceType() {
		return this.blast;
	}

}
