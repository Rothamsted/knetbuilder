package net.sourceforge.ondex.algorithm.graphquery;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.InvalidFileException;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.StateMachineInvalidException;
import net.sourceforge.ondex.algorithm.graphquery.flatfile.StateMachineFlatFileParser;
import net.sourceforge.ondex.algorithm.graphquery.pathrank.NumericalRank;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;

/**
 * @author hindlem
 */
public class StateMachineFlatFileParserTest extends TestCase {

    private MemoryONDEXGraph aog;

    private File file;

    public void setUp() {

        aog = new MemoryONDEXGraph("testGraph");
        aog.getMetaData().getFactory().createConceptClass("Protein");
        aog.getMetaData().getFactory().createConceptClass("TF");
        aog.getMetaData().getFactory().createConceptClass("Gene");
        aog.getMetaData().getFactory().createConceptClass("Enzyme");
        aog.getMetaData().getFactory().createConceptClass("Reaction");
        aog.getMetaData().getFactory().createConceptClass("Pathway");
        aog.getMetaData().getFactory().createConceptClass("TARGETSEQUENCE");

        aog.getMetaData().getFactory().createRelationType("en_by");

        aog.getMetaData().getFactory().createRelationType("is_a");

        aog.getMetaData().getFactory().createRelationType("m_isp");

        aog.getMetaData().getFactory().createRelationType("interacts");

        aog.getMetaData().getFactory().createRelationType("rg_by");

        aog.getMetaData().getFactory().createRelationType("cat_by");

        aog.getMetaData().getFactory().createRelationType("h_s_s");

        aog.getMetaData().getFactory().createAttributeName("BLEV", Double.class);

        try {
            file = File.createTempFile("tmp" + System.currentTimeMillis(), "tmp");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            //write states
            bw.append("#Finite States *=start state ^=end state");
            bw.newLine();
            bw.append("1*	TARGETSEQUENCE");
            bw.newLine();
            bw.append("2	Protein");
            bw.newLine();
            bw.append("3	Gene");
            bw.newLine();
            bw.append("4	TF");
            bw.newLine();
            bw.append("5	Enzyme");
            bw.newLine();
            bw.append("6	Reaction");
            bw.newLine();
            bw.append("7^	Pathway");
            bw.newLine();
            //write transitions
            bw.append("#Transitions");
            bw.newLine();
            bw.append("1-2	h_s_s");
            bw.newLine();
            bw.append("2-5	is_a");
            bw.newLine();
            bw.append("5-6	cat_by");
            bw.newLine();
            bw.append("6-7	m_isp");
            bw.newLine();
            bw.append("2-4	is_a");
            bw.newLine();
            bw.append("3-4	rg_by");
            bw.newLine();
            bw.append("2-3	en_by");
            bw.newLine();
            bw.append("2-2	h_s_s");
            bw.newLine();
            bw.append("#Weightings on transitions	AttributeName	Modifiers(CSV i=inverted lower is better, m=modulus values e.g. (i,m))	Relative Rank	TYPE(sum, mean, probability)");
            bw.newLine();
            bw.append("1-2	BLEV	i	1	probability");
            bw.newLine();
            bw.append("2-2	BLEV	i	2	probability");

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void tearDown() {
        aog = null;
        file.delete();
    }

    public void testBasicParse() throws IOException, InvalidFileException, StateMachineInvalidException {
        StateMachineFlatFileParser smp = new StateMachineFlatFileParser();
        smp.parseFile(file, aog);
        StateMachine sm = smp.getStateMachine();

        assertEquals(8, sm.getAllTransitions().size());
        assertEquals(7, sm.getAllStates().size());
        assertEquals("TARGETSEQUENCE", sm.getStart().getValidConceptClass().getId());
        assertEquals(1, sm.getFinishes().size());
        assertEquals("Pathway", sm.getFinishes().iterator().next().getValidConceptClass().getId());

        List<NumericalRank> ranks = smp.getRanks();
        assertEquals(2, ranks.size());

    }

}
