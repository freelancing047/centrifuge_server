package csi.server.connector.webservice;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.NodeList;

import com.sun.tools.ws.processor.model.AbstractType;
import com.sun.tools.ws.processor.model.Block;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.Operation;
import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Request;
import com.sun.tools.ws.processor.model.java.JavaType;
import com.sun.tools.ws.wsdl.document.WSDLDocument;

import csi.server.common.model.query.QueryParameterDef;

public class WebServiceConnection implements Connection {
   protected static final Logger LOG = LogManager.getLogger(WebServiceConnection.class);

    protected URL wsdlURL;

    protected WSDLDocument wsdl;

    protected Model model;

    protected Properties requestProps;

    protected Service theService;

    protected Class<?> inputMessage;

    protected Dispatch<SOAPMessage> dii;

    protected WebServiceConnection() {

    }

    public void setWSDLURL(URL url) {
        wsdlURL = url;
    }

    void setProperties(Properties props) {
        this.requestProps = props;
    }

    void validate() {

    }

    /*
     * java.sql.Connection Methods
     * 
     */

    public void clearWarnings() throws SQLException {
    }

    public void close() throws SQLException {

    }

    public void commit() throws SQLException {

    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {

        return null;
    }

    public Blob createBlob() throws SQLException {

        return null;
    }

    public Clob createClob() throws SQLException {

        return null;
    }

    public Statement createStatement() throws SQLException {

        return null;
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {

        return null;
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {

        return null;
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {

        return null;
    }

    public void setSchema(String schema) throws SQLException {

    }

    public String getSchema() throws SQLException {
        return null;
    }

    public void abort(Executor executor) throws SQLException {

    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

    }

    public int getNetworkTimeout() throws SQLException {
        return 0;
    }

    public boolean getAutoCommit() throws SQLException {

        return false;
    }

    public String getCatalog() throws SQLException {

        return null;
    }

    public Properties getClientInfo() throws SQLException {

        return null;
    }

    public String getClientInfo(String name) throws SQLException {

        return null;
    }

    public int getHoldability() throws SQLException {

        return 0;
    }

    public DatabaseMetaData getMetaData() throws SQLException {

        return null;
    }

    public int getTransactionIsolation() throws SQLException {

        return 0;
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {

        return null;
    }

    public SQLWarning getWarnings() throws SQLException {

        return null;
    }

    public boolean isClosed() throws SQLException {

        return false;
    }

    public boolean isReadOnly() throws SQLException {

        return false;
    }

    public boolean isValid(int timeout) throws SQLException {

        return false;
    }

    public String nativeSQL(String sql) throws SQLException {

        return null;
    }

    public CallableStatement prepareCall(String sql) throws SQLException {

        return null;
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {

        return null;
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {

        return null;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {

        return null;
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {

        return null;
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {

        return null;
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {

        return null;
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {

        return null;
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {

        return null;
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {

    }

    public void rollback() throws SQLException {

    }

    public void rollback(Savepoint savepoint) throws SQLException {

    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {

    }

    public void setCatalog(String catalog) throws SQLException {

    }

    public void setHoldability(int holdability) throws SQLException {

    }

    public void setReadOnly(boolean readOnly) throws SQLException {

    }

    public Savepoint setSavepoint() throws SQLException {

        return null;
    }

    public Savepoint setSavepoint(String name) throws SQLException {

        return null;
    }

    public void setTransactionIsolation(int level) throws SQLException {

    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {

    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {

        return false;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {

        return null;
    }

    public ResultSet execute(String operationName, List<QueryParameterDef> params, String query) throws Throwable {

        /*
         * with the given (parsed) wsdl model do the following:
         * 
         * get the first defined port ( assume that there's only one service _and_ port binding
         * for a given wsdl).
         * 
         * obtain the operation and determine what the JAXB msg class name is for binding our
         * parameters.
         * 
         * dynamically create an instance of the service's port then use DII in conjunction with JAXB 
         * for sending the operation request.
         * 
         * Although we import (and indirectly generate) the associated static Java classes, we'll treat the
         * response as a generic XML document.  Using the response document, we'll apply the query via XPath.
         * 
         * We make the assumption that the xpath query accounts for namespaces i.e. .//*::<RecordName> searches
         * the entire document for any element named <RecordName> in any namespace.  (There's not a very clean mechanism
         * to simply define a query if ns is present!).
         * 
         * Once we have the resulting list of Nodes that match the query, wrap it in an pseudo JDBC ResultSet for 
         * synchronization.  
         * 
         * NB:  the web service resultset provides minimal javax.sql.ResultSet support -- only strings
         * are supported at this time!!!
         * 
         * 
         */

        List<com.sun.tools.ws.processor.model.Service> services = model.getServices();
        com.sun.tools.ws.processor.model.Service service = services.get(0);
        Port port = service.getPorts().get(0);
        Operation operation = port.getOperationByUniqueName(operationName);

        Request request = operation.getRequest();

        Block wrappedBlock = request.getBodyBlocks().next();
        AbstractType type = wrappedBlock.getType();
        JavaType bodyType = type.getJavaType();
        String payloadClassName = bodyType.getFormalName();
        Class<?> payloadType = Class.forName(payloadClassName, true, Thread.currentThread().getContextClassLoader());
        Object payload = payloadType.newInstance();

        BeanInfo beanInfo = Introspector.getBeanInfo(payloadType, Object.class);
        if (params != null) {
            setParameters(beanInfo, payload, params);
        }

        JAXBContext binding = JAXBContext.newInstance(payloadType);
        MessageFactory msgFactory = MessageFactory.newInstance();
        SOAPMessage msg = msgFactory.createMessage();

        QName portName = port.getName();

        Service serviceImpl = Service.create(this.wsdlURL, service.getName());

        // Service concreteService = (Service)Class.forName( serviceStubName, true,
        // Thread.currentThread().getContextClassLoader() ).newInstance();

        // Method method = serviceImpl.getClass().getMethod( portGetterName, null );
        // Service concreteService = (Service)method.invoke( serviceImpl, null );
        Dispatch<SOAPMessage> dii = serviceImpl.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
        dii.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, operation.getSOAPAction());

        Marshaller marshaller = binding.createMarshaller();

        marshaller.marshal(payload, msg.getSOAPBody());

        SOAPMessage response = dii.invoke(msg);

        XPathFactory queryFactory = XPathFactory.newInstance();
        XPath xpath = queryFactory.newXPath();
        Object queryResult = xpath.evaluate(query, response.getSOAPBody(), XPathConstants.NODESET);
        if (queryResult instanceof NodeList) {
            NodeList records = (NodeList) queryResult;
            if (records.getLength() <= 0) {
               LOG.info("No results present in the response, or your query is incorrectly formed : " + query);
            }
            WebServiceResultSet resultSet = new WebServiceResultSet(records);
            return resultSet;
        } else {
            throw new RuntimeException("invalid.service.results");
        }

    }

    private void setParameters(BeanInfo beanInfo, Object payload, List<QueryParameterDef> params) throws Throwable {
        PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
        for (QueryParameterDef queryParameter : params) {
            String qpName = queryParameter.getName();
            for (int i = 0; i < props.length; i++) {
                if (props[i].getName().equals(qpName)) {
                    Method setter = props[i].getWriteMethod();
                    Object value = convert(queryParameter, setter.getParameterTypes()[0]);
                    Object[] args = { value };
                    setter.invoke(payload, args);
                    break;
                }
            }

        }
    }

    private Object convert(QueryParameterDef queryParameter, Class<?> target) throws IllegalArgumentException, InstantiationException, IllegalAccessException,
            InvocationTargetException, SecurityException, NoSuchMethodException {
        if (target == String.class) {
            return queryParameter.getValue();
        } else if (target.isPrimitive()) {
            Constructor<?> constructor = target.getConstructor(String.class);
            return constructor.newInstance(queryParameter.getValue());
        } else {
            return null;
        }
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    @Override
    public NClob createNClob() throws SQLException {

        return null;
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {

        return null;
    }

    @Override
    public void setClientInfo(Properties arg0) throws SQLClientInfoException {

    }

    @Override
    public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException {

    }

}