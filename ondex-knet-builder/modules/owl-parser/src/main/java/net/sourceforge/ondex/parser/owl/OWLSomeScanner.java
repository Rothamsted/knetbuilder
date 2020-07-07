package net.sourceforge.ondex.parser.owl;

import org.apache.jena.ontology.Restriction;
import org.apache.jena.ontology.SomeValuesFromRestriction;
import org.apache.jena.rdf.model.Resource;

public class OWLSomeScanner extends OWLRestrictionScanner<SomeValuesFromRestriction> 
{
	@Override
	protected SomeValuesFromRestriction asRestriction ( Restriction ontRestriction )
	{
		if ( !ontRestriction.isSomeValuesFromRestriction () ) return null;
		return ontRestriction.asSomeValuesFromRestriction ();
	}

	@Override
	protected Resource getRestrictionClass ( SomeValuesFromRestriction someRestr )
	{
		return someRestr.getSomeValuesFrom ();
	}
}
