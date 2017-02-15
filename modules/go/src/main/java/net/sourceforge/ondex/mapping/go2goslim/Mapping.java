package net.sourceforge.ondex.mapping.go2goslim;

import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.config.LuceneRegistry;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.searchable.LuceneConcept;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.core.searchable.LuceneQueryBuilder;
import net.sourceforge.ondex.mapping.ONDEXMapping;
import org.apache.lucene.search.Query;

import java.util.*;

/**
 * An equivilent of the map2slim algorithm for Ondex
 * :http://search.cpan.org/~cmungall/go-perl/scripts/map2slim The annotation is
 * collected from child terms which are adapt for subsumed children where S1-S2
 * where S1 and S2 are sets of GO children for GOSLIM terms t1 and t2
 * respectively and t2 is a child of t1
 *
 * @author hindlem
 */
@Custodians(custodians = {"Keywan Hassani-pak"}, emails = {"keywan at users.sourceforge.net"})
public class Mapping extends ONDEXMapping
{

    private DataSource dataSourceGO;
    private DataSource dataSourceGOSLIM;
    private ConceptClass goProcess;
    private ConceptClass goFunction;
    private ConceptClass goComponant;

    private RelationType is_a;
    private RelationType part_of;

    @Override
    public void start() throws Exception {

        dataSourceGO = graph.getMetaData().getDataSource(MetaData.cvGO);
        dataSourceGOSLIM = graph.getMetaData().getDataSource(MetaData.cvGOSLIM);

        goProcess = graph.getMetaData().getConceptClass(MetaData.BioProc);
        goFunction = graph.getMetaData().getConceptClass(MetaData.MolFunc);
        goComponant = graph.getMetaData().getConceptClass(MetaData.CelComp);

        RelationType goProcessRelationAnnotation = graph.getMetaData().getRelationType(MetaData.hasParticipant);
        RelationType goFunctionRelationAnnotation = graph.getMetaData().getRelationType(MetaData.hasFunction);
        RelationType goComponantRelationAnnotation = graph.getMetaData().getRelationType(MetaData.locatedIn);

        RelationType not_goProcessRelationAnnotation = graph.getMetaData().getRelationType(MetaData.notParticipant);
        RelationType not_goFunctionRelationAnnotation = graph.getMetaData().getRelationType(MetaData.notFunction);
        RelationType not_goComponantRelationAnnotation = graph.getMetaData().getRelationType(MetaData.notLocatedIn);

        Set<RelationType> processAnnotationTypes = new HashSet<RelationType>();
        processAnnotationTypes.add(goProcessRelationAnnotation);
        processAnnotationTypes.add(not_goProcessRelationAnnotation);

        Set<RelationType> functionAnnotationTypes = new HashSet<RelationType>();
        functionAnnotationTypes.add(goFunctionRelationAnnotation);
        functionAnnotationTypes.add(not_goFunctionRelationAnnotation);

        Set<RelationType> componantAnnotationTypes = new HashSet<RelationType>();
        componantAnnotationTypes.add(goComponantRelationAnnotation);
        componantAnnotationTypes.add(not_goComponantRelationAnnotation);


        is_a = graph.getMetaData().getRelationType(MetaData.is_a);
        part_of = graph.getMetaData().getRelationType(MetaData.is_p);

        Set<ONDEXConcept> goConcepts = graph.getConceptsOfDataSource(dataSourceGO);
        Set<ONDEXConcept> goSlimConcepts = graph.getConceptsOfDataSource(dataSourceGOSLIM);

        List<RelationType> internalRelationTypes = new ArrayList<RelationType>();
        internalRelationTypes.add(is_a);
        internalRelationTypes.add(part_of);

        Map<ONDEXConcept, Set<ONDEXConcept>> processMapping = getGOSLIM2GOMapping(goConcepts,
                goSlimConcepts,
                goProcess,
                internalRelationTypes);

        Map<ONDEXConcept, Set<ONDEXConcept>> functionMapping = getGOSLIM2GOMapping(goConcepts,
                goSlimConcepts,
                goFunction,
                internalRelationTypes);

        Map<ONDEXConcept, Set<ONDEXConcept>> componantMapping = getGOSLIM2GOMapping(goConcepts,
                goSlimConcepts,
                goComponant,
                internalRelationTypes);

        transferAnnotations(processMapping, processAnnotationTypes);
        transferAnnotations(functionMapping, functionAnnotationTypes);
        transferAnnotations(componantMapping, componantAnnotationTypes);
    }

    /**
     * Transfers annotation from GO to GOSLIM in accordance with map and of the specified relation type
     *
     * @param mapping         the definition of which GOSLIM terms map to GO
     * @param annotationTypes the relation type to transfer annotation of
     */
    private void transferAnnotations(
            Map<ONDEXConcept, Set<ONDEXConcept>> mapping,
            Set<RelationType> annotationTypes) {

        for (ONDEXConcept goSlim : mapping.keySet()) {
            for (ONDEXConcept go : mapping.get(goSlim)) {
                for (ONDEXRelation relation : graph.getRelationsOfConcept(go)) {
                    if (annotationTypes.contains(relation.getOfType())) {
                        ONDEXConcept fromConcept = null;
                        ONDEXConcept toConcept = null;
                        if (relation.getKey().getFromID() == go.getId()) {
                            fromConcept = goSlim;
                            toConcept = relation.getToConcept();
                        } else {
                            fromConcept = relation.getFromConcept();
                            toConcept = goSlim;
                        }
                        graph.createRelation(
                                fromConcept,
                                toConcept,
                                relation.getOfType(),
                                relation.getEvidence());
                    }
                }
            }
        }
    }

