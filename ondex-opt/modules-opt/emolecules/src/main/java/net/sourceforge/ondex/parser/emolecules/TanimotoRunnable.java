package net.sourceforge.ondex.parser.emolecules;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import net.sourceforge.ondex.emolecules.utils.BitSetUtils;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Node;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.similarity.Tanimoto;

/**
 * Internal TanimotoService's class. User usually does not neet to use it.
 *
 * @author grzebyta
 * @see TanimotoService
 */
public class TanimotoRunnable implements Runnable {
    
    private static Logger log = Logger.getLogger(TanimotoRunnable.class);
    private TanimotoService service;
    private BitSet target;
    private int start;
    private int skip;
    private List<Result> localResult;
    
    public class Result {

        private long id;
        private float result;
        
        public long getId() {
            return id;
        }
        
        public void setId(long id) {
            this.id = id;
        }
        
        public float getResult() {
            return result;
        }
        
        public void setResult(float result) {
            this.result = result;
        }
    }
    
    public TanimotoRunnable(TanimotoService service, BitSet target, int start, int skip) {
        this.service = service;
        this.target = target;
        this.start = start;
        this.skip = skip;
    }
    
    public void run() {
        //log.debug("start tanimoto thread");
        
        Node[] task = service.getTask();
        // instantiate local result storage
        int size = (int) Math.ceil(Double.valueOf(task.length) / skip);
        localResult = new ArrayList<Result>(size);

        // run the main action
        for (int i = start; i < task.length; i += skip) {
            // get data from node
            Node n = task[i];
            // if property doesn't exists than skip that node
            log.trace("have a node: " + n.getId());
            if (!n.hasProperty("fingerprint")) {
                continue;
            }
            BitSet fp = BitSetUtils.fromString((String) n.getProperty("fingerprint"));
            
            try {
                Result r = new Result();
                r.id = n.getId();
                r.result = Tanimoto.calculate(target, fp);
                
                localResult.add(r);
            } catch (CDKException e) {
                log.error("missing calculation for node: " + n.getId(), e);
                if (n.hasProperty("smile")) {
                    log.error("molecude smile: " + n.getProperty("smile"));
                    log.error("fingerprint: " + n.getProperty("fingerprint"));
                    continue;
                }
            }
        }
        
        service.registerResults(localResult);
        log.debug("thread finished");
    }
}
