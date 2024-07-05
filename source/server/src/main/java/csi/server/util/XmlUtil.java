package csi.server.util;

import java.io.OutputStream;
import java.io.Writer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class XmlUtil {

    private static XMLOutputFactory xmlOutFactory = XMLOutputFactory.newInstance();

    public static XMLStreamWriter createXMLStreamWriter(OutputStream os) throws XMLStreamException {
        return xmlOutFactory.createXMLStreamWriter(os);
    }

    public static XMLStreamWriter createXMLStreamWriter(Writer writer) throws XMLStreamException {
        return xmlOutFactory.createXMLStreamWriter(writer);
    }

}
