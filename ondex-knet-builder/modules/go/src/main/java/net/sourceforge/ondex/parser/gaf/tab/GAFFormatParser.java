package net.sourceforge.ondex.parser.gaf.tab;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import net.sourceforge.ondex.parser.gaf.sink.AnnotationLine;
import net.sourceforge.ondex.parser.gaf.transformer.GAFTransformer;

/**
 * Parser for the generic GOA Format
 * <p/>
 * Documentation:
 * http://www.geneontology.org/GO.annotation.shtml#file
 *
 * @author hoekmanb
 */

public class GAFFormatParser {
    /**
     * Generic parser for the GO-Annotation format:
     *
     * @param filename
     * @return ObjectOpenHashSet<AnnotationLine> - Collection of all valid lines
     * @throws Exception
     */
    public void getFileContent(String filename, GAFTransformer gafParser) throws Exception {
        BufferedReader input;

        if (filename.endsWith(".gz")) {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(filename));
            input = new BufferedReader(new InputStreamReader(gzip));
        } else {
            input = new BufferedReader(new FileReader(filename));
        }

        String inputLine = input.readLine();
        int count = 0;
        int skip = 0;
        while (inputLine != null) {
            
            if (!inputLine.startsWith("!")) {
                AnnotationLine temp = processLine(inputLine);

                if (temp != null) {
                    gafParser.parseGAFLine(temp);
                    count++;
                    if ((count % 10000) == 0) {
                        System.out.println("parsed " + count + " annotations");
                    }
                }else{
                	skip++;
                }
            }

            inputLine = input.readLine();
        }
        
        System.out.println(filename + ": PARSED ANNOTATIONS: "+count+"; SKIPPED: "+skip+" (lines not in GAF format)");
    }

    /**
     * Parses one line into a GOALine object
     *
     * @param inputLine
     * @return
     */
    private AnnotationLine processLine(String inputLine) {
        AnnotationLine result = new AnnotationLine();

        String[] fields = inputLine.split("\t"); //Tab delimited

        //15 fields in GAF 1.0; 17 fields in GAF 2.0 (FIXME: 2 last ones are ignored right now)
        if (fields.length == 15 || fields.length == 17) {
            fields[6] = fields[6].trim().toUpperCase();
            fields[8] = fields[8].trim().toUpperCase();
            
            if (fields[12].length() >= 1) {
                String[] taxons = fields[12].trim().split("\\|");
                for (String taxon : taxons)
                    result.addTaxon(taxon.trim());
            }

            String[] synonyms = fields[10].trim().split("\\|");
            for (String synonym : synonyms)
                result.addDBObjectSynonym(synonym.trim());

            String[] dbRefs = fields[5].trim().split("\\|");
            for (String dbRef : dbRefs)
                result.addDBReference(dbRef.trim());


            //Check it there is a sencond evidencetype and if it is valid
            if (fields[7].trim().length() >= 1) {
                result.setWithFrom(fields[7].trim());
            }

            result.setDatabase(fields[0].trim());
            result.setDBObjectID(fields[1].trim());
            result.setDBObjectSymbol(fields[2].trim());
            result.setQualifier(fields[3].trim());
            result.setGOID(fields[4].trim());

            result.setEvidenceCode(fields[6].trim());

            result.setAspect(fields[8].trim());

            if (fields[9].length() >= 1) {
                result.setDBObjectName(fields[9].trim());
            }
            
            String dbObjType = fields[11].toLowerCase().trim();
            if(!dbObjType.equals(""))
            	result.setDBObjectType(dbObjType);
            else{
            	System.err.println("DbObjectType is empty and is set to Thing, line: "+inputLine);
            	result.setDBObjectType("Thing");
            	
            }	

            result.setDate(fields[13].trim());
            result.setAssignedBy(fields[14].trim());

            return result;
        } else {
        	System.out.println("Line has not 15 or 17 columns, make sure it is in the GAF 1.0 or GAF 2.0 format");
            return null;
        }
    }
}
