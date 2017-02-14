package net.sourceforge.ondex.parser.wgs;

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
    private Map<String, ONDEXConcept> diseases = new Hashtable<String, ONDEXConcept>();
    private Map<String, ONDEXConcept> snps = new Hashtable<String, ONDEXConcept>();

    private enum Fields {
        ERSNAME(0),
        ESNPTYPE(1),
        EGENE(2),
        EEXPNAME(3),
        EDISEASE(4),
        EMINP(5);

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
        return "wgs";
    }

    @Override
    public String getVersion() {
        return "24.06.09";
    }

    @Override
    public String getId() {
        return "wgs";
    }

    @Override
    public String[] requiresValidators() {
        // TODO Auto-generated method stub
        return new String[0];
    }

    @Override
    public void start() throws Exception {
        ConceptClass ccGene = requireConceptClass("Gene");
        ConceptClass ccDisease = requireConceptClass("Disease");

        ConceptClass ccSNP = graph.getMetaData().getFactory().createConceptClass("SNP");
        ConceptClass ccRes = graph.getMetaData().getFactory().createConceptClass("WGS:RESULT");

        DataSource dataSourceEnsembl = requireDataSource("EMBL");
        DataSource dataSourceDbSNP = graph.getMetaData().getFactory().createDataSource("DBSNP", "dbSNP", "NCBI dbSNP database");
        DataSource dataSourceUnknown = requireDataSource("unknown");

        EvidenceType etImpd = requireEvidenceType("IMPD");
        EvidenceType etWGS = graph.getMetaData().getFactory().createEvidenceType("WGS", "Whole Genome Scan", "This information was inferred from the results of a Whole Genome Scan");

        AttributeName atExp = graph.getMetaData().getFactory().createAttributeName("Experiment", "Experiment", String.class);
        AttributeName atPValue = graph.getMetaData().getFactory().createAttributeName("P-Value", "P-Value", Double.class);
        AttributeName atSNP = graph.getMetaData().getFactory().createAttributeName("SNP", "SNP", String.class);
        AttributeName atType = graph.getMetaData().getFactory().createAttributeName("SNPType", "SNP Type", String.class);
        AttributeName atDis = graph.getMetaData().getFactory().createAttributeName("Disease", "Disease", String.class);

        RelationType rtGeneSnp = graph.getMetaData().getFactory().createRelationType("gene_snp");
        RelationType rtSnpDis = graph.getMetaData().getFactory().createRelationType("snp_wgs_dis");

        File file = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE));
        BufferedReader reader = new BufferedReader(new FileReader(file));

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

            String rsName = line[Fields.ERSNAME.index()];
            ONDEXConcept snp = snps.get(rsName);

            if (snp == null) {
                snp = graph.getFactory().createConcept(rsName, dataSourceDbSNP, ccSNP, etImpd);

                snp.createConceptName(rsName, true);
                snp.createConceptAccession(rsName, dataSourceDbSNP, false);

                snp.createAttribute(atType, line[Fields.ESNPTYPE.index()], false);

                snps.put(rsName, snp);
            }

            ONDEXConcept disease = diseases.get(line[Fields.EDISEASE.index()]);

            if (disease == null) {
                disease = graph.getFactory().createConcept(line[Fields.EDISEASE.index()], dataSourceUnknown, ccDisease, etImpd);

                disease.createConceptName(line[Fields.EDISEASE.index()], true);

                diseases.put(line[Fields.EDISEASE.index()], disease);
            }

            String old_gene_ids[] = line[Fields.EGENE.index()].split("&");


            // remove any duplicate entries from the array
            String gene_ids[] = removeDuplicates(old_gene_ids);

            for (int i = 0; i < gene_ids.length; i++) {

                if (gene_ids[i].length() > 0) {
                    ONDEXConcept gene = genes.get(gene_ids[i]);


                    if (gene == null) {
                        // create new gene
                        gene = graph.getFactory().createConcept(gene_ids[i], dataSourceEnsembl, ccGene, etImpd);

                        gene.createConceptName(gene_ids[i], true);
                        gene.createConceptAccession(gene_ids[i], dataSourceEnsembl, false);

                        genes.put(gene_ids[i], gene);
                    }

                    graph.getFactory().createRelation(snp, gene, rtGeneSnp, etImpd);
                }
            }

            // create a WGS Result
            ONDEXConcept res = graph.getFactory().createConcept("", dataSourceUnknown, ccRes, etWGS);

            res.createAttribute(atExp, line[Fields.EEXPNAME.index()], false);
            res.createAttribute(atDis, line[Fields.EDISEASE.index()], false);

            if (line.length > Fields.EMINP.index()) {
                res.createAttribute(atPValue, line[Fields.EMINP.index()], false);
            }

            res.createAttribute(atSNP, line[Fields.ERSNAME.index()], false);

            graph.getFactory().createRelation(snp, disease, rtSnpDis, etWGS);
            //graph.getFactory().createRelation(snp, disease, res, rtSnpDis, etWGS);
        }
    }

    private String[] removeDuplicates(String[] input) {
        String[] output = new String[0];

        for (int i = 0; i < input.length; i++) {
            if (input[i].length() > 0) {
                boolean duplicate = false;
                for (int j = 0; j < output.length; j++) {
                    if (output[j].equals(input[i])) {
                        duplicate = true;
                        break;
                    }
                }
                if (duplicate == false) {
                    String[] tmp = new String[output.length + 1];
                    tmp[output.length] = input[i];
                    System.arraycopy(output, 0, tmp, 0, output.length);
                    output = tmp;
                }
            }
        }
        return output;
    }

}
