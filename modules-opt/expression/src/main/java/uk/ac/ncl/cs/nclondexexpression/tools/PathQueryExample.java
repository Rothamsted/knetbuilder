/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.nclondexexpression.tools;

import java.util.Map;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.exception.type.ParsingFailedException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author jweile
 */
public class PathQueryExample {

    public void query(ONDEXGraph graph) {
        try {

            AttributeName anExpression = graph.getMetaData().getAttributeName("EXPMAP");

            DefaultPathTemplates templates = new DefaultPathTemplates(graph);

            /*
             * Set<String> keys = templates.getTemplateKeys()
             * contains the following keys:
             *    homologue
             *    metabolic_path
             *    regulation
             *    physical_interaction
             *    genetic_interaction
             */


            PathTemplate template = templates.getTemplate("physical_interaction");

            //initialize pathsearcher with the template
            PathSearcher searcher = new PathSearcher(template, graph);
            //start the search
            searcher.search();

            //iterate over resulting paths
            Path path;
            while ((path = searcher.nextPath()) != null) {
                //retrive head and tail of path
                ONDEXConcept gene1 = path.head();
                ONDEXConcept gene2 = path.tail();

                Map<String,Double> expressionValues1 = (Map<String,Double>) gene1.getAttribute(anExpression).getValue();
                Map<String,Double> expressionValues2 = (Map<String,Double>) gene2.getAttribute(anExpression).getValue();

                //do something with the genes and the expression values here.
            }



        } catch (ParsingFailedException ex) {
            Logger.getLogger(PathQueryExample.class).log(Level.ERROR, ex.getMessage(), ex);
        }


    }
}
