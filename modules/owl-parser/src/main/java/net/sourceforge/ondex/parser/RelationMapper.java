package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;

/**
 * A marker interface to declare a mapper that returns a {@link ONDEXRelation} from a source of type S.
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public interface RelationMapper<S, OT> extends ONDEXMapper<ONDEXRelation, S, OT>
{
}
