package net.sourceforge.ondex.parser.transfac.site;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.transfac.AbstractTFParser;
import net.sourceforge.ondex.parser.transfac.ConceptWriter;
import net.sourceforge.ondex.parser.transfac.DBlink;
import net.sourceforge.ondex.parser.transfac.Parser;
import net.sourceforge.ondex.parser.transfac.sink.Publication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Pattern;


public class SiteParser extends AbstractTFParser {

    public SiteParser(ConceptWriter conceptWriter, ONDEXPluginArguments pa) throws InvalidPluginArgumentException {
        super(conceptWriter, pa);
    }

    public
    @Override
    void start() throws IOException, InvalidPluginArgumentException {
        Parser.propagateEventOccurred(new GeneralOutputEvent("SiteParser started...", "start()"));

        File dir = new File((String) pa.getUniqueValue(FileArgumentDefinition.INPUT_DIR));


        BufferedReader br = new BufferedReader(
                new FileReader(dir.getAbsolutePath() +
                        File.separator + "site.dat"));

        Pattern semiColon = Pattern.compile(";");

        Publication pub = null;
        SiteObject site = null;
        while (br.ready()) {
            String line = br.readLine();

            if (line.startsWith(Abreviations.accession)) {
                if (site != null) conceptWriter.createSite(site);
                pub = null;
                String acc = parseLine(line).trim().toUpperCase();
                site = new SiteObject(acc);
            } else if (line.startsWith(Abreviations.identifier)) {
                site.setId(parseLine(line));
            } else if (line.startsWith(Abreviations.comments)) {
                site.setComments(parseLine(line));
            } else if (line.startsWith(Abreviations.description)) {
                String line_content = parseLine(line);
                site.setDescription(line_content);
                String genAcc = parseGeneAcc(line_content);
                if (genAcc != null) {
                    site.setSituatedTo(genAcc);
                }
            } else if (line.startsWith(Abreviations.org)) {
                site.setSpecies(parseSpecies(parseLine(line)));
            } else if (line.startsWith(Abreviations.binding_factor)) {
                site.addBindingFactorAccession(semiColon.split(parseLine(line))[0].trim().toUpperCase());
            } else if (line.startsWith(Abreviations.deduced_matrix)) {
                site.addMatrixAccession(semiColon.split(parseLine(line))[0].trim().toUpperCase());
            } else if (line.startsWith(Abreviations.xdb_ref)) {
                DBlink link = parseDatabaseLink(parseLine(line));
                if (link != null) {
                    site.addDatabaseLink(link);
                }
            } else if (line.startsWith(Abreviations.sequence)) {
                site.setSequence(parseLine(line));
            } else if (line.startsWith(Abreviations.sequence_type)) {
                site.setSequenceType(parseLine(line));
            } else if (line.startsWith(Abreviations.element_denom)) {
                site.setElementDenomination(parseLine(line));
            } else if (line.startsWith(Abreviations.first_pos_bindingsite)) {
                site.setFirstPosBindingSite(Integer.parseInt(parseLine(line)));
            } else if (line.startsWith(Abreviations.last_pos_bindingsite)) {
                site.setLastPosBindingSite(Integer.parseInt(parseLine(line)));
            } else if (line.startsWith(Abreviations.def_first_pos_bindingsite)) {
                site.setDefinitionOfFirstPosition(parseLine(line));
            } else if (line.startsWith(Abreviations.medline_id)) {
                String type = line.substring(2, line.indexOf(':')).toLowerCase();
                if (type.contains("ubmed")) {
                    String pubmedId = line.replaceAll("[^0-9]", "");
                    if (pubmedId.length() > 0) {
                        pub = new Publication(pubmedId);
                        site.addPublication(pub);
                    }
                    // ubmed (not pubmed) for one exception
                } else {
                    Parser.propagateEventOccurred(new DataFileErrorEvent(
                            "Unknown publication id : " + type, ""));
                }
            } else if (pub != null && line.startsWith(Abreviations.ref_authors)) {
                pub.setAuthors(parseLine(line));
            } else if (pub != null && line.startsWith(Abreviations.ref_title)) {
                pub.setTitle(parseLine(line));
            } else if (pub != null && line.startsWith(Abreviations.ref_data)) {
                pub.setSource(parseLine(line));
            }
        }

        if (site != null) conceptWriter.createSite(site);

        br.close();
        br = null;

        // print all clashed taxa
        Iterator<String> it = clashedTaxa.iterator();
        while (it.hasNext()) {
            Parser.propagateEventOccurred(
                    new DataFileErrorEvent("No taxid found for " + it.next(), ""));
        }
        conceptWriter.validateSiteToTF("Site");
        Parser.propagateEventOccurred(new GeneralOutputEvent("SiteParser finished...", ""));

    }

}
