package net.sourceforge.ondex.scripting.wrappers;

/**
 * An interface container to swap out blocks of code
 *
 * @author lysenkoa
 */
public interface ContextualReferenceResolver<V extends Object> {

    public V resolveRef(Object arg) throws Exception;
}
