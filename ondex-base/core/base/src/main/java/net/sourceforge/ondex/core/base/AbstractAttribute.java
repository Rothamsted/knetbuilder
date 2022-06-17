package net.sourceforge.ondex.core.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.api.config.Config;
import net.sourceforge.ondex.core.api.config.ONDEXGraphRegistry;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.exception.type.StorageException;
import net.sourceforge.ondex.exception.type.WrongParameterException;

/**
 * Abstract Attribute implementation, to pull the common code from
 * ConceptAttribute and RelationAttribute into one place.
 * 
 * @author Matthew Pocock
 */
public abstract class AbstractAttribute extends AbstractONDEXEntity 
  implements Attribute, Serializable {

	/**
	 * when to start compressing strings
	 */
	private static final int COMPRESS_THRESHOLD = 100;

	/**
	 * Run compression in thread.
	 * 
	 * This is public due to bad tests elsewhere, please DO NOT USE IT.
	 * 
	 * (Old comment, probably doesn't apply anymore, MB 2020) ATTENTION: Need to be able to shutdown this Executor in 
	 * Applet destroy method! Otherwise reloading applet is not going to work.
	 */
	public static final ExecutorService COMPRESSOR;

	/**
	 * id of owning entity.
	 */
	protected final int ownerID;

	/**
	 * representing attribute name
	 */
	protected AttributeName attrname;

	/**
	 * Result of compressor thread
	 */
	protected Future<byte[]> compressed = null;

	/**
	 * the attribute value for this Attribute
	 */
	protected Object value = null;

	/**
	 * whether or not to index this Attribute
	 */
	protected boolean doIndex = true;

	/**
	 * pre-computed hash code
	 */
	protected int hashCode;

	static {
		COMPRESSOR = Executors.newWorkStealingPool();
		Runtime.getRuntime().addShutdownHook ( 
			new Thread ( () -> {
				COMPRESSOR.shutdownNow ();
				System.err.println ( "OXL attribute compressor shut down" );
			})
		);
	}
	
	
	/**
	 * Constructor
	 * 
	 * @param sid
	 * @param conceptID
	 * @param attributeName
	 * @param value
	 * @param doIndex
	 */
	protected AbstractAttribute(long sid, int conceptID,
			AttributeName attributeName, Object value, boolean doIndex) {
		this.sid = sid;
		this.ownerID = conceptID;
		this.attrname = attributeName;
		this.doIndex = doIndex;
		this.setValue(value);
		this.hashCode = value.hashCode() + attributeName.hashCode();
	}

	@Override
	public int getOwnerId() {
		return ownerID;
	}

	@Override
	public AttributeName getOfType() {
		return attrname;
	}

	@Override
	public Object getValue() {
		if (value != null)
			return value;
		else {
			// decompress value
			try {
				return decompress(compressed.get());
			} 
			catch (InterruptedException|ExecutionException ex)
			{
				throw new IllegalStateException ( String.format ( 
					"Error while decompressing value for Ondex attribute '%s': %s",
					attrname == null ? "<null>" : attrname.getId (),
					ex.getMessage ()
				), ex);
			}
		}
	}

	@Override
	public void setValue(Object value) throws NullValueException,
			UnsupportedOperationException, WrongParameterException {

		// null values not allowed
		if (value == null)
			throw new NullValueException(
					Config.properties.getProperty("GDS.ValueNull"));

		// read-only graph
		if (ONDEXGraphRegistry.graphs.get(sid).isReadOnly())
			throw new UnsupportedOperationException();

		// data type mismatch
		if (!(getOfType().getDataType().isAssignableFrom(value.getClass())))
			throw new WrongParameterException(
					Config.properties.getProperty("GDS.ObjectTypeMismatch"));

		if ( ! ( value instanceof String ) ) {
			this.value = value;
			return;
		}
		
		final String strVal = (String) value;
		if ( strVal.length() < COMPRESS_THRESHOLD ) {
			this.value = value;
			return;
		}

		compressed = COMPRESSOR.submit( () ->
		{
			Thread myThread = Thread.currentThread ();
			String myName = myThread.getName ();
			if ( ! myName.startsWith ( "[attrZip] " ) )
				myThread.setName ( "[attrZip] " + myName );
			return compress ( strVal ); 
		});
	}

	/**
	 * Compress a given String
	 * 
	 * @param s
	 *            String to compress
	 * @return byte[] compressed
	 */
	protected byte[] compress(String s)
	{

		byte[] input = s.getBytes();

		// Compressor with highest level of compression
		Deflater compressor = new Deflater();
		compressor.setLevel(Deflater.BEST_COMPRESSION);

		// Give the compressor the data to compress
		compressor.setInput(input);
		compressor.finish();

		// Create an expandable byte array to hold the compressed data.
		// It is not necessary that the compressed data will be smaller than
		// the uncompressed data.
		try ( ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length) )
		{
			// Compress the data
			byte[] buf = new byte[1024];
			while ( !compressor.finished() )
			{
				int count = compressor.deflate(buf);
				bos.write(buf, 0, count);
			}

			// Get the compressed data
			return bos.toByteArray();
		}
		catch (IOException e) {
			throw new StorageException(e);
		}
		finally {
			compressor.end();
		}
	}

	/**
	 * Decompress from a given byte array
	 * 
	 * @param compressedData
	 *            byte[]
	 * @return decompressed String
	 */
	protected String decompress(byte[] compressedData) 
	{
		// Create the decompressor and give it the data to compress
		Inflater decompressor = new Inflater();
		decompressor.setInput(compressedData);

		// Create an expandable byte array to hold the decompressed data
		try ( ByteArrayOutputStream bos = new ByteArrayOutputStream( compressedData.length) )
		{
			// Decompress the data
			byte[] buf = new byte[1024];
			while (!decompressor.finished())
			{
				try {
					int count = decompressor.inflate(buf);
					bos.write(buf, 0, count);
				} 
				catch (DataFormatException e) {
					throw new StorageException(e);
				}
			}

			// Get the decompressed data
			return new String(bos.toByteArray());
		} 
		catch (IOException e) {
			throw new StorageException(e);
		}
		finally {
			decompressor.end();
		}
	}

	@Override
	public boolean isDoIndex() {
		return doIndex;
	}

	@Override
	public void setDoIndex(boolean doIndex) throws AccessDeniedException {
		this.doIndex = doIndex;
	}

	@Override
	public boolean equals(Object o) 
	{
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof Attribute)) return false;
		
		Attribute attribute = (Attribute) o;
		return getOfType().equals(attribute.getOfType())
				&& getValue().equals(attribute.getValue());
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean inheritedFrom(AttributeName attributeName) {
		AttributeName attrname = getOfType();
		while (!attrname.equals(attributeName)) {
			attrname = attrname.getSpecialisationOf();
			if (attrname == null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int compareTo(Attribute o) {
		return this.attrname.compareTo(o.getOfType());
	}

}
