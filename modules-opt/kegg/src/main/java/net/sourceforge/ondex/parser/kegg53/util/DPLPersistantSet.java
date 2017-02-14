package net.sourceforge.ondex.parser.kegg53.util;

import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class DPLPersistantSet<T> {

    private static HashSet<Class<?>> classes = new HashSet<Class<?>>();

    private Class<T> c;

    private PrimaryIndex<String, T> pIdx;

    public DPLPersistantSet(BerkleyLocalEnvironment env, Class<T> c)
            throws DatabaseException {
        this.c = c;
        if (classes.contains(c))
            throw new RuntimeException("DPLPersistantSet for " + c
                    + " already exist NOT ALLOWED");
        classes.add(c);

        // Primary key for T classes
        pIdx = env.getStore().getPrimaryIndex(String.class, c);
    }

    public T add(T o) throws DatabaseException {
        if (o == null)
            throw new IllegalArgumentException("Object added to database is null, type is: " + c.getName());
        return pIdx.put(o);
    }

    public void addAll(Collection<? extends T> c) throws DatabaseException {
        for (T element : c) {
            add(element);
        }
    }

    public T get(String key) throws DatabaseException {
        return pIdx.get(key);
    }

    public boolean contains(String key) throws DatabaseException {
        return pIdx.contains(key);
    }

    public boolean containsAll(Collection<String> c) throws DatabaseException {
        boolean hasall = true;
        for (String element : c)
            if (!contains(element))
                hasall = false;
        return hasall;
    }

    public boolean isEmpty() throws DatabaseException {
        return size() == 0;
    }

    public EntityCursor<T> getCursor() throws DatabaseException {
        EntityCursor<T> entCursor;
        CursorConfig cfg = new CursorConfig();
        cfg.setReadUncommitted(true);
        entCursor = pIdx.entities(null, cfg);
        return entCursor;
    }

    public void closeCursor(EntityCursor<T> cursor) throws DatabaseException {
        cursor.close();
    }

    public boolean remove(String o) throws DatabaseException {
        return pIdx.delete(o);
    }

    public boolean removeAll(Collection<String> c) throws DatabaseException {
        boolean hasall = true;
        for (String element : c)
            if (!remove(element))
                hasall = false;
        return hasall;
    }

    public boolean retainAll(Collection<String> c) throws DatabaseException {
        boolean hasall = true;
        Iterator<String> it = c.iterator();
        while (it.hasNext())
            if (!contains(it.next()))
                if (!remove(it.next()))
                    hasall = false;
        return hasall;
    }

    public long size() {
        return pIdx.sortedMap().size();
    }

}
