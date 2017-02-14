package net.sourceforge.ondex.programcalls.decypher;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.programcalls.*;
import net.sourceforge.ondex.programcalls.MetaData;
import net.sourceforge.ondex.programcalls.exceptions.AlgorithmNotSupportedException;
import net.sourceforge.ondex.programcalls.exceptions.MissingFileException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static net.sourceforge.ondex.programcalls.SequenceType.AA;
import static net.sourceforge.ondex.programcalls.SequenceType.NA;

/**
 * An implementation of Decypher for allignments
 *
 * @author hindlem
 */
public class DecypherAlignment implements BLASTAlignmentProgram, HMMAlignmentProgram {

    private static String FILE_DC_NEW_TARGET = "dc_new_target_rt";
    private static String FILE_DC_TEMPLATE_RT = "dc_template_rt";
    private static String FILE_DC_DELETE_TARGET = "dc_delete_target";

    private static boolean initalized = false;

    private final String decypherTemplates;
    private final String dirForDecypherClient;

    private final int cutoff;
    private final float overlap;
    private final float evalue;
    private final int maxResultsPerQuery;
    private final String ondexDir;
    private boolean queryOnlyWithTAXID;
    private int bitscore = 0;

    /**
     * @param ondexDir           the ondex.dir directory
     * @param programDir         the decypher client bin dir
     * @param cutoff             the length of sequence to align
     * @param overlap            the % coverage of the shortest sequence to allow
     * @param evalue             the maximum e value to allow
     * @param bitscore           the minimum score required before a hit is detected
     * @param maxResultsPerQuery the number of results per query to return
     * @param queryOnlyWithTAXID send concepts with TAXID only
     * @throws MissingFileException if any of the required decypher program files are missing
     */
    public DecypherAlignment(
            String ondexDir,
            String programDir,
            int cutoff,
            float overlap,
            float evalue,
            int bitscore,
            int maxResultsPerQuery,
            boolean queryOnlyWithTAXID) throws MissingFileException {
        this.ondexDir = new File(ondexDir).getAbsoluteFile().getAbsolutePath();
        this.cutoff = cutoff;
        this.overlap = overlap;
        this.evalue = evalue;
        this.maxResultsPerQuery = maxResultsPerQuery;
        this.dirForDecypherClient = new File(programDir).getAbsoluteFile().getAbsolutePath();
        this.queryOnlyWithTAXID = queryOnlyWithTAXID;
        this.bitscore = bitscore;
        decypherTemplates = new File(net.sourceforge.ondex.config.Config.ondexDir + File.separator + "decypher" + File.separator + "templates").getAbsoluteFile().getAbsolutePath();

        if (!initalized && System.getProperty("os.name").toLowerCase().contains("windows")) { //detect windows version and add exe to file names
            initalized = true;
            FILE_DC_NEW_TARGET = FILE_DC_NEW_TARGET + ".exe";
            FILE_DC_TEMPLATE_RT = FILE_DC_TEMPLATE_RT + ".exe";
            FILE_DC_DELETE_TARGET = FILE_DC_DELETE_TARGET + ".exe";
        }

        testProgramFilesExist();
    }

    /**
     * @param ondexDir           the ondex.dir directory
     * @param programDir         the decypher client bin dir
     * @param evalue             the maximum e value to allow
     * @param bitscore           the minimum score required before a hit is detected
     * @param maxResultsPerQuery the number of results per query to return
     * @param queryOnlyWithTAXID send concepts with TAXID only
     * @throws MissingFileException if any of the required decypher program files are missing
     */
    public DecypherAlignment(String ondexDir, String programDir, float evalue,
                             int bitscore, int maxResultsPerQuery, boolean queryOnlyWithTAXID) throws MissingFileException {
        this(ondexDir, programDir, 0, 0, evalue, bitscore, maxResultsPerQuery, queryOnlyWithTAXID);
    }

