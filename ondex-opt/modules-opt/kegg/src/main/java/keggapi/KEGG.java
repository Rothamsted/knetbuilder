/**
 * KEGG.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package keggapi;

public interface KEGG extends javax.xml.rpc.Service {
    public java.lang.String getKEGGPortAddress();

    public keggapi.KEGGPortType getKEGGPort() throws javax.xml.rpc.ServiceException;

    public keggapi.KEGGPortType getKEGGPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
