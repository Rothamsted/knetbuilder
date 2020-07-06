package net.sourceforge.ondex.core;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * This class provides information about the Class of a Concept. It has a
 * mandatory name and an optional description field for additional information.
 * A ConceptClass can be a specialisation of another ConceptClass.
 * 
 * @author taubertj
 * 
 */
@XmlJavaTypeAdapter(net.sourceforge.ondex.webservice.Adapters.AnyTypeAdapter.class)
public interface ConceptClass extends MetaData, Hierarchy<ConceptClass> {

	/**
	 * Returns whether this concept class is a transitive superclass of
	 * <code>cc</code>.
	 */
	public boolean isAssignableFrom(ConceptClass cc);
}
