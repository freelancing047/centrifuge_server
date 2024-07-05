package csi.server.connector.webservice;

/**
 * Entry point for obtaining data from executing a web service operation that
 * simulates JDBC.
 * <p>
 * This is a temporary approach. Longer term Centrifuge Server will support
 * hierarchical/networked data by default. Tabular data is a simple case of
 * hierarchical where each row is represented as a node with simple properties.
 *
 * @author Centrifuge Systems, Inc.
 *
 */
public class WebServiceConnectionFactory extends AbstractWebServiceConnectionFactory {
   public static final String CENTRIFUGE_TEMPLATE = "ws:centrifuge:";
   public static final String STANDARD_TEMPLATE = "ws:standard:";

   {
      this.template = STANDARD_TEMPLATE;
   }

   @Override
   protected WebServiceConnection createConnection() {
      return new WebServiceConnection();
   }
}
