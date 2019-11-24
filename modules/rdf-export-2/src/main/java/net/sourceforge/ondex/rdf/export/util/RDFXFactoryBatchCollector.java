package net.sourceforge.ondex.rdf.export.util;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import info.marcobrandizi.rdfutils.jena.elt.ModelBatchCollector;
import net.sourceforge.ondex.rdf.export.mappers.RDFXFactory;
import uk.ac.ebi.utils.threading.batchproc.ItemizedSizedBatchCollector;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Nov 2019</dd></dl>
 *
 */
public class RDFXFactoryBatchCollector implements ItemizedSizedBatchCollector<RDFXFactory, Object>
{
	private ModelBatchCollector modelCollector = new ModelBatchCollector ( 50000 );
	
	@Override
	public Supplier<RDFXFactory> batchFactory () {
		return () -> new RDFXFactory ( this.modelCollector.batchFactory ().get () );
	}

	@Override
	public BiConsumer<RDFXFactory, Object> accumulator () {
		return RDFXFactory::map;
	}
	
	@Override
	public Function<RDFXFactory, Long> batchSizer () {
		return xfact -> xfact.getGraphModel ().size ();
	}

	@Override
	public long maxBatchSize () {
		return this.modelCollector.maxBatchSize ();
	}
	
	public void setMaxBatchSize ( long maxBatchSize ) {
		this.modelCollector.setMaxBatchSize ( maxBatchSize );
	}	
}

