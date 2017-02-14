package net.sourceforge.ondex.parser.genego;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.parser.ONDEXParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Map;

public class Parser extends ONDEXParser
{

    private Map<String, ONDEXConcept> genes = new Hashtable<String, ONDEXConcept>();
    private Map<String, ONDEXConcept> proteins = new Hashtable<String, ONDEXConcept>();
    private Map<String, ONDEXConcept> compounds = new Hashtable<String, ONDEXConcept>();
    private Map<String, ONDEXConcept> enzymes = new Hashtable<String, ONDEXConcept>();
    private Map<String, ONDEXConcept> reactions = new Hashtable<String, ONDEXConcept>();

    private Map<String, ONDEXRelation> rels = new Hashtable<String, ONDEXRelation>();

    private ConceptClass ccGene = null;
    private ConceptClass ccProtein = null;
    private ConceptClass ccCompound = null;
    private ConceptClass ccEnzyme = null;
    private ConceptClass ccReaction = null;

    private DataSource dataSourceEntrez = null;
    private DataSource dataSourceUnknown = null;
    private DataSource dataSourceGGID = null;

    private EvidenceType etImpd = null;

    private RelationType rtGeneGOInt = null;

    private enum Fields {
        EGGID1(0),
        EGGID2(1),
        ENAME1(2),
        ENAME2(3),
        ELLID1(4),
        ELLID2(5),
        ETYPE1(6),
        ETYPE2(7),
        EREL_TYPE_ID(8),
        EREL_TYPE(9),
        EREL_MECH_TYPE_ID(10),
        EREL_MECH_TYPE(11);


        private final int index;

        Fields(int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }

    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                        FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false),
        };
    }

    @Override
    public String getName() {
        return "genego";
    }

    @Override
    public String getVersion() {
        return "20.07.09";
    }

    @Override
    public String getId() {
        return "genego";
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {
        ccGene = requireConceptClass("Gene");
        ccProtein = requireConceptClass("Protein");
        ccCompound = graph.getMetaData().getFactory().createConceptClass("Compound");
        ccEnzyme = requireConceptClass("Enzyme");
        ccReaction = requireConceptClass("Reaction");

        dataSourceEntrez = requireDataSource("NC_GE");
        dataSourceUnknown = requireDataSource("unknown");
        dataSourceGGID = graph.getMetaData().getFactory().createDataSource("GGID", "GENEGO ID", "GeneGO ID for the Regulation Object");

        etImpd = requireEvidenceType("IMPD");

        AttributeName atType = graph.getMetaData().getFactory().createAttributeName("Type", "Type", String.class);
        AttributeName atMech = graph.getMetaData().getFactory().createAttributeName("Mechanism", "Mechanism", String.class);

        rtGeneGOInt = graph.getMetaData().getFactory().createRelationType("genego_int");

        File file = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE));

        BufferedReader reader = new BufferedReader(new FileReader(file));

        long length = file.length();

        System.out.println("Parsing file: " + file.getAbsolutePath() + " Length: " + length);

        // skip first line
        reader.readLine();

        int counter = 1;

        while (reader.ready()) {

            if ((counter % 500) == 0)
                System.out.println("Line: " + counter);

            counter++;
            String line[] = reader.readLine().split("\t");

            if (!line[Fields.EREL_TYPE.index()].equals("0")) {
                ONDEXConcept from = createConcept(line[Fields.ETYPE1.index()], line[Fields.EGGID1.index()], line[Fields.ELLID1.index()], line[Fields.ENAME1.index()]);
                ONDEXConcept to = createConcept(line[Fields.ETYPE2.index()], line[Fields.EGGID2.index()], line[Fields.ELLID2.index()], line[Fields.ENAME2.index()]);

                if (from != null && to != null && rels.get(line[Fields.EGGID1.index()] + "&" + line[Fields.EGGID2.index()]) == null) {
                    ONDEXRelation r = graph.getFactory().createRelation(from, to, rtGeneGOInt, etImpd);
                    r.createAttribute(atType, line[Fields.EREL_TYPE.index()], false);
                    r.createAttribute(atMech, line[Fields.EREL_MECH_TYPE.index()], false);
                    rels.put(line[Fields.EGGID1.index()] + "&" + line[Fields.EGGID2.index()], r);
                }
            }
        }

    }

    ONDEXConcept createConcept(String type, String id, String llid, String name) {
        ONDEXConcept ret = null;

        if (type.equals("A")) {
            ret = proteins.get(id);

            if (ret == null) {
                ret = graph.getFactory().createConcept(id, dataSourceGGID, ccProtein, etImpd);
                ret.createConceptName(name, true);

                String ids[] = llid.split("&");

                for (int i = 0; i < ids.length; i++) {
                    if (ids[i].length() > 0) {
                        ret.createConceptAccession(ids[i], dataSourceEntrez, false);
                    }
                }

                proteins.put(id, ret);
            }
        } else if (type.equals("G")) {
            ret = genes.get(id);

            if (ret == null) {
                ret = graph.getFactory().createConcept(id, dataSourceGGID, ccGene, etImpd);
                ret.createConceptName(name, true);

                String ids[] = llid.split("&");

                for (int i = 0; i < ids.length; i++) {
                    if (ids[i].length() > 0) {
                        ret.createConceptAccession(ids[i], dataSourceEntrez, false);
                    }
                }

                genes.put(id, ret);
            }
        } else if (type.equals("C")) {
            ret = compounds.get(id);

            if (ret == null) {
                ret = graph.getFactory().createConcept(id, dataSourceGGID, ccCompound, etImpd);
                ret.createConceptName(name, true);

                compounds.put(id, ret);
            }
        } else if (type.equals("F")) {
            ret = enzymes.get(id);

            if (ret == null) {
                ret = graph.getFactory().createConcept(id, dataSourceGGID, ccEnzyme, etImpd);
                ret.createConceptName(name, true);

                enzymes.put(id, ret);
            }
        } else if (type.equals("R")) {
            ret = reactions.get(id);

            if (ret == null) {
                ret = graph.getFactory().createConcept(id, dataSourceGGID, ccReaction, etImpd);
                ret.createConceptName(name, true);

                reactions.put(id, ret);
            }
        }


        return ret;

    }

}
