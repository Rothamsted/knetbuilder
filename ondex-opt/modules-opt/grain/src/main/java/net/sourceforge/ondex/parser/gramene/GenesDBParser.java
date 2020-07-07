package net.sourceforge.ondex.parser.gramene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.parser.gramene.genes.GeneDBRef2ObjectDB;
import net.sourceforge.ondex.parser.gramene.genes.GeneRef2ObjectRow;
import net.sourceforge.ondex.parser.gramene.genes.GeneSpeciesDB;
import net.sourceforge.ondex.parser.gramene.genes.GeneXRefDB;

/**
 * INDEX for gene_gene data table
 * 0 `gene_id` int(11) NOT NULL default '0' PRIMARY KEY,
 * 1 `accession` varchar(32) NOT NULL default '',
 * 2 `symbol` varchar(128) default NULL,
 * 3 `name` varchar(128) default NULL,
 * 4 `species_id` int(11) NOT NULL default '0',
 * 5 `gene_type_id` int(11) NOT NULL default '0',
 * 6 `chromosome` varchar(32) default NULL,
 * 7 `description` text NOT NULL,
 * 8 `public_curation_comment` text,
 * 9 `internal_curation_comment` text,
 * 10 `has_phenotype` enum('yes','no','not curated') NOT NULL default 'not curated',
 * 11 `is_obsolete` tinyint(1) NOT NULL default '0'
 * <p/>
 * Controls the parsing of genes from the gramene gene database (as oppose the gene products or qtldb)
 *
 * @author hoekmanb
 */
public class GenesDBParser {


    private ONDEXGraph graph;

    private OntologyParser ontoParser;

    public GenesDBParser(ONDEXGraph graph, OntologyParser ontoParser) {

        this.graph = graph;
        this.ontoParser = ontoParser;
    }

    private static final String SPECIES_FILE = "gene_species.txt";
    private static final String XREF_FILE = "gene_dbxref.txt";
    private static final String REF2OBJECT_FILE = "gene_dbxref_to_object.txt";

    private static final String GENEGENE_FILE = "gene_gene.txt";

    private HashMap<String, Integer> grameneGeneAccession2conceptId = new HashMap<String, Integer>();
    private HashMap<Integer, Integer> grameneGeneID2conceptId = new HashMap<Integer, Integer>();


    /**
     * Parses genes from the specified dir
     *
     * @param dir the folder containing tab dump files from the GeneDB
     */
    public void parseGenes(String dir) {

        GeneSpeciesDB speciesTable = new GeneSpeciesDB(dir + File.separator + SPECIES_FILE);
        GeneXRefDB xrefTable = new GeneXRefDB(dir + File.separator + XREF_FILE, graph);
        GeneDBRef2ObjectDB xref2ObjectTable = new GeneDBRef2ObjectDB(dir + File.separator + REF2OBJECT_FILE);

        Map<String, ConceptClass> ccMap = new TreeMap<String, ConceptClass>();
        ccMap.put("Biological Process", graph.getMetaData().getConceptClass(MetaData.BioProc));
        ccMap.put("Molecular Function", graph.getMetaData().getConceptClass(MetaData.MolFunc));
        ccMap.put("Cellular Component", graph.getMetaData().getConceptClass(MetaData.CelComp));
        ccMap.put("Trait", graph.getMetaData().getConceptClass(MetaData.TraitOnt));
        ccMap.put("Plant Structure", graph.getMetaData().getConceptClass(MetaData.POSTRUC));
        ccMap.put("Plant Growth and Development Stage", graph.getMetaData().getConceptClass(MetaData.PODevStag));
        ccMap.put("Cereal Plant Growth Stage", graph.getMetaData().getConceptClass(MetaData.GRO));

        Iterator<String> nameIt = ccMap.keySet().iterator();
        while (nameIt.hasNext()) {
            String name = nameIt.next();
            Parser.checkCreated(ccMap.get(name), name);
        }

        DataSource elementOfGRAMENE = graph.getMetaData().getDataSource(MetaData.gramene);
        Parser.checkCreated(elementOfGRAMENE, MetaData.gramene);

        EvidenceType etIMPD = graph.getMetaData().getEvidenceType(MetaData.IMPD);
        Parser.checkCreated(etIMPD, MetaData.IMPD);

        AttributeName taxIdAttr = graph.getMetaData().getAttributeName(MetaData.taxID);
        Parser.checkCreated(taxIdAttr, MetaData.taxID);

        ConceptClass ccGene = graph.getMetaData().getConceptClass(MetaData.gene);
        Parser.checkCreated(ccGene, MetaData.gene);

        RelationType hasFunction = Parser.getRelationType(MetaData.hasFunction, graph);
        RelationType hasParticipant = Parser.getRelationType(MetaData.hasParticipant, graph);
        RelationType locIn = Parser.getRelationType(MetaData.locatedIn, graph);

        Map<String, ONDEXConcept> ontologyAccessionsMap = new HashMap<String, ONDEXConcept>();

        // Logical parsing GENE into the graph:

        Pattern tabPattern = Pattern.compile("\t");

        try {
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(dir + File.separator + GENEGENE_FILE), "UTF8"));

