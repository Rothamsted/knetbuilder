package net.sourceforge.ondex.parser.kegg53;

import net.sourceforge.ondex.data.DataRetrieval;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;

/**
 * Under development!!! No where near finished
 *
 * @author hindlem
 */
public class KEGGDownloader implements DataRetrieval {

    @Override
    public boolean fetchFiles(File directory) {

        try {

            URI[] uris = getURIs();
            for (URI uri : uris) {

                InputStream stream = uri.toURL().openConnection()
                        .getInputStream();

                DigestInputStream dis = new DigestInputStream(stream, "MD5");

                GZIPInputStream gzipInputStream = new GZIPInputStream(dis);

                //then feed this gzip stream to a de-tar'r

                int intMd5Sum = new BigInteger(1, dis.getDigest()).intValue();

                stream.close();

            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public String getCurrentStatus() {
        return "downloading";
    }

    @Override
    public double getProgressOnFile() {
        return 0;
    }

    @Override
    public double getProgressOnOverall() {
        return 0;
    }

    @Override
    public URI[] getURIs() {

        String site = "ftp://ftp.genome.jp/pub/kegg/release/current/";

        String[] files = new String[]{
                "pathway.tar.gz", "pathway_dbget.tar.gz",
                "ligand.tar.gz", "kgml.tar.gz", "brite.tar.gz",
                "genes.tar.gz", "medicus.tar.gz"
        };

        URI[] uris = new URI[files.length];

        for (int i = 0; i < files.length; i++) {
            try {
                uris[i] = new URI(site + files[i]);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

        }
        return uris;
    }

    /**
     * Calculates the MD5 code on an inputstream
     *
     * @author hindlem
     */
    public class DigestInputStream extends FilterInputStream {

        private MessageDigest algorithm;

        /**
         * @param is            the inputstream to digest
         * @param algorithmName the digest algorithm e.g. MD5 @see MessageDigest
         * @throws NoSuchAlgorithmException if the algorithm doesn exist
         */
        public DigestInputStream(InputStream is, String algorithmName) throws NoSuchAlgorithmException {
            super(is);
            algorithm = MessageDigest.getInstance(algorithmName);
        }

        public int read() throws IOException {
            int got = super.read();
            if (got > -1) {
                algorithm.update((byte) got);
            }
            return got;
        }

        /**
         * @return the digest of this stream
         */
        public byte[] getDigest() {
            return algorithm.digest();
        }

    }

}
