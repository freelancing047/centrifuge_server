package csi.server.connector.rest;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.sql.rowset.RowSetMetaDataImpl;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Iterators;

import csi.server.common.dto.TypeNames;

/*
 * Namespaces totally screws this up!
 *
 * The document builder (constructed in the ServiceConnection) needs to be
 * marked as namespace aware.  Not doing so results in tag names that include
 * the namespace-prefix included e.g. ns1:columns.
 *
 * With that in place, we need one of two things: a configured namespace URI
 * so that we can have a canned static query that substitutes
 *
 */
public class RestServiceResultSetFort extends RestServiceResultSet {
   protected static final Logger LOG = LogManager.getLogger(RestServiceResultSetFort.class);

   protected static String ID = "RestServiceResultSetFort";

   public static final String DOCNAME = "results";
    public static final String COLUMNS = "columns";
    public static final String COLUMN = "column";
    public static final String ROWS = "rows";
    public static final String ROW = "row";
    public static final String VALUE = "value";
    public static final String DOT = ".";
    public static final String AT = "@";

    static final String XP_DOCNAME = "ns1:results";
    static final String XP_COLUMNS = "ns1:columns";
    static final String XP_COLUMN = "ns1:column";
    static final String XP_ROWS = "ns1:rows";
    static final String XP_ROW = "ns1:row";
    static final String XP_VALUE = "ns1:value";

    static final String COLUMNS_QUERY = "/results/columns";
    static final String COLUMNS_QUERY_NS = "/ns1:results/ns1:columns";

    private XPath xpath = XPathFactory.newInstance().newXPath();

    private RestServiceConnectionFort conn;

    RestServiceResultSetFort(RestServiceConnectionFort connection, Document document, String query) {

        conn = connection;
        HashMap resultsmap = new HashMap<String, String>();

        try {
            getColumnInfo(document, resultsmap);
            setMetaData(resultsmap);
            records = getRows(document);
        } catch (Exception ex) {
            LOG.debug(ID + ": getColumnInfo exception: " + ex.getMessage());
        }

    }

    protected RowSetMetaDataImpl setMetaData(HashMap<String, String> resultsmap) throws SQLException {
        rsmd = new RowSetMetaDataImpl();

        boolean hit;
        int cnt = -1;

        do {
            cnt++;
            hit = resultsmap.containsKey(COLUMNS + DOT + COLUMN + DOT + cnt);
        } while (hit);
        LOG.debug(ID + ": setMetaData found " + cnt + " columns");

        rsmd.setColumnCount(cnt);
        for (int i = 0; i < cnt; i++) {
            String cn = resultsmap.get(COLUMNS + DOT + COLUMN + DOT + i);

            rsmd.setColumnLabel(i + 1, cn);
            rsmd.setColumnName(i + 1, cn);
            rsmd.setColumnType(i + 1, Types.VARCHAR);
            rsmd.setColumnTypeName(i + 1, TypeNames.VARCHAR);
        }
        return rsmd;
    }

    public Object getObject(int columnIndex) throws SQLException {
        Node item = records.item(currentPosition);
        // LOG.debug(ID+": getObject: getColumnValueFromRow "+columnIndex);
        Object value = getColumnValueFromRow(item, columnIndex);

        return value;
    }

    public String getString(int columnIndex) throws SQLException {
        Node item = records.item(currentPosition);
        // LOG.debug(ID+": getString: getColumnValueFromRow "+columnIndex);
        Object value = getColumnValueFromRow(item, columnIndex);

        return (String) value;

    }

    static String ROW_QUERY = "/results/rows/row";
    static String ROW_QUERY_NS = "/ns1:results/ns1:rows/ns1:row";

    private NodeList getRows(Document document) throws Exception {
        xpath.reset();
        String expr = ROW_QUERY;
        if (conn.isUseNamespaces()) {
            expr = ROW_QUERY_NS;
            xpath.setNamespaceContext(buildNamespaceContext("ns1", conn.getMetaNamespaceURI()));
        }
        NodeList nl = (NodeList) xpath.evaluate(expr, document, XPathConstants.NODESET);
        int ncount = nl.getLength();

        LOG.debug(ID + ": getRows found " + ncount + " rows");
        return nl;
    }

    private Object getColumnValueFromRow(Node n, int col) {
        String returnval = null;

        xpath.reset();

        try {
            LOG.debug(ID + ": Get column " + col + " for this row");
            int vcnt = 0;

            if (n.hasChildNodes()) {
                NodeList nl = n.getChildNodes();
                int len = nl.getLength();

                for (int i = 0; i < len; i++) {
                    Node c = nl.item(i);
                    // look at only value elements
                    if (getNodeName(c).equals(VALUE)) {
                        vcnt++;
                        if (vcnt == col) {
                            returnval = c.getTextContent();
                            break;
                        }
                    } else {
                        LOG.debug(ID + ":   ignoring element " + c.getNodeName());
                    }
                }
                LOG.debug(ID + ":   got col" + col + "=" + returnval);
            }
        } catch (Exception ex) {
            LOG.debug(ID + ": getColumnValueFromRow: " + ex.getMessage());
        }
        return returnval;
    }

