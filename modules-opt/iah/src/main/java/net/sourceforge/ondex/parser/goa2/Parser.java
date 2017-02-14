package net.sourceforge.ondex.parser.goa2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.config.ValidatorRegistry;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.event.type.PluginErrorEvent;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.exception.type.ParsingFailedException;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.exception.type.PluginException;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.tools.MetaDataLookup;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;
import org.apache.log4j.Level;

public class Parser extends ONDEXParser {

	public static final String CVREGEX = "cvregex";

	// ##### ARGUMENTS #####

	public static final String TRLT_FILE_ARG = "TranslationFile";
	public static final String TRLT_FILE_ARG_DESC = "Path to translation file";
	public static final String NOT_ARG = "CreateNegativeRelations";
	public static final String NOT_ARG_DESC = "Whether or not to create negated relations";
	public static final String PUB_ARG = "ParsePublications";
	public static final String PUB_ARG_DESC = "Whether or not to parse publications";

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[] {
				new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
						FileArgumentDefinition.INPUT_FILE_DESC, true, true,
						false, false),
				new FileArgumentDefinition(TRLT_FILE_ARG, TRLT_FILE_ARG_DESC,
						true, true, false),
				new BooleanArgumentDefinition(PUB_ARG, PUB_ARG_DESC, false,
						true),
				new BooleanArgumentDefinition(NOT_ARG, NOT_ARG_DESC, false,
						true), };
	}

	// ##### FIELDS #####

	String inputFile;

	Boolean pubs, parseNots;

	MetaDataLookup<ConceptClass> ccLookup;
	MetaDataLookup<DataSource> dataSourceLookup;
	MetaDataLookup<RelationType> rtLookup;
	MetaDataLookup<EvidenceType> etLookup;

	AbstractONDEXValidator cvMatcher;

	Map<String, Integer> concepts;

	private void initFields() throws Exception {
        inputFile = (String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE);

        pubs = (Boolean) args.getUniqueValue(PUB_ARG);
        if (pubs == null) pubs = true;

        parseNots = (Boolean) args.getUniqueValue(NOT_ARG);
        if (parseNots == null) parseNots = true;

        ONDEXGraphMetaData md = graph.getMetaData();
        File trltFile = new File((String) args.getUniqueValue(TRLT_FILE_ARG));
        ccLookup = new MetaDataLookup<ConceptClass>(trltFile, md, ConceptClass.class);
        dataSourceLookup = new MetaDataLookup<DataSource>(trltFile, md, DataSource.class);
        rtLookup = new MetaDataLookup<RelationType>(trltFile, md, RelationType.class);
        etLookup = new MetaDataLookup<EvidenceType>(trltFile, md, EvidenceType.class);

        cvMatcher = ValidatorRegistry.validators.get(CVREGEX);

        concepts = LazyMap.decorate(new HashMap<String, Integer>(), new Factory<Integer>() {

			@Override
			public Integer create() {
				return Integer.valueOf(-1);
			}});
    }

	// ##### METADATA #####

	private ConceptClass ccPub;
	private DataSource dataSourceGO, dataSourcePubMed;
	private EvidenceType etIMPD;
	private AttributeName anTaxid;

	private void initMetaData() throws MetaDataMissingException {
		ccPub = requireConceptClass("Publication");
		dataSourceGO = requireDataSource("GO");
		dataSourcePubMed = requireDataSource("NLM");
		etIMPD = requireEvidenceType("IMPD");
		anTaxid = requireAttributeName("TAXID");
	}

	// ##### MAIN METHODS #####

	@Override
	public void start() throws Exception {
		initFields();
		initMetaData();

		parseFile();
	}

	// 1 DB required SGD
	// 2 DB_Object_ID required S000000296
	// 3 DB_Object_Symbol required PHO3
	// 4 Qualifier optional NOT
	// 5 GO ID required GO:0003993
	// 6 DB:Reference (|DB:Reference) required PMID:2676709
	// 7 Evidence code required IMP
	// 8 With (or) From optional GO:0000346
	// 9 Aspect required F
	// 10 DB_Object_Name optional acid phosphatase
	// 11 DB_Object_Synonym (|Synonym) optional YBR092C
	// 12 DB_Object_Type required gene
	// 13 taxon(|taxon) required taxon:4932
	// 14 Date required 20010118
	// 15 Assigned_by required SGD

	private void parseFile() throws PluginException {
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			String line = null;
			int lineNum = 0;
			while ((line = br.readLine()) != null) {
				lineNum++;
				if (line.startsWith("!")) {
					continue;
				}

				String[] cols = line.split("\t");
				if (cols.length != 15) {
					logInconsistency("Unexpected number of columns in line "
							+ lineNum);
				}

				// cv
				String db = cols[V1.DB.index()];
				DataSource dataSource = dataSourceLookup.get(db);
				if (dataSource == null) {
					throw new PluginConfigurationException(
							"Unknown data source: " + db);
				}

				// concept class
				String entityType = cols[V1.DB_OBJ_TYPE.index()];
				ConceptClass ccEntity = ccLookup.get(entityType);
				if (ccEntity == null) {
					throw new PluginConfigurationException(
							"Unknown entity type: " + entityType);
				}

				String entityID = cols[V1.DB_OBJ_ID.index()];

				// create concept
				ONDEXConcept entityConcept = getOrCreateConcept(db + ":"
						+ entityID, dataSource, ccEntity, etIMPD);

				// main db accession
				addAccession(entityConcept, entityID);

				// name
				String symbol = cols[V1.DB_OBJECT_SYM.index()];
				if (entityConcept.getConceptName(symbol) == null)
					entityConcept.createConceptName(symbol, true);

				// annotation
				entityConcept.setAnnotation(cols[V1.DB_OBJ_NAME.index()]);// FIXME:
																			// this
																			// seems
																			// wrong!

				// synonyms as accessions or names
				addAccessionOrName(entityConcept,
						cols[V1.DB_OBJ_SYNONYM.index()].split("\\|"));

				// taxid
				addTaxid(entityConcept, cols[V1.TAXON.index()]);

				// GO ConceptClass
				String aspect = cols[V1.ASPECT.index()];
				ConceptClass ccGO = ccLookup.get(aspect);
				if (ccGO == null) {
					throw new PluginConfigurationException(
							"Missing aspect to ConceptClass mapping: " + aspect);
				}

				// GO ID
				String goid = cols[V1.GO_ID.index()];

				// create GO concept
				ONDEXConcept goConcept = getOrCreateConcept(goid, dataSource,
						ccGO, etIMPD);
				if (goConcept.getConceptAccession(goid, dataSourceGO) == null) {
					goConcept.createConceptAccession(goid, dataSourceGO, false);
				}

				ONDEXConcept pmConcept = null;
				String pubMedId = extractPubMed(cols[V1.DB_REF.index()]);
				if (pubs && pubMedId != null) {
					pmConcept = getOrCreateConcept("Publ" + pubMedId,
							dataSourcePubMed, ccPub, etIMPD);
					if (pmConcept.getConceptAccession(pubMedId,
							dataSourcePubMed) == null) {
						pmConcept.createConceptAccession(pubMedId,
								dataSourcePubMed, false);
					}
				}

				String qual = cols[V1.QUAL.index()];
				boolean negative = qual.equals("NOT");

				// GO relation type
				RelationType rt = rtLookup.get(qual + aspect);
				if (rt == null) {
					throw new PluginConfigurationException(
							"Missing aspect to RelationType mapping: " + qual
									+ aspect);
				}

				if (negative && !parseNots) {
					continue;
				}

				// evidence
				EvidenceType et = etLookup.get(cols[V1.EV_CODE.index()]);

				// create relation
				// if (graph.getRelation(entityConcept, goConcept, pmConcept,
				// rt) == null) {
				// graph.getFactory().createRelation(entityConcept, goConcept,
				// pmConcept, rt, et);
				// }
				if (graph.getRelation(entityConcept, goConcept, rt) == null) {
					graph.getFactory().createRelation(entityConcept, goConcept,
							rt, et);
				}

			}
		} catch (IOException e) {
			throw new ParsingFailedException(e);
		}
	}

	private ONDEXConcept getOrCreateConcept(String id, DataSource elementOf,
			ConceptClass ofType, EvidenceType evidencetype) {
		int cid = concepts.get(id);
		ONDEXConcept c = null;
		if (cid == -1) {
			c = graph.getFactory().createConcept(id, elementOf, ofType,
					evidencetype);
			concepts.put(id, c.getId());
		} else {
			c = graph.getConcept(cid);
		}
		return c;
	}

	private String extractPubMed(String string) {
		for (String acc : string.split("\\|")) {
			String[] split = acc.split(":");
			if (split.length == 2 && dataSourceLookup.get(split[0]) != null
					&& dataSourceLookup.get(split[0]).equals(dataSourcePubMed)) {
				return split[1];
			}
		}
		return null;
	}

	private void addTaxid(ONDEXConcept entityConcept, String taxString) {
		if (entityConcept.getAttribute(anTaxid) != null) {
			return;
		}
		String[] split = taxString.split(":");
		if (split.length == 2) {
			try {
				int taxid = Integer.parseInt(split[1]);
				entityConcept.createAttribute(anTaxid, taxid, false);
			} catch (NumberFormatException nfe) {
				logInconsistency(split[1] + "is not a valid taxonomy id");
			}
		}
	}

	private void addAccessionOrName(ONDEXConcept c, String[] accs)
			throws DataSourceMissingException {
		for (String acc : accs) {
			if (acc.trim().equals("")) {
				continue;
			}

			// if (acc.equals("tM(CAU)J1")) {
			// System.out.println("bla");
			// }

			DataSource ns = null;
			String nsid = (String) cvMatcher.validate(acc);
			if (nsid != null) {
				ns = requireDataSource(nsid);
				if (c.getConceptAccession(acc, ns) == null) {
					c.createConceptAccession(acc, ns, false);
				}
			} else {
				if (c.getConceptName(acc) == null) {
					c.createConceptName(acc, false);
				}
			}
		}
	}

	private void addAccession(ONDEXConcept c, String acc)
			throws DataSourceMissingException {
		DataSource ns = null;
		String nsid = (String) cvMatcher.validate(acc);
		if (nsid != null) {
			ns = requireDataSource(nsid);
		} else {
			String[] split = acc.split(":");
			if (split.length == 2) {
				ns = dataSourceLookup.get(split[0]);
				if (ns == null) {
					logInconsistency("Unknown data source: " + split[0]);
				} else {
					acc = split[0];
				}
			}
		}
		if (ns != null && (c.getConceptAccession(acc, ns) == null)) {
			c.createConceptAccession(acc, ns, false);
		}
	}

	// ##### HELPER METHODS #####

	private void log(String s) {
		GeneralOutputEvent e = new GeneralOutputEvent("\n" + s, "");
		e.setLog4jLevel(Level.INFO);
		fireEventOccurred(e);
	}

	/**
	 * Logs an inconsistency event with message <code>s</code>.
	 * 
	 * @param s
	 *            the message to log as an inconsistency.
	 */
	private void logInconsistency(String s) {
		InconsistencyEvent e = new InconsistencyEvent("\n" + s, "");
		e.setLog4jLevel(Level.DEBUG);
		fireEventOccurred(e);
	}

	private void logError(String s) {
		PluginErrorEvent e = new PluginErrorEvent("\n" + s, "");
		e.setLog4jLevel(Level.ERROR);
		fireEventOccurred(e);
	}

	private enum V1 {
		DB(0), DB_OBJ_ID(1), DB_OBJECT_SYM(2), QUAL(3), GO_ID(4), DB_REF(5), EV_CODE(
				6), WITH_FROM(7), ASPECT(8), DB_OBJ_NAME(9), DB_OBJ_SYNONYM(10), DB_OBJ_TYPE(
				11), TAXON(12), DATE(13), BY(14);

		private int i;

		private V1(int i) {
			this.i = i;
		}

		public int index() {
			return i;
		}
	}

	// ##### OBLIGATORY INFO METHODS #####

	@Override
	public String getName() {
		return "Alternative GOA parser";
	}

	@Override
	public String getVersion() {
		return "13.08.2009";
	}

	@Override
	public String getId() {
		return "goa2";
	}

	@Override
	public String[] requiresValidators() {
		return new String[] { CVREGEX };
	}

}
