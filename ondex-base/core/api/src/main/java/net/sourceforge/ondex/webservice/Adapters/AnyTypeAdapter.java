/*
 * Adapter to allow an interface to to used as a parameter or return in a webservice.
 *
 * Same as: com.sun.xml.bind.AnyTypeAdapter
 *
 * Does nothing more than pass the Object through.
 *
 * Note: The use of type specific adapter was not possible
 * due to the split between projects.
 * The Interface needs a reference to the adapter,
 * which would need a reference to the implementing class.
 * This would only be possible if all three interface, implementor and apadter
 * where placed in the same Maven project.
 * 
 */
package net.sourceforge.ondex.webservice.Adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Christian Brenninkmeijer
 *
 * Taken from: https://jaxb.dev.java.net/guide/Mapping_interfaces.html
 */
public class AnyTypeAdapter extends XmlAdapter<Object,Object>{

    @Override
    public Object unmarshal(Object v) { return v; }

    @Override
    public Object marshal(Object v) { return v; }
  }

