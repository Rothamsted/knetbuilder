package net.sourceforge.ondex.parser.owl;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Jul 2017</dd></dl>
 *
 */
public class DefaultRdfPropertyConfigurator implements RdfPropertyConfigurator
{
	private String propertyIri;
	
	
	public DefaultRdfPropertyConfigurator () {
	}

	public DefaultRdfPropertyConfigurator ( String propertyIri )
	{
		this.setPropertyIri ( propertyIri );
	}

	
	@Override
	public String getPropertyIri ()
	{
		return propertyIri;
	}

	@Override
	public void setPropertyIri ( String propertyIri )
	{
		this.propertyIri = propertyIri;
	}
}
