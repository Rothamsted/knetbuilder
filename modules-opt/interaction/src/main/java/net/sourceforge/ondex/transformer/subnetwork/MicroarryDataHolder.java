package net.sourceforge.ondex.transformer.subnetwork;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author lysenkoa
 */
public class MicroarryDataHolder {
    private final String prefix;
    private final String postfix;
    private SwappableSet set;
    private SwappableSet temp_set;
    private final ONDEXGraph graph;
    private final ONDEXGraphMetaData md;
    private final Random rnd = new Random(System.currentTimeMillis());
    private boolean isGeneVectorPermuted = false;
    private int[] permutedVectorAssignmets = null;
    private boolean isClassesPermuted = false;
    private Map<String, Object> randomised = null;

    private class SwappableSet {
        public final Map<Integer, GeneDataItem> idToData = new HashMap<Integer, GeneDataItem>();
        public double[][] data;
        public double[][] zdata;
        public String[][] treatmentIds;
        public Object[][] groupTags;
        public Integer[][][] markup;
        public int dataRowCounter = 0;
        public int groupTagRowCounter = 0;
        public int treatmentIdsRowCounter = 0;
        public int markupIdsRowCounter = 0;

        public SwappableSet(int size) {
            this.data = new double[size][];
            this.treatmentIds = new String[size][];
            this.groupTags = new Object[size][];
        }

        public void finalize() {
            this.data = Arrays.copyOfRange(this.data, 0, this.dataRowCounter);
            this.treatmentIds = Arrays.copyOfRange(this.treatmentIds, 0, this.treatmentIdsRowCounter);
            this.groupTags = Arrays.copyOfRange(this.groupTags, 0, this.groupTagRowCounter);
            this.markup = Arrays.copyOfRange(this.markup, 0, this.markupIdsRowCounter);
            System.err.println("Loaded microarray data report: ");
            System.err.println("\tGenes in array: " + this.dataRowCounter);
            System.err.println("\tUnique treatment sets: " + this.treatmentIdsRowCounter);
            System.err.println("\tUnique treatment groups: " + this.groupTagRowCounter);
        }
    }


    public MicroarryDataHolder(String prefix, String postfix, ONDEXGraph graph, int n) {
        this.postfix = postfix;
        this.prefix = prefix;
        this.graph = graph;
        this.md = graph.getMetaData();
        this.set = new SwappableSet(n);
    }

    /**
     * Sets the permutation of gene vectors to true in order to generate null p1 distribution
     *
     * @param isPermutedMode
     */
    public void setGeneVectorPermutedMode(boolean isPermutedMode) {
        if (!isGeneVectorPermuted && isPermutedMode) {
            permutedVectorAssignmets = new int[set.data.length];
            for (int i = 0; i < set.data.length; i++) {
                permutedVectorAssignmets[i] = i;
                shuffle(permutedVectorAssignmets, rnd);
            }
        } else if (!isPermutedMode) {
            permutedVectorAssignmets = null;
        }
        this.isGeneVectorPermuted = isPermutedMode;
    }

    public boolean getGeneVectorPermutedMode() {
        return isGeneVectorPermuted;
    }

