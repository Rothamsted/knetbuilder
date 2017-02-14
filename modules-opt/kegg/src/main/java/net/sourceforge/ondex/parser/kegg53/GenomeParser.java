package net.sourceforge.ondex.parser.kegg53;

import net.sourceforge.ondex.parser.kegg53.args.SpeciesArgumentDefinition;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Parses out the genome information from the genome file (found in the genes directory)
 *
 * @author hindlem
 */
public class GenomeParser {

    private final static String H_ENTRY = "ENTRY";
    private final static String H_TAXONOMY = "TAXONOMY";
    private final static String H_NAME = "NAME";

    private final Map<String, Taxonomony> keggIdIndex = new HashMap<String, Taxonomony>();
    private final Map<Integer, Taxonomony> taxidIndex = new HashMap<Integer, Taxonomony>();
    private final Map<String, Taxonomony> namesIndex = new HashMap<String, Taxonomony>();

    private List<String> blacklistedNames = new ArrayList<String>(); //names that apply to multiple taxonomies

    private static final Pattern comma = Pattern.compile(",");

    /**
     * @param genomeFile the path to the KEGG geneome file from genes directory
     * @throws java.io.IOException on reading file
     */
    public GenomeParser(String genomeFile) throws IOException {
        parseFile(new FileInputStream(genomeFile));
    }

    /**
     * @param genomeFile inputstream to the genomeFile
     * @throws IOException
     */
    public GenomeParser(InputStream genomeFile) throws IOException {
        parseFile(genomeFile);
    }

    private void parseFile(InputStream is) throws IOException {
        System.out.println("Parsing taxonomony file");
        keggIdIndex.put(SpeciesArgumentDefinition.ALL,
                new Taxonomony(SpeciesArgumentDefinition.ALL));

        BufferedReader br = new BufferedReader(
                new InputStreamReader(is));
       
        parseTaxonomony(br);
        
        System.out.println("Found " + keggIdIndex.size() + " species in taxonomony file");
    }

    /**
     * @return list of all species in kegg
     */
    public Set<String> getAllKeggSpecies() {
        return keggIdIndex.keySet();
    }

    /**
     * @param taxid NCBI taxid
     * @return a kegg taxonomony record if any (null if not present)
     */
    public Taxonomony getTaxonomony(int taxid) {
        return taxidIndex.get(taxid);
    }

    /**
     * @param keggId the KEGG species id
     * @return a kegg taxonomony record if any (null if not present)
     */
    public Taxonomony getTaxonomony(String keggId) {
        return keggIdIndex.get(keggId.toLowerCase());
    }

    /**
     * @param name a species common name (must be unique to a kegg species)
     * @return a kegg taxonomony record if any (null if not present)
     */
    public Taxonomony getTaxonomonyByUniqueName(String name) {
        return namesIndex.get(name.toLowerCase());
    }

    /**
     * @param keggIdLine the whole of the ENTRY line
     * @param br         the reader of the genome file
     * @throws IOException on buffered reader
     */
    private void parseTaxonomony(BufferedReader br) throws IOException {
    	Taxonomony taxonomony = null;

        while (br.ready()) {
            String line = br.readLine().trim();
            if (line.startsWith(H_ENTRY)) {
            	
            	/*
            	 * skip over meta genomes as they contain multiple taxonomy IDs
            	 */
            	if (line.contains("Meta")) {
            		while (br.ready()) {
            			line = br.readLine().trim();
            			if (line.startsWith(H_ENTRY) && 
            					!line.contains("Meta")) {
            				break;
            			}
            		}
            	}
            } else if (line.startsWith(H_TAXONOMY)) {
                String taxCode = line.substring(H_TAXONOMY.length()).trim();
                String taxid = taxCode.substring("TAX:".length()).trim();
                int taxNumber = Integer.parseInt(taxid);
                taxonomony.setTaxId(taxNumber);
                taxidIndex.put(taxNumber, taxonomony);
            } else if (line.startsWith(H_NAME)) {
                String names = line.substring(H_NAME.length()).trim();
                String[] splitNames = comma.split(names);
               
                /*
                 * old fashioned KEGG ID now moved to first name 
                 */
                String keggId = splitNames[0].trim().toLowerCase();
        		taxonomony = new Taxonomony(keggId);
        		keggIdIndex.put(keggId, taxonomony);
               
        		// all shifted
                taxonomony.setScientificName(splitNames[1].trim().toLowerCase());
                for (String splitName : splitNames) {
                    splitName = splitName.trim();
                    taxonomony.addName(splitName);

                    if (namesIndex.containsKey(splitName.toLowerCase())) {
                        namesIndex.remove(splitName.toLowerCase());
                        blacklistedNames.add(splitName.toLowerCase());
                    } else if (!blacklistedNames.contains(splitName.toLowerCase())) {
                        namesIndex.put(splitName.toLowerCase(), taxonomony);
                    }

                }
            }
        }
    }

    /**
     * @author hindlem
     */
    public class Taxonomony implements Comparable<Taxonomony> {

        private final String keggId;
        private int taxNumber;

        private String scientificName;
        private final Set<String> names = new HashSet<String>();

        /**
         * @param keggId the KEGG species id
         */
        public Taxonomony(String keggId) {
            this.keggId = keggId;
        }

        /**
         * @param taxNumber the NCBI taxid
         */
        public void setTaxId(int taxNumber) {
            this.taxNumber = taxNumber;
        }

        /**
         * @param name a common name for the species
         */
        public void addName(String name) {
            names.add(name);
        }

        /**
         * @return the KEGG species id
         */
        public String getKeggId() {
            return keggId;
        }

        /**
         * @return the NCBI taxid
         */
        public int getTaxNumber() {
            return taxNumber;
        }

        /**
         * @return common names for the species
         */
        public Set<String> getNames() {
            return names;
        }

        public String getScientificName() {
            return scientificName;
        }

        public void setScientificName(String scientificName) {
            this.scientificName = scientificName;
        }

        @Override
        public int compareTo(Taxonomony o) {
            return o.getScientificName().compareTo(getScientificName());
        }

        public boolean equals(Object o) {
            if (o instanceof Taxonomony) {
                return ((Taxonomony) o).getKeggId().equalsIgnoreCase(getKeggId());
            }
            return false;
        }

        public int hashCode() {
            return getKeggId().hashCode();
        }

    }
}
