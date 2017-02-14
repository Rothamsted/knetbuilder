package net.sourceforge.ondex.programcalls.decypher;

import net.sourceforge.ondex.programcalls.HMMMatch;
import net.sourceforge.ondex.programcalls.SequenceType;
import net.sourceforge.ondex.programcalls.exceptions.AlgorithmNotSupportedException;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Parsers a Decypher tab delim file
 *
 * @author hindlem
 */
public class HMMTabularDecypherParser {

    public final static boolean DEBUG = true;

    //these are some possible fields see doc for latest
    //see http://decypher/doc/User_Guide_Keyword_Reference.pdf

    public final static String QUERYACCESSION = "QUERYACCESSION";
    public final static String TARGETACCESSION = "TARGETACCESSION";
    public final static String SEARCHID = "SEARCHID";
    public final static String RANK = "RANK";
    public final static String STATUS = "STATUS";
    public final static String SCORE = "SCORE";
    public final static String SIGNIFICANCE = "SIGNIFICANCE";
    public final static String ALGORITHM = "ALGORITHM";
    public final static String OPENPENALTY = "OPENPENALTY";
    public final static String EXTENDPENALTY = "EXTENDPENALTY";
    public final static String FRAMEPENALTY = "FRAMEPENALTY";
    public final static String SCALEFACTOR = "SCALEFACTOR";
    public final static String MATRIX = "MATRIX";
    public final static String QUERYFILE = "QUERYFILE";
    public final static String QUERYLENGTH = "QUERYLENGTH";
    public final static String QUERYFRAME = "QUERYFRAME";
    public final static String QUERYTEXT = "QUERYTEXT";
    public final static String TARGETFILE = "TARGETFILE";
    public final static String TARGETLENGTH = "TARGETLENGTH";
    public final static String TARGETFRAME = "TARGETFRAME";
    public final static String TARGETLOCUS = "TARGETLOCUS";
    public final static String QUERYLOCUS = "QUERYLOCUS";
    public final static String TARGETDESCRIPTION = "TARGETDESCRIPTION";
    public final static String PERCENTSCORE = "PERCENTSCORE";
    public final static String MAXSCORE = "MAXSCORE";
    public final static String MAXSCORED = "MAXSCORED";
    public final static String MAXSCOREC = "MAXSCOREC";
    public final static String MAXSCORE1 = "MAXSCORE1";
    public final static String MAXSCORE2 = "MAXSCORE2";
    public final static String MAXSCORE3 = "MAXSCORE3";
    public final static String MAXSCOREN1 = "MAXSCOREN1";
    public final static String MAXSCOREN2 = "MAXSCOREN2";
    public final static String MAXSCOREN3 = "MAXSCOREN3";
    public final static String MATCHES = "MATCHES";
    public final static String SIMILARITIES = "SIMILARITIES";
    public final static String GAPS = "GAPS";
    public final static String PERCENTALIGNMENT = "PERCENTALIGNMENT";
    public final static String PERCENTQUERY = "PERCENTQUERY";
    public final static String PERCENTTARGET = "PERCENTTARGET";
    public final static String SIMPERCENTALIGNMENT = "SIMPERCENTALIGNMENT";
    public final static String SIMPERCENTQUERY = "SIMPERCENTQUERY";
    public final static String SIMPERCENTTARGET = "SIMPERCENTTARGET";
    public final static String QUERYSTART = "QUERYSTART";
    public final static String QUERYEND = "QUERYEND";
    public final static String TARGETSTART = "TARGETSTART";
    public final static String TARGETEND = "TARGETEND";
    public final static String QUERYNT = "QUERYNT";
    public final static String QUERYAA = "QUERYAA";
    public final static String COMPARISON = "COMPARISON";
    public final static String TARGETNT = "TARGETNT";
    public final static String TARGETAA = "TARGETAA";
    public final static String QUERYNUMBER = "QUERYNUMBER";
    public final static String ALIGNMENTLENGTH = "ALIGNMENTLENGTH";

    private final static Pattern tabDelim = Pattern.compile("[\t]");
    private final static Pattern dotPattern = Pattern.compile("[\\.]");

