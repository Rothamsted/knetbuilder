package net.sourceforge.ondex.parser.glyco;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FileFilter;

/**
 * A parser for GlycomeDB.
 *
 * @author taubertj
 */
@DatabaseTarget(name = "GlycomeDB", description = "A carbohydrate structure metadatabase", version = "", url = "http://www.glycome-db.org")
@Authors(authors = {"Jan Taubert"}, emails = {"jantaubert at users.sourceforge.net"})
@Custodians(custodians = {"Victor Lesk"}, emails = {"v.lesk at imperial.ac.uk"})
public class Parser extends ONDEXParser implements ArgumentNames {

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{new BooleanArgumentDefinition(
                EXPANDED_ARG, EXPANDED_ARG_DESC, false, false),
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR, FileArgumentDefinition.INPUT_DIR_DESC, true, true, true, false)
        };
    }

    @Override
    public String getName() {
        return "GlycomeDB-template";
    }

    @Override
    public String getVersion() {
        return "12.05.2009";
    }

    @Override
    public String getId() {
        return "glyco";
    }


    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {

        // get parser arguments for expanded
        boolean expanded = (Boolean) args.getUniqueValue(EXPANDED_ARG);

        // which directory to parse XML files
        File dir = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_DIR));

        fireEventOccurred(new GeneralOutputEvent("Parsing files from "
                + dir.getAbsolutePath(), "[Parser - start]"));

        // get all xml files from directory
        File[] files = dir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("xml");
            }
        });

        // parse every xml file
        int nbFiles = files.length;
        for (int i = 0; i < files.length; i++) {
            parse(files[i], expanded);
            if (i % 1000 == 0) {
                fireEventOccurred(new GeneralOutputEvent("Processing finished "
                        + i + " out of " + nbFiles, "[Parser - start]"));
            }
        }
    }

    SAXBuilder builder = new SAXBuilder();


    /**
     * Parses XML file and performs transformation into ONDEX entities.
     *
     * @param file     XML file to parse
     * @param expanded use expanded representation
     * @throws Exception
     */
    private void parse(File file, boolean expanded) throws Exception {

        // transform XML document into DOM
        Document doc = builder.build(file);

        // get root of XML document
        Element root = doc.getRootElement();

    }

}
