package net.sourceforge.ondex.parser.kegg53.reaction;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import net.sourceforge.ondex.parser.kegg53.data.Pathway;
import net.sourceforge.ondex.parser.kegg53.data.Reaction;
import net.sourceforge.ondex.parser.kegg53.util.DPLPersistantSet;

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
 * Parses the reaction componant of the ligand database
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
     * @param pathways
     */
    public void addReactionInfoToPathways(DPLPersistantSet<Pathway> pathways) {
        int i = 0;
        EntityCursor<Pathway> cursor = pathways.getCursor();
        for (Pathway pathway : cursor) {
            Map<String, Reaction> pathwayReactions = pathway.getReactions();
            for (String pathwayReactionId : pathwayReactions.keySet()) {
                if (pathwayReactionId.toUpperCase().startsWith("RN:")) {
                    pathwayReactionId = pathwayReactionId.substring("RN:".length());
                }
                ReactionBean reactionBean = reactions.get(pathwayReactionId);
                if (reactionBean == null) {
                    System.err.println(pathwayReactionId + " is not found in reaction (ligand db) file");
                } else {
                    i++;
                    Reaction pathwayReaction = pathwayReactions.get("RN:" + pathwayReactionId);
                    pathwayReaction.setNames(reactionBean.geNames());
                    pathwayReaction.setECTerms(reactionBean.getECTerms());
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
     * @param ligandFile
     */
    public void parse(InputStream ligandFile) throws IOException {
        reactions = new HashMap<String, ReactionBean>();

        final Pattern colon = Pattern.compile(":");
        final Pattern startLabel = Pattern.compile("^[A-Z]+");
        final Pattern space = Pattern.compile("[ |    ]+");
        final Pattern underscore = Pattern.compile("_");

        ReactionBean bean = null;
        String currentAnnotation = null;
        StringBuilder nameList = new StringBuilder();

        BufferedReader br = new BufferedReader(new InputStreamReader(ligandFile));
        while (br.ready()) {
            String line = br.readLine();

            if (line.startsWith(ENTRY)) {
                if (nameList.length() > 0) {
                    for (String name : colon.split(nameList.toString()))
                        bean.addName(name.trim());
                    nameList.setLength(0);
                }

                String entry = space.split(line.substring(ENTRY.length()).trim().toUpperCase())[0].trim();
                bean = new ReactionBean(entry);
                reactions.put(entry, bean);
                currentAnnotation = null;
            }
            if (bean != null) {

                Matcher matcher = startLabel.matcher(line);

                if (matcher.find()) {
                    currentAnnotation = matcher.group(0);
                }

                if (currentAnnotation != null && !line.startsWith("///")) {
                    if (currentAnnotation.equals(DEFINITION)) {
                        if (line.startsWith(DEFINITION))
                            line = line.substring(DEFINITION.length()).trim();
                        else
                            line = line.trim();
                        if (bean.getDefinition() != null)
                            bean.setDefinition(bean.getDefinition() + line);
                        else
                            bean.setDefinition(line);
                    } else if (currentAnnotation.equals(EQUATION)) {
                        if (line.startsWith(EQUATION))
                            line = line.substring(EQUATION.length()).trim();
                        else
                            line = line.trim();
                        if (bean.getEquation() != null)
                            bean.setEquation(bean.getEquation() + line);
                        else
                            bean.setEquation(line);
                    } else if (currentAnnotation.equals(PATHWAY)) {
                        //do nothing
                    } else if (currentAnnotation.equals(ENZYME)) {
                        String enzymes = line.trim().toUpperCase();
                        if (enzymes.startsWith(ENZYME))
                            enzymes = enzymes.substring(ENZYME.length()).trim();
                        for (String ecnumber : space.split(enzymes)) {
                            bean.addECTerm(ecnumber.trim());
                        }
                    } else if (currentAnnotation.equals(ORTHOLOGY)) {
                        //do nothing
                    } else if (currentAnnotation.equals(NAME)) {
                        String names = line.trim();
                        if (names.startsWith(NAME))
                            names = line.substring(NAME.length()).trim();
                        nameList.append(names);
                    } else if (currentAnnotation.equals(RPAIR)) {
                        String rpair = line.trim();
                        if (line.startsWith(RPAIR))
                            rpair = line.substring(RPAIR.length()).trim();

                        String[] values = space.split(rpair);
                        if (values.length == 3) {
                            String[] compoundPair = underscore.split(values[1]);
                            if (compoundPair.length == 2) {
                                try {
                                    RPAIRBean rpairBean = new RPAIRBean(values[0].trim(),
                                            RPAIRBean.Type.getType(values[2].trim()),
                                            compoundPair[0].trim(),
                                            compoundPair[1].trim());
                                    bean.addRpair(rpairBean);
                                } catch (RPAIRBean.Type.UnknownRPAIRTypeException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                System.err.println("Compound pair in RPAIR of reaction " + bean.getReactionId() + " of ligand/reaction is not in recognized format: " + rpair);
                            }
                        } else {
                            System.err.println("RPAIR in reaction " + bean.getReactionId() + " of ligand/reaction is not in recognized format: " + rpair);
                        }
                    } else if (currentAnnotation.equals(COMMENT)) {
                        String comment = line.trim();
                        if (line.startsWith(COMMENT))
                            comment = line.substring(COMMENT.length()).trim();
                        if (bean.getComment() != null)
                            bean.setComment(bean.getComment() + comment);
                        else
                            bean.setComment(comment);
                    }
                }
            }
        }
        br.close();
        System.out.println("Parsed " + reactions.size() + " reactions from reactions file");
    }

    public Map<String, ReactionBean> getReactions() {
        return reactions;
    }

    /**
     * Bean that internally represents rpair in the reaction
     *
     * @author hindlem
     */
    public static class RPAIRBean {

        private String id;

        private Type type;

        private String compound1;
        private String compound2;

        /**
         * Class representing RPAIR types
         *
         * @author hindlem
         */
        enum Type {
            MAIN("main"),
            LEAVE("leave"),
            TRANS("trans"),
            COFAC("cofac"),
            LIGASE("ligase");

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

            public static Type getType(String description) throws UnknownRPAIRTypeException {
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
                public UnknownRPAIRTypeException(String message) {
                    super(message);
                }
            }
        }

        /**
         * @param id        the name of the RPAIR
         * @param type      the type of RPAIR
         * @param compound1 the from compound in the conversion
         * @param compound2 the to compound in the conversion
         */
        public RPAIRBean(String id, Type type, String compound1, String compound2) {
            this.id = id;
            this.type = type;
            this.compound1 = compound1;
            this.compound2 = compound2;
        }

    }

    /**
     * Bean that internaly represents reactions in this file
     *
     * @author hindlem
     */
    public static class ReactionBean {

        private final String reactionId;
        private final Set<String> ecTerms = new HashSet<String>();
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

        public void addECTerm(String ecTerm) {
            ecTerms.add(ecTerm);
        }

        public void addName(String name) {
            names.add(name);
        }

        public void addRpair(RPAIRBean rpair) {
            rpairs.add(rpair);
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
