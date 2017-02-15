package net.sourceforge.ondex.core.persistent.binding;

import com.sleepycat.je.Cursor;
import org.apache.log4j.Logger;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Management of berkeley cursors.
 *
 * @author Matthew Pocock
 */
public class CursorManager
{
    private static final Logger LOG = Logger.getLogger(CursorManager.class);

    private final ReferenceQueue<Object> referenceQueue;
    private final Map<PhantomReference<?>, Cursor> cursors;
    private final Map<Object, PhantomReference<?>> refByPeer;
    private final CursorCleaner cursorCleaner;

    {
        referenceQueue = new ReferenceQueue<Object>();
        cursors = new HashMap<PhantomReference<?>, Cursor>();
        refByPeer = new WeakHashMap<Object, PhantomReference<?>>();
        cursorCleaner = new CursorCleaner();
        Thread t = new Thread(new CursorCleaner());
        t.setDaemon(true);
        t.start();
    }


    public void registerCursor(Object peer, Cursor c)
    {
        PhantomReference<?> pr = new PhantomReference<Object>(peer, referenceQueue);
        cursors.put(pr, c);
        refByPeer.put(peer, pr);
    }

    public void releaseCursor(Object peer)
    {
        PhantomReference<?> pr = refByPeer.get(peer);
        if(pr != null)
        {
            Cursor c = cursors.remove(pr);
            if(c != null) c.close();
        }
    }

    public void close()
    {
        for(Cursor c : cursors.values())
        {
            c.close();
        }
        cursors.clear();
        refByPeer.clear();
        cursorCleaner.stop = true;
    }

    private class CursorCleaner implements Runnable
    {
        volatile boolean stop = false;

        @Override
        public void run()
        {
            LOG.info("Started CursorClean thread");
            while(!stop)
            {
                try {
                    PhantomReference<?> pr = (PhantomReference<?>) referenceQueue.remove(100);
                    if(pr != null)
                    {
                        Cursor c = cursors.get(pr);
                        if(c != null)
                        {
                            c.close();
                        }
                    }
                    cursors.remove(pr);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            LOG.info("Preparing to shut down CursorClean thread");
            // asked to stop - drain referenceQueue
            for(Reference<?> pr = referenceQueue.poll(); pr != null; pr = referenceQueue.poll())
            {
                Cursor c = cursors.get((PhantomReference<?>) pr);
                if(c != null)
                {
                    c.close();
                }
            }

            LOG.info("Shutting down CursorClean thread");
        }
    }
}
