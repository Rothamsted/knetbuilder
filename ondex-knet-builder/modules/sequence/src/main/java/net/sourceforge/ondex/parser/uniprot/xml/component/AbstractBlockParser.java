package net.sourceforge.ondex.parser.uniprot.xml.component;

/**
 * 
 * @author peschr
 *
 */
import net.sourceforge.ondex.parser.uniprot.xml.ComponentParser;
import net.sourceforge.ondex.parser.uniprot.xml.filter.ValueFilter;

public abstract class AbstractBlockParser implements ComponentParser{
	
	protected ValueFilter filter;
	
	protected boolean filtered = true;
	
	public boolean filter(){
		return false;
	}
	
}