    /**
     * @param file
     * @param querySeqType
     * @param targetSeqType
     * @param scorecuttoff
     * @param gzip
     * @return
     * @throws IOException
     * @throws AlgorithmNotSupportedException
     */
    public Collection<HMMMatch> parseMap(File file,
                                  SequenceType querySeqType,
                                  SequenceType targetSeqType,
                                  int scorecuttoff,
                                  boolean gzip) throws IOException, AlgorithmNotSupportedException {
        Collection<HMMMatch> results;
        if (!gzip)
            results = parseMap(new BufferedReader(new FileReader(file)), querySeqType, targetSeqType, scorecuttoff);
        else
            results = parseMap(new BufferedReader(new InputStreamReader(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file), 512 * 2)))), querySeqType, targetSeqType, scorecuttoff);
        file.delete();
        return results;
    }

    /**
     * @param br
     * @param querySeqType
     * @param targetSeqType
     * @param scorecuttoff
     * @return
     * @throws IOException
     * @throws AlgorithmNotSupportedException
     */
    public Collection<HMMMatch> parseMap(BufferedReader br,
                                  SequenceType querySeqType,
                                  SequenceType targetSeqType,
                                  int scorecuttoff) throws IOException, AlgorithmNotSupportedException {

        //queryid to HashMap
        List<HMMMatch> matches = new ArrayList<HMMMatch>(50000);

        Map<String, Integer> headerIndex = new HashMap<String, Integer>();

        String line = br.readLine();

        if (line == null) {
            System.err.println("Error in Decypher call...returned file is empty");
            return new HashSet<HMMMatch>(0);
        }

        if (DEBUG) System.out.println(line);
        String[] headers = tabDelim.split(line);
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];
            headerIndex.put(header, i);
        }

        int scoreIndex = headerIndex.get(SCORE);
        int significanceIndex = headerIndex.get(SIGNIFICANCE);
        int alignmentLength = headerIndex.get(ALIGNMENTLENGTH);

        int accessionIndex = -1;
        int targetId = -1;

        SequenceType seqType = null;

        if (targetSeqType != null) {
            seqType = targetSeqType;
            accessionIndex = headerIndex.get(QUERYACCESSION);
            targetId = headerIndex.get(TARGETLOCUS);
        } else if (querySeqType != null) {
            accessionIndex = headerIndex.get(TARGETACCESSION);
            targetId = headerIndex.get(QUERYLOCUS);
            seqType = querySeqType;
        } else {
            throw new AlgorithmNotSupportedException("Inapropriate Query -- Target sequence type combination");
        }

        int args = requiredFields.length;

        int lineCount = 0;

        while (br.ready()) {
            line = br.readLine();
            if (DEBUG) System.out.println(line);

            if (line.length() == 0 || line.charAt(0) == '\t') { //indicates target sequence is missing
                continue;
            }

            String[] values = tabDelim.split(line);
            if (values.length != args) {
                System.err.println("wrong number of arguments in line: " + lineCount + " :" + line);
                continue;
            }

            String[] target = dotPattern.split(values[targetId].trim());

            String targetStringId = target[0].trim();
            String targetTaxStringId = target[1].trim();

            int targetid = Integer.parseInt(targetStringId);
            int targetTaxId = -1;

            if (targetTaxStringId.length() == 1 && !(targetTaxStringId.charAt(0) == DecypherAlignment.EMPTY_TAXID))
                targetTaxId = Integer.parseInt(targetTaxStringId);

            if (target.length != 2) {
                System.err.println(targetid + " " + targetTaxId);
            }

            try {
                String hmmAccession = values[accessionIndex].trim();
                double score = Double.parseDouble(values[scoreIndex]);
                double eValue = Double.parseDouble(values[significanceIndex]);
                int alignmentLen = Integer.parseInt(values[alignmentLength]);

                if (score >= scorecuttoff) {

                    matches.add(new HMMMatch(
                            hmmAccession,
                            targetid,
                            targetTaxId,
                            score,
                            eValue,
                            alignmentLen,
                            seqType));
                }

                lineCount++;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Total hits " + lineCount + " of which met conditions " + matches.size());
        return matches;
    }

    private final static String[] requiredFields = new String[]{
            TARGETLOCUS,
            SCORE,
            SIGNIFICANCE,
            ALIGNMENTLENGTH,
            QUERYACCESSION,
            TARGETACCESSION,
            QUERYLOCUS
    };

    public static String[] getRequiredFields() {
        return requiredFields;
    }
}
