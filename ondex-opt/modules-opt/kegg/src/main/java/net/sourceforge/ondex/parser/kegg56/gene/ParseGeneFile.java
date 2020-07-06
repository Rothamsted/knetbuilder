package net.sourceforge.ondex.parser.kegg56.gene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.kegg56.GenomeParser;
import net.sourceforge.ondex.parser.kegg56.MetaData;
import net.sourceforge.ondex.parser.kegg56.Parser;
import net.sourceforge.ondex.parser.kegg56.sink.Concept;
import net.sourceforge.ondex.parser.kegg56.sink.ConceptAcc;
import net.sourceforge.ondex.parser.kegg56.sink.ConceptName;
import net.sourceforge.ondex.parser.kegg56.sink.Relation;
import net.sourceforge.ondex.parser.kegg56.sink.Sequence;
import net.sourceforge.ondex.parser.kegg56.util.DPLPersistantSet;

public class ParseGeneFile implements Callable<ParseGeneFile> {

	private static final String DEFINITION = "DEFINITION";
	private static final String ORTHOLOGY = "ORTHOLOGY";
	private static final String STRUCTURE = "STRUCTURE";
	private static final String ENTRY = "ENTRY";
	private static final String NAME = "NAME";
	private static final String DBLINKS = "DBLINKS";
	private static final String AASEQ = "AASEQ";
	private static final String NTSEQ = "NTSEQ";

	private GenomeParser.Taxonomony org;
	private boolean allSeq;
	private InputStream genomeFile;
	private DPLPersistantSet<Sequence> sequenceCache;
	private DPLPersistantSet<Relation> relationsCache;
	private Set<String> foundGenes = new HashSet<String>();
	private Map<String, Boolean> missingGenes = new HashMap<String, Boolean>();
	private Map<String, Set<String>> references;

	private Set<String> nonEnzymeGenes;
	private Set<String> enzymeGenes;

	public ParseGeneFile(Set<String> nonEnzymeGenes, Set<String> enzymeGenes,
			GenomeParser.Taxonomony org, InputStream genomeFile,
			boolean allSeq, DPLPersistantSet<Sequence> sequenceCache,
			DPLPersistantSet<Relation> relationsCache,
			Map<String, Set<String>> references) {
		this.nonEnzymeGenes = nonEnzymeGenes;
		this.enzymeGenes = enzymeGenes;
		this.org = org;
		this.genomeFile = genomeFile;
		this.allSeq = allSeq;
		this.sequenceCache = sequenceCache;
		this.relationsCache = relationsCache;
		this.references = references;
	}

