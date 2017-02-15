package net.sourceforge.ondex.core.searchable;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.ONDEXEvent;
import net.sourceforge.ondex.event.ONDEXListener;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.AccessDeniedException;

import org.apache.log4j.Level;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

/**
 * This class is the entry point for the indexed ONDEX graph representation. It
 * initialises the LUCENE Index system.
 * 
 * @author taubertj
 * @version 18.07.2011
 */
public class LuceneEnv implements ONDEXLuceneFields {

	private static final String LASTDOCUMENT = "LASTDOCUMENT";

	/**
	 * Collects Bits from a given LUCENE field and adds them to a BitSet.
	 * 
	 * @author hindlem, taubertj
	 */
	private class DocIdCollector extends Collector {

		/**
		 * Collects results
		 */
		private final BitSet bits;

		/**
		 * In case of Multiple-Reader use
		 */
		private int docBase;

		/**
		 * Set IndexReader used to determine size of BitSet.
		 * 
		 * @param indexReader
		 */
		public DocIdCollector(IndexReader indexReader) {
			bits = new BitSet(indexReader.maxDoc());
		}

		/**
		 * accept docs out of order (for a BitSet it doesn't matter)
		 */
		@Override
		public boolean acceptsDocsOutOfOrder() {
			return true;
		}

		@Override
		public void collect(int doc) {
			bits.set(doc + docBase);
		}

		/**
		 * @return the bits returned
		 */
		public BitSet getBits() {
			return bits;
		}

		@Override
		public void setNextReader(IndexReader reader, int docBase) {
			this.docBase = docBase;
		}

		/**
		 * ignore scorer
		 */
		@Override
		public void setScorer(Scorer scorer) {

		}
	}

	/**
	 * Collects Bits from a given LUCENE field and adds them to a
	 * Int2FloatOpenHashMap
	 * 
	 * @author hindlem
	 */
	private class ScoreCollector extends Collector {

		/**
		 * In case of Multiple-Reader use
		 */
		private int docBase;

		/**
		 * Returns associated scores
		 */
		private Scorer scorer;

		/**
		 * Collection score per document
		 */
		private final Map<Integer, Float> docScores;

		/**
		 * Collects results
		 */
		private final BitSet bits;

		/**
		 * Set IndexReader used to determine size of BitSet.
		 * 
		 * @param indexReader
		 */
		public ScoreCollector(IndexReader indexReader) {
			bits = new BitSet(indexReader.maxDoc());
			docScores = new HashMap<Integer, Float>(indexReader.maxDoc());
		}

		/**
		 * accept docs out of order (for a Map it doesn't matter)
		 */
		@Override
		public boolean acceptsDocsOutOfOrder() {
			return true;
		}

		@Override
		public void collect(int doc) throws IOException {
			bits.set(doc + docBase);
			docScores.put(doc + docBase, scorer.score());
		}

		/**
		 * @return the bits returned
		 */
		public BitSet getBits() {
			return bits;
		}

		/**
		 * @return doc id to scores
		 */
		public Map<Integer, Float> getScores() {
			return docScores;
		}

		@Override
		public void setNextReader(IndexReader indexReader, int docBase) {
			this.docBase = docBase;
		}

		@Override
		public void setScorer(Scorer scorer) {
			this.scorer = scorer;
		}
	}

	/**
	 * global analyser used for the index
	 */
	public final static Analyzer DEFAULTANALYZER = new DefaultONDEXLuceneAnalyser();

	/**
	 * remove double spaces
	 */
	private final static Pattern DOUBLESPACECHARS = Pattern.compile("\\s{2,}");

	/**
	 * empty BitSet for empty results
	 */
	private final static BitSet EMPTYBITSET = new BitSet(0);

	private final static Map<Integer, Float> EMPTYSCOREMAP = new HashMap<Integer, Float>(
			0);

	/**
	 * threaded indexing of graph
	 */
	public static ExecutorService EXECUTOR;

	/**
	 * Allows only the id of a document to be loaded
	 */
	private static FieldSelector idSelector = new FieldSelector() {

		private static final long serialVersionUID = 1L;

		@Override
		public FieldSelectorResult accept(String arg0) {
			if (arg0.equals(CONID_FIELD) || arg0.equals(RELID_FIELD)) {
				return FieldSelectorResult.LOAD_AND_BREAK;
			} else {
				return FieldSelectorResult.NO_LOAD;
			}
		}
	};

	// pre-compiled patterns for text stripping
	private final static Pattern patternNonWordChars = Pattern.compile("\\W");

	/**
	 * LUCENE RAM buffer size in MB
	 */
	private final static double RAMBufferSizeMB = 128;

