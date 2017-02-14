/*
 * Created on 25-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg52.data;

import com.sleepycat.persist.model.Persistent;

import java.io.Serializable;

/**
 * @author taubertj
 */
@Persistent
public class Graphics implements Serializable {

    /**
     * Default serial version unique id
     */
    private static final long serialVersionUID = 1L;

    private String name;
    private String x;
    private String y;
    private String type;
    private String width;
    private String height;
    private String fgcolor;
    private String bgcolor;

    public Graphics() {
    }

    public String getBgcolor() {
        return bgcolor;
    }

    public void setBgcolor(String bgcolor) {
        this.bgcolor = bgcolor;
    }

    public String getFgcolor() {
        return fgcolor;
    }

    public void setFgcolor(String fgcolor) {
        this.fgcolor = fgcolor;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

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
        this.type = type.intern();
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }
}
