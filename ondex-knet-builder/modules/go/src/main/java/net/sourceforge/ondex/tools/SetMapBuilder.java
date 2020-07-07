package net.sourceforge.ondex.tools;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @param <K>
 * @param <V>
 */
public class SetMapBuilder<K extends Object, V extends Object> {
    Map<K, Set<V>> map;

    public SetMapBuilder(Map<K, Set<V>> index) {
        map = index;
    }

    public void setMap(Map<K, Set<V>> map) {
        this.map = map;
    }

    public void addEntry(K k, V v) {
        Set<V> n = map.get(k);
        if (n == null) {
            n = new HashSet<V>();
            map.put(k, n);
        }
        n.add(v);
    }

    public void addAll(K k, Set<V> set) {
        Set<V> n = map.get(k);
        if (n == null) {
            n = new HashSet<V>();
            map.put(k, n);
        }
        n.addAll(set);
    }

    public Map<K, Set<V>> getSetMap() {
        return map;
    }

    public void clear() {
        map.clear();
    }
}