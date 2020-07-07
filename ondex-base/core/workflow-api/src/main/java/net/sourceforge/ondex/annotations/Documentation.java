package net.sourceforge.ondex.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the URL of the documentation for this producer
 *
 * @author hindlem
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Documentation {

    /**
     * @return the url with the location of the documentation
     */
    public abstract String url();

}