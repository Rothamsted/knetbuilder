package net.sourceforge.ondex.parser.iah;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.tools.MetaDataLookup;
import net.sourceforge.ondex.tools.MetaDataUtil;
import org.apache.log4j.Level;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Parser extends ONDEXParser
{

    public static final String TRANSLATION_FILE_ARG = "TranslationFile";
    public static final String TRANSLATION_FILE_ARG_DESC = "Path to the translation file";

    private MetaDataUtil mdu;

    private ConceptClass ccGene;

    private DataSource dataSource, dataSourceMips;

    private EvidenceType etImpd;

    private RelationType rtPartAct, rtPartPass;

    private MetaDataLookup<ConceptClass> interactionLookup;
    private MetaDataLookup<EvidenceType> etLookup;

    private Map<String, Integer> acc2cid = new HashMap<String, Integer>();


    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                        FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false),
                new FileArgumentDefinition(TRANSLATION_FILE_ARG, TRANSLATION_FILE_ARG_DESC, true, true, false)
        };
    }

    @Override
    public String getName() {
        return "IAH Genetic interaction parser";
    }

    @Override
    public String getVersion() {
        return "15.05.2009";
    }

    @Override
    public String getId() {
        return "iah";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {
        fetchMetaData();

        File dir = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE));
        if (!dir.exists()) {
            throw new PluginConfigurationException("Input directory does not exist:" + dir.getAbsolutePath());
        }
        if (dir.list().length == 0) {
            throw new PluginConfigurationException("Input directory is empty: " + dir.getAbsolutePath());
        }

        debug("Reading directory: " + dir.getAbsolutePath());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }
            parseFile(file);
            debug("Parsing: " + file.getName());
        }
    }

    private void parseFile(File file) throws IOException, ParserConfigurationException {
        BufferedReader r = new BufferedReader(new FileReader(file));
        String line;
        int lineCount = 0;
        while ((line = r.readLine()) != null) {
            lineCount++;
            String[] cols = line.split("\t");
            if (cols.length >= 3) {
                String fromAcc = cols[0].trim();
                String interactionType = cols[1].trim();
                String toAcc = cols[2].trim();

                ONDEXConcept from = conceptForAccession(fromAcc);
                ONDEXConcept to = conceptForAccession(toAcc);

                if (from != null && to != null) {
                    ConceptClass ccInteraction = interactionLookup.get(interactionType);
                    if (ccInteraction == null) {
                        throw new ParserConfigurationException("undefined interaction type: " + interactionType);
                    }
                    ONDEXConcept interaction = graph.getFactory().createConcept("IAH:Interaction#" + lineCount, dataSource, ccInteraction, etImpd);

                    if (cols.length == 4) {
                        String evId = cols[3].trim();
                        EvidenceType et = etLookup.get(evId);
                        if (et == null) {
                            throw new ParserConfigurationException("undefined evidence type: " + evId);
                        }
                        interaction.addEvidenceType(et);
                    }

                    graph.getFactory().createRelation(from, interaction, rtPartAct, etImpd);
                    graph.getFactory().createRelation(to, interaction, rtPartPass, etImpd);
                }
            }
        }
    }

    private ONDEXConcept conceptForAccession(String acc) {
        Integer id = acc2cid.get(acc);
        ONDEXConcept c = null;
        if (id != null) {
            c = graph.getConcept(id);
        } else {
            c = graph.getFactory().createConcept(acc, dataSource, ccGene, etImpd);
            c.createConceptAccession(acc, dataSourceMips, false);
            acc2cid.put(acc, c.getId());
        }
        return c;
    }

//	private void indexAccessions() {
//		Set<ONDEXConcept> cs = graph.getConcepts();
//		while (cs.hasNext()) {
//			ONDEXConcept c = cs.next();
//			Set<ConceptAccession> accs = c.getConceptAccessions();
//			while (accs.hasNext()) {
//				ConceptAccession acc = accs.next();
//				if (acc.getElementOf().equals(dataSource)) {
//					acc2cid.put(acc.getAccession(), c.getId());
//					break;
//				}
//			}
//			accs.close();
//		}
//		cs.close();
//	}

    private void fetchMetaData() throws Exception {
        mdu = new MetaDataUtil(graph.getMetaData(), null);

        ccGene = requireConceptClass("Gene");

        dataSource = mdu.safeFetchDataSource("IAH");
        dataSourceMips = mdu.safeFetchDataSource("MIPS");


        rtPartAct = requireRelationType("part_act");
        rtPartPass = requireRelationType("part_pass");

        etImpd = mdu.safeFetchEvidenceType("IMPD");

        File trFileName = new File((String) args.getUniqueValue(TRANSLATION_FILE_ARG));
        interactionLookup = new MetaDataLookup<ConceptClass>(trFileName, graph.getMetaData(), ConceptClass.class);
        etLookup = new MetaDataLookup<EvidenceType>(trFileName, graph.getMetaData(), EvidenceType.class);
    }

//	private void logFail(String s) {
//		EventType e = new InconsistencyEvent(s,"");
//		e.setLog4jLevel(Level.ERROR);
//		fireEventOccurred(e);
//	}
//	
//	private void log(String s) {
//		EventType e = new GeneralOutputEvent(s,"");
//		e.setLog4jLevel(Level.INFO);
//		fireEventOccurred(e);
//	}

    private void debug(String s) {
        EventType e = new GeneralOutputEvent(s, "");
        e.setLog4jLevel(Level.DEBUG);
        fireEventOccurred(e);
    }

}
