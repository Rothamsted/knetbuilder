package net.sourceforge.ondex.export.oxl;

import java.util.Set;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import net.sourceforge.ondex.core.util.Holder;

/**
 * Utility bean to wrap up set values.
 *
 * @author Matthew Pocock
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class SetHolder<E> implements Holder<Set<E>> {
    @XmlElementWrapper(name = "item")
    private Set<E> set;

    public SetHolder() {
    }

    public SetHolder(Set<E> set) {
        this.set = set;
    }


    public Set<E> getValue() {
        return set;
    }

    public void setValue(Set<E> list) {
        this.set = list;
    }

}
