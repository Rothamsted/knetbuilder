package net.sourceforge.ondex.workflow.engine;

import java.util.*;

/**
 * Implements a store for the shared Objects that can be passed between
 * workflow plugins
 *
 * @author lysenkoa
 */
public class ResourcePool {

    private final Map<UUID, Object> map = new HashMap<UUID, Object>();
    private final Set<UUID> temp = new HashSet<UUID>();

    public ResourcePool() {
    }

    /*public UUID putResource(Object obj, boolean isTemporary){
         UUID id = UUID.randomUUID();
         map.put(id, obj);
         if(isTemporary){
             temp.add(id);
         }
         return id;
     }*/

    public UUID putResource(UUID id, Object obj, boolean isTemporary) {
        map.put(id, obj);
        if (isTemporary) {
            temp.add(id);
        }
        return id;
    }

    public boolean setResource(UUID id, Object obj) {
        boolean result = false;
        if (map.containsKey(id)) {
            result = true;
        }
        map.put(id, obj);
        temp.add(id);
        return result;
    }

    public void clearTemporary() {
        map.keySet().removeAll(temp);
        temp.clear();
    }

    public void clear() {
        map.clear();
        temp.clear();
    }

    public Object getResource(UUID id) {
        return map.get(id);
    }

    /*public Object[] getResources(UUID [] ids){
         Object [] result = new Object[ids.length];
         for(int i = 0; i < ids.length; i++){
             result[i] = map.get(ids[i]);
         }
         return result;
     }*/
}