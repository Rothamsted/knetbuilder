package net.sourceforge.ondex.annotations.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author hindlem
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConceptClassRequired {

	/**
	 * 
	 * @return the ids of the metadata of the specified type
	 */
	String[] ids();
	
}
