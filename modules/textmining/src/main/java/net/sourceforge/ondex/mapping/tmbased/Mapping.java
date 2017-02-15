package net.sourceforge.ondex.mapping.tmbased;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.config.LuceneRegistry;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.searchable.LuceneConcept;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.core.searchable.LuceneQueryBuilder;
import net.sourceforge.ondex.core.searchable.ScoredHits;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.mapping.ONDEXMapping;
import net.sourceforge.ondex.mapping.tmbased.args.ArgumentNames;

import org.apache.lucene.search.Query;


/**
 * Text mining based mapping
 *
 * @author keywan
 */
@Status(description = "Text mining to map concepts to publications. Assigns tf-idf scores and evidence text to relations. (Hassani-Pak et al, JIB 2010). Tested June 2011 (Keywan Hassani-Pak)", status = StatusType.STABLE)
@Authors(authors = {"Keywan Hassani-Pak"}, emails = {"keywan at users.sourceforge.net"})
@Custodians(custodians = {"Keywan Hassani-pak"}, emails = {"keywan at users.sourceforge.net"})
public class Mapping extends ONDEXMapping
{

    //contains all parsed publications
    private Map<Integer, PublicationMapping> parsed;

    //search strategies to query the lucene index
    private static final String AND_SEARCH = "and";
    private static final String EXACT_SEARCH = "exact";
    private static final String FUZZY_SEARCH = "fuzzy";
    private static final String PROXIMITY_SEARCH = "proximity";


    public Mapping() {
        parsed = new HashMap<Integer, PublicationMapping>();
    }

    public String getId() {
        return "tmbased";
    }

    public String getName() {
        return "Text Mining based mapping";
    }

    public String getVersion() {
        return "05.08.2008";
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition[]{
                new StringArgumentDefinition(ArgumentNames.CONCEPTCLASS_ARG, ArgumentNames.CONCEPTCLASS_DESC, true, null, true),
                new BooleanArgumentDefinition(ArgumentNames.PREFERRED_NAMES_ARG, ArgumentNames.PREFERRED_NAMES_ARG_DESC, false, false),
                new BooleanArgumentDefinition(ArgumentNames.USE_FULLTEXT_ARG, ArgumentNames.USE_FULLTEXT_ARG_DESC, false, false),
                new StringArgumentDefinition(ArgumentNames.SEARCH_STRATEGY_ARG, ArgumentNames.SEARCH_STRATEGY_DESC, true, "exact", false),
                new StringArgumentDefinition(ArgumentNames.FILTER_ARG, ArgumentNames.FILTER_DESC, false, null, false),
        };
    }


    public boolean requiresIndexedGraph() {
        return true;
    }


