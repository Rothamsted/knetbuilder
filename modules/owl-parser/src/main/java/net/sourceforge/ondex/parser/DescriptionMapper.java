package net.sourceforge.ondex.parser;

/**
 * A marker interface that identify the mapping from a source of type S and a parameter of type P to a description of 
 * string type.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Apr 2017</dd></dl>
 *
 */
public interface DescriptionMapper<P, S> extends ParametricMapper<String, P, S>
{
}
