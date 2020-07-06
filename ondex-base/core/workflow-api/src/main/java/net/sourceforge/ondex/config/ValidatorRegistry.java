package net.sourceforge.ondex.config;

import java.util.Hashtable;

import net.sourceforge.ondex.validator.AbstractONDEXValidator;

/**
 * Registers global ONDEXListeners and AbstractONDEXGraphs.
 * 
 * @author taubertj
 * 
 */
public class ValidatorRegistry {

	/**
	 * Contains all initialise AbstractONDEXValidators indexed by name.
	 */
	public static Hashtable<String, AbstractONDEXValidator> validators = new Hashtable<String, AbstractONDEXValidator>();	

}
