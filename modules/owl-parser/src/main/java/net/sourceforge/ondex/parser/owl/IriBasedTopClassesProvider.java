package net.sourceforge.ondex.parser.owl;

import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.ConceptClassMapper;
import net.sourceforge.ondex.parser2.Scanner;

/**
 * A {@link TopClassesProvider} that returns a root class from a specified URI corresponding to an OWL class on top
 * of a tree of interest (eg, GO biological process). Such URI is wrapped by a {@link ConceptClassMapper}, which is 
 * also used to map the class to a corrsponding concept class. See Spring configurations (e.g., go_cfg.xml) for examples
 * on how to set up this component.  
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Jun 2017</dd></dl>
 *
 */
public class IriBasedTopClassesProvider implements Scanner<OntModel, OntClass>
{
	private String topClassIri;

	public IriBasedTopClassesProvider ()
	{
		super ();
	}

	public IriBasedTopClassesProvider ( String topClassIri )
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