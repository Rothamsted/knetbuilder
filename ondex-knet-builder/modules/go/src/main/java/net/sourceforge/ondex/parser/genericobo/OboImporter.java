package net.sourceforge.ondex.parser.genericobo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 
 * @author hoekmanb
 *
 */
public class OboImporter {

	private String getText(String input, boolean removeSpecialChars) {
		if (removeSpecialChars) {
			// input = input.replace(',', ' ');
			input = input.replace('/', ' ');
			input = input.replace('\\', ' ');
		}

		if (input.startsWith("\"")) {
			input = input.substring(1, input.length());
			int index = input.indexOf("\"");
			input = input.substring(0, index);
		}
		return input;
	}

	private String[] getReferences(String input) {
		input = input.replaceAll(" ", "");
		int start = input.indexOf('[') + 1;
		int end = input.indexOf(']');
		ArrayList<String> ids = new ArrayList<String>();
		if(start<end){
			for (String id : input.substring(start, end).split(",")) {
				if (id.indexOf(':') != -1) {
					ids.add(id);
				}
			}
		}
		return ids.toArray(new String[ids.size()]);
	}

	public List<OboConcept> getFileContent(String filename, boolean getObsoletes)
			throws Exception {
		// public List<OboConcept> getFileContent(String filename, boolean
		// getObsoletes, boolean onlyExact)
		// throws Exception {
		// return List
		List<OboConcept> content = new ArrayList<OboConcept>();

		BufferedReader in = new BufferedReader(new FileReader(filename));

		// flags in the file which indicate the type of the information
		String id_flag = "id:";
		String alt_id_flag = "alt_id:";
		String name_flag = "name:";
		String namespace_flag = "namespace:";
		String definition_flag = "def:";
		String obsolete_flag = "is_obsolete:";

		String is_a_flag = "is_a:";
		String relation_flag = "relationship:";

		String synonym_flag = "synonym:";
		String exact_synonym_flag = "exact_synonym:";
		String broad_synonym_flag = "broad_synonym:";
		String narrow_synonym_flag = "narrow_synonym:";
		String related_synonym_flag = "related_synonym:";

		String ref_1_0_flag = "xref_analog:";
		String ref_1_2_flag = "xref:";

		OboConcept entry = null;
		String inputline = in.readLine();
		boolean get = true;

		while (inputline != null) {

			if ((inputline != null) && (inputline.startsWith("[Term]"))) {

				if (entry != null) {

					if (get) {
						content.add(entry);
					}

					get = true;
				}

				inputline = in.readLine();
				while ((inputline != null) && !inputline.startsWith("[")) {

					if (inputline.startsWith(id_flag)) {
						entry = new OboConcept();
						entry.setId(inputline.substring(id_flag.length() + 1,
								inputline.length()));
					} else if (inputline.startsWith(alt_id_flag)) {
						String alt_id = inputline.substring(alt_id_flag
								.length() + 1, inputline.length());
						entry.addAlt_ids(alt_id);
					} else if (inputline.startsWith(name_flag)) {
						entry.setName(inputline.substring(
								name_flag.length() + 1, inputline.length()));
					} else if (inputline.startsWith(definition_flag)) {
						entry.setDefinition(getText(inputline.substring(
								definition_flag.length() + 1, inputline
										.length()), true));
						entry.setDefinitionRefs(getReferences(inputline));
					} else if (inputline.startsWith(obsolete_flag)) {
						String obs = inputline.substring(
								obsolete_flag.length() + 1, inputline.length());
						if ((obs.startsWith("true")) && !getObsoletes)
							get = false;
					} else if (inputline.startsWith(is_a_flag)) {
						// Format changed occurred, so the following line
						// produced errors ...
						// replaced by getting the second token of the line
						// String isa =
						// inputline.substring(is_a_flag.length()+1,
						// inputline.length());

						StringTokenizer st = new StringTokenizer(inputline);
						st.nextToken();
						String isa = st.nextToken();
						List<String> relation = new ArrayList<String>();
						relation.add("is_a");
						relation.add(isa);
						entry.addRelation(relation);
					} else if (inputline.startsWith(relation_flag)) {
						StringTokenizer st = new StringTokenizer(inputline);

						if (st.countTokens() > 2) {
							ArrayList<String> relation = new ArrayList<String>();

							String token = st.nextToken(); // "relationship:"
							token = st.nextToken(); // relation type
							relation.add(token);
							token = st.nextToken(); // relation id
							relation.add(token);

							entry.addRelation(relation);
						}
					} else if (inputline.startsWith(synonym_flag)) {
						String syn = inputline.substring(
								synonym_flag.length() + 1, inputline.length());

						if (syn.indexOf("EXACT") > -1) {

							syn = getText(syn, false);

							if (syn.compareTo(entry.getName()) != 0) {// a
																		// synonym
																		// exactly
								// equal to the
								// concept's name
								// wouldn't make sense

								entry.addSynonym(OboConcept.exactSynonym, syn);
							}
						} else {
							syn = getText(syn, false);

							if (syn.compareTo(entry.getName()) != 0) {// a
																		// synonym
																		// exactly
								// equal to the
								// concept's name
								// wouldn't make sense
								entry.addSynonym(OboConcept.normalSynonym, syn);
							}
							// System.out.println("Not writing "+syn+", because
							// not Exact!");
						}
					} else if (inputline.startsWith(exact_synonym_flag)) {
						String syn = inputline.substring(exact_synonym_flag
								.length() + 1, inputline.length());
						syn = getText(syn, false);

						if (syn.compareTo(entry.getName()) != 0) {// a synonym
																	// exactly
							// equal to the
							// concept's name
							// wouldn't make sense
							entry.addSynonym(OboConcept.exactSynonym, syn);
						}
						// } else if (inputline.startsWith(broad_synonym_flag)
						// && !onlyExact) {
					} else if (inputline.startsWith(broad_synonym_flag)) {
						String syn = inputline.substring(broad_synonym_flag
								.length() + 1, inputline.length());
						syn = getText(syn, false);

						if (syn.compareTo(entry.getName()) != 0) {// a synonym
																	// exactly
							// equal to the
							// concept's name
							// wouldn't make sense
							entry.addSynonym(OboConcept.broadSynonym, syn);
						}
						// } else if (inputline.startsWith(narrow_synonym_flag)
						// && !onlyExact) {
					} else if (inputline.startsWith(narrow_synonym_flag)) {
						String syn = inputline.substring(narrow_synonym_flag
								.length() + 1, inputline.length());
						syn = getText(syn, false);

						if (syn.compareTo(entry.getName()) != 0) {// a synonym
																	// exactly
							// equal to the
							// concept's name
							// wouldn't make sense
							entry.addSynonym(OboConcept.narrowlSynonym, syn);
						}
						// } else if (inputline.startsWith(related_synonym_flag)
						// && !onlyExact) {
					} else if (inputline.startsWith(related_synonym_flag)) {
						String syn = inputline.substring(related_synonym_flag
								.length() + 1, inputline.length());
						syn = getText(syn, false);

						if (syn.compareTo(entry.getName()) != 0) {// a synonym
																	// exactly
							// equal to the
							// concept's name
							// wouldn't make sense
							entry.addSynonym(OboConcept.relatedSynonym, syn);
						}
					} else if (inputline.startsWith(ref_1_0_flag)) {
						String ref = inputline.substring(
								ref_1_0_flag.length() + 1, inputline.length());
						entry.addRef(ref);
					} else if (inputline.startsWith(ref_1_2_flag)) {
						String ref = inputline.substring(
								ref_1_2_flag.length() + 1, inputline.length());
						entry.addRef(ref);
					} else if (inputline.startsWith(namespace_flag)) {
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
		if (get) {
			content.add(entry);
		}

		return content;
	}

}
