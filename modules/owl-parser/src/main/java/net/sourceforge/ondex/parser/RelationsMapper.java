package net.sourceforge.ondex.parser;

import java.util.stream.Stream;

import net.sourceforge.ondex.core.ONDEXRelation;

/**
 * A marker interface to declare a mapper that returns multiple {@link ONDEXRelation}s from a source of type S.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Apr 2017</dd></dl>
 *
 */
public interface RelationsMapper<S> extends ONDEXMapper<Stream<ONDEXRelation>, S>
{
}
