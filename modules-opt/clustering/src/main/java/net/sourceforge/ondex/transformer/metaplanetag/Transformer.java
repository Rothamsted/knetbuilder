/**
 *
 */
package net.sourceforge.ondex.transformer.metaplanetag;

import net.sourceforge.ondex.algorithm.metaplanecrawler.MetaPlaneCrawler;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author jweile
 */
@Authors(authors = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Transformer extends ONDEXTransformer
{

    private final String depthArg = "Depth";
    private final String depthArgDesc = "The search depth determines how many metagraph planes will be part of each tag";
    private final String uniqsArg = "UniqueConceptClass";
    private final String uniqsArgDesc = "The ID of a ConceptClass that will be allowed only one instance per plane.";
    private final String nospawnArg = "ConceptClassSpawnExclusion";
    private final String nospawnArgDesc = "The ID of a ConceptClass that will not be allowed to spawn new planes.";
    private final String nosearchArg = "ConceptClassSearchExclusion";
    private final String nosearchArgDesc = "The ID of a ConceptClass that will not be allowed to be searched for further connections.";
    private final String maxPlanesArg = "MaxPlanes";
    private final String maxPlanesArgDesc = "Maximal allowed number of planes.";
    private final String rootArg = "RootConceptClass";
    private final String rootArgDesc = "The ID of the ConceptClass for which to create the contexts.";

    private ArgumentDefinition<?>[] argdefs = new ArgumentDefinition<?>[]{
            new StringArgumentDefinition(rootArg, rootArgDesc, true, null, false),
            new RangeArgumentDefinition<Integer>(depthArg, depthArgDesc, true, 1, 1, 10, Integer.class),
            new RangeArgumentDefinition<Integer>(maxPlanesArg, maxPlanesArgDesc, false, Integer.MAX_VALUE, 1, Integer.MAX_VALUE, Integer.class),
            new StringArgumentDefinition(nospawnArg, nospawnArgDesc, false, null, true),
            new StringArgumentDefinition(nosearchArg, nosearchArgDesc, false, null, true),
            new StringArgumentDefinition(uniqsArg, uniqsArgDesc, false, null, true)};

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getArgumentDefinitions()
     */
    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return argdefs;
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getName()
     */
    @Override
    public String getName() {
        return "MetaGraph-plane-based context transformer";
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getVersion()
     */
    @Override
    public String getVersion() {
        return "29.01.2009";
    }

    @Override
    public String getId() {
        return "metaplanetag";
    }


    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#requiresIndexedGraph()
     */
    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#requiresValidators()
     */
    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#start()
     */
    @Override
    public void start() throws Exception {
        MetaPlaneCrawler mpc = new MetaPlaneCrawler(graph);

        if (args.getObjectValueList(uniqsArg) != null) {
            for (Object u : args.getObjectValueList(uniqsArg)) {
                String uniq = (String) u;
                ConceptClass cc = graph.getMetaData().getConceptClass(uniq);
                if (cc != null)
                    mpc.addUniqueConceptClass(cc);
                else
                    System.err.println("ConceptClass " + uniq + " unknown. Ignored!");
            }
        }
        if (args.getObjectValueList(nospawnArg) != null) {
            for (Object n : args.getObjectValueList(nospawnArg)) {
                String nospawn = (String) n;
                ConceptClass cc = graph.getMetaData().getConceptClass(nospawn);
                if (cc != null)
                    mpc.addConceptClassSpawnExclusion(cc);
                else
                    System.err.println("ConceptClass " + nospawn + " unknown. Ignored!");
            }
        }
        if (args.getObjectValueList(nosearchArg) != null) {
            for (Object n : args.getObjectValueList(nosearchArg)) {
                String nosearch = (String) n;
                ConceptClass cc = graph.getMetaData().getConceptClass(nosearch);
                if (cc != null)
                    mpc.addConceptClassSearchExclusion(cc);
                else
                    System.err.println("ConceptClass " + nosearch + " unknown. Ignored!");
            }
        }

        Integer maxPlanes = (Integer) args.getUniqueValue(maxPlanesArg);
        if (maxPlanes == null)
            mpc.setMaximalNumberOfPlanes(Integer.MAX_VALUE);
        else
            mpc.setMaximalNumberOfPlanes(maxPlanes);

        int depth = (Integer) args.getUniqueValue(depthArg);

        String rootCcId = (String) args.getUniqueValue(rootArg);
        ConceptClass rootCC = requireConceptClass(rootCcId);

        for (ONDEXConcept currContext : graph.getConceptsOfConceptClass(rootCC)) {

            String searchlist = "YDL220C,YOL138C,YER177W,YFL023W,YBR274W,YJR109C,YIL036W,YPL194W,YNL080C,YOR033C,YCR034W,YMR058W,YLR192C,YOL095C,YMR080C,YHR077C,YOR209C,YPL052W,YDL232W,YOR368W,YER173W,YDR217C,YLR039C,YCR028C-A,YNL069C,YDR389W,YDR143C,YER120W,YOR035C,YDL033C,YOR327C,YLR313C,YLR372W,YBR126C,YBR082C,YGR072W,YOR089C,YAL002W";
            HashSet<String> searchitems = new HashSet<String>();
            searchitems.addAll(Arrays.asList(searchlist.split(",")));

            boolean found = false;
            for (ConceptAccession ca : currContext.getConceptAccessions()) {
                if (searchitems.contains(ca.getAccession())) {
                    found = true;
                    break;
                }
            }

            if (found) {
                mpc.crawl(currContext, depth);
                Collection<ONDEXConcept> cs = mpc.getConceptSet();
                Collection<ONDEXRelation> rs = mpc.getRelationSet();
                for (ONDEXConcept c : cs) {
                    c.addTag(currContext);
                }
                for (ONDEXRelation r : rs) {
                    r.addTag(currContext);
                }
            }
        }
    }

}
