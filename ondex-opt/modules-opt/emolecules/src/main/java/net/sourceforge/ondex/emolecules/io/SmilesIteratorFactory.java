package net.sourceforge.ondex.emolecules.io;

import com.google.common.collect.AbstractIterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

/**
 * The Smiterator (smiles iterator) factory.
 * 
 * @author grzebyta
 * @see Iterable
 */
public class SmilesIteratorFactory implements Iterable<Smile> {
    
    private LineIterator li;
    private static Logger log = Logger.getLogger(SmilesIteratorFactory.class);
    
    public SmilesIteratorFactory(File f) throws IOException {
        // convert compressed file to input stream
        GzipCompressorInputStream is =
                new GzipCompressorInputStream(new FileInputStream(f));
        try {
            this.li = IOUtils.lineIterator(is, null);
        } catch (IOException io) {
            IOUtils.closeQuietly(is);
            throw io;
        }
    }
    
    public SmilesIteratorFactory(File f, String encoding) throws IOException {
        GzipCompressorInputStream is =
                new GzipCompressorInputStream(new FileInputStream(f));
        try {
            this.li = IOUtils.lineIterator(is, encoding);
        } catch (IOException io) {
            IOUtils.closeQuietly(is);
            throw io;
        }
    }
    
    public SmilesIteratorFactory(InputStream is, String encoding) throws IOException {
        try {
            this.li = IOUtils.lineIterator(is, encoding);
        } catch (IOException io) {
            IOUtils.closeQuietly(is);
            throw io;
        }
    }
    
    public Iterator<Smile> iterator() {
        log.debug("prepare iterator");
        
        return new AbstractIterator<Smile>() {

            @Override
            protected Smile computeNext() {
                while(li.hasNext()) {
                    String line = ((String) li.next()).trim();
                    try {
                        String[] splited = line.split("\\s");
                        String smile = splited[0];
                        Long id = Long.valueOf(splited[1]);
                        Long parent = Long.valueOf(splited[2]);
                        
                        Smile value = new Smile();
                        value.setSmile(smile);
                        value.setId(id);
                        value.setParent(parent);
                        
                        return value;
                    } catch (NumberFormatException e) {
                        // do nothing
                        // just omit the step
                        log.trace(String.format("handled exception: %s. No actinon", e));
                    }
                }
                return endOfData();
            }
            
        };
    }
}
