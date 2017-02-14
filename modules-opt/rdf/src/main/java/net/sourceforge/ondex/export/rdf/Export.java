package net.sourceforge.ondex.export.rdf;

import java.io.FileWriter;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.config.Constants;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.export.ONDEXExport;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DC_11;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
/**
 * RDF export.
 *
 * @author Matthew Pocock
 * @author taubertj
 */
@Authors(authors={"Matthew Pocock"}, emails={"drdozer@sourceforge.net"})
@Status(status=StatusType.EXPERIMENTAL)
public class Export extends ONDEXExport {

	public static class ONDEXRdf {

		public static String createONDEXCoreURI(String s) {
			return Constants.ONDEX_CORE_URI + s;
		}

		public static String annotationUri = createONDEXCoreURI("annotation");
		public static String conceptAccessionUri = createONDEXCoreURI("conceptAccession");
		public static String conceptNameUri = createONDEXCoreURI("conceptName");
		public static String contextUri = createONDEXCoreURI("context");
		public static String descriptionUri = createONDEXCoreURI("conceptDescription");
		public static String elementOfUri = createONDEXCoreURI("elementOf");
		public static String evidenceUri = createONDEXCoreURI("evidence");
		public static String idUri = createONDEXCoreURI("id");
		public static String ofTypeUri = createONDEXCoreURI("ofType");
		public static String pidUri = createONDEXCoreURI("pid");

		public static String toUri(String graphUri, String typeName, String id) {
			StringBuilder sb = new StringBuilder();
			sb.append(graphUri);
			sb.append("/");
			sb.append(typeName);
			sb.append("/");
			sb.append(id);
			return sb.toString();
		}

		public static String conceptToUri(String graphUri, ONDEXConcept c) {
			return toUri(graphUri, "concept", String.valueOf(c.getId()));
		}
		public static String relationToUri(String graphUri, ONDEXRelation r) { 
			return toUri(graphUri, "relation", String.valueOf(r.getId()));
		}
		public static String dataSourceToUri(String graphUri, DataSource dataSource) {
			return toUri(graphUri, "cv", dataSource.getId());
		}
		public static String ccToUri(String graphUri, ConceptClass cc) {
			return toUri(graphUri, "conceptClass", cc.getId());
		}
		public static String etToUri(String graphUri, EvidenceType et) {
			return toUri(graphUri, "evidenceType", et.getId());
		}

		public static String attributeNameToUri(String graphUri, AttributeName an) {
			String id = an.getId();
			int colI = id.indexOf(":");
			String anid = id;
			if (colI > -1) anid = id.substring(0, colI);
			return toUri(graphUri, "attributeName", anid);
		}
		public static String rtToUri(String graphUri, RelationType rt) {
			return toUri(graphUri, "relationType", rt.getId());
		}


