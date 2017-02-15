package net.sourceforge.ondex.parser.wordnet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;

/**
 * Reads wordnet flatfiles and imports data into ONDEX-database
 * 
 * @author afriedri, jbutz, messmeie
 */
public class WNExtractor {

	ArrayList<Entity> WNArray = new ArrayList<Entity>();

	BufferedReader in;

	/** the current line of the current file */
	String inputline;

	StringTokenizer st;

	/** contains the names of one synset */
	String[] con_names;

	/** contains the relations of one synset */
	String[][] relations;

	String id;

	String lex_id;

	String wordtype;

	String wordnum;

	String rel_counter;

	String con_descr;

	/** an array of the filenames to be parsed */
	static String[] filenames = { "data.adv", "data.verb", "data.adj",
			"data.noun" };

	/** contains the id and position for each name */
	Hashtable<String, String> namerelations;

	/**
	 * constructor
	 * 
	 */
	public WNExtractor() {
		namerelations = new Hashtable<String, String>();
	}

	/**
	 * translates the wordnet pos-tag into qtag pos-tag
	 * 
	 * @param tag
	 *            - the original WordNet POS-tag
	 * @return the translatd tag as a String
	 */
	public static String translate_tag(String tag) {
		String return_tag = "";
		if (tag.equals("n")) {
			return_tag = "NN";
		} else if (tag.equals("v")) {
			return_tag = "VB";
		} else if (tag.equals("a") || tag.equals("s")) {
			return_tag = "JJ";
		} else if (tag.equals("r")) {
			return_tag = "RB";
		}
		return return_tag;
	}

	/**
	 * translates the wordnet relation symbols into ONDEX relation symbols
	 * 
	 * @param rel
	 *            - the original WordNet relation symbol
	 * @return the translated relation as a String
	 */
	public static String translate_relation(String rel) {
		String return_rel = "";
		if (rel.equals("!")) {
			return_rel = "anto";
		} else if (rel.equals("@")) {
			return_rel = "is_a";
		} else if (rel.equals("@i")) {
			return_rel = "is_hyperym_of";
		} else if (rel.equals("#m")) {
			return_rel = "member_of";
		} else if (rel.equals("#s")) {
			return_rel = "s_isp";
		} else if (rel.equals("#p")) {
			return_rel = "p_isp";
		} else if (rel.equals("%m")) {
			return_rel = "m_co";
		} else if (rel.equals("%s")) {
			return_rel = "s_co";
		} else if (rel.equals("%p")) {
			return_rel = "p_co";
		} else if (rel.equals("=")) {
			return_rel = "att";
		} else if (rel.equals("+")) {
			return_rel = "der";
		} else if (rel.equals(";c") || rel.equals("-c") || rel.equals(";u")
				|| rel.equals("-u") || rel.equals(";r") || rel.equals("-r")
				|| rel.equals("*") || rel.equals(">") || rel.equals("^")
				|| rel.equals("$") || rel.equals("<") || rel.equals("\\")) {
			return_rel = "r";
		} else if (rel.equals("&")) {
			return_rel = "sim";
		}

		// for unknown relation types, i.e. new wordnet versions
		if (return_rel.equals("")) {
			return_rel = "unkwn";
			// if (!rel.equals("@i") && !rel.equals("~i"))
			System.out.println("Unknown, it was " + rel);
		}

		return return_rel;
	}

