package net.sourceforge.ondex.marshal;

import com.thoughtworks.xstream.XStream;

/**
 * Main Utils class to support conversion of Objects to XML and back.
 *
 * Xstream used purely as an example.
 * Feel free to update to a more suitable method.
 *
 * @author Christian Brennikmeijer
 */
public class Marshaller {

    private static Marshaller marshaller = null;
    //To be replaced with better option.
    private static XStream xstream = new XStream();

    private Marshaller(){

    }

    public static Marshaller getMarshaller(){
        if (marshaller == null){
            marshaller = new Marshaller();
        }
        return marshaller;
    }
    public String toXML(Object object){
        return xstream.toXML(object);
    }

    public Object fromXML(String xml){
        return xstream.fromXML(xml);
    }
}
