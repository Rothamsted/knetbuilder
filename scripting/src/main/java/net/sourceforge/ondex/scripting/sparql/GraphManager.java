package net.sourceforge.ondex.scripting.sparql;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.tools.ondex.MdHelper;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
/**
 * Handles creation of concepts and relations and their attributes
 * Manages the default values for properties and indexes concepts to URIs.
 * 
 * @author lysenkoa
 *
 */
public class GraphManager {
	private final Map<ONDEXGraph, Map<String, ONDEXConcept>> index = new HashMap<ONDEXGraph, Map<String, ONDEXConcept>>();
	private final Set<String> prefixes = new HashSet<String>();
	private Map<String, ONDEXConcept> selectedIndex;
	private ONDEXGraph graph;
	private String myEndPoint = "IMPD";
	private static Map<String, Setter> setters;
	private ConceptClass obj;
	private AttributeName uriAtt;
	private AttributeName typeUriAtt;
	private EvidenceType ev;
	private DataSource dataSource;
	
	private BitSet cs = new BitSet();
	private BitSet rs = new BitSet();
	
	private static Setter getMethod(String prediate) {
		return setters.get(prediate);
	}
	
	public Map<String, ONDEXConcept> getIndex(){
		return selectedIndex;
	}
	
	public void updateIndex(){
		selectedIndex.clear();
		for(ONDEXConcept c : graph.getConceptsOfAttributeName(uriAtt)){
			selectedIndex.put(c.getAttribute(uriAtt).getValue().toString(), c);
		}
	}

	public void startProcessing(){
		cs = new BitSet();
		rs = new BitSet();
	}
	
	public BitSet[] finishProcessing(){
		BitSet [] bits = new BitSet[]{cs, rs};
		cs = new BitSet();
		rs = new BitSet();
		return bits;
	}
	
	public void setGraph(ONDEXGraph graph){
		this.graph = graph;
		selectedIndex = index.get(graph);
		if (selectedIndex == null) {
			selectedIndex = new HashMap<String, ONDEXConcept>();
			AttributeName uriAtt = MdHelper.createAttName(graph, "URI", String.class);
			for(ONDEXConcept c : graph.getConceptsOfAttributeName(uriAtt)){
				selectedIndex.put(c.getAttribute(uriAtt).getValue().toString(), c);
			}
			index.put(graph, selectedIndex);
			for(ONDEXConcept c : graph.getConceptsOfAttributeName(uriAtt)){
				selectedIndex.put(c.getAttribute(uriAtt).toString(), c);
			}
		}
		obj = MdHelper.createCC(graph, "Object");
		uriAtt = MdHelper.createAttName(graph, "URI", String.class);
		typeUriAtt = MdHelper.createAttName(graph, "TYPE_URI", String.class);
		ev = MdHelper.createEvidence(graph, "IMPD");
	}
	
	

	public void addPrefix(String prefix){
		prefixes.add(prefix);
	}
	
	public void addPrefixs(Collection<String> prefixs){
		prefixes.addAll(prefixs);
	}
	
	public void removePrefixs(Collection<String> prefixs){
		prefixes.removeAll(prefixs);
	}
	
	public void removePrefix(String prefix){
		prefixes.remove(prefix);
	}

	
	{	Class<ONDEXConcept> conC = ONDEXConcept.class;
		setters = new HashMap<String, Setter>();
		try {
			setters.put("annotation", new MethodSetter(conC.getMethod("setAnnotation", String.class)));
			setters.put("conceptAccession", new Setter() {
				@Override
				public void set(ONDEXConcept concept, Object value) throws InvocationTargetException, IllegalAccessException
				{
					if(concept.getConceptAccession((String) value, concept.getElementOf()) == null){
						concept.createConceptAccession((String) value, concept.getElementOf(), false);	
					}
				}
			});
			setters.put("conceptName", new Setter() {
				@Override
				public void set(ONDEXConcept concept, Object value) throws InvocationTargetException, IllegalAccessException
				{
					if(concept.getConceptName((String) value) == null){
						concept.createConceptName((String) value, true);
					}
				}
			});
			setters.put("conceptDescription", new MethodSetter(conC.getMethod("setDescription", String.class)));
			//ofType?
			setters.put("pid", new MethodSetter(conC.getMethod("setPID", String.class)));
		}
		catch (NoSuchMethodException e) {
			throw new Error(e); // think of a better error type
		}
	}
	
	public void setEndpointURI(String uri){
		this.myEndPoint = uri;
		dataSource = MdHelper.createDataSource(graph, myEndPoint);
	}
	
	private interface Setter {
		public void set(ONDEXConcept concept, Object value) throws InvocationTargetException, IllegalAccessException;
	}
	
	private class MethodSetter implements Setter {
		private final Method method;

		private MethodSetter(Method method)
		{
			this.method = method;
		}

