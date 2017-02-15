/**
 *
 */
package net.sourceforge.ondex.parser.madisease;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.parser.ONDEXParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author cg262987
 */
public class Parser extends ONDEXParser
{

    private Map<String, ONDEXConcept> genes = new Hashtable<String, ONDEXConcept>();
    private Map<String, ONDEXConcept> diseases = new Hashtable<String, ONDEXConcept>();

    private enum Fields {
        EHUGO(0),
        EENSEMBL(1),
        ELOCUSLINK(2),
        ETIGID(3),
        EExperiment(4),
        ECall(5),
        ESpecies(6),
        EDisease(7),
        EPrimDisease(8),
        EMFC(9),
        EDir(10),
        EPValue(11);

        private final int index;

        Fields(int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }

    }

    /* (non-Javadoc)
      * @see net.sourceforge.ondex.ONDEXPlugin#getArgumentDefinitions()
      */

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{new FileArgumentDefinition(
                FileArgumentDefinition.INPUT_FILE,
                "madisease data file to import", true, true, false, false)};
    }

    /* (non-Javadoc)
      * @see net.sourceforge.ondex.ONDEXPlugin#getName()
      */

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "madisease";
    }

    /* (non-Javadoc)
      * @see net.sourceforge.ondex.ONDEXPlugin#getVersion()
      */

    @Override
    public String getVersion() {
        // TODO Auto-generated method stub
        return "16.06.09";
    }

    @Override
    public String getId() {
        return "madisease";
    }

    /* (non-Javadoc)
      * @see net.sourceforge.ondex.ONDEXPlugin#requiresValidators()
      */

    @Override
    public String[] requiresValidators() {
        // TODO Auto-generated method stub
        return new String[0];
    }

    /* (non-Javadoc)
      * @see net.sourceforge.ondex.ONDEXPlugin#start()
      */

    @Override
    public void start() throws Exception {
        ConceptClass ccGene = requireConceptClass("Gene");

        ConceptClass ccRes = graph.getMetaData().getFactory().createConceptClass("maresult");
        ConceptClass ccDisease = requireConceptClass("Disease");

        DataSource dataSourceEnsembl = requireDataSource("EMBL");
        DataSource dataSourceEntrez = requireDataSource("NC_GE");
        DataSource dataSourceTIG = graph.getMetaData().getFactory().createDataSource("TIGID", "GSK TIG ID", "GSK In-House Gene Identifier");
        DataSource dataSourceUnknown = requireDataSource("unknown");

        EvidenceType etImpd = requireEvidenceType("IMPD");
        EvidenceType etIEP = requireEvidenceType("IEP");

        AttributeName atTIG = graph.getMetaData().getFactory().createAttributeName("TIGID", "TIGID", String.class);
        AttributeName atExp = graph.getMetaData().getFactory().createAttributeName("Experiment", "Experiment", String.class);
        AttributeName atCall = graph.getMetaData().getFactory().createAttributeName("Call", "Call", String.class);
        AttributeName atSpecies = graph.getMetaData().getFactory().createAttributeName("Species", "Species", String.class);
        AttributeName atDisease = graph.getMetaData().getFactory().createAttributeName("Disease", "Disease", String.class);
        AttributeName atMFC = graph.getMetaData().getFactory().createAttributeName("Max Fold Change", "Max Fold Change", Double.class);
        AttributeName atDirection = graph.getMetaData().getFactory().createAttributeName("Direction", "Direction", String.class);
        AttributeName atPValue = graph.getMetaData().getFactory().createAttributeName("P-Value", "P-Value", Double.class);
        AttributeName atGDSAttrName = graph.getMetaData().getFactory().createAttributeName("GDSAttName", "GDSAttName", String.class);
        // ID for genes is TIGID

        RelationType rtGeneDis = graph.getMetaData().getFactory().createRelationType("gene_disease");

        File file = new File((String) args
                .getUniqueValue(FileArgumentDefinition.INPUT_FILE));
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

            /*System.out.print(line.length + ":");
               for(int i = 0; i < line.length; i++){
                   System.out.print(line[i] + "\t");
               }
               System.out.print("\n");*/

            String tig = line[Fields.ETIGID.index()];
            ONDEXConcept gene = genes.get(tig);


            if (gene == null) {
                // create new gene
                gene = graph.getFactory().createConcept(tig, dataSourceTIG, ccGene, etImpd);

                String hugo = line[Fields.EHUGO.index()];

                if (hugo.length() > 0)
                    gene.createConceptName(hugo, true);
                else
                    gene.createConceptName(tig, true);

                if (line[1].length() > 0)
                    gene.createConceptAccession(line[Fields.EENSEMBL.index()], dataSourceEnsembl, false);

                if (line[2].length() > 0)
                    gene.createConceptAccession(line[Fields.ELOCUSLINK.index()], dataSourceEntrez, false);

                gene.createConceptAccession(tig, dataSourceTIG, false);

                genes.put(tig, gene);
            }

            ONDEXConcept disease = diseases.get(line[Fields.EDisease.index()]);

            if (disease == null) {
                disease = graph.getFactory().createConcept(line[Fields.EDisease.index()], dataSourceUnknown, ccDisease, etImpd);

                disease.createConceptName(line[Fields.EDisease.index()], true);

                diseases.put(line[Fields.EDisease.index()], disease);
            }

            // create an experimental result
            ONDEXConcept res = graph.getFactory().createConcept("", dataSourceUnknown, ccRes, etIEP);

            res.createAttribute(atTIG, line[Fields.ETIGID.index()], false);
            res.createAttribute(atExp, line[Fields.EExperiment.index()], false);
            res.createAttribute(atCall, line[Fields.ECall.index()], false);
            res.createAttribute(atSpecies, line[Fields.ESpecies.index()], false);

            String dis = line[Fields.EDisease.index()];
            if (dis.length() > 0)
                res.createAttribute(atDisease, dis, false);
            else
                res.createAttribute(atDisease, line[Fields.EPrimDisease.index()], false);

            res.createAttribute(atMFC, line[Fields.EMFC.index()], false);
            res.createAttribute(atDirection, line[Fields.EDir.index()], false);
            res.createAttribute(atPValue, line[Fields.EPValue.index()], false);

            // add the MFC for the result to the gene
            String name = line[Fields.EExperiment.index()] + ":" + line[Fields.EDisease.index()];

            int count = 1;

            // EACH Attribute WILL BE LABELLED:
            // EXPERIMENT:DISEASE:COUNT
            // THIS SHALL BE MIRRORED IN THE MARESULT Attribute: GDS_ATTRIBUTE_NAME
            for (Attribute attribute : gene.getAttributes()) {
                if (attribute.getOfType().getId().contains(name)) {
                    count++;
                }
            }

            AttributeName atRes = graph.getMetaData().getFactory().createAttributeName(name + ":" + count, name + ":" + count, Double.class);

            Double mfc = Double.parseDouble(line[Fields.EMFC.index()].trim());

            String dir = line[Fields.EDir.index()];

            if (dir.equalsIgnoreCase("Down") && mfc > 0)
                mfc = mfc * -1;

            gene.createAttribute(atRes, mfc, false);
            res.createAttribute(atGDSAttrName, name + ":" + count, false);

            // create the relations
            //graph.getFactory().createRelation(gene, disease, res, rtGeneDis, etIEP);
            graph.getFactory().createRelation(gene, disease, rtGeneDis, etIEP);
        }
    }

}
