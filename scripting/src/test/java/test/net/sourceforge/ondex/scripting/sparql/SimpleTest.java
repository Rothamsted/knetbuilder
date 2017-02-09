package test.net.sourceforge.ondex.scripting.sparql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 *
 * @author ucbtgrz
 */
public class SimpleTest {

    private Logger log = Logger.getLogger(SimpleTest.class);
    
    @Test
    public void test() throws Exception {
        log.info("simple test");
        
        String str = "\"http://www.google.com/\",\"ma\",\"kota\"";
        String pattern = "[^\",]+";
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        
        if (m.find()) {
            do {
                log.info("group: " + m.group());
            } while (m.find());
            
        } else {
            log.info("no matches");
        }
    }
}
