package net.sourceforge.ondex.export.tab;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.export.ONDEXExport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Export extends ONDEXExport
{

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{new FileArgumentDefinition(
                FileArgumentDefinition.EXPORT_FILE, "tab delimited file", true,
                false, false, false)};
    }

    public String getName() {
        return "tab export";
    }

    public String getVersion() {
        return "alpha";
    }

    @Override
    public String getId() {
        return "tab";
    }

    public boolean requiresIndexedGraph() {
        return false;
    }

    public void start() throws InvalidPluginArgumentException {
        System.out.println("EXPORT to TAB!!!!");
        ConceptClass ccTaxon = graph.getMetaData().getConceptClass("Taxon");
        if (ccTaxon == null) {
            fireEventOccurred(new ConceptClassMissingEvent(
                    "CC TAXON not found", "[Export - start]"));
        }

        RelationType rtTM = graph.getMetaData().getRelationType("r");
        if (rtTM == null) {
            fireEventOccurred(new RelationTypeMissingEvent("RT IS_R not found",
                    "[Export - start]"));
        }

        AttributeName anScore = graph.getMetaData()
                .getAttributeName("TM_SCORE");
        if (anScore == null) {
            fireEventOccurred(new AttributeNameMissingEvent(
                    "AN TM_SCORE not found", "[Export - start]"));
        }

        AttributeName anHeader = graph.getMetaData().getAttributeName(
                "AbstractHeader");
        if (anHeader == null) {
            fireEventOccurred(new AttributeNameMissingEvent(
                    "AN HEADER not found", "[Export - start]"));
        }

        AttributeName anYear = graph.getMetaData().getAttributeName("YEAR");
        if (anYear == null) {
            fireEventOccurred(new AttributeNameMissingEvent(
                    "AN YEAR not found", "[Export - start]"));
        }

        AttributeName anEvidence = graph.getMetaData().getAttributeName(
                "EVIDENCE");
        if (anEvidence == null) {
            fireEventOccurred(new AttributeNameMissingEvent(
                    "AN EVIDENCE not found", "[Export - start]"));
        }

        File file = new File((String) args.getUniqueValue(FileArgumentDefinition.EXPORT_FILE));
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer
                    .append("PMID\tAbstract Header\tYear\tIR Score\tFound Entities (NER)\tAccession\tNER Score\tNER Evidence\n");
            for (ONDEXRelation rel : graph.getRelationsOfRelationType(rtTM)) {
                if (rel.getToConcept().getOfType().equals(ccTaxon)) {
                    ONDEXConcept pub = rel.getFromConcept();
                    Attribute attributeScore = rel.getAttribute(anScore);

                    String pmid = "=HYPERLINK(\"http://www.ncbi.nlm.nih.gov/pubmed/"
                            + pub.getPID()
                            + "\",\"PMID:"
                            + pub.getPID()
                            + "\")";
                    String title = pub.getAttribute(anHeader).getValue()
                            .toString();
                    String year = pub.getAttribute(anYear).getValue()
                            .toString();
                    String scoreIR = attributeScore.getValue().toString();
                    String annotName = "";
                    String accLink = "";
                    String scoreNER = "";
                    String evidences = "";

                    String botrytisInfo = pmid + "\t" + title + "\t" + year
                            + "\t" + scoreIR;
                    boolean hasAnnotation = false;
                    for (ONDEXRelation tmRel : graph.getRelationsOfConcept(pub)) {
                        // only consider TM relations
                        if (!tmRel.getOfType().getId().equals("r")) {
                            continue;
                        }
                        if (!tmRel.getToConcept().getOfType().equals(ccTaxon)) {

                            Attribute annoAttribute = tmRel
                                    .getAttribute(anScore);
                            scoreNER = annoAttribute.getValue().toString();

                            ONDEXConcept annot = tmRel.getToConcept();
                            String conCV = annot.getElementOf().getId()
                                    .toUpperCase();

                            for (ConceptAccession conAcc : annot.getConceptAccessions()) {
                                String accCV = conAcc.getElementOf().getId()
                                        .toUpperCase();
                                if (conCV.equals("GO") && accCV.equals("GO")) {
                                    String acc = conAcc.getAccession();
                                    accLink = "=HYPERLINK(\"http://amigo.geneontology.org/cgi-bin/amigo/term-details.cgi?term="
                                            + acc + "\",\"" + acc + "\")";
                                } else if (conCV.equals("EC")
                                        && accCV.equals("EC")) {
                                    String acc = conAcc.getAccession();
                                    accLink = "=HYPERLINK(\"http://www.expasy.org/enzyme/"
                                            + acc + "\",\"EC:" + acc + "\")";
                                } else if (conCV.equals("UNIPROTKB")
                                        && accCV.equals("UNIPROTKB")) {
                                    String acc = conAcc.getAccession();
                                    accLink = "=HYPERLINK(\"http://beta.uniprot.org/uniprot/"
                                            + acc
                                            + "\",\"UniProt:"
                                            + acc
                                            + "\")";
                                }
                            }

                            annotName = annot.getConceptName().getName();
                            Attribute eviAttribute = tmRel
                                    .getAttribute(anEvidence);
                            evidences = eviAttribute.getValue().toString();
                            writer.append(botrytisInfo + "\t" + annotName
                                    + "\t" + accLink + "\t" + scoreNER + "\t"
                                    + evidences + "\n");
                            hasAnnotation = true;
                        }
                    }
                    if (!hasAnnotation) {
                        writer.append(botrytisInfo + "\n");
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] requiresValidators() {
        return new String[0];
    }
}