	/**
	 * opens input files and inserts the sorted input values into vectors
	 * 
	 * @param files
	 *            - array of filenames to be processed
	 */
	private void parse_file(String infilesdir, String[] files) throws Exception {

		for (int k = 0; k < files.length; k++) {

			try {
				String filename = infilesdir + files[k];
				Parser
						.propagateEventOccurred(new GeneralOutputEvent(
								"Processing: " + filename,
								"[WNExtractor - parse_file]"));
				in = new BufferedReader(new FileReader(filename));

				while ((inputline = in.readLine()) != null) {
					st = new StringTokenizer(inputline);
					if (!inputline.startsWith("  ")) {

						if (st.countTokens() > 0) {

							WNEntity wE = new WNEntity();

							id = st.nextToken();
							lex_id = st.nextToken();
							wordtype = st.nextToken();
							wordnum = st.nextToken();
							int wordnum_int = Integer.parseInt(wordnum, 16);

							con_names = new String[wordnum_int];
							for (int i = 0; i < wordnum_int; i++) {
								con_names[i] = st.nextToken().replace('_', ' ');
								st.nextToken();
							}

							rel_counter = st.nextToken();
							int relcount_int = Integer.parseInt(rel_counter);
							relations = new String[relcount_int][4];
							for (int i = 0; i < relcount_int; i++) {
								for (int j = 0; j < 4; j++) {
									relations[i][j] = st.nextToken();
								}
							}

							con_descr = inputline.substring((inputline
									.indexOf("|") + 1), inputline.length() - 1);
							wordtype = translate_tag(wordtype);
							id = id + wordtype;

							wE.setId(id.trim());
							wE.setDescription(con_descr.trim());
							wE.setPosTag(wordtype.trim());

							ArrayList<String> a = new ArrayList<String>();
							for (int i = 1; i <= wordnum_int; i++) {
								a.add(con_names[i - 1]);
								namerelations.put(id + "_" + i,
										con_names[i - 1]);
							}
							wE.setSynonyms(a);
							ArrayList<Relation> relA = new ArrayList<Relation>();

							for (int i = 0; i < relcount_int; i++) {

								if (!(relations[i][0].equals("~")
										|| relations[i][0].equals("%p")
										|| relations[i][0].equals("~i")
										|| relations[i][0].equals("%s") || relations[i][0]
										.equals("%m"))) {
									Relation rel;

									if (relations[i][3].equals("0000")) {

										rel = new Relation(
												id.trim(),
												relations[i][1].trim()
														+ translate_tag(relations[i][2]),
												translate_relation(relations[i][0]));

										/*
										 * The following was inserted to get rid
										 * of lines with equal (from_id, to_id,
										 * of_type) values, which is the new key
										 * of the "relation" table. However,
										 * there might exist different lines due
										 * to different values in from_name and
										 * to_name, but these are discarded so
										 * far.
										 */
									} else {

										String[] id_rel_id = new String[3];

										id_rel_id[0] = id
												+ "_"
												+ Integer
														.parseInt(
																relations[i][3]
																		.substring(
																				0,
																				2),
																16);
										id_rel_id[1] = translate_relation(relations[i][0]);
										id_rel_id[2] = relations[i][1]
												+ translate_tag(relations[i][2])
												+ "_"
												+ Integer.parseInt(
														relations[i][3]
																.substring(2),
														16);

										rel = new Relation(id_rel_id[0]
												.substring(0, id_rel_id[0]
														.indexOf("_")),
												id_rel_id[2].substring(0,
														id_rel_id[2]
																.indexOf("_")),
												id_rel_id[1]);

										/*
										 * rel.setToName(namerelations
										 * .get(id_rel_id[2]));
										 * rel.setFromName(namerelations
										 * .get(id_rel_id[0]));
										 */

									}
									relA.add(rel);

								}

							}
							wE.setRelations(relA);
							WNArray.add(wE);
						}

					}

				}
				in.close();
			} catch (Exception e) {
				Parser.propagateEventOccurred(new DataFileErrorEvent(e
						.getMessage(), "[WNExtractor - parse_file]"));
                throw e;
			}

		}

	}

	public ArrayList<Entity> start(String infilesdir) throws Exception {

		try {
			parse_file(infilesdir, filenames);
		} catch (Exception e) {
			Parser.propagateEventOccurred(new DataFileErrorEvent("Exception: "
					+ e.getMessage(), "[WNExtractor - start]"));
            throw e;
		}
		return WNArray;
	}

}
