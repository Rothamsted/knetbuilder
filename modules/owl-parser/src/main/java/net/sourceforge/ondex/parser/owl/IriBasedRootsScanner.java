package net.sourceforge.ondex.parser.owl;

import java.util.Collections;
import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;

import net.sourceforge.ondex.parser2.Scanner;

/**
 * An adapter to {@link Scanner} that returns an OWL class based on its {@link #getTopClassIri() IRI/URI}. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 Aug 2017</dd></dl>
 *
 */
public class IriBasedRootsScanner implements Scanner<OntModel, OntClass>
{
	private String topClassIri;

	public IriBasedRootsScanner ()
	{
		super ();
	}

	public IriBasedRootsScanner ( String topClassIri )
	{
		super ();
		this.setTopClassIri ( topClassIri ); 
	}


	public String getTopClassIri ()
	{
		return topClassIri;
	}

	public void setTopClassIri ( String topClassIri )
	{
		this.topClassIri = topClassIri;
	}

	@Override
	public Stream<OntClass> scan ( OntModel model )
	{
		String topClsIri = this.getTopClassIri ();
		return Collections.singleton ( model.getOntClass ( topClsIri ) ).stream ();
	}
}