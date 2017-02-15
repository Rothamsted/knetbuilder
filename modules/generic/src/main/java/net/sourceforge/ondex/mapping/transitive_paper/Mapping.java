package net.sourceforge.ondex.mapping.transitive_paper;

import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.mapping.ONDEXMapping;

import java.util.HashSet;

@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Mapping extends ONDEXMapping
{

    private ConceptClass ccProtein;

    private HashSet<ConceptClass> go = new HashSet<ConceptClass>();

    private ConceptClass ccCelComp;

    private ConceptClass ccBioProc;

    private ConceptClass ccMolFunc;

    private ConceptClass ccProtFam;

    public Mapping() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void start() {
        EvidenceType eqTrans = graph.getMetaData().getEvidenceType("TRANS");
        RelationType rtMis = graph.getMetaData().getRelationType("member_of");

        ccProtFam = graph.getMetaData().getConceptClass("ProtFam");

        ccProtein = graph.getMetaData().getConceptClass("Protein");

        ccCelComp = graph.getMetaData().getConceptClass("CelComp");

        ccBioProc = graph.getMetaData().getConceptClass("BioProc");

        ccMolFunc = graph.getMetaData().getConceptClass("MolFunc");

        go.add(ccCelComp);
        go.add(ccBioProc);
        go.add(ccMolFunc);
        int relations = 0;
        for (ONDEXConcept protein : graph.getConceptsOfConceptClass(ccProtein)) {
            for (ONDEXRelation proteinRealtion : graph.getRelationsOfConcept(protein)) {
                if (proteinRealtion.getToConcept().getOfType().equals(ccProtFam)) {
                    ONDEXConcept protFam = proteinRealtion.getToConcept();
                    for (ONDEXRelation protFamRelation : graph.getRelationsOfConcept(protFam)) {
                        if (go.contains(protFamRelation.getToConcept().getOfType())) {
                            if (graph.getRelation(protein, protFamRelation.getToConcept(), rtMis) == null) {
                                graph.getFactory().createRelation(protein, protFamRelation.getToConcept(), rtMis, eqTrans);

                                relations++;
                            }
                        }
                    }
                }
            }
            if (relations % 100 == 0)
                System.out.println("Created " + relations + " relations");
        }
        System.out.println("Created " + relations + " relations");
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[0];
    }

    public String getName() {
        return "transitive_paper";
    }

    public String getVersion() {
        return "pre-alpha";
    }

    @Override
    public String getId() {
        return "transitive_paper";
    }

    public boolean requiresIndexedGraph() {
        return false;
    }

    public String[] requiresValidators() {
        return new String[0];
    }
}