    public void setClassesPermutedMode(boolean permuteClasses) throws Exception {
        if (!isClassesPermuted && permuteClasses) {
            temp_set = set;
            Map<Object, Double> treatmentsPerGroup = new HashMap<Object, Double>();
            for (Object[] os : set.groupTags) {
                for (Object o : os) {
                    treatmentsPerGroup.put(o, 0.0d);
                }
            }
            Set<String> uniqueTreatmentNames = new HashSet<String>();
            for (String[] os : set.treatmentIds) {
                for (String s : os) {
                    uniqueTreatmentNames.add(s);
                }
            }
            List<String> treatments = new ArrayList<String>(uniqueTreatmentNames);
            double n = ((double) set.idToData.size());
            for (GeneDataItem gdi : set.idToData.values()) {
                for (Entry<Object, Double> countsForGene : gdi.getGroupMemberCount().entrySet()) {
                    Double current = treatmentsPerGroup.get(countsForGene.getKey());
                    countsForGene.setValue(current + countsForGene.getValue() / n);
                }
            }
            double sum = 0.0d;
            for (Entry<Object, Double> ent : treatmentsPerGroup.entrySet()) {
                ent.setValue(Math.floor(ent.getValue() + 0.5d));
                sum = sum + ent.getValue();
            }
            if (sum > treatments.size()) {
                int[] remove = new int[((int) sum) - treatments.size()];
                fillWithRandom(remove, 0, treatmentsPerGroup.size(), rnd);
                int j = 0;
                int a = 0;
                for (Entry<Object, Double> ent : treatmentsPerGroup.entrySet()) {
                    if (remove[a] == j) {
                        ent.setValue(ent.getValue() - 1d);
                        a++;
                    }
                    j++;
                }
            } else if (sum < treatments.size()) {
                int[] extra = new int[treatments.size() - ((int) sum)];
                fillWithRandom(extra, 0, treatmentsPerGroup.size(), rnd);
                int j = 0;
                int a = 0;
                for (Entry<Object, Double> ent : treatmentsPerGroup.entrySet()) {
                    if (extra[a] == j) {
                        ent.setValue(ent.getValue() + 1d);
                        a++;
                    }
                    j++;
                }
            }
            randomised = new HashMap<String, Object>();
            int[] newAssignments = new int[treatments.size()];
            fillWithRandom(newAssignments, 0, treatments.size(), rnd);
            double pos = 0.0;
            for (Entry<Object, Double> ent : treatmentsPerGroup.entrySet()) {
                for (double i = pos; i < pos + ent.getValue(); i++) {
                    randomised.put(treatments.get(newAssignments[(int) pos]), ent.getKey());
                }
                pos = pos + ent.getValue();
            }
            this.rebuild();
        } else if (!permuteClasses) {
            set = temp_set;
            temp_set = null;
        }
        isClassesPermuted = permuteClasses;
    }

    public boolean isClassesPermutedMode() {
        return isClassesPermuted;
    }

    public void finalise() throws Exception {
        set.finalize();
    }

    public void processConcept(ONDEXConcept concept) throws Exception {
        new GeneDataItem(concept);
    }

    public synchronized void normalizeToZ() {
        set.zdata = new double[set.data.length][];
        for (int i = 0; i < set.data.length; i++) {
            set.zdata[i] = convetertToZScore(set.data[i]);
        }
    }

    public List<GeneDataItem> getValues(final Integer[] ids) {
        final List<GeneDataItem> readers = new LinkedList<GeneDataItem>();
        for (Integer id : ids) {
            readers.add(set.idToData.get(id));
        }
        return readers;
    }

    public Map<Object, double[]> getActivities(Integer[] ids) throws Exception {
        final List<GeneDataItem> subset = getValues(ids);
        final GeneDataItem one = subset.get(0);
        final double n = ((double) ids.length);
        final double sqrtn = Math.sqrt(n);
        final double[] activities = new double[one.getData().length];
        for (int i = 0; i < subset.size(); i++) {
            final GeneDataItem currentRecord = subset.get(i);
            if (!one.isComparableRecord(currentRecord))
                throw new Exception("Missing treatment records are not supported at the moment!");
            final double[] data = currentRecord.getZData();
            for (int j = 0; j < data.length; j++) {
                activities[j] = activities[j] + data[j] / sqrtn;
            }
        }
        return splitByGroup(activities, one.getGroups(), one.getMarkup());
    }

    private Map<Object, int[]> getDiscrtisedActivities(final Integer[] ids) throws Exception {
        final List<GeneDataItem> subset = getValues(ids);
        final GeneDataItem one = subset.get(0);
        final double n = ((double) ids.length);
        final double sqrtn = Math.sqrt(n);
        final double[] activities = new double[one.getData().length];
        for (int i = 0; i < subset.size(); i++) {
            final GeneDataItem currentRecord = subset.get(i);
            if (!one.isComparableRecord(currentRecord))
                throw new Exception("Missing treatment records are not supported at the moment!");
            final double[] data = currentRecord.getZData();
            for (int j = 0; j < data.length; j++) {
                activities[j] = activities[j] + data[j] / sqrtn;
            }
        }
        final int[] dValues = discretise(activities);
        return splitByGroup(dValues, one.getGroups(), one.getMarkup());
    }

    public double getMIForSubnetwork(final Integer[] ids) throws Exception {
        return MI(getDiscrtisedActivities(ids));
    }

