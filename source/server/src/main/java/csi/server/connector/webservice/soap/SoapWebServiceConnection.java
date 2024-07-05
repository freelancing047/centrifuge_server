package csi.server.connector.webservice.soap;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.connector.webservice.WebServiceConnection;
import csi.server.connector.webservice.WebServiceResultSet;

public class SoapWebServiceConnection extends WebServiceConnection {
   @Override
   public ResultSet execute(String xslPath, List<QueryParameterDef> params, String query) throws Throwable {
      try {
         String xmldata = query;
         String useXslPath = xslPath;

         // remove extraneous SQL artifacts
         xmldata = xmldata.replace("'", "");

         if (xmldata.contains(")")) {
            xmldata = xmldata.substring(0, xmldata.indexOf(")"));
         }
         if (!useXslPath.startsWith("http")) {
            useXslPath = useXslPath.substring(useXslPath.indexOf("(") + 1);
         }

         // Convert String to XML DOM
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document xmldoc = builder.parse(new InputSource(new StringReader(xmldata)));

         // First create the connection
         SOAPConnectionFactory soapConnFactory = SOAPConnectionFactory.newInstance();
         SOAPConnection connection = soapConnFactory.createConnection();

         // Next, create the actual message
         MessageFactory messageFactory = MessageFactory.newInstance();
         SOAPMessage message = messageFactory.createMessage();

         SOAPPart soapPart = message.getSOAPPart();
         DOMSource domSource = new DOMSource(xmldoc);
         soapPart.setContent(domSource);

         int anchor = wsdlURL.toString().indexOf('?');
         // Web Service Endpoint
         String destination = wsdlURL.toString().substring(0, anchor);
         // Username and Password
         String auth = wsdlURL.toString().substring(anchor + 1);

         // Set Auth in HTTP Headers
         MimeHeaders hd = message.getMimeHeaders();
         String authorization = Base64.getEncoder().encodeToString(auth.getBytes());
         hd.addHeader("Authorization", "Basic " + authorization);
         message.saveChanges();

         // Get the reply
         SOAPMessage reply = connection.call(message, destination);

         // Use xslt to transform the response payload
         Writer outWriter = new StringWriter();
         Source xslSource = new StreamSource(useXslPath);
         Result result = new StreamResult(outWriter);

         // Create the transformer
         TransformerFactory transformerFactory = new com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl();
//            TransformerFactory transformerFactory = TransformerFactory.newInstance();
         Templates template = transformerFactory.newTemplates(xslSource);
         Transformer transformer = template.newTransformer();

         // Perform the transformation
         transformer.transform(reply.getSOAPPart().getContent(), result);

         DocumentBuilder xslBuilder = factory.newDocumentBuilder();
         Document recordsDoc = xslBuilder.parse(new InputSource(new StringReader(outWriter.toString())));

         // TODO - temporary, perhaps take this as an input parameter rather than
         // hardcode the "Row"
         // element as the designation for each individual record.
         NodeList records = recordsDoc.getElementsByTagName("Row");

         // Needs to be returned as a ResultSet to be inserted into the cache
         WebServiceResultSet resultSet = new WebServiceResultSet(records);

         return resultSet;
      } catch (Throwable t) {
         throw new CentrifugeException("Failed to execute web service", t);
      }
   }
}
