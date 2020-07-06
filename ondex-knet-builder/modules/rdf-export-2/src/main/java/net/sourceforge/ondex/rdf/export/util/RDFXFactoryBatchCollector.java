package net.sourceforge.ondex.rdf.export.util;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.jena.rdf.model.Model;

import info.marcobrandizi.rdfutils.jena.elt.ModelBatchCollector;
import net.sourceforge.ondex.rdf.export.RDFExporter;
import net.sourceforge.ondex.rdf.export.mappers.RDFXFactory;
import uk.ac.ebi.utils.threading.batchproc.ItemizedSizedBatchCollector;

/**
 * A {@link ItemizedSizedBatchCollector batch collector} for the {@link RDFExporter exporting processor}.
 * This allows to create a new {@link RDFXFactory RDF mapping factory} when the RDF processor/mapper 
 * needs it, which implies creating a new {@link Model RDF model/container} to store batches of RDF
 * to export, which are then exported by parallel jobs.   
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Nov 2019</dd></dl>
 *
 */
public class RDFXFactoryBatchCollector implements ItemizedSizedBatchCollector<RDFXFactory, Object>
{
	/**
	 * It's a delegate to this, so that the {@link RDFXFactory#getJenaModel() exporter factory model}
	 * can be used. It has a default batch size of 50000.
	 */
	private ModelBatchCollector modelCollector = new ModelBatchCollector ( 50000 );
	
	@Override
	public Supplier<RDFXFactory> batchFactory () {
		return () -> new RDFXFactory ( this.modelCollector.batchFactory ().get () );
	}

	@Override
	public BiConsumer<RDFXFactory, Object> accumulator () {
		return RDFXFactory::map;
	}
	
	/**
	 * The size of the underlining {@link RDFXFactory#getJenaModel() model}.
	 */
	@Override
	public Function<RDFXFactory, Long> batchSizer () {
		return xfact -> xfact.getJenaModel ().size ();
	}

	@Override
	public long maxBatchSize () {
		return this.modelCollector.maxBatchSize ();
	}
	
	public void setMaxBatchSize ( long maxBatchSize ) {
		this.modelCollector.setMaxBatchSize ( maxBatchSize );
	}	
}

