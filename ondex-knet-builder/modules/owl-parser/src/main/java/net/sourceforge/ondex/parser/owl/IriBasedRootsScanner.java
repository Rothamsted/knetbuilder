package net.sourceforge.ondex.parser.owl;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.parser.Scanner;

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
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );


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
		OntClass ontClass = Optional.ofNullable ( model.getOntClass ( topClsIri ) )
			.orElseThrow ( () -> new NullPointerException ( 
				"There is no OWL class for the URI '" + topClsIri + "', plese review the OWL parser configuration" ) 
		);		
		return Collections.singleton ( ontClass )
			.stream ()
			.peek ( cls -> log.info ( "Scanning from the class <{}>", cls.getURI () ) );
	}
}