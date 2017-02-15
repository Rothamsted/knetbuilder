package net.sourceforge.ondex.export.intersectionofcvs;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.export.ONDEXExport;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;

/**
 * For ontology to pathway work
 *
 * @author hindlem
 */
@Authors(authors = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
@Status(description = "Set to DISCONTINUED 4 May 2010 due to System.out usage. (Christian)", status = StatusType.DISCONTINUED)
public class Export extends ONDEXExport implements ArgumentNames {

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(CCS_ARG, CCS_ARG_DESC, false, null, true),
                new StringArgumentDefinition(FILE_ARG, FILE_ARG_DESC, false, null, false)
        };
    }

    @Override
    public String getName() {
        return "Intesection of cvs";
    }

    @Override
    public String getVersion() {
        return "07-Feb-08";
    }

    @Override
    public String getId() {
        return "intersectionofcvs";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    private PrintStream os = null;

    @Override
    public void start() throws InvalidPluginArgumentException {

        String fileArg = (String) getArguments().getUniqueValue(FILE_ARG);

        if (fileArg != null) {
            try {
                os = new PrintStream(new FileOutputStream(fileArg));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            os = System.out;
        }

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
        } else {
            for (ConceptClass cc : graph.getMetaData().getConceptClasses()) {
                if (graph.getConceptsOfConceptClass(cc).size() > 0) {
                    ccs.add(cc);
                }
            }
        }

        for (ConceptClass cc : ccs) {

            os.println("ConceptClass: " + cc.getId());
            Map<String, Integer> cvCount = new HashMap<String, Integer>();
            Map<String, Integer> cvTotalCount = new HashMap<String, Integer>();


            for (ONDEXConcept concept : graph.getConceptsOfConceptClass(cc)) {
                String cvFull = concept.getElementOf().getId();

                Integer count = cvCount.get(cvFull);

                if (count == null) {
                    count = 1;
                } else {
                    count++;
                }
                cvCount.put(cvFull, count);

                String[] cvs = cvFull.split(":");
                for (String cv : cvs) {
                    count = cvTotalCount.get(cv);
                    if (count == null) {
                        count = 1;
                    } else {
                        count++;
                    }
                    cvTotalCount.put(cv, count);
                }
            }


            Set<String> cvs4Header = cvTotalCount.keySet();
            List<String> cvs = new ArrayList<String>(cvs4Header);
            String header = null;
            for (String cv : cvs) {
                if (header == null)
                    header = cv;
                else
                    header = header + "\t" + cv;
            }
            if (header != null)
                os.println(header + "\t" + "Counts");
            else
                os.println("None present");

            for (String intersectioncv : cvCount.keySet()) {
                Integer count = cvCount.get(intersectioncv);

                String[] componantCvs = intersectioncv.split(":");
                boolean[] indexes = new boolean[cvs.size()];
                Arrays.fill(indexes, false);
                for (String cv : componantCvs) {
                    indexes[cvs.indexOf(cv)] = true;
                }
                String intersection = null;
                for (boolean present : indexes) {
                    if (intersection == null) {
                        intersection = Boolean.toString(present);
                    } else {
                        intersection = intersection + "\t" + Boolean.toString(present);
                    }
                }
                os.println(intersection + "\t" + count.toString());
            }
            os.println("");

            if (os != System.out) os.close();


            System.out.println();
            for (String cv : cvTotalCount.keySet()) {
                Integer count = cvTotalCount.get(cv);
                os.println("Total Count: (" + cv + "," + count + ") ");
            }


        }
    }

    /**
     * Gets the print stream to which the statistics are printed
     *
     * @return the print stream
     */
    public PrintStream getPrintStream() {
        return os;
    }

    /**
     * Sets the print stream to which the statistics are printed
     *
     * @param os
     */
    public void setPrintStream(PrintStream os) {
        this.os = os;
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
        return new String[0];
    }
}