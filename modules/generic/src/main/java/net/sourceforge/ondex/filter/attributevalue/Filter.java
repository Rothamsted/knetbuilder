package net.sourceforge.ondex.filter.attributevalue;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.exception.type.WrongArgumentException;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

/**
 * Removes concepts with specified Attribute values from the graph.
 *
 * @author taubertj
 * @version 16.04.2008
 */
@Authors(authors = {"Jan Taubert"}, emails = {"jantaubert at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Filter extends ONDEXFilter implements ArgumentNames {

    // contains list of visible concepts
    private Set<ONDEXConcept> concepts = null;

    // contains list of visible relations
    private Set<ONDEXRelation> relations = null;

    // contains list of invisible concepts
    private Set<ONDEXConcept> invconcepts = null;

    // contains list of invisible relations
    private Set<ONDEXRelation> invrelations = null;

    /**
     * Constructor
     */
    public Filter() {
        super();
    }

    @Override
    public void copyResultsToNewGraph(ONDEXGraph exportGraph) {

        ONDEXGraphCloner graphCloner = new ONDEXGraphCloner(graph, exportGraph);
        for (ONDEXConcept c : concepts) {
            graphCloner.cloneConcept(c);
        }
        for (ONDEXRelation r : relations) {
            graphCloner.cloneRelation(r);
        }
    }

    @Override
    public Set<ONDEXConcept> getVisibleConcepts() {
        return BitSetFunctions.unmodifiableSet(concepts);
    }

    @Override
    public Set<ONDEXRelation> getVisibleRelations() {
        return BitSetFunctions.unmodifiableSet(relations);
    }

    public Set<ONDEXConcept> getInVisibleConcepts() {
        return BitSetFunctions.unmodifiableSet(invconcepts);
    }

    public Set<ONDEXRelation> getInVisibleRelations() {
        return BitSetFunctions.unmodifiableSet(invrelations);
    }

    /**
     * Returns the name of this filter.
     *
     * @return name
     */
    public String getName() {
        return "AttributeValue Filter";
    }

    /**
     * Returns the version of this filter.
     *
     * @return version
     */
    public String getVersion() {
        return "07.05.2008";
    }

    @Override
    public String getId() {
        return "attributevalue";
    }


    /**
     * Arguments about AttributeName, GDSValue and including parameter.
     *
     * @return three argument definitions
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        StringArgumentDefinition at_arg = new StringArgumentDefinition(
                ATTRNAME_ARG, ATTRNAME_ARG_DESC, true, null, false);
        StringArgumentDefinition value_arg = new StringArgumentDefinition(
                VALUE_ARG, VALUE_ARG_DESC, true, null, true);
        StringArgumentDefinition concept_class_arg = new StringArgumentDefinition(
                CC_ARG, CC_ARG_DESC, false, null, true);
        StringArgumentDefinition rt_arg = new StringArgumentDefinition(RTS_ARG,
                RTS_ARG_DESC, false, null, true);
        StringArgumentDefinition op_arg = new StringArgumentDefinition(
                OPERATOR_ARG, OPERATOR_ARG_DESC, false, "=", false);
        BooleanArgumentDefinition include_arg = new BooleanArgumentDefinition(
                INCLUDING_ARG, INCLUDING_ARG_DESC, true, true);
        BooleanArgumentDefinition ignore_arg = new BooleanArgumentDefinition(
                IGNORE_ARG, IGNORE_ARG_DESC, false, false);
        BooleanArgumentDefinition modulus_arg = new BooleanArgumentDefinition(
                MODULUS_ARG, MODULUS_ARG_DESC, false, false);
        return new ArgumentDefinition<?>[]{at_arg, value_arg, include_arg,
                concept_class_arg, rt_arg, ignore_arg, op_arg, modulus_arg};
    }

    /**
     * Defines operators to apply when filtering gds
     *
     * @author hindlem
     */
    static enum Operator {
        EQU, GREATER, LESS, GREATER_EQU, LESS_EQU;

        static Operator getOperator(String value) throws WrongArgumentException {
            if (value == null)
                return EQU;
            if (value.equalsIgnoreCase("=") || value.equalsIgnoreCase("==")) {
                return EQU;
            } else if (value.equalsIgnoreCase(">")) {
                return GREATER;
            } else if (value.equalsIgnoreCase("<")) {
                return LESS;
            } else if (value.equalsIgnoreCase(">=")) {
                return GREATER_EQU;
            } else if (value.equalsIgnoreCase("<=")) {
                return LESS_EQU;
            }
            throw new WrongArgumentException(value
                    + " is an invalid argument for " + OPERATOR_ARG);
        }
    }

    /**
     * Filters the graph and constructs the lists for visible concepts and
     * relations.
     *
     * @throws WrongArgumentException
     */
    public void start() throws WrongArgumentException, InvalidPluginArgumentException {

        Operator operator = Operator.getOperator((String) args
                .getUniqueValue(OPERATOR_ARG));

        Boolean modulus = (Boolean) args.getUniqueValue(MODULUS_ARG);

        fireEventOccurred(new GeneralOutputEvent("Operator: " + operator,
                "[Filter - start]"));

        concepts = graph.getConcepts();
        relations = graph.getRelations();

        Object[] conceptClasses = args.getObjectValueArray(CC_ARG);

        if (conceptClasses != null && conceptClasses.length > 0) {
            concepts = BitSetFunctions.create(graph,
                    ONDEXConcept.class, new BitSet());

            for (Object cClass : conceptClasses) {

                if (((String) cClass).equalsIgnoreCase("none")
                        && conceptClasses.length == 1) {
                    fireEventOccurred(new GeneralOutputEvent(
                            "Restrictions will not be applied to Concepts",
                            "[Filter - start]"));
                    break; // we take the empty set so do not filter concepts
                }

                ConceptClass cc = graph.getMetaData().getConceptClass(
                        (String) cClass);
                if (cc == null) {
                    fireEventOccurred(new GeneralOutputEvent(
                            "Unknown Concept Class :" + cClass + " ignoring",
                            "[Filter - start]"));
                } else {
                    fireEventOccurred(new GeneralOutputEvent(
                            "Restrictions will be applied to Concept of ConceptClass "
                                    + cc.getId(), "[Filter - start]"));
                    concepts = BitSetFunctions.or(concepts, graph
                            .getConceptsOfConceptClass(cc));
                }
            }
        }

        String[] rtss = (String[]) args.getObjectValueArray(RTS_ARG);

        if (rtss != null && rtss.length > 0) {
            relations = BitSetFunctions.create(graph,
                    ONDEXRelation.class, new BitSet());

            for (String rtsName : rtss) {
                if (rtsName.equalsIgnoreCase("none")
                        && rtss.length == 1) {
                    fireEventOccurred(new GeneralOutputEvent(
                            "Restrictions will not be applied to Relations",
                            "[Filter - start]"));
                    break; // we take the empty set so do not filter relations
                }

                RelationType rts = graph.getMetaData().getRelationType(rtsName);
                if (rts == null) {
                    fireEventOccurred(new GeneralOutputEvent(
                            "Unknown Relation type set :" + rts + " ignoring",
                            "[Filter - start]"));
                } else {
                    fireEventOccurred(new GeneralOutputEvent(
                            "Restrictions will be applied to Relations of RelationType"
                                    + rts.getId(), "[Filter - start]"));
                    relations = BitSetFunctions.or(relations, graph
                            .getRelationsOfRelationType(rts));
                }
            }
        }

        fireEventOccurred(new GeneralOutputEvent("Filtering from "
                + concepts.size() + " concepts and " + relations.size()
                + " relations", "[Filter - start]"));

        // get AttributeName
        AttributeName an = null;
        String anID = (String) args.getUniqueValue(ATTRNAME_ARG);
        if (anID.trim().length() > 0) {
            an = graph.getMetaData().getAttributeName(anID);
            if (an != null) {
                fireEventOccurred(new GeneralOutputEvent("For AttributeName "
                        + an.getId(), "[Filter - start]"));
            } else {
                fireEventOccurred(new WrongParameterEvent(anID
                        + " is not a valid AttributeName.", "[Filter - start]"));
            }
        } else {
            fireEventOccurred(new WrongParameterEvent(
                    "No AttributeName given.", "[Filter - start]"));
        }

        // check for null values
        boolean ignore = false;
        if (args.getUniqueValue(IGNORE_ARG) != null)
            ignore = (Boolean) args.getUniqueValue(IGNORE_ARG);

        // sets for concepts/relations identified for removal
        BitSet removeConcepts = new BitSet();
        BitSet removeRelations = new BitSet();

        // check concepts for Attribute
        Set<ONDEXConcept> itc = graph.getConceptsOfAttributeName(an);
        itc = BitSetFunctions.and(concepts, itc); // if concepts are all this makes no
        // difference else if only on concept
        // class is selected it deletes only
        // within one

        if (ignore) {

            // visibility of concepts is easy
            invconcepts = BitSetFunctions.andNot(concepts, itc);
            concepts = BitSetFunctions.andNot(graph.getConcepts(), invconcepts);

            return;
        }

        Object[] values = args.getObjectValueArray(VALUE_ARG);
        boolean nulls = false;
        HashSet<Object> valSet = new HashSet<Object>();
        for (Object v : values) {
            nulls = nulls || (v == null);
            if (v != null) {
                if (Number.class.isAssignableFrom(an.getDataType()) && v instanceof String) {
                    v = castToNumber(v);
                }
                // this is the case for boolean values
                else if (Boolean.class.isAssignableFrom(an.getDataType()) && v instanceof String) {
            	   v = Boolean.valueOf((String) v);
                }
                valSet.add(v);
            }
        }
        if (values.length == 0 || nulls) {
            fireEventOccurred(new WrongParameterEvent("No GDSValue given.",
                    "[Filter - start]"));
            return;
        }

        // get desired behaviour
        boolean including = (Boolean) args.getUniqueValue(INCLUDING_ARG);

        // remove none matching concepts or relations
        for (ONDEXConcept c: itc) {
            Attribute attribute = c.getAttribute(an);
            if (including == !isValidValue(valSet, attribute.getValue(), operator, modulus)) {
                // including and not a valid value remove or not including and a
                // valid value remove
                removeConcepts.set(c.getId());
                for (ONDEXRelation relation : graph.getRelationsOfConcept(c)) {
                    removeRelations.set(relation.getId());
                }
            }
        }

        // check relations for Attribute
        Set<ONDEXRelation> itr = graph.getRelationsOfAttributeName(an);
        itr = BitSetFunctions.and(relations, itr);
        for (ONDEXRelation r : itr) {
            Attribute attribute = r.getAttribute(an);
            if (including == !isValidValue(valSet, attribute.getValue(), operator, modulus)) {
                // including and not a valid value remove or not including and a
                // valid value remove
                removeRelations.set(r.getId());
            }
        }

        // visibility of concepts is easy
        invconcepts = BitSetFunctions.create(graph,
                ONDEXConcept.class, removeConcepts);
        int conceptsToRemove = invconcepts.size();

        concepts = BitSetFunctions.copy(graph.getConcepts());
        concepts.removeAll(invconcepts);
        invconcepts = BitSetFunctions.copy(graph.getConcepts());
        invconcepts.removeAll(concepts);

        // so is relations
        invrelations = BitSetFunctions.create(graph,
                ONDEXRelation.class, removeRelations);
        int relationsToRemove = invrelations.size();

        relations = BitSetFunctions.copy(graph.getRelations());
        relations.removeAll(invrelations);
        invrelations = BitSetFunctions.copy(graph.getRelations());
        invrelations.removeAll(relations);

        fireEventOccurred(new GeneralOutputEvent(conceptsToRemove
                + " concepts to remove", "[Filter - start]"));
        fireEventOccurred(new GeneralOutputEvent(relationsToRemove
                + " relations to remove", "[Filter - start]"));
    }

    private boolean isValidValue(HashSet<Object> valSet, Object value,
                                 Operator operator, boolean modulus) {

        if (value instanceof Number) {
            value = Math.abs(((Number) value).doubleValue());
        }

        if (operator == Operator.EQU) {
            return valSet.contains(value);
        } else if (operator == Operator.GREATER_EQU) {
            boolean equ = valSet.contains(value);
            if (!equ) {
                return greaterOnNumericals(valSet, value);
            }
            return equ;
        } else if (operator == Operator.LESS_EQU) {
            boolean equ = valSet.contains(value);
            if (!equ) {
                return lessOnNumericals(valSet, value);
            }
            return equ;
        } else if (operator == Operator.GREATER) {
            return greaterOnNumericals(valSet, value);
        } else if (operator == Operator.LESS) {
            return lessOnNumericals(valSet, value);
        }
        throw new RuntimeException("Unhandled operator :" + operator);
    }

    /**
     * returns true if value is greater than a value in valSet (and both are
     * Number Objects)
     *
     * @param valSet a set of objects
     * @param value  an object
     * @return value is greater than a value in valSet
     */
    private boolean greaterOnNumericals(HashSet<Object> valSet, Object value) {
        if (value instanceof Number) {
            double num = ((Number) value).doubleValue();
            for (Object val : valSet) {
                if (val instanceof Number) {
                    if (num > ((Number) val).doubleValue()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Object castToNumber(Object val) {
        try {
            val = Double.parseDouble(val.toString());
        } catch (NumberFormatException e) {
            try {
                val = Float.parseFloat(val.toString());
            } catch (NumberFormatException e2) {
                //ignore
                try {
                    val = Integer.parseInt(val.toString());
                } catch (NumberFormatException e3) {
                    //ignore
                }
            }
            //ignore
        }
        return val;
    }

    /**
     * returns true if value is less than a value in valSet (and both are Number
     * Objects)
     *
     * @param valSet a set of objects
     * @param value  an object
     * @return value is greater than a value in valSet
     */
    private boolean lessOnNumericals(HashSet<Object> valSet, Object value) {
        if (value instanceof Number) {
            double num = ((Number) value).doubleValue();
            for (Object val : valSet) {
                if (val instanceof Number) {
                    if (num < ((Number) val).doubleValue()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * An indexed graph is not required.
     *
     * @return false
     */
    public boolean requiresIndexedGraph() {
        return false;
    }

    public String[] requiresValidators() {
        return new String[0];
    }
}
