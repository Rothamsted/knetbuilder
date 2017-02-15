package net.sourceforge.ondex.export.oxl;

import net.sourceforge.ondex.core.util.Holder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

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
