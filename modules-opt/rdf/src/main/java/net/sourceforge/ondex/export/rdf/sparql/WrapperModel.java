package net.sourceforge.ondex.export.rdf.sparql;

import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import net.sourceforge.ondex.core.*;

/**
 * This is based on a schema transformation where ConceptClasses become Classes and RelationTypes become Properties. i.e. its not an exact schema match.
 *
 * @author hindlem
 *         Date: 9/2/11
 *         Time: 11:27 AM
 * @version 0.10
 */
public class WrapperModel {

    public static String urlPrefixMetaData = "http://www.ondex.org/metadata/sparql/";
    public static String urlPrefixInstances = "http://www.ondex.org/instance/sparql/";

    public OntModel om;
    private Model im;

    public WrapperModel(ONDEXGraph og, OntModelSpec spec) {
        om = createOntModel(og.getMetaData(), spec);
        im = createModel(og);
        om.addSubModel(im);
    }

    public ResultSet runSPARQLQuery(String queryString) {
       Query query = QueryFactory.create(queryString);
       QueryExecution qe = QueryExecutionFactory.create(query, om);
       return qe.execSelect();
    }

    public Model createModel(ONDEXGraph og) {
        Model instanceModel = ModelFactory.createDefaultModel();
        for (ONDEXConcept concept : og.getConcepts()) {
            Resource classOfConcept = instanceModel.createResource(urlPrefixMetaData + concept.getOfType().getId());
            instanceModel.createResource(urlPrefixInstances + concept.getId(), classOfConcept);
        }

        for (ONDEXRelation or : og.getRelations()) {
            Property propertyOfPredicate = instanceModel.createProperty(urlPrefixMetaData, or.getOfType().getId());

            Resource fromConcept = instanceModel.getResource(urlPrefixInstances + or.getFromConcept().getId());
            Resource toConcept = instanceModel.getResource(urlPrefixInstances + or.getToConcept().getId());

            instanceModel.createStatement(toConcept, propertyOfPredicate, fromConcept);
        }

        return instanceModel;
    }


    public OntModel createOntModel(ONDEXGraphMetaData omd, OntModelSpec spec) {
        if (spec == null)
            spec = OntModelSpec.RDFS_MEM_RDFS_INF;

        OntModel model = ModelFactory.createOntologyModel(spec);
        //create a super class from which all ConceptClasses will inherit
        OntClass rdfsSuperConceptClass = model.createClass(urlPrefixMetaData + "ConceptClass");
        rdfsSuperConceptClass.addLabel("Concept Class", "en");
        rdfsSuperConceptClass.addComment("The super for classes describing Concept types", "en");

        //we create a class in the OntModel for each
        for (ConceptClass cc : omd.getConceptClasses()) {
            OntClass rdfsClass = model.createClass(urlPrefixMetaData + cc.getId());
            rdfsClass.addLabel(cc.getFullname(), "en");
            rdfsClass.addComment(cc.getDescription(), "en");
        }

        //we then add the hierarchy in a second pass
        for (ConceptClass cc : omd.getConceptClasses()) {
            OntClass rdfsClass = model.getOntClass(urlPrefixMetaData + cc.getId());
            ConceptClass superClass = cc.getSpecialisationOf();
            if (superClass != null) {
                rdfsClass.addSuperClass(model.getOntClass(urlPrefixMetaData + superClass.getId()));
            } else {
                rdfsClass.addSuperClass(rdfsSuperConceptClass);
            }
        }

        Property rdfsSuperRelationType = model.createObjectProperty(urlPrefixMetaData + "RelationType");
        for (RelationType rt : omd.getRelationTypes()) {
            ObjectProperty objectProperty = model.createObjectProperty(urlPrefixMetaData + rt.getId());
            objectProperty.addComment(rt.getDescription(), "en");
            objectProperty.addLabel(rt.getFullname(), "en");
        }

        for (RelationType rt : omd.getRelationTypes()) {
            ObjectProperty objectProperty = model.getObjectProperty(urlPrefixMetaData + rt.getId());
            RelationType superRt = rt.getSpecialisationOf();
            if (superRt != null) {
                objectProperty.addSuperProperty(model.getObjectProperty(urlPrefixMetaData + superRt.getId()));
            } else {
                objectProperty.addSuperProperty(rdfsSuperRelationType);
            }
        }
        return model;
    }


}
