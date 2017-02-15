package net.sourceforge.ondex.tools.functions;


import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createAttName;
import static net.sourceforge.ondex.tools.functions.StandardFunctions.getAccessionsOfType;
import static net.sourceforge.ondex.tools.functions.StandardFunctions.getOtherNode;
import static net.sourceforge.ondex.tools.functions.StandardFunctions.getOutgoingRelationsToConceptClass;
import static net.sourceforge.ondex.tools.functions.StandardFunctions.hasMatch;
import static net.sourceforge.ondex.tools.functions.StandardFunctions.relationsToTargets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.StreamGobbler;

/**
 * @author lysenkoa
 */
public class Clustering {

    private Clustering() {
    }

    // fixme: I don't understand the while loop, and this doesn't seem to be used anywhere
//    public static void annotateMCLClusters(OutputPrinter out, ONDEXGraph aog, String clusterName, String attributeName, double cutoffLevel) {
//
//        AttributeName att = createAttName(aog, clusterName, Integer.class);
//        AttributeName clustered = aog.getMetaData().getAttributeName(attributeName);
//        Set<ONDEXConcept> unprocessedConcepts = aog.getConcepts();
//        BitSet valid = new BitSet();
//        for(ONDEXRelation r : aog.getRelationsOfAttributeName(clustered)) {
//            Attribute gds = r.getAttribute(att);
//            if (gds != null && ((Double) gds.getValue()) > cutoffLevel) {
//                valid.set(r.getId());
//            }
//        }
//        Set<Integer> takenIds = new HashSet<Integer>();
//        int lastId = 1;
//        while (unprocessedConcepts.size() > 0) {
//            BitSet[] group = StandardFunctions.getAllConnected(unprocessedConcepts.next(),
//                    aog,
//                    SetImpl.create(aog, ONDEXRelation.class, valid));
//            Set<ONDEXConcept> concGroup = SetImpl.create(aog, ONDEXConcept.class, group[0]);
//            unprocessedConcepts = BitSetFunctions.andNot(unprocessedConcepts, concGroup);
//            SortedMap<Integer, Object> prevalence = StandardFunctions.gdsRanking(aog, concGroup, att.getId());
//            int groupID = 1;
//            if (prevalence.size() != 0)
//                groupID = Integer.valueOf(prevalence.get(prevalence.lastKey()).toString());
//            if (takenIds.contains(groupID)) {
//                groupID = lastId + 1;
//                while (takenIds.contains(groupID))
//                    groupID++;
//                lastId = groupID;
//            }
//            takenIds.add(groupID);
//
//            for (Integer id : group[0]) {
//                ONDEXConcept c = aog.getConcept(id);
//                if (c.getAttribute(att) != null) {
//                    c.deleteAttribute(att);
//                }
//                c.createAttribute(att, groupID, false);
//            }
//        }
//        for(ONDEXRelation r : aog.getRelations()) {
//            Integer groupId = (Integer) r.getFromConcept().getAttribute(att).getValue();
//            if (groupId.equals(r.getToConcept().getAttribute(att).getValue())) {
//                r.createAttribute(att, groupId, false);
//            }
//        }
//    }

    public static void runMCL(final OutputPrinter outPtr, ONDEXGraph graph, String path, String attributeName, String clusterName, double inflation) {
        Boolean isUndirected = true;
        AttributeName an = graph.getMetaData().getAttributeName(attributeName);
        AttributeName clustAtt = createAttName(graph, clusterName, Integer.class);

        for (ONDEXConcept c: graph.getConceptsOfAttributeName(clustAtt))
            c.deleteAttribute(clustAtt);

        for (ONDEXRelation r: graph.getRelationsOfAttributeName(clustAtt))
            r.deleteAttribute(clustAtt);

        String tempFileName = System.getProperty("java.io.tmpdir") + File.separator + "tmp_mcl.tab";
        File temp = new File(tempFileName);
        if (temp.exists())
            temp.delete();
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(tempFileName));
            //DataOutputStream out = new DataOutputStream(new FileOutputStream("tmp_mcl.tab"));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
            for (ONDEXRelation r : graph.getRelationsOfAttributeName(an)) {
                bw.write(r.getFromConcept().getId() + " " + r.getToConcept().getId() + " " + r.getAttribute(an).getValue() + "\n");
                bw.flush();
                if (isUndirected) {
                    bw.write(r.getToConcept().getId() + " " + r.getFromConcept().getId() + " " + r.getAttribute(an).getValue() + "\n");
                    bw.flush();
                }
            }
            out.close();

