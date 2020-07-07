package net.sourceforge.ondex.export.prolog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringMappingPairArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.export.ONDEXExport;
import net.sourceforge.ondex.marshal.Marshaller;

/**
 * Exports the given graph as prolog taken certain restrictions into account.
 * 
 * @author taubertj
 */
public class Export extends ONDEXExport implements ArgumentNames {

	/**
	 * Transforms ONDEX meta data IDs to lower case and removes special
	 * characters.
	 * 
	 * @param s
	 *            String to transform
	 * @return lower cases String
	 */
	public static String makeLower(String s) {
		return s.toLowerCase().replaceAll("[\\s,-]", "_");
	}

	/**
	 * fill argument for attribute names
	 */
	Map<String, String> anMapping = new Hashtable<String, String>();

	/**
	 * arguments for concept classes
	 */
	Map<String, String> ccMapping = new Hashtable<String, String>();

	/**
	 * concatenate id and preferred name?
	 */
	boolean concat = false;

	/**
	 * file to write to
	 */
	File file = null;

	/**
	 * restrict length of anyone quoted string?
	 */
	boolean restrict = false;

	/**
	 * arguments for relation types
	 */
	Map<String, String> rtMapping = new Hashtable<String, String>();

	/**
	 * Adds special formating for Boolean.
	 * 
	 * @param b
	 *            Boolean
	 * @param clause
	 *            StringBuffer
	 */
	private void composeBoolean(Boolean b, StringBuffer clause) {
		clause.append("',");
		clause.append(b.toString());
		clause.append(",");
	}

	/**
	 * Adds special formating for Number.
	 * 
	 * @param n
	 *            Number
	 * @param clause
	 *            StringBuffer
	 */
	private void composeNumber(Number n, StringBuffer clause) {
		clause.append("',");
		clause.append(n.toString());
		clause.append(",");
	}

	/**
	 * General case using XStream wrapper
	 * 
	 * @param o
	 *            Object
	 * @param clause
	 *            StringBuffer
	 */
	private void composeObject(Object o, StringBuffer clause) {
		clause.append("','");
		clause.append(quote(Marshaller.getMarshaller().toXML(o)));
		clause.append("',");
	}

	/**
	 * Adds special case for String
	 * 
	 * @param s
	 *            String
	 * @param clause
	 *            StringBuffer
	 */
	private void composeString(String s, StringBuffer clause) {
		clause.append("','");
		clause.append(quote(s));
		clause.append("',");
	}

	/**
	 * Determines the type of Attribute data.
	 * 
	 * @param data
	 *            Attribute
	 * @param clause
	 *            StringBuffer
	 */
	private void decideCompose(Attribute data, StringBuffer clause) {
		Object o = data.getValue();
		if (o instanceof Number) {
			composeNumber((Number) o, clause);
		} else if (o instanceof Boolean) {
			composeBoolean((Boolean) o, clause);
		} else if (o instanceof String) {
			composeString((String) o, clause);
		} else {
			composeObject(o, clause);
		}
		clause.append(data.isDoIndex());
		clause.append(").\n");
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[] {
				new FileArgumentDefinition(FileArgumentDefinition.EXPORT_FILE,
						"Prolog export file", true, false, false, false),
				new StringMappingPairArgumentDefinition(CONCEPTCLASS_ARG,
						CONCEPTCLASS_ARG_DESC, true, null, true),
				new StringMappingPairArgumentDefinition(RELATIONTYPE_ARG,
						RELATIONTYPE_ARG_DESC, false, null, true),
				new StringMappingPairArgumentDefinition(ATTRNAME_ARG,
						ATTRNAME_ARG_DESC, false, null, true),
				new BooleanArgumentDefinition(CONCAT_ARG, CONCAT_ARG_DESC,
						false, false),
				new BooleanArgumentDefinition(RESTRICTLENGTH_ARG,
						RESTRICTLENGTH_ARG_DESC, false, false) };
	}

	/**
	 * Constructs the local name of a given concept.
	 * 
	 * @param c
	 *            ONDEXConcept
	 * @return String
	 */
	private String getLocalName(ONDEXConcept c) {
		String local = String.valueOf(c.getId());
		if (concat) {
			// check if preferred name is present
			ConceptName preferredName = c.getConceptName();
			if (preferredName != null) {
				local = local + ": " + quote(preferredName.getName());
			} else {
				// use PID as fall-back
				local = local + ": " + quote(c.getPID());
			}
		}
		return local;
	}

