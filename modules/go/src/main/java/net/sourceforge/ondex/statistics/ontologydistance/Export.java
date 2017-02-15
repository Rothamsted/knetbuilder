package net.sourceforge.ondex.statistics.ontologydistance;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.algorithm.hierarchicalsimilarity.Evaluation;
import net.sourceforge.ondex.algorithm.hierarchicalsimilarity.HierarchicalSimilarity;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.export.ONDEXExport;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;

/**
 * Statistics class to evaluate a set of GO annotations to a "Gold Standard".
 * It uses the hierarchical similarity algorithm to calculate precision/recall
 * for every annotation and then averaging overall annotations
 * <p/>
 * For example to compare text mining based vs. GOA the parameters are:
 * CC = Publication
 * PedictedRT = is_r
 *
 * @author keywan
 */
@Custodians(custodians = {"Keywan Hassani-pak"}, emails = {"keywan at users.sourceforge.net"})
@Status(description = "Set to DISCONTINUED 4 May 2010 due to Writer not being set in Arguements. (Christian)", status = StatusType.DISCONTINUED)
public class Export extends ONDEXExport implements ArgumentNames, MetaDataConstants {

    private StatisticResult result;

    /**
     * Hierarchical evaluation method. For each class
     * MolFunc, BioBroc and CelComp a separate evaluation
     *
     * @throws IOException
     */
    public void start() throws InvalidPluginArgumentException, IOException {
        RelationType rtSet = graph.getMetaData().getRelationType(RT_IS_RELATED);
        if (rtSet == null) {
            RelationType rt = graph.getMetaData().getRelationType(RT_IS_RELATED);
            if (rt == null) {
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new RelationTypeMissingEvent("Missing", RT_IS_RELATED));
            }
            rtSet = graph.getMetaData().getFactory().createRelationType(RT_IS_RELATED, rt);
        }

        String outDir = (String) getArguments().getUniqueValue(STATISTICS_DIR_ARG);
        String ccStr = (String) getArguments().getUniqueValue(CONC_CLASS_ARG);
        String rtsStr = (String) getArguments().getUniqueValue(PR_RT_ARG);


        String dateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileName = outDir + "om_stats[" + dateTime + "].txt";

        FileWriter statsOut = new FileWriter(fileName);

        statsOut.write("#GENERAL ONTOLOGY MAPPING STATISTICS\n");