    private Map<ONDEXConcept, Set<ONDEXConcept>> getGOSLIM2GOMapping(Set<ONDEXConcept> goConcepts,
                                                                     Collection<ONDEXConcept> goSlimConcepts, ConceptClass goOntologySubset,
                                                                     List<RelationType> internalRelationTypes) {

        Map<ONDEXConcept, Set<ONDEXConcept>> goslim2goMap = new HashMap<ONDEXConcept, Set<ONDEXConcept>>();
        Map<ONDEXConcept, Set<ONDEXConcept>> goslim2goslimMap = new HashMap<ONDEXConcept, Set<ONDEXConcept>>();

        for (ONDEXConcept goSlimConcept : goSlimConcepts) {
            List<String> goAccessions = new ArrayList<String>();

            for (ConceptAccession accession : goSlimConcept.getConceptAccessions()) {
                if (accession.getElementOf().equals(dataSourceGO)) {
                    goAccessions.add(accession.getAccession());
                }
            }
            for (String accession : goAccessions) {
                Query q = LuceneQueryBuilder.searchConceptByConceptAccessionExact(dataSourceGO,
                        accession, dataSourceGOSLIM, goOntologySubset, false);
                LuceneEnv lenv = LuceneRegistry.sid2luceneEnv.get(graph.getSID());
                for (ONDEXConcept res : lenv.searchInConcepts(q)) {
                    ONDEXConcept result = ((LuceneConcept) res).getParent();
                    if (result.getElementOf().equals(dataSourceGO)) {

                        Set<ONDEXConcept> goChildren = goslim2goMap.get(goSlimConcept);
                        if (goChildren == null) {
                            goChildren = new HashSet<ONDEXConcept>();
                            goslim2goMap.put(goSlimConcept, goChildren);
                        }
                        //add direct ancester in GO
                        goChildren.add(result);

                        Set<ONDEXConcept> possibleChildTerms = collectChildrenOfGOTerm(
                                result,
                                internalRelationTypes,
                                goOntologySubset,
                                dataSourceGO);
                        System.out.println("Poss:" + possibleChildTerms.size());
                        goChildren.addAll(possibleChildTerms);
                    }
                }
            }


            Set<ONDEXConcept> possibleChildTerms = collectChildrenOfGOTerm(
                    goSlimConcept,
                    internalRelationTypes,
                    goOntologySubset,
                    dataSourceGOSLIM);
            Set<ONDEXConcept> goSlimChildren = goslim2goslimMap.get(goSlimConcept);
            if (goSlimChildren == null) {
                goSlimChildren = new HashSet<ONDEXConcept>();
                goslim2goslimMap.put(goSlimConcept, goSlimChildren);
            }
            goSlimChildren.addAll(possibleChildTerms);
        }

        //adapt for subsumed children where S1-S2 where S1 and S2 are sets of GO children for GOSLIM terms t1 and t2 respectively and t2 is a child of t1
        for (ONDEXConcept goSlim : goslim2goMap.keySet()) {
            Set<ONDEXConcept> goChildren = goslim2goMap.get(goSlim);
            Set<ONDEXConcept> goSlimChildren = goslim2goslimMap.get(goSlim);
            for (ONDEXConcept goSlimConcept : goSlimChildren) {
                Set<ONDEXConcept> childsChildren = goslim2goMap.get(goSlimConcept);
                minusSets(goChildren, childsChildren);
            }
        }
        return goslim2goMap;
    }

    /**
     * removes from set1 all componants that are a member of set2
     *
     * @param set1
     * @param set2
     */
    private void minusSets(Set<ONDEXConcept> set1,
                           Set<ONDEXConcept> set2) {
        for (ONDEXConcept concept : set2) {
            set1.remove(concept);
        }

    }

    private Set<ONDEXConcept> collectChildrenOfGOTerm(
            ONDEXConcept parent,
            List<RelationType> internalRelationTypes, ConceptClass cc, DataSource dataSource) { // todo: cc and cv aren't used
        Set<ONDEXConcept> terms = new HashSet<ONDEXConcept>();

        for (ONDEXRelation relation : graph.getRelationsOfConcept(parent)) {
            //check its an internal relation
            if (internalRelationTypes.contains(relation.getOfType())) {
                //check its a child term
                if (relation.getKey().getToID() == parent.getId()) {
                    ONDEXConcept goChild = relation.getFromConcept();
                    terms.add(goChild);
                    terms.addAll(collectChildrenOfGOTerm(goChild,
                            internalRelationTypes,
                            cc, dataSource));
                }
            }
        }
        return terms;
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[0];
    }

    @Override
    public String getName() {
        return "go2goslim mapping";
    }

    @Override
    public String getVersion() {
        return "alpha";
    }

    @Override
    public String getId() {
        return "go2goslim";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return true;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

}
