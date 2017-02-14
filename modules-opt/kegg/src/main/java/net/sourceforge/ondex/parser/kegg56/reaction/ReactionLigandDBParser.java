package net.sourceforge.ondex.parser.kegg56.reaction;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import net.sourceforge.ondex.parser.kegg56.data.Pathway;
import net.sourceforge.ondex.parser.kegg56.data.Reaction;
import net.sourceforge.ondex.parser.kegg56.util.DPLPersistantSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the reaction component of the LIGAND database
 * 
 * @author hindlem
 */
public class ReactionLigandDBParser {

	private static final String ENTRY = "ENTRY";
	private static final String DEFINITION = "DEFINITION";
	private static final String EQUATION = "EQUATION";
	private static final String PATHWAY = "PATHWAY";
	private static final String ENZYME = "ENZYME";
	private static final String ORTHOLOGY = "ORTHOLOGY";
	private static final String NAME = "NAME";
	private static final String RPAIR = "RPAIR";
	private static final String COMMENT = "COMMENT";

	private Map<String, ReactionBean> reactions;

	/**
	 * Transforms internal ReactionBeans into Reaction prototypes
	 * 
	 * @param pathways
	 */
	public void addReactionInfoToPathways(DPLPersistantSet<Pathway> pathways) {
		int i = 0;
		EntityCursor<Pathway> cursor = pathways.getCursor();
		for (Pathway pathway : cursor) {
			Map<String, Reaction> pathwayReactions = pathway.getReactions();
			for (String pathwayReactionId : pathwayReactions.keySet()) {
				if (pathwayReactionId.toUpperCase().startsWith("RN:")) {
					pathwayReactionId = pathwayReactionId.substring("RN:"
							.length());
				}
				ReactionBean reactionBean = reactions.get(pathwayReactionId);
				if (reactionBean == null) {
					System.err.println(pathwayReactionId
							+ " is not found in reaction (ligand db) file");
				} else {
					i++;
					Reaction pathwayReaction = pathwayReactions.get("RN:"
							+ pathwayReactionId);
					pathwayReaction.setNames(reactionBean.geNames());
					pathwayReaction.setECTerms(reactionBean.getECTerms());
					pathwayReaction.setKoTerms(reactionBean.getKoTerms());
					pathwayReaction.setDefinition(reactionBean.getDefinition());
					pathwayReaction.setComment(reactionBean.getComment());
					pathwayReaction.setEquation(reactionBean.getEquation());
				}
			}
			try {
				cursor.update(pathway);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		System.out.println(i + " reactions updated");
	}

	/**
	 * Parses given reaction file from LIGAND database into set of
	 * ReactionBeans.
	 * 
	 * @param ligandFile
	 */
	public void parse(InputStream ligandFile) throws IOException {
		reactions = new HashMap<String, ReactionBean>();

		final Pattern semicolon = Pattern.compile("; ");
		final Pattern startLabel = Pattern.compile("^[A-Z]+");
		final Pattern space = Pattern.compile("[ |    ]+");
		final Pattern underscore = Pattern.compile("_");

		ReactionBean bean = null;
		String currentAnnotation = null;
		StringBuilder nameList = new StringBuilder();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(ligandFile));
		while (br.ready()) {
			String line = br.readLine();

			// some post processing at end of entry
			if (line.startsWith("///")) {
				// multiple names are separated via semicolon
				if (nameList.length() > 0) {
					// adding names to previous bean
					for (String name : semicolon.split(nameList.toString()))
						bean.addName(name.trim());
					nameList.setLength(0);
				}
			}

			// get ready for new entry
			if (line.startsWith(ENTRY)) {
				String entry = space.split(line.substring(ENTRY.length())
						.trim().toUpperCase())[0].trim();
				bean = new ReactionBean(entry);
				reactions.put(entry, bean);
				currentAnnotation = null;
			}

			// check we are ready
			if (bean != null) {

				// what is the current element we are in
				Matcher matcher = startLabel.matcher(line);
				if (matcher.find()) {
					currentAnnotation = matcher.group(0);
				}

				// has not yet ended
				if (currentAnnotation != null && !line.startsWith("///")) {

					// check for DEFINITION
					if (currentAnnotation.equals(DEFINITION)) {

						// in DEFINITION first line
						if (line.startsWith(DEFINITION)) {
							line = line.substring(DEFINITION.length()).trim();
							bean.setDefinition(line);
						} else {
							// adding to existing information
							line = line.trim();
							bean.setDefinition(bean.getDefinition() + " "
									+ line);
						}
					}

					// check for EQUATION
					else if (currentAnnotation.equals(EQUATION)) {

						// in EQUATION first line
						if (line.startsWith(EQUATION)) {
							line = line.substring(EQUATION.length()).trim();
							bean.setEquation(line);
						} else {
							// adding to existing information
							line = line.trim();
							bean.setEquation(bean.getEquation() + " " + line);
						}
					}

					// check for PATHWAY
					else if (currentAnnotation.equals(PATHWAY)) {
						// do nothing
					}

					// check for ENZYME
					else if (currentAnnotation.equals(ENZYME)) {

						String enzymes = line.trim().toUpperCase();
						// in ENZYME first line
						if (enzymes.startsWith(ENZYME))
							enzymes = enzymes.substring(ENZYME.length()).trim();
						// there can be multiple enzymes
						for (String ecnumber : space.split(enzymes)) {
							bean.addECTerm(ecnumber.trim());
						}
					}

					// check for ORTHOLOGY (KO)
					else if (currentAnnotation.equals(ORTHOLOGY)) {

						String orthology = line.trim().toUpperCase();
						// in ORTHOLOGY first line
						if (orthology.startsWith(ORTHOLOGY))
							orthology = orthology.substring(ORTHOLOGY.length())
									.trim();
						// only get the K... identifier, drop rest
						String[] split = space.split(orthology);
						if (split[0].startsWith("K"))
							bean.addKoTerm(split[0].trim());
					}

					// check for NAME
					else if (currentAnnotation.equals(NAME)) {

						String names = line.trim();
						// in NAME first line
						if (names.startsWith(NAME))
							names = line.substring(NAME.length()).trim();
						nameList.append(names);
						nameList.append(" "); // space between names
					}

					// check for RPAIR
					else if (currentAnnotation.equals(RPAIR)) {

						String rpair = line.trim();
						// in RPAIR first line
						if (line.startsWith(RPAIR))
							rpair = line.substring(RPAIR.length()).trim();

						// parse different kinds of RPAIRs
						String[] values = space.split(rpair);
						if (values.length == 3) {
							String[] compoundPair = underscore.split(values[1]);
							if (compoundPair.length == 2) {
								try {
									RPAIRBean rpairBean = new RPAIRBean(
											values[0].trim(), RPAIRBean.Type
													.getType(values[2].trim()),
											compoundPair[0].trim(),
											compoundPair[1].trim());
									bean.addRpair(rpairBean);
								} catch (RPAIRBean.Type.UnknownRPAIRTypeException e) {
									e.printStackTrace();
								}
							} else {
								System.err
										.println("Compound pair in RPAIR of reaction "
												+ bean.getReactionId()
												+ " of ligand/reaction is not in recognized format: "
												+ rpair);
							}
						} else {
							System.err
									.println("RPAIR in reaction "
											+ bean.getReactionId()
											+ " of ligand/reaction is not in recognized format: "
											+ rpair);
						}
					}

					// check for COMMENT
					else if (currentAnnotation.equals(COMMENT)) {

						// in COMMENT first line
						if (line.startsWith(COMMENT)) {
							line = line.substring(COMMENT.length()).trim();
							bean.setComment(line);
						} else {
							// adding to existing information
							line = line.trim();
							bean.setComment(bean.getComment() + " " + line);
						}
					}
				}
			}
		}
		br.close();
		System.out.println("Parsed " + reactions.size()
				+ " reactions from reactions file");
	}

	public Map<String, ReactionBean> getReactions() {
		return reactions;
	}

	/**
	 * Bean that internally represents RPAIR in the reaction. Question: Is this
	 * information really used somewhere?
	 * 
	 * @author hindlem
	 */
	public static class RPAIRBean {

		public String id;

		public Type type;

		public String compound1;
		public String compound2;

		/**
		 * Class representing RPAIR types
		 * 
		 * @author hindlem
		 */
		enum Type {
			MAIN("main"), LEAVE("leave"), TRANS("trans"), COFAC("cofac"), LIGASE(
					"ligase");

			private String type;

			/**
			 * @param type
			 */
			Type(String type) {
				this.type = type;
			}

			public String getType() {
				return type;
			}

			public static Type getType(String description)
					throws UnknownRPAIRTypeException {
				if (description.equalsIgnoreCase("main")) {
					return MAIN;
				} else if (description.equalsIgnoreCase("leave")) {
					return LEAVE;
				} else if (description.equalsIgnoreCase("trans")) {
					return TRANS;
				} else if (description.equalsIgnoreCase("cofac")) {
					return COFAC;
				} else if (description.equalsIgnoreCase("ligase")) {
					return LIGASE;
				} else {
					throw new UnknownRPAIRTypeException(description);
				}
			}

			private static class UnknownRPAIRTypeException extends Throwable {
				/**
				 * default
				 */
				private static final long serialVersionUID = 1L;

				public UnknownRPAIRTypeException(String message) {
					super(message);
				}
			}
		}

		/**
		 * Constructor to set all fields
		 * 
		 * @param id
		 *            the name of the RPAIR
		 * @param type
		 *            the type of RPAIR
		 * @param compound1
		 *            the from compound in the conversion
		 * @param compound2
		 *            the to compound in the conversion
		 */
		public RPAIRBean(String id, Type type, String compound1,
				String compound2) {
			this.id = id;
			this.type = type;
			this.compound1 = compound1;
			this.compound2 = compound2;
		}

	}

	/**
	 * Bean that internally represents reactions in this file
	 * 
	 * @author hindlem
	 */
	public static class ReactionBean {

		private final String reactionId;

		private final Set<String> ecTerms = new HashSet<String>();
		private final Set<String> koTerms = new HashSet<String>();
		private final Set<String> names = new HashSet<String>();
		private final Set<RPAIRBean> rpairs = new HashSet<RPAIRBean>();

		private String definition;
		private String comment;
		private String equation;

		public ReactionBean(String reactionId) {
			this.reactionId = reactionId;
		}

		public String getReactionId() {
			return reactionId;
		}

		public void addKoTerm(String koTerm) {
			koTerms.add(koTerm);
		}

		public void addECTerm(String ecTerm) {
			ecTerms.add(ecTerm);
		}

		public void addName(String name) {
			names.add(name);
		}

		public void addRpair(RPAIRBean rpair) {
			rpairs.add(rpair);
		}

		public Set<String> getKoTerms() {
			return koTerms;
		}

		public Set<String> getECTerms() {
			return ecTerms;
		}

		public Set<String> geNames() {
			return names;
		}

		public Set<RPAIRBean> getRpairs() {
			return rpairs;
		}

		public String getDefinition() {
			return definition;
		}

		public void setDefinition(String definition) {
			this.definition = definition;
		}

		public String getEquation() {
			return equation;
		}

		public void setEquation(String equation) {
			this.equation = equation;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}
	}

}
