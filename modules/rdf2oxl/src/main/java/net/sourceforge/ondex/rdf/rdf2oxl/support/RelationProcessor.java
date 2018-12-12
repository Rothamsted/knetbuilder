package net.sourceforge.ondex.rdf.rdf2oxl.support;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Dec 2018</dd></dl>
 *
 */
@Component ( "relationProcessor" )
public class RelationProcessor extends QueryProcessor implements Resettable
{
	private long relationsCount = -1;

	// @Autowired @Qualifier ( "graphSummary" )
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

	@Override
	public void reset () {
		this.relationsCount = -1;
	}
	
}
