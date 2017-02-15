package net.sourceforge.ondex.parser.medline;

import java.io.IOException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.medline.args.ArgumentNames;
import org.apache.log4j.Level;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

class MyFilter implements FileFilter {

    String prefix = "";

    public MyFilter(String prefix) {
        this.prefix = prefix;
    }

    public boolean accept(File f) {
        if (f.getName().startsWith(this.prefix)) {
            return true;
        } else return false;
    }
}

public class Extractor {

    ImportSession isession = new ImportSession();

    private void checkArguments(ONDEXGraph graph, ONDEXPluginArguments pa) throws InvalidPluginArgumentException {

        String inputDirName = (String) pa.getUniqueValue(FileArgumentDefinition.INPUT_DIR);
        if (inputDirName != null && !inputDirName.isEmpty()){
            File inputDir = new File((String) pa.getUniqueValue(FileArgumentDefinition.INPUT_DIR));
    	    if (inputDir != null) {
               isession.setImportDir(inputDir.getAbsolutePath());
            }
        }

        String compression = (String) pa.getUniqueValue(ArgumentNames.COMPRESSION_ARG);

        if (compression != null) {
            isession.setCompression(compression);
        }

        String prefix = (String) pa.getUniqueValue(ArgumentNames.PREFIX_ARG);

        if (prefix != null) {
            isession.setFilePrefix(prefix);
        }

        List<Integer> idValues = (List<Integer>) pa.getObjectValueList(ArgumentNames.IDLIST_ARG);

        if (idValues != null && idValues.size() > 0) {
            HashSet<String> pmids = new HashSet<String>();
            for (Integer pmid : idValues) {
                pmids.add(pmid.toString());
            }

            isession.setImportList(pmids);
        }

        Boolean isImpOnlyCitedPub = (Boolean) pa.getUniqueValue(ArgumentNames.IMP_ONLY_CITED_PUB_ARG);

        if (isImpOnlyCitedPub == null) isImpOnlyCitedPub = false;

        if (isImpOnlyCitedPub) {
            ConceptClass ccPub = graph.getMetaData().getConceptClass(MetaData.CC_PUBLICATION);
            HashSet<String> pmids = new HashSet<String>();
            for (ONDEXConcept c : graph.getConceptsOfConceptClass(ccPub)) {
                for (ConceptAccession accession : c.getConceptAccessions()) {
                    if (accession.getElementOf().getId().equalsIgnoreCase(MetaData.CV_NLM)) {
                        pmids.add(accession.getAccession());
                    }
                }
            }
            System.out.println(pmids.size() + " Publications with NLM accessions found in the graph.");
            isession.setImportList(pmids);
        }


        List<String> pubmedFiles = (List<String>) pa.getObjectValueList(ArgumentNames.PUBMEDFILE_ARG);
        List<Integer> xmlFileValues = (List<Integer>) pa.getObjectValueList(ArgumentNames.XMLFILES_ARG);

        Integer upperBounds = (Integer) pa.getUniqueValue(ArgumentNames.UPPERXMLBOUNDARY_ARG);
        Integer lowerBounds = (Integer) pa.getUniqueValue(ArgumentNames.LOWERXMLBOUNDARY_ARG);

        //this is for PubMed export files
        if (pubmedFiles != null) {
            HashSet<String> fileNames = new HashSet<String>();
            for (String pubmedFile : pubmedFiles) {
                fileNames.add(pubmedFile);
            }
            isession.setFileNames(fileNames);
        } else if (xmlFileValues == null) {
            //read all xml files
            MyFilter myFilter = new MyFilter(isession.getFilePrefix());
            //find out, whether boundary was set
            if (upperBounds != null) {

                isession.setupperxmlBoundary(upperBounds);

                GeneralOutputEvent so1 = new GeneralOutputEvent("Set upper XML Boundary to " + upperBounds, Parser.getCurrentMethodName());
                so1.setLog4jLevel(Level.INFO);
                Parser.propagateEventOccurred(so1);
            }

            if (lowerBounds != null) {

                isession.setlowerxmlBoundary(lowerBounds);

                GeneralOutputEvent so1 = new GeneralOutputEvent("Set lower XML Boundary to " + lowerBounds, Parser.getCurrentMethodName());
                so1.setLog4jLevel(Level.INFO);
                Parser.propagateEventOccurred(so1);
            }

            if (lowerBounds == null && upperBounds != null) {

                HashSet<Integer> ints = new HashSet<Integer>();

                for (int i = 0; i <= isession.getupperxmlBoundary(); i++) {
                    ints.add(i);
                }
                isession.setXmlNumbers(ints);
            }

            if (lowerBounds != null && upperBounds != null) {

                HashSet<Integer> ints = new HashSet<Integer>();

                for (int i = isession.getlowerxmlBoundary(); i <= isession.getupperxmlBoundary(); i++) {
                    ints.add(i);
                }
                isession.setXmlNumbers(ints);
            }

            if (lowerBounds == null && upperBounds == null) {

                //no boundary given, so take all
                HashSet<Integer> ints = new HashSet<Integer>();

                File dir = new File(isession.getImportDir());
                File[] files = dir.listFiles(myFilter);
                Arrays.sort(files);

                if (files == null || files.length == 0) {
                    GeneralOutputEvent so1 = new GeneralOutputEvent("No medline files found at " + isession.getImportDir(), Parser.getCurrentMethodName());
                    so1.setLog4jLevel(Level.ERROR);
                    Parser.propagateEventOccurred(so1);
                }

                for (File file : files) {
                    if (!file.getName().endsWith(isession.getEnding())) continue;
                    String fileName = file.getName();
                    String cutNumber = fileName.substring(
                            fileName.indexOf(isession.getFilePrefix()) + isession.getFilePrefix().length(),
                            fileName.indexOf(isession.getEnding()));
                    int number = Integer.parseInt(cutNumber);
                    ints.add(number);
                }

                isession.setXmlNumbers(ints);
            }

        } else {
            //if Array -> list of numbers
            if (xmlFileValues != null) {

                HashSet<Integer> xmlFiles = new HashSet<Integer>();
                for (Integer xmlFileValue : xmlFileValues)
                    xmlFiles.add(xmlFileValue);

                isession.setXmlNumbers(xmlFiles);

            }
        }
    }

    //readXML
    public void parse(ONDEXPluginArguments pa, ONDEXGraph graph, ONDEXParser parser) throws XMLStreamException, 
            InvalidPluginArgumentException, IOException, MetaDataMissingException {

        this.checkArguments(graph, pa);

        new MEDLINEParser().parse(isession, graph, pa);

    }

}
