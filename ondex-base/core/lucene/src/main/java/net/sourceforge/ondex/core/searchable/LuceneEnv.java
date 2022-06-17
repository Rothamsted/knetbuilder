package net.sourceforge.ondex.core.searchable;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.machinezoo.noexception.Exceptions;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.base.util.BitSetFunctions;
import net.sourceforge.ondex.event.ONDEXEvent;
import net.sourceforge.ondex.event.ONDEXListener;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.EventType.Level;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import uk.ac.ebi.utils.exceptions.ExceptionUtils;
import uk.ac.ebi.utils.runcontrol.PercentProgressLogger;

/**
 * <p>This class idxSearcher the entry point for the indexed ONDEX graph representation. It
 * initialises the LUCENE Index system.</p>
 *
 * <p><b>WARNING</b>: this class <b>is not completely thread-safe</b>. In particular, {@link #setONDEXGraph(ONDEXGraph)}, 
 * which creates a new index or opens an existing one, should be run in a single-thread mode and then the 
 * read-only operations (searches and alike) can proceed in multi-thread mode. TODO: review and tidy up the 
 * multi-threading.</p> 
 * 
 * @author taubertj, reviewed in 2017, 2019 by brandizi (mainly to migrate to Lucene 6.x)
 */
public class LuceneEnv implements ONDEXLuceneFields 
{

	/**
	 * Collects Bits from a given LUCENE field and adds them to a BitSet.
	 * 
	 * Marco Brandizi: these collectors were adapted from Lucene 3.6 to 6, maybe we don't need collectors and
	 * simply search/check idxSearcher enough.
	 * 
	 * See Lucene docs for details.
	 * 
	 * @author hindlem, taubertj
	 */
	private class DocIdCollector extends SimpleCollector 
	{
		private final BitSet bits;
		protected int docBase;

		public DocIdCollector ( IndexReader indexReader ) {
			bits = new BitSet ( indexReader.maxDoc() );
		}


		@Override
		public void collect ( int doc ) {
			bits.set ( doc + docBase );
		}

		public BitSet getBits() {
			return bits;
		}
		
		@Override
		protected void doSetNextReader ( LeafReaderContext context ) throws IOException
		{
			this.docBase = context.docBase;
		}

		@Override
		public boolean needsScores () {
			return false;
		}
	}

	/**
	 * Collects documents and their scores from a search.
	 * 
	 * @see DocIdCollector
	 * 
	 * @author hindlem, brandizi
	 */
	private class ScoreCollector extends DocIdCollector 
	{
		private Scorer scorer;

		/**
		 * Collects scores for each doc.
		 */
		private final Map<Integer, Float> docScores;

		public ScoreCollector ( IndexReader indexReader ) 
		{
			super ( indexReader );
			docScores = new HashMap<> ( indexReader.maxDoc() );
		}

		@Override
		public void collect ( int doc )
		{
			try {
				super.collect ( doc );
				docScores.put ( doc + docBase, scorer.score() );
			}
			catch ( IOException ex ) {
				throw new UncheckedIOException ( "Internal error while working with Lucene: " + ex.getMessage (), ex );
			}
		}

		/**
		 * @return a map going from do IDs to scores.
		 */
		public Map<Integer, Float> getScores() {
			return docScores;
		}

		@Override
		public void setScorer ( Scorer scorer ) {
			this.scorer = scorer;
		}
		
		@Override
		public boolean needsScores () {
			return true;
		}		
	}
	
	/**
	 * true if the class was instantiated with the instruction to create/replace a new index.
	 */
	private boolean createNewIndex;

	private boolean isReadOnlyMode = false;
	
	
	/**
	 * The index directory handler required by Lucene 
	 */
	private Directory idxDirectory;

	/**
	 * Lucene index writer
	 */
	private IndexWriter idxWriter;

	/**
	 * The index directory location. This idxSearcher used for logging and alike.
	 */
	private String indexDirPath = "";

	/**
	 * index searcher
	 */
	private IndexSearcher idxSearcher;
	
	/**
	 * index reader
	 */
	private IndexReader idxReader;

	/**
	 * contains all registered listeners
	 */
	private final Set<ONDEXListener> listeners = new HashSet<>();

	/**
	 * wrapped LUCENE ONDEX graph
	 */
	private LuceneONDEXGraph og = null;

	// contains all used DataSources for concept accessions
	private Set<String> listOfConceptAccDataSources = new HashSet<>();

	// contains all used attribute names for concepts
	private Set<String> listOfConceptAttrNames = new HashSet<>();

	// contains all used attribute names for relations
	private Set<String> listOfRelationAttrNames = new HashSet<>();
	
	/**
	 * global analyser used for the index
	 */
	public final static Analyzer DEFAULTANALYZER = new OndexAnalyzer ();

	/** A marker used to mark a metadata document. */
	private static final String LASTDOCUMENT_FIELD = "LASTDOCUMENT_FIELD";

	/**
	 * remove double spaces
	 */
	private final static Pattern DOUBLE_SPACES_RE = Pattern.compile("\\s{2,}");

	/**
	 * empty BitSet for empty results
	 */
	private final static BitSet EMPTYBITSET = new BitSet(0);

	private final static Map<Integer, Float> EMPTYSCOREMAP = new HashMap<>( 0 );

	/* Options to reflect these names are set in the class initialising block, see below */
	private final static FieldType FIELD_TYPE_STORED_INDEXED_NO_NORMS = new FieldType ( TextField.TYPE_STORED );
	private final static FieldType FIELD_TYPE_STORED_INDEXED_VECT_STORE = new FieldType ( TextField.TYPE_STORED );
	private final static FieldType FIELD_TYPE_STORED_INDEXED_UNCHANGED = new FieldType ( StringField.TYPE_STORED );


	/**
	 * threaded indexing of graph
	 */
	private static final ExecutorService EXECUTOR;

	/**
	 * Allows only the id of a document to be loaded
	 */
	private static Set<String> ID_FIELDS = new HashSet<> ( Arrays.asList ( CONID_FIELD, RELID_FIELD ) ); 

	// pre-compiled patterns for text stripping
	private final static Pattern NON_WORD_RE = Pattern.compile("\\W");