		@Override
		public void set(ONDEXConcept concept, Object value) throws InvocationTargetException, IllegalAccessException
		{
			method.invoke(concept, value);
		}
	}
	
	public ConceptClass makeConceptClass(List<String> types) {
		if(types.size()==0)
			return obj;
		String[] typeNamesForOndex=new String[types.size()];
		int i=0;
		for(String typeURI : types) {
			Resource res=ResourceFactory.createResource(typeURI);
			typeNamesForOndex[i++]=res.getLocalName();
		}
		Arrays.sort(typeNamesForOndex);
		StringBuilder sb = new StringBuilder();
		sb.append(typeNamesForOndex[0]);
		for(int j = 1; j < typeNamesForOndex.length;j++){
			sb.append(":");
			sb.append(typeNamesForOndex[j]);
		}
		if(sb.toString().isEmpty()){
			return MdHelper.createCC(graph, "Object");	
		}
		return MdHelper.createCC(graph, sb.toString());	
	}
	
	/**
	 * Get a concept from the URI resource. Will return existing concept, if found or create a new one if not.
	 *
	 * @param n             - Jena node
	 * @param selectedIndex - index of String uris to concepts
	 * @param graph         - Ondex graph
	 * @return Ondex concept
	 */
	
	public String recoverURI(Node n){
		if (n.isURI()) {
			return n.getURI();
		}
		else if (n.isBlank()) {
			return myEndPoint + "/" +n.getBlankNodeId().getLabelString();
		}
		else return null;
	}
	
	/**
	 * Checks the index and returns the existing concept if present
	 * @param n
	 * @return
	 */
	public ONDEXConcept getExistingConcept(Node n){
		String uri = recoverURI(n);
		if(uri == null){
			return null;
		}
		ONDEXConcept result = selectedIndex.get(uri);
		if(result != null){
			cs.set(result.getId());	
		}
		return result;
	}
	
	/**
	 * Creates concept from the node
	 * 
	 * @param n
	 * @param obj
	 * @param uriAtt
	 * @param ev
	 * @param dataSource
	 * @param types
	 * @return
	 */
	public ONDEXConcept createConceptForNode(Node n, ConceptClass obj, AttributeName uriAtt, EvidenceType ev, DataSource dataSource, List<String>  types) {
		String uri = recoverURI(n);
		ONDEXConcept result = selectedIndex.get(uri);
		if(result == null){
			ConceptClass cc = (obj == null)?this.obj:obj;	
			uriAtt  = (uriAtt == null)?this.uriAtt:uriAtt;
			ev = (ev == null)?this.ev:ev;
			dataSource = (dataSource == null)?this.dataSource:dataSource;
			
			String name = null;
			
			boolean checkForTypes = false;
			if (n.isURI()) {
				name = n.getLocalName();
				checkForTypes = true; 
			} else if (n.isBlank()) {
				name = n.getBlankNodeId().getLabelString();
			}
			if (name == null || name.length() == 0) {
				name = uri;
			}
			
			if(checkForTypes){
				if(types.size() > 0) {
					cc = makeConceptClass(types);
				}
			}

			result = graph.getFactory().createConcept(name, dataSource, cc, ev);
			result.createConceptName(name, true);
			result.createAttribute(uriAtt, uri, false);
			selectedIndex.put(uri, result);
		}
		cs.set(result.getId());
		return result;	
	}
	
	public ONDEXConcept createConceptForNode(Node n, List<String>  types) {
		return createConceptForNode(n, obj, uriAtt, ev, dataSource, types);	
	}
	
	/**
	 * Adds literal as an attribute to a concept
	 *
	 * @param node
	 * @param predicate
	 * @param c
	 */
	public void addAttribute(ONDEXConcept c, Node node, String predicate)throws InvocationTargetException, IllegalAccessException{
		System.err.println("Processing literal for: " + predicate);
		Setter setter = getMethod(predicate);
		if(setter != null) {
			setter.set(c, node.getLiteralValue());
		} else {
			addAttribute(c, predicate, node.getLiteralValue());
		}
	}
	