            while (input.ready()) {
                String inputLine = input.readLine();

                String[] columns = tabPattern.split(inputLine);

                if (columns.length < 5) continue;

                // get needed fields

                int geneId = 0;
                try {
                    geneId = Integer.parseInt(columns[0]);
                } catch (NumberFormatException e) {
                    continue; //a bug in the way the line is formed
                }

                String geneName = columns[3].replaceAll("['`\"]", "").trim();

                String accession = columns[1].trim();
                int speciesId = Integer.parseInt(columns[4]);

                String description = "";
                if (columns.length >= 8) {
                    description = columns[7].trim();
                }

                int obsolete = 0;
                if (columns.length >= 12) {
                    obsolete = Integer.parseInt(columns[11]);
                }

                if (obsolete != 0) continue;

                ONDEXConcept ac = graph.getFactory().createConcept(accession, "", description, elementOfGRAMENE, ccGene, etIMPD);
                if (geneName.length() > 0) {
                    ac.createConceptName(geneName, true);
                }

                Set<Integer> assocs = ontoParser.getGeneAssocations(geneId);
                if (assocs != null) {
                    Iterator<Integer> assocsIt = assocs.iterator();
                    while (assocsIt.hasNext()) {
                        Integer assoc = assocsIt.next();
                        ONDEXConcept assocConcept = graph.getConcept(assoc);

                        if (assocConcept != null) {
                            ac.addTag(assocConcept);
                            if (assocConcept.getOfType().getId().equalsIgnoreCase(MetaData.BioProc)) {
                                graph.getFactory().createRelation(ac, assocConcept, hasParticipant, etIMPD);
                            } else if (assocConcept.getOfType().getId().equalsIgnoreCase(MetaData.CelComp)) {
                                graph.getFactory().createRelation(ac, assocConcept, locIn, etIMPD);
                            } else if (assocConcept.getOfType().getId().equalsIgnoreCase(MetaData.MolFunc)) {
                                graph.getFactory().createRelation(ac, assocConcept, hasFunction, etIMPD);
                            }
                        } else {
                            System.err.println("Id :" + assoc + " returned on gene " + geneId + " from OntologyParser is not created");
                        }
                    }
                }

                grameneGeneAccession2conceptId.put(accession, ac.getId());
                grameneGeneID2conceptId.put(geneId, ac.getId());
                ac.createConceptAccession(accession, elementOfGRAMENE, false);

                // Add TaxID
                String taxId = String.valueOf(speciesTable.getNCBITaxID(speciesId));
                if (taxId != null) ac.createAttribute(taxIdAttr, taxId, true);

                // Add DB XRefs:

                Set<GeneRef2ObjectRow> xRef2ObjectList = xref2ObjectTable.getAssocation(geneId);

                if (xRef2ObjectList != null) {

                    Iterator<GeneRef2ObjectRow> xRef2ObjectIt = xRef2ObjectList.iterator();

                    while (xRef2ObjectIt.hasNext()) {

                        GeneRef2ObjectRow xRef2Object = xRef2ObjectIt.next();

                        // System.err.println(xRef2Object.getdbXRefId()
                        // + " " + xRef2Object.getDBXRefValue());

                        DataSource accessionDataSource = xrefTable.getDataSourceForID(xRef2Object.getdbXRefId());
                        accession = xRef2Object.getDBXRefValue();

                        // System.err.println(xRef2Object.getdbXRefId()
                        // + " " + accessionCV);
                        if (accessionDataSource != null) {

                            if (accessionDataSource.getId().equals(MetaData.ec)) {
                                accession = "EC:" + accession;
                            }

                            if (ac.getConceptAccession(accession,
                                    accessionDataSource) == null) {
                                ac.createConceptAccession(accession,
                                        accessionDataSource, false);
                            }
                        }
                    }
                }
            }
            input.close();
            ontologyAccessionsMap.clear();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(grameneGeneAccession2conceptId.size() + " Genes Parsed");
    }

    /**
     * Method for getting concept id for a previously written gene
     *
     * @param geneAccession the gramene gene accession
     * @return the id for the written gene
     */
    public Integer getGrameneGeneConceptId(String geneAccession) {
        return grameneGeneAccession2conceptId.get(geneAccession);
    }

    /**
     * Method for getting concept id for a previously written gene
     *
     * @param geneId the gramene gene id
     * @return the id for the written gene
     */
    public Integer getGrameneGeneConceptId(Integer geneId) {
        return grameneGeneID2conceptId.get(geneId);
	}
}