	/**
	 * This idxSearcher the RAM allocated for Lucene buffering, during indexing operation. It idxSearcher expressed as a fraction of
	 * Runtime.getRuntime ().maxMemory (). 
	 * 
	 */
	private final static double RAM_BUFFER_QUOTA = 0.1;
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	static
	{
		{ 
			FieldType f =  FIELD_TYPE_STORED_INDEXED_NO_NORMS;
			f.setOmitNorms ( true );
			f.freeze ();
		}
		
		{
			FieldType f = FIELD_TYPE_STORED_INDEXED_VECT_STORE;
			f.setStoreTermVectors ( true );
			f.freeze ();
		}
		{
			FieldType f = FIELD_TYPE_STORED_INDEXED_UNCHANGED;
			f.setIndexOptions ( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS );
			f.setOmitNorms ( true );
			f.freeze ();
		}
		
		EXECUTOR = Executors.newCachedThreadPool ();
		Runtime.getRuntime ().addShutdownHook ( 
			new Thread ( 
				() -> { if ( EXECUTOR != null ) EXECUTOR.shutdownNow (); } 
			) 
		); 
	}

	
	/**
	 * Optimises the RAM to be used for Lucene indexes, based on {@link #RAM_BUFFER_QUOTA} and
	 * max memory available.
	 */
	private static long getOptimalRamBufferSize ()
	{
		return Math.round ( RAM_BUFFER_QUOTA * Runtime.getRuntime ().maxMemory () / ( 1 << 20 ) );		
	}
	
	/**
	 * Pre-process a text to be sent to a tokenised index field.
	 * 
	 * @param text
	 *            String
	 * @return String
	 */
	public static String preProcessTokenizedText(String text) 
	{
		// trim and lower case
		text = text.trim().toLowerCase();

		// replace all non-word characters with space
		text = NON_WORD_RE.matcher(text).replaceAll(SPACE);

		// replace double spaces by single ones
		text = DOUBLE_SPACES_RE.matcher(text).replaceAll(SPACE);

		return text;
	}
	