    //
    // get information from the columns element
    // columns.column.n will be the key in the hashmap
    //
    private void getColumnInfo(Document document, HashMap<String, String> parametermap) throws Exception {
        xpath.reset();

        String expr = COLUMNS_QUERY;

        if (conn.isUseNamespaces()) {
            expr = COLUMNS_QUERY_NS;
            xpath.setNamespaceContext(buildNamespaceContext("ns1", conn.getMetaNamespaceURI()));
        }
        Node colsnode = (Node) xpath.evaluate(expr, document, XPathConstants.NODE);

        if (null != colsnode) {
            // get column values
            getElementsByTagName(COLUMN, colsnode, parametermap);
        }
    }

    //
    // Grab all the attributes for a specified node
    // The attributes are identified by a @ delimeter when they're
    // stowed in the hashmap for later use.
    //
    private void getAttributes(Node n, HashMap<String, String> parametermap) {
        getAttributes(null, n, -1, parametermap);
    }

    private void getAttributes(Node n, int seqnum, HashMap<String, String> parametermap) {
        getAttributes(null, n, seqnum, parametermap);
    }

    protected String getNodeName(Node n) {
        String name = null;
        if (n instanceof Element) {
            name = ((Element) n).getLocalName();
        } else if (n instanceof Attr) {
            name = ((Attr) n).getLocalName();
        }

        if (name == null) {
            name = n.getNodeName();
        }

        return name;
    }

    private void getAttributes(String parentkey, Node n, int seqnum, HashMap<String, String> parametermap) {
        String key = null;

        if (null != parentkey) {
            key = getKeyNameFromNode(parentkey, n, seqnum);
        } else {
            key = getKeyNameFromNode(n, seqnum);
        }

        NamedNodeMap attr = n.getAttributes();
        int acount = attr.getLength();
        Node a = null;
        String name = null;
        String val = null;

        for (int i = 0; i < acount; i++) {
            a = attr.item(i);
            name = getNodeName(a);
            val = a.getNodeValue(); // get attribute value

            parametermap.put(key + AT + name, val);
            LOG.debug(ID + ": gAt stored: " + key + AT + name + "=" + val);
        }
    }

    //
    // Get elements by tag name whose parent is the specified node
    //
    private void getElementsByTagName(String tagname, Node parent, HashMap<String, String> parametermap) throws Exception {
        getElementsByTagName(tagname, parent, -1, parametermap);
    }

    private void getElementsByTagName(String tagname, Node parent, int seqnum, HashMap<String, String> parametermap) throws Exception {
        String parentkey = null;

        parentkey = getKeyNameFromNode(parent, seqnum);

        Node n = null;
        int seq = 0;

        xpath.reset();

        String expr = tagname;
        if (conn.isUseNamespaces()) {
            xpath.setNamespaceContext(buildNamespaceContext("ns1", conn.getMetaNamespaceURI()));
            expr = "ns1:" + tagname;
        }
        NodeList nl = (NodeList) xpath.evaluate(expr, parent, XPathConstants.NODESET);
        int ncount = nl.getLength();

        for (int i = 0; i < ncount; i++) {
            n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                LOG.debug(ID + ": gEB stored: " + parentkey + DOT + tagname + DOT + seq + "=" + n.getTextContent());
                parametermap.put(parentkey + DOT + tagname + DOT + seq, n.getTextContent());
                // get all the attributes for the element also
                getAttributes(parentkey, n, seq, parametermap);
                seq++;
            }
        }
    }

    //
    // Create a hashtable key of the form parent.parent.parent.node
    //
    private String getKeyNameFromNode(Node n) {
        return getKeyNameFromNode(null, n, -1);
    }

    private String getKeyNameFromNode(Node n, int seqnum) {
        return getKeyNameFromNode(null, n, seqnum);
    }

    private String getKeyNameFromNode(String parentkey, Node n, int seqnum) {
        String name = getNodeName(n);

        if (seqnum >= 0) {
            name = name + DOT + seqnum;
        }

        Node p = n.getParentNode();
        String pname = null;

        if (null != parentkey) {
            name = parentkey + DOT + name;
        } else {
            while (null != p) {
                pname = getNodeName(p);
                if (pname.equals(DOCNAME)) {
                    break;
                }
                name = pname + DOT + name;
                p = p.getParentNode();
            }
        }

        return name;
    }

    //
    // Grab a node's value and store it in the parameter map
    // using the appropriate key
    //
    private void getNodeValue(Node n, HashMap<String, String> parametermap) {
        getNodeValue(n, -1, parametermap);
    }

    private void getNodeValue(Node n, int seqnum, HashMap<String, String> parametermap) {
        String key = null;

        if (seqnum < 0) {
            key = getKeyNameFromNode(n);
        } else {
            key = getKeyNameFromNode(n, seqnum);
        }
        String val = n.getTextContent();

        LOG.debug(ID + ": gNV stored: " + key + "=" + val);
        parametermap.put(key, val);
    }

    protected NamespaceContext buildNamespaceContext(final String p, final String u) {
        return new NamespaceContext() {

            String prefix = p;
            String uri = u;

            @Override
            public Iterator getPrefixes(String uri) {
                if (this.uri.equals(uri)) {
                    return Iterators.singletonIterator(prefix);
                }
                return Collections.emptyIterator();
            }

            @Override
            public String getPrefix(String uri) {
                if (this.uri.equals(uri)) {
                    return prefix;
                }
                return null;
            }

            @Override
            public String getNamespaceURI(String prefix) {
                if (this.prefix.equals(prefix)) {
                    return uri;
                }
                return null;
            }
        };
    }
}
