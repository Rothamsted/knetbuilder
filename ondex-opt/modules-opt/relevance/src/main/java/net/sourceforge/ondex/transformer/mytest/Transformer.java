package net.sourceforge.ondex.transformer.mytest;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

/**
 * A dummy transformer that simply adds "bob" as the value of the "fact" Attribute to each relation.
 *
 * @author Matthew Pocock
 */
public class Transformer extends ONDEXTransformer
{

    public String getName() {
        return "mytest";
    }

    public String getVersion() {
        return "alpha";
    }

    @Override
    public String getId() {
        return "mytest";
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void start() throws Exception {
        AttributeName att = this.graph.getMetaData().createAttributeName("fact", "fact", "Interesting fact", null, String.class, null);

        for (ONDEXRelation rel : this.graph.getRelations()) {
            rel.createAttribute(att, "bob", false);
        }
    }

    public boolean requiresIndexedGraph() {
        return false;
    }

    public String[] requiresValidators() {
        return new String[0];
    }
}
