package net.sourceforge.ondex.core.util;

import javax.xml.bind.JAXBException;

/**
 * @author hindlem
 * @param <Value> the type of object held by the holder
 */
public interface Holder<Value> {

    public void setValue(Value v);

    public Value getValue() throws JAXBException;

}