	@Override
	public String getName() {
		return "Prolog Export";
	}

	@Override
	public String getVersion() {
		return "20.09.2011";
	}

	@Override
	public String getId() {
		return "prolog";
	}

	/**
	 * Some sanity checks necessary for ProLog.
	 * 
	 * @param q
	 *            String to check
	 * @return checked/quoted String
	 */
	private String quote(String q) {
		String temp = q.replaceAll("\'", "\\\\\'").replaceAll("\n", "");
		if (restrict && temp.length() > 250)
			temp = temp.substring(0, 250) + "...";
		return temp;
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

		// fill arguments for concept classes
		for (Object o : args.getObjectValueList(CONCEPTCLASS_ARG)) {
			String[] split = o.toString().split(",");
			ccMapping.put(split[0], split[1]);
		}

		// fill arguments for relation types
		if (args.getOptions().containsKey(RELATIONTYPE_ARG))
			for (Object o : args.getObjectValueList(RELATIONTYPE_ARG)) {
				String[] split = o.toString().split(",");
				rtMapping.put(split[0], split[1]);
			}

		// fill argument for attribute names
		if (args.getOptions().containsKey(ATTRNAME_ARG))
			for (Object o : args.getObjectValueArray(ATTRNAME_ARG)) {
				String[] split = o.toString().split(",");
				anMapping.put(split[0], split[1]);
			}

		// concat id and preferred name?
		if (args.getUniqueValue(CONCAT_ARG) != null)
			concat = (Boolean) args.getUniqueValue(CONCAT_ARG);

		// restrict length of strings?
		if (args.getUniqueValue(RESTRICTLENGTH_ARG) != null)
			restrict = (Boolean) args.getUniqueValue(RESTRICTLENGTH_ARG);

		// write output to file
		file = new File(
				(String) args
						.getUniqueValue(FileArgumentDefinition.EXPORT_FILE));
		fireEventOccurred(new GeneralOutputEvent("Saving to "
				+ file.getAbsolutePath(), getCurrentMethodName()));
		write();
	}

