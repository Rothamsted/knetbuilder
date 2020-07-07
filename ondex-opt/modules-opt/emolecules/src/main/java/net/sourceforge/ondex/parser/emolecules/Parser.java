package net.sourceforge.ondex.parser.emolecules;
/*
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.parser.ONDEXParser;

/**
 *
 * @author grzebyta
 */
/**
public class Parser extends ONDEXParser{

    public String getId() {
        return "emolecules";
    }

    public String getName() {
        return "eMolecules parser";
    }

    public String getVersion() {
        return "0.1";
    }

    /**
     * This specifies the input file to the parser
     * @return 
     */

/**
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        
        FileArgumentDefinition fad = new FileArgumentDefinition(
                FileArgumentDefinition.INPUT_FILE,
                FileArgumentDefinition.INPUT_FILE_DESC,
                true,
                true,
                false);
        
        return new ArgumentDefinition[] {fad};
    }

    public void start() throws Exception {
        
        // get the full inputfile path
        String inFileName = this.args
                .getUniqueValue(FileArgumentDefinition.INPUT_FILE).toString();
        ConceptClass cc = this.graph.getMetaData().getConceptClass("Comp");
        
        // create datasource
        DataSource emolDataSource = this.graph
                .getMetaData().getFactory().createDataSource("emolecules");
        
        // create evidence type
        EvidenceType evType = this.graph.getMetaData().getEvidenceType("IMPD");
    }

    public String[] requiresValidators() {
        return new String[0];
    }
    
}
**/

