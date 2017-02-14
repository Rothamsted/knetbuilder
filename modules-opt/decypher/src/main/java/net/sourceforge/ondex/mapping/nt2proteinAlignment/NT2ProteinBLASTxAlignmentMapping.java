package net.sourceforge.ondex.mapping.nt2proteinAlignment;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.mapping.ONDEXMapping;
import net.sourceforge.ondex.programcalls.BLASTAlignmentProgram;
import net.sourceforge.ondex.programcalls.Match;
import net.sourceforge.ondex.programcalls.decypher.DecypherAlignment;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A example plugin to demonstrate the DeCypher API
 * @author hindlem
 *         Date: 9/1/11
 *         Time: 2:57 PM
 * @version 0.10
 */
@Authors(authors = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
@Custodians(custodians = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
@Status(status = StatusType.EXPERIMENTAL)
public class NT2ProteinBLASTxAlignmentMapping extends ONDEXMapping {


    final static String PROGRAM_DIR_ARG = "ProgramDir";
    final static String PROGRAM_DIR_DESC = "The directory where the decypher program is located";

    @Override
    public String getId() {
        return NT2ProteinBLASTxAlignmentMapping.class.getSimpleName();
    }

    @Override
    public String getName() {
        return "BLASTs all concepts with NT sequences against all concepts with Protein sequences using BLASTx";
    }

    @Override
    public String getVersion() {
        return "alpha";
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        Set<ArgumentDefinition<?>> extendedDefinition = new HashSet<ArgumentDefinition<?>>();
        extendedDefinition.add(new FileArgumentDefinition(PROGRAM_DIR_ARG, PROGRAM_DIR_DESC, true, true, true));

        return extendedDefinition.toArray(new ArgumentDefinition<?>[extendedDefinition.size()]);
    }

    @Override
    public void start() throws Exception {
        Set<ONDEXConcept> conceptsWithNT = graph.getConceptsOfAttributeName(graph.getMetaData().getAttributeName("NA"));
        Set<ONDEXConcept> conceptsWithProteins = graph.getConceptsOfAttributeName(graph.getMetaData().getAttributeName("AA"));

        DecypherAlignment dcAlign = new DecypherAlignment(
                    net.sourceforge.ondex.config.Config.ondexDir,
                    getArguments().getUniqueValue(PROGRAM_DIR_ARG).toString(),
                    0, //cutff
                    0f, //%
                    0001f, //evalue
                    0, //bitscore
                    Integer.MAX_VALUE, //max alignments
                    false);

        Collection<Match> hits = dcAlign.query(graph, conceptsWithNT, conceptsWithProteins, BLASTAlignmentProgram.ALGO_BLASTX);

        RelationType h_s_s = graph.getMetaData().getRelationType("h_s_s");
        EvidenceType ev = graph.getMetaData().createEvidenceType("BLASTx", "BLASTx", "BLASTx");
        Set<EvidenceType> evs = new HashSet<EvidenceType>();

        for (Match hit : hits) {
            //you might prefer to build an index query-->Hit if you want to take the best n
            ONDEXConcept query = graph.getConcept(hit.getQueryId());
            ONDEXConcept hitTarget = graph.getConcept(hit.getTargetId());

            ONDEXRelation relation = graph.createRelation(query, hitTarget, h_s_s, evs);
            //add some properties to the relations...etc
        }
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }
}
