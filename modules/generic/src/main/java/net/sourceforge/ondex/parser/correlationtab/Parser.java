package net.sourceforge.ondex.parser.correlationtab;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.*;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.*;
import net.sourceforge.ondex.parser.ONDEXParser;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A relation centric aproach to importing concepts and relations Concepts are
 * defined as a header list. Relations are defined in value matrix/s where the
 * values are added as a Attribute property on the relation. Where no attributes are
 * specified no relation is created. Multiple values may be assigned by the use
 * of additional matrices.
 * <p/>
 * e.g. use: Table headers = gene names Matrix 1 = correlation coefficants
 * corresponding to the genes Matrix 2 = p values
 * <p/>
 * NB: one restriction of this parser that AttributeName objects parsed in must
 * be instantiatable with a single string as the constructor (I can adapt this
 * to cope with xml if anyone is interested but currently I see little point)
 *
 * @author hindlem
 * @version 02.07.2008
 */
@Authors(authors = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Parser extends ONDEXParser implements ArgumentNames,
        MetaData {

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(HEADER_VALUES_ARG,
                        HEADER_VALUES_ARG_DESC, true, null, false),
                new StringArgumentDefinition(INCLUSION_LIST_ARG,
                        INCLUSION_LIST_ARG_DESC, false, null, false),
                new StringArgumentDefinition(HEADER_CONCEPTCLASS_ARG,
                        HEADER_CONCEPTCLASS_ARG_DESC, true, "TARGETSEQ", false),
                new StringArgumentDefinition(CV_ARG, CV_ARG_DESC, true, null,
                        false),
                new StringArgumentDefinition(ACCESSION_CV_ARG,
                        ACCESSION_CV_ARG_DESC, true, null, false),
                new StringArgumentDefinition(EVIDENCE_TYPE_ARG,
                        EVIDENCE_TYPE_ARG_DESC, true, null, false),
                new StringMappingPairArgumentDefinition(
                        HEADER_ATTRIBUTENAME_ARG,
                        HEADER_ATTRIBUTENAME_ARG_DESC, true, null, true),
                new StringArgumentDefinition(CORRELATION_ATTRIBUTENAME_ARG,
                        CORRELATION_ATTRIBUTENAME_ARG_DESC, true, null, false),
                new StringMappingPairArgumentDefinition(CORRELATION_TABLE_ARG,
                        CORRELATION_TABLE_ARG_DESC, true, null, true),
                new RangeArgumentDefinition<Float>(PVALUE_CUT_ARG,
                        PVALUE_CUT_ARG_DESC, true, 0.05f, 0f, Float.MAX_VALUE,
                        Float.class),
                new RangeArgumentDefinition<Float>(MIN_CORR_ARG,
                        MIN_CORR_ARG_DESC, true, 0f, 0f, Float.MAX_VALUE,
                        Float.class),
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR,
                        "directory with correlation files", true, true, true,
                        false)

        };
    }

    @Override
    public String getName() {
        return "Correlation table parser";
    }

    @Override
    public String getVersion() {
        return "02.07.2008";
    }

    @Override
    public String getId() {
        return "correlationtab";
    }

    @Override
    public void start() throws InvalidPluginArgumentException {

        File dir = new File((String) args
                .getUniqueValue(FileArgumentDefinition.INPUT_DIR));

        // String taxID = (String) pa.getUniqueValue(TAXID_ARG);
        AttributeName taxIdAttr = graph.getMetaData().getAttributeName(
                ATT_TAXID);
        if (taxIdAttr == null) {
            fireEventOccurred(new AttributeNameMissingEvent(ATT_TAXID
                    + " does not exist", getCurrentMethodName()));
            return;
        }

        String headerTable = dir.getAbsolutePath() + File.separator
                + args.getUniqueValue(HEADER_VALUES_ARG);
        if (!new File(headerTable).exists()) {
            fireEventOccurred(new WrongParameterEvent(headerTable
                    + " does not exist", getCurrentMethodName()));
            return;
        }

        String headerCCName = (String) args
                .getUniqueValue(HEADER_CONCEPTCLASS_ARG);
        ConceptClass headerConceptClass = graph.getMetaData().getConceptClass(
                headerCCName);

        if (headerConceptClass == null) {
            fireEventOccurred(new ConceptClassMissingEvent(headerCCName
                    + " does not exist", getCurrentMethodName()));
            return;
        }

        RelationType rtsDirReg = graph.getMetaData().getRelationType(
                RTS_DIR_REG);

        if (rtsDirReg == null) {
            rtsDirReg = getRelationType(RTS_DIR_REG);
            if (rtsDirReg == null) {
                fireEventOccurred(new RelationTypeMissingEvent(RTS_DIR_REG
                        + " does not exist", getCurrentMethodName()));
                return;
            }
        }

        RelationType rtsInvReg = graph.getMetaData().getRelationType(
                RTS_INV_REG);

        if (rtsInvReg == null) {
            rtsInvReg = getRelationType(RTS_INV_REG);
            if (rtsInvReg == null) {
                fireEventOccurred(new RelationTypeMissingEvent(RTS_INV_REG
                        + " does not exist", getCurrentMethodName()));
                return;
            }
        }

        String cvName = (String) args.getUniqueValue(CV_ARG);
        DataSource dataSource = graph.getMetaData().getDataSource(cvName);

        if (dataSource == null) {
            fireEventOccurred(new DataSourceMissingEvent(cvName
                    + " does not exist : creating", getCurrentMethodName()));
            dataSource = graph.getMetaData().getFactory().createDataSource(cvName);
        }

        String cvaName = (String) args.getUniqueValue(ACCESSION_CV_ARG);
        DataSource cva = graph.getMetaData().getDataSource(cvName);

        if (cva == null) {
            fireEventOccurred(new DataSourceMissingEvent(cvaName + " does not exist",
                    getCurrentMethodName()));
            return;
        }

        String etName = (String) args.getUniqueValue(EVIDENCE_TYPE_ARG);
        EvidenceType et = graph.getMetaData().getEvidenceType(etName);
        if (et == null) {
            fireEventOccurred(new EvidenceTypeMissingEvent(etName
                    + " does not exist", getCurrentMethodName()));
            return;
        }

        EvidenceType etIMPD = graph.getMetaData()
                .getEvidenceType(MetaData.IMPD);
        if (etIMPD == null) {
            fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.IMPD,
                    Parser.getCurrentMethodName()));
        }

        AttributeName pValue = graph.getMetaData().getAttributeName(
                MetaData.PVALUE);

        AttributeName att = graph.getMetaData().getAttributeName(
                (String) args.getUniqueValue(CORRELATION_ATTRIBUTENAME_ARG));
        if (att == null) {
            fireEventOccurred(new AttributeNameMissingEvent(args
                    .getUniqueValue(CORRELATION_ATTRIBUTENAME_ARG)
                    + " does not exist", getCurrentMethodName()));
            return;
        }

        HashMap<AttributeName, Object> objectValue = new HashMap<AttributeName, Object>();

        List<String> attributeToValue = (List<String>) args
                .getObjectValueList(HEADER_ATTRIBUTENAME_ARG);
        for (String valuePair : attributeToValue) {
            String[] values = valuePair.split(",");
            AttributeName patt = graph.getMetaData()
                    .getAttributeName(values[0]);
            if (patt == null) {
                fireEventOccurred(new AttributeNameMissingEvent(values[0]
                        + " does not exist ignoring attribute value pair :"
                        + valuePair, getCurrentMethodName()));
                continue;
            }
            Object obj = castToNativeObject(patt, values[1]);
            objectValue.put(patt, obj);
        }

        ArrayList<String> inclusions = null;

        String inclusionFile = (String) args.getUniqueValue(INCLUSION_LIST_ARG);
        if (inclusionFile != null) {
            inclusions = new ArrayList<String>();
            try {
                BufferedReader br = new BufferedReader(new FileReader(dir
                        .getAbsolutePath()
                        + File.separator + inclusionFile));
                while (br.ready()) {
                    String line = br.readLine();
                    inclusions.add(line.trim());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ArrayList<ONDEXConcept> headers = new ArrayList<ONDEXConcept>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(headerTable));
            while (br.ready()) {
                String line = br.readLine();

                if (inclusions != null && !inclusions.contains(line.trim())) {
                    headers.add(null); // ignore this concept
                    continue;
                }

                ONDEXConcept newConcept = graph.getFactory().createConcept(
                        line, dataSource, headerConceptClass, et);
                newConcept.createConceptAccession(line, cva, false);

                headers.add(newConcept);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        float[][] pValues = new float[headers.size()][headers.size()];
        float[][] corr = new float[headers.size()][headers.size()];

        // init coorelation matrix
        for (float[] sub : corr) {
            Arrays.fill(sub, 0);
        }

        Pattern tab = Pattern.compile("\t");

        boolean first = true;

        List<String> list = (List<String>) args
                .getObjectValueList(CORRELATION_TABLE_ARG);
        for (String obj : list) {
            String[] files = obj.split(",");

            String pValueFile = dir.getAbsolutePath() + File.separator
                    + files[1];
            if (!new File(pValueFile).exists()) {
                fireEventOccurred(new WrongParameterEvent(pValueFile
                        + " does not exist: ignoring", getCurrentMethodName()));
                return;
            }

            System.out.println("reading " + pValueFile);

            // clear and init pValue matrix
            for (float[] sub : pValues) {
                Arrays.fill(sub, -1);
            }

            Float pValueCut = (Float) args.getUniqueValue(PVALUE_CUT_ARG);

            try {
                BufferedReader br = new BufferedReader(new FileReader(
                        pValueFile), 8192 * 10);
                int lineNum = 0;
                while (br.ready()) {
                    String line = br.readLine();

                    if (lineNum % 1000 == 0)
                        System.out.println("parsed pValue :" + lineNum);

                    String[] values = tab.split(line);

                    for (int i = 1; i < values.length; i++) {

                        if (i - 1 == lineNum || values[i].trim().length() == 0) {
                            continue; // do not create self relations or
                            // relations for blank values
                        }

                        float pValueInst = Float.parseFloat(values[i].trim());

                        if (pValueInst > pValueCut) {
                            continue;
                        }

                        pValues[lineNum][i - 1] = pValueInst;

                    }
                    lineNum++;
                }
                br.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String corrFile = dir.getAbsolutePath() + File.separator + files[0];

            System.out.println("reading " + corrFile);

            if (!new File(corrFile).exists()) {
                fireEventOccurred(new WrongParameterEvent(corrFile
                        + " does not exist: exiting", getCurrentMethodName()));
                return;
            }

            float corrCut = (Float) args.getUniqueValue(MIN_CORR_ARG);

            try {
                BufferedReader br = new BufferedReader(
                        new FileReader(corrFile), 8192 * 2);
                int lineNum = 0;

                while (br.ready()) {
                    String line = br.readLine();

                    if (lineNum % 1000 == 0)
                        System.out.println("parsed correlation :" + lineNum);

                    String[] values = tab.split(line);

                    for (int i = 1; i < values.length; i++) {

                        if (i - 1 == lineNum || values[i].trim().length() == 0) {
                            continue; // do not create self relations or
                            // relations for blank values
                        }

                        float pValueInst = pValues[lineNum][i - 1];

                        float correlation = Float.parseFloat(values[i].trim());

                        if (pValueInst == -1 || corrCut > 0
                                && Math.abs(correlation) < corrCut) {
                            if (corr != null) {
                                corr[lineNum][i - 1] = 0; // clear this
                                // correlation
                            }
                            continue;
                        }

                        if (first) {
                            corr[lineNum][i - 1] = correlation;
                        }
                    }

                    lineNum++;
                }
                br.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            first = false;
        }

        System.out.println("creating correlation relations");

        for (int j = 0; j < corr.length; j++) {
            ONDEXConcept fromConcept = headers.get(j);

            if (fromConcept == null) { // this is a ignore concept
                continue;
            }

            for (int k = 0; k < corr[j].length; k++) { // corr[j].length should
                // always be the same as
                // corr.length (its a
                // square matrix)

                if (corr[j][k] == 0) {
                    continue;
                }

                RelationType rts = rtsDirReg;
                if (corr[j][k] < 0) {
                    rts = rtsInvReg;
                }

                ONDEXConcept toConcept = headers.get(k);

                if (toConcept == null) { // this is a ignore concept
                    continue;
                }

                ONDEXRelation relation = graph.getRelation(fromConcept,
                        toConcept, rts);
                if (relation == null) {
                    relation = graph.getFactory().createRelation(fromConcept,
                            toConcept, rts, et);
                }

                relation.createAttribute(pValue,
                        Float.valueOf(pValues[j][k]), false);
                relation.createAttribute(att, Float.valueOf(corr[j][k]),
                        false);
            }
        }

    }

    /**
     * Attempts to change a string value to a native object based on the
     * AttributeName class
     *
     * @param att    the attribute name that the string belongs to
     * @param string the string value
     * @return native object if possible, else null
     */
    private Object castToNativeObject(AttributeName att, String string) {
        if (att.getDataType() == String.class) {
            return string;
        }

        // attempt to instantiate with a String constructor
        try {
            Class<?>[] args = new Class<?>[]{String.class};
            Constructor<?> constructor = att.getDataType().getConstructor(args);
            return constructor.newInstance(new Object[]{string.trim()});
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns an RelationType for a given RelationType or RelationTypeSet ID.
     *
     * @param name - name of a RelationType or relationTypeSet
     * @return RelationType
     */
    private RelationType getRelationType(String name) {
        RelationType rts = graph.getMetaData().getRelationType(name);
        if (rts != null) {
            return rts;
        }
        RelationType rt = graph.getMetaData().getRelationType(name);
        if (rt != null) {
            return graph.getMetaData().getFactory().createRelationType(
                    rt.getId(), rt);
        } else {
            System.out.println("Entity Parser Missing RelationType: " + name);
        }
        return null;
    }

    /**
     * Convenience method for outputing the current method name in a dynamic way
     *
     * @return the calling method name
     */
    public static String getCurrentMethodName() {
        Exception e = new Exception();
        StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
        String name = trace.getMethodName();
        String className = trace.getClassName();
        int line = trace.getLineNumber();
        return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line
                + "]";
    }

}
