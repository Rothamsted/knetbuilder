package net.sourceforge.ondex.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicating that if this plug should be in the webServices.
 *
 * Should only be used to override the default action based on the Status.
 *
 * So only use if Experimental, Discontinued plugins should be included in the Webservices,
 *     or for Plugins that should not be included no matter what their Status is or becomes.
 * <P>
 * The default conditionaL include is used in all cases where the decision to include a plugin in the webservices
 *    is deferred to the Status.
 * Description should state when and why the default behaviour is to be overridden..
 * @author Christian
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Webservice {
	
	/**
	 * 
	 * @return the value of this annotation
	 */
	String description() default  "No Webservice annotation found.";
	
	/**
	 * @return the include instruction of this annotation
	 */
	IncludeType include() default IncludeType.CONDITIONAL;
		
}