        ConceptClass ofType = graph.getMetaData().getConceptClass(ccStr);
        if (ofType == null) {
            ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new ConceptClassMissingEvent(ccStr, ""));
            return;
        }
        Set<ONDEXConcept> conIt;
        if (ofType.getId().equals("Publication")) {
            AttributeName abstractAN = graph.getMetaData().getAttributeName("Abstract");
            if (abstractAN == null) {
                AttributeNameMissingEvent so = new AttributeNameMissingEvent("AttributeName Abstract is missing.", "[Statistics]");
                fireEventOccurred(so);
            }
            //only get Publications that contain an abstract
            conIt = graph.getConceptsOfAttributeName(abstractAN);
        } else {
            conIt = graph.getConceptsOfConceptClass(ofType);
        }

        HierarchicalSimilarity hSim = new HierarchicalSimilarity(graph);

        int numF = 0;
        int numP = 0;
        int numC = 0;

        double precisionF = 0;
        double recallF = 0;
        double scoreF = 0;

        double precisionP = 0;
        double recallP = 0;
        double scoreP = 0;

        double precisionC = 0;
        double recallC = 0;
        double scoreC = 0;

        double allPredictionsMadeFE = 0;
        double allTrueCasesInRefSetFE = 0;
        double truePositivesFE = 0;

        double allPredictionsMadePE = 0;
        double allTrueCasesInRefSetPE = 0;
        double truePositivesPE = 0;

        double allPredictionsMadeCE = 0;
        double allTrueCasesInRefSetCE = 0;
        double truePositivesCE = 0;

        Set<ONDEXConcept> autoF = new HashSet<ONDEXConcept>();
        Set<ONDEXConcept> handF = new HashSet<ONDEXConcept>();

        Set<ONDEXConcept> autoP = new HashSet<ONDEXConcept>();
        Set<ONDEXConcept> handP = new HashSet<ONDEXConcept>();

        Set<ONDEXConcept> autoC = new HashSet<ONDEXConcept>();
        Set<ONDEXConcept> handC = new HashSet<ONDEXConcept>();

        for (ONDEXConcept conc : conIt) {
            for (ONDEXRelation rel : graph.getRelationsOfConcept(conc)) {
                if (rel.getOfType().getId().equals(hasFunction)) {
                    handF.add(rel.getToConcept());
                } else if (rel.getOfType().getId().equals(locatedIn)) {
                    handC.add(rel.getToConcept());
                } else if (rel.getOfType().getId().equals(hasParticipant)) {
                    handP.add(rel.getToConcept());
                } else if (rel.getOfType().getId().equals(rtsStr)) {

                    if (rel.getToConcept().getOfType().getId().equals("MolFunc")) {
                        autoF.add(rel.getToConcept());
                    }
                    if (rel.getToConcept().getOfType().getId().equals("BioProc")) {
                        autoP.add(rel.getToConcept());
                    }
                    if (rel.getToConcept().getOfType().getId().equals("CelComp")) {
                        autoC.add(rel.getToConcept());
                    }

                }
            }

            if (handF.size() > 0) {
                numF++;
                if (autoF.size() > 0) {
                    Evaluation statsF = hSim.getEvaluation(handF, autoF);
                    precisionF += statsF.getPrecision();
                    recallF += statsF.getRecall();
                    scoreF += statsF.getScore();

                    allPredictionsMadeFE += statsF.getAllPredictionsMade();
                    allTrueCasesInRefSetFE += statsF.getAllTrueCasesInRefSet();
                    truePositivesFE += statsF.getTruePositives();
                }
            }

            if (handP.size() > 0) {
                numP++;
                if (autoP.size() > 0) {
                    Evaluation statsP = hSim.getEvaluation(handP, autoP);
                    precisionP += statsP.getPrecision();
                    recallP += statsP.getRecall();
                    scoreP += statsP.getScore();

                    allPredictionsMadePE += statsP.getAllPredictionsMade();
                    allTrueCasesInRefSetPE += statsP.getAllTrueCasesInRefSet();
                    truePositivesPE += statsP.getTruePositives();
                }
            }

            if (handC.size() > 0) {
                numC++;
                if (autoC.size() > 0) {
                    Evaluation statsC = hSim.getEvaluation(handC, autoC);
                    precisionC += statsC.getPrecision();
                    recallC += statsC.getRecall();
                    scoreC += statsC.getScore();

                    allPredictionsMadeCE += statsC.getAllPredictionsMade();
                    allTrueCasesInRefSetCE += statsC.getAllTrueCasesInRefSet();
                    truePositivesCE += statsC.getTruePositives();
                }
            }
            autoF.clear();
            handF.clear();
            autoP.clear();
            handP.clear();
            autoC.clear();
            handC.clear();
        }

        precisionF /= numF;
        precisionP /= numP;
        precisionC /= numC;

        recallF /= numF;
        recallP /= numP;
        recallC /= numC;

        scoreF = (2 * precisionF * recallF) / (precisionF + recallF);
        scoreP = (2 * precisionP * recallP) / (precisionP + recallP);
        scoreC = (2 * precisionC * recallC) / (precisionC + recallC);

        double precisionFE = truePositivesFE / allPredictionsMadeFE;
        double precisionPE = truePositivesPE / allPredictionsMadePE;
        double precisionCE = truePositivesCE / allPredictionsMadeCE;

        double recallFE = truePositivesFE / allTrueCasesInRefSetFE;
        double recallPE = truePositivesPE / allTrueCasesInRefSetPE;
        double recallCE = truePositivesCE / allTrueCasesInRefSetCE;

        double scoreFE = 2 * precisionFE * recallFE / (recallFE + precisionFE);
        double scorePE = 2 * precisionPE * recallPE / (recallPE + precisionPE);
        double scoreCE = 2 * precisionCE * recallCE / (recallCE + precisionCE);

        double precisionAll = (precisionF + precisionP + precisionC) / 3;
        double recallAll = (recallF + recallP + recallC) / 3;
        double scoreAll = (2 * precisionAll * recallAll) / (precisionAll + recallAll);

        double precisionAllE = (truePositivesFE + truePositivesPE + truePositivesCE) / (allPredictionsMadeFE + allPredictionsMadePE + allPredictionsMadeCE);
        double recallAllE = (truePositivesFE + truePositivesPE + truePositivesCE) / (allTrueCasesInRefSetFE + allTrueCasesInRefSetPE + allTrueCasesInRefSetCE);
        double scoreAllE = 2 * precisionAllE * recallAllE / (recallAllE + precisionAllE);

        result = new StatisticResult(precisionAll, recallAll, scoreAll);
        try {
            statsOut.write("Precision: " + precisionAll + "\n");
            statsOut.write("Recall: " + recallAll + "\n");
            statsOut.write("F-Score: " + scoreAll + "\n");

            statsOut.write("Precision(Exact): " + precisionAllE + "\n");
            statsOut.write("Recall(Exact): " + recallAllE + "\n");
            statsOut.write("F-Score(Exact): " + scoreAllE + "\n");

            statsOut.write("MolFunc Precision: " + precisionF + "\n");
            statsOut.write("MolFunc Recall: " + recallF + "\n");
            statsOut.write("MolFunc F-Score: " + scoreF + "\n");

            statsOut.write("MolFunc Precision(Exact): " + precisionFE + "\n");
            statsOut.write("MolFunc Recall(Exact): " + recallFE + "\n");
            statsOut.write("MolFunc F-Score(Exact): " + scoreFE + "\n");
            result.setMolFunc(new Result(precisionF, recallF, scoreF));

            statsOut.write("BioProc Precision: " + precisionP + "\n");
            statsOut.write("BioProc Recall: " + recallP + "\n");
            statsOut.write("BioProc F-Score: " + scoreP + "\n");

            statsOut.write("BioProc Precision(Exact): " + precisionPE + "\n");
            statsOut.write("BioProc Recall(Exact): " + recallPE + "\n");
            statsOut.write("BioProc F-Score(Exact): " + scorePE + "\n");
            result.setBioProc(new Result(precisionP, recallP, scoreP));

            statsOut.write("CelComp Precision: " + precisionC + "\n");
            statsOut.write("CelComp Recall: " + recallC + "\n");
            statsOut.write("CelComp F-Score: " + scoreC + "\n");

            statsOut.write("CelComp Precision(Exact): " + precisionCE + "\n");
            statsOut.write("CelComp Recall(Exact): " + recallCE + "\n");
            statsOut.write("CelComp F-Score(Exact): " + scoreCE + "\n");
            result.setCelComp(new Result(precisionC, recallC, scoreC));

            statsOut.close();
        }
        catch (IOException e) {
        }

    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(
                        PR_RT_ARG,
                        PR_RT_ARG_DESC,
                        true, null, false
                ),
                new StringArgumentDefinition(
                        STATISTICS_DIR_ARG,
                        STATISTICS_DIR_ARG_DESC,
                        true, null, false
                ),
                new StringArgumentDefinition(
                        CONC_CLASS_ARG,
                        CONC_CLASS_ARG_DESC,
                        true, null, false
                )
        };
    }

    public String getName() {
        return "Hirarchy adjusted F-score for mapping to GO ontology.";
    }

    public String getVersion() {
        return "10-Mar-2008";
    }

    @Override
    public String getId() {
        return "ontologydistance";
    }

    public boolean requiresIndexedGraph() {
        return false;
    }

    public StatisticResult getResult() {
        return result;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }
}



