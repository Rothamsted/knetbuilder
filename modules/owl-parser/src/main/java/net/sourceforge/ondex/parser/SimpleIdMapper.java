package net.sourceforge.ondex.parser;

/**
 * This is a marker interface semantically similar to {@link IdMapper}, but the idea here is that 
 * a parameter of type P is mapped to a string. The source (S) doesn't appear hereby, since this mapper is supposed to
 * either maps from data already extracted from S (e.g., an XML Element), or to get a required source from some 
 * pointer in the P type. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Apr 2017</dd></dl>
 *
 */
public interface SimpleIdMapper<P> extends Mapper<String, P>
{
}