            File pathF = new File(path);
            path = pathF.getCanonicalPath();
            if (!path.endsWith(File.separator))
                path = path + File.separator;

            try {

                String filePath = temp.getAbsolutePath().replaceAll("/", File.separator);
                Process p = Runtime.getRuntime().exec(path + "mcl " + filePath + " --abc -I " + inflation + " -o " + path + attributeName + "[I" + inflation + "].cls");
                outPtr.print("\n");
                OutputStream console = new OutputStream() {
                    public void write(int b) throws IOException {
                        outPtr.print((char) b);
                    }
                };

                StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), console);
                StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), console);


                errorGobbler.start();
                outputGobbler.start();

                p.waitFor();

                String output = path + attributeName + "[I" + inflation + "].cls";
                DataInputStream in = new DataInputStream(new FileInputStream(output));
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                int clusterId = 1;
                while ((strLine = br.readLine()) != null) {
                    if (strLine.equals(""))
                        continue;
                    String[] ids = strLine.split("\t");
                    BitSet set = new BitSet(ids.length);
                    for (String id : ids) {
                        int i = Integer.valueOf(id);
                        set.set(i);
                        ONDEXConcept c = graph.getConcept(i);
                        if (c.getAttribute(clustAtt) != null)
                            c.deleteAttribute(clustAtt);
                        c.createAttribute(clustAtt, clusterId, false);
                    }
                    for(int i=set.nextSetBit(0); i>=0; i=set.nextSetBit(i+1)) {
                        ONDEXConcept one = graph.getConcept(i);
                        for (ONDEXRelation r : graph.getRelationsOfConcept(one)) {
                            Attribute attribute = getOtherNode(one, r).getAttribute(clustAtt);
                            if (attribute != null && attribute.getValue().equals(clusterId)) {
                                if (r.getAttribute(clustAtt) != null)
                                    r.deleteAttribute(clustAtt);
                                r.createAttribute(clustAtt, clusterId, false);
                            }
                        }
                    }
                    clusterId++;
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void generalAnnoationStatistics(ONDEXGraph graph, String sourceConceptClass, String fileName) {
        ConceptClass cs = graph.getMetaData().getConceptClass(sourceConceptClass);
        String[] goClasses = new String[]{"MolFunc", "BioProc", "CelComp"};

        Map<ConceptClass, FrequencyMapBuilder<String>> freqs = new HashMap<ConceptClass, FrequencyMapBuilder<String>>();
        for (String s : goClasses)
            freqs.put(graph.getMetaData().getConceptClass(s), new FrequencyMapBuilder<String>(new HashMap<String, Integer>()));
        Set<ConceptClass> validClasses = freqs.keySet();
        Map<String, String> nameToId = new HashMap<String, String>();
        for (ONDEXConcept c : graph.getConceptsOfConceptClass(cs)) {
            for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
                ONDEXConcept go = StandardFunctions.getOtherNode(c, r);
                if (validClasses.contains(go.getOfType())) {
                    freqs.get(go.getOfType()).addEntry(go.getConceptName().getName());
                    boolean found = false;
                    for (ConceptAccession ca : go.getConceptAccessions()) {
                        String acc = ca.getAccession();
                        if (acc.startsWith("GO")) {
                            nameToId.put(go.getConceptName().getName(), acc);
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        nameToId.put(go.getConceptName().getName(), "N/A");
                }
            }
        }

        for (String ontoType : goClasses) {
            Map<Integer, List<String>> counts = freqs.get(graph.getMetaData().getConceptClass(ontoType)).getReverseMap();
            try {
                DataOutputStream out = new DataOutputStream(new FileOutputStream(ontoType + "_" + fileName));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
                bw.write("GO_ID\tGO_NAME\tOccurances\n");
                bw.flush();
                for (Entry<Integer, List<String>> ent1 : counts.entrySet()) {
                    for (String goName : ent1.getValue()) {
                        bw.write(nameToId.get(goName) + "\t" + goName + "\t" + ent1.getKey() + "\n");
                        bw.flush();
                    }
                }
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static final void generateGoAnnotationOfClusters(ONDEXGraph graph, String clusterName, String fileName, int level) {
        Map<Object, BitSet> clusters = new HashMap<Object, BitSet>();
        AttributeName an = graph.getMetaData().getAttributeName(clusterName);
        String[] goClasses = new String[]{"MolFunc", "BioProc", "CelComp"};
        DataSource dataSourceGo = graph.getMetaData().getDataSource("GO");
        Set<String> roots = new HashSet<String>(Arrays.asList("GO:0008150", "GO:0005575", "GO:0003674"));
        Map<ConceptClass, FrequencyMapBuilder<String>> freqs = new HashMap<ConceptClass, FrequencyMapBuilder<String>>();
        for (String s : goClasses)
            freqs.put(graph.getMetaData().getConceptClass(s), new FrequencyMapBuilder<String>(new HashMap<String, Integer>()));
        ConceptClass[] goCCarray = freqs.keySet().toArray(new ConceptClass[3]);
        Set<ConceptClass> validClasses = freqs.keySet();
        for (ONDEXConcept c: graph.getConceptsOfAttributeName(an)) {
            BitSet set = clusters.get(c.getAttribute(an).getValue());
            if (set == null) {
                set = new BitSet();
                clusters.put(c.getAttribute(an).getValue(), set);
            }
            set.set(c.getId());
        }
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);

        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
            bw.write("Cluster id\tCluster size");
            for (String s : goClasses)
                bw.write("\t" + s);
            bw.write("\n");
            bw.flush();
            for (Entry<Object, BitSet> cluster : clusters.entrySet()) {
                bw.write(cluster.getKey().toString() + "\t" + cluster.getValue().cardinality());
                bw.flush();
                int noAnnoationCounter = 0;
                BitSet bs = cluster.getValue();
                for(int i=bs.nextSetBit(0); i>=0; i=bs.nextSetBit(i+1)) {
                    ONDEXConcept c = graph.getConcept(i);
                    boolean noAnnoation = true;
                    Set<String> uniqueAnnotationChecker = new HashSet<String>();
                    for(ONDEXRelation r : graph.getRelationsOfConcept(c)) {
                        ONDEXConcept go = StandardFunctions.getOtherNode(c, r);
                        if (validClasses.contains(go.getOfType()) && !hasMatch(roots, getAccessionsOfType(go, dataSourceGo))) {
                            FrequencyMapBuilder<String> typeMap = freqs.get(go.getOfType());
                            if (uniqueAnnotationChecker.contains(go.getConceptName().getName()))
                                continue;
                            uniqueAnnotationChecker.add(go.getConceptName().getName());
                            typeMap.addEntry(go.getConceptName().getName());
                            int levelCounter = level;
                            while (levelCounter > 0) {
                                Set<ONDEXConcept> parents = relationsToTargets(getOutgoingRelationsToConceptClass(graph, go, goCCarray));
                                for (ONDEXConcept goParent : parents) {
                                    if (!hasMatch(roots, getAccessionsOfType(goParent, dataSourceGo)))
                                        if (uniqueAnnotationChecker.contains(goParent.getConceptName().getName()))
                                            continue;
                                    uniqueAnnotationChecker.add(goParent.getConceptName().getName());
                                    typeMap.addEntry(goParent.getConceptName().getName());
                                }
                                levelCounter--;
                            }
                            noAnnoation = false;
                        }
                    }
                    if (noAnnoation)
                        noAnnoationCounter++;
                }
                for (String s : goClasses) {
                    SortedMap<Integer, List<String>> annotation = new TreeMap<Integer, List<String>>(new LargestFirstNumberComparator());
                    annotation.putAll(freqs.get(graph.getMetaData().getConceptClass(s)).getReverseMap());
                    int j = 1;
                    int numHighestEntries = 3;
                    StringBuffer output = new StringBuffer();
                    for (Entry<Integer, List<String>> ent1 : annotation.entrySet()) {
                        for (String goName : ent1.getValue()) {
                            if (j > 1)
                                output.append(", ");
                            output.append(goName);
                            output.append("(");
                            output.append(nf.format(Double.valueOf(ent1.getKey()) / Double.valueOf(cluster.getValue().cardinality() - noAnnoationCounter) * 100d));
                            output.append(")");
                            j++;
                            if (j > numHighestEntries)
                                break;
                        }
                        if (j > numHighestEntries)
                            break;
                    }
                    bw.write("\t" + output.toString());
                    bw.flush();
                    freqs.get(graph.getMetaData().getConceptClass(s)).clear();
                }
                bw.write("\n");
                bw.flush();
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class LargestFirstNumberComparator implements Comparator<Integer> {
        public int compare(Integer o1, Integer o2) {
            if (o1.equals(o2))
                return 0;
            return o1 < o2 ? 1 : -1;
        }
    }
}
