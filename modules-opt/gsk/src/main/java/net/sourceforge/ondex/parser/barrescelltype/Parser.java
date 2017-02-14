package net.sourceforge.ondex.parser.barrescelltype;

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
    private Map<String, ONDEXConcept> cellTypes = new Hashtable<String, ONDEXConcept>();

    private enum Fields {
        EHUGO(0),
        EENSEMBL(1),
        ELOCUSLINK(2),
        ETIGID(3),
        ECall(4),
        ESpecies(5),
        ECellType(6),
        EDetection(7),
        EBestIntensity(8);

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
                        FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false)

        };
    }

    @Override
    public String getName() {
        return "barres cell type";
    }

    @Override
    public String getVersion() {
        return "06.07.09";
    }

    @Override
    public String getId() {
        return "barrescelltype";
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {
        // TODO Auto-generated method stub
        ConceptClass ccGene = requireConceptClass("Gene");

        ConceptClass ccRes = graph.getMetaData().getFactory().createConceptClass("protdisease");
        ConceptClass ccCellType = graph.getMetaData().getFactory().createConceptClass("CellType");

        DataSource dataSourceEnsembl = requireDataSource("EMBL");
        DataSource dataSourceEntrez = requireDataSource("NC_GE");
        DataSource dataSourceTIG = graph.getMetaData().getFactory().createDataSource("TIGID", "GSK TIG ID", "GSK In-House Gene Identifier");
        DataSource dataSourceUnknown = requireDataSource("unknown");

        EvidenceType etImpd = requireEvidenceType("IMPD");

        AttributeName atTIG = graph.getMetaData().getFactory().createAttributeName("TIGID", "TIGID", String.class);
        AttributeName atCall = graph.getMetaData().getFactory().createAttributeName("Call", "Call", String.class);
        AttributeName atSpecies = graph.getMetaData().getFactory().createAttributeName("Species", "Species", String.class);
        AttributeName atCellType = graph.getMetaData().getFactory().createAttributeName("CellType", "CellType", String.class);
        AttributeName atDetection = graph.getMetaData().getFactory().createAttributeName("Detection", "Detection", String.class);
        AttributeName atBestIntensity = graph.getMetaData().getFactory().createAttributeName("BestIntensity", "BestIntensity", Integer.class);

        RelationType rtGeneCellType = graph.getMetaData().getFactory().createRelationType("gene_celltype");

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

            ONDEXConcept cellType = cellTypes.get(line[Fields.ECellType.index()]);

            if (cellType == null) {
                cellType = graph.getFactory().createConcept(line[Fields.ECellType.index()], dataSourceUnknown, ccCellType, etImpd);

                cellType.createConceptName(line[Fields.ECellType.index()], true);

                cellTypes.put(line[Fields.ECellType.index()], cellType);
            }

            // create an experimental result
            ONDEXConcept res = graph.getFactory().createConcept("", dataSourceUnknown, ccRes, etImpd);

            res.createAttribute(atTIG, line[Fields.ETIGID.index()], false);
            res.createAttribute(atCall, line[Fields.ECall.index()], false);
            res.createAttribute(atSpecies, line[Fields.ESpecies.index()], false);
            res.createAttribute(atCellType, line[Fields.ECellType.index()], false);
            res.createAttribute(atDetection, line[Fields.EDetection.index()], false);
            res.createAttribute(atBestIntensity, line[Fields.EBestIntensity.index()], false);
            // create the relations
            graph.getFactory().createRelation(gene, cellType, rtGeneCellType, etImpd);
            //graph.getFactory().createRelation(gene, cellType, res, rtGeneCellType, etImpd);
        }
    }

}
