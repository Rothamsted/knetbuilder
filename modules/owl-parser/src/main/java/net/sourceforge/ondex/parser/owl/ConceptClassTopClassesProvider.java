package net.sourceforge.ondex.parser.owl;

import java.util.Collections;
import java.util.Iterator;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;

import net.sourceforge.ondex.parser.ConceptClassMapper;

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
public class ConceptClassTopClassesProvider implements TopClassesProvider
{
	private OWLConceptClassMapper conceptClassMapper;

	public ConceptClassTopClassesProvider ()
	{
		super ();
	}

	public ConceptClassTopClassesProvider ( OWLConceptClassMapper conceptClassMapper )
	{
		super ();
		this.setConceptClassMapper ( conceptClassMapper );
	}

	public OWLConceptClassMapper getConceptClassMapper ()
	{
		return conceptClassMapper;
	}

	public void setConceptClassMapper ( OWLConceptClassMapper conceptClassMapper )
	{
		this.conceptClassMapper = conceptClassMapper;
	}

	@Override
	public Iterator<OntClass> apply ( OntModel model )
	{
		OWLConceptClassMapper ccmap = this.getConceptClassMapper ();
		String rootClass = ccmap.getClassIri ();
		return Collections.singleton ( model.getOntClass ( rootClass ) ).iterator ();
	}
}