package net.sourceforge.ondex.parser.helloworld;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.parser.ONDEXParser;


/**
 * Hello world Parser
 * <p/>
 * This is intended to do nothing except run and print 0 to 9
 *
 * @author sckuo
 */


public class Parser extends ONDEXParser
{

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[0];
    }

    @Override
    public String getName() {
        return "Hello World!";
    }

    @Override
    public String getVersion() {
        return "final";
    }

    @Override
    public String getId() {
        return "helloworld";
    }


    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {

        for (int i = 0; i < 10; i++) {

            System.out.println("Hello world!" + i);

        }


    }

}
