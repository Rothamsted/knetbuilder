package net.sourceforge.ondex.parser.metacyc.objects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
/**
 * Factory class for creating sink objects and managing them. You musnt't create
 * instances of this class, use instead the static getInstance() method.
 * 
 * @author robert
 */
public class SinkFactory implements Iterable<AbstractNode>{
	private static SinkFactory instance;
	
	private SinkFactory() {
		clearCache();
	}

	/**
	 * returns a SinkFactory instance
	 * @return SinkFactory
	 */
	public static SinkFactory getInstance() {
		if (instance == null)
			instance = new SinkFactory();
		return instance;
	}
	//UniqueID -> Node
	private HashMap<String, AbstractNode> registry;

	/**
	 * clears the intern sink object cache
	 */
	public void clearCache() {
		registry =  new HashMap<String, AbstractNode>();
	}
	/**
	 * register a AbstractNode
	 * @param node
	 */
	private void register(AbstractNode node) {
		this.registry.put(node.getUniqueId(), node);
	}
	
	/**
	 * 
	 * @param uniqueId the id to check for
	 * @return if this contains the specified id
	 */
	public boolean containsUniqueId(String uniqueId) {
		return registry.containsKey(uniqueId);
	}
	
	/**
	 * finds a sink object by its uniqueId or throws a NoSuchElementException
	 * 
	 * @param uniqueId
	 * @throws NoSuchElementException
	 * @return AbstractNode
	 */
	public AbstractNode findByUniqueId(String uniqueId) throws NoSuchElementException {
		AbstractNode node = registry.get(uniqueId);
		if (node == null)
			throw new NoSuchElementException();
		return node;
	}

	/**
	 * creates a new sink object or returns a sink object with the same
	 * uniqueId. You have to pass the destination class and an uniqueId
	 * 
	 * @param className  defines the type
	 * @param uniqueId   for the sink object
	 * @throws Exception 
	 * @return AbstractNode an object with given type and uniqueId
	 */
	public AbstractNode create(Class<? extends AbstractNode> className, String uniqueId)
			throws Exception {
		//tries to find the uniqueId in the registry
		if (registry.get(uniqueId) != null){
			if (! className.getSimpleName().equals(registry.get(uniqueId).getClass().getSimpleName()) ){
				
				
				if (Protein.class.isAssignableFrom(className) 
						&& registry.get(uniqueId) instanceof Compound) {
					//override compound with protein
				} else if (Compound.class.isAssignableFrom(className) 
						&& registry.get(uniqueId) instanceof Protein) {	
					return registry.get(uniqueId); //overwrite compound with protein
				} else {
					throw new Exception("uniqueId was already given to another sink class type :"+registry.get(uniqueId).getClass().getSimpleName()+" to "+className.getSimpleName()+" for "+uniqueId);
				}
			} else {
				return registry.get(uniqueId);
			}
		}
		//checks the constructor
		if (className.getConstructors().length != 1 || className.getConstructors()[0].getParameterTypes().length != 0){
			throw new Exception("Sink object constructor doesn't match. Caused by:" + className);
		}
		//creates the sink object using reflecting
		AbstractNode node = (AbstractNode) className.getConstructors()[0]
				.newInstance((Object[]) null);
		node.setUniqueId(uniqueId);
		this.register(node);
		return node;
	}

	/**
	 * Iterates over all values
	 * @return Iterator<AbstractNode>
	 */
	public Iterator<AbstractNode> iterator() {
		return this.registry.values().iterator();
	}
	
	/**
	 * Iterates over all values with given type
	 * @param type the sink type
	 * @return the Iterator<AbstractNode> for the given type
	 */
	public Iterator<AbstractNode> typeIterator(String type) {
		return new TypeIterator(type);
	}
	/**
	 * Represents a iterator for a given type
	 * @author peschr
	 * TODO find a better solution -slightly inefficient
	 */
	class TypeIterator implements Iterator<AbstractNode>{
		private HashSet<AbstractNode> typeRegistry;
		private Iterator<AbstractNode> iterator;
		/**
		 * initialises the type iterator
		 * @param type the iterator  type
		 */
		public TypeIterator(String type){
			typeRegistry = new  HashSet<AbstractNode>();
			Iterator<AbstractNode> i = SinkFactory.this.registry.values().iterator();
			while(i.hasNext()){
				AbstractNode node = i.next();
				if ( node.getClass().getSimpleName().equals(type)){
					typeRegistry.add(node);
				}
			}
			iterator = typeRegistry.iterator();
		}
		/**
		 * @return boolean
		 */
		public boolean hasNext() {
			return iterator.hasNext();
		}
		/**
		 *	Unsupported operation
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
		/**
		 * @return AbstractNode next value
		 */
		public AbstractNode next() {
			return iterator.next();
		}
		
	}
}
