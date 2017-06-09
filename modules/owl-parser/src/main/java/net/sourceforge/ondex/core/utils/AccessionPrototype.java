package net.sourceforge.ondex.core.utils;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.base.ConceptAccessionImpl;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>6 Jun 2017</dd></dl>
 *
 */
public class AccessionPrototype extends ConceptAccessionImpl
{
	private DataSourcePrototype dataSourcePrototype;

	protected AccessionPrototype ( String accession, DataSource elementOf, boolean ambiguous )
	{
		super ( -1, -1, accession, elementOf, ambiguous );
	}

}
