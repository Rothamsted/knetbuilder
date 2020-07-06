package net.sourceforge.ondex.tools.functions;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
/**
 * 
 * @author lysenkoa
 *
 * @param <K>
 */
public class FrequencyMapBuilder<K extends Object> {
	private Map<K, Integer> map;
	
	public FrequencyMapBuilder(Map<K, Integer> map){
		this.map = map;
	}
	
	public void setMap(Map<K, Integer> map){
		this.map = map;	
	}
	
	public void addEntry(K k){
		Integer n = map.get(k);
		if(n == null){
			n = 0;
		}
		map.put(k, ++n);
	}
	
	public Map<K, Integer> getFrequencyCounts(){
		return map;
	}
	
	public Map<Integer, List<K>> getReverseMap(){
		CategoryMapBuilder<Integer, K> cat = new CategoryMapBuilder<Integer, K>(new HashMap<Integer, List<K>>());	
		for(Entry<K, Integer> ent:map.entrySet())
			cat.addEntry(ent.getValue(), ent.getKey());
		return cat.getCategoryMap();
	}
	
	public void clear(){
		map.clear();
	}
}