    @Override
    public void start() throws InvalidPluginArgumentException {
        AttributeName abstractAN = graph.getMetaData().getAttributeName(MetaData.ATT_NAME_ABSTRACT);
        if (abstractAN == null) {
            AttributeNameMissingEvent so = new AttributeNameMissingEvent(MetaData.ATT_NAME_ABSTRACT + " missing.", Mapping.getCurrentMethodName());
            fireEventOccurred(so);
        }

        AttributeName headerAN = graph.getMetaData().getAttributeName(MetaData.ATT_NAME_ABSTRACT_HEADER);
        if (headerAN == null) {
            AttributeNameMissingEvent so = new AttributeNameMissingEvent(MetaData.ATT_NAME_ABSTRACT_HEADER + " missing.", Mapping.getCurrentMethodName());
            fireEventOccurred(so);
        }
        
        AttributeName attFullText = graph.getMetaData().getAttributeName(MetaData.ATT_NAME_FULLTEXT);
        if (attFullText == null) {
            AttributeNameMissingEvent so = new AttributeNameMissingEvent(MetaData.ATT_NAME_FULLTEXT + " missing.", Mapping.getCurrentMethodName());
            fireEventOccurred(so);
        }
        
        List<String> ccs = (List<String>) args.getObjectValueList(ArgumentNames.CONCEPTCLASS_ARG);
        String searchStrategy = (String) args.getUniqueValue(ArgumentNames.SEARCH_STRATEGY_ARG);
        boolean useOnlyPreferredNames = (Boolean) args.getUniqueValue(ArgumentNames.PREFERRED_NAMES_ARG);
        boolean useFullText = (Boolean) args.getUniqueValue(ArgumentNames.USE_FULLTEXT_ARG);
        
        int pubsWithHeader = graph.getConceptsOfAttributeName(headerAN).size();
        int pubsWithAbstract = graph.getConceptsOfAttributeName(abstractAN).size();
        int pubsWithFullText = 0;
//        if(graph.getConceptsOfAttributeName(attFullText) != null)
        	pubsWithFullText = graph.getConceptsOfAttributeName(attFullText).size();
        
        AttributeName[] fields;

        if(useFullText && pubsWithFullText > 0){
        	fireEventOccurred(new GeneralOutputEvent("Search " + searchStrategy + " in " + pubsWithHeader + " publications: " + pubsWithAbstract + " with abstract, "+pubsWithFullText+" with full text.", Mapping.getCurrentMethodName()));
        	fields = new AttributeName[3];
        	fields[0] = headerAN;
        	fields[1] = abstractAN;
        	fields[2] = attFullText;
        }else{
        	fireEventOccurred(new GeneralOutputEvent("Search " + searchStrategy + " in " + pubsWithHeader + " publications, of which " + pubsWithAbstract + " have an abstract...", Mapping.getCurrentMethodName()));
        	fields = new AttributeName[2];
        	fields[0] = headerAN;
        	fields[1] = abstractAN;
        }
        
        //concept class
        for (String ccName : ccs) {
            ConceptClass cc = graph.getMetaData().getConceptClass(ccName);
            Set<ONDEXConcept> ccConcepts = graph.getConceptsOfConceptClass(cc);

            if (ccConcepts == null) {
                continue;
            }
            fireEventOccurred(new GeneralOutputEvent("look for " + ccConcepts.size() + " " + cc.getId() + " concepts", Mapping.getCurrentMethodName()));
            int ignoredNames = 0;
            //concepts
            for (ONDEXConcept c : ccConcepts) {
                //synonyms
                for (ConceptName name : c.getConceptNames()) {
                    if (useOnlyPreferredNames && !name.isPreferred()) {
                        ignoredNames++;
                        continue;
                    }
                    if (!validName(name.getName())) {
                        ignoredNames++;
                        continue;
                    }

                    String query = TextProcessing.stripText(name.getName());

                    //search in all indexed pubs for pattern
                    ScoredHits<ONDEXConcept> abstractsContainingTerm;

                    //select search strategy
                    LuceneEnv lenv = LuceneRegistry.sid2luceneEnv.get(graph.getSID());
                    if (searchStrategy.toLowerCase().equals(EXACT_SEARCH.toLowerCase())) {
                        query = "\"" + query + "\"";
                        Query luceneQuery = LuceneQueryBuilder.searchConceptByConceptAttribute(fields, query, LuceneEnv.DEFAULTANALYZER, LuceneQueryBuilder.DEFAULT_ATTR_BOOSTS);
                        abstractsContainingTerm = lenv.scoredSearchInConcepts(luceneQuery);
                    } else if (searchStrategy.toLowerCase().equals(FUZZY_SEARCH.toLowerCase())) {
                        //TODO: check NormValue
                        float normValue = getFuzzyFactor(query, 13);
                        Query luceneQuery = LuceneQueryBuilder.searchConceptByConceptAttributeFuzzy(abstractAN, query, normValue);
                        abstractsContainingTerm = lenv.scoredSearchInConcepts(luceneQuery);
                    } else if (searchStrategy.toLowerCase().equals(AND_SEARCH.toLowerCase())) {
                        query = query.replaceAll(" ", " AND ");
                        if (query.equals("")) continue;
                        Query luceneQuery = LuceneQueryBuilder.searchConceptByConceptAttribute(fields, query, LuceneEnv.DEFAULTANALYZER, LuceneQueryBuilder.DEFAULT_ATTR_BOOSTS);
                        abstractsContainingTerm = lenv.scoredSearchInConcepts(luceneQuery);
                    } else if (searchStrategy.toLowerCase().equals(PROXIMITY_SEARCH.toLowerCase())) {
                        query = "\"" + query + "\"~10";
                        Query luceneQuery = LuceneQueryBuilder.searchConceptByConceptAttribute(fields, query, LuceneEnv.DEFAULTANALYZER, LuceneQueryBuilder.DEFAULT_ATTR_BOOSTS);
                        abstractsContainingTerm = lenv.scoredSearchInConcepts(luceneQuery);
                    } else {
                        // search exact if search method is unknown
                        Query luceneQuery = LuceneQueryBuilder.searchConceptByConceptAttribute(fields, query, LuceneEnv.DEFAULTANALYZER, LuceneQueryBuilder.DEFAULT_ATTR_BOOSTS);
                        abstractsContainingTerm = lenv.scoredSearchInConcepts(luceneQuery);
                    }

                    if (abstractsContainingTerm.getOndexHits().size() == 0) {
                        continue;
                    }

                    Map<Integer, Float> scores = abstractsContainingTerm.getScoresForHits();

                    //frequencies of every each search word
                    //int[] freqs = lenv.getFrequenceyOfWordInConceptGDS(abstractAN, query.split(" "));

                    //iterate through all pubs that contain search pattern
                    for (ONDEXConcept pub : abstractsContainingTerm.getOndexHits()) {
                        if (pub instanceof LuceneConcept)
                            pub = ((LuceneConcept) pub).getParent();

                        PublicationMapping pubMap;
                        if (!parsed.containsKey(pub.getId())) {
                            pubMap = new PublicationMapping(pub.getId());
                            parsed.put(pub.getId(), pubMap);
                        }
                        pubMap = parsed.get(pub.getId());

                        Hit hit = pubMap.getHit(c.getOfType().getId(), c.getId());
                        if (hit == null) {
                            hit = new Hit(c.getId());
                            pubMap.addHit(c.getOfType().getId(), hit);
                        }

                        Double score = (double) scores.get(pub.getId());
                        
                        String headerText = "";
                        if(pub.getAttribute(headerAN) != null){
	                        headerText = (String) pub.getAttribute(headerAN).getValue();
	                        
	                        if (!headerText.endsWith(".")) {
	                            headerText = headerText + ".";
	                        }
                        
                        }
                        String publicationText = "";
                        if (pub.getAttribute(abstractAN) != null) {
                            publicationText = (String) pub.getAttribute(abstractAN).getValue();
                        }
                        
                        String fullText = "";
                        if (pub.getAttribute(attFullText) != null) {
                        	fullText = (String) pub.getAttribute(attFullText).getValue();
                        }
                        
                        String text = headerText + " " + publicationText + " " + fullText;
                        HashSet<String> evidence = TextProcessing.searchEvidenceSentence(text, query);

                        Occurrence occ = new Occurrence(query, score);
                        // occ.setEvidence(evidence);

                        hit.addOccurrence(occ);
                        hit.addEvidence(evidence);

                    }
                }
            }
//			System.out.println("Number of unvalid conceptnames: "+ignoredNames);
        }

        createTextMiningMappings(graph);

    }


