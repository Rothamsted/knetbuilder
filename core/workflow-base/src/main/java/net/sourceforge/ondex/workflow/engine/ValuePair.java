package net.sourceforge.ondex.workflow.engine;

import java.util.Map.Entry;

/**
 * @author lysenkoa
 */
public class ValuePair<Z extends Object, T extends Object> implements Entry<Z, T> {
    private Z key;
    private T value;

    public ValuePair(Z key, T value) {
        this.key = key;
        this.value = value;
    }

    public Z getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    public T setValue(T value) {
        T oldValue = this.value;
        this.value = value;
        return oldValue;
    }
}