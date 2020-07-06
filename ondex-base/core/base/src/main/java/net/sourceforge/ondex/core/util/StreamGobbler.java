package net.sourceforge.ondex.core.util;

import java.io.*;

/**
 * @author lysenkoa
 */
public class StreamGobbler extends Thread {
    InputStream is;
    OutputStream os;

    public StreamGobbler(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                os.write(line.getBytes());
                os.write("\n".getBytes());
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}