	/**
	 * Writes the content of the AbstractONDEXGraph to a ProLog file.
	 */
	private void write() throws Exception {
		// get all graph related things
		ONDEXGraphMetaData meta = graph.getMetaData();

		// hold extracted informations
		Map<ONDEXConcept, String> naming = new Hashtable<ONDEXConcept, String>();
		Map<RelationType, List<RelationType>> reverseTypes = new Hashtable<RelationType, List<RelationType>>();
		Map<ConceptClass, List<ConceptClass>> reverseClasses = new Hashtable<ConceptClass, List<ConceptClass>>();

		Map<ConceptClass, Set<ONDEXConcept>> concepts = new Hashtable<ConceptClass, Set<ONDEXConcept>>();
		Map<RelationType, List<ONDEXRelation>> relations = new Hashtable<RelationType, List<ONDEXRelation>>();
		Map<AttributeName, Set<ONDEXEntity>> gds = new Hashtable<AttributeName, Set<ONDEXEntity>>();

		// get participating concepts of concept classes
		for (String ccid : ccMapping.keySet()) {
			// get concept class from meta data
			ConceptClass cc = meta.getConceptClass(ccid);

			// get all concepts of concept class
			for (ONDEXConcept c : graph.getConceptsOfConceptClass(cc)) {
				naming.put(c, getLocalName(c));
				if (!concepts.containsKey(cc)) {
					concepts.put(cc, new HashSet<ONDEXConcept>());
				}
				concepts.get(cc).add(c);
				// get concept Attribute
				for (Attribute cattribute : c.getAttributes()) {
					// check for valid attribute name
					AttributeName an = cattribute.getOfType();
					String anid = an.getId();
					if (anMapping.containsKey(anid)) {
						if (!gds.containsKey(an)) {
							gds.put(an, new HashSet<ONDEXEntity>());
						}
						gds.get(an).add(c);
					}
				}
			}
		}

		// get participating concept info first
		for (String rtid : rtMapping.keySet()) {

			// get relation type from meta data
			RelationType rt = meta.getRelationType(rtid);

			// handle specialisation cases as special clauses
			RelationType sp = rt.getSpecialisationOf();
			if (sp != null) {
				if (!reverseTypes.containsKey(sp)) {
					reverseTypes.put(sp, new ArrayList<RelationType>());
				}
				reverseTypes.get(sp).add(rt);
			}

			// get all relations of relation type
			List<ONDEXRelation> list = new ArrayList<ONDEXRelation>();

			for (ONDEXRelation r : graph.getRelationsOfRelationType(rt)) {
				boolean validRelation = true;

				// get from concept with concept class
				ONDEXConcept from = r.getFromConcept();
				ConceptClass fromCC = from.getOfType();
				if (!concepts.containsKey(fromCC)
						|| !concepts.get(fromCC).contains(from)) {
					validRelation = false;
				}

				// get to concept with concept class
				ONDEXConcept to = r.getToConcept();
				ConceptClass toCC = to.getOfType();
				if (!concepts.containsKey(toCC)
						|| !concepts.get(toCC).contains(to)) {
					validRelation = false;
				}

				// checks if both concepts are allowed
				if (validRelation) {
					list.add(r);
					// get relation Attribute
					for (Attribute rattribute : r.getAttributes()) {
						// check for valid attribute name
						AttributeName an = rattribute.getOfType();
						String anid = an.getId();
						if (anMapping.containsKey(anid)) {
							if (!gds.containsKey(an)) {
								gds.put(an, new HashSet<ONDEXEntity>());
							}
							gds.get(an).add(r);
						}
					}
				}
			}

			relations.put(rt, list);
		}

		// handle reverse lookup of specialisation of
		for (ConceptClass cc : concepts.keySet()) {
			ConceptClass sp = cc.getSpecialisationOf();
			if (sp != null) {
				if (!reverseClasses.containsKey(sp))
					reverseClasses.put(sp, new ArrayList<ConceptClass>());
				reverseClasses.get(sp).add(cc);
			}
		}

		// open writer to file
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		// concepts with concept class as clause name
		writeConceptAtoms(writer, naming, concepts, reverseClasses);

		// static clause name concept_name
		writeConceptNames(writer, naming, concepts);

		// static clause name concept_accession
		writeConceptAccessions(writer, naming, concepts);

		// relations with relation type as clause name
		writeRelationAtoms(writer, naming, relations, reverseTypes);

		// left over not yet written hierarchy
		for (RelationType rt : reverseTypes.keySet())
			for (RelationType sp : reverseTypes.get(rt))
				writeRelationTypesHierarchy(writer, sp);

		// left over not yet written hierarchy
		for (ConceptClass cc : reverseClasses.keySet())
			for (ConceptClass sp : reverseClasses.get(cc))
				writeConceptClassesHierarchy(writer, sp);

		// Attribute for concepts & relations with attribute name as clause name
		writeGDS(writer, naming, gds);

		writer.flush();
		writer.close();
	}

	/**
	 * Writes all clauses containing concept accessions.
	 * 
	 * @param writer
	 *            BufferedWriter
	 * @param naming
	 *            Map<ONDEXConcept, String>
	 * @param concepts
	 *            Map<ConceptClass, Set<ONDEXConcept>>
	 * @throws IOException
	 */
	private void writeConceptAccessions(BufferedWriter writer,
			Map<ONDEXConcept, String> naming,
			Map<ConceptClass, Set<ONDEXConcept>> concepts) throws IOException {
		// buffer for clauses
		StringBuffer clause = new StringBuffer();

		// write all concept accessions
		for (ConceptClass key : concepts.keySet()) {
			for (ONDEXConcept c : concepts.get(key)) {
				// a concept can have more than one accession
				for (ConceptAccession ca : c.getConceptAccessions()) {
					clause.delete(0, clause.length());
					clause.append("concept_accession");
					clause.append("('");
					clause.append(naming.get(c));
					clause.append("','");
					clause.append(ca.getElementOf().getId());
					clause.append("','");
					clause.append(quote(ca.getAccession()));
					clause.append("',");
					clause.append(ca.isAmbiguous());
					clause.append(").\n");
					writer.write(clause.toString());
				}
			}
		}
	}

