package net.sourceforge.ondex.parser.go.data;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * General OBO Format parser modified for GO.
 *
 * @author taubertj
 */
public class OboImporter {

    /**
     * Removes trailing character and possible last double quote.
     *
     * @param input String
     * @return String
     */
    private String getText(String input) {
        if (input.startsWith("\"")) {
            input = input.substring(1, input.length());
            int index = input.lastIndexOf("\"");
            input = input.substring(0, index);
        }
        return input;
    }

    /**
     * Parses a list of references form the end of a "def" line. A reference
     * typically contains a colon, e.g. PMID:10873824.
     *
     * @param input String
     * @return String[]
     */
    private String[] getReferences(String input) {
        input = input.replaceAll(" ", "");
        int start = input.lastIndexOf('[') + 1;
        int end = input.lastIndexOf(']');
        ArrayList<String> ids = new ArrayList<String>();
        for (String id : input.substring(start, end).split(",")) {
            if (id.indexOf(':') > -1) {
                ids.add(id);
            }
        }
        return ids.toArray(new String[ids.size()]);
    }

    /**
     * Parses content of OBO file into a list of OboConcepts.
     *
     * @param filename String
     * @return List<OboConcept>
     * @throws FileNotFoundException
     * @throws IOException
     */
    public List<OboConcept> getFileContent(File filename)
            throws FileNotFoundException, IOException {

        // return list
        List<OboConcept> content = new ArrayList<OboConcept>();

        // open file
        BufferedReader in = new BufferedReader(new FileReader(filename));

        // flags in the file which indicate the type of the information
        String id_flag = "id:";
        String name_flag = "name:";
        String namespace_flag = "namespace:";
        String alt_id_flag = "alt_id:";
        String definition_flag = "def:";
        String synonym_flag = "synonym:";
        String obsolete_flag = "is_obsolete:";
        String replaced_flag = "replaced_by:";
        String is_a_flag = "is_a:";
        String relation_flag = "relationship:";
        String ref_flag = "xref:";

        // start with a new line
        OboConcept entry = null;
        String inputline = in.readLine();

        while (inputline != null) {

            if ((inputline != null) && (inputline.startsWith("[Term]"))) {
                if (entry != null) {
                    content.add(entry);
                }

                inputline = in.readLine();
                while ((inputline != null) && !inputline.startsWith("[")) {

                    if (inputline.startsWith(id_flag)) {
                        // e.g.: id: GO:0009560
                        entry = new OboConcept();
                        entry.setId(inputline.substring(id_flag.length() + 1,
                                inputline.length()));
                    } else if (inputline.startsWith(alt_id_flag)) {
                        // e.g.: alt_id: GO:0048233
                        String alt_id = inputline.substring(alt_id_flag
                                .length() + 1, inputline.length());
                        entry.addAlt_id(alt_id);
                    } else if (inputline.startsWith(name_flag)) {
                        // e.g.: name: cellularization of the embryo sac
                        entry.setName(inputline.substring(
                                name_flag.length() + 1, inputline.length()));
                    } else if (inputline.startsWith(definition_flag)) {
                        // e.g.: def: "The space enclosed by the double membrane
                        // of an elaioplast." [GOC:mah]
                        entry.setDefinition(getText(inputline.substring(
                                definition_flag.length() + 1, inputline
                                        .length())));
                        entry.setDefinitionRefs(getReferences(inputline));
                    } else if (inputline.startsWith(obsolete_flag)) {
                        // e.g.: is_obsolete: true
                        String obs = inputline.substring(
                                obsolete_flag.length() + 1, inputline.length());
                        entry.setObsolete(obs.startsWith("true"));
                    } else if (inputline.startsWith(replaced_flag)) {
                        // e.g.: replaced_by: GO:0043682
                        String replacement = inputline.substring(replaced_flag
                                .length() + 1, inputline.length());
                        entry.setReplacement(replacement);
                    } else if (inputline.startsWith(is_a_flag)) {
                        // e.g.: is_a: GO:0044459 ! plasma membrane part
                        StringTokenizer st = new StringTokenizer(inputline);
                        st.nextToken();
                        String isa = st.nextToken();
                        entry.addRelation(new String[]{"is_a", isa});
                    } else if (inputline.startsWith(relation_flag)) {
                        // e.g.: relationship: part_of GO:0009530 ! primary cell
                        // wall
                        StringTokenizer st = new StringTokenizer(inputline);
                        if (st.countTokens() > 2) {
                            st.nextToken(); // "relationship:"
                            String type = st.nextToken(); // relation type
                            String id = st.nextToken(); // relation id
                            entry.addRelation(new String[]{type, id});
                        }
                    } else if (inputline.startsWith(synonym_flag)) {
                        // e.g.: synonym: "acyl-CoA or acyl binding" BROAD []
                        String syn = inputline.substring(
                                synonym_flag.length() + 1, inputline.length());

                        if (syn.indexOf("EXACT") > -1) {
                            syn = getText(syn);
                            // a synonym exactly equal to the concept's name
                            // wouldn't make sense
                            if (syn.compareTo(entry.getName()) != 0) {
                                entry.addSynonym(OboConcept.exactSynonym, syn);
                            }
                        } else if (syn.indexOf("NARROW") > -1) {
                            syn = getText(syn);
                            // a synonym exactly equal to the concept's name
                            // wouldn't make sense
                            if (syn.compareTo(entry.getName()) != 0) {
                                entry.addSynonym(OboConcept.narrowSynonym, syn);
                            }
                        } else if (syn.indexOf("BROAD") > -1) {
                            syn = getText(syn);
                            // a synonym exactly equal to the concept's name
                            // wouldn't make sense
                            if (syn.compareTo(entry.getName()) != 0) {
                                entry.addSynonym(OboConcept.broadSynonym, syn);
                            }
                        } else if (syn.indexOf("RELATED") > -1) {
                            syn = getText(syn);
                            // a synonym exactly equal to the concept's name
                            // wouldn't make sense
                            if (syn.compareTo(entry.getName()) != 0) {
                                entry
                                        .addSynonym(OboConcept.relatedSynonym,
                                                syn);
                            }
                        }
                    } else if (inputline.startsWith(ref_flag)) {
                        // e.g.: xref: Reactome:69620
                        String ref = inputline.substring(ref_flag.length() + 1,
                                inputline.length());
                        entry.addRef(ref);
                    } else if (inputline.startsWith(namespace_flag)) {
                        // e.g.: namespace: biological_function
                        String namespace = inputline.substring(namespace_flag
                                .length() + 1, inputline.length());
                        entry.setNamespace(namespace);
                    }

                    inputline = in.readLine();
                }
            } else
                inputline = in.readLine();
        }
        in.close();

        // add the last entry
		content.add(entry);

		return content;
	}

}
