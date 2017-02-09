package net.sourceforge.ondex.scripting.wrappers;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.memory.MemoryONDEXConcept;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.memory.MemoryONDEXRelation;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import static net.sourceforge.ondex.tools.functions.StandardFunctions.*;

/**
 * General-purpose functions used in scripting
 *
 * @author lysenkoa
 *         net.sourceforge.ondex.xten.functions.scripting_functions.General
 */
public class General {

    public static void createDir(String parent, String name) {
        File f = new File(parent + File.separator + name);
        f.mkdir();
    }

    public static void saveGraph(ONDEXGraph graph, String filename) {
        //new OXLExport(graph, new File(filename));
    }

    /**
     * Cleans up go annotation by leaving only the most specific ones on each branch.
     *
     * @param graph - graph with go annotations to process
     */
    public static String goAnnotationCleaner(final ONDEXGraph graph) {
        int removed = 0;
        Map<ConceptClass, Set<ONDEXConcept>> goCCs = new HashMap<ConceptClass, Set<ONDEXConcept>>();
        final Map<ConceptClass, Set<ONDEXConcept>> toRemove = new HashMap<ConceptClass, Set<ONDEXConcept>>();
        final Set<String> roots = new HashSet<String>(Arrays.asList(new String[]{"GO:0008150", "GO:0003674", "GO:0005575"}));
        Set<ONDEXConcept> other = graph.getConcepts();
        for (String gc : new String[]{"MolFunc", "BioProc", "CelComp"}) {
            ConceptClass cc = graph.getMetaData().getConceptClass(gc);
            other.removeAll(graph.getConceptsOfConceptClass(cc));
            goCCs.put(cc, new HashSet<ONDEXConcept>());
            toRemove.put(cc, new HashSet<ONDEXConcept>());
        }

        int entCounter = 1;
        for (ONDEXConcept c : other) {
            //System.err.println("Processing annotated entyty "+entCounter+" out of "+other.size());
            entCounter++;
            for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
                ONDEXConcept opp = getOtherNode(c, r);
                Set<ONDEXConcept> holder = goCCs.get(opp.getOfType());
                if (holder != null) {
                    holder.add(opp);
                }
            }

            Set<Thread> toWaitFor = new HashSet<Thread>();
            for (final Entry<ConceptClass, Set<ONDEXConcept>> ent : goCCs.entrySet()) {
                Thread t = new Thread() {
                    public void run() {
                        Set<ONDEXConcept> subset = new HashSet<ONDEXConcept>();
                        for (ONDEXConcept z : ent.getValue()) {
                            if (subset.contains(z))
                                continue;
                            if (accTest(z, roots)) {
                                subset.add(z);
                                continue;
                            }
                            Set<ONDEXConcept> parents = relationsToTargets(getOutgoingRelationsToConceptClass(graph, z, ent.getKey()));
                            while (parents.size() > 0) {
                                Set<ONDEXConcept> next = new HashSet<ONDEXConcept>();
                                for (ONDEXConcept p : parents) {
                                    if (!accTest(p, roots)) {
                                        if (ent.getValue().contains(p) && !p.equals(z))
                                            subset.add(p);
                                        next.addAll(relationsToTargets(getOutgoingRelationsToConceptClass(graph, p, ent.getKey())));
                                    } else {
                                        subset.add(p);
                                    }
                                }
                                parents = next;
                            }
                        }
                        ent.getValue().clear();
                        toRemove.get(ent.getKey()).addAll(subset);
                    }
                };
                toWaitFor.add(t);
                t.start();
            }
            for (Thread t : toWaitFor) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            toWaitFor.clear();

            Set<ONDEXConcept> removeSet = new HashSet<ONDEXConcept>();
            for (Set<ONDEXConcept> set : toRemove.values()) {
                removeSet.addAll(set);
                set.clear();
            }
            for (ONDEXRelation del : graph.getRelationsOfConcept(c)) {
                if (removeSet.contains(getOtherNode(c, del))) {
                    graph.deleteRelation(del.getId());
                    removed++;
                }
            }
        }
        return "Removed " + removed + " less specific annotations form the graph.";

    }

    private static boolean accTest(ONDEXConcept c, Set<String> accs) {
        boolean result = false;
        for (ConceptAccession ca : c.getConceptAccessions()) {
            if (accs.contains(ca.getAccession())) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static void exportToSungear(ONDEXGraph graph, String acc_type_to_use, String fileName, String... atts) {
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
            List<AttributeName> ats = new ArrayList<AttributeName>();
            DataSource dataSource = graph.getMetaData().getDataSource(acc_type_to_use);
            boolean first = true;
            for (String s : atts) {
                ats.add(graph.getMetaData().getAttributeName(s));
                if (first) {
                    first = false;
                    bw.write(s);
                    bw.flush();
                } else {
                    bw.write(" | " + s);
                    bw.flush();
                }
            }
            bw.write(" | gene\n");
            bw.flush();

            for (ONDEXConcept c : graph.getConcepts()) {
                Set<AttributeName> found = new HashSet<AttributeName>();
                for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
                    for (AttributeName at : ats) {
                        if (r.getAttribute(at) != null) {
                            found.add(at);
                        }
                    }
                }
                if (found.size() > 0) {
                    String acc = "???";
                    List<String> accs = getAccessionsOfType(c, dataSource);
                    if (accs.size() > 0) {
                        if (acc_type_to_use.equals("TAIR")) {
                            for (String s : accs) {
                                if (!s.contains(".")) {
                                    acc = s;
                                    break;
                                }
                            }
                            if (acc.equals("???")) {
                                acc = accs.get(0).substring(0, accs.get(0).indexOf("."));
                            }
                        } else {
                            acc = accs.get(0);
                        }
                    }
                    first = true;
                    for (AttributeName an : ats) {
                        if (first) {
                            first = false;
                            bw.write(found.contains(an) ? "1" : "0");
                            bw.flush();
                        } else {
                            bw.write(" | " + (found.contains(an) ? "1" : "0"));
                            bw.flush();
                        }
                    }
                    bw.write(" | " + acc + "\n");
                    bw.flush();
                }
            }
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*private static class OXLExport extends Export {

         public OXLExport(ONDEXGraph aog, File file) {

             XMLOutputFactory2 xmlOutput = (XMLOutputFactory2) XMLOutputFactory2
                     .newInstance();
             xmlOutput.configureForSpeed();
             xmlOutput.setProperty(XMLOutputFactory2.IS_REPAIRING_NAMESPACES, false);

             int detectedEnding = ZipEndings.getPostfix(file);

             try {

                 OutputStream outStream = null;

                 switch (detectedEnding) {

                 case ZipEndings.GZ:
                     // use gzip compression
                     outStream = new GZIPOutputStream(new FileOutputStream(file));
                     System.out.println("Detected GZIP file");
                     break;
                 case ZipEndings.ZIP:
                     System.err.println("ZIP file not supported");
                     break;
                 case ZipEndings.XML:
                     // output file writer
                     outStream = new FileOutputStream(file);
                     System.out.println("Detected Uncompressed file");
                     break;
                 default:
                     File f = new File(file.getAbsolutePath()+".xml.gz");
                     outStream = new GZIPOutputStream(new FileOutputStream(f));
                 }

                 if (outStream != null) {

                     XMLStreamWriter2 xmlWriteStream = (XMLStreamWriter2) xmlOutput
                             .createXMLStreamWriter(outStream, CharsetNames.CS_UTF8);
                     buildDocument(xmlWriteStream, aog);

                     xmlWriteStream.flush();
                     xmlWriteStream.close();

                     outStream.flush();
                     outStream.close();
                 }

             } catch (MalformedURLException e) {
                 e.printStackTrace();
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             } catch (XMLStreamException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }*/
    
    /**
     * Clone a concept from one graph to another.
     * @param concept to clone
     * @param fromGraph
     * @param toGraph
     * @return id of the cloned concept
     */
    public static int cloneConcept(MemoryONDEXConcept concept, ONDEXGraph fromGraph, ONDEXGraph toGraph) {
    	ONDEXGraphCloner graphCloner = new ONDEXGraphCloner(fromGraph, toGraph);
    	ONDEXConcept clonedConcept = graphCloner.cloneConcept(concept);
    	return clonedConcept.getId();
    }
    
    /**
     * Clone a relation from one graph to another.
     * @param relation to clone
     * @param fromGraph
     * @param toGraph
     * @return id of the cloned relation
     */
    public static int cloneRelation(MemoryONDEXRelation relation, ONDEXGraph fromGraph, ONDEXGraph toGraph) {
    	ONDEXGraphCloner graphCloner = new ONDEXGraphCloner(fromGraph, toGraph);
    	ONDEXRelation clonedRelation = graphCloner.cloneRelation(relation);
    	return clonedRelation.getId();
    }

    /**
     * Create a new {@link MemoryONDEXGraph} with the given name.
     * @param name Name of the new graph.
     * @return The new {@link MemoryONDEXGraph}.
     */
    public static ONDEXGraph createGraph(String name) {
    	return new MemoryONDEXGraph(name);
    }
    
    /**
     * Shows a dialog with the given <code>message</code>.
     * @param message
     */
    public static void alert(String message) {
    	JOptionPane.showMessageDialog(null, message);
    }
    
    /**
     * Shows a input dialog with the given <code>message</code>. 
     * @param message
     * @return The user input.
     */
    public static String prompt(String message) {
    	return JOptionPane.showInputDialog(message);
    }
    
    /**
     * Shows a confirm dialog with the given <code>message</code>.
     * @param message
     * @return true if the user selected the YES option.
     */
    public static boolean confirm(String message) {
    	return JOptionPane.YES_OPTION==JOptionPane.showConfirmDialog(null, message, "Select an Option", JOptionPane.YES_NO_OPTION);
    }
    
}