    private static Map<Object, double[]> splitByGroup(final double[] data, final Object[] groups, final Integer[][] markups) {
        final Map<Object, double[]> result = new HashMap<Object, double[]>();
        for (int i = 0; i < groups.length; i++) {
            result.put(groups[i], Arrays.copyOfRange(data, markups[i][0], markups[i][1]));
        }
        return result;
    }

    private static Map<Object, int[]> splitByGroup(final int[] data, final Object[] groups, final Integer[][] markups) {
        final Map<Object, int[]> result = new HashMap<Object, int[]>();
        for (int i = 0; i < groups.length; i++) {
            result.put(groups[i], Arrays.copyOfRange(data, markups[i][0], markups[i][1]));
        }
        return result;
    }

    private static double MI(final Map<Object, int[]> zByCategory) throws Exception {
        final Map<Integer, double[]> counts = new HashMap<Integer, double[]>();
        final double[] groupTotal = new double[zByCategory.size()];
        int group = 0;
        double total = 0.0d;
        final int occurnencesLength = zByCategory.size() + 1;
        final int occurnencesLastElement = zByCategory.size();
        for (Entry<Object, int[]> ent : zByCategory.entrySet()) {
            total = total + ((double) ent.getValue().length);
            for (int v = 0; v < ent.getValue().length; v++) {
                double[] occurnences = counts.get(ent.getValue()[v]);
                if (occurnences == null) {
                    occurnences = new double[occurnencesLength];
                    Arrays.fill(occurnences, 0.0d);
                }
                occurnences[group] = occurnences[group] + 1d;
                occurnences[occurnencesLastElement] = occurnences[occurnencesLastElement] + 1d;
                groupTotal[group] = groupTotal[group] + 1d;
            }
            group++;
        }
        for (int i = 0; i < groupTotal.length; i++) {
            groupTotal[i] = groupTotal[i] / total;
        }
        final double[][] perOccurence = new double[counts.values().size()][];
        int c = 0;
        for (double[] col : counts.values()) {
            for (int i = 0; i < col.length; i++) {
                col[i] = col[i] / total;
            }
            perOccurence[c] = col;
            c++;
        }
        counts.clear();
        double result = 0.0d;
        final int lastElement = perOccurence[0].length - 1;
        for (int i = 0; i < perOccurence.length; i++) {
            for (int j = 0; j < lastElement; j++) {
                result = result + perOccurence[i][j] * Math.log10(perOccurence[i][j] / (groupTotal[j] * perOccurence[i][lastElement]));
            }
        }
        if (result > 1 || result < 0)
            throw new Exception("Bad MI value produced: " + result + " Verify that your input is valid");
        return result;
    }

    private void rebuild() throws Exception {
        for (Integer id : temp_set.idToData.keySet()) {
            processConcept(graph.getConcept(id));
        }
    }

    private class GeneDataItem {
        private final int row;
        private int treatmentRow;
        private int classesRow;
        private int markUpRow;

        public Object[] getGroups() {
            return set.groupTags[classesRow];
        }

        public String[] getTreatments() {
            return set.treatmentIds[treatmentRow];
        }

        public Integer[][] getMarkup() {
            return set.markup[markUpRow];
        }

        public double[] getData() {
            return set.data[row];
        }

        public double[] getZData() {
            if (set.zdata[row] == null)
                normalizeToZ();
            return set.zdata[row];
        }

        public Map<Object, Double> getGroupMemberCount() {
            Map<Object, Double> result = new HashMap<Object, Double>();
            Object[] groups = getGroups();
            Integer[][] markup = getMarkup();
            for (int i = 0; i < markup.length; i++) {
                result.put(groups[i], (double) markup[i].length);
            }
            return result;
        }

        public boolean isComparableRecord(GeneDataItem gdi) {
            if (this.row == gdi.row &&
                    this.treatmentRow == gdi.treatmentRow &&
                    this.classesRow == gdi.classesRow &&
                    this.markUpRow == gdi.markUpRow) {
                return true;
            }
            return false;
        }

