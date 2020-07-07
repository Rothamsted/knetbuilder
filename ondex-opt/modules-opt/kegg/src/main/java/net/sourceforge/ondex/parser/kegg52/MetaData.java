package net.sourceforge.ondex.parser.kegg52;

import java.util.HashMap;

/**
 * @author hindlem
 */
public class MetaData {

    private static HashMap<String, String> dbmap;

    /**
     * @param db the query database
     * @return the dbname in the metadata
     */
    public static String getMapping(String db) {
        if (dbmap == null) {
            dbmap = new HashMap<String, String>(20);
            dbmap.put("KAZUSA", "KAZUSA");
            dbmap.put("TAIR", CV_TAIR);
            dbmap.put("UNIPROT", CV_UNIPROT);
            dbmap.put("UPROT", CV_UNIPROT);
            dbmap.put("MIPS", "MIPS");
            dbmap.put("NCBI-GI", "NC_GI");
            dbmap.put("NCBI-GENEID", "UNIGENE");
            dbmap.put("TIGR", CV_TIGR);
            dbmap.put("NITE", "NITE");
            dbmap.put("CHEBI", CV_CHEBI);
            dbmap.put("3DMET", "3DMET");
            dbmap.put("CYORF", "CYORF");
            dbmap.put("UWASH", "UWASH");
            dbmap.put("PUBCHEM", "PUBCHEM");
            dbmap.put("JGI", "JGI");
            dbmap.put("ZFIN", "ZFIN");
            dbmap.put("NC_GI", CV_NCBI_GI);
            dbmap.put("NC_GE", CV_NCBI_GENEID);
            dbmap.put("CAS", CV_CAS);
            dbmap.put("GENEDB", "GENEDB");
            dbmap.put("KEGG", CV_KEGG);
            dbmap.put("EC", CV_EC);
            dbmap.put("GLYCOMEDB", CV_GLYCODB);
            dbmap.put("            GLYCOMEDB", CV_GLYCODB);
        }
        return dbmap.get(db.toUpperCase());
    }

    //DataSource
    public final static String CV_CHEBI = "CHEBI";
    public final static String CV_KEGG = "KEGG";
    public final static String CV_UNIPROT = "UNIPROTKB";
    public final static String CV_NCBI_GI = "NC_GI";
    public final static String CV_NCBI_GENEID = "NC_GE";
    public final static String CV_SANGER = "SA";
    public final static String CV_WORMBASE = "WB";
    public final static String CV_FLYBASE = "FLYBASE";
    public final static String CV_SAGALIST = "SA";
    public final static String CV_TAIR = "TAIR";
    public final static String CV_TIGR = "TIGR";
    public final static String CV_CAS = "CAS";
    public final static String CV_EC = "EC";
    public final static String CV_GLYCODB = "GLYCODB";

    //conceptClass
    public final static String CC_PATHWAY = "Path";
    public final static String CC_COMPOUND = "Comp";
    public final static String CC_GENE = "Gene";
    public final static String CC_PROTEIN = "Protein";
    public final static String CC_ENZYME = "Enzyme";
    public final static String CC_REACTION = "Reaction";
    public final static String CC_KEGG_ONTOLOGY = "KO";
    public final static String CC_KEGG_GENE_ORTHOLOG_GROUPS = "KOGE";
    public final static String CC_KEGG_PROTEIN_ORTHOLOG_GROUPS = "KOPR";
    public final static String CC_KEGG_ENZYME_ORTHOLOG_GROUPS = "KOEN";
    public static final String CC_PROTCOMP = "Protcmplx";
    public final static String CC_EC = "EC";
    //evidence
    public final static String EVIDENCE_IMPD = "IMPD";

    //AttributeName
    public final static String ATTR_NAME_UBIQUITOUS = "ubiquitous";
    public final static String ATTR_NAME_TAXID = "TAXID";
    public final static String ATTR_NAME_POSTAG = "pos_tag";
    public final static String ATTR_NAME_URL = "URL";
    public final static String ATTR_NAME_GRAPHICAL = "graphical";
    public final static String ATTR_NAME_AMINO_ACID = "AA";
    public final static String ATTR_NAME_NUCLEIC_ACID = "NA";

    //relationTypes
    public final static String RT_IS_A = "is_a";
    public final static String RT_IS_PART_OF = "part_of";
    public final static String RT_MEMBER_PART_OF = "member_of";
    public final static String RT_PRODUCED_BY = "pd_by";
    public final static String RT_CONSUMED_BY = "cs_by";
    public final static String RT_CATALYSED_BY = "ca_by";
    public final static String RT_EXPRESSED_BY = "ex_by";
    public final static String RT_REPRESSED_BY = "re_by";
    public final static String RT_INHIBITED_BY = "in_by";
    public final static String RT_ACTIVATED_BY = "ac_by";
    public final static String RT_INDERECTLY_EFFECTED_BY = "id_by";
    public final static String RT_PHOSPHORYLATED_BY = "phosphorylated_by";
    public final static String RT_DEPHOSPHORYLATED_BY = "dephosphorylated_by";
    public final static String RT_DISSOCIATED_FROM = "di_fr";
    public final static String RT_BINDS_TO = "bi_to";
    public final static String RT_METHYLATED_BY = "methylated_by";
    public final static String RT_DEMETHYLATED_BY = "demethylated_by";
    public final static String RT_UBIQUINATED_BY = "ubiquinated_by";
    public final static String RT_STATE_CHANGED_FROM = "st_fr";
    public final static String RT_INTERACTS_WITH = "it_wi";
    public final static String RT_INTERMEDIATE_OF = "im_of";
    public final static String RT_SHARED_INTERMEDIATE = "sh_im";
    public final static String RT_CATALYSEING_CLASS = "cat_c";
    public final static String RT_ENCODED_BY = "en_by";
    public final static String RT_GLYCOSYLATED_BY = "glycosylated_by";
    public final static String RT_DERIVES_FROM = "derives_from";
    public final static String RT_ADJACENT_TO = "adjacent_to";

	
}
