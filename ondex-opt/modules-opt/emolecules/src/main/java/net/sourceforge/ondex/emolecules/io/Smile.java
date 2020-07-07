package net.sourceforge.ondex.emolecules.io;

import java.io.Serializable;

/**
 *
 * @author grzebyta
 */
public class Smile implements Serializable {
    private static final long serialVersionUID = 3367958709001736353L;
    
    private String smile;
    private Long parent;
    private Long id;

    public String getSmile() {
        return smile;
    }

    public void setSmile(String smile) {
        this.smile = smile;
    }

    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.smile != null ? this.smile.hashCode() : 0);
        hash = 71 * hash + (this.parent != null ? this.parent.hashCode() : 0);
        hash = 71 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Smile other = (Smile) obj;
        if ((this.smile == null) ? (other.smile != null) : !this.smile.equals(other.smile)) {
            return false;
        }
        if (this.parent != other.parent && (this.parent == null || !this.parent.equals(other.parent))) {
            return false;
        }
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
}