        public GeneDataItem(ONDEXConcept concept) throws Exception {
            this.row = set.dataRowCounter;
            set.idToData.put(concept.getId(), this);
            SortedMap<Object, SortedMap<String, Double>> map = new TreeMap<Object, SortedMap<String, Double>>();
            for (Attribute valueAttribute : concept.getAttributes()) {
                String name = valueAttribute.getOfType().getId();
                if (name.startsWith(prefix)) {
                    if (!valueAttribute.getOfType().getId().endsWith(postfix)) {
                        Object cls = null;
                        if (isClassesPermuted) {
                            cls = randomised.get(name);
                        } else {
                            try {
                                cls = concept.getAttribute(md.getAttributeName(name + postfix)).getValue();
                            }
                            catch (NullPointerException e) {
                                throw new Exception("Wrong values supplied to transformer: each value must have a corresponding \n marker gds that has the same attribute name + marker gds ending. Concept with problem: " + concept.getId());
                            }
                            if (cls == null) {
                                throw new Exception("Marker on concept " + concept.getId() + "is null, which is not allowed.");
                            }
                        }

                        SortedMap<String, Double> treatmentdata = map.get(cls);
                        if (treatmentdata == null) {
                            treatmentdata = new TreeMap<String, Double>();
                            map.put(cls, treatmentdata);
                        }
                        treatmentdata.put(valueAttribute.getOfType().getId(), (Double) valueAttribute.getValue());
                    }
                }
            }

            int len = 0;
            for (Entry<Object, SortedMap<String, Double>> treatmentGroups : map.entrySet()) {
                len = len + treatmentGroups.getValue().size();
            }
            Integer[][] markupTemp = new Integer[map.size()][];
            double[] values = new double[len];
            String[] treatmetnTags = new String[len];
            int start = 0;
            int g = 0;
            int d = 0;
            Object[] groupTagsTemp = map.keySet().toArray();
            for (Entry<Object, SortedMap<String, Double>> treatmentGroups : map.entrySet()) {
                markupTemp[g] = new Integer[]{start, start + treatmentGroups.getValue().size() - 1};
                start = treatmentGroups.getValue().size();
                for (Entry<String, Double> treatmentData : treatmentGroups.getValue().entrySet()) {
                    treatmetnTags[d] = treatmentData.getKey();
                    values[d] = treatmentData.getValue();
                    d++;
                }
                g++;
            }
            set.data[row] = values;
            if (set.treatmentIds[set.treatmentIdsRowCounter] == null) {
                set.treatmentIds[set.treatmentIdsRowCounter] = treatmetnTags;
            } else {
                if (!Arrays.equals(set.treatmentIds[set.treatmentIdsRowCounter], treatmetnTags)) {
                    boolean matchFound = false;
                    for (int i = 0; i < set.treatmentIdsRowCounter; i++) {
                        if (Arrays.equals(set.treatmentIds[set.treatmentIdsRowCounter], treatmetnTags)) {
                            matchFound = true;
                            this.treatmentRow = i;
                        }
                    }
                    if (!matchFound) {
                        set.treatmentIdsRowCounter++;
                        set.treatmentIds[set.treatmentIdsRowCounter] = treatmetnTags;
                        this.treatmentRow = set.treatmentIdsRowCounter;
                    }
                } else {
                    this.treatmentRow = set.treatmentIdsRowCounter;
                }
            }


            if (set.groupTags[set.groupTagRowCounter] == null) {
                set.groupTags[set.groupTagRowCounter] = groupTagsTemp;
            } else {
                if (!Arrays.equals(set.groupTags[set.groupTagRowCounter], groupTagsTemp)) {
                    boolean matchFound = false;
                    for (int i = 0; i < set.groupTagRowCounter; i++) {
                        if (Arrays.equals(set.groupTags[set.groupTagRowCounter], groupTagsTemp)) {
                            matchFound = true;
                            this.classesRow = i;
                        }
                    }
                    if (!matchFound) {
                        set.groupTagRowCounter++;
                        set.groupTags[set.groupTagRowCounter] = groupTagsTemp;
                        this.classesRow = set.groupTagRowCounter;
                    }
                } else {
                    this.classesRow = set.groupTagRowCounter;
                }
            }


            if (set.markup[set.markupIdsRowCounter] == null) {
                set.markup[set.markupIdsRowCounter] = markupTemp;
            } else {
                if (!Arrays.deepEquals(set.markup[set.markupIdsRowCounter], markupTemp)) {
                    boolean matchFound = false;
                    for (int i = 0; i < set.markupIdsRowCounter; i++) {
                        if (Arrays.deepEquals(set.markup[set.markupIdsRowCounter], markupTemp)) {
                            matchFound = true;
                            this.markUpRow = i;
                        }
                    }
                    if (!matchFound) {
                        set.markupIdsRowCounter++;
                        set.markup[set.markupIdsRowCounter] = markupTemp;
                        this.markUpRow = set.markupIdsRowCounter;
                    }
                } else {
                    this.markUpRow = set.markupIdsRowCounter;
                }
            }

            set.dataRowCounter++;
        }
    }

