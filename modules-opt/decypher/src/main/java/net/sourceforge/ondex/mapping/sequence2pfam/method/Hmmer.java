package net.sourceforge.ondex.mapping.sequence2pfam.method;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.core.searchable.LuceneQueryBuilder;
import net.sourceforge.ondex.mapping.sequence2pfam.MetaData;
import net.sourceforge.ondex.programcalls.HMMMatch;
import net.sourceforge.ondex.programcalls.SequenceType;
import net.sourceforge.ondex.programcalls.StreamGobbler;
import org.apache.lucene.search.Query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * HMMER based pfam mapping method
 *
 * @author peschr
 */
public class Hmmer extends AbstractMethod {
    private static final String EV_HMMER = "HMMER";

    private ConceptClass ccPfam;
    private DataSource dataSourcePfam;
    private EvidenceType hmmer;

    private String outFileName = "out_" + System.currentTimeMillis() + "_tbl";
    private String stdoutFileName = "out_" + System.currentTimeMillis() + "_stdout";

    public Hmmer(String blastDir, String pfamPfad, String tmpDir,
                 String evalue, String bitscore, String hmmThreshold,
                 ONDEXGraph graph, AttributeName seqAtt, ConceptClass conceptType) {
        super(blastDir, pfamPfad, tmpDir, evalue, bitscore, hmmThreshold,
                graph, seqAtt, conceptType);
        this.ccPfam = graph.getMetaData().getConceptClass(
                MetaData.CC_Pfam);
        this.dataSourcePfam = graph.getMetaData().getDataSource(MetaData.CV_PFAM);

        this.hmmer = graph.getMetaData().getEvidenceType(EV_HMMER);
    }

    public Set<HMMMatch> execute() throws Exception {
        Set<HMMMatch> resultList = new HashSet<HMMMatch>();

        StringBuilder sb = new StringBuilder();
        for (String arg : getCommandArgments()) {
            sb.append(arg + " ");
        }
        System.out.println(sb.toString());

        System.out.println("running: " + this.getCommandArgments());
        Process hmmer = Runtime.getRuntime().exec(this.getCommandArgments(), null, new File(this.getTmpDir()).getParentFile());

        StreamGobbler errorStream = new StreamGobbler(hmmer.getErrorStream(), "MAIN_ERR_PROC:", true);
        errorStream.start();

        StreamGobbler outStream = new StreamGobbler(hmmer.getInputStream(), "MAIN_PROC:", false);
        outStream.start();

        outStream.join();
        hmmer.waitFor();

        if (hmmer.exitValue() != 0) {
            System.err.println("Hmmer process terminated abnormaly");
        }

        BufferedReader br = new BufferedReader(new FileReader(new File(getTmpDir()).getParent() + File.separator + outFileName));

        Pattern tabPattern = Pattern.compile("[     ]+");

        Map<String, Integer> headerToCol = new HashMap<String, Integer>();

        int targetName = 0;
        int targetAccession = 1;

        int queryName = 2;
        int accessionQuery = 3; //ignore is blank
        int globalEval = 4;
        int globalScore = 5;
        int globale_bias = 6;
        int bestDomainEvalue = 7;
        int bestDomainScore = 8;
        int bestBias = 9;

        int exp = 10;
        int reg = 11;
        int clu = 12;
        int ov = 13;
        int env = 14;
        int dom = 15;
        int rep = 16;
        int inc = 17;

        int targetDescription = 18; //and everything after

        Pattern lclPatten = Pattern.compile("[\\||:]");

        int count = 0;
        while (br.ready()) {

            String line = br.readLine().trim();

            if (line.length() == 0 || line.startsWith("#")) {
                continue;
            }
            count++;
            String[] values = tabPattern.split(line);

            String[] ids = lclPatten.split(values[queryName].trim());

            System.out.println("name=" + values[queryName].trim() + "#" + ids.length);
            
            int id;
            
            if (ids.length > 1) {
            	id = Integer.parseInt(ids[1].trim());
            } else {
            	id = Integer.parseInt(ids[0].trim());
            }            

            String accession = values[targetAccession].trim();

            double scoreLocal = Double.parseDouble(values[globalScore].trim());
            double eValueLocal = Double.parseDouble(values[globalEval].trim());

            HMMMatch result = new HMMMatch(accession,
                    id, -1, scoreLocal, eValueLocal, -1, SequenceType.NA);

            System.out.println(id + " --> " + accession + " score=" + scoreLocal + " evalue=" + eValueLocal);

            if (ids.length >= 3) {
                result.setQueryFrame(Double.valueOf(ids[2]).intValue());
            }

            result.setBestDomainEvalue(Double.parseDouble(values[bestDomainEvalue].trim()));
            result.setBestDomainScore(Double.parseDouble(values[bestDomainScore].trim()));

            //int sourceId, double value, double score
            resultList.add(result);
        }

        System.out.println(count + " sequence 2 pfam hits found");

        return resultList;
    }

    public String[] getCommandArgments() {

        List<String> cmd = new ArrayList<String>();
        cmd.add(this.getProgramDir() + File.separator + "hmmscan");

        /*
          if (this.getSeqAtt().getId().equals("AA")) {
              cmd.add("-n");
          }
          */

        cmd.add("--notextw");
        cmd.add("--noali");

        if (getHmmThreshold() != null) {
            cmd.add("--cut_" + getHmmThreshold().toLowerCase());
        }
        cmd.add("--cpu");
        cmd.add(String.valueOf(Runtime.getRuntime().availableProcessors() * 3));
        cmd.add("--tblout");
        cmd.add(outFileName);
        cmd.add("-o");
        cmd.add(stdoutFileName);
        cmd.add(getPfamPfad());
        cmd.add(getTmpDir());

        return cmd.toArray(new String[cmd.size()]);
    }

    public Set<ONDEXConcept> searchMatchingConceptsInLuceneEnvironment(
            LuceneEnv lenv, HMMMatch result) {
        String accession = result.getHmmAccession();
        int version = accession.indexOf(".");
        if (version > -1) {
            accession = accession.substring(0, version);
        }
        Query query = LuceneQueryBuilder.searchConceptByConceptAccessionExact(dataSourcePfam, accession, ccPfam, true);
        return lenv.searchInConcepts(query);
    }

    public EvidenceType getEvidenceType() {
        return this.hmmer;
    }

}
