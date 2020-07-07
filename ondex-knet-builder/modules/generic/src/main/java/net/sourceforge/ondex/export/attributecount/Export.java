package net.sourceforge.ondex.export.attributecount;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.config.ValidatorRegistry;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.export.ONDEXExport;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;

/**
 * Count instances of Attribute name values e.g. TAXID lists value then count '3702	{instances}, 2456	{instances}, ...'
 *
 * @author hindlem
 */
@Authors(authors = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
@Status(description = "Set to DISCONTINUED 4 May 2010 due to System.out usage. (Christian)", status = StatusType.DISCONTINUED)
public class Export extends ONDEXExport implements ArgumentNames {

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(ATT_NAME_ARG, ATT_NAME_ARG_DESC, true, null, true),
                new StringArgumentDefinition(CCS_ARG, CCS_ARG_DESC, false, null, true)
        };
    }

    @Override
    public String getName() {
        return "Counts value instances for Attribute";
    }

    @Override
    public String getVersion() {
        return "30-June-08";
    }

    @Override
    public String getId() {
        return "attributecount";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public void start() throws InvalidPluginArgumentException {

        Set<ConceptClass> ccs = new HashSet<ConceptClass>();
        String[] ccNames = (String[]) getArguments().getObjectValueArray(CCS_ARG);
        if (ccNames != null && ccNames.length > 0) {
            for (String ccName : ccNames) {
                ConceptClass cc = graph.getMetaData().getConceptClass(ccName);
                if (cc == null)
                    ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new ConceptClassMissingEvent(
                            ccName, getCurrentMethodName()));
                else
                    ccs.add(cc);
            }
        }
        Set<ONDEXConcept> conceptBase = graph.getConcepts();

        if (ccs.size() > 0) {
            conceptBase = BitSetFunctions.create(graph, ONDEXConcept.class, new BitSet(0));

            for (ConceptClass cc : ccs) {
                conceptBase.addAll(graph.getConceptsOfConceptClass(cc));
            }
        }

        for (String attName : (List<String>) getArguments().getObjectValueList(ATT_NAME_ARG)) {
            AttributeName att = graph.getMetaData().getAttributeName(attName);
            if (att == null) {
                fireEventOccurred(new AttributeNameMissingEvent(attName + " not found", getCurrentMethodName()));
            } else {
                HashMap<Object, Integer> objectsToValues = new HashMap<Object, Integer>();

                Set<ONDEXConcept> concepts = graph.getConceptsOfAttributeName(att);
                concepts.retainAll(conceptBase);

                for (ONDEXConcept abstractConcept : concepts) {
                    System.out.println(abstractConcept.getOfType().getId());
                    String value = abstractConcept.getAttribute(att).getValue().toString();
                    Integer count = objectsToValues.get(value);
                    if (count == null) {
                        count = 1;
                    } else {
                        count++;
                    }

                    objectsToValues.put(value, count);
                }

                printValueMap(objectsToValues);

                objectsToValues = new HashMap<Object, Integer>();
                for (ONDEXRelation relation : graph.getRelationsOfAttributeName(att)) {
                    Object value = relation.getAttribute(att).getValue();
                    Integer count = objectsToValues.get(value);
                    if (count == null) {
                        count = 1;
                    } else {
                        count++;
                    }

                    objectsToValues.put(value, count);
                }

                System.out.println("\nRelation Attribute Instances of " + att.getId() + "+\n");
                printValueMap(objectsToValues);
            }
        }

    }

    /**
     * prints to sys out the values in this map
     *
     * @param objectsToValues values to print
     */
    private void printValueMap(HashMap<Object, Integer> objectsToValues) {

        for (Object o : objectsToValues.keySet()) {
            String string = o.toString();
            Integer count = objectsToValues.get(string);
            AbstractONDEXValidator validator = ValidatorRegistry.validators.get("scientificspeciesname");

            String scientificName = (String) validator.validate(string);
            if (scientificName != null) {
                string = scientificName + " \t " + string;
            }
            System.out.println(string + "\t" + count);
        }
    }

    /**
     * Convenience method for outputing the current method name in a dynamic way
     *
     * @return the calling method name
     */
    public static String getCurrentMethodName() {
        Exception e = new Exception();
        StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
        String name = trace.getMethodName();
        String className = trace.getClassName();
        int line = trace.getLineNumber();
        return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line + "]";
    }

    @Override
    public String[] requiresValidators() {
        return new String[]{"scientificspeciesname"};
    }
}