    private static double[] convetertToZScore(final double[]... dataLists) {
        int size = 0;
        for (double[] list : dataLists) {
            size = size + list.length;
        }
        final double[] result = new double[size];
        final double mean = getMean(dataLists);
        final double std = getStd(mean, dataLists);
        int j = 0;
        for (double[] data : dataLists) {
            for (double value : data) {
                result[j] = ((mean - value) / std);
                j++;
            }
        }
        return result;
    }

    private static int[] discretise(final double[] row) {
        final int[] result = new int[row.length];
        int bin = 1;
        final double[] bins = getBins(row);
        for (Entry<Double, Integer> ent : getSortedMapToOriginalOrder(row).entrySet()) {
            boolean binned = false;
            while (!binned) {
                if (ent.getKey() <= bins[bin]) {
                    binned = true;
                    result[ent.getValue()] = bin;
                } else {
                    bin++;
                    if (bin == bins.length) {
                        result[ent.getValue()] = bins.length - 1;
                        binned = true;
                    }
                }
            }
        }
        return result;
    }

    private static SortedMap<Double, Integer> getSortedMapToOriginalOrder(final double[] data) {
        final SortedMap<Double, Integer> sorted = new TreeMap<Double, Integer>(new Comparator<Double>() {
            public int compare(Double o1, Double o2) {
                if (o1 > o2)
                    return 1;
                else if (o1.equals(o2))
                    return 0;
                return -1;
            }
        });
        for (int i = 0; i < data.length; i++) {
            sorted.put(data[i], i);
        }
        return sorted;
    }

    private static double[] getBins(final double[] data) {
        final double nDouble = Math.floor(Math.log(data.length) / Math.log(2.0) + 1.0);
        final int n = (int) nDouble;
        final double[] result = new double[n];
        final double mean = getMean(data);
        final double std = getStd(mean, data);
        double startBin = mean + 2.0 * std;
        final double binWidth = 4.0 * std / (nDouble);
        for (int i = n - 1; i >= 0; i--) {
            result[n] = startBin;
            startBin = startBin - binWidth;
        }
        return result;
    }

    private static double getMean(final double[]... lists) {
        double sum = 0.0;
        double size = 0.0;
        for (double[] row : lists) {
            for (double i : row) {
                sum = sum + i;
            }
            size = size + ((double) row.length);
        }
        return sum / size;
    }

    private static double getStd(final double mean, final double[]... lists) {
        double sum = 0.0;
        double size = 0.0;
        for (double[] row : lists) {
            for (double i : row) {
                double diff = i - mean;
                sum = sum + diff * diff;
                size = size + ((double) row.length);
            }
        }
        return Math.sqrt(sum / size);
    }


    /**
     * @param array
     * @param rnd
     * @author toxiclibs - code snippet from toxi.util.datatypes
     * Rearranges the array items in random order using the given RNG. Operation
     * is in-place, no copy is created.
     */
    private static void shuffle(final int[] array, final Random rnd) {
        final int N = array.length;
        for (int i = 0; i < N; i++) {
            int r = i + (int) (rnd.nextFloat() * (N - i)); // between i and N-1
            int swap = array[i];
            array[i] = array[r];
            array[r] = swap;
        }
    }

    private static void fillWithRandom(final int[] array, final int min, final int max, final Random rnd) {
        final int[] subset = new int[max - min];
        int j = 0;
        for (int i = min; i < max; i++) {
            subset[j] = i;
            j++;
        }
        shuffle(subset, rnd);
        for (int i = 0; i < array.length; i++) {
            array[i] = subset[i];
        }
    }
}
