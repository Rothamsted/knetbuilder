package net.sourceforge.ondex.parser.eqtl;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.parser.ONDEXParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Parser extends ONDEXParser
{

    private enum Fields {
        EENSEMBL(0),
        EHardyPValue(1),
        EHardySNP(2),
        ECooksonPValue(3),
        ECooksonSNP(4),
        EGeneVarPValue(5),
        EGeneVarSNP(6);

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
        return "Expression Quantitative Trait Loci";
    }

    @Override
    public String getVersion() {
        return "22.06.09";
    }

    @Override
    public String getId() {
        return "eqtl";
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {
        ConceptClass ccGene = requireConceptClass("Gene");
        ConceptClass ccQTL = requireConceptClass("QTL");
        ConceptClass ccSNP = graph.getMetaData().getFactory().createConceptClass("SNP");

        DataSource dataSourceEnsembl = requireDataSource("EMBL");
        DataSource dataSourceDbSNP = graph.getMetaData().getFactory().createDataSource("DBSNP", "dbSNP", "NCBI dbSNP database");
        DataSource dataSourceUnknown = requireDataSource("unknown");

        EvidenceType etImpd = requireEvidenceType("IMPD");
        EvidenceType etQTL = graph.getMetaData().getFactory().createEvidenceType("EQTL", "Expression Quantitative Trait Loci", "This information was inferred from the results of a Expression Quantitative Trait Loci Experiment");

        AttributeName atExp = graph.getMetaData().getFactory().createAttributeName("Experiment", "Experiment", String.class);
        AttributeName atPValue = graph.getMetaData().getFactory().createAttributeName("P-Value", "P-Value", Double.class);
        AttributeName atGene = graph.getMetaData().getFactory().createAttributeName("Gene", "Gene", String.class);
        AttributeName atSNP = graph.getMetaData().getFactory().createAttributeName("SNP", "SNP", String.class);

        RelationType rtGeneSNP = graph.getMetaData().getFactory().createRelationType("gene_eqtl_snp");

        File file = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE));
        BufferedReader reader = new BufferedReader(new FileReader(file));

        reader.readLine();

        int counter = 1;

        while (reader.ready()) {
            if ((counter % 500) == 0)
                System.out.println("Line: " + counter);

            counter++;

            String line[] = reader.readLine().split("\t");

            ONDEXConcept gene = graph.getFactory().createConcept(line[Fields.EENSEMBL.index()], dataSourceEnsembl, ccGene, etImpd);

            gene.createConceptName(line[Fields.EENSEMBL.index()], true);
            gene.createConceptAccession(line[Fields.EENSEMBL.index()], dataSourceEnsembl, false);

            // check for HARDY Data
            if (line.length > Fields.EHardySNP.index() && line[Fields.EHardySNP.index()].length() > 0) {
                ONDEXConcept snp = graph.getFactory().createConcept(line[Fields.EHardySNP.index()], dataSourceDbSNP, ccSNP, etImpd);
                snp.createConceptName(line[Fields.EHardySNP.index()], true);
                snp.createConceptAccession(line[Fields.EHardySNP.index()], dataSourceDbSNP, false);

                ONDEXConcept eqtl = graph.getFactory().createConcept("", dataSourceUnknown, ccQTL, etQTL);
                eqtl.createAttribute(atExp, "Hardy", false);
                eqtl.createAttribute(atPValue, Double.parseDouble(line[Fields.EHardyPValue.index()]), false);
                eqtl.createAttribute(atGene, line[Fields.EENSEMBL.index()], false);
                eqtl.createAttribute(atSNP, line[Fields.EHardySNP.index()], false);
                
                graph.getFactory().createRelation(gene, snp, rtGeneSNP, etQTL);
                //graph.getFactory().createRelation(gene, snp, eqtl, rtGeneSNP, etQTL);
            }

            // check for Cookson Data
            if (line.length > Fields.ECooksonSNP.index() && line[Fields.ECooksonSNP.index()].length() > 0) {
                ONDEXConcept snp = graph.getFactory().createConcept(line[Fields.ECooksonSNP.index()], dataSourceDbSNP, ccSNP, etImpd);
                snp.createConceptName(line[Fields.ECooksonSNP.index()], true);
                snp.createConceptAccession(line[Fields.ECooksonSNP.index()], dataSourceDbSNP, false);

                ONDEXConcept eqtl = graph.getFactory().createConcept("", dataSourceUnknown, ccQTL, etQTL);
                eqtl.createAttribute(atExp, "Cookson", false);
                eqtl.createAttribute(atPValue, Double.parseDouble(line[Fields.ECooksonPValue.index()]), false);
                eqtl.createAttribute(atGene, line[Fields.EENSEMBL.index()], false);
                eqtl.createAttribute(atSNP, line[Fields.ECooksonSNP.index()], false);

                graph.getFactory().createRelation(gene, snp, rtGeneSNP, etQTL);
                //graph.getFactory().createRelation(gene, snp, eqtl, rtGeneSNP, etQTL);
            }

            // check for GENEVAR Data
            if (line.length > Fields.EGeneVarSNP.index() && line[Fields.EGeneVarSNP.index()].length() > 0) {
                ONDEXConcept snp = graph.getFactory().createConcept(line[Fields.EGeneVarSNP.index()], dataSourceDbSNP, ccSNP, etImpd);
                snp.createConceptName(line[Fields.EGeneVarSNP.index()], true);
                snp.createConceptAccession(line[Fields.EGeneVarSNP.index()], dataSourceDbSNP, false);

                ONDEXConcept eqtl = graph.getFactory().createConcept("", dataSourceUnknown, ccQTL, etQTL);
                eqtl.createAttribute(atExp, "GeneVar", false);
                eqtl.createAttribute(atPValue, Double.parseDouble(line[Fields.EGeneVarPValue.index()]), false);
                eqtl.createAttribute(atGene, line[Fields.EENSEMBL.index()], false);
                eqtl.createAttribute(atSNP, line[Fields.EGeneVarSNP.index()], false);

                graph.getFactory().createRelation(gene, snp, rtGeneSNP, etQTL);
                //graph.getFactory().createRelation(gene, snp, eqtl, rtGeneSNP, etQTL);
            }
        }
    }

}
