/*
 * Created on 12-May-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.ondex.parser.kegg52.util;

import net.sourceforge.ondex.parser.kegg52.MetaData;

import java.util.concurrent.ConcurrentHashMap;


/**
 * @author taubertj
 */
public class ConceptAccMapping {

    public static ConcurrentHashMap<String, String> mapping = new ConcurrentHashMap<String, String>();

    static {
        fillMapping();
    }

    private static void fillMapping() {
        mapping.put("UniProt", MetaData.CV_UNIPROT);
        mapping.put("NCBI-GI", MetaData.CV_NCBI_GI);
        mapping.put("NCBI-GeneID", MetaData.CV_NCBI_GENEID);
        mapping.put("Sanger", MetaData.CV_SANGER);
        mapping.put("WormBase", MetaData.CV_WORMBASE);
        mapping.put("FlyBase", MetaData.CV_FLYBASE);
        mapping.put("SagaList", MetaData.CV_SAGALIST);
        mapping.put("JCVI-CMR", MetaData.CV_SANGER);
    }
}
