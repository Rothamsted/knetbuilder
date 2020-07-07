package net.sourceforge.ondex.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the custodians of this producer
 *
 * @author hindlem
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Custodians {

    /**
     * @return the custodians of this producer
     */
    public abstract String[] custodians();

    /**
     * @return @return the e-mail addresses of the custodians of this producer in order of authors
     */
    public abstract String[] emails();

}