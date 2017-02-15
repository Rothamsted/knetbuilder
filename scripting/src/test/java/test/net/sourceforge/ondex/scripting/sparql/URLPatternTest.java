package test.net.sourceforge.ondex.scripting.sparql;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author grzebyta
 */
@RunWith(Parameterized.class)
public class URLPatternTest {
    private Logger log = Logger.getLogger(getClass());
    private String query;
    private int grops;

    public URLPatternTest(String query, Integer groups) {
        this.query = query;
        this.grops = groups;
    }
    
    @Parameterized.Parameters
    public static Collection<Object[]> params() {
        Object [][] toReturn = new Object[][] { 
            {"connect(\"http://some.url/sparql\")", 4}, 
            {"connect(\"http://some.url/sparql\",\"user\",\"password\")", 4},
            {"connect(\"http://some.url/sparql\", \"user\", \"password\")", 4}
        };
        
        return Arrays.asList(toReturn);
    }
    
    @Test
    public void patternTest() throws Exception {
        log.info("** pattern test");
        log.debug(String.format("request '%s'", query));
        
                       //"connect\\s*?\\(\\\"(.+?)\\\"\\)"        
        String pattern = "connect\\s*?\\(\\\"([^\"]+)\\\"(,\\s*?\\\"([^\"]+?)\\\",\\s*?\\\"(.+?)\\\")??\\)";  //valid pattern
        //String pattern = "connect\\s*?\\((.+?)\\)" ;
        
        
        Pattern loadPath = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = loadPath.matcher(query);
        
        
        Assert.assertTrue(String.format("string '%s' should be matched", query), matcher.matches());
        
        log.info("groups: "+matcher.groupCount());
        log.info("expected: "+grops);
        
        // display groups
        for (int i=1; i<matcher.groupCount()+1; i++) {
            log.info(String.format("\t%d\t\t%s", i, matcher.group(i)));
        }
        
        
        Assert.assertTrue(matcher.groupCount() == grops);
    }
}
