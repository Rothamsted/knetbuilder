package net.sourceforge.ondex.parser.fourdim;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.FloatRangeArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.exception.type.ParsingFailedException;
import net.sourceforge.ondex.parser.ONDEXParser;

public class Parser extends ONDEXParser
{

    public static final boolean DEBUG = true;
    public static final String COORDFILE_ARG = "CoordinateFile";
    public static final String COORDFILE_ARG_DESC = "The file containing 2D coordinates for " +
            "the genes (usually from PCA)";
    public static final String NAMEFILE_ARG = "NameFile";
    public static final String NAMEFILE_ARG_DESC = "The file containing names for " +
            "the genes (from SGD)";
    public static final String NORM_CUTOFF_ARG = "NormCutoff";
    public static final String NORM_CUTOFF_ARG_DESC = "Cutoff value for the genes by norm. " +
            "(All genes closer to the origin than " +
            "this value will be disregarded)";
    public static final String DIST_CUTOFF_ARG = "DistanceCutoff";
    public static final String DIST_CUTOFF_ARG_DESC = "Cutoff value for relations by distance. " +
            "No relation will be created between two genes " +
            "if their distance is greater than this value.";
    private ConceptClass ccGene;
    private RelationType rt;
    private DataSource dataSource;
    private EvidenceType et;
    private AttributeName atDist, atX, atY, atVis;

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                        FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false),
                new FileArgumentDefinition(COORDFILE_ARG, COORDFILE_ARG_DESC, true, true, false),
                new FileArgumentDefinition(NAMEFILE_ARG, NAMEFILE_ARG_DESC, true, true, false),
                new FloatRangeArgumentDefinition(NORM_CUTOFF_ARG, NORM_CUTOFF_ARG_DESC, true, 0.5f, 0.0f, Float.MAX_VALUE),
                new FloatRangeArgumentDefinition(DIST_CUTOFF_ARG, DIST_CUTOFF_ARG_DESC, true, 1.0f, Float.MIN_VALUE, Float.MAX_VALUE)
        };
    }

    @Override
    public String getName() {
        return "IAH 4D data analyzer";
    }

    @Override
    public String getVersion() {
        return "30.06.2009";
    }

    @Override
    public String getId() {
        return "fourdim";
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    private void fetchMetaData() throws MetaDataMissingException {
        ccGene = requireConceptClass("Gene");
        rt = requireRelationType("r");
        dataSource = requireDataSource("IAH");
        et = requireEvidenceType("IMPD");
        atDist = graph.getMetaData().getFactory().createAttributeName("distance", Double.class);
        atX = graph.getMetaData().getAttributeName("graphicalX");
        if (atX == null) {
            atX = graph.getMetaData().getFactory().createAttributeName("graphicalX", Double.class);
        }

        atY = graph.getMetaData().getAttributeName("graphicalY");
        if (atY == null)
            atY = graph.getMetaData().getFactory().createAttributeName("graphicalY", Double.class);

        atVis = graph.getMetaData().getAttributeName("visible");
        if (atVis == null)
            atVis = graph.getMetaData().getFactory().createAttributeName("visible", Boolean.class);
    }

    @Override
    public void start() throws Exception {
        double normCutoff = (Float) args.getUniqueValue(NORM_CUTOFF_ARG);

        ArrayList<MultiVector> vectors = new ArrayList<MultiVector>();

        String fileStr = (String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE);
        BufferedReader br = new BufferedReader(new FileReader(fileStr));
        String line;
        int lineCount = 0;
        while ((line = br.readLine()) != null) {
            lineCount++;
            if (lineCount == 1) {
                continue;//skip first line
            }
            String[] cols = line.split("\t");
            try {
                MultiVector vec = MultiVector.parse(cols, 1, 5, 0);
                vectors.add(vec);
            } catch (NumberFormatException nfe) {
                throw new ParsingFailedException("invalid line: " + lineCount);
            }
        }
        br.close();

//		//create norm histogram
//		System.out.println("Norms:");
//		double[] norms = computeNorms(vectors);
//		histogram(norms);

        //cutoff by norm value
        cutoffByNorms(vectors, normCutoff);

        //register gene names for indices
       Map<String, Integer> name2id = new HashMap<String, Integer>();
        for (int i = 0; i < vectors.size(); i++) {
            MultiVector vec = vectors.get(i);
            name2id.put(vec.getName(), i);
        }

        //compute distance matrix
        double[][] distances = computeDistanceMatrix(vectors);


        if (graph != null) {
            createGraph(name2id, distances);
        }
    }

    private void createGraph(Map<String, Integer> name2id,
                             double[][] distances) throws MetaDataMissingException, IOException, InvalidPluginArgumentException {

        fetchMetaData();

        double distCutoff = (Float) args.getUniqueValue(DIST_CUTOFF_ARG);

        String coordFile = (String) args.getUniqueValue(COORDFILE_ARG);
        HashMap<String, Point2D.Double> pcaCoord = readCoordinates(coordFile);

        String nameFile = (String) args.getUniqueValue(NAMEFILE_ARG);
        HashMap<String, String> names = readNames(nameFile);

        HashMap<String, ONDEXConcept> concepts = new HashMap<String, ONDEXConcept>();
        for (String orf_i : name2id.keySet()) {
            if (orf_i == null) {
                continue;
            }
            int intI = name2id.get(orf_i);
            for (String orf_j : name2id.keySet()) {
                if (orf_j == null) {
                    continue;
                }
                if (orf_i.equals(orf_j)) {
                    continue;
                }
                int intJ = name2id.get(orf_j);
                double distance = distances[intI][intJ];
                if (distance < distCutoff) {
                    ONDEXConcept c_i = getConcept(concepts, orf_i, pcaCoord, names);
                    ONDEXConcept c_j = getConcept(concepts, orf_j, pcaCoord, names);
                    ONDEXRelation r = graph.getFactory().createRelation(c_i, c_j, rt, et);
                    r.createAttribute(atDist, distance, false);
                }
            }
        }
    }

//	private double[] computeNorms(ArrayList<MultiVector> vectors) {
//		double[] norms = new double[vectors.size()];
//		for (int i = 0; i < vectors.size(); i++) {
//			MultiVector v = vectors.get(i);
//			double n = v.euclidNorm();
//			norms[i] = n;
//		}
//		return norms;
//	}

    private ONDEXConcept getConcept(HashMap<String, ONDEXConcept> concepts,
                                    String orf, HashMap<String, Point2D.Double> pcaCoord,
                                    HashMap<String, String> names) {
        ONDEXConcept c = concepts.get(orf);
        if (c == null) {
            c = graph.getFactory().createConcept(orf, dataSource, ccGene, et);
            if (names.get(orf) != null) {
                c.createConceptName(names.get(orf), true);
            }
            if (pcaCoord.get(orf) != null) {
                Point2D.Double point = pcaCoord.get(orf);
                c.createAttribute(atX, point.x, false);
                c.createAttribute(atY, point.y, false);
                c.createAttribute(atVis, Boolean.TRUE, false);
            } else {
                c.createAttribute(atX, 0.0, false);
                c.createAttribute(atY, 0.0, false);
                c.createAttribute(atVis, Boolean.FALSE, false);
            }
            concepts.put(orf, c);
        }
        return c;
    }

    private HashMap<String, String> readNames(String nameFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(nameFile));
        HashMap<String, String> names = new HashMap<String, String>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] cols = line.split("\t");
            if (cols.length >= 6) {
                if (cols[0] != null && !cols[0].equals("")
                        && cols[5] != null && !cols[5].equals("")) {
                    names.put(cols[5], cols[0]);
                }
            }
        }
        br.close();
        return names;
    }

    private HashMap<String, Point2D.Double> readCoordinates(String coordFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(coordFile));
        HashMap<String, Point2D.Double> coords = new HashMap<String, Point2D.Double>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] cols = line.split("\t");
            if (cols.length >= 3) {
                try {
                    double x = Double.parseDouble(cols[1]);
                    double y = Double.parseDouble(cols[2]);
                    Point2D.Double point = new Point2D.Double(x, y);
                    coords.put(cols[0], point);
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        br.close();
        return coords;
    }

    /**
     *
     */
    private void cutoffByNorms(ArrayList<MultiVector> vectors, double cutoff) {
        ArrayList<MultiVector> toRemove = new ArrayList<MultiVector>();
        for (int i = 0; i < vectors.size(); i++) {
            MultiVector v = vectors.get(i);
            if (v.euclidNorm() < cutoff) {
                toRemove.add(v);
            }
        }
        vectors.removeAll(toRemove);
    }

    private double[][] computeDistanceMatrix(ArrayList<MultiVector> vectors) {
        //i know i only need half a matrix, but i'm lazy and i've got plenty of memory :D
        double[][] distances = new double[vectors.size()][vectors.size()];
        //also i want a histogram, so i collect the values:
        List<Double> histoVals = new ArrayList<Double>();
        for (int i = 0; i < vectors.size(); i++) {
            MultiVector vi = vectors.get(i);
            for (int j = 0; j < i; j++) {
                MultiVector vj = vectors.get(j);
                double d = vi.euclidDistance(vj);
                histoVals.add(d);
                if (DEBUG) {
                    assert (d == vj.euclidDistance(vi));
                }
                distances[i][j] = d;
                distances[j][i] = d;
            }
        }
        System.out.println("\nDistances:");
        histogram(histoVals.toArray(new Double[histoVals.size()]));
        return distances;
    }

    private void histogram(Double[] vals) {
        //stretch value for histogram resolution
        int blowup = 50;
        double blowupD = (double) blowup;

        //determine maximum
        double max = Double.NEGATIVE_INFINITY;
        for (double val : vals) {
            if (val > max) {
                max = val;
            }
        }

        //init histogram array
        int l = (int) Math.ceil(max);
        int[] histo = new int[l * blowup];

        //fill histogram
        for (double val : vals) {
            int target = (int) (val * blowupD);
            histo[target]++;
        }

        //printout histogram
        for (int i = 0; i < histo.length; i++) {
            double val = ((double) i) / blowupD;
            System.out.println(val + "\t" + histo[i]);
        }
    }

    public static void main(String args[]) {
        try {
            Parser p = new Parser();
            ONDEXPluginArguments pargs = new ONDEXPluginArguments(p.getArgumentDefinitions());
            pargs.addOption(FileArgumentDefinition.INPUT_FILE, args[0]);
            pargs.addOption(COORDFILE_ARG, args[1]);
            pargs.addOption(NORM_CUTOFF_ARG, Float.parseFloat(args[2]));
            pargs.addOption(DIST_CUTOFF_ARG, Float.parseFloat(args[3]));
            p.setArguments(pargs);

            p.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
