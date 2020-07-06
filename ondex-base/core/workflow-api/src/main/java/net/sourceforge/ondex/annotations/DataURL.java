package net.sourceforge.ondex.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a group of files at a location needed by this parser
 * @author hindlem
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DataURL {

	/**
	* @return name identifier for this file set
	 */
	String name();
	
	/**
	* @return describe the file set
	 */
	String description() default  "";
	
	/**
	* @return the url/s for the file/s or dir/s
	 */
	String[] urls();
	
}
