package net.sourceforge.ondex.producer;

import net.sourceforge.ondex.AbstractONDEXPlugin;
import net.sourceforge.ondex.ONDEXPlugin;

/**
 * This class is a template for a type of Ondex plugins that can construct new
 * Objects to be used by other modules in the workflow. 
 * 
 * @author lysenkoa
 *
 */
public abstract class ProducerONDEXPlugin extends AbstractONDEXPlugin implements ONDEXPlugin
{
	//Holds the Objects created by this producer
	protected Object [] results = null;
	//Holds the types of the objects created by this producer
	protected Class<?> [] resultTypes = null;
	
	/**
	 * This method should return any objects that where created by the producer. It will delegate to the
	 * start method and the start method should create and fill Object [] results protected variable
	 * if there are objects produced by this producer. If nothing was created the array of size 0 should be returned;
	 * 
	 * The contract on this method requires that the protected variable resultTypes[] is initialized 
	 * by the extending class
	 * 
	 * @return an array of Object[]
	 * 
	 * @throws Exception
	 */
	public Object[] collectResults() throws Exception{
		if(results == null){
			start();
		}
		if(resultTypes == null || results.length != resultTypes.length){
			throw new Exception("Return types must be declared");
		}
		for(int i = 0; i < resultTypes.length;i++){
			if(results[i] != null && !resultTypes[i].isInstance(results[i])){
				throw new Exception("Plug-in "+this.getClass().getCanonicalName()+" declares to return "+resultTypes[i].getCanonicalName()+" at position "+ i +" (got "+results[i].getClass().getCanonicalName()+")");
			}
		}

		return results;
	}

	public Class<?>[] getResultTypes(){
		return resultTypes;
	}

}

