package net.sourceforge.ondex.rdf.rdf2oxl.support;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

/**
 * # A special {@link QueryProcessor} for relations
 * 
 * This just gets some summary information from {@link GraphSummaryHandler}, in order to compare these to the relations
 * collected via the one-by-one query. 
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Dec 2018</dd></dl>
 *
 */
@Component ( "relationProcessor" )
public class RelationProcessor extends QueryProcessor implements Resettable
{
	private long relationsCount = -1;

	// This doesn't work with maps @Autowired @Qualifier ( "graphSummary" )
	@Resource ( name = "graphSummary" )
	private Map<String, Object> graphSummary;
	
	@Override
	public void process ( String resourcesQuery, Object... opts )
	{
		super.process ( resourcesQuery, opts );
		
		if ( relationsCount == -1 ) {
			relationsCount = this.lastExecutionCount; return;
		}
		
		this.relationsCount += this.lastExecutionCount;
		long summaryCount = (long) graphSummary.get ( "relationsCount" );
		
		// Let's do this sanity check
		if ( this.relationsCount != summaryCount ) log.warn ( 
			"Mismatch between SPARQL-counted relations ({}) and those collected ({})",
			summaryCount, this.relationsCount 
		);
	}

	/**
	 * Resets the processed relations internal counter.  
	 */
	@Override
	public void reset () {
		this.relationsCount = -1;
	}
	
}