    /**
     * Tests all decypher program files exist before going to the bother of writing sequence files ect.
     *
     * @throws MissingFileException if any of the required decypher program files are missing
     */
    private void testProgramFilesExist() throws MissingFileException {
        if (!new File(dirForDecypherClient).exists()) {
            throw new MissingFileException("Dir " + dirForDecypherClient + " does not exist");
        } else if (!new File(dirForDecypherClient + File.separator + FILE_DC_NEW_TARGET).exists()) {
            throw new MissingFileException("Program " + FILE_DC_NEW_TARGET + " does not exist in decypher dir " + dirForDecypherClient);
        } else if (!new File(dirForDecypherClient + File.separator + FILE_DC_TEMPLATE_RT).exists()) {
            throw new MissingFileException("Program " + FILE_DC_TEMPLATE_RT + " does not exist in decypher dir " + dirForDecypherClient);
        } else if (!new File(dirForDecypherClient + File.separator + FILE_DC_DELETE_TARGET).exists()) {
            throw new MissingFileException("Program " + FILE_DC_DELETE_TARGET + " does not exist in decypher dir " + dirForDecypherClient);
        }
    }

    public String[] getSupportedAlgorithms() {
        return new String[]{
                ALGO_BLASTN,
                ALGO_BLASTP,
                ALGO_TBLASTN,
                ALGO_TBLASTX,
                ALGO_BLASTX,
                ALGO_HMM_TO_AA,
                ALGO_HMM_TO_NA,
        };
    }

    private HashMap<String, String> translation;

    /**
     * @param algorithmType the algorithm type
     * @return
     */
    private String convertToDecypherAlgorithmTemplate(String algorithmType) {
        if (translation == null) {
            translation = new HashMap<String, String>();
            translation.put(ALGO_BLASTN, decypherTemplates + File.separator + "tera-blastn_tab");
            translation.put(ALGO_BLASTP, decypherTemplates + File.separator + "sw_aa_vs_aa_tab");
            translation.put(ALGO_BLASTX, decypherTemplates + File.separator + "tera-blastx_tab");
            translation.put(ALGO_TBLASTN, decypherTemplates + File.separator + "tera-tblastn_tab");
            translation.put(ALGO_TBLASTX, decypherTemplates + File.separator + "tera-tblastx_tab");
            translation.put(ALGO_HMM_TO_AA, decypherTemplates + File.separator + "hmm_hmm_vs_aa");
            translation.put(ALGO_HMM_TO_NA, decypherTemplates + File.separator + "hmm_hmm_vs_nt");
            translation.put(ALGO_AA_TO_HMM, decypherTemplates + File.separator + "hmm_aa_vs_hmm");
            translation.put(ALGO_NA_TO_HMM, decypherTemplates + File.separator + "hmm_nt_vs_hmm");
            translation.put(ALGO_HMM_BUILD, decypherTemplates + File.separator + "hmmbuild");
        }
        return translation.get(algorithmType);
    }

