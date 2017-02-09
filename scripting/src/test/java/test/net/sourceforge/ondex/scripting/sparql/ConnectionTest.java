package test.net.sourceforge.ondex.scripting.sparql;

import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author ucbtgrz
 */
public class ConnectionTest {

    private Logger log = Logger.getLogger(ConnectionTest.class);

    @Test
    public void test() throws Exception {
        log.info("connection test");

        HttpURLConnection connection = (HttpURLConnection) new URL("http://rdf.phibase.org/sparql/").openConnection();
        String user = "user";
        String passwd = "user";
        //String encoded = Base64.encode(String.valueOf(user + ":" + passwd).getBytes());
        byte[] encoded = Base64.encodeBase64(String.valueOf(user+":"+passwd).getBytes());
        connection.setRequestProperty("Authorization", "Basic " + new String(encoded));
        
        int status = connection.getResponseCode();
        Assert.assertEquals(String.format("has status %d - %s", status, connection.getResponseMessage()),200, status);
        connection.disconnect();
    }
}
