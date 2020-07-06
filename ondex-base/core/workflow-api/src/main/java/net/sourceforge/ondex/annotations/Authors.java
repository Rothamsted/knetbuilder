package net.sourceforge.ondex.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the authors of this producer
 *
 * @author hindlem
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Authors {

    /**
     * @return the authors of this producer
     */
    String[] authors();

    /**
     * @return the e-mail addresses of the authors of this producer in order of authors
     */
    String[] emails();

}
