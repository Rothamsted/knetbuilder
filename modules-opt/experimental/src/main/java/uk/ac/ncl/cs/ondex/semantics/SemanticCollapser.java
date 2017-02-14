/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.ondex.semantics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.exception.type.PluginException;
import net.sourceforge.ondex.transformer.ONDEXTransformer;
import uk.ac.ncl.cs.ondex.semantics.tools.Path;
import uk.ac.ncl.cs.ondex.semantics.tools.PathSearcher;
import uk.ac.ncl.cs.ondex.semantics.tools.PathTemplate;

/**
 *
 * @author jweile
 */
public class SemanticCollapser extends ONDEXTransformer {

    public static final String MOTIF_ARG = "Motif";
    public static final String MOTIF_ARG_DESC = "Motif descriptor: Circular " +
            "statement of interchanging concept classes and relation types, " +
            "beginning and ending with the same concept class.";

    public static final String RELATION_TYPE_ARG = "RelationType";
    public static final String RELATION_TYPE_ARG_DESC = "Relation type to be used" +
            "for collapsing motifs.";

    public static final String ANNOTATION_MODE_ARG = "AnnotationMode";
    public static final String ANNOTATION_MODE_ARG_DESC = "When enabled, new relations are" +
            "annotated with the collapsed pathway the represent. Otherwise the pathway is deleted.";

    private String motif;
    private RelationType rt;
    private boolean annotationMode;

    private EvidenceType etTrans;
    private ConceptClass ccContext;
    private DataSource dataSourceUC;
    private AttributeName anCollapsed;

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[] {
            new StringArgumentDefinition(MOTIF_ARG, MOTIF_ARG_DESC, true, "", false),
            new StringArgumentDefinition(RELATION_TYPE_ARG, RELATION_TYPE_ARG_DESC, true, "r", false),
            new BooleanArgumentDefinition(ANNOTATION_MODE_ARG, ANNOTATION_MODE_ARG_DESC, false, true)
        };
    }

    private void fetchArgs() throws PluginException {
        motif = (String) getArguments().getUniqueValue(MOTIF_ARG);

        String rtId = (String) getArguments().getUniqueValue(RELATION_TYPE_ARG);
        rt = requireRelationType(rtId);

        Boolean aModeRaw = (Boolean) getArguments().getUniqueValue(ANNOTATION_MODE_ARG);
        annotationMode = aModeRaw == null ? true : aModeRaw;
    }

    private void initMetadata() throws MetaDataMissingException {
        etTrans = requireEvidenceType("InferredByTransformation");

        ConceptClass ccThing = requireConceptClass("Thing");
        ccContext = graph.getMetaData().getFactory().createConceptClass(rt.getId().toUpperCase()+"_CONTEXT",
                "Context of "+rt.getFullname()+" relation", "", ccThing);

        dataSourceUC = requireDataSource("UC");
        anCollapsed = graph.getMetaData().getFactory()
                .createAttributeName("collapsedPath",
                                    "Collapsed Path",
                                    "Set linking the members of the path "
                                    + "represented by the carrier of this"
                                    + " attriute.",
                                    Set.class);
    }

    @Override
    public void start() throws Exception {

        fetchArgs();
        initMetadata();

        PathTemplate template = new PathTemplate(motif, graph);

        PathSearcher searcher = new PathSearcher(template, graph);
        searcher.search();

//        Map<String,ONDEXConcept> contexts = new HashMap<String, ONDEXConcept>();

        Set<ONDEXEntity> toDelete = new HashSet<ONDEXEntity>();

        //iterate over resulting paths
        Path path; int pathId = 0;
        while ((path = searcher.nextPath()) != null) {
            pathId++;

            ONDEXConcept from = path.head();
            ONDEXConcept to = path.tail();

            if (annotationMode) {

                //find or create edge
                ONDEXRelation r = graph.getRelation(from, to, rt);
                if (r == null) {
                    r = graph.getFactory().createRelation(from, to, rt, etTrans);
                }

                //find or create path attribute
                Attribute aColl = r.getAttribute(anCollapsed);
                Set<ONDEXEntity> pathSet = null;
                if (aColl == null) {
                    pathSet = new HashSet<ONDEXEntity>();
                    aColl = r.createAttribute(anCollapsed, pathSet, false);
                } else {
                    pathSet = (Set<ONDEXEntity>)aColl.getValue();
                }
                
                //add elements to attribute set
                pathSet.addAll(Arrays.asList(path.getElements()));

            } else {//if annotationMode is disabled

                //create edge
                ONDEXRelation r = graph.getFactory().createRelation(from, to, rt, etTrans);

                //delete collapsed elements except from and to
                for (ONDEXEntity entity : path.getElements()) {
                    if (entity instanceof ONDEXConcept) {
                        if (!entity.equals(from) && !entity.equals(to)) {
                            toDelete.add(entity);
                        }
                    } else {
                        toDelete.add(entity);
                    }
                }
            }
        }

        for (ONDEXEntity entity: toDelete) {
            if (entity instanceof ONDEXConcept) {
                graph.deleteConcept(entity.getId());
            } else {
                graph.deleteRelation(entity.getId());
            }
        }
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public String getId() {
        return "ncl_semantic_collapser";
    }

    @Override
    public String getName() {
        return "Semantic collapse (Newcastle Ondex project)";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }
}
