package net.sourceforge.ondex.export.oxl;

import net.sourceforge.ondex.core.util.Holder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;

/**
 * Utility bean to wrap up list values.
 *
 * @author hindlem
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "rgb_color")
public class ColorHolder implements Holder<Color> {

    /**
     * Empty constructor for JAXB
     */
    public ColorHolder() {
    }

    @XmlAttribute
    private int rgbColor;

    @Override
    public void setValue(Color v) {
        rgbColor = v.getRGB();
    }

    @Override
    public Color getValue() {
        return new Color(rgbColor);
    }

}