	/**
	 * Writes atom clauses about concepts using concept id and concept name.
	 * 
	 * @param writer
	 *            BufferedWriter
	 * @param naming
	 *            Map<ONDEXConcept, String>
	 * @param concepts
	 *            Map<ConceptClass, Set<ONDEXConcept>>
	 * @param reverseClasses
	 *            Map<ConceptClass, List<ONDEXConcept>>
	 * @throws IOException
	 */
	private void writeConceptAtoms(BufferedWriter writer,
			Map<ONDEXConcept, String> naming,
			Map<ConceptClass, Set<ONDEXConcept>> concepts,
			Map<ConceptClass, List<ConceptClass>> reverseClasses)
			throws IOException {
		// buffer for clauses
		StringBuffer clause = new StringBuffer();

		// write concept clauses
		for (ConceptClass key : concepts.keySet()) {
			// handle hierarchy of concept classes
			if (reverseClasses.containsKey(key)) {
				for (ConceptClass sp : reverseClasses.get(key))
					writeConceptClassesHierarchy(writer, sp);
				reverseClasses.remove(key);
			}
			String ccid = key.getId();
			for (ONDEXConcept c : concepts.get(key)) {
				String anno = "";
				if (c.getAnnotation() != null)
					anno = c.getAnnotation();
				String desc = "";
				if (c.getDescription() != null)
					desc = c.getDescription();
				for (EvidenceType et : c.getEvidence()) {
					clause.delete(0, clause.length());
					// here mapping of clause name
					clause.append(ccMapping.get(ccid));
					clause.append("('");
					clause.append(naming.get(c));
					clause.append("','");
					clause.append(quote(anno));
					clause.append("','");
					clause.append(quote(desc));
					clause.append("','");
					clause.append(c.getElementOf().getId());
					clause.append("','");
					clause.append(quote(et.getId()));
					clause.append("').\n");
					writer.write(clause.toString());
				}
			}
		}
	}

	/**
	 * Writes hierarchy of concept classes.
	 * 
	 * @param writer
	 *            BufferedWriter
	 * @param cc
	 *            ConceptClass
	 * @throws IOException
	 */
	private void writeConceptClassesHierarchy(BufferedWriter writer,
			ConceptClass cc) throws IOException {
		// buffer for clauses
		StringBuffer clause = new StringBuffer();

		// write knowledge rules about concept classes
		ConceptClass sp = cc.getSpecialisationOf();
		if (sp != null) {
			String spid = sp.getId();
			// don't forget to transform all clause names
			String map = makeLower(spid);
			if (ccMapping.containsKey(spid))
				map = ccMapping.get(spid);
			clause.append(map);
			clause.append("(A,B,C,D,E) :- ");
			String id = cc.getId();
			map = makeLower(cc.getId());
			if (ccMapping.containsKey(id))
				map = ccMapping.get(id);
			clause.append(map);
			clause.append("(A,B,C,D,E).\n");
			writer.write(clause.toString());
		}
	}

	/**
	 * Writes all clauses containing concept names.
	 * 
	 * @param writer
	 *            BufferedWriter
	 * @param naming
	 *            Map<ONDEXConcept, String>
	 * @param concepts
	 *            Map<ConceptClass, Set<ONDEXConcept>>
	 * @throws IOException
	 */
	private void writeConceptNames(BufferedWriter writer,
			Map<ONDEXConcept, String> naming,
			Map<ConceptClass, Set<ONDEXConcept>> concepts) throws IOException {
		// buffer for clauses
		StringBuffer clause = new StringBuffer();

		// write all concept names
		for (ConceptClass key : concepts.keySet()) {
			for (ONDEXConcept c : concepts.get(key)) {
				// a concept can have more than one name
				for (ConceptName cn : c.getConceptNames()) {
					clause.delete(0, clause.length());
					clause.append("concept_name");
					clause.append("('");
					clause.append(naming.get(c));
					clause.append("','");
					clause.append(quote(cn.getName()));
					clause.append("',");
					clause.append(cn.isPreferred());
					clause.append(").\n");
					writer.write(clause.toString());
				}
			}
		}
	}

