package net.sourceforge.ondex.parser;

/**
 * A marker interface that identify the mapping from a source of type S and a parameter of type P to an identifier of 
 * string type.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>6 Apr 2017</dd></dl>
 *
 */
public interface IdMapper<P, S> extends ParametricMapper<String, P, S>
{
}
