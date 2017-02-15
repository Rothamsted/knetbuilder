package net.sourceforge.ondex.tools.subgraph;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * 
 * @author lysenkoa
 *
 */
abstract class GraphEntityPrototype {
	
	protected static EvidenceType createEvidence(ONDEXGraphMetaData meta, String type) throws NullValueException, EmptyStringException{
		EvidenceType evidence = meta.getEvidenceType(type);
		if(evidence == null)evidence = meta.getFactory().createEvidenceType(type);
		return evidence;
	}
	
	protected static ConceptClass createCC(ONDEXGraphMetaData meta, String type) throws NullValueException, EmptyStringException{
		ConceptClass cc = meta.getConceptClass(type);
		if(cc == null)cc = meta.getFactory().createConceptClass(type);
		return cc;
	}
	
	protected static DataSource createDataSource(ONDEXGraphMetaData meta, String type) throws NullValueException, EmptyStringException{
		DataSource dataSource = meta.getDataSource(type);
		if(dataSource == null)dataSource = meta.getFactory().createDataSource(type);
		return dataSource;
	}
	
	protected static RelationType createRT(ONDEXGraphMetaData meta, String type) throws NullValueException, EmptyStringException{
		RelationType rt = meta.getRelationType(type);
		if(rt == null)rt = meta.getFactory().createRelationType(type);
		return rt;
	}
	
	protected static AttributeName createAttName(ONDEXGraphMetaData meta, String type, Class<?> cls) throws NullValueException, EmptyStringException{
		AttributeName att = meta.getAttributeName(type);
		if(att == null)att = meta.getFactory().createAttributeName(type, cls);
		return att;
	}
}
