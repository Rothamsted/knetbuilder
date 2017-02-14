package net.sourceforge.ondex.parser.poplar_chip;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.tools.auxfunctions.SQLTableReader;
import net.sourceforge.ondex.tools.auxfunctions.TabArrayObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Parser for Poplar blast hits
 * <p/>
 * This reads an internal Rothamsted MySQL table and uses auxfunctions.
 * Cut offs can be defined by either top n hits, bit score or eval
 * Sorting by eval or bit score is possible
 * <p/>
 * Should the location of the database change, the line saying
 * Connection conn = DriverManager.getConnection(blah blah blah) should be updated.
 *
 * @author sckuo
 */

public class Parser extends ONDEXParser
{

    // declaring metadata elements needed

    private DataSource dataSourceJGI;
    private DataSource dataSourceUNI;
    private DataSource dataSourceRefSeq;
    private DataSource dataSourceTair;

    private ConceptClass ccGene;
    private ConceptClass ccProt;

    private EvidenceType etIMPD;
    private EvidenceType etBlast;

    private RelationType rtHSS;

    private AttributeName anTaxID;
    private AttributeName anBLEV;
    private AttributeName anBitscore;

    // these are <id, concept> maps

    private HashMap<Integer, ONDEXConcept> jgiConcepts = new HashMap<Integer, ONDEXConcept>();
    private HashMap<String, ONDEXConcept> uniprotConcepts = new HashMap<String, ONDEXConcept>();
    private HashMap<String, ONDEXConcept> refseqConcepts = new HashMap<String, ONDEXConcept>();
    private HashMap<String, ONDEXConcept> tairConcepts = new HashMap<String, ONDEXConcept>();

    // helper function
    private EntityFactory ef;

    // this reads a database so neither directory or files are needed

    // this takes a few parameters

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {

        RangeArgumentDefinition<Integer> topnhit = new RangeArgumentDefinition<Integer>(ArgumentNames.TOPNHITS, ArgumentNames.TOPNHITS_DESC, false, 10, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.class);
        BooleanArgumentDefinition bitscore_or_eval = new BooleanArgumentDefinition(ArgumentNames.EVAL_OR_SCORE, ArgumentNames.EVAL_OR_SCORE_DESC, false, true);
        RangeArgumentDefinition<Float> evalCutOff = new RangeArgumentDefinition<Float>(ArgumentNames.EVALCUTOFF, ArgumentNames.EVALCUTOFF_DESC, false, 0.000001f, Float.MIN_VALUE, Float.MAX_VALUE, Float.class);
        RangeArgumentDefinition<Float> bitScoreCutOff = new RangeArgumentDefinition<Float>(ArgumentNames.SCORECUTOFF, ArgumentNames.SCORECUTOFF_DESC, false, 100f, Float.MIN_VALUE, Float.MAX_VALUE, Float.class);
        StringArgumentDefinition dbParam = new StringArgumentDefinition(ArgumentNames.DBPARAM, ArgumentNames.DBPARAM_DESC, false, null, false);

        return new ArgumentDefinition<?>[]{topnhit, bitscore_or_eval, evalCutOff, bitScoreCutOff, dbParam};

    }

    @Override
    public String getName() {
        return "Poplar chip annotation parser";
    }

    @Override
    public String getVersion() {
        return "live";
    }

    @Override
    public String getId() {
        return "poplar_chip";
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
        // no validators required
    }

