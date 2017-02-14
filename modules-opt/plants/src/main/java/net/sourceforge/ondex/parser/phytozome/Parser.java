package net.sourceforge.ondex.parser.phytozome;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.DataURL;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.annotations.metadata.AttributeNameRequired;
import net.sourceforge.ondex.annotations.metadata.ConceptClassRequired;
import net.sourceforge.ondex.annotations.metadata.DataSourceRequired;
import net.sourceforge.ondex.annotations.metadata.EvidenceTypeRequired;
import net.sourceforge.ondex.annotations.metadata.RelationTypeRequired;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.IntegerRangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;


import org.apache.log4j.Level;

/**
 * Parser for any PHYTOZOME species.
 * <p/>
 * Requires files from annotation directories of parsed species:
 * ftp://ftp.jgi-psf.org/pub/JGI_data/phytozome/
 * 
 *
 * @author keywan
 */
@Status(description = "Tested November 2013 for Arabidopsis, Rice, Poplar, Brachypodium in Phytozome 9.1 (Keywan Hassani-Pak)", status = StatusType.STABLE)
@Authors(authors = {"Keywan Hassani-Pak"}, emails = {"keywan at users.sourceforge.net"})
@DatabaseTarget(name = "phytozome", description = "Phytozome is a joint project of JGI and CIG to facilitate comparative genomic studies amongst green plants. This Phytozome parser creates Chromosome, Scaffold, Gene, CDS and Protein concepts.", version = "9.0", url = "http://phytozome.net/")
@DataURL(name = "Phytozome annotation directory",
        description = "This parser requires as input the annotation directory of a specific species. Do not change the file names. It parses the GFF3, peptide FASTA, CDS FASTA and Synonyms TXT file inside of the annotation directory. It was tested on Arabidopsis, rice, poplar and brachypodium.",
        urls = {"ftp://ftp.jgi-psf.org/pub/JGI_data/phytozome/v9.0/"})
@Custodians(custodians = {"Keywan Hassani-pak"}, emails = {"keywan at users.sourceforge.net"})
@DataSourceRequired(ids = {MetaData.DS_PHYTOZOME })
@ConceptClassRequired(ids = {MetaData.CC_PROTEIN, MetaData.CC_CDS, MetaData.CC_GENE, MetaData.CHROMOSOME, MetaData.SCAFFOLD })
@EvidenceTypeRequired(ids = {MetaData.ET_IMPD}) 
@RelationTypeRequired(ids = {MetaData.RT_ENCODES}) 
@AttributeNameRequired(ids = {MetaData.AN_AA, MetaData.AN_BEGIN, MetaData.AN_END, MetaData.AN_NA, MetaData.AN_STR, MetaData.AN_TAXID, MetaData.CHROMOSOME, MetaData.SCAFFOLD})
public class Parser extends ONDEXParser{

    //global storage of gene, protein and chromosome IDs
    private Registry genomeReg;

    public Parser() {
        genomeReg = new Registry();
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR,
                        "Path to a phytozome annotation directory for a given species, e.g. phytozome/v7.0/Osativa/annotation/", true, true, true, false),
                new StringArgumentDefinition(ArgumentNames.TAXID_ARG, ArgumentNames.TAXI_ARG_DESC, true, null, false),
                new StringArgumentDefinition(ArgumentNames.ACC_DATASOURCE_ARG, ArgumentNames.ACC_DATASOURCE_DESC, false, "ENSEMBL", false),
                new IntegerRangeArgumentDefinition(ArgumentNames.NUM_CHROMOSOMES_ARG, ArgumentNames.NUM_CHROMOSOMES_ARG_DESC, false, 19, 1, 100),
                new BooleanArgumentDefinition(ArgumentNames.SYNONYMS_PREF_ARG, ArgumentNames.SYNONYMS_PREF_DESC, false, false)
        };
    }

    @Override
    public String getName() {
        return "Phytozome";
    }

    @Override
    public String getVersion() {
        return "08/04/2011";
    }

    @Override
    public String getId() {
        return "phytozome";
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {
        GeneralOutputEvent goe = new GeneralOutputEvent("Start parsing Phytozome organism...", "[Parser - start()]");
        goe.setLog4jLevel(Level.INFO);
        fireEventOccurred(goe);
        
        //parse FASTA, GFF3 and SYNONYM files
        GenomicParser genomic = new GenomicParser(genomeReg);
        genomic.start(graph, args, this);

        goe = new GeneralOutputEvent("Successfully parsed Phytozome.", "[Parser - start()]");
        fireEventOccurred(goe);
    }

    /**
     * @return the poplarReg
     */
    public Registry getPoplarReg() {
        return genomeReg;
    }

}
