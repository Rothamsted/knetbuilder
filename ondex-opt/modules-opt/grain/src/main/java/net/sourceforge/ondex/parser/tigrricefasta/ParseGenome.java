package net.sourceforge.ondex.parser.tigrricefasta;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;

public class ParseGenome {

    private RelationType rtSetEncodeBy;
    private EvidenceType etIMPD;

    public void parse(String[] files, ONDEXGraph graph) {

        Map<String, ONDEXConcept> genesMap = null;
        Map<String, ONDEXConcept> proteinsMap = null;

        for (String fileName : files) {

            if (fileName.endsWith(".seq") || fileName.endsWith(".cds")) {

                //	Parses the Gene sequences:
                ParseGeneSequences parseGeneSeqs = new ParseGeneSequences();
                if (genesMap == null) {
                    genesMap = parseGeneSeqs.parse(graph, fileName);
                } else {
                    genesMap.putAll(parseGeneSeqs.parse(graph, fileName));
                }
            } else if (fileName.endsWith(".pep")) {

                //	Parses the Proteins sequences:
                ParseProteinSequences parseProteinSeqs = new ParseProteinSequences();
                if (proteinsMap == null) {
                    proteinsMap = parseProteinSeqs.parse(graph, fileName);
                } else {
                    proteinsMap.putAll(parseProteinSeqs.parse(graph, fileName));
                }
            } else {
                System.out.println("File extension unknown, not able to parse file" + "(" + fileName + "). (use: seq/cds = NA & pep = AA))");
            }
        }

        if (proteinsMap != null && genesMap != null) {

            //Genes and proteins should contain the same accession if they do then map them:
            Set<String> keys = new HashSet<String>();
            keys.addAll(genesMap.keySet());
            keys.addAll(proteinsMap.keySet());

            if (rtSetEncodeBy == null) rtSetEncodeBy = graph.getMetaData().getRelationType(MetaData.encodedBy);
            if (etIMPD == null) etIMPD = graph.getMetaData().getEvidenceType(MetaData.IMPD);

            Iterator<String> keysIt = keys.iterator();

            while (keysIt.hasNext()) {

                String accession = keysIt.next();

                if (genesMap.containsKey(accession) && proteinsMap.containsKey(accession)) {
                    //map the gene to protein:
                    graph.getFactory().createRelation(proteinsMap.get(accession), genesMap.get(accession), rtSetEncodeBy, etIMPD);
                }

            }
        }
    }

    public static String chompVersion(String acc) {
        return acc.substring(0, acc.indexOf("."));
    }

}
