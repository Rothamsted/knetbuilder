package net.sourceforge.ondex.parser.kegg53.util;

import java.util.*;

public class PersistentBerkleySet<V> implements Set<V> {

    public interface Deserialiser<V> {
        public abstract V deserialise(byte[] array);
    }

    private final Set<String> storeReference;
    private final TempBerkleyStore berkStore;
    private final Class<V> classType;
    private final Deserialiser<V> deserialiser;

    public PersistentBerkleySet(Class<V> classType, Deserialiser<V> deserialiser, TempBerkleyStore berkStore) {
        this.classType = classType;
        this.berkStore = berkStore;
        this.deserialiser = deserialiser;
        storeReference = Collections.synchronizedSet(new HashSet<String>(10000));
    }

    public boolean add(V o) {
        berkStore.insertIntoDatabase(classType, o.toString(), ((Serialises) o).serialise());
        return storeReference.add(o.toString());
    }

    public boolean addAll(Collection<? extends V> c) {
        boolean doneok = true;
        final Iterator<? extends V> it = c.iterator();
        for (V element : c)
            if (!add(element))
                doneok = false;
        return doneok;
    }

    public void clear() {
        for (String ref : storeReference) {
            berkStore.deleteFromDatabase(classType, ref);
        }
        storeReference.clear();

    }

    public boolean contains(Object o) {
        return storeReference.contains(o.toString());
    }

    public boolean containsAll(Collection<?> c) {
        boolean hasall = true;
        Iterator<?> it = c.iterator();
        for (Object element : c)
            if (!contains(element)) hasall = false;
        return hasall;
    }

    public boolean isEmpty() {
        return storeReference.isEmpty();
    }

    public Iterator<V> iterator() {

        return new Iterator<V>() {

            private final Iterator<String> referenceIt = storeReference.iterator();
            private String currentRef;

            public boolean hasNext() {
                return referenceIt.hasNext();
            }

            public V next() {
                currentRef = referenceIt.next();
                final byte[] array = berkStore.getFromDatabase(classType, currentRef);
                if (array == null)
                    throw new NullPointerException(currentRef + " in " + classType + " database is null");
                else return deserialiser.deserialise(array);
            }

            public void remove() {
                berkStore.deleteFromDatabase(classType, currentRef);
                currentRef = null;
            }

        };
    }

    public boolean remove(Object o) {
        berkStore.deleteFromDatabase(classType, o.toString());
        return storeReference.remove(o);
    }

    public boolean removeAll(Collection<?> c) {
        boolean hasall = true;
        for (Object element : c)
            if (!remove(element))
                hasall = false;
        return hasall;
    }

    public boolean retainAll(Collection<?> c) {
        boolean hasall = true;
        Iterator<?> it = c.iterator();
        while (it.hasNext())
            if (!contains(it.next()))
                if (!remove(it.next()))
                    hasall = false;
        return hasall;
    }

    public int size() {
        return storeReference.size();
    }

    public V[] toArray() {
        return null;
    }


    public <T> T[] toArray(T[] a) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void finalize() {
        berkStore.closeDatabase(classType);
    }

}
