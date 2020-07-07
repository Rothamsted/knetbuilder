package net.sourceforge.ondex.parser.ahd;

public interface MetaData {

    // evidence types
    public static final String evidence = "IMPD";
    public static final String etGOA = "GOA";
    public static final String etGI = "GI";

    // cv
    public static final String ahd_cv = "AHD";
    public static final String tair_cv = "TAIR";
    public static final String pubmed_cv = "PubMed";

    // concept classes
    public static final String mutant = "At_Mutant";
    public static final String hormone = "Plant_Hormone";
    public static final String ahdpo = "AHDPO";
    public static final String exp = "Experiment";
    public static final String gene = "Gene";
    public static final String publication = "Publication";
    public static final String microarrayexp = "MicroArrayExperiment";


    // relation types
    public static final String rtISA = "is_a";
    public static final String rtPub = "pub_in";
    public static final String rtPP = "participates_in";
    public static final String rtOP = "has_observ_pheno";

    public static final String rtAssoc = "assoc_hormone";
    public static final String rtTreat = "hormone_treated";
    public static final String rtNotTreat = "hormone_control";
    public static final String rtHasMutatedGene = "has_mutated_gene";
    public static final String rtPart = "participates_in";
    public static final String rtContrib = "contrib";


    // attribute names
    public static final String antlconf = "2Lconf";
    public static final String anDominance = "dominance";
    public static final String anMut = "mutagenesis_type";
    public static final String anEco = "ecotype";
    public static final String anPT = "PlantType";
    public static final String anDesc = "description";
    public static final String anSite = "mutatedSite";
    public static final String anGeneRole = "gene_role";
    public static final String anGOA = "GOA";
    public static final String anMicro = "microarray";


}