	/**
	 * Strips text to be inserted into the index.
	 * 
	 * @param text
	 *            String
	 * @return String
	 */
	public static String stripText(String text) {

		// trim and lower case
		text = text.trim().toLowerCase();

		// replace all non-word characters with space
		text = patternNonWordChars.matcher(text).replaceAll(SPACE);

		// replace double spaces by single ones
		text = DOUBLESPACECHARS.matcher(text).replaceAll(SPACE);

		return text;
	}

	/**
	 * whether or not to create new index
	 */
	private boolean create;

	/**
	 * directory containing index
	 */
	private Directory directory;

	/**
	 * LUCENE index writer
	 */
	private IndexWriter im;

	/**
	 * directory for index
	 */
	private String indexdir = "";

	/**
	 * whether or not the index is still open
	 */
	private boolean indexWriterIsOpen = false;

	/**
	 * index searcher
	 */
	private IndexSearcher is;
	
	/**
	 * index reader
	 */
	private IndexReader ir;

	/**
	 * contains all registered listeners
	 */
	private final Set<ONDEXListener> listeners = new HashSet<ONDEXListener>();

	/**
	 * wrapped LUCENE ONDEX graph
	 */
	private LuceneONDEXGraph og = null;

	// contains all used DataSources for concept accessions
	protected Set<String> listOfConceptAccDataSources = new HashSet<String>();

	// contains all used attribute names for concepts
	protected Set<String> listOfConceptAttrNames = new HashSet<String>();

	// contains all used attribute names for relations
	protected Set<String> listOfRelationAttrNames = new HashSet<String>();