		//def nullOpt[T](t: T): Option[T] = if(t == null) None else Some(t)
		//def emptyOp(s: String): Option[String] = if(s == null || s == "") None else Some(s)
	}


	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition[]{new FileArgumentDefinition(FileArgumentDefinition.EXPORT_FILE, "RDF file to export", true, false, false, false),
				new StringArgumentDefinition("uri", "graph URI, used as the base for all concepts/relations etc. exported", true, "", false)};
	}

	@Override
	public String getVersion() {
		return "17/05/2010";
	}

	@Override
	public String getName() {
		return "rdf";
	}

	@Override
	public String getId() {
		return "rdf";
	}

	public void writeEntity(Resource res, ONDEXEntity entity, Model model, String graphURI) {
		// contexts
		for(ONDEXConcept ctxt : entity.getTags()) {
			res.addProperty(model.createProperty(ONDEXRdf.contextUri), model.createResource(ONDEXRdf.conceptToUri(graphURI, ctxt)));
		}

		// evidence
		for(EvidenceType evt : entity.getEvidence()) {
			res.addProperty(model.createProperty(ONDEXRdf.evidenceUri), model.createResource(ONDEXRdf.etToUri(graphURI, evt)));
		}

		// Attribute values
		for(Attribute attribute : entity.getAttributes()) {
			// todo: add more handlers
			Property p = model.createProperty(ONDEXRdf.attributeNameToUri(graphURI, attribute.getOfType()));
			Object value = attribute.getValue();
			if (value instanceof String) res.addProperty(p, (String) value);
			else if (value instanceof Integer) res.addLiteral(p, (Integer) value);
			else if (value instanceof Double) res.addLiteral(p, (Double) value);
			else if (value instanceof Boolean) res.addLiteral(p, (Boolean) value);
			else if (value instanceof Long) res.addLiteral(p, (Long) value);
			//else res.addProperty(p, value.toString());
		}

	}

	public void rdfMetadata(Resource r, MetaData md) {
		if (md.getDescription() != null && md.getDescription().length() > 0) 
			r.addLiteral(DC_11.description, md.getDescription());
		if (md.getFullname() != null && md.getFullname().length() > 0)    
			r.addLiteral(DC_11.title, md.getFullname());
		r.addLiteral(DC_11.identifier, md.getId());
	}

	public void cRap(Resource cR, Property p, String s) {
		if (s != "") 
			cR.addProperty(p, s);
	}

	@Override 
	public void start() throws Exception  {

		String rdfFileName = ((String) args.getUniqueValue(FileArgumentDefinition.EXPORT_FILE)).trim();
		String graphURI = ((String) args.getUniqueValue("uri")).trim();
		
		if(graphURI.length() == 0){
			throw new Exception("URI argument must specify a valid, non-empty URI prefix.");
		}
		
		
			

		Model model = ModelFactory.createDefaultModel();

		ONDEXGraphMetaData md = graph.getMetaData();

		for(ConceptClass cc  : md.getConceptClasses()) {
			Resource cC = model.createResource(ONDEXRdf.ccToUri(graphURI, cc));
			rdfMetadata(cC, cc);
			if (cc.getSpecialisationOf() != null) cC.addProperty(RDFS.subClassOf, model.createResource(ONDEXRdf.ccToUri(graphURI, cc.getSpecialisationOf())));
		}

		for(RelationType rt : md.getRelationTypes()) {
			Resource rT = model.createResource(ONDEXRdf.rtToUri(graphURI, rt));
			rdfMetadata(rT, rt);
			if (rt.getSpecialisationOf() != null) rT.addProperty(RDFS.subPropertyOf, model.createResource(ONDEXRdf.rtToUri(graphURI, rt.getSpecialisationOf())));
			// todo: add transitive/symmetric etc.
		}

		for(AttributeName an : md.getAttributeNames()) {
			Property aN = model.createProperty(ONDEXRdf.attributeNameToUri(graphURI, an));
			rdfMetadata(aN, an);
			if (an.getSpecialisationOf() != null) aN.addProperty(RDFS.subPropertyOf, model.createProperty(ONDEXRdf.attributeNameToUri(graphURI, an.getSpecialisationOf())));
			// todo: Units
		}

		for(DataSource dataSource : md.getDataSources()) rdfMetadata(model.createResource(ONDEXRdf.dataSourceToUri(graphURI, dataSource)), dataSource);
		for(EvidenceType et : md.getEvidenceTypes()) rdfMetadata(model.createResource(ONDEXRdf.etToUri(graphURI, et)), et);

		for(ONDEXConcept c  : graph.getConcepts()) {
			Resource cR = model.createResource(ONDEXRdf.conceptToUri(graphURI, c));

			cRap(cR, model.createProperty(ONDEXRdf.annotationUri), c.getAnnotation());
			for(ConceptAccession ca : c.getConceptAccessions()) {
				cR.addProperty(model.createProperty(ONDEXRdf.conceptAccessionUri), ca.getAccession());
				// todo: add reification to support isPreferred and elementOf
			}
			for(ConceptName cn : c.getConceptNames()) {
				cR.addProperty(model.createProperty(ONDEXRdf.conceptNameUri), cn.getName());
			}
			cRap(cR, model.createProperty(ONDEXRdf.descriptionUri), c.getDescription());
			cR.addProperty(model.createProperty(ONDEXRdf.elementOfUri), model.createResource(ONDEXRdf.dataSourceToUri(graphURI, c.getElementOf())));

			cR.addProperty(RDF.type, model.createResource(ONDEXRdf.ccToUri(graphURI, c.getOfType())));
			cRap(cR, model.createProperty(ONDEXRdf.pidUri), c.getPID());

			writeEntity(cR, c, model, graphURI);
		}

		for(ONDEXRelation r : graph.getRelations()) {
			Resource f = model.createResource(ONDEXRdf.conceptToUri(graphURI, r.getFromConcept()));
			Resource t = model.createResource(ONDEXRdf.conceptToUri(graphURI, r.getToConcept()));
			Property rt = model.createProperty(ONDEXRdf.rtToUri(graphURI, r.getOfType()));

			Statement stmt = model.createStatement(f, rt, t); model.add(stmt);
			ReifiedStatement rel = stmt.createReifiedStatement(ONDEXRdf.relationToUri(graphURI, r));

			writeEntity(rel, r, model, graphURI);
		}

		FileWriter rdfOut = new FileWriter(rdfFileName);
		model.write(rdfOut);
	}
}

