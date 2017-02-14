/*
 * Created on 16-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg52.ko;

import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.kegg52.MetaData;
import net.sourceforge.ondex.parser.kegg52.Parser;
import net.sourceforge.ondex.parser.kegg52.sink.ConceptWriter;
import net.sourceforge.ondex.parser.kegg52.sink.Relation;
import net.sourceforge.ondex.parser.kegg52.util.Util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * @author taubertj
 */
public class KoRelationMerger {


    public void mergeAndWrite(Map<String, Set<String>> ko2Genes) {
        ConceptWriter cw = Parser.getConceptWriter();
        Util util = Parser.getUtil();

        Set<String> notFound = new HashSet<String>();

        //sort ko names
        Iterator<String> koIt = ko2Genes.keySet().iterator();
        while (koIt.hasNext()) {

            //get set of genes for ko sorted by gene name
            String kogene = koIt.next();
            Iterator<String> genesIt = ko2Genes.get(kogene).iterator();
            //go through all genes
            while (genesIt.hasNext()) {
                String gene = genesIt.next() + "_GE";
                if (!cw.conceptParserIDIsWritten(gene)) {
                    //Parser.propagateEventOccurred(
                    //		new GeneralOutputEvent(gene+" not found in written concepts", ""));
                    notFound.add(gene);
                } else {
                    //this relation always exists
                    Relation relation_gene = new Relation(gene, kogene, MetaData.RT_MEMBER_PART_OF);
                    relation_gene.setFrom_element_of(MetaData.CV_KEGG);
                    relation_gene.setTo_element_of(MetaData.CV_KEGG);
                    Parser.getUtil().writeRelation(relation_gene);

                    //m_isp for protein and KOPR
                    String protein = gene.substring(0, gene.length() - 3) + "_PR";
                    if (cw.conceptParserIDIsWritten(protein)) {
                        String koprotein = kogene.substring(0, kogene.length() - 3) + "_PR";
                        Relation relation_protein = new Relation(protein, koprotein, MetaData.RT_MEMBER_PART_OF);
                        relation_protein.setFrom_element_of(MetaData.CV_KEGG);
                        relation_protein.setTo_element_of(MetaData.CV_KEGG);
                        util.writeRelation(relation_protein);
                    }

                    //m_isp for enzyme and KOEN
                    String enzyme = gene.substring(0, gene.length() - 3) + "_EN";
                    if (cw.conceptParserIDIsWritten(enzyme)) {
                        String koenzyme = kogene.substring(0, kogene.length() - 3) + "_EN";
                        Relation relation_enzyme = new Relation(enzyme, koenzyme, MetaData.RT_MEMBER_PART_OF);
                        relation_enzyme.setFrom_element_of(MetaData.CV_KEGG);
                        relation_enzyme.setTo_element_of(MetaData.CV_KEGG);
                        util.writeRelation(relation_enzyme);
                    }
                }
            }
        }

        Parser.propagateEventOccurred(
                new GeneralOutputEvent(notFound.size() + " genes not found in written concepts", ""));
    }
}