    public Collection<HMMMatch> query(
            ONDEXGraph og,
            File hmmFile,
            String hmmThreshold, Set<ONDEXConcept> to,
            String algorithmType) throws AlgorithmNotSupportedException, MissingFileException, IOException {

        Template template = buildTemplate(algorithmType);
        template.setFields(HMMTabularDecypherParser.getRequiredFields());
        if (hmmThreshold != null && hmmThreshold.trim().length() != 0) {
            template.setPropertyValue("[hmm thresholds]", hmmThreshold);
        } else {
            template.removePropertyValue("[hmm thresholds]");
        }

        String time = new SimpleDateFormat("yyMMdd_HHmmss_SSS").format(new Date(System.currentTimeMillis()));

        String seqFile = ondexDir + File.separator + "seqs" + File.separator + "ONDEX_TARGETFASTA_" + time;

        String sourceFile;
        String targetFile;

        SequenceType targetType = getSequenceTypeTarget(algorithmType);

        String targetDatabaseName = "ONDEX_TARGETDB_" + time;
        File databaseTemplate = null;

        File tempTemplateFile = new File(decypherTemplates + File.separator + "ONDEX_Template_" + time + ".tmp");
        tempTemplateFile.mkdirs();
        try {
            template.writeNewTemplate(tempTemplateFile);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        SequenceType dbType;
        if (targetType != null) {
            dbType = targetType;
        } else {
            dbType = getSequenceTypeQuery(algorithmType);
        }

        try {
            int seqWritten = writeDecypherFASTAFile(og, to, seqFile, dbType, queryOnlyWithTAXID);
            if (seqWritten == 0) {
                System.out.println("No sequence targets to execute of type :" + dbType);
                return new HashSet<HMMMatch>(0);
            } else {
                System.out.println("Written " + seqWritten + " targets ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (targetType != null) { //this means the target is a hmm
            if (targetType.equals(SequenceType.AA)) {
                databaseTemplate = new File(decypherTemplates + File.separator + "format_aa_into_aa");
            } else if (targetType.equals(SequenceType.NA)) {
                databaseTemplate = new File(decypherTemplates + File.separator + "format_nt_into_nt");
            }

            sourceFile = hmmFile.getAbsolutePath();
            ;
            targetFile = seqFile;
        } else {
            databaseTemplate = new File(decypherTemplates + File.separator + "format_hmm");

            sourceFile = seqFile;
            targetFile = hmmFile.getAbsolutePath();
        }

        StringBuffer dc_new_target_rt_options = new StringBuffer();
        dc_new_target_rt_options.append(dirForDecypherClient + File.separator + FILE_DC_NEW_TARGET);//rt waits untill job is finished
        dc_new_target_rt_options.append(" -template " + databaseTemplate.getAbsolutePath());
        dc_new_target_rt_options.append(" -targ " + targetDatabaseName);
        dc_new_target_rt_options.append(" -desc ONDEX_target_database");
        dc_new_target_rt_options.append(" -source " + targetFile);

        try {
            System.out.println(dc_new_target_rt_options.toString());
            Process create_new_database = Runtime.getRuntime().exec(dc_new_target_rt_options.toString(), null,
                    new File(dirForDecypherClient));

            StreamGobbler errorStream = new StreamGobbler(create_new_database.getErrorStream(), "MAKEDB:", true);
            StreamGobbler inputStream = new StreamGobbler(create_new_database.getInputStream(), "MAKEDB:");
            errorStream.start();
            inputStream.start();

            create_new_database.waitFor();
            inputStream.join();
            errorStream.join();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        File results = runDecypherProgram(new File(sourceFile),
                targetDatabaseName,
                tempTemplateFile,
                algorithmType);

        if (targetType != null) new File(seqFile).delete(); //clean up seq file
        if (targetType == null) new File(sourceFile).delete();

        return parseHMMResults(results, algorithmType);
    }


    public Collection<Match> query(
            ONDEXGraph og,
            Set<ONDEXConcept> from,
            Set<ONDEXConcept> to,
            String algorithmType) throws AlgorithmNotSupportedException, MissingFileException, IOException {
        Template template = buildTemplate(algorithmType);
        template.setFields(BLASTTabularDecypherParser.getRequiredFields());

        String time = new SimpleDateFormat("yyMMdd_HHmmss_SSS").format(new Date(System.currentTimeMillis()));

        String queryFile = new File(ondexDir + File.separator + "seqs" + File.separator + "ONDEX_QUERYFASTA_" + time).getAbsoluteFile().getAbsolutePath();
        String targetFile = new File(ondexDir + File.separator + "seqs" + File.separator + "ONDEX_TARGETFASTA_" + time).getAbsoluteFile().getAbsolutePath();

        SequenceType queryType = getSequenceTypeQuery(algorithmType);
        SequenceType targetType = getSequenceTypeTarget(algorithmType);

        try {
            int seqWritten = writeDecypherFASTAFile(og, from, queryFile, queryType, queryOnlyWithTAXID);
            if (seqWritten == 0) {
                System.out.println("No sequence queries to execute of type :" + queryType);
                return null;
            } else {
                System.out.println("Written " + seqWritten + " targets ");
            }
            seqWritten = writeDecypherFASTAFile(og, to, targetFile, targetType, queryOnlyWithTAXID);
            if (seqWritten == 0) {
                System.out.println("No sequence targets to execute of type :" + targetType);
                return null;
            } else {
                System.out.println("Written " + seqWritten + " targets ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String targetDatabaseName = "ONDEX_TARGETDB_" + time;

        File tempTemplateFile = new File(decypherTemplates + File.separator + "ONDEX_Template_" + time + ".tmp");
        tempTemplateFile.mkdirs();
        try {
            template.writeNewTemplate(tempTemplateFile);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        //create database
        File databaseTemplate = null;


        if (targetType.equals(SequenceType.AA)) {
            databaseTemplate = new File(decypherTemplates + File.separator + "format_aa_into_aa");
        } else if (targetType.equals(SequenceType.NA)) {
            databaseTemplate = new File(decypherTemplates + File.separator + "format_nt_into_nt");
        }

        StringBuffer dc_new_target_rt_options = new StringBuffer();
        dc_new_target_rt_options.append(dirForDecypherClient + File.separator + FILE_DC_NEW_TARGET);//rt waits untill job is finished
        dc_new_target_rt_options.append(" -template " + databaseTemplate.getAbsolutePath());
        dc_new_target_rt_options.append(" -targ " + targetDatabaseName);
        dc_new_target_rt_options.append(" -desc ONDEX_target_database");
        dc_new_target_rt_options.append(" -source " + targetFile);

        try {
            System.out.println(dc_new_target_rt_options.toString());
            Process create_new_database = Runtime.getRuntime().exec(dc_new_target_rt_options.toString(), null,
                    new File(dirForDecypherClient));

            StreamGobbler errorStream = new StreamGobbler(create_new_database.getErrorStream(), "MAKEDB:", true);
            StreamGobbler inputStream = new StreamGobbler(create_new_database.getInputStream(), "MAKEDB:");
            errorStream.start();
            inputStream.start();

            create_new_database.waitFor();
            inputStream.join();
            errorStream.join();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new File(targetFile).delete();

        File resultsFile = runDecypherProgram(new File(queryFile),
                targetDatabaseName,
                tempTemplateFile,
                algorithmType);
        new File(queryFile).delete();
        return parseBLASTResults(resultsFile, algorithmType);
    }

    private Template buildTemplate(String algorithmType) throws AlgorithmNotSupportedException, MissingFileException {
        String templateFileName = convertToDecypherAlgorithmTemplate(algorithmType);

        if (templateFileName == null) {
            throw new AlgorithmNotSupportedException(algorithmType + " is not supported");
        }

        File templateFile = new File(templateFileName);
        if (!templateFile.exists()) {
            throw new MissingFileException(templateFileName);
        }

        Template template = new Template(templateFile);
        template.setEvalueAndBitscoreThreshold(evalue, bitscore);

        template.setMaxAlignments(maxResultsPerQuery);
        template.setMaxScores(maxResultsPerQuery);

        return template;
    }

    /**
     * A dc_template_rt with a results call
     *
     * @param query    the query file
     * @param targ     the target database
     * @param template the template file
     * @return results file
     * @throws AlgorithmNotSupportedException if results from this algorithm can't be parsed
     */
    private File runDecypherProgram(File query, String targ, File template, String algorithm) throws AlgorithmNotSupportedException {

        String time = new SimpleDateFormat("yyMMdd_HHmmss_SSS").format(new Date(System.currentTimeMillis()));

        System.out.println("<< Created decypher database >>");
        File resultsFile = new File(net.sourceforge.ondex.config.Config.ondexDir + File.separator + "seqs" + File.separator + "ONDEX_Results_" + time + ".gz");
        resultsFile.getParentFile().mkdirs();
        try {
            resultsFile.createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        StringBuffer dc_template_rt_options = new StringBuffer();
        dc_template_rt_options.append(dirForDecypherClient + File.separator + FILE_DC_TEMPLATE_RT); //rt waits untill job is finished
        dc_template_rt_options.append(" -priority 0");//set as the highest priority
        dc_template_rt_options.append(" -template " + template.getAbsolutePath());
        dc_template_rt_options.append(" -targ " + targ);
        dc_template_rt_options.append(" -query " + query.getAbsolutePath());
        dc_template_rt_options.append(" -quiet");

        try {
            System.out.println(dc_template_rt_options.toString());

            BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(resultsFile), 512 * 2)));
            Process run_process = Runtime.getRuntime().exec(dc_template_rt_options.toString(), null,
                    new File(dirForDecypherClient));

            StreamGobbler errorStream = new StreamGobbler(run_process.getErrorStream(), "MAIN_PROC:", true);
            errorStream.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(run_process.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                fw.write(line);
                fw.newLine();
            }
            run_process.waitFor();
            br.close();
            fw.flush();
            fw.close();
            if (errorStream.hasError() == true) {

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new BufferedInputStream(
                                new GZIPInputStream(new FileInputStream(
                                        resultsFile), 512 * 2))));
                while (reader.ready()) {
                    System.err.println(reader.readLine());
                }
                reader.close();
                throw new RuntimeException("Error in DECYPHER CALL");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        template.delete();

        try {
            Process delete_database = Runtime.getRuntime().exec(dirForDecypherClient + File.separator + FILE_DC_DELETE_TARGET + " -targ " + targ, null,
                    new File(dirForDecypherClient));

            StreamGobbler errorStream = new StreamGobbler(delete_database.getErrorStream(), "DELETEDB:", true);
            StreamGobbler inputStream = new StreamGobbler(delete_database.getInputStream(), "DELETEDB:");
            errorStream.start();
            inputStream.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("<< Cleaned up decypher database >>");

        return resultsFile;
    }

    /**
     * Extract BLAST results in tabular format from a decypher results file
     *
     * @param resultsFile BLAST results in tabular format (can be compressed)
     * @param algorithm   the blast algorithm
     * @return matches
     * @throws AlgorithmNotSupportedException
     */
    private Collection<Match> parseBLASTResults(File resultsFile, String algorithm) throws AlgorithmNotSupportedException, IOException {
        BLASTTabularDecypherParser parser = new BLASTTabularDecypherParser();
        return parser.parseMap(resultsFile, cutoff, overlap, algorithm, true);
    }

    /**
     * Extract HMM results in tabular format from a decypher results file
     *
     * @param resultsFile   HMM results in tabular format (can be compressed)
     * @param algorithmType the hmm algorithm
     * @return matches
     * @throws AlgorithmNotSupportedException
     * @throws AlgorithmNotSupportedException
     */
    private Collection<HMMMatch> parseHMMResults(File resultsFile, String algorithmType) throws AlgorithmNotSupportedException, IOException {
        HMMTabularDecypherParser parser = new HMMTabularDecypherParser();
        SequenceType queryType = getSequenceTypeQuery(algorithmType);
        SequenceType targetType = getSequenceTypeTarget(algorithmType);
        return parser.parseMap(resultsFile, queryType, targetType, bitscore, true);
    }

    /**
     * gets the sequence type on this algorithm type
     *
     * @param blastType DecypherAlignment algorithm see static strings
     * @return MetaData AttributeName NA or AA
     * @throws AlgorithmNotSupportedException
     */
    public static SequenceType getSequenceTypeTarget(String blastType) throws AlgorithmNotSupportedException {
        if (blastType.equals(DecypherAlignment.ALGO_BLASTN)
                || blastType.equals(DecypherAlignment.ALGO_TBLASTN)
                || blastType.equals(DecypherAlignment.ALGO_TBLASTX)
                || blastType.equals(DecypherAlignment.ALGO_HMM_TO_NA)) {
            return SequenceType.NA;
        } else if (blastType.equals(DecypherAlignment.ALGO_BLASTP)
                || blastType.equals(DecypherAlignment.ALGO_BLASTX)
                || blastType.equals(DecypherAlignment.ALGO_HMM_TO_AA)
                || blastType.equals(DecypherAlignment.ALGO_HMM_BUILD)) {
            return SequenceType.AA;
        } else if (blastType.equals(DecypherAlignment.ALGO_AA_TO_HMM)
                || blastType.equals(DecypherAlignment.ALGO_NA_TO_HMM)) {
            return null;
        } else {
            throw new AlgorithmNotSupportedException(blastType + " is not supported by this decypher results parser");
        }
    }

    /**
     * gets the sequence type on this algorithm type
     *
     * @param blastType DecypherAlignment algorithm see static strings
     * @return MetaData AttributeName NA or AA
     * @throws AlgorithmNotSupportedException
     */
    public static SequenceType getSequenceTypeQuery(String blastType) throws AlgorithmNotSupportedException {
        if (blastType.equals(DecypherAlignment.ALGO_BLASTN)
                || blastType.equals(DecypherAlignment.ALGO_BLASTX)
                || blastType.equals(DecypherAlignment.ALGO_TBLASTX)
                || blastType.equals(DecypherAlignment.ALGO_NA_TO_HMM)) {
            return SequenceType.NA;
        } else if (blastType.equals(DecypherAlignment.ALGO_BLASTP)
                || blastType.equals(DecypherAlignment.ALGO_TBLASTN)
                || blastType.equals(DecypherAlignment.ALGO_AA_TO_HMM)) {
            return SequenceType.AA;
        } else if (blastType.equals(DecypherAlignment.ALGO_HMM_TO_AA)
                || blastType.equals(DecypherAlignment.ALGO_HMM_TO_NA)) {
            return null;
        } else {
            throw new AlgorithmNotSupportedException(blastType + " is not supported by this decypher results parser");
        }
    }

    private final static char headerLineStart = '>';

    private static final Pattern replaceAllNonChars = Pattern.compile("[^A-Z]");

    /**
     * 1970. IUPAC-IUB Commission on Biochemical Nomenclaure (CBN). Abbreviations and symbols for nucleic acids, polynucleotides and their constituents. Recommendations 1970. Eur J. Biochem. 15:203-208
     * 1986. Nomenclature Committee of the International Union of Biochemistry (NC-IUB). Nomenclature for incompletely specified basis in nucleic acid sequences. Recommendations 1984. Proc. Natl. Acad. Sci. USA 83:4-8.
     */
    private static final Pattern replaceAllNonIUPAC_NA_Notation_Chars = Pattern.compile("[^G|T|A|C|R|Y|S|W|K|M|D|H|B|V|N]");

    public static final char EMPTY_TAXID = 'N';
    public static final char TAXID_DELIMINATOR = '.';

    /**
     * @param og                  the current graph
     * @param seqs                the Concepts containing the sequences to write
     * @param filename            the file to write to
     * @param sequenceType        the type of sequence @see net.sourceforge.ondex.programcalls.SequenceType
     * @param includeOnlyTaxIdSeq include only sequences with taxid
     * @return
     * @throws IOException
     */
    public static int writeDecypherFASTAFile(
            ONDEXGraph og,
            Set<ONDEXConcept> seqs,
            String filename,
            SequenceType sequenceType,
            boolean includeOnlyTaxIdSeq) throws IOException {

        int seqWritten = 0;

        String attSeq = null;
        if (sequenceType.equals(AA)) {
            attSeq = MetaData.Att_AA;
        } else if (sequenceType.equals(NA)) {
            attSeq = MetaData.Att_NA;
        } else {
            System.err.println("Unknow attribute sequence type");
        }

        AttributeName defaultAt = og.getMetaData().getAttributeName(attSeq);

        List<AttributeName> attributesOfSequenceType = new ArrayList<AttributeName>();
        attributesOfSequenceType.add(defaultAt);
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            AttributeName next = og.getMetaData().getAttributeName(attSeq + ':' + i);
            if (next == null) {
                break;
            } else {
                System.out.println("Found " + attSeq + ':' + i);
                attributesOfSequenceType.add(next);
            }
        }

        AttributeName aTaxID = og.getMetaData().getAttributeName("TAXID");
        if (includeOnlyTaxIdSeq) {
            //filter out concepts without taxid
            Set<ONDEXConcept> taxIdConcepts = og.getConceptsOfAttributeName(aTaxID);
            seqs.retainAll(taxIdConcepts);
        }

        //find concepts with sequences
        Set<ONDEXConcept> attConcepts = null;
        for (AttributeName att : attributesOfSequenceType) {
            Set<ONDEXConcept> sequenceConcepts = og.getConceptsOfAttributeName(att);
            System.out.println(att.getId() + " " + sequenceConcepts.size());
            if (attConcepts == null) {
                attConcepts = sequenceConcepts;
                continue;
            }
            attConcepts.addAll(sequenceConcepts);
            System.out.println("Including " + att.getId());
        }

        //concepts with sequences and TAXID
        seqs.retainAll(attConcepts);

        System.out.println("Writing " + seqs.size() + " concepts with sequences");

        if (seqs.size() == 0) {
            return 0;
        }

        File f = new File(filename);
        f.createNewFile();

        // new buffered writer
        BufferedWriter out = new BufferedWriter(new FileWriter(f));
        StringBuilder line = new StringBuilder(70);

        for (ONDEXConcept concept : seqs) {
            int cid = concept.getId();

            Attribute taxIdAttribute = concept.getAttribute(aTaxID);
            String taxId = null;

            if (taxIdAttribute != null) {
                taxId = (String) taxIdAttribute.getValue();
                if (taxId.trim().length() == 0) {
                    if (includeOnlyTaxIdSeq) continue;

                }
            } else {
                if (includeOnlyTaxIdSeq) continue;
            }

            for (AttributeName aSeq : attributesOfSequenceType) {

                Attribute attribute = concept.getAttribute(aSeq);
                if (attribute == null) {
                    continue;
                }

                Object value = attribute.getValue();

                String[] sequences = null;

                if (value instanceof String)
                    sequences = new String[]{((String) value).toUpperCase().trim()};
                else if (value instanceof Collection) {
                    sequences = (String[]) ((Collection) value).toArray();
                } else if (value instanceof String[]) {
                    sequences = (String[]) value;
                } else {
                    System.err.println(value.getClass() + " is an unknown format for " + aSeq.getId());
                    continue;
                }

                for (String seq : sequences) {

                    if (seq == null || seq.length() == 0) {
                        System.err.println("empty seq");
                        continue;
                    }
                    seq = seq.toUpperCase();

                    line.append(headerLineStart);
                    line.append(cid);
                    line.append(TAXID_DELIMINATOR);
                    if (taxId != null) {
                        line.append(taxId);
                    } else {
                        line.append(EMPTY_TAXID);
                    }
                    line.append(TAXID_DELIMINATOR);
                    line.append(aSeq.getId());

                    out.write(line.toString());
                    line.setLength(0);
                    out.newLine();

                    String stringToWrite = null;

                    if (sequenceType.equals(NA)) {
                        stringToWrite = replaceAllNonIUPAC_NA_Notation_Chars.matcher(seq).replaceAll("");
                    } else {
                        stringToWrite = replaceAllNonChars.matcher(seq).replaceAll("");
                    }

                    if (stringToWrite == null || stringToWrite.length() == 0) {
                        System.err.println("nout to write");
                        continue;
                    }

                    for (int i = 0; i < stringToWrite.length(); i = i + 70) {
                        int end = i + 70;
                        if (end > stringToWrite.length() - 1) {
                            end = stringToWrite.length() - 1;
                        }
                        String toWrite = stringToWrite.substring(i, end);
                        if (toWrite.length() > 0) {
                            out.write(toWrite);
                            out.newLine();
                        }
                    }
                    seqWritten++;
                }
            }
        }
        out.flush();
        out.close();

        return seqWritten;
    }

}
