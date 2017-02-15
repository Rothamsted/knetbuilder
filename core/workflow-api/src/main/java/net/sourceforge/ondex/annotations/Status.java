package net.sourceforge.ondex.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicates that this producer is no longer supported, and exists for legacy/reproducibility/comparison reasons. Value should state when and why it was discontinued.
 * @author hindlem
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Status {
	
	/**
	 * 
	 * @return the value of this annotation
	 */
	String description() default  "";
	
	/**
	 * 
	 * @return teh status of this annotation
	 */
	StatusType status() default StatusType.EXPERIMENTAL;
		
}