	/**
	 * Concept accessions are saved both in their tokenised form
	 * and as the transformation returned here, ie {@code '_' + <value> + '_'}. This way, it's
	 * possible to get unique results from '123', which instead would match both '123' and
	 * 'go 123'. TODO: This dirty trick is necessary because the proper solution is too complicated.
	 * That would be getting rid of this Lucene field names composed with data source IDs
	 * and storing multiple documents per accession.
	 * 
	 * TODO: remove
	 * 
	 */
	@Deprecated
	private static String rawAccession ( String accStr )
	{
		return '_' + accStr + '_';		
	}

	
	/**
	 * Constructor which initialises an empty LuceneEnv.
	 * 
	 * @param indexDirPath
	 *            String
	 * @param createNewIndex
	 *            boolean
	 */
	public LuceneEnv(String indexDirPath, boolean create) 
	{
		this.indexDirPath = indexDirPath;
		this.createNewIndex = create;

		if (create) new File(indexDirPath).mkdirs();
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
	public void closeAll () 
	{
		this.closeIdxWriter (); // contains reader's closing too
	}

	/**
	 * Flushes and commits a potentially open index writer.
	 */
	public void closeIdxWriter () 
	{
		if ( this.idxWriter == null ) return;
				
		try 
		{
			// add last document to index
			addMetadataToIndex ();

			this.idxWriter.commit ();
			this.idxWriter.close ();
			this.idxWriter = null;
			
			this.closeIdxReader (); // Just to be sure it's re-read
		} 
		catch (IOException ex) {
			fireEventOccurred ( new DataFileErrorEvent ( ex.getMessage(), "[LucenceEnv - closeIndex]" ) );
			throw new UncheckedIOException ( "Internal error while working with Lucene: " + ex.getMessage (), ex );
		} 
	}

	/**
	 * @param cid
	 *            the concept id to check for
	 * @return if one or more indexes of this concept exist in the index
	 */
	public boolean conceptExistsInIndex(int cid) 
	{
		DocIdCollector collector = null;
		try {
			openIdxReader ();
			collector = new DocIdCollector(idxSearcher.getIndexReader());
			idxSearcher.search( new TermQuery(new Term(CONID_FIELD, String.valueOf(cid))), collector);
		} 
		catch (IOException ex) {
			throw new UncheckedIOException ( 
				String.format ( "I/O error while doing conceptExistsInIndex(%s): %s", cid, ex.getMessage () ), 
				ex 
			);
		}
		return collector.getBits().length() > 0;
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
	public int getFrequenceyOfWordInConceptAttribute(AttributeName an, String word) 
	{
		String fieldname = CONATTRIBUTE_FIELD + DELIM + an.getId();
		Term term = new Term(fieldname, word);
		try {
			this.openIdxReader ();
			return idxReader.docFreq( term );
		} 
		catch (IOException ex) 
		{
			fireEventOccurred(
				new DataFileErrorEvent(ex.getMessage(), "[LuceneEnv - getFrequenceyOfWordInConceptAttribute]")
			);
			throw new UncheckedIOException ( "Internal error while working with Lucene: " + ex.getMessage (), ex );
		}
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
	public int[] getFrequenceyOfWordInConceptAttribute(AttributeName an, String[] word) 
	{
		String fieldname = CONATTRIBUTE_FIELD + DELIM + an.getId();

		try {
			this.openIdxReader ();			
			int[] freqs = new int[word.length];
			for (int i = 0; i < word.length; i++) {
				freqs[i] = idxReader.docFreq ( new Term ( fieldname, word[i] ) );
			}

			// Returns the number of documents containing the terms.
			return freqs;
		} 
		catch (IOException ex) 
		{
			fireEventOccurred(
				new DataFileErrorEvent(ex.getMessage(), "[LuceneEnv - getFrequenceyOfWordInConceptAttribute]")
			);
			throw new UncheckedIOException ( "Internal error while working with Lucene: " + ex.getMessage (), ex );
		}
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
	public int getFrequenceyOfWordInRelationAttribute(AttributeName an, String word)
	{
		String fieldname = RELATTRIBUTE_FIELD + DELIM + an.getId();
		Term term = new Term(fieldname, word);
		try {
			// Returns the number of documents containing the term.
			this.openIdxReader ();						
			return idxReader.docFreq ( term );
		} 
		catch (IOException ex) {
			fireEventOccurred ( 
				new DataFileErrorEvent(ex.getMessage(), "[LuceneEnv - getFrequenceyOfWordInRelationAttribute]") 
			);
			throw new UncheckedIOException ( "Internal error while working with Lucene: " + ex.getMessage (), ex );
			
		}
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
	public int[] getFrequenceyOfWordInRelationAttribute(AttributeName an, String[] word) 
	{
		String fieldname = RELATTRIBUTE_FIELD + DELIM + an.getId();

		try {
			this.openIdxReader ();
			int[] freqs = new int[word.length];
			for (int i = 0; i < word.length; i++) {
				freqs[i] = idxReader.docFreq ( new Term(fieldname, word[i]) );
			}

			// Returns the number of documents containing the terms.
			return freqs;
		}
		catch (IOException ex) {
			fireEventOccurred(
				new DataFileErrorEvent(ex.getMessage(), "[LuceneEnv - getFrequenceyOfWordInRelationAttribute]")
			);
			throw new UncheckedIOException ( "Internal error while working with Lucene: " + ex.getMessage (), ex );
		}
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
	
	
	public boolean isReadOnlyMode () {
		return isReadOnlyMode;
	}

	/**
	 * This can be set to true when you know that you're not going to write to the index anymore and you 
	 * want to speedup search-only operations.
	 * 
	 * Components like the workflow engine or the desktop filters set this to true by default, assuming that
	 * their created index will be used in read-only mode. Clients like plug-ins need to set it back to false
	 * if they want to write to the index.
	 * 
	 * <b>WARNING</b>: be careful when using this flag. If you set this to true and then invoke some
	 * writing operation, you'll get an exception. Moreover, if the index directory that this {@link LuceneEnv}
	 * is based on is changed from some other component, this flag will probably cause problems like non-updated
	 * or inconsistent lucene search results.
	 * 
	 */
	public void setReadOnlyMode ( boolean isReadOnlyMode ) {
		this.isReadOnlyMode = isReadOnlyMode;
	}

	private void checkReadOnlyMode ()
	{
		if ( this.isReadOnlyMode ) 
			throw new IllegalStateException ( "Can't do writing operations while LuceneEnv is in read-only mode" );
	}
	
	
	/**
	 * Open index for writing.
	 */
	public void openIdxWriter ()
	{
		checkReadOnlyMode ();		
		
		// Double-check lazy init, see above
		if ( this.idxWriter != null ) return;
		
		try
		{			
			// Just in case it's not flushed
			this.closeIdxReader ();
			
			
			this.idxDirectory = FSDirectory.open ( Paths.get ( indexDirPath ) );
			
			IndexWriterConfig writerConfig = new IndexWriterConfig ( DEFAULTANALYZER );
			writerConfig.setOpenMode ( OpenMode.CREATE_OR_APPEND );
			// set RAM buffer, hopefully speeds up things
			writerConfig.setRAMBufferSizeMB ( getOptimalRamBufferSize () );

			this.idxWriter = new IndexWriter ( idxDirectory, writerConfig );
			
			// deletes the last record that has attribute names,
			// that will have to be rebuilt
			idxWriter.deleteDocuments ( new Term ( LASTDOCUMENT_FIELD, "true" ) );
			idxWriter.commit ();
			log.info ( "Index opened, old metadata deletions: ", idxWriter.hasDeletions () );
		}
		catch ( IOException ex ) {
			fireEventOccurred ( new DataFileErrorEvent ( ex.getMessage (), "[LucenceEnv - openIndex]" ) );
			throw new UncheckedIOException ( "Internal error while working with Lucene: " + ex.getMessage (), ex );
		}
	}
	
	
	/**
	 * This was added by brandizi in 2017, to ensure {@link #idxReader} and {@link #idxSearcher} are open 
	 * before starting using them in an operation. 
	 */
	private void openIdxReader () throws IOException
	{
		if ( this.idxDirectory == null )
			this.idxDirectory = FSDirectory.open ( new File ( indexDirPath ).toPath () );

		if ( this.isReadOnlyMode ) return;
		
		DirectoryReader newReader = null;
		
		try
		{
			if ( this.idxReader == null )
				newReader = DirectoryReader.open ( this.idxDirectory );
			else
			{
				try {
					newReader = DirectoryReader.openIfChanged ( (DirectoryReader) idxReader );
				}
				catch ( AlreadyClosedException ex ) {
					// It seems there isn't a method to know if the reader was already closed, apart from a listener, which
					// would be a bit more cumbersome here and not so useful
					newReader = DirectoryReader.open ( idxDirectory );
				}
			}
		}
		finally {
			if ( newReader != null )
			{
				if ( this.idxReader != null ) this.idxReader.close ();
				this.idxReader = newReader;
				this.idxSearcher = new IndexSearcher ( this.idxReader );
			}
		}
	}	
	
	/**
	 * This is normally  not needed, except by {@link #openIdxWriter()}, which invokes it to invalidated the current
	 * reader objects and ensure they're refreshed.
	 */
	private void closeIdxReader () throws IOException
	{
		if ( this.idxReader != null ) {
			this.idxReader.close ();
			this.idxReader = null;
		}
		this.idxSearcher = null;
		if ( this.idxDirectory != null ) {
			this.idxDirectory.close ();
			this.idxDirectory = null;
		}
	}
	
	
	/**
	 * <p>Execute an action, which presumably will do something with the current {@link LuceneEnv}, and 
	 * then invokes {@link #closeAll()}.</p>
	 * 
	 * <p>This is some syntactic sugar that avoids try/finally.</p>
	 *  
	 */
	public <T> T wrapIdxFunction ( Supplier<T> operation )
	{
		try {
			return operation.get ();
		}
		finally {
			this.closeAll ();
		}
	}

	/**
	 * Variant of {@link #wrapIdxFunction(Supplier)}.
	 */
	public void wrapIdxOperation ( Runnable operation ) {
		this.wrapIdxFunction ( () -> { operation.run (); return null; });
	}

	
	/**
	 * @param rid
	 *            the relation id to check for
	 * @return if one or more indexes of this concept exist in the index
	 */
	public boolean relationExistsInIndex ( int rid )
	{
		DocIdCollector collector = null;
		try 
		{
			this.openIdxReader ();
			collector = new DocIdCollector ( idxSearcher.getIndexReader () );
			idxSearcher.search ( new TermQuery ( new Term ( RELID_FIELD, String.valueOf ( rid ) ) ), collector );
		}
		catch ( IOException ex ) {
			throw new UncheckedIOException ( 
				String.format ( "I/O error while doing relationExistsInIndex(%s): %s", rid, ex.getMessage () ), 
				ex 
			);
		}
		return ( collector.getBits ().length () > 0 );
	}

	/**
	 * Removes the selected concept from the index NB this idxSearcher an expensive
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
	public boolean removeConceptsFromIndex(int[] cids) 
	{
		return updateIndex ( () -> 
		{
			Query[] terms = new Query[cids.length];
			for (int i = 0; i < terms.length; i++) {
				terms[i] = new TermQuery(new Term(CONID_FIELD,
						String.valueOf(cids[i])));
				log.trace ( "Term removed:", terms[i].toString());
			}
			Exceptions.sneak ().get (	() -> idxWriter.deleteDocuments ( terms ) );
			return idxWriter.hasDeletions();			
		});
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
	 * Removes the selected relation from the index NB this idxSearcher an expensive
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
	public boolean removeRelationsFromIndex(int[] rids) 
	{
		return updateIndex ( () -> 
		{
			Term[] terms = new Term[rids.length];
			for (int i = 0; i < terms.length; i++) {
				terms[i] = new Term(RELID_FIELD, String.valueOf(rids[i]));
			}
			Exceptions.sneak ().get ( () -> idxWriter.deleteDocuments(terms) );
			return idxWriter.hasDeletions();
		});		
	}

	/**
	 * 
	 * Searches for an {@link ONDEXEntity} in the index, ie, a concept or a relation.
	 * 
	 * @param luceneIdField the Lucene field that is used to store the entity internal unique ID, ie, 
	 * 
	 * TODO: filter non relevant Lucene documents in the query, see #53 for details.
	 *  
	 */
	private <E extends ONDEXEntity> Set<E> searchOndexEntity ( Query q, String luceneIdField, Class<E> entityClass )
	{
		try
		{
			this.openIdxReader ();
			DocIdCollector collector = new DocIdCollector ( idxSearcher.getIndexReader () );
			idxSearcher.search ( q, collector );

			BitSet bs = collector.getBits ();
			if ( bs.length () == 0 ) return BitSetFunctions.create ( og, entityClass, EMPTYBITSET );

			BitSet set = new BitSet ( bs.length () );
			// iterator of document indices
			for ( int i = bs.nextSetBit ( 0 ); i >= 0; i = bs.nextSetBit ( i + 1 ) )
			{
				// retrieve associated document
				Document document = idxSearcher.doc ( i, ID_FIELDS );
				
				// get concept ID from document
				Integer entityId = Optional.ofNullable ( document.get ( luceneIdField ) )
					.map ( Integer::valueOf )
					.orElse ( null );
				
				if ( entityId == null ) {
					log.warn ( 
						"Skipping Lucene document having a null ID for the Lucene field: \"{}\", found by the query \"{}\"."
						+ " This is likely to be caused by https://github.com/Rothamsted/knetbuilder/issues/53",
						luceneIdField,
						q.toString ()
					);
					continue;
				}
				
				set.set ( entityId );
			}
			return BitSetFunctions.create ( og, entityClass, set );
		}
		catch ( IOException ex ) {
			fireEventOccurred ( new DataFileErrorEvent ( ex.getMessage (), "[LuceneEnv - searchEntity]" ) );
			log.error ( "Error while searching: " + q + " over field " + luceneIdField + ": " + ex.getMessage (), ex );
			throw new UncheckedIOException ( "Internal error while working with Lucene: " + ex.getMessage (), ex );			
		}
	}	
	
	
	/**
	 * Searches for an {@link ONDEXEntity} in the index, ie, a concept or a relation.
	 * 
	 * @param luceneIdField the Lucene field that is used to store the entity internal unique ID, ie, 
	 * 
	 * TODO: filter non relevant Lucene documents in the query, see #53 for details.
	 * 
	 */
	private <E extends ONDEXEntity> ScoredHits<E> searchScoredOndexEntity ( 
		Query q, String luceneIdField, Class<E> entityClass 
	)
	{
		try
		{
			openIdxReader ();
			ScoreCollector collector = new ScoreCollector ( idxSearcher.getIndexReader () );
			idxSearcher.search ( q, collector );
			Set<E> view;
			Map<Integer, Float> doc2Scores = collector.getScores ();
			Map<Integer, Float> id2scores = new HashMap<> ();
			BitSet bs = collector.getBits ();
			if ( bs.length () > 0 )
			{
				BitSet set = new BitSet ( bs.length () );
				// iterator of document indices
				for ( int i = bs.nextSetBit ( 0 ); i >= 0; i = bs.nextSetBit ( i + 1 ) )
				{
					Document document = idxSearcher.doc ( i, ID_FIELDS );
					float score = doc2Scores.get ( i );
					
					Integer entityId = Optional.ofNullable ( document.get ( luceneIdField ) )
						.map ( Integer::valueOf )
						.orElse ( null );
					
					if ( entityId == null ) {
						log.warn ( 
							"Skipping Lucene document having a null ID for the Lucene field: \"{}\", found by the query \"{}\"."
							+ " This is likely to be caused by https://github.com/Rothamsted/knetbuilder/issues/53",
							luceneIdField,
							q.toString ()
						);
						continue;
					}
							
					id2scores.put ( entityId, score );
					set.set ( entityId );
				}
				view = BitSetFunctions.create ( og, entityClass, set );
				return new ScoredHits<> ( view, id2scores );
			} 
			else
			{
				view = BitSetFunctions.create ( og, entityClass, EMPTYBITSET );
				return new ScoredHits<> ( view, EMPTYSCOREMAP );
			}
		}
		catch ( IOException ex ) {
			fireEventOccurred ( new DataFileErrorEvent ( ex.getMessage (), "[LuceneEnv - searchScoredEntity]" ) );
			throw new UncheckedIOException ( "Internal error while working with Lucene: " + ex.getMessage (), ex );
		}
	}
	
	
	/**
	 * Searches in Concepts and returns found Concepts.
	 * 
	 * @param q
	 *            Query
	 * @return ScoredHits<ONDEXConcept>
	 */
	public ScoredHits<ONDEXConcept> scoredSearchInConcepts(Query q)  {
		return searchScoredOndexEntity ( q, CONID_FIELD, ONDEXConcept.class );
	}

	/**
	 * Searches in Relations and returns found Relations.
	 * 
	 * @param q
	 *            Query
	 * @return ScoredHits<ONDEXRelation>
	 */
	public ScoredHits<ONDEXRelation> scoredSearchInRelations(Query q) {
		return searchScoredOndexEntity ( q, RELID_FIELD, ONDEXRelation.class );
	}

	

	
	
	
	/**
	 * Searches in Concepts and returns found Concepts.
	 * 
	 * @param q
	 *            Query
	 * @return Set<ONDEXConcept>
	 */
	public Set<ONDEXConcept> searchInConcepts(Query q) {
		return searchOndexEntity ( q, CONID_FIELD, ONDEXConcept.class );
	}

	/**
	 * A wrapper of {@link LuceneQueryBuilder#searchByTypeAndAccession(String, String, boolean)}
	 * that passes the accession-based query to {@link #searchInConcepts(Query)}
	 */
	public Set<ONDEXConcept> searchByTypeAndAccession ( String conceptClassId, String accessionTerm, boolean isCaseSensitive )
	{
		try
		{
			var q = LuceneQueryBuilder.searchByTypeAndAccession ( conceptClassId, accessionTerm, isCaseSensitive );
			if (log.isDebugEnabled () ) log.debug ( "Searching by accession with: {}", q );
			return this.searchInConcepts ( q );
		}
		catch ( ParseException ex )
		{
			throw ExceptionUtils.buildEx ( IllegalArgumentException.class, ex, 
				"Error while querying '%s':'%s': %s", conceptClassId, accessionTerm, ex.getMessage ()
			);
		}
	}

	/**
	 * Defaults to isCaseSensitive = true 
	 */
	public Set<ONDEXConcept> searchByTypeAndAccession ( String conceptClassId, String accessionTerm )
	{
		return this.searchByTypeAndAccession ( conceptClassId, accessionTerm, true );
	}

	/**
	 * A wrapper of {@link LuceneQueryBuilder#searchByTypeAndName(String, String, boolean)}
	 * that passes the accession-based query to {@link #searchInConcepts(Query)}
	 */
	public Set<ONDEXConcept> searchByTypeAndName ( String conceptClassId, String nameTerm, boolean isCaseSensitive )
	{
		try
		{
			var q = LuceneQueryBuilder.searchByTypeAndName ( conceptClassId, nameTerm, isCaseSensitive );
			if (log.isDebugEnabled () ) log.debug ( "Searching by name with: {}", q );
			return this.searchInConcepts ( q );
		}
		catch ( ParseException ex )
		{
			throw ExceptionUtils.buildEx ( IllegalArgumentException.class, ex, 
				"Error while querying '%s':'%s': %s", conceptClassId, nameTerm, ex.getMessage ()
			);
		}
	}

	/**
	 * Defaults to isCaseSensitive = true 
	 */
	public Set<ONDEXConcept> searchByTypeAndName ( String conceptClassId, String nameTerm )
	{
		return this.searchByTypeAndName ( conceptClassId, nameTerm, true );
	}
	
	
	/**
	 * Searches in Relations and returns found Relations.
	 * 
	 * @param q
	 *            Query
	 * @return Set<ONDEXRelation>
	 */
	public Set<ONDEXRelation> searchInRelations(Query q) {
		return searchOndexEntity ( q, RELID_FIELD, ONDEXRelation.class );
	}

	/**
	 * Base method to get a single concept or relation by using the 'iri' attribute.  
	 */
	private <E extends ONDEXEntity> E getEntityByIRI ( String iri, Function<Query, Set<E>> searcher )
	{
		try
		{
			openIdxReader ();
			PhraseQuery q = new PhraseQuery ( "iri", iri );
			Set<E> results = searcher.apply ( q );
			if ( results == null ) return null;
			int size = results.size ();
			if ( size == 0 ) return null;
			E result = results.iterator ().next ();
			if ( size > 1 ) log.warn ( 
				"I've found {} instances of {} for the URI '{}'", size, result.getClass ().getSimpleName (), iri 
			);
			return result;
		}
		catch ( IOException ex )
		{
			throw ExceptionUtils.buildEx ( 
				UncheckedIOException.class, ex, "Error while using lucene to lookup for <%s>: %s", iri, ex.getMessage () 
			);
		} 
	}
	
	/**
	 * These IRI-based fetching methods are used with the new architecture, to bridge graph databases and in-memory Ondex
	 * graphs.
	 */
	public ONDEXConcept getConceptByIRI ( String iri ) {
		return getEntityByIRI ( iri, this::searchInConcepts );
	}
	
	/**
	 * These IRI-based fetching methods are used with the new architecture, to bridge graph databases and in-memory Ondex
	 * graphs.
	 */
	public ONDEXRelation getRelationByIRI ( String iri ) {
		return getEntityByIRI ( iri, this::searchInRelations );
	}
	
	
	private <E extends ONDEXEntity> ScoredHits<E> searchScoredEntity ( 
		Query q, String field, Class<E> returnedValueClass, int limit 
	)
	{
		try
		{
			this.openIdxReader ();
			final BitSet bits = new BitSet ();
			TopDocs hits = idxSearcher.search ( q, limit );
			Map<Integer, Float> scores = new HashMap<> ();
			for ( int i = 0; i < hits.scoreDocs.length; i++ )
			{
				int docId = hits.scoreDocs[ i ].doc;
				float score = hits.scoreDocs[ i ].score;
				Document document = idxSearcher.doc ( docId, ID_FIELDS );
								
				Integer entityId = Optional.ofNullable ( document.get ( field ) )
					.map ( Integer::valueOf )
					.orElse ( null );
				
				if ( entityId != null ) {
					bits.set ( entityId );
					scores.put ( entityId, score );
				}
			}

			Set<E> view = BitSetFunctions.create ( og, returnedValueClass, bits );
			return new ScoredHits<> ( view, scores );
		}
		catch ( IOException ex ) {
			fireEventOccurred ( new DataFileErrorEvent ( ex.getMessage (), "[LuceneEnv - searchScoredEntity]" ) );
			throw new UncheckedIOException ( "Internal error while working with Lucene: " + ex.getMessage (), ex );
		}
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
		return searchScoredEntity ( q, CONID_FIELD, ONDEXConcept.class, n );
	}
	
	public ScoredHits<ONDEXConcept> searchTopConceptsByIdxField ( String keywords, String idxFieldName, int resultLimit )
	{
		return searchTopConceptsByIdxField ( keywords, idxFieldName, null, resultLimit );
	}

	public ScoredHits<ONDEXConcept> searchTopConceptsByIdxField ( 
		String keywords, String idxFieldName, String idxFieldSubName, int resultLimit )
	{
		Query query = getIdxFieldQuery ( keywords, idxFieldName, idxFieldSubName );
		return this.searchTopConcepts ( query, resultLimit );
	}
	
	
	public Query getIdxFieldQuery ( String keywords, String idxFieldName )
	{
		return getIdxFieldQuery ( keywords, idxFieldName, null );
	}
	
	/**
	 * Uses {@link #getIndexFieldName(String, String)} and {@link #DEFAULTANALYZER} to 
	 * build an index Query.
	 * 
	 * TODO: there is redundancy between these methods here (used by Knetminer) and
	 * {@link LuceneQueryBuilder}.
	 * 
	 */
	public Query getIdxFieldQuery ( String keywords, String idxFieldName, String idxFieldSubName )
	{
		try
		{
			String idxFullFN = getIndexFieldName ( idxFieldName, idxFieldSubName );
			QueryParser parser = new QueryParser ( idxFullFN, DEFAULTANALYZER );
			return parser.parse ( keywords );
		}
		catch ( ParseException ex ) {
			throw new IllegalArgumentException ( "Internal error while searching over Ondex index: " + ex.getMessage (), ex );
		}
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
		return searchScoredEntity ( q, RELID_FIELD, ONDEXRelation.class, n );
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
			"Using Lucene with index dir: " + this.indexDirPath,
			"[LuceneEnv - setONDEXGraph]",
			Level.INFO
		);
		fireEventOccurred(so);
		
		// store an immutable version of the graph
		this.og = new LuceneONDEXGraph(aog);

		// start indexing
		indexONDEXGraph( aog );
	}


	/**
	 * Updates or adds new concepts to the index
	 * 
	 * @param concepts
	 *            the relations to add to the index
	 * @throws AccessDeniedException
	 */
	public void updateConceptsToIndex(Set<ONDEXConcept> concepts) throws AccessDeniedException 
	{
		updateIndexVoid ( () -> 
		{
			for ( ONDEXConcept concept : concepts )
			{
				// try a delete this idxSearcher quicker than reopening the "idxSearcher"
				Exceptions.sneak ().run ( 
					() -> idxWriter.deleteDocuments ( new Term ( CONID_FIELD, String.valueOf ( concept.getId () ) ) ) 
				);
				addConceptToIndex ( concept );
			}
		});		
	}

	/**
	 * Update or add a concept to the index NB this idxSearcher an expensive operations,
	 * so try to use the batch job @see updateConceptsToIndex(Set<ONDEXConcept>
	 * concepts)
	 * 
	 * @param concept
	 *            the concept to add to the index
	 * @throws AccessDeniedException
	 */
	public void updateConceptToIndex ( ONDEXConcept concept ) throws AccessDeniedException
	{
		updateIndexVoid ( () -> 
		{
			boolean exists = conceptExistsInIndex ( concept.getId () );
			if ( exists ) Exceptions.sneak ().run ( 
				() -> idxWriter.deleteDocuments ( new Term ( CONID_FIELD, String.valueOf ( concept.getId () ) ) ) 
			);
			addConceptToIndex ( concept );
		});
	}

	/**
	 * Updates or adds new relations to the index
	 * 
	 * @param relations
	 *            the relations to add to the index
	 * @throws AccessDeniedException
	 */
	public void updateRelationsToIndex ( Set<ONDEXRelation> relations ) throws AccessDeniedException
	{
		updateIndexVoid ( () ->
		{
			for ( ONDEXRelation relation : relations )
			{
				Exceptions.sneak ().run ( () -> 
					idxWriter.deleteDocuments ( new Term ( RELID_FIELD, String.valueOf ( relation.getId () ) ) )
				);
				addRelationToIndex ( relation );
			}
		});
	}

	/**
	 * Updates or adds a relations to an Index NB this idxSearcher an expensive
	 * operations, so try to use the batch job @see
	 * updateRelationsToIndex(Set<ONDEXRelation> relations)
	 * 
	 * @param relation
	 *            ondex relation to add to the index
	 * @throws AccessDeniedException
	 */
	public void updateRelationToIndex ( ONDEXRelation relation ) throws AccessDeniedException
	{
		updateIndexVoid ( () -> 
		{
			boolean exists = relationExistsInIndex ( relation.getId() );
			if ( exists )
				Exceptions.sneak ().run ( () ->
					idxWriter.deleteDocuments ( new Term ( RELID_FIELD, String.valueOf ( relation.getId () ) ) )				
				);
			addRelationToIndex ( relation );
		});
	}

	private Document getCommonFields ( ONDEXEntity e )
	{
		AttributeName iriAttrType = this.og.getMetaData ().getAttributeName ( "iri" );
		
		String iri = iriAttrType == null 
			? null 
			: Optional.ofNullable ( e.getAttribute ( iriAttrType ) )
				.map ( attr -> (String) attr.getValue () )
				.orElse ( null );
		
		Document doc = new Document ();
		if ( iri != null ) doc.add ( new Field ( "iri", iri, FIELD_TYPE_STORED_INDEXED_UNCHANGED ) );
		
		return doc;
	}
	
	
	/**
	 * Add the given ONDEXConcept to the current index.
	 * 
	 * @param c
	 *            ONDEXConcept to add to index
	 * @throws AccessDeniedException
	 */
	private void addConceptToIndex(ONDEXConcept c) throws AccessDeniedException 
	{
		checkReadOnlyMode ();		

		// get textual properties
		String conceptID = String.valueOf ( c.getId () );
		String parserID = c.getPID ();
		String annotation = c.getAnnotation ();
		String description = c.getDescription ();
		
		if ( StringUtils.length ( parserID ) > 32766 ) throw new IllegalArgumentException ( 
			"Lucene cannot index a concept with PID length > 32766, PID is:\n" +
			"'" + StringUtils.truncate ( parserID, 120 ) + "...'"
		);

		// get all properties iterators
		Set<ConceptAccession> caccs = c.getConceptAccessions ();
		if ( caccs.isEmpty () ) caccs = null;
		
		Set<ConceptName> cnames = c.getConceptNames ();
		if ( cnames.size () == 0 ) cnames = null;
		
		Set<Attribute> cattrs = c.getAttributes ();
		if ( cattrs.size () == 0 ) cattrs = null;

		// leave if there are no properties
		if ( caccs == null && cnames == null && cattrs == null && annotation.length () == 0
				&& description.length () == 0 )
			return; // there idxSearcher nothing to index, no document should be created!

		// createNewIndex a new document for each concept and sets fields
		Document doc = this.getCommonFields ( c );
		
		doc.add ( new Field ( CONID_FIELD, conceptID, FIELD_TYPE_STORED_INDEXED_UNCHANGED ) );
		doc.add ( new Field ( PID_FIELD, parserID, StringField.TYPE_STORED ) );
		
		doc.add ( new Field ( CC_FIELD, c.getOfType ().getId (), FIELD_TYPE_STORED_INDEXED_UNCHANGED ) );
		doc.add ( new Field ( DataSource_FIELD, c.getElementOf ().getId (), FIELD_TYPE_STORED_INDEXED_UNCHANGED ) );

		if ( annotation.length () > 0 )
			doc.add ( new Field ( ANNO_FIELD, LuceneEnv.preProcessTokenizedText ( annotation ), FIELD_TYPE_STORED_INDEXED_VECT_STORE ) );

		if ( description.length () > 0 )
			doc.add ( new Field ( DESC_FIELD, LuceneEnv.preProcessTokenizedText ( description ), FIELD_TYPE_STORED_INDEXED_VECT_STORE ) );

				
		// start concept accession handling
		if ( caccs != null )
		{
			// add all concept accessions for this concept
			for ( ConceptAccession ca : caccs )
			{
				String accession = ca.getAccession ();
				String elementOf = ca.getElementOf ().getId ();
				Boolean isAmbiguous = ca.isAmbiguous ();
				listOfConceptAccDataSources.add ( elementOf );
				
				String accFieldId = CONACC_FIELD + DELIM + elementOf;
				if ( isAmbiguous ) accFieldId += "" + DELIM + AMBIGUOUS;

				FieldType ftype = FIELD_TYPE_STORED_INDEXED_VECT_STORE;
				doc.add ( new Field ( accFieldId, preProcessTokenizedText ( accession ), ftype ));

				// see rawAccession about why we do this
				// TODO: remove var rawAcc = rawAccession ( accession );
				doc.add ( new StringField ( accFieldId + DELIM + RAW, accession, Store.YES  ));
				
				// TODO: do we need the lower case version?
				
				// Needed for exact search by accession independently on the source
				doc.add ( new StringField ( CONACC_FIELD + DELIM + RAW, accession, Store.YES ) );
				// To support case-insensitive searches too
				doc.add ( new StringField ( CONACC_FIELD + DELIM + RAW, accession.toLowerCase (), Store.YES ) );
			}
		}

		// start concept name handling
		if ( cnames != null )
		{			
			cnames.stream ()
			.map ( ConceptName::getName )
			.filter ( Objects::nonNull )
			.forEach ( nameStr -> { 
				doc.add ( new Field ( CONNAME_FIELD, LuceneEnv.preProcessTokenizedText ( nameStr ), FIELD_TYPE_STORED_INDEXED_VECT_STORE ) );
				// Like the accession case, allows for exact searches and case-insensitive exact searches
				var nameFieldId = CONNAME_FIELD + DELIM + RAW; 
				doc.add ( new StringField ( nameFieldId, nameStr, Store.YES ) );
				doc.add ( new StringField ( nameFieldId, nameStr.toLowerCase (), Store.YES ) );
			});
		}

		// start concept gds processing
		if ( cattrs != null )
		{
			// mapping attribute name to gds value
			Map<String, String> attrsRaw = new HashMap<> ();

			// add all concept gds for this concept
			for ( Attribute attribute : cattrs )
			{
				if ( attribute.isDoIndex () )
				{
					String name = attribute.getOfType ().getId ();
					listOfConceptAttrNames.add ( name );
					String value = attribute.getValue ().toString ();
					attrsRaw.put ( name, LuceneEnv.preProcessTokenizedText ( value ) );
				}
			}

			// write attribute name specific Attribute fields
			attrsRaw.forEach ( ( name, value ) -> 
				doc.add ( new Field ( CONATTRIBUTE_FIELD + DELIM + name, value, FIELD_TYPE_STORED_INDEXED_VECT_STORE ) )
			);
			
			attrsRaw = null; // clear() was here, maybe to cut memory?
		}
		
		try
		{
			idxWriter.addDocument ( doc );
		}
		catch ( IOException ex ) {
			fireEventOccurred ( new DataFileErrorEvent ( ex.getMessage (), "[LuceneEnv - addConceptToIndex]" ) );
			throw new UncheckedIOException ( "Internal error while working with Lucene: " + ex.getMessage (), ex );
		}
	}


	
	/**
	 * Adds sets of used meta data to the index.
	 * WARNING: this assumes the index is already {@link #openIdxWriter() opened}. 
	 */
	private void addMetadataToIndex() 
	{
		checkReadOnlyMode ();		

		// new document for fields
		Document doc = new Document();
		doc.add(new Field(LASTDOCUMENT_FIELD, "true", StringField.TYPE_NOT_STORED ));
		// Attribute fields about the last document were initially not stored. However, this isn't good for Lucene 6,
		// because it complaints that a field name having mixed docs where the field it's stored and not stored cannot 
		// be used to build certain searches (https://goo.gl/Ee1sfm)
		//
		for (String name : listOfConceptAttrNames)
			doc.add(new Field(CONATTRIBUTE_FIELD + DELIM + name, name,	FIELD_TYPE_STORED_INDEXED_VECT_STORE ));
		for (String name : listOfRelationAttrNames)
			doc.add(new Field(RELATTRIBUTE_FIELD + DELIM + name, name,	FIELD_TYPE_STORED_INDEXED_VECT_STORE ));
		for (String elementOf : listOfConceptAccDataSources)
			doc.add(new Field(CONACC_FIELD + DELIM + elementOf, elementOf, FIELD_TYPE_STORED_INDEXED_VECT_STORE ));

		// add last document
		try {
			this.idxWriter.addDocument(doc);
		} 
		catch (IOException ex) {
			fireEventOccurred(new DataFileErrorEvent(ex.getMessage(), "[LuceneEnv - addMetadataToIndex]"));
			throw new UncheckedIOException ( "Internal error while working with Lucene: " + ex.getMessage (), ex );
		}
	}

	/**
	 * Add the given ONDEXRelation to the current index.
	 * WARNING: this assumes the index is already {@link #openIdxWriter() opened}. 
	 */
	private void addRelationToIndex ( ONDEXRelation r ) throws AccessDeniedException
	{
		checkReadOnlyMode ();		
		
		// get Relation and RelationAttributes
		Set<Attribute> attrs = r.getAttributes ();

		// If it hasn't attributes, everything is already in Ondex
		if ( attrs.size () == 0 ) return;

		// createNewIndex a Document for each relation and store ids
		Document doc = this.getCommonFields ( r );

		doc.add ( new Field ( RELID_FIELD, String.valueOf ( r.getId () ), FIELD_TYPE_STORED_INDEXED_UNCHANGED ) );
		doc.add ( new Field ( FROM_FIELD, String.valueOf ( r.getFromConcept ().getId () ), FIELD_TYPE_STORED_INDEXED_UNCHANGED ) );
		doc.add ( new Field ( TO_FIELD, String.valueOf ( r.getToConcept ().getId () ), FIELD_TYPE_STORED_INDEXED_UNCHANGED ) );
		doc.add ( new Field ( OFTYPE_FIELD, r.getOfType ().getId (), StringField.TYPE_STORED ) );

		// mapping attribute name to gds value
		Map<String, String> attrsRaw = new HashMap<> ();

		// add all relation gds for this relation
		for ( Attribute attribute : attrs )
		{
			if ( attribute.isDoIndex () )
			{
				String name = attribute.getOfType ().getId ();
				listOfRelationAttrNames.add ( name );
				String value = attribute.getValue ().toString ();
				attrsRaw.put ( name, LuceneEnv.preProcessTokenizedText ( value ) );
			}
		}

		// write attribute name specific Attribute fields
		for ( String name : attrsRaw.keySet () )
		{
			String value = attrsRaw.get ( name );
			doc.add ( new Field ( RELATTRIBUTE_FIELD + DELIM + name, value, FIELD_TYPE_STORED_INDEXED_VECT_STORE ) );
		}
		
		attrsRaw = null; // clear() was initially here, maybe it's useful for memory consumption

		// store document to index
		try {
			idxWriter.addDocument ( doc );
		}
		catch ( IOException ex ) {
			fireEventOccurred ( new DataFileErrorEvent ( ex.getMessage (), "[LuceneEnv - addRelationToIndex]" ) );
			throw new UncheckedIOException ( "Internal error while working with Lucene: " + ex.getMessage (), ex );
		}
	}

	/**
	 * Builds the index for a given ONDEXGraph.
	 */
	private void indexONDEXGraph(final ONDEXGraph graph ) throws AccessDeniedException
	{
		checkReadOnlyMode ();		

		fireEventOccurred(
			new GeneralOutputEvent(	"Starting the Lucene environment.", "[LuceneEnv - indexONDEXGraph]")
		);

		try 
		{
			// if index idxSearcher new created, fill index
			if ( this.createNewIndex ) 
			{
				// Just in case
				this.closeIdxWriter();
				this.openIdxWriter ();
				
				try
				{
					indexingHelper ( graph.getConcepts (), "concept", this::addConceptToIndex );
					indexingHelper ( graph.getRelations (), "relation", this::addRelationToIndex );
	
					// last Document contains meta data lists
					addMetadataToIndex();
					idxWriter.commit();
				}
				finally {
					this.closeIdxWriter ();
					this.openIdxReader (); // Cause they're going to read it next and threads want it open
				}
				return;
			} // if ( createNewIndex )

			// if !createNewIndex, read some general data from existing index
			this.openIdxReader ();
			try
			{
				// read in attribute names and accession DataSources
				Document doc = idxReader.document ( idxReader.maxDoc() - 1 );
				for (Object o : doc.getFields()) 
				{
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
			finally {
				this.openIdxReader (); // Again, because next step is likely threads opening it.
			}
		} 
		catch (IOException ex) {
			fireEventOccurred(new DataFileErrorEvent(ex.getMessage(), "[LucenceEnv - indexONDEXGraph]"));
			throw new UncheckedIOException ( "Internal error while working with Lucene: " + ex.getMessage (), ex );
		}
	}

	/**
	 * Little helper used by {@link #indexONDEXGraph(ONDEXGraph)} to factorise the submission to the index of 
	 * concepts and relations.
	 * 
	 * It indexes a set of concepts or relations, by means of {@link #addConceptToIndex(ONDEXConcept)} or
	 * {@link #addRelationToIndex(ONDEXRelation)}. {@code label} should be "concept" or "relation" and idxSearcher
	 * used for logging.
	 */
	private <OE extends ONDEXEntity> void indexingHelper ( Set<OE> inputs, String label, Consumer<OE> indexSubmitter )
		throws IOException
	{		
		final int sz = inputs.size ();
		log.info ( "Start indexing the Ondex Graph, {} {}(s) sent to index", sz, label );
			
		PercentProgressLogger progressLogger = new PercentProgressLogger ( 
			"{}% of " + label + "s submitted to index", sz 
		);
		progressLogger.appendProgressReportAction ( 
			Exceptions.sneak ().fromBiConsumer ( (oldp, newp) -> this.idxWriter.commit () )
		);
		
		// TODO: the use of a parallel task manager comes from existing code, what's the point of it?!
		Future<?> task = EXECUTOR.submit ( 
			() -> {
				for ( OE entity : inputs )
				{
					indexSubmitter.accept ( entity );
					progressLogger.updateWithIncrement ();
				}
			}
		);
		
		try {
			task.get();
			this.idxWriter.commit ();
		} 
		catch ( InterruptedException|ExecutionException ex ) {
			throw new RuntimeException ( "Error while indexing Ondex Graph:" + ex.getMessage (), ex );
		}
	}
	
	/**
	 * Notify all listeners that have registered with this class.
	 * 
	 * TODO: In most cases, it should be replaced by exception wrapping/rethrowing and then there 
	 * should be a top-level reporter, dealing with the GUI, as currently this does. 
	 * 
	 * @param e
	 *            type of event
	 */
	private void fireEventOccurred(EventType e) 
	{
		if (listeners.size() > 0) {
			// new ondex event
			ONDEXEvent oe = new ONDEXEvent(this, e);
			// notify all listeners

			for (ONDEXListener listener : listeners) {
				listener.eventOccurred(oe);
			}
		}
	}
	
	/**
	 * Utility that factorises an index updating operation, wrapping it into common pre/post processing, like opening the
	 * index, reopening it at the end.
	 *  
	 * @param action what to do, assuming it will return a result.
	 */
	private <R> R updateIndex ( Supplier<R> action )
	{
		openIdxWriter ();
		
		try {
			return action.get ();
		}	
		finally {
			closeIdxWriter ();
		}		
	}
	
	/**
	 * Like {@link #updateIndex(Supplier)}, but doesn't return anything, so it's easier to use this in those lambdas that
	 * don't have anything to return.
	 */
	private void updateIndexVoid ( Runnable action ) {
		this.updateIndex ( () -> { action.run (); return null; } );
	}

  /**
   * Gets a Lucene field name as it is structured in an Ondex Lucene index. 
   */
  private static String getIndexFieldName ( String idxFieldName, String idxFieldSubName )
  {
  	return idxFieldSubName == null ? idxFieldName : idxFieldName + "_" + idxFieldSubName;
  }
}
