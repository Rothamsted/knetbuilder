package net.sourceforge.ondex.parser2;

import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.collections15.iterators.IteratorChain;
import org.apache.jena.ontology.OntClass;

import com.sun.xml.internal.xsom.impl.scd.Iterators;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.utils.ONDEXElemWrapper;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 May 2017</dd></dl>
 *
 */
public abstract class TreeParserScanner<S> implements ParserScanner<S, Stream<ONDEXElemWrapper<ONDEXConcept>>>
{
	private ConceptMapper<S> conceptMapper;
	
	@Override
	public Stream<ONDEXElemWrapper<ONDEXConcept>> scan ( S source )
	{
	}

	protected Iterator<ParserSourceItem<S>> scanTree ( ParserSourceItem<S> rootSourceItem )
	{
		Iterator<ParserSourceItem<S>> result = Iterators.empty ();
		Iterable<ParserSourceItem<S>> itr = () -> scanChildren ( rootSourceItem );

		for ( ParserSourceItem<S> child: itr )
			result = new IteratorChain<ParserSourceItem<S>> ( result, scanTree ( child ) );
		return result;
	}
	
	protected abstract Iterator<ParserSourceItem<S>> scanSourceItems ( S source );
	
	protected abstract Iterator<ParserSourceItem<S>> scanChildren ( ParserSourceItem<S> sourceItem );
}
