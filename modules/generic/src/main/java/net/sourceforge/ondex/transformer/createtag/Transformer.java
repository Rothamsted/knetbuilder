package net.sourceforge.ondex.transformer.createtag;

import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import java.util.Set;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.*;

@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Transformer extends ONDEXTransformer implements
        ArgumentNames {

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition[]{
                new StringArgumentDefinition(ArgumentNames.CC,
                        ArgumentNames.CC_DESC, true, null, false),
                new StringArgumentDefinition(ArgumentNames.NAME,
                        ArgumentNames.NAME_DESC, true, null, false)};

    }

    @Override
    public String getName() {
        return "createtag";
    }

    @Override
    public String getVersion() {
        return "v0.1";
    }

    @Override
    public String getId() {
        return "createtag";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {
        String conceptClass = (String) args.getUniqueValue(CC);
        String name = (String) args.getUniqueValue(NAME);
        Set<ONDEXConcept> cs = graph.getConcepts();
        Set<ONDEXRelation> rs = graph.getRelations();
        ONDEXConcept c = graph.getFactory().createConcept(name, createDataSource(graph, "UC"), createCC(graph, conceptClass), createEvidence(graph, "M"));
        c.createConceptName(name, true);

        for (ONDEXConcept co : cs) {
            co.addTag(c);
        }

        for (ONDEXRelation re : rs) {
            re.addTag(c);
        }
    }

}