    /*
      * filter, write, make statistics for text mining based mappings
      */

    private void createTextMiningMappings(ONDEXGraph graph) throws InvalidPluginArgumentException {

        Collection<PublicationMapping> pubMaps = parsed.values();

        if (args.getUniqueValue(ArgumentNames.FILTER_ARG) != null) {
            fireEventOccurred(new GeneralOutputEvent("FILTER TEXT MINING MAPPINGS...", Mapping.getCurrentMethodName()));
            MappingFilter filter = new MappingFilter(graph, args);
            filter.filterMappings(pubMaps);
        }

        fireEventOccurred(new GeneralOutputEvent("WRITE TEXT MINING MAPPINGS...", Mapping.getCurrentMethodName()));
        MappingWriter writer = new MappingWriter(graph);
        writer.addAllHitsToONDEXGraph(pubMaps);


    }

    private boolean validName(String name) {
        name = TextProcessing.stripText(name);
        name = TextProcessing.removeStopWords(name);

        if (name.length() <= 2) {
            return false;
        } else if (name.toLowerCase().equals("response to")) {
            return false;
        } else if (name.toLowerCase().equals("has")) {
            return false;
        } else if (name.contains("GO:")) {
            return false;
        } else if (name.toLowerCase().contains("predicted protein")) {
            return false;
        } else if (name.toLowerCase().equals("can")) {
            return false;
        } else {
//			if(!oldName.equals(name))
//				System.out.println(oldName+" --> "+name);
            return true;

        }
    }

    private float getFuzzyFactor(String word, float n) {
        float fuzzyFactor = (float) (1 - Math.exp(word.length() - n));
        if (fuzzyFactor <= 0) fuzzyFactor = 0;
        if (fuzzyFactor >= 1) fuzzyFactor = 1;
        return fuzzyFactor;
    }

    /**
     * Convenience method for outputing the current method name in a dynamic way
     *
     * @return the calling method name
     */
    public static String getCurrentMethodName() {
        Exception e = new Exception();
        StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
        String name = trace.getMethodName();
        String className = trace.getClassName();
        int line = trace.getLineNumber();
        return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line + "]";
    }

    public String[] requiresValidators() {
        return new String[0];
    }
}