	/**
	 * Constructor which initialises an empty LuceneEnv.
	 * 
	 * @param indexdir
	 *            String
	 * @param create
	 *            boolean
	 */
	public LuceneEnv(String indexdir, boolean create) {
		this.indexdir = indexdir;
		this.create = create;

		// make sure only one EXECUTOR is started
		if (EXECUTOR == null) {
			EXECUTOR = Executors.newCachedThreadPool();
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					if (EXECUTOR != null)
						EXECUTOR.shutdownNow();
				}
			});
		}

		if (create)
			new File(indexdir).mkdirs();

		try {
			// open a Directory for the index
			directory = FSDirectory.open(new File(indexdir));
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					"[LuceneEnv - constructor]"));
		}
	}

	/**
	 * Adds a ONDEX listener to the list.
	 * 
	 * @param l
	 *            ONDEXListener
	 */
	public void addONDEXListener(ONDEXListener l) {
		listeners.add(l);
	}

	/**
	 * Close all open index handles.
	 */
	public void cleanup() {
		try {
			if (im != null)
				im.close();
			if (is != null)
				is.close();
			if (ir != null)
				ir.close();
			if (directory != null)
				directory.close();
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					"[LuceneEnv - cleanup]"));
		}
	}

	/**
	 * Close a potentially open index.
	 */
	public void closeIndex() {
		try {
			// check if index open for writing
			if (!indexWriterIsOpen)
				return;

			// add last document to index
			addMetadataToIndex();

			im.prepareCommit();
			im.commit();
			im.close();
			indexWriterIsOpen = false;
		} catch (CorruptIndexException cie) {
			fireEventOccurred(new DataFileErrorEvent(cie.getMessage(),
					"[LucenceEnv - closeIndex]"));
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					"[LucenceEnv - closeIndex]"));
		}

	}

	/**
	 * @param cid
	 *            the concept id to check for
	 * @return if one or more indexes of this concept exist in the index
	 */
	public boolean conceptExistsInIndex(int cid) {
		DocIdCollector collector = new DocIdCollector(is.getIndexReader());
		try {
			is.search(
					new TermQuery(new Term(CONID_FIELD, String.valueOf(cid))),
					collector);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (collector.getBits().length() > 0);
	}

	/**
	 * Typical usage get the number of Publications (Concept) with Abstracts
	 * (Attribute) that contain the word "regulates". Return the number of
	 * Concepts that contain this word.
	 * 
	 * @param an
	 *            the Attribute attribute to search within
	 * @param word
	 *            the word/term to search for
	 * @return int
	 */
	public int getFrequenceyOfWordInConceptAttribute(AttributeName an,
			String word) {

		String fieldname = CONATTRIBUTE_FIELD + DELIM + an.getId();
		Term term = new Term(fieldname, word);
		try {
			return is.docFreq(term);
		} catch (IOException e) {
			fireEventOccurred(new DataFileErrorEvent(e.getMessage(),
					"[LuceneEnv - getFrequenceyOfWordInConceptAttribute]"));
		}
		return 0;

	}

	/**
	 * Faster method than getFrequenceyOfWordInConceptAttribute(AttributeName
	 * an, String word) as calls to IO are less (-: Typical usage get the number
	 * of Publications (Concept) with Abstracts (Attribute) that contain the
	 * word "regulates". Returns the number of Concepts that contain this word.
	 * 
	 * @param an
	 *            the Attribute attribute to search within
	 * @param word
	 *            the word/term to search for
	 * @return int[]
	 */
	public int[] getFrequenceyOfWordInConceptAttribute(AttributeName an,
			String[] word) {

		String fieldname = CONATTRIBUTE_FIELD + DELIM + an.getId();

		try {
			int[] freqs = new int[word.length];
			for (int i = 0; i < word.length; i++) {
				freqs[i] = is.docFreq(new Term(fieldname, word[i]));
			}

			// Returns the number of documents containing the terms.
			return freqs;
		} catch (IOException e) {
			fireEventOccurred(new DataFileErrorEvent(e.getMessage(),
					"[LuceneEnv - getFrequenceyOfWordInConceptAttribute]"));
		}
		return new int[0];
	}

	/**
	 * Returns the number of Relations that contain this word.
	 * 
	 * @param an
	 *            the Attribute attribute to search within
	 * @param word
	 *            the word/term to search for
	 * @return int
	 */
	public int getFrequenceyOfWordInRelationAttribute(AttributeName an,
			String word) {

		String fieldname = RELATTRIBUTE_FIELD + DELIM + an.getId();
		Term term = new Term(fieldname, word);
		try {
			// Returns the number of documents containing the term.
			return is.docFreq(term);
		} catch (IOException e) {
			fireEventOccurred(new DataFileErrorEvent(e.getMessage(),
					"[LuceneEnv - getFrequenceyOfWordInRelationAttribute]"));
		}
		return 0;

	}

	/**
	 * Returns the number of Relations that contain these words.
	 * 
	 * @param an
	 *            the Attribute attribute to search within
	 * @param word
	 *            the word/term to search for
	 * @return int[]
	 */
	public int[] getFrequenceyOfWordInRelationAttribute(AttributeName an,
			String[] word) {

		String fieldname = RELATTRIBUTE_FIELD + DELIM + an.getId();

		try {
			int[] freqs = new int[word.length];
			for (int i = 0; i < word.length; i++) {
				freqs[i] = is.docFreq(new Term(fieldname, word[i]));
			}

			// Returns the number of documents containing the terms.
			return freqs;
		} catch (IOException e) {
			fireEventOccurred(new DataFileErrorEvent(e.getMessage(),
					"[LuceneEnv - getFrequenceyOfWordInRelationAttribute]"));
		}
		return new int[0];
	}

	public Set<String> getListOfConceptAccDataSources() {
		return listOfConceptAccDataSources;
	}

	public Set<String> getListOfConceptAttrNames() {
		return listOfConceptAttrNames;
	}

	public Set<String> getListOfRelationAttrNames() {
		return listOfRelationAttrNames;
	}

	/**
	 * Returns the actual LuceneONDEXGraph as an AbstractONDEXGraph.
	 * 
	 * @return AbstractONDEXGraph
	 */
	public ONDEXGraph getONDEXGraph() {
		return this.og;
	}

	/**
	 * Returns the list of ONDEX listener listeners.
	 * 
	 * @return ONDEXListener[]
	 */
	public ONDEXListener[] getONDEXListeners() {
		return listeners.toArray(new ONDEXListener[listeners.size()]);
	}

	/**
	 * Open index for writing.
	 */
	public void openIndex() {
		// open index modifier to write to index
		try {

			if (indexWriterIsOpen)
				closeIndex();
			is.close();
			ir.close();

			IndexWriterConfig writerConfig = new IndexWriterConfig(
					Version.LUCENE_36, DEFAULTANALYZER);
			writerConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
			// set RAM buffer, hopefully speeds up things
			writerConfig.setRAMBufferSizeMB(RAMBufferSizeMB);

			im = new IndexWriter(directory, writerConfig);
			indexWriterIsOpen = true;

			// deletes the last record that has attribute names,
			// that will have to be rebuilt
			im.deleteDocuments(new Term(LASTDOCUMENT, "true"));
			System.out.println("Lucene Metadata delete: " + im.hasDeletions());
			im.commit();
		} catch (CorruptIndexException cie) {
			fireEventOccurred(new DataFileErrorEvent(cie.getMessage(),
					"[LucenceEnv - openIndex]"));
		} catch (LockObtainFailedException lofe) {
			fireEventOccurred(new DataFileErrorEvent(lofe.getMessage(),
					"[LucenceEnv - openIndex]"));
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					"[LucenceEnv - openIndex]"));
		}
	}

	/**
	 * @param rid
	 *            the relation id to check for
	 * @return if one or more indexes of this concept exist in the index
	 */
	public boolean relationExistsInIndex(int rid) {
		DocIdCollector collector = new DocIdCollector(is.getIndexReader());
		try {
			is.search(
					new TermQuery(new Term(RELID_FIELD, String.valueOf(rid))),
					collector);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (collector.getBits().length() > 0);
	}

	/**
	 * Removes the selected concept from the index NB this is an expensive
	 * operation where possible group deletes together and @see
	 * removeConceptsFromIndex(int[] cids)
	 * 
	 * @param cid
	 *            the conceptId to remove from the index
	 * @return sucess?
	 */
	public boolean removeConceptFromIndex(int cid) {
		return removeConceptsFromIndex(new int[] { cid });
	}

	/**
	 * Removes the selected concepts from the index
	 * 
	 * @param cids
	 *            the conceptIds to remove from the index
	 * @return the number of concepts removed
	 */
	public boolean removeConceptsFromIndex(int[] cids) {
		boolean success = false;
		openIndex();
		try {
			Query[] terms = new Query[cids.length];
			for (int i = 0; i < terms.length; i++) {
				terms[i] = new TermQuery(new Term(CONID_FIELD,
						String.valueOf(cids[i])));
				System.out.println(terms[i].toString());
			}

			try {
				im.deleteDocuments(terms);
				success = im.hasDeletions();
			} catch (CorruptIndexException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			closeIndex();
			try {
				ir = IndexReader.open(directory);
				is = new IndexSearcher(ir);
			} catch (CorruptIndexException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return success;
	}

	/**
	 * Removes a ONDEX listener listener from the list.
	 * 
	 * @param l
	 *            ONDEXListener
	 */
	public void removeONDEXListener(ONDEXListener l) {
		listeners.remove(l);
	}

	/**
	 * Removes the selected relation from the index NB this is an expensive
	 * operation where possible group deletes together and @see
	 * removeRelationsFromIndex(int[] rids)
	 * 
	 * @param rid
	 *            the relationId to remove from the index
	 * @return success?
	 */
	public boolean removeRelationFromIndex(int rid) {
		return removeRelationsFromIndex(new int[] { rid });
	}

	/**
	 * Removes the selected relations from the index
	 * 
	 * @param rids
	 *            the relationIds to remove from the index
	 * @return the number of relations removed
	 */
	public boolean removeRelationsFromIndex(int[] rids) {
		boolean success = false;
		openIndex();
		try {
			Term[] terms = new Term[rids.length];
			for (int i = 0; i < terms.length; i++) {
				terms[i] = new Term(RELID_FIELD, String.valueOf(rids[i]));
			}

			try {
				im.deleteDocuments(terms);
				success = im.hasDeletions();
			} catch (CorruptIndexException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			closeIndex();
			try {
				is.close();
				ir.close();
				ir = IndexReader.open(directory);
				is = new IndexSearcher(ir);
			} catch (CorruptIndexException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return success;
	}

	/**
	 * Searches in Concepts and returns found Concepts.
	 * 
	 * @param q
	 *            Query
	 * @return ScoredHits<ONDEXConcept>
	 */
	public ScoredHits<ONDEXConcept> scoredSearchInConcepts(Query q) {
		try {
			ScoreCollector collector = new ScoreCollector(is.getIndexReader());
			is.search(q, collector);

			Set<ONDEXConcept> view;
			Map<Integer, Float> doc2Scores = collector.getScores();
			Map<Integer, Float> cid2scores = new HashMap<Integer, Float>();
			BitSet bs = collector.getBits();
			if (bs.length() > 0) {
				BitSet set = new BitSet(bs.length());
				// iterator of document indices
				for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
					Document document = is.doc(i, idSelector);
					float score = doc2Scores.get(i);
					Fieldable cid = document.getFieldable(CONID_FIELD);
					int conceptId = Integer.valueOf(cid.stringValue());
					cid2scores.put(conceptId, score);
					set.set(conceptId);
				}
				view = BitSetFunctions.create(og, ONDEXConcept.class, set);
				return new ScoredHits<ONDEXConcept>(view, cid2scores);
			} else {
				view = BitSetFunctions.create(og, ONDEXConcept.class,
						EMPTYBITSET);
				return new ScoredHits<ONDEXConcept>(view, EMPTYSCOREMAP);
			}
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					"[LuceneEnv - searchInConcepts]"));
		}
		return null;
	}

	/**
	 * Searches in Relations and returns found Relations.
	 * 
	 * @param q
	 *            Query
	 * @return ScoredHits<ONDEXRelation>
	 */
	public ScoredHits<ONDEXRelation> scoredSearchInRelations(Query q) {
		try {
			ScoreCollector collector = new ScoreCollector(is.getIndexReader());
			is.search(q, collector);

			Set<ONDEXRelation> view;
			Map<Integer, Float> doc2Scores = collector.getScores();
			Map<Integer, Float> rid2scores = new HashMap<Integer, Float>();
			BitSet bs = collector.getBits();
			if (bs.length() > 0) {
				BitSet set = new BitSet(bs.length());
				// iterator of document indices
				for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
					Document document = is.doc(i, idSelector);
					float score = doc2Scores.get(i);
					Fieldable rid = document.getFieldable(RELID_FIELD);
					int relationId = Integer.valueOf(rid.stringValue());
					rid2scores.put(relationId, score);
					set.set(relationId);
				}
				view = BitSetFunctions.create(og, ONDEXRelation.class, set);
				return new ScoredHits<ONDEXRelation>(view, rid2scores);
			} else {
				view = BitSetFunctions.create(og, ONDEXRelation.class,
						EMPTYBITSET);
				return new ScoredHits<ONDEXRelation>(view, EMPTYSCOREMAP);
			}
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					"[LuceneEnv - searchInConcepts]"));
		}
		return null;
	}

	/**
	 * Searches in Concepts and returns found Concepts.
	 * 
	 * @param q
	 *            Query
	 * @return Set<ONDEXConcept>
	 */
	public Set<ONDEXConcept> searchInConcepts(Query q) {
		try {
			DocIdCollector collector = new DocIdCollector(is.getIndexReader());
			is.search(q, collector);

			BitSet bs = collector.getBits();
			if (bs.length() > 0) {
				BitSet set = new BitSet(bs.length());
				// iterator of document indices
				for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
					// retrieve associated document
					Document document = is.doc(i, idSelector);
					// get concept ID from document
					Fieldable cid = document.getFieldable(CONID_FIELD);
					set.set(Integer.valueOf(cid.stringValue()));
				}
				return BitSetFunctions.create(og, ONDEXConcept.class, set);
			} else {
				return BitSetFunctions.create(og, ONDEXConcept.class,
						EMPTYBITSET);
			}
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					"[LuceneEnv - searchInConcepts]"));
		}
		return null;
	}

	/**
	 * Searches in Relations and returns found Relations.
	 * 
	 * @param q
	 *            Query
	 * @return Set<ONDEXRelation>
	 */
	public Set<ONDEXRelation> searchInRelations(Query q) {
		try {
			DocIdCollector collector = new DocIdCollector(is.getIndexReader());
			is.search(q, collector);

			BitSet bs = collector.getBits();
			if (bs.length() > 0) {
				BitSet set = new BitSet(bs.length());
				// iterator of document indices
				for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
					// retrieve associated document
					Document document = is.doc(i, idSelector);
					// get relation ID from document
					Fieldable rid = document.getFieldable(RELID_FIELD);
					set.set(Integer.valueOf(rid.stringValue()));
				}
				return BitSetFunctions.create(og, ONDEXRelation.class, set);
			} else {
				return BitSetFunctions.create(og, ONDEXRelation.class,
						EMPTYBITSET);
			}
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					"[LuceneEnv - searchInRelations]"));
		}
		return null;
	}

	/**
	 * Searches the top n hits for query in Concepts.
	 * 
	 * @param q
	 *            Query
	 * @param n
	 *            int
	 * @return ScoredHits<ONDEXConcept>
	 */
	public ScoredHits<ONDEXConcept> searchTopConcepts(Query q, int n) {

		try {
			final BitSet bits = new BitSet();
			TopDocs hits = is.search(q, null, n);
			Map<Integer, Float> scores = new HashMap<Integer, Float>();
			for (int i = 0; i < hits.scoreDocs.length; i++) {
				int docId = hits.scoreDocs[i].doc;
				float score = hits.scoreDocs[i].score;
				Document document = is.doc(docId, idSelector);
				Fieldable cid = document.getFieldable(CONID_FIELD);
				if (cid != null) {
					int id = Integer.parseInt(cid.stringValue());
					bits.set(id);
					scores.put(id, score);
				}
			}

			Set<ONDEXConcept> view = BitSetFunctions.create(og,
					ONDEXConcept.class, bits);
			return new ScoredHits<ONDEXConcept>(view, scores);
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					"[LuceneEnv - searchTopConcepts]"));
		}
		return null;
	}

	/**
	 * Searches the top n hits for query in Relations.
	 * 
	 * @param q
	 *            Query
	 * @param n
	 *            int
	 * @return ScoredHits<ONDEXRelation>
	 */
	public ScoredHits<ONDEXRelation> searchTopRelations(Query q, int n) {

		try {
			BitSet bits = new BitSet();
			TopDocs hits = is.search(q, null, n);
			Map<Integer, Float> scores = new HashMap<Integer, Float>();
			for (int i = 0; i < hits.scoreDocs.length; i++) {
				int docId = hits.scoreDocs[i].doc;
				float score = hits.scoreDocs[i].score;
				Document document = is.doc(docId, idSelector);
				Fieldable rid = document.getFieldable(RELID_FIELD);
				if (rid != null) {
					int id = Integer.parseInt(rid.stringValue());
					bits.set(id);
					scores.put(id, score);
				}
			}

			Set<ONDEXRelation> view = BitSetFunctions.create(og,
					ONDEXRelation.class, bits);
			return new ScoredHits<ONDEXRelation>(view, scores);

		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					"[LuceneEnv - searchTopRelations]"));
		}
		return null;
	}

	/**
	 * Takes a given AbstractONDEXGraph and builds the index around it.
	 * 
	 * @param aog
	 *            AbstractONDEXGraph
	 * @throws AccessDeniedException
	 */
	public void setONDEXGraph(ONDEXGraph aog) throws AccessDeniedException {

		GeneralOutputEvent so = new GeneralOutputEvent(
				"Using Lucene with index dir: " + this.indexdir,
				"[LuceneEnv - setONDEXGraph]");
		so.setLog4jLevel(Level.INFO); // todo: fix this - should be configured
										// externally
		fireEventOccurred(so);

		// start indexing
		indexONDEXGraph(aog, create);

		// make the ONDEXGraph immutable
		og = new LuceneONDEXGraph(aog);
	}

	/**
	 * Updates or adds new concepts to the index
	 * 
	 * @param concepts
	 *            the relations to add to the index
	 * @throws AccessDeniedException
	 */
	public void updateConceptsToIndex(Set<ONDEXConcept> concepts)
			throws AccessDeniedException {
		openIndex();
		try {
			for (ONDEXConcept concept : concepts) {
				// try a delete this is quicker than reopening the "is"
				try {
					im.deleteDocuments(new Term(CONID_FIELD, String
							.valueOf(concept.getId())));
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				addConceptToIndex(concept);
			}
		} finally {
			closeIndex();
			try {
				ir = IndexReader.open(directory);
				is = new IndexSearcher(ir);
			} catch (CorruptIndexException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Update or add a concept to the index NB this is an expensive operations,
	 * so try to use the batch job @see updateConceptsToIndex(Set<ONDEXConcept>
	 * concepts)
	 * 
	 * @param concept
	 *            the concept to add to the index
	 * @throws AccessDeniedException
	 */
	public void updateConceptToIndex(ONDEXConcept concept)
			throws AccessDeniedException {
		boolean exists = conceptExistsInIndex(concept.getId());
		openIndex();
		try {
			if (exists) {
				try {
					im.deleteDocuments(new Term(CONID_FIELD, String
							.valueOf(concept.getId())));
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			addConceptToIndex(concept);
		} finally {
			closeIndex();
			try {
				ir = IndexReader.open(directory);
				is = new IndexSearcher(ir);
			} catch (CorruptIndexException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Updates or adds new relations to the index
	 * 
	 * @param relations
	 *            the relations to add to the index
	 * @throws AccessDeniedException
	 */
	public void updateRelationsToIndex(Set<ONDEXRelation> relations)
			throws AccessDeniedException {
		openIndex();
		try {
			for (ONDEXRelation relation : relations) {
				try {
					im.deleteDocuments(new Term(RELID_FIELD, String
							.valueOf(relation.getId())));
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				addRelationToIndex(relation);
			}
		} finally {
			closeIndex();
			try {
				ir = IndexReader.open(directory);
				is = new IndexSearcher(ir);
			} catch (CorruptIndexException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Updates or adds a relations to an Index NB this is an expensive
	 * operations, so try to use the batch job @see
	 * updateRelationsToIndex(Set<ONDEXRelation> relations)
	 * 
	 * @param relation
	 *            ondex relation to add to the index
	 * @throws AccessDeniedException
	 */
	public void updateRelationToIndex(ONDEXRelation relation)
			throws AccessDeniedException {
		boolean exists = relationExistsInIndex(relation.getId());
		openIndex();
		try {
			if (exists) {
				try {
					im.deleteDocuments(new Term(RELID_FIELD, String
							.valueOf(relation.getId())));
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			addRelationToIndex(relation);
		} finally {
			closeIndex();
			try {
				ir = IndexReader.open(directory);
				is = new IndexSearcher(ir);
			} catch (CorruptIndexException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Add the given ONDEXConcept to the current index.
	 * 
	 * @param c
	 *            ONDEXConcept to add to index
	 * @throws AccessDeniedException
	 */
	private void addConceptToIndex(ONDEXConcept c) throws AccessDeniedException {
		// ensures duplicates are not written to the Index
		Set<String> cacheSet = new HashSet<String>(100);

		// get textual properties
		String conceptID = String.valueOf(c.getId());
		String parserID = c.getPID();
		String annotation = c.getAnnotation();
		String description = c.getDescription();

		// get all properties iterators
		Set<ConceptAccession> it_ca = c.getConceptAccessions();
		if (it_ca.size() == 0) {
			it_ca = null;
		}
		Set<ConceptName> it_cn = c.getConceptNames();
		if (it_cn.size() == 0) {
			it_cn = null;
		}
		Set<Attribute> it_attribute = c.getAttributes();
		if (it_attribute.size() == 0) {
			it_attribute = null;
		}

		// leave if there are no properties
		if (it_ca == null && it_cn == null && it_attribute == null
				&& annotation.length() == 0 && description.length() == 0) {
			return; // there is nothing to index, no document should be created!
		}

		// create a new document for each concept and sets fields
		Document doc = new Document();

		doc.add(new Field(CONID_FIELD, conceptID, Field.Store.YES,
				Field.Index.NOT_ANALYZED_NO_NORMS));
		doc.add(new Field(PID_FIELD, parserID, Field.Store.YES, Field.Index.NO));
		doc.add(new Field(CC_FIELD, c.getOfType().getId(), Field.Store.YES,
				Field.Index.NOT_ANALYZED_NO_NORMS));
		doc.add(new Field(DataSource_FIELD, c.getElementOf().getId(),
				Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));

		if (annotation.length() > 0)
			doc.add(new Field(ANNO_FIELD, LuceneEnv.stripText(annotation),
					Store.YES, Field.Index.ANALYZED));

		if (description.length() > 0)
			doc.add(new Field(DESC_FIELD, LuceneEnv.stripText(description),
					Store.YES, Field.Index.ANALYZED));

		// start concept accession handling
		if (it_ca != null) {

			// add all concept accessions for this concept
			for (ConceptAccession ca : it_ca) {
				String accession = ca.getAccession();
				String elementOf = ca.getElementOf().getId();
				Boolean isAmbiguous = ca.isAmbiguous();
				listOfConceptAccDataSources.add(elementOf);

				String id = CONACC_FIELD + DELIM + elementOf;

				if (isAmbiguous) {
					id = id + DELIM + AMBIGUOUS;
				}
				// concept accessions should not be ANALYZED?
				doc.add(new Field(id, LuceneEnv.stripText(accession),
						Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
			}
		}

		// start concept name handling
		if (it_cn != null) {

			// add all concept names for this concept
			for (ConceptName cn : it_cn) {
				String name = cn.getName();
				cacheSet.add(LuceneEnv.stripText(name));
			}

			// exclude completely equal concept names from being
			// represented twice
			for (String aCacheSet : cacheSet) {
				doc.add(new Field(CONNAME_FIELD, aCacheSet, Store.YES,
						Field.Index.ANALYZED));
			}
			cacheSet.clear();
		}

		// start concept gds processing
		if (it_attribute != null) {

			// mapping attribute name to gds value
			Map<String, String> attrNames = new HashMap<String, String>();

			// add all concept gds for this concept
			for (Attribute attribute : it_attribute) {
				if (attribute.isDoIndex()) {
					String name = attribute.getOfType().getId();
					listOfConceptAttrNames.add(name);
					String value = attribute.getValue().toString();
					attrNames.put(name, LuceneEnv.stripText(value));
				}
			}

			// write attribute name specific Attribute fields
			for (String name : attrNames.keySet()) {
				String value = attrNames.get(name);

				doc.add(new Field(CONATTRIBUTE_FIELD + DELIM + name, value,
						Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
			}
			attrNames.clear();
		}

		// store document to index
		try {
			im.addDocument(doc);
		} catch (CorruptIndexException cie) {
			fireEventOccurred(new DataFileErrorEvent(cie.getMessage(),
					"[LuceneEnv - addConceptToIndex]"));
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					"[LuceneEnv - addConceptToIndex]"));
		}
	}

	/**
	 * Adds sets of used meta data to the index.
	 */
	private void addMetadataToIndex() {

		// new document for fields
		Document doc = new Document();
		doc.add(new Field(LASTDOCUMENT, "true", Field.Store.YES, Field.Index.NO));
		for (String name : listOfConceptAttrNames)
			doc.add(new Field(CONATTRIBUTE_FIELD + DELIM + name, name,
					Field.Store.YES, Field.Index.NO));
		for (String name : listOfRelationAttrNames)
			doc.add(new Field(RELATTRIBUTE_FIELD + DELIM + name, name,
					Field.Store.YES, Field.Index.NO));
		for (String elementOf : listOfConceptAccDataSources)
			doc.add(new Field(CONACC_FIELD + DELIM + elementOf, elementOf,
					Field.Store.YES, Field.Index.NO));

		// add last document
		try {
			im.addDocument(doc);
		} catch (CorruptIndexException cie) {
			fireEventOccurred(new DataFileErrorEvent(cie.getMessage(),
					"[LuceneEnv - addMetadataToIndex]"));
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					"[LuceneEnv - addMetadataToIndex]"));
		}
	}

	/**
	 * Add the given ONDEXRelation to the current index.
	 * 
	 * @param r
	 *            ONDEXRelation to add to index
	 * @throws AccessDeniedException
	 */
	private void addRelationToIndex(ONDEXRelation r)
			throws AccessDeniedException {

		// get Relation and RelationAttributes
		Set<Attribute> it_attribute = r.getAttributes();

		// leave if there is nothing to index
		if (it_attribute.size() == 0) {
			return;
		}

		// create a Document for each relation and store ids
		Document doc = new Document();

		doc.add(new Field(RELID_FIELD, String.valueOf(r.getId()),
				Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
		doc.add(new Field(FROM_FIELD, String
				.valueOf(r.getFromConcept().getId()), Field.Store.YES,
				Field.Index.NO));
		doc.add(new Field(TO_FIELD, String.valueOf(r.getToConcept().getId()),
				Field.Store.YES, Field.Index.NO));

		doc.add(new Field(OFTYPE_FIELD, r.getOfType().getId(), Field.Store.YES,
				Field.Index.NOT_ANALYZED_NO_NORMS));

		// mapping attribute name to gds value
		Map<String, String> attrNames = new HashMap<String, String>();

		// add all relation gds for this relation
		for (Attribute attribute : it_attribute) {
			if (attribute.isDoIndex()) {
				String name = attribute.getOfType().getId();
				listOfRelationAttrNames.add(name);
				String value = attribute.getValue().toString();
				attrNames.put(name, LuceneEnv.stripText(value));
			}
		}

		// write attribute name specific Attribute fields
		for (String name : attrNames.keySet()) {
			String value = attrNames.get(name);

			doc.add(new Field(RELATTRIBUTE_FIELD + DELIM + name, value,
					Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
		}
		attrNames.clear();

		// store document to index
		try {
			im.addDocument(doc);
		} catch (CorruptIndexException cie) {
			fireEventOccurred(new DataFileErrorEvent(cie.getMessage(),
					"[LuceneEnv - addRelationToIndex]"));
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					"[LuceneEnv - addRelationToIndex]"));
		}
	}

	/**
	 * Builds the index for a given ONDEXGraph.
	 * 
	 * @param graph
	 *            ONDEXGraph
	 * @param create
	 *            boolean
	 * @throws AccessDeniedException
	 */
	private void indexONDEXGraph(final ONDEXGraph graph, boolean create)
			throws AccessDeniedException {

		fireEventOccurred(new GeneralOutputEvent(
				"Starting the Lucene environment.",
				"[LuceneEnv - indexONDEXGraph]"));

		try {

			// if index is new created, fill index
			if (create) {

				// open index modifier to write to index
				if (indexWriterIsOpen)
					closeIndex();

				IndexWriterConfig writerConfig = new IndexWriterConfig(
						Version.LUCENE_36, DEFAULTANALYZER);
				writerConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
				// set RAM buffer, hopefully speeds up things
				writerConfig.setRAMBufferSizeMB(RAMBufferSizeMB);

				im = new IndexWriter(directory, writerConfig);
				indexWriterIsOpen = true;

				// INDEX CONCEPTS
				final Set<ONDEXConcept> it_c = graph.getConcepts();
				Future<?> cF = EXECUTOR.submit(new Runnable() {
					@Override
					public void run() {
						for (ONDEXConcept c : it_c) {
							addConceptToIndex(c);
						}
					}
				});

				final Set<ONDEXRelation> it_r = graph.getRelations();
				Future<?> rF = EXECUTOR.submit(new Runnable() {
					@Override
					public void run() {
						for (ONDEXRelation r : it_r) {
							addRelationToIndex(r);
						}
					}
				});

				try {
					cF.get();
					rF.get();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} catch (ExecutionException e) {
					throw new RuntimeException(e);
				}

				// last Document contains meta data lists
				addMetadataToIndex();

				im.prepareCommit();
				im.commit();
				im.close();
				indexWriterIsOpen = false;
			}

			// open index read-only for searching
			ir = IndexReader.open(directory);
			is = new IndexSearcher(ir);

			if (!create) {
				// read in attribute names and accession DataSources
				Document doc = is.doc(is.maxDoc() - 1);
				for (Object o : doc.getFields()) {
					Field field = (Field) o;
					String name = field.name();
					if (name.startsWith(CONATTRIBUTE_FIELD + DELIM)) {
						listOfConceptAttrNames.add(field.stringValue());
					} else if (name.startsWith(RELATTRIBUTE_FIELD + DELIM)) {
						listOfRelationAttrNames.add(field.stringValue());
					} else if (name.startsWith(CONACC_FIELD + DELIM)) {
						listOfConceptAccDataSources.add(field.stringValue());
					}
				}
			}
		} catch (IOException ioe) {
			fireEventOccurred(new DataFileErrorEvent(ioe.getMessage(),
					"[LucenceEnv - indexONDEXGraph]"));
		}
	}

	/**
	 * Notify all listeners that have registered with this class.
	 * 
	 * @param e
	 *            type of event
	 */
	protected void fireEventOccurred(EventType e) {

		if (listeners.size() > 0) {
			// new ondex event
			ONDEXEvent oe = new ONDEXEvent(this, e);
			// notify all listeners

			for (ONDEXListener listener : listeners) {
				listener.eventOccurred(oe);
			}
		}
	}
}
