package net.sourceforge.ondex.tools.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * 
 * @author lysenkoa
 *
 * @param <K>
 * @param <V>
 */
public class CategoryMapBuilder<K extends Object, V extends Object> {
	private Map<K, List<V>> map;
	
	public CategoryMapBuilder(Map<K, List<V>> map){
		this.map = map;
	}
	
	public void setMap(Map<K, List<V>> map){
		this.map = map;	
	}
	
	public void addEntry(K k, V v){
		List<V> n = map.get(k);
		if(n == null){
			n = new ArrayList<V>();
			map.put(k, n);
		}
		n.add(v);
	}
	
	public Map<K, List<V>> getCategoryMap(){
		return map;
	}
	
	public void clear(){
		map.clear();
	}
}