package net.sourceforge.ondex.workflow.model;

import net.sourceforge.ondex.workflow.exceptions.ReplicatedValueException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hindlem
 *         Created 06-Apr-2010 15:04:03
 */
public class GraphInit {

    public static final String BERKELEY = "berkeley";
    public static final String MEMORY = "memory";
    public static final String SQL = "sql";
    public static final String SQL2 = "sql2";
    public static final String SQL3 = "sql3";
    public static final String TRIPLESTORE = "triplestore";

    private String name;
    private String type = BERKELEY; //default
    private Map<String, String> options = new HashMap<String, String>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addOption(String name, String value) throws ReplicatedValueException {
        if (options.put(name, value) != null) {
            throw new ReplicatedValueException(name + " is declared more than once");
        }
    }

    public Map<String, String> getOptions() {
        return options;
    }
}