	/**
	 * This method will attempt to detect the class from the one of the Object
	 * However this is computationally expensive and can lead to complications
	 * if the type was guessed incorrectly.
	 * 
	 * @param ent
	 * @param predicate
	 * @param value
	 */
	public void addAttribute(ONDEXEntity ent, String predicate, Object value){
		System.err.println(value+" :: "+value.getClass().getCanonicalName());
		ONDEXGraphMetaData md = graph.getMetaData();
		AttributeName att = md.getAttributeName(predicate);
		Class<?> clsTarget = null;
		Object assignableValue = value;
		if(att != null){
			clsTarget = att.getDataType();
			Class<?> clsObject = value.getClass();
			if(!clsTarget.isAssignableFrom(clsObject)){
				if(clsTarget.equals(String.class)){
					assignableValue = value.toString();	
				}
				if(clsTarget.equals(Double.class)){
					try{
						assignableValue = Double.valueOf(value.toString());
					}
					catch(RuntimeException e){
						return;
					}
				}
				else if(clsTarget.equals(Integer.class)){
					try{
						assignableValue = Integer.valueOf(value.toString());
					}
					catch(RuntimeException e){
						return;
					}
				}
				else if(clsTarget.equals(Boolean.class)){
					try{
						assignableValue = Boolean.valueOf(value.toString());
					}
					catch(RuntimeException e){
						return;
					}
				}
			}
			Integer counter = 0;
			while (true){
				Attribute attribute = ent.getAttribute(att);
				if(attribute != null){
					if(attribute.getValue().equals(assignableValue)){
						return;
					}	
				}
				else{
					break;
				}
				att = md.getFactory().createAttributeName(predicate + ":" + (counter++).toString(), clsTarget);
			}
			ent.createAttribute(att, assignableValue, false);
		}
		else {
			Class<?> clsObject = value.getClass();
			if(clsObject.equals(String.class) || clsObject.equals(Double.class) || clsObject.equals(Integer.class) || clsObject.equals(Boolean.class)){
				clsTarget = clsObject;
			}
			else if(clsObject.isPrimitive()){
				if(clsObject.equals(int.class)){
					clsTarget = Integer.class;	
				}
				else if(clsObject.equals(double.class)){
					clsTarget = Double.class;		
				}
				else if(clsObject.equals(boolean.class)){
					clsTarget = Boolean.class;		
				}
			}
			else{
				clsTarget = String.class;
				assignableValue = value.toString();
			}
			System.err.println(assignableValue+" || "+assignableValue.getClass().getCanonicalName());
			att = md.getFactory().createAttributeName(predicate, clsTarget);
			ent.createAttribute(att, assignableValue, false);	
		}
	}
	
	/**
	 * Use this method if the class of the attribute is known
	 * 
	 * @param ent
	 * @param predicate
	 * @param value
	 * @param cls
	 */
	public void addAttribute(ONDEXEntity ent, String predicate, Object value, Class cls){
		ONDEXGraphMetaData md = graph.getMetaData();
		AttributeName att = md.getAttributeName(predicate);
		Class<?> clsTarget = null;
		Object assignableValue = null;
		if(att != null){
			clsTarget = att.getDataType();
			if(!clsTarget.isAssignableFrom(cls)){
				return;
			}
			Class<?> clsObject = value.getClass();
			if(!clsTarget.isAssignableFrom(clsObject)){
				if(clsTarget.equals(String.class)){
					assignableValue = value.toString();	
				}
				if(clsTarget.equals(Double.class)){
					try{
						assignableValue = Double.valueOf(value.toString());
					}
					catch(RuntimeException e){
						return;
					}
				}
				else if(clsTarget.equals(Integer.class)){
					try{
						assignableValue = Integer.valueOf(value.toString());
					}
					catch(RuntimeException e){
						return;
					}
				}
				else if(clsTarget.equals(Boolean.class)){
					try{
						assignableValue = Boolean.valueOf(value.toString());
					}
					catch(RuntimeException e){
						return;
					}
				}
			}
			Integer counter = 0;
			while (true){
				Attribute attribute = ent.getAttribute(att);
				if(attribute != null){
					if(attribute.getValue().equals(assignableValue)){
						return;
					}	
				}
				else{
					break;
				}
				att = md.getFactory().createAttributeName(predicate + ":" + (counter++).toString(), clsTarget);
			}
			ent.createAttribute(att, assignableValue, false);
		}
		else{
			att = md.getFactory().createAttributeName(predicate, cls);
			ent.createAttribute(att, assignableValue, false);	
		}
	}

	/**
	 * 
	 * @param from
	 * @param to
	 * @param p
	 * @param ev
	 * @param rs
	 * @return
	 */
	public ONDEXRelation createRelation(ONDEXConcept from, ONDEXConcept to, Node p, EvidenceType ev) {
		ev = (ev == null)?this.ev:ev;
		String typeId = p.getLocalName();
		if (typeId == null || typeId.length() == 0) {
			typeId = p.getURI();
		}
		String uri = p.getURI();
		if(uri == null || uri.length() == 0){
			uri = this.myEndPoint +"/"+typeId;
		}
		RelationType rt = MdHelper.createRT(graph, typeId);
		ONDEXRelation r = graph.getRelation(from, to, rt);
		if(r == null){
			r = graph.getFactory().createRelation(from, to, rt, ev);
			r.createAttribute(typeUriAtt, uri, false);
		}

		rs.set(r.getId());
		return r;
	}
	

	public ONDEXRelation createRelation(ONDEXConcept from, ONDEXConcept to, Node p) {
		return createRelation(from, to, p, ev);
	}
}
