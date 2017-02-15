package net.sourceforge.ondex.core;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * This class represents the evidence type belonging to an Evidence for a
 * Concept or Relation. It has a mandatory name and an optional description
 * field for additional information.
 * 
 * @author taubertj
 * 
 */
@XmlJavaTypeAdapter(net.sourceforge.ondex.webservice.Adapters.AnyTypeAdapter.class)
public interface EvidenceType extends MetaData {
}
