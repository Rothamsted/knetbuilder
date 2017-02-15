package net.sourceforge.ondex.tools;

import java.util.regex.Pattern;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.RelationType;



/**
 * Utilities for manipulating meta-data.
 *
 * @author Matthew Pocock
 */
public class MetaDataUtil
{
	private ONDEXGraphMetaData metaData;
	
	private MetaDataLookup<DataSource> dataSourceLookup;
	
	public MetaDataUtil(ONDEXGraphMetaData md, MetaDataLookup<DataSource> cvl) {
		metaData = md;
		dataSourceLookup = cvl;
	}
	
    public ConceptClass safeFetchConceptClass(String id, String description, ConceptClass SBML_Parent)
    {
        ConceptClass cc = metaData.getConceptClass(id);
        if (cc == null)
        {
            cc = metaData.getFactory().createConceptClass(id, description, SBML_Parent);
        }
        return cc;
    }

    public AttributeName safeFetchAttributeName(String id, Class<?> type)
    {
        AttributeName an = metaData.getAttributeName(id);
        if (an == null)
        {
            an = metaData.getFactory().createAttributeName(id, type);
        }
        return an;
    }

    public RelationType safeFetchRelationType(String id, String description)
    {
        RelationType rt = metaData.getRelationType(id);
        if (rt == null)
        {
            rt = metaData.getFactory().createRelationType(id, description);
        }
        return rt;
    }
    
    public DataSource safeFetchDataSource(String id) {
    	DataSource dataSource = metaData.getDataSource(id);
    	if (dataSource == null) {
    		dataSource = metaData.getFactory().createDataSource(id);
    	}
    	return dataSource;
    }
    
    public EvidenceType safeFetchEvidenceType(String id) {
    	EvidenceType et = metaData.getEvidenceType(id);
    	if (et == null) {
    		et = metaData.getFactory().createEvidenceType(id);
    	}
    	return et;
    }
    
    public ConceptAccession checkAddAccession(ONDEXConcept c, String s) {
    	DataSource dataSource = null; 
    	String acc = null;
    	if (Pattern.matches("Y\\w{2}\\d{3}\\w{1}(\\-\\w)?",s)) {
    		dataSource = safeFetchDataSource("MIPS");
    		acc = s;
    	} else {
    		String[] cols = s.split("#");
    		if (cols.length == 2) {
    			dataSource = dataSourceLookup.get(cols[0]);
    			if (dataSource == null) {
    				//throw exception?
    			}
    			acc = cols[1];
    		}
    	}
    	if (dataSource != null) {
    		return c.createConceptAccession(acc, dataSource, false);
    	} else {
    		return null;
    	}
    }

}
