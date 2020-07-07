/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sourceforge.ondex.ovtk2.filter.kpmfilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.exception.type.ParsingFailedException;
/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class MaAttacher implements MAInterface {

    /**
     *
     */
    private ONDEXGraph graph;

    /**
     *
     */
    private ConceptClass ccGene;

    private StringBuilder warnings = new StringBuilder();

    public String getWarnings() {
        return warnings.toString();
    }



    /**
     *
     * @param g
     * @throws MetaDataMissingException
     */
    @Override
    public void setGraph(ONDEXGraph g) throws MetaDataMissingException {

        this.graph = g;
        if (g == null) {
            throw new IllegalArgumentException("Graph is null");
        }

        ccGene = graph.getMetaData().getConceptClass("Gene");
        if (ccGene == null) {
            throw new ConceptClassMissingException("Gene");
        }
    }



    /**
     *
     * @return
     */
    @Override
    public DataSource[] getGeneDataSources()  {

        Set<DataSource> nss = new HashSet<DataSource>();

        for (ONDEXConcept c : graph.getConceptsOfConceptClass(ccGene)) {
            for (ConceptAccession acc : c.getConceptAccessions()) {
                nss.add(acc.getElementOf());
            }
        }

        return nss.toArray(new DataSource[nss.size()]);
    }



    /**
     *
     * @param file
     * @param ns
     */
    @Override
    public void parseMaFile(File file, DataSource ns) throws ParsingFailedException {

        Map<String,ONDEXConcept> geneIndex = indexGenes(ns);

        AttributeName anExp = graph.getMetaData().getAttributeName("EXPMAP");
        if (anExp == null) {
            anExp = graph.getMetaData().getFactory()
                    .createAttributeName("EXPMAP",
                    "Gene expression data map",
                    "Map linking expression data points to expression values",
                    Map.class);
        }

         //read file
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));

            //process file header
            String header = reader.readLine();
            String[] headerCols = null;
            if (header != null && header.length() > 0) {
                headerCols = header.split("\t");
                //complain if not enough columns
                if (header.length() < 2) {
                    throw new ParsingFailedException("Incorrect file format in file "+
                            file.getAbsolutePath());
                }
            //complain if header is empty
            } else {
                throw new ParsingFailedException("Empty file header in file "+
                        file.getAbsolutePath());
            }

//            //determine namespace of gene name from first header column
//            DataSource geneNamespace = graph.getMetaData().getDataSource(headerCols[0]);

            //start reading the contents
            String line; int lineNum = 0;
            while ((line = reader.readLine()) != null) {

                lineNum++;
                if (line.length() == 0) {
                    continue;
                }
                String[] cols = line.split("\t");

                //complain if line has incorrect number of fields
                if (cols.length != headerCols.length) {
                    warnings.append("Skipping line #").append(lineNum)
                            .append("; Incorrect number of fields.\n");
                }

                String geneName = cols[0];

                //read expression values
                try {

                    Map<String,Double> values = new HashMap<String,Double>();
                    //starting from the second column, transfer values to map
                    for (int i = 1 ; i < cols.length; i++) {
                        double val = Double.parseDouble(cols[i]);
                        values.put(headerCols[i], val);
                    }

                    //retrieve concept
                    ONDEXConcept geneConcept = geneIndex.get(geneName);

                    //attach expression map
                    if (geneConcept != null) {
                        geneConcept.createAttribute(anExp, values, false);
                    } else {
                        warnings.append("No gene concept for ").append(geneName).append("\n");
                    }

                //complain if non-double values occurred
                } catch (NumberFormatException nfe) {
                    warnings.append("Skipping line #").append(lineNum)
                            .append("; Non-numeric values detected.\n");
                }
            }

        } catch (IOException ioe) {
            throw new ParsingFailedException("Unable to read file "+file.getAbsolutePath());
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                warnings.append("Unable to close file handle ")
                        .append(file.getAbsolutePath())
                        .append("\n");
            }
        }
    }

    /**
     *
     * @param ns namespace over which to index
     * @return the index map
     */
    private Map<String, ONDEXConcept> indexGenes(DataSource ns) {

        Map<String, ONDEXConcept> index = new HashMap<String, ONDEXConcept>();

        for (ONDEXConcept c : graph.getConceptsOfConceptClass(ccGene)) {
            ConceptAccession acc = null;
            for (ConceptAccession currAcc : c.getConceptAccessions()) {
                if (currAcc.getElementOf().equals(ns)) {
                    acc = currAcc;
                    break;
                }
            }
            if (acc != null) {
                System.out.println("Found gene with accession: "+acc.getAccession());
                index.put(acc.getAccession(), c);
            } else {
                warnings.append("Gene without accession!\n");
            }
        }

        return index;
    }

}
