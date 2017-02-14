/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.psimi2ondex;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author jweile
 */
public abstract class ZipHandler {

    private ZipFile zf;

    private long totalUncompressedSize = 0L;
    private long readUncompressed = 0L;
    private long startTime;

    public ZipHandler(ZipFile zf) {
        this.zf = zf;
        Enumeration<? extends ZipEntry> entries = zf.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            long size = entry.getSize();
            if (size > -1) {
                totalUncompressedSize += size;
            } else {
                totalUncompressedSize = -1L;
                break;
            }
        }
    }

    public abstract void handleZipStream(InputStream in) throws Exception;

    public void process() throws IOException {

        startTime = System.currentTimeMillis();

        Enumeration<? extends ZipEntry> entries = zf.entries();
        while (entries.hasMoreElements()) {

            ZipEntry entry = entries.nextElement();
            String entrySuffix = entry.getName().substring(entry.getName().length()-4);

            if (entrySuffix.equalsIgnoreCase(".xml")) {

                System.out.println("Parsing zip entry "+entry.getName());
                handleEntry(entry);
                readUncompressed += entry.getSize();

            } else {
                
                System.out.println("Skipping zip entry "+entry.getName());

            }

        }
    }

    private void handleEntry(final ZipEntry entry) throws IOException {

        InputStream in = zf.getInputStream(entry);
        final ByteCounterInputStream bcin = new ByteCounterInputStream(in);

        SupervisedThread t = new SupervisedThread("ZipHandler") {

            public void runSupervised() throws Exception {
                handleZipStream(bcin);
            }

        };
        t.start();

        while(t.isAlive()) {
            assessProgress(readUncompressed + bcin.getBytesRead());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {}
        }

        if (t.getThrown() != null) {
            if (t.getThrown() instanceof IOException) {
                throw (IOException) t.getThrown();
            } else if (t.getThrown() instanceof RuntimeException) {
                throw (RuntimeException) t.getThrown();
            } else {
                throw new RuntimeException(t.getThrown());
            }
        }

    }


    int __lastPercent = 0;
    private void assessProgress(long bytesRead) {

        int percent = (int)(bytesRead * 100L / totalUncompressedSize);

        if (percent > __lastPercent) {

            long time = System.currentTimeMillis() - startTime;
            long bytesToRead = totalUncompressedSize - bytesRead;
            long eta = time * bytesToRead / bytesRead;

            System.out.println("Progress: "+percent+"%. ETA: "+convertTime(eta));

            __lastPercent = percent;
        }

    }

    private abstract class SupervisedThread extends Thread {

        private Exception thrown = null;

        public SupervisedThread(String name) {
            super(name);
        }

        public abstract void runSupervised() throws Exception;

        public void run() {
            try {
                runSupervised();
            } catch (Exception e) {
                thrown = e;
            }
        }

        public Exception getThrown() {
            return thrown;
        }
    }

     public String convertTime(long millis) {
        String[] symbols = {"wks","days","hrs","mins","secs"};
        long[] multiples = {7L,24L,60L,60L};
        long[] units = new long[5];
        units[4] = 1000;
        for (int i = 3; i >= 0; i--)
            units[i] = units[i+1] * multiples[i];

        StringBuilder b = new StringBuilder();
        long rest = millis;
        for (int i = 0; i < 5; i++) {
            int num = (int)(rest / units[i]);
            if (num > 0)
                b.append(num + symbols[i]+" ");
            rest %= units[i];
        }
        return b.toString();
    }

}
