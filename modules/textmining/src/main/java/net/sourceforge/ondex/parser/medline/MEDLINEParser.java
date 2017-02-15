package net.sourceforge.ondex.parser.medline;

import java.io.IOException;
import java.net.MalformedURLException;
import javax.xml.stream.XMLStreamException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.medline.args.ArgumentNames;
import net.sourceforge.ondex.parser.medline.data.Abstract;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Class for parsing MEDLINE XML files.
 *
 * @author winnenbr, keywan
 * @version 4.00 10/06/08
 */
public class MEDLINEParser {

    private AbstractWriter writer;

    private ImportSession importsession;

    private ONDEXGraph graph;

    private XMLParser xmlParser;

    int files_written;


    /**
     * Parses all data from all MEDLINE files. Data to be parsed can be
     * specified through parser arguments evaluated in Importsession object.
     *
     * @param is Importsession specific data
     * @param ag the ONDEX graph
     * @param pa parser arguments when called from back end
     */
    public void parse(ImportSession is, ONDEXGraph ag, ONDEXPluginArguments pa) throws InvalidPluginArgumentException, 
            IOException, XMLStreamException, MetaDataMissingException {

        this.importsession = is;
        this.graph = ag;

        // for all the files which have to be opened
        Iterator<String> files = is.getFullFileNames();

        writer = new AbstractWriter(graph);

        xmlParser = new XMLParser();
        files_written = 0;

        if ((Boolean) pa.getUniqueValue(ArgumentNames.USE_EFETCH_WS)) {
            //use webservice to query pubmed
            HashSet<String> pubmedIDs = importsession.getImportList();
            this.readPubMedFile(pubmedIDs);

        } else {
            //use local MEDLINE/PubMed files
            while (files.hasNext()) {
                String file = files.next();
                this.readMEDLINEFile(file);
            }
        }

        xmlParser = null;
        writer.finalize();
        writer = null;
    }

    private void readMEDLINEFile(String f) throws IOException, XMLStreamException {

        long start = System.currentTimeMillis();
        String file = f;
        files_written++;

        System.out.print("Parsing: " + file + "...");

        List<Abstract> abs = xmlParser.parseMEDLINE(file, importsession);
        writer.writeConcepts(abs);

        abs = null;

        long end = System.currentTimeMillis();
        long time = (end - start) / 1000;
        System.out.println(" [in " + time + " sec.]");
        System.out.println("Publications written so far: " + writer.getHeadersWritten() + " (" + writer.getAbstractsWritten() + " have abstracts)");

//		if (files_written % 3 == 0) {
//			arguments.getPersistentEnv().commit();
//			System.runFinalization();
//		}

    }

    private void readPubMedFile(HashSet<String> ids) throws MalformedURLException, IOException, XMLStreamException {

        // String file = f;
        files_written++;

        long start = System.currentTimeMillis();
        Parser.propagateEventOccurred(new GeneralOutputEvent(
                "Start parsing online..", Parser.getCurrentMethodName()));

        List<Abstract> abs = xmlParser.parsePUBMED(ids, importsession);
        Parser.propagateEventOccurred(new GeneralOutputEvent("Writing  "
                + abs.size() + " concepts", Parser.getCurrentMethodName()));

        writer.writeConcepts(abs);

        abs = null;

        Parser.propagateEventOccurred(new GeneralOutputEvent(
                "Parsing online took " + (System.currentTimeMillis() - start), Parser.getCurrentMethodName()));


//		if (files_written % 3 == 0) {
//			BerkeleyEnv penv = BerkeleyRegistry.sid2berkeleyEnv.get(graph.getSID());
//			if (penv != null) {
//				penv.commit();
//			}
////			arguments.getPersistentEnv().commit();
//			System.runFinalization();
//		}

	}

}
