package net.sourceforge.ondex.tools.tab.importer;

import static uk.ac.ebi.utils.exceptions.ExceptionUtils.buildEx;
import static uk.ac.ebi.utils.exceptions.ExceptionUtils.throwEx;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import uk.ac.ebi.utils.exceptions.UncheckedFileNotFoundException;
import uk.ac.ebi.utils.exceptions.UnexpectedValueException;

/**
 * The old delimited reader Ondex interface, rewritten using the opencsv library.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Nov 2020</dd></dl>
 *
 * TODO: tests.
 * 
 */
public class DelimitedReader extends DataReader
{
	/**
	 * Options accepted by the constructor.
	 * 
	 */
	public static final String OPT_SEPARATOR = "fieldSeparator", 
		OPT_QUOTE_CHAR = "quoteChar",
		OPT_CHARSET = "charset",
		OPT_ESCAPE_CHAR = "escapeChar";
	
	private String filePath = null;
	private CSVReader csvReader = null;
	private int maxLines = Integer.MAX_VALUE;
	
	/**
	 * Variant with array of key/value.
	 */
  public DelimitedReader ( String filePath, Object... opts )
  {
  	initFromFilePath ( filePath, opts );
  }

	
  public DelimitedReader ( String filePath, Map<String, Object> opts )
  {
  	initFromFilePath ( filePath, opts );
  }
	

  public DelimitedReader ( String filePath, char separator, char quoteChar )
  {
  	this ( filePath, OPT_SEPARATOR, separator, OPT_QUOTE_CHAR, quoteChar );
	}

  public DelimitedReader ( String filePath, char delimiter )
  {
  	this ( filePath, delimiter, '"' );
	}

  public DelimitedReader ( String filePath )
  {
  	this ( filePath, '\t' );
	}

  public DelimitedReader ( CSVReader csvReader )
  {
  	this.csvReader = csvReader;
	}
  
  
  
	@Override
	public String[] readLine ()
	{
		if ( !hasNext () ) throw new NoSuchElementException ( "DelimitedReader hasn't any further element" );
		try
		{
			return this.csvReader.readNext ();
		}
		catch ( IOException ex )
		{
			throw buildEx ( UncheckedIOException.class, ex, 
				"I/O Error while reading the CSV file %s: %s", 
				Optional.ofNullable ( filePath ).map ( p -> '"' + p + "'" ).orElse ( "<NA>" ),
				ex.getMessage () 
			);
		}
		catch ( CsvValidationException ex )
		{
			throw buildEx ( UnexpectedValueException.class, ex, 
				"Validation error while reading the CSV file %s: %s", 
				Optional.ofNullable ( filePath ).map ( p -> '"' + p + "'" ).orElse ( "<NA>" ),
				ex.getMessage () 
			);
		}		
	}

	@Override
	public boolean hasNext ()
	{
		try
		{
			if ( csvReader.getLinesRead () >= this.maxLines ) return false;
			return csvReader.peek () != null;
		}
		catch ( IOException ex )
		{
			throw buildEx ( UncheckedIOException.class, ex, 
				"Error while reading the CSV file %s: %s", 
				Optional.ofNullable ( filePath ).map ( p -> '"' + p + "'" ).orElse ( "<NA>" ),
				ex.getMessage () 
			);
		}
	}

	/**
	 * It can only go forward. Errors are expected to occur if you try to go back from the current point.
	 */
	@Override
	public void setLine ( int lineNumber )
	{
		try {
			this.csvReader.skip ( lineNumber );
		}
		catch ( IOException ex )
		{
			throwEx ( UncheckedIOException.class, ex, 
				"I/O Error while skipping lines on the CSV file %s: %s", 
				Optional.ofNullable ( filePath ).map ( p -> '"' + p + "'" ).orElse ( "<NA>" ),
				ex.getMessage () 
			);
		}
	}

	@Override
	public void setLastLine ( int lastLineExcluded )
	{
		this.maxLines = lastLineExcluded;
	}

	@Override
	public void close ()
	{
		try {
			this.csvReader.close ();
		}
		catch ( IOException ex )
		{
			throwEx ( UncheckedIOException.class, ex, 
				"I/O Error while closing the CSV file %s: %s", 
				Optional.ofNullable ( filePath ).map ( p -> '"' + p + "'" ).orElse ( "<NA>" ),
				ex.getMessage () 
			);
		}
	}

	private void initFromFilePath ( String filePath, Object... opts )
	{
		Map<String, Object> optsm = new HashMap<> ();
		for ( int i = 0; i < opts.length -2; i++ )
		{
			String key = (String) opts [ i ];
			Object val = opts [ ++i ];
			optsm.put ( key, val );
		}
		initFromFilePath ( filePath, optsm );
	}

	
	private void initFromFilePath ( String filePath, Map<String, Object> opts )
	{
		try
		{
			this.filePath = filePath;
			
			CSVParser csvParser = new CSVParserBuilder ()
				.withSeparator ( (char) opts.getOrDefault ( OPT_SEPARATOR, '\t' ) )
				.withQuoteChar ( (char) opts.getOrDefault ( OPT_QUOTE_CHAR, '"' ) )
				.withEscapeChar ( (char) opts.getOrDefault ( OPT_ESCAPE_CHAR, '\\' ) )
				.build ();
			
			String encoding = (String) opts.getOrDefault ( OPT_CHARSET, "UTF-8" );
			
			Reader reader = new BufferedReader ( new FileReader ( filePath, Charset.forName ( encoding ) ), 1 << 20 );
			
			this.csvReader = new CSVReaderBuilder ( reader )
				.withCSVParser ( csvParser )
				.build ();
		}
		catch ( FileNotFoundException ex )
		{
			throwEx ( UncheckedFileNotFoundException.class, ex, 
				"CSV file '%s' not found", filePath 
			);
		}
		catch ( IOException ex )
		{
			throwEx ( UncheckedIOException.class, ex, 
				"Error while opening CSV file '%s': %s", filePath, ex.getMessage () 
			);
		}
	}
	
}