	public ParseGeneFile call() throws IOException, MetaDataMissingException,
			InconsistencyException {
		final Pattern tabsOrSpace = Pattern.compile("[ \t]+");

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				genomeFile));

		while (reader.ready()) {
			String line = reader.readLine();
			if (line.startsWith(ENTRY)) {

				line = tabsOrSpace.split(line)[1].toUpperCase();
				boolean isEnzyme = enzymeGenes.contains(line);

				if (allSeq || isEnzyme || nonEnzymeGenes.contains(line)) {
					foundGenes.add(line);
					parseConceptFromFile(org, line, isEnzyme, reader);
				}
			}
		}
		reader.close();

		// compare sets of found genes with given genes
		if (!foundGenes.containsAll(nonEnzymeGenes)) { // IE MISSING GENES
			for (String gene : nonEnzymeGenes) {
				// if a gene was missing, add it to clashed genes
				if (!foundGenes.contains(gene)) {
					missingGenes.put(gene, Boolean.FALSE); // no sync exclusive
					// to this object on
					// an org
				}
			}
		}

		if (!foundGenes.containsAll(enzymeGenes)) { // IE MISSING GENES
			for (String gene : enzymeGenes) {
				// if a gene was missing, add it to missing genes
				if (!foundGenes.contains(gene)) {
					missingGenes.put(gene, Boolean.TRUE); // no sync exclusive
					// to this object on
					// an org
				}
			}
		}

		nonEnzymeGenes = null;
		enzymeGenes = null;
		foundGenes = null;
		return this;
	}

	private StringBuilder naSeq = new StringBuilder(200);
	private StringBuilder aaSeq = new StringBuilder(200);

	private final Pattern spaceSplit = Pattern.compile(" ");
	private final Pattern commaSplit = Pattern.compile(",");
	private final Pattern colonSplit = Pattern.compile(":");
	private final Pattern leadingWhiteSpace = Pattern.compile("^[ \t]+");

	/**
	 * Parse everything belonging to a gene concept from file
	 * 
	 * @param org
	 *            the organism as prefixed in KEGG eg.. hsa, mmu, ath
	 * @param gene
	 *            kegg ID
	 * @param isEnzyme
	 *            Is this gene encoding an enzyme?
	 * @param reader
	 *            The Reader attached to KEGG org specific gene file
	 * @throws IOException
	 * @throws InconsistencyException
	 */
	public void parseConceptFromFile(GenomeParser.Taxonomony org, String gene,
			boolean isEnzyme, BufferedReader reader) throws IOException,
			MetaDataMissingException, InconsistencyException {

		String id = (org.getKeggId() + ':' + gene).toUpperCase();

		List<Concept> conceptCache = new ArrayList<Concept>();

		// Gene as a concept
		Concept concept_gene = new Concept(id + "_GE", MetaData.CV_KEGG,
				MetaData.CC_GENE);

		ConceptName cn = new ConceptName(gene, true);
		concept_gene.getConceptNames().add(cn);

		String link = "http://www.genome.jp/dbget-bin/www_bget?"
				+ org.getKeggId() + "+" + gene.toLowerCase();
		concept_gene.setUrl(link);

		Sequence ntseq = new Sequence(id + "_"
				+ MetaData.ATTR_NAME_NUCLEIC_ACID);
		ntseq.setConcept_fk(concept_gene.getId());
		ntseq.setSequence_type_fk(MetaData.ATTR_NAME_NUCLEIC_ACID);

		// Protein derive from gene
		Concept concept_protein = new Concept(id + "_PR", MetaData.CV_KEGG,
				MetaData.CC_PROTEIN);
		concept_protein.setDescription("derived protein");

		cn = new ConceptName(gene, true);
		concept_protein.getConceptNames().add(cn);

		Sequence aaseq = new Sequence(id + "_" + MetaData.ATTR_NAME_AMINO_ACID);
		aaseq.setConcept_fk(concept_protein.getId());
		aaseq.setSequence_type_fk(MetaData.ATTR_NAME_AMINO_ACID);

		// Relation between Gene and Protein
		Relation en_by = new Relation(concept_protein.getId(),
				concept_gene.getId(), MetaData.RT_ENCODED_BY);
		if (references.containsKey(id)) {
			for (String context : references.get(id))
				en_by.addContext(context);
		} else {
			System.out.println(id + " not found in referenced list.");
		}

		// Enzyme derive from protein
		Concept concept_enzyme = new Concept(id + "_EN", MetaData.CV_KEGG,
				MetaData.CC_ENZYME);
		concept_enzyme.setDescription("derived enzyme");

		// Relation between Enzyme and Protein
		Relation is_a = new Relation(concept_enzyme.getId(),
				concept_protein.getId(), MetaData.RT_IS_A);
		if (references.containsKey(id)) {
			for (String context : references.get(id))
				is_a.addContext(context);
		}

		// same state variables to see, where we are
		boolean inDefinition = false;
		boolean inDblinks = false;
		boolean inAAseq = false;
		boolean inNTseq = false;

		boolean finished = false;

		Set<String> ecTerms = new HashSet<String>();
		Set<String> pdbStructures = new HashSet<String>();
		String previousDB = null;

		while (reader.ready()) {
			String line = reader.readLine();

			// this ends an entry
			if (line.indexOf("///") > -1 || finished) {

				// additional name processing
				List<String> names = getNames(concept_gene.getDescription());
				for (String name : names) {
					if (name.indexOf("EC:") > -1) {
						ecTerms.add(name);
						continue;
					}
					cn = new ConceptName(name, false);
					concept_gene.getConceptNames().add(cn);
					cn = new ConceptName(name, false);
					concept_protein.getConceptNames().add(cn);
				}

				// gene is standard entry
				Parser.getUtil().writeConcept(concept_gene);

				// if there is an aaseq, its a protein
				if (aaSeq.length() > 0) {
					aaseq.setSeq(aaSeq.toString());
					aaSeq.setLength(0);
					conceptCache.add(concept_protein);
					relationsCache.add(en_by);
					sequenceCache.add(aaseq);
				}

				if (naSeq.length() > 0) {
					ntseq.setSeq(naSeq.toString());
					naSeq.setLength(0);
					sequenceCache.add(ntseq);
				}

				// if its an enzyme it has to be also a protein
				if (isEnzyme) {
					if (aaseq.getSeq() == null) {
						conceptCache.add(concept_protein);
						relationsCache.add(en_by);
					}
					conceptCache.add(concept_enzyme);
					relationsCache.add(is_a);

					// if there is an ec number given in definition use it as
					// cat_c
					String description = concept_gene.getDescription()
							.toUpperCase();

					parseECFromString(description, concept_enzyme.getId(),
							conceptCache, relationsCache);

					for (String term : ecTerms) {
						parseECFromString(term, concept_enzyme.getId(),
								conceptCache, relationsCache);
					}
				}
				Parser.getUtil().writeConcepts(conceptCache);
				return;
			} else if (line.length() > 0) {

				int oldLineSize = line.length();
				line = leadingWhiteSpace.matcher(line).replaceFirst("");

				if (oldLineSize == line.length() || line.length() == 0) {
					inDefinition = false;
					inDblinks = false;
					inAAseq = false;
					inNTseq = false;
				}

				if (inDefinition || line.startsWith(DEFINITION)) {
					// parse multiline definition as description
					inDefinition = true;

					if (concept_gene.getDescription() == null) {
						int indexof = line.indexOf(DEFINITION);
						concept_gene.setDescription(line.substring(
								indexof + DEFINITION.length()).trim());
					} else {
						concept_gene.setDescription(concept_gene
								.getDescription() + " " + line.trim());
					}
				}

				else if (line.startsWith(ORTHOLOGY)) {
					int indexof = line.indexOf(ORTHOLOGY);
					line = line.substring(indexof + ORTHOLOGY.length()).trim();
					int ec = line.indexOf("[EC:");
					if (ec > -1) {
						ecTerms.add(line.substring(ec));
					}
				}

				else if (line.startsWith(STRUCTURE)) {
					int indexof = line.indexOf(STRUCTURE);
					line = line.substring(indexof + STRUCTURE.length()).trim();
					int pdb = line.indexOf("PDB:");
					if (pdb > -1) {
						line = line.substring(pdb + 4).trim();
						String[] split = line.split(" ");
						for (String s : split) {
							pdbStructures.add(s);
						}
						concept_protein.setStructures(pdbStructures);
					}
				}

				else if (line.startsWith(NAME)) {
					// parse name line into several concept names
					int indexof = line.indexOf(NAME);
					String[] results = commaSplit.split(line.substring(
							indexof + NAME.length()).trim());
					for (String name : results) {
						name = name.trim();

						if (name.length() == 0) {
							continue;
						}

						// concept name for gene
						ConceptName conceptNameGE = new ConceptName(name, false);
						if (concept_gene.getConceptNames() == null
								|| concept_gene.getConceptNames().size() == 0) {
							// the first name is preferred
							conceptNameGE.setPreferred(true);
						}
						concept_gene.getConceptNames().add(conceptNameGE);

						// concept name for protein
						ConceptName conceptNamePR = new ConceptName(name, false);
						if (concept_protein.getConceptNames() == null
								|| concept_gene.getConceptNames().size() == 0) {
							// the first name is preferred
							conceptNamePR.setPreferred(true);
						}
						concept_protein.getConceptNames().add(conceptNamePR);

						// concept name for enzyme
						ConceptName conceptNameEN = new ConceptName(name, false);
						boolean hasPreferred = false;
						for (ConceptName cnEN : concept_enzyme
								.getConceptNames()) {
							hasPreferred = hasPreferred || cnEN.isPreferred();
						}
						// the first name is preferred
						conceptNameEN.setPreferred(!hasPreferred);
						concept_enzyme.getConceptNames().add(conceptNameEN);
					}
				}

				else if (inDblinks || line.startsWith(DBLINKS)) {
					// parse multi-line DBlinks into concept accessions
					inDblinks = true;
					int indexof = line.indexOf(DBLINKS);
					if (indexof > -1) {
						line = line.substring(indexof + DBLINKS.length());
					}
					String[] result = colonSplit.split(line.trim());
					String db = null;
					String[] accs = null;
					if (result.length == 2) {
						// first comes DB, then list of accessions
						db = result[0];

						if (db.length() == 0) {
							System.err.println("Unknown DB: " + line);
						}

						accs = spaceSplit.split(result[1].trim());
					} else {
						// accessions are spanning across multiple lines
						if (previousDB != null) {
							db = previousDB;
						} else {
							System.err.println("Unknown accession format: "
									+ line);
						}

						accs = spaceSplit.split(line.trim());
					}

					if (db != null) {
						for (String accSplit : accs) {

							// concept accession for gene
							ConceptAcc conceptAccGE = new ConceptAcc(accSplit,
									db);

							concept_gene.getConceptAccs().add(conceptAccGE);

							// concept accession for protein
							ConceptAcc conceptAccPR = new ConceptAcc(accSplit,
									db);

							concept_protein.getConceptAccs().add(conceptAccPR);

							// concept accession for enzyme
							ConceptAcc conceptAccEN = new ConceptAcc(accSplit,
									db);

							concept_enzyme.getConceptAccs().add(conceptAccEN);
						}

						// keep a reference to DB name for multiple line
						// accessions
						previousDB = db;
					} else {
						System.err.println("Unable to get DB: " + line);
					}
				} else if (line.startsWith(AASEQ)) {
					inAAseq = true;
					aaSeq.setLength(0);
				} else if (line.startsWith(NTSEQ)) {
					inNTseq = true;
					naSeq.setLength(0);
				} else if (inAAseq) {
					aaSeq.append(line);
				} else if (inNTseq) {
					naSeq.append(line);
				}

			} else {
				// if we have an empty line after aa and na are filled
				if (aaSeq.length() > 0 && naSeq.length() > 0) {
					finished = true;
				}
			}
		}
		Parser.getUtil().writeConcepts(conceptCache);
	}

	/**
	 * Parses EC terms for String annotation and annotates them to enzyme
	 * 
	 * @param annotation
	 * @param enzyme_id
	 * @param conceptCache
	 * @param relationsCache2
	 */
	private void parseECFromString(String annotation, String enzyme_id,
			List<Concept> conceptCache,
			DPLPersistantSet<Relation> relationsCache2) {

		int ecIndex = annotation.indexOf("[EC:");

		if (ecIndex > -1) {
			annotation = annotation.substring(ecIndex + 4);

			int endIndex = annotation.indexOf("]");
			if (endIndex > 0) {
				annotation = annotation.substring(0, endIndex);
			}
			String[] result = spaceSplit.split(annotation);
			for (String ecID : result) {
				
				// new EC concept if not already exists
				if (!Parser.getConceptWriter().conceptParserIDIsWritten(
						ecID.toUpperCase())) {
					Concept ec = new Concept(ecID, MetaData.CV_KEGG,
							MetaData.CC_EC);
					ec.setDescription("parsed ec from gene annotation");
					ConceptName cn = new ConceptName(ec.getId(), false);
					if (ec.getConceptNames() == null
							|| ec.getConceptNames().size() == 0) {
						cn.setPreferred(true);
					}
					ec.getConceptNames().add(cn);
					ec.getConceptAccs().add(
							new ConceptAcc(ec.getId(), MetaData.CV_EC));
					conceptCache.add(ec);
				}
				Relation member_of = new Relation(enzyme_id, ecID,
						MetaData.RT_MEMBER_PART_OF);
				String id = enzyme_id.substring(0, enzyme_id.length() - 3);
				if (references.containsKey(id)) {
					for (String context : references.get(id))
						member_of.addContext(context);
				} else {
					System.out.println(id + " not found in referenced list.");
				}
				relationsCache2.add(member_of);
			}
		}
	}

	public Map<String, Boolean> getMissingGenes() {
		return missingGenes;
	}

	public GenomeParser.Taxonomony getOrganism() {
		return org;
	}

	private static ArrayList<String> exclusionEndingList;
	private static ArrayList<String> exclusionList;
	private static ArrayList<String> emptyReturn = new ArrayList<String>(0);

	/**
	 * @param description
	 *            the string to split into names
	 * @return the names found
	 */
	protected static List<String> getNames(String description) {

		if (exclusionList == null) {
			exclusionList = new ArrayList<String>(1);
			exclusionList.add("transporter".toUpperCase());
			exclusionList.add("catalytic".toUpperCase());
		}

		if (description.contains("unknown protein")
				|| description.contains("hypothetical protein")) {
			return emptyReturn;
		}
		description = description.replace("family protein", "");
		description = description.replace("putative", "");
		description = description.replaceAll("[,]", "");

		ArrayList<String> names = new ArrayList<String>();

		int ecIndex = description.indexOf("[EC:");

		if (ecIndex > -1)
			description = description.substring(0, ecIndex);

		String[] split = description.split("\\(");
		for (String s : split) {
			int endOpen = s.indexOf(')');
			if (endOpen > -1) {
				String name = s.substring(0, endOpen).trim();
				description = description.replace("(" + s, "");
				if (name.length() > 0 && checkEndsConditions(name)
						&& !exclusionList.contains(name.toUpperCase())
						&& !name.contains("transferring")
						&& !name.contains("binding"))
					names.add(name);
			}
		}

		String[] moreNames = description.split("[;/]");
		for (String name : moreNames) {
			name = name.trim();
			if (name.length() > 0 && checkEndsConditions(name)
					&& !exclusionList.contains(name.toUpperCase())
					&& !name.contains("transferring")
					&& !name.contains("binding"))
				names.add(name);
		}

		return names;
	}

	public static boolean checkEndsConditions(String name) {

		if (exclusionEndingList == null) {
			exclusionEndingList = new ArrayList<String>(1);
			exclusionEndingList.add("ase");
			exclusionEndingList.add("compounds");
			exclusionEndingList.add("enzyme");
			exclusionEndingList.add("carrier");
			exclusionEndingList.add("substances");
			exclusionEndingList.add("ase");
		}

		for (String end : exclusionEndingList) {
			if (name.endsWith(end)) {
				return false;
			}
		}
		return true;
	}

}