    @Override
    public void start() throws Exception {

        // obtain the parameters passed to this parser
        Integer topnhits = (Integer) args.getUniqueValue(ArgumentNames.TOPNHITS);
        Boolean eval_or_bitScore = (Boolean) args.getUniqueValue(ArgumentNames.EVAL_OR_SCORE);
        Float evalCutOff = (Float) args.getUniqueValue(ArgumentNames.EVALCUTOFF);
        Float bitScoreCutOff = (Float) args.getUniqueValue(ArgumentNames.SCORECUTOFF);
        String dbParam = (String) args.getUniqueValue(ArgumentNames.DBPARAM);

        ef = graph.getFactory();

        // Initialise the MetaData elements
        try {

            dataSourceJGI = requireDataSource(MetaData.CV_JGI);
            dataSourceUNI = requireDataSource(MetaData.CV_UNI);
            dataSourceRefSeq = requireDataSource(MetaData.CV_RefSeq);
            dataSourceTair = requireDataSource(MetaData.CV_Tair);

            ccGene = requireConceptClass(MetaData.CC_GENE);
            ccProt = requireConceptClass(MetaData.CC_PROTEIN);

            etIMPD = requireEvidenceType(MetaData.ET_IMPD);
            etBlast = requireEvidenceType(MetaData.ET_BLAST);

            anTaxID = requireAttributeName(MetaData.AN_TAXID);
            anBLEV = requireAttributeName(MetaData.AN_BLEV);
            anBitscore = requireAttributeName(MetaData.AN_BITSCORE);

            rtHSS = requireRelationType(MetaData.RT_HSS);

        } catch (DataSourceMissingException e) {
            e.printStackTrace();
        } catch (ConceptClassMissingException e) {
            e.printStackTrace();
        } catch (EvidenceTypeMissingException e) {
            e.printStackTrace();
        } catch (AttributeNameMissingException e) {
            e.printStackTrace();
        }

        HashSet<String> id_set = new HashSet<String>(2000);

        // grab database connection
        Connection conn = DriverManager.getConnection("jdbc:mysql://coco:3306/chip_annotation_test", "wheataffydev", "affymetrix");

        // grab all unique poplar ids

        Class[] id_mask = {String.class};

        String id_query = "select distinct query_accn from alignment_hit";

        SQLTableReader get_ids = new SQLTableReader(conn, id_query, id_mask);
        TabArrayObject id_tao;

        while ((id_tao = get_ids.getNext()) != null) {
            id_set.add((String) id_tao.getElement(0));
        }
        get_ids.close();

        // dump some statistics

        int id_set_size = id_set.size();

        System.out.println("Id set cardinality: " + id_set_size);


        // end dump


        // defined expected results from the database query
        Class[] ahit_mask = {Integer.class, Integer.class, Integer.class, String.class, String.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class, Double.class, Float.class};

        Iterator<String> it = id_set.iterator();

        int track = 0; // this is for showing progress
        float progress = 0; // this is for showing progress

        while (it.hasNext()) {

            // dump progress reporting

            progress = (100 * track) / id_set_size;
            if ((track % 1000) == 0) {
                System.out.println("Processing " + progress + "%");
            }
            track++;

            // end progress statistics

            // these are required for the settings
            String limit;
            String orderBy;
            String eval;
            String bitScore;
            String database;

            // grab the next id and compose the SQL statement with it
            String accn = it.next();

            String ahit_query = "select * from alignment_hit where query_accn = \"" + accn + "\"";

            // this is a set of if parameter exists, then add sql option, else do nothing blocks

            if (topnhits == null) {
                limit = "";
            } else {
                limit = " limit " + topnhits.toString();
            }

            if (eval_or_bitScore == null) {
                orderBy = "";
            } else {

                if (eval_or_bitScore) {
                    orderBy = " order by score";
                } else {
                    orderBy = " order by evalue";
                }

            }

            if (evalCutOff == null) {
                eval = "";
            } else {
                eval = " and evalue < " + evalCutOff;
            }


            if (bitScoreCutOff == null) {
                bitScore = "";
            } else {
                bitScore = " and score > " + bitScoreCutOff;
            }


            if (dbParam == null) {
                database = "";
            } else {
                database = " and xref_type = '" + dbParam + "'";
            }

            // compose the SQL
            ahit_query += eval + bitScore + database + orderBy + limit;

            // grab a table reader
            SQLTableReader sr = new SQLTableReader(conn, ahit_query, ahit_mask);

            // grab a container for output of SQL query
            TabArrayObject tophits_tao;

            while ((tophits_tao = sr.getNext()) != null) {

                // if the JGI concept doesn't exist, create it
                Integer jgiId = Integer.parseInt(tophits_tao.getElement(3).toString());

                ONDEXConcept jgiObject;

                if (jgiConcepts.containsKey(jgiId)) {
                    jgiObject = jgiConcepts.get(jgiId);
                } else {
                    jgiObject = ef.createConcept(jgiId.toString(), dataSourceJGI, ccGene, etIMPD);
                    jgiObject.createConceptAccession(jgiId.toString(), dataSourceJGI, false);
                    jgiConcepts.put(jgiId, jgiObject);
                }

                // figure out whether this is uniprot, tair, or refseq

                ONDEXConcept targetObject = null;

                String xref = (String) tophits_tao.getElement(5);
                String hit_accn = (String) tophits_tao.getElement(4);

                if (xref.equals("UNIPROT_TrEMBL")) {

                    // check for existence
                    if (uniprotConcepts.containsKey(hit_accn)) {
                        targetObject = uniprotConcepts.get(hit_accn);
                    } else {

                        // create TrEMBL object
                        targetObject = ef.createConcept(hit_accn, dataSourceUNI, ccProt, etIMPD);
                        uniprotConcepts.put(hit_accn, targetObject);

                        // TODO: add tax_id
                        targetObject.createConceptAccession(hit_accn, dataSourceUNI, false);
                    }


                } else if (xref.equals("UNIPROT_Swiss-Prot")) {

                    // check for existence
                    if (uniprotConcepts.containsKey(hit_accn)) {
                        targetObject = uniprotConcepts.get(hit_accn);
                    } else {

                        // create SwissProt object
                        targetObject = ef.createConcept(hit_accn, dataSourceUNI, ccProt, etIMPD);
                        uniprotConcepts.put(hit_accn, targetObject);

                        // TODO: tax_id?
                        targetObject.createConceptAccession(hit_accn, dataSourceUNI, false);
                    }


                } else if (xref.equals("TAIR")) {

                    // check for existence
                    if (tairConcepts.containsKey(hit_accn)) {
                        targetObject = tairConcepts.get(hit_accn);
                    } else {

                        // create TAIR object
                        targetObject = ef.createConcept(hit_accn, dataSourceTair, ccProt, etIMPD);
                        tairConcepts.put(hit_accn, targetObject);


                        // TODO: add tax_id?
                        targetObject.createConceptAccession(hit_accn, dataSourceTair, false);
                    }


                } else if (xref.equals("REFSEQ")) {

                    // check for existence
                    if (refseqConcepts.containsKey(hit_accn)) {
                        targetObject = refseqConcepts.get(hit_accn);
                    } else {

                        // create RefSeq object
                        targetObject = ef.createConcept(hit_accn, dataSourceRefSeq, ccProt, etIMPD);
                        refseqConcepts.put(hit_accn, targetObject);

                        // TODO: add tax_id?
                        targetObject.createConceptAccession(hit_accn, dataSourceRefSeq, false);
                    }

                } else {

                    // xref that isn't recognised to be any of the above
                    System.err.println("Parser panic! Fail to recognise.");
                    System.err.println(xref);

                }

                // TODO: The following if needs to be removed when things work.

                if ((graph.getRelation(jgiObject, targetObject, rtHSS)) != null) {
                    continue; // if there is already a relation like this, skip.
                }

                // otherwise, create a relation like this
                ONDEXRelation r = ef.createRelation(jgiObject, targetObject, rtHSS, etBlast);

                r.createAttribute(anBLEV, tophits_tao.getElement(10), false);
                r.createAttribute(anBitscore, tophits_tao.getElement(11), false);


            }
            sr.close(); // close the SQLTableReader

        }

    }

}
