package net.sourceforge.ondex.parser.wordnet;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import org.apache.log4j.Level;

import java.io.File;
import java.util.ArrayList;

/**
 * Parser for WORDNET.
 *
 * @author rwinnenb
 * @version 3.00 10.03.2008
 */
public class Parser extends ONDEXParser
{

    private ArrayList<Entity> wCollection;

    private static Parser instance;

    public String getName() {
        return new String("Wordnet parser");
    }

    public String getVersion() {
        return new String("10.03.2009");
    }

    @Override
    public String getId() {
        return "wordnet";
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
    	FileArgumentDefinition inputDir = new FileArgumentDefinition(
				FileArgumentDefinition.INPUT_DIR,
				"directory with generif files", true, true, true, false);
		return new ArgumentDefinition<?>[] { inputDir };
    }

    static void propagateEventOccurred(EventType et) {
        if (instance != null)
            instance.fireEventOccurred(et);
    }

    @Override
    public void start() throws Exception {

        instance = this;

        GeneralOutputEvent so = new GeneralOutputEvent(
                "Starting Wordnet parsing...", "[Parser - start]");
        so.setLog4jLevel(Level.INFO);
        fireEventOccurred(so);

        Writer writer = new Writer(graph, args, instance);

        File dir = new File((String) args
				.getUniqueValue(FileArgumentDefinition.INPUT_DIR));
        
        wCollection = new WNExtractor().start(dir.getAbsolutePath());
        //System.out.println("Size: " + wCollection.size());
        writer.write(wCollection);

        GeneralOutputEvent so1 = new GeneralOutputEvent(
                "Wordnet parsing finished!", "[Parser - start]");
        so1.setLog4jLevel(Level.INFO);
        fireEventOccurred(so1);

    }

    @Override
    public String[] requiresValidators() {
        return new String[]{"cvregex"};
    }

}