	/**
	 * Writes clauses for concept & relation Attribute.
	 * 
	 * @param writer
	 *            BufferedWriter
	 * @param naming
	 *            Map<ONDEXConcept, String>
	 * @param gds
	 *            Map<AttributeName, Set<AbstractONDEXEntity>>
	 * @throws IOException
	 */
	private void writeGDS(BufferedWriter writer,
			Map<ONDEXConcept, String> naming,
			Map<AttributeName, Set<ONDEXEntity>> gds) throws IOException {
		// buffer for clauses
		StringBuffer clause = new StringBuffer();

		// write all Attribute
		for (AttributeName key : gds.keySet()) {
			String an = key.getId();

			// write all concepts first
			for (ONDEXEntity e : gds.get(key)) {
				if (e instanceof ONDEXConcept) {
					// write concept Attribute
					ONDEXConcept c = (ONDEXConcept) e;
					Attribute data = c.getAttribute(key);
					clause.delete(0, clause.length());
					clause.append("concept_");
					clause.append(anMapping.get(an));
					clause.append("('");
					clause.append(naming.get(c));

					// write value of Attribute
					decideCompose(data, clause);
					writer.write(clause.toString());
				}
			}

			// write all relations second
			for (ONDEXEntity e : gds.get(key)) {
				if (e instanceof ONDEXRelation) {
					// write relation Attribute
					ONDEXRelation r = (ONDEXRelation) e;
					Attribute data = r.getAttribute(key);
					String rtid = r.getOfType().getId();
					clause.delete(0, clause.length());
					clause.append("relation_");
					clause.append(anMapping.get(an));
					clause.append("(");
					clause.append(rtMapping.get(rtid));
					clause.append(",'");
					ONDEXConcept from = r.getFromConcept();
					clause.append(naming.get(from));
					clause.append("','");
					ONDEXConcept to = r.getToConcept();
					clause.append(naming.get(to));

					// write value of Attribute
					decideCompose(data, clause);
					writer.write(clause.toString());
				}
			}
		}
	}

	/**
	 * Writes atom clauses about relationships between concepts.
	 * 
	 * @param writer
	 *            BufferedWriter
	 * @param naming
	 *            Map<ONDEXConcept, String>
	 * @param relations
	 *            Map<RelationType, List<ONDEXRelation>>
	 * @param reverseTypes
	 *            Map<RelationType, List<RelationType>>
	 * @throws IOException
	 */
	private void writeRelationAtoms(BufferedWriter writer,
			Map<ONDEXConcept, String> naming,
			Map<RelationType, List<ONDEXRelation>> relations,
			Map<RelationType, List<RelationType>> reverseTypes)
			throws IOException {
		// buffer for clauses
		StringBuffer clause = new StringBuffer();

		// write relation clauses
		for (RelationType key : relations.keySet()) {
			if (reverseTypes.containsKey(key)) {
				for (RelationType sp : reverseTypes.get(key))
					writeRelationTypesHierarchy(writer, sp);
				reverseTypes.remove(key);
			}
			String rtid = key.getId();
			for (ONDEXRelation r : relations.get(key)) {
				for (EvidenceType et : r.getEvidence()) {
					clause.delete(0, clause.length());
					clause.append(rtMapping.get(rtid));
					clause.append("('");
					ONDEXConcept from = r.getFromConcept();
					clause.append(naming.get(from));
					clause.append("','");
					ONDEXConcept to = r.getToConcept();
					clause.append(naming.get(to));
					clause.append("','");
					clause.append(quote(et.getId()));
					clause.append("').\n");
					writer.write(clause.toString());
				}
			}
		}
	}

	/**
	 * Writes hierarchy of relation types.
	 * 
	 * @param writer
	 *            BufferedWriter
	 * @param rt
	 *            RelationType
	 * @throws IOException
	 */
	private void writeRelationTypesHierarchy(BufferedWriter writer,
			RelationType rt) throws IOException {
		// buffer for clauses
		StringBuffer clause = new StringBuffer();

		// write knowledge rules about relation types
		RelationType sp = rt.getSpecialisationOf();
		if (sp != null) {
			String spid = sp.getId();
			// don't forget to transform all clause names
			String map = makeLower(spid);
			if (rtMapping.containsKey(spid))
				map = rtMapping.get(spid);
			clause.append(map);
			clause.append("(X,Y,Z) :- ");
			String id = rt.getId();
			map = makeLower(id);
			if (rtMapping.containsKey(id))
				map = rtMapping.get(id);
			clause.append(map);
			clause.append("(X,Y,Z).\n");
			writer.write(clause.toString());
		}
	}

}
