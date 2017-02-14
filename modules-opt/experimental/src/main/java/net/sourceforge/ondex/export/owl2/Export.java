package net.sourceforge.ondex.export.owl2;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.exception.type.ONDEXConfigurationException;
import net.sourceforge.ondex.export.ONDEXExport;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.OWLXMLOntologyFormat;
import org.semanticweb.owl.model.*;
import org.semanticweb.owl.util.SimpleURIMapper;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Export as OWL2.
 * <p/>
 * This does a naive mapping of the core data structure.
 *
 * @author Matthew Pocock
 */
public class Export extends ONDEXExport
{
    private static final String ONTOLOGY_URI_ARG = "ontologyURI";
    private static final String ONTOLOGY_URI_ARG_DESC = "The URI of the generated ontology.";

    @Override
    public String getName() {
        return "owl2";
    }

    @Override
    public String getVersion() {
        return "20/11/2009";
    }

    @Override
    public String getId() {
        return "owl2";
    }


    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(ONTOLOGY_URI_ARG, ONTOLOGY_URI_ARG_DESC, true, null, false),
                new FileArgumentDefinition(FileArgumentDefinition.EXPORT_FILE, FileArgumentDefinition.EXPORT_FILE_DESC, true, false, false, false)
        };
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
    public void start() throws Exception {
        File ondexDir = new File(Config.ondexDir);

        String expFileName = (String) args.getUniqueValue(FileArgumentDefinition.EXPORT_FILE);
        if (expFileName == null) {
            throw new ONDEXConfigurationException("No export file specified.");
        }
        File expFile = new File(ondexDir, expFileName);
        URI physicalURI = expFile.toURI();
        URI ontologyURI = new URI((String) getArguments().getUniqueValue(ONTOLOGY_URI_ARG));

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        SimpleURIMapper mapper = new SimpleURIMapper(ontologyURI, physicalURI);
        manager.addURIMapper(mapper);

        OWLOntology ontology = manager.createOntology(ontologyURI);
        OWLDataFactory factory = manager.getOWLDataFactory();

        exportMetaData(manager, ontology, factory, ontologyURI, graph.getMetaData());
        exportData(manager, ontology, factory, ontologyURI, graph);

        manager.saveOntology(ontology, new OWLXMLOntologyFormat());
    }

    private void exportMetaData(OWLOntologyManager manager, OWLOntology ontology, OWLDataFactory factory, URI ontologyURI, ONDEXGraphMetaData metaData) throws OWLOntologyChangeException, URISyntaxException {
        exportConceptClasses(manager, ontology, factory, ontologyURI, metaData.getConceptClasses());
        exportRelationTypes(manager, ontology, factory, ontologyURI, metaData.getRelationTypes());
    }

    private void exportConceptClasses(OWLOntologyManager manager, OWLOntology ontology, OWLDataFactory factory, URI ontologyURI, Set<ConceptClass> conceptClasses)
            throws OWLOntologyChangeException, URISyntaxException {
        for (ConceptClass cc : conceptClasses) {
            ConceptClass csp = cc.getSpecialisationOf();
            if (csp != null) {
                OWLClass sub = factory.getOWLClass(makeURI(ontologyURI, cc));
                OWLClass sup = factory.getOWLClass(makeURI(ontologyURI, csp));
                OWLAxiom axiom = factory.getOWLSubClassAxiom(sub, sup);
                AddAxiom addAxiom = new AddAxiom(ontology, axiom);
                manager.applyChange(addAxiom);
            }
        }
    }

    private void exportRelationTypes(OWLOntologyManager manager, OWLOntology ontology, OWLDataFactory factory, URI ontologyURI, Set<RelationType> relationTypes)
            throws OWLOntologyChangeException, URISyntaxException {
        for (RelationType rt : relationTypes) {
            OWLObjectProperty rtp = factory.getOWLObjectProperty(makeURI(ontologyURI, rt));

            // inverses
            String inverseName = rt.getInverseName();
            if (inverseName != null) {
                RelationType ir = graph.getMetaData().getRelationType(inverseName);
                if (ir == null) {
                    System.err.println("Meta-data inconsistency: property " + rt.getId() + " has inverse " + inverseName + " which doesn't appear to exist");
                    continue;
                }
                OWLObjectProperty rsp = factory.getOWLObjectProperty(makeURI(ontologyURI, ir));
                OWLAxiom axiom = factory.getOWLInverseObjectPropertiesAxiom(rtp, rsp);
                AddAxiom addAxiom = new AddAxiom(ontology, axiom);
                manager.applyChange(addAxiom);
            }

            // symmetric
            if (rt.isSymmetric()) {
                OWLAxiom axiom = factory.getOWLSymmetricObjectPropertyAxiom(rtp);
                AddAxiom addAxiom = new AddAxiom(ontology, axiom);
                manager.applyChange(addAxiom);
            }

            if (rt.isAntisymmetric()) {
                OWLAxiom axiom = factory.getOWLAntiSymmetricObjectPropertyAxiom(rtp);
                AddAxiom addAxiom = new AddAxiom(ontology, axiom);
                manager.applyChange(addAxiom);
            }

            if (rt.isReflexive()) {
                OWLAxiom axiom = factory.getOWLReflexiveObjectPropertyAxiom(rtp);
                manager.applyChange(new AddAxiom(ontology, axiom));
            }

            if (rt.isTransitiv()) {
                OWLAxiom axiom = factory.getOWLTransitiveObjectPropertyAxiom(rtp);
                manager.applyChange(new AddAxiom(ontology, axiom));
            }

            // subsumption
            RelationType rst = rt.getSpecialisationOf();
            if (rst != null) {
                OWLObjectProperty rsp = factory.getOWLObjectProperty(makeURI(ontologyURI, rst));
                OWLAxiom axiom = factory.getOWLSubObjectPropertyAxiom(rtp, rsp);
                AddAxiom addAxiom = new AddAxiom(ontology, axiom);
                manager.applyChange(addAxiom);
            }
        }
    }

    private void exportData(OWLOntologyManager manager, OWLOntology ontology, OWLDataFactory factory, URI ontologyURI, ONDEXGraph graph)
            throws URISyntaxException, OWLOntologyChangeException {
        exportConcepts(manager, ontology, factory, ontologyURI, graph.getConcepts());
        exportRelations(manager, ontology, factory, ontologyURI, graph.getRelations());
    }

    private void exportConcepts(OWLOntologyManager manager, OWLOntology ontology, OWLDataFactory factory, URI ontologyURI, Set<ONDEXConcept> concepts)
            throws URISyntaxException, OWLOntologyChangeException {
        for (ONDEXConcept c : concepts) {
            OWLIndividual ind = factory.getOWLIndividual(makeURI(ontologyURI, c));

            OWLClass type = factory.getOWLClass(makeURI(ontologyURI, c.getOfType()));
            OWLAxiom axiom = factory.getOWLClassAssertionAxiom(ind, type);
            manager.applyChange(new AddAxiom(ontology, axiom));
        }
    }

    private void exportRelations(OWLOntologyManager manager, OWLOntology ontology, OWLDataFactory factory, URI ontologyURI, Set<ONDEXRelation> relations)
            throws URISyntaxException, OWLOntologyChangeException {
        for (ONDEXRelation r : relations) {
            OWLIndividual from = factory.getOWLIndividual(makeURI(ontologyURI, r.getFromConcept()));
            OWLIndividual to = factory.getOWLIndividual(makeURI(ontologyURI, r.getToConcept()));
            OWLObjectProperty prop = factory.getOWLObjectProperty(makeURI(ontologyURI, r.getOfType()));

            OWLObjectPropertyAssertionAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(from, prop, to);
            manager.applyChange(new AddAxiom(ontology, axiom));
        }
    }

    private URI makeURI(URI ontologyURI, ConceptClass cc)
            throws URISyntaxException {
        return new URI(ontologyURI + "#conceptClass_" + cc.getId());
    }

    private URI makeURI(URI ontologyURI, ONDEXConcept c)
            throws URISyntaxException {
        return new URI(ontologyURI + "#concept_" + c.getId());
    }

    private URI makeURI(URI ontologyURI, RelationType r)
            throws URISyntaxException {
        return new URI(ontologyURI + "#relation_" + r.getId());
    }
}
