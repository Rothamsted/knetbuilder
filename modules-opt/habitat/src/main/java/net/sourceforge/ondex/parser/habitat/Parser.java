package net.sourceforge.ondex.parser.habitat;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.exception.type.PluginException;
import net.sourceforge.ondex.exception.type.WrongArgumentException;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.tools.MetaDataUtil;
import org.apache.log4j.Level;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Set;

/**
 * Parser for the NCL Habitat DB project.
 *
 * @author jweile
 */
public class Parser extends ONDEXParser
{

    private HashMap<String, File> fileRegister;

    private MetaDataUtil mdu;

    private ConceptClass ccThing;
    private RelationType rtFeatures;
    private DataSource dataSourceHabitat;
    private EvidenceType etImpd;

//	private HashMap<String,Integer> habitat2concept;
//	private HashMap<String,Integer> taxa2concept;
//	private HashMap<String,Integer> protein2concept;
//	private HashMap<String,Integer> pfam2concept;
//	private HashMap<String,Integer> ipr2concept;

    private Index habitats;
    private Index taxa;
    private Index proteins;
    private Index pfam;
    private Index ipro;

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{new FileArgumentDefinition(FileArgumentDefinition.INPUT_DIR,
                FileArgumentDefinition.INPUT_DIR_DESC, true, true, true, false),
        };
    }

    @Override
    public String getName() {
        return "Habitat Project Parser";
    }

    @Override
    public String getVersion() {
        return "24.03.2009";
    }

    @Override
    public String getId() {
        return "habitat";
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {

        registerFiles();

        prepareIndices();

        parseMapping("prot_tagid_iprhit.txt",
                "protein_tagid",
                "ipr_entry",
                proteins, ipro, rtFeatures, false);

        parseMapping("prot_tagid_pfam.txt",
                "protein_tagid",
                "hitid",
                proteins, pfam, rtFeatures, false);

        parseMapping("taxon_protein.txt",
                "taxonid",
                "tagid",
                taxa, proteins, rtFeatures, true);

        parseMapping("taxon_habitat.txt",
                "habitat",
                "taxonid",
                habitats, taxa, rtFeatures, true);

    }

    private void prepareIndices() throws MetaDataMissingException {
        mdu = new MetaDataUtil(graph.getMetaData(), null);

        ccThing = requireConceptClass("Thing");
        ConceptClass ccH = mdu.safeFetchConceptClass("Habitat", "Habitat of Organisms", ccThing);
        ConceptClass ccT = mdu.safeFetchConceptClass("Taxa", "Organism category", ccThing);
        ConceptClass ccP = mdu.safeFetchConceptClass("Protein", "Protein", ccThing);
        ConceptClass ccDom = mdu.safeFetchConceptClass("ProtDomain", "Protein Domain", ccThing);

        rtFeatures = mdu.safeFetchRelationType("feat", "features");
        etImpd = requireEvidenceType("IMPD");
        dataSourceHabitat = mdu.safeFetchDataSource("NCL:DB:Habitat");

        habitats = new Index(graph, dataSourceHabitat, ccH, etImpd);
        taxa = new Index(graph, dataSourceHabitat, ccT, etImpd);
        proteins = new Index(graph, dataSourceHabitat, ccP, etImpd);
        pfam = new Index(graph, dataSourceHabitat, ccDom, etImpd);
        ipro = new Index(graph, dataSourceHabitat, ccDom, etImpd);
    }

    private void registerFiles() throws PluginException {
        fileRegister = new HashMap<String, File>();
        File dir = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_DIR));
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".txt");
                }
            });
            for (File file : files) {
                String[] pathArray = file.getAbsolutePath().split(File.separator);
                String fileName = pathArray[pathArray.length - 1];
                fileRegister.put(fileName, file);
            }
            ensureFilePresence(new String[]{"habitat_term.txt",
                    "ipr_entry.txt",
                    "pfam.txt",
                    "protein_refseq.txt",
                    "prot_tagid_iprhit.txt",
                    "prot_tagid_pfam.txt",
                    "taxon_habitat.txt",
                    "taxon_protein.txt",
                    "taxon.txt"});
        } else {
            throw new WrongArgumentException("Directory " + dir.getAbsolutePath() + " does not exist");
        }
    }

    private void ensureFilePresence(String[] fileNames) throws PluginConfigurationException {
        Set<String> fileSet = fileRegister.keySet();
        for (String fileName : fileNames) {
            if (!fileSet.contains(fileName)) {
                throw new PluginConfigurationException("Input file missing: " + fileName);
            }
        }
    }


//	private void parseMapping(String filename, String leftCol, String rightCol, Index left, Index right, RelationType rt) throws Exception {
//		log("Parsing mapping: "+filename);
//		
//		TabFileReader r = new TabFileReader(fileRegister.get(filename));
//		r.start();
//		
//		int leftIndex = r.getColumnIndex(leftCol);
//		int rightIndex = r.getColumnIndex(rightCol);
//		
//		String[] cols = null;
//		int i = 0;
//		while ((cols = r.poll()) != null) {
//			if ((++i % 1000) == 0) System.out.println(i);
//			if (!cols[leftIndex].equals("") && !cols[rightIndex].equals("")) {
//				ONDEXConcept leftConcept = left.getOrCreate(cols[leftIndex]);
//				ONDEXConcept rightConcept = right.getOrCreate(cols[rightIndex]);
//				ONDEXRelation rel = graph.getFactory().createRelation(leftConcept, rightConcept, rt, etImpd);
//				if (rel == null) {
//					logFail("Failed to create Relation");
//				}
//			} else {
//				log("Empty mapping entry detected!");
//			}
//		}
//		if (r.getError() != null) {
//			throw new Exception(r.getError());
//		}
//	}


    private void parseMapping(String filename, String leftCol, String rightCol, Index left, Index right, RelationType rt, boolean rightDependent) throws Exception {
        log("Parsing mapping: " + filename);

        TabFileReader r = new TabFileReader(fileRegister.get(filename));
        r.start();

        int leftIndex = r.getColumnIndex(leftCol);
        int rightIndex = r.getColumnIndex(rightCol);

        String[] cols = null;
        int i = 0;
        while ((cols = r.poll()) != null) {
            if ((++i % 1000) == 0) System.out.println(i);
            if (!cols[leftIndex].equals("") && !cols[rightIndex].equals("")) {
                if (!rightDependent || right.containsKey(cols[rightIndex])) {
                    ONDEXConcept leftConcept = left.getOrCreate(cols[leftIndex]);
                    ONDEXConcept rightConcept = right.getOrCreate(cols[rightIndex]);
                    ONDEXRelation rel = graph.getFactory().createRelation(leftConcept, rightConcept, rt, etImpd);
                    if (rel == null) {
                        logFail("Failed to create Relation");
                    }
                }
            } else {
                log("Empty mapping entry detected!");
            }
        }
        if (r.getError() != null) {
            throw new Exception(r.getError());
        }
    }

    private void log(String s) {
        fireEventOccurred(new GeneralOutputEvent(s, ""));
    }

    private void logFail(String s) {
        EventType e = new InconsistencyEvent(s, "");
        e.setLog4jLevel(Level.ERROR);
        fireEventOccurred(e);
    }

}
