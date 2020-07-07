package net.sourceforge.ondex.core.util.prototypes;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.DataSource;

/**
 * Facility to be used to create a constant value {@link ConceptAccession}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>6 Jun 2017</dd></dl>
 *
 */
public class AccessionPrototype
{
	private String accession;
	private boolean isAmbiguous;
	private DataSource dataSource;
	private DataSourcePrototype dataSourcePrototype;

	public AccessionPrototype ()
	{
		this ( "", (DataSourcePrototype) null, false );
	}

	public AccessionPrototype ( String accession, DataSource dataSource, boolean ambiguous )
	{
		this.setAccession ( accession );
		this.setDataSource ( dataSource );
		this.setAmbiguous ( ambiguous );
	}

	public AccessionPrototype ( String accession, DataSourcePrototype dataSourceProto, boolean ambiguous )
	{
		this ( accession, (DataSource) null, ambiguous );
		this.setDataSourcePrototype ( dataSourceProto );
	}
	
	
	public String getAccession ()
	{
		return accession;
	}


	public void setAccession ( String accession )
	{
		this.accession = accession;
	}


	public DataSource getDataSource ()
	{
		return dataSource;
	}

	public void setDataSource ( DataSource dataSource )
	{
		this.dataSource = dataSource;
	}

	public DataSourcePrototype getDataSourcePrototype ()
	{
		return dataSourcePrototype;
	}

	public void setDataSourcePrototype ( DataSourcePrototype dataSourcePrototype )
	{
		this.dataSourcePrototype = dataSourcePrototype;
	}

	public boolean isAmbiguous ()
	{
		return isAmbiguous;
	}

	public void setAmbiguous ( boolean isAmbiguous )
	{
		this.isAmbiguous = isAmbiguous;
	}
}
