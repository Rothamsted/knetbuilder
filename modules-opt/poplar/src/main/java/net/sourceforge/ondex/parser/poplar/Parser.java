package net.sourceforge.ondex.parser.poplar;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.parser.poplar.genomic.GenomicPoplarParserV2;
import net.sourceforge.ondex.parser.poplar.qtl.QTLPoplarParser;

import org.apache.log4j.Level;

/**
 * Parser for Poplar sequences and functional annotations.
 * <p/>
 * Files can be downloaded from:
 * http://genome.jgi-psf.org/Poptr1_1/Poptr1_1.download.ftp.html
 *
 * @author keywan
 */
public class Parser extends ONDEXParser
{

    public static final String POPLAR_TAX_ID = "3694";

    //global storage for Poplar gene and protein IDs
    private Registry poplarReg;


    public Parser() {
        poplarReg = new Registry();
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR,
                        FileArgumentDefinition.INPUT_DIR_DESC, true, true, true, false)
        };
    }

    @Override
    public String getName() {
        return "Poplar Genome";
    }

    @Override
    public String getVersion() {
        return "25/02/2009";
    }

    @Override
    public String getId() {
        return "poplar";
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {
        GeneralOutputEvent goe = new GeneralOutputEvent("Start parsing Poplar genome...", "[Parser - start()]");
        goe.setLog4jLevel(Level.INFO);
        fireEventOccurred(goe);

        //parse genes, proteins, chromosomes
        GenomicPoplarParserV2 genomic = new GenomicPoplarParserV2(poplarReg);
        genomic.start(graph, args, this);

        //QTL data
//        QTLPoplarParser qtl = new QTLPoplarParser(poplarReg);
//        qtl.start(graph, args, this);
//
//        //parse GO annotations
//        GoaPoplarParser goa = new GoaPoplarParser(poplarReg);
//        goa.start(graph, pa, this);
//
//        //parse EC and KEGG annotations
//        PathwayPoplarParser pathway = new PathwayPoplarParser(poplarReg);
//        pathway.start(graph, pa, this);
//
//        //parse TranscriptionFactors
//        TFPoplarParser tf = new TFPoplarParser(poplarReg);
//        tf.start(graph, pa, this);

        //add more parser

        goe = new GeneralOutputEvent("Successfully parsed Poplar genome.", "[Parser - start()]");
        fireEventOccurred(goe);
    }

    /**
     * @return the poplarReg
     */
    public Registry getPoplarReg() {
        return poplarReg;
    }

}
