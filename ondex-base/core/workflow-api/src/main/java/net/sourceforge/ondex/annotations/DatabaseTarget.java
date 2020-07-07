package net.sourceforge.ondex.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes the database and version that a parser (or producer) is designed to work against
 * @author hindlem
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DatabaseTarget {

	/**
	 * The name of the database
	 * @return
	 */
	String name();
	
	/**
	 * The url of the main page of the database (not the file locations)
	 * @return
	 */
	String url() default  "";
	
	/**
	 * A short description of the database
	 * @return
	 */
	String description() default  "";
	
	/**
	 * 
	 * @return the version or versions this database the parser is tested to work against
	 */
	String[] version();
	
}
