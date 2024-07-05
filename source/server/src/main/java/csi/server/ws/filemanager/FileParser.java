package csi.server.ws.filemanager;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FileParser implements IFileParser {

    private static String CLASS_ID = "FileParser";
    private static Log log = LogFactory.getLog(FileParser.class);

    public static final String HDR = "header";
    public static final String USER = "user";
    public static final String ATTR_IDTYPE = "idtype";
    public static final String USERID = "userid";
    public static final String GROUP = "group";

    public static final String DATA = "data";
    public static final String ATTR_DELIM = "delimiter";
    public static final String ATTR_QUOTE = "quotes";
    public static final String DATAVIEW = "dataview";
    public static final String PROCEDURE = "procedure";
    public static final String INPUT = "input";
    public static final String INPUTPARM = "inputparm";
    public static final String ATTR_STYLE = "style";
    public static final String ITEM = "item";

    public static final String VIEW = "view";
    public static final String ATTR_LAYOUT = "layout";
    public static final String ATTR_CHARTT = "charttype";
    public static final String ATTR_LABELS = "labels";
    public static final String DIMENSION = "dimension";
    public static final String ATTR_SEQUENCE = "sequence";
    public static final String AGGREGATE = "aggregate";
    public static final String ATTR_FIELD = "fieldname";
    public static final String ATTR_FUNC = "function";

    public static final String ATTR_NAME = "name";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_ID = "id";

    public static final String DOT = ".";
    public static final String AT = "@";
    public static final String DOCNAME = "servicerequest";

    public static HashMap<String, String> VIEWTYPE;
    public static final int VIEW_TABLE = 0;
    public static final int VIEW_RELGR = 1;
    public static final int VIEW_CHART = 2;
    public static final int VIEW_MAPCH = 3;
    public static final int VIEW_TIMEL = 4;
    public static final int VIEW_GEOSP = 5;

    public static HashMap<String, String> CHARTTYPE;
    public static final int CHART_HEAT = 0;
    public static final int CHART_VBAR = 1;
    public static final int CHART_HBAR = 2;
    public static final int CHART_PIE = 3;
    public static final int CHART_LINE = 4;
    public static final int CHART_SSHT = 5;
    public static final int CHART_BUBL = 6;

    public static HashMap<String, String> LAYOUT;
    public static final int LAYOUT_CENTRIFUGE = 0;
    public static final int LAYOUT_CIRCULAR = 1;
    public static final int LAYOUT_FORCEDIR = 2;
    public static final int LAYOUT_LINEAR = 3;
    public static final int LAYOUT_RADIAL = 4;
    public static final int LAYOUT_SCRAMBLE = 5;

    public static HashMap<String, HashMap<String, String>> TRXMAP;

    static boolean initialized = false;

    File subject;
    DocumentBuilderFactory dbf;
    DocumentBuilder db;
    HashMap<String, String> parametermap = null;

    public FileParser() {
        if (!initialized)
       {
         init(); // do this only once
      }

        dbf = DocumentBuilderFactory.newInstance();
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException pcx) {
        }
    }

    private void init() {
        // create translation maps which are used to translate values into
        // something the integration service expects
        debug(CLASS_ID, "initializing translation maps");
        VIEWTYPE = new HashMap<String, String>();
        VIEWTYPE.put("table", Integer.toString(VIEW_TABLE));
        VIEWTYPE.put("relgraph", Integer.toString(VIEW_RELGR));
        VIEWTYPE.put("rg", Integer.toString(VIEW_RELGR));
        VIEWTYPE.put("chart", Integer.toString(VIEW_CHART));
        VIEWTYPE.put("mapchart", Integer.toString(VIEW_CHART));
        VIEWTYPE.put("timeline", Integer.toString(VIEW_TIMEL));
        VIEWTYPE.put("time", Integer.toString(VIEW_TIMEL));
        VIEWTYPE.put("geospatial", Integer.toString(VIEW_GEOSP));
        VIEWTYPE.put("geo", Integer.toString(VIEW_GEOSP));

        CHARTTYPE = new HashMap<String, String>();
        CHARTTYPE.put("matrix", Integer.toString(CHART_HEAT));
        CHARTTYPE.put("heatmap", Integer.toString(CHART_HEAT));
        CHARTTYPE.put("vertical", Integer.toString(CHART_VBAR));
        CHARTTYPE.put("vbar", Integer.toString(CHART_VBAR));
        CHARTTYPE.put("verticalbar", Integer.toString(CHART_VBAR));
        CHARTTYPE.put("horizontal", Integer.toString(CHART_HBAR));
        CHARTTYPE.put("hbar", Integer.toString(CHART_HBAR));
        CHARTTYPE.put("horizontalbar", Integer.toString(CHART_HBAR));
        CHARTTYPE.put("pie", Integer.toString(CHART_PIE));
        CHARTTYPE.put("line", Integer.toString(CHART_LINE));
        CHARTTYPE.put("spreadsheet", Integer.toString(CHART_SSHT));
        CHARTTYPE.put("tabular", Integer.toString(CHART_SSHT));
        CHARTTYPE.put("bubble", Integer.toString(CHART_BUBL));

        LAYOUT = new HashMap<String, String>();
        LAYOUT.put("centrifuge", Integer.toString(LAYOUT_CENTRIFUGE));
        LAYOUT.put("scramble", Integer.toString(LAYOUT_SCRAMBLE));
        LAYOUT.put("forcedirected", Integer.toString(LAYOUT_FORCEDIR));
        LAYOUT.put("circular", Integer.toString(LAYOUT_CIRCULAR));
        LAYOUT.put("radial", Integer.toString(LAYOUT_RADIAL));
        LAYOUT.put("linear", Integer.toString(LAYOUT_LINEAR));

        // used to lookup attributes that need their values translated
        // to something we can use. for example, "relgraph" becomes "1".
        // the key is of the form elementname@attributename, the value is
        // one of the translation maps above.
        TRXMAP = new HashMap<String, HashMap<String, String>>();
        TRXMAP.put(VIEW + AT + ATTR_LAYOUT, LAYOUT);
        TRXMAP.put(VIEW + AT + ATTR_CHARTT, CHARTTYPE);
        TRXMAP.put(VIEW + AT + ATTR_TYPE, VIEWTYPE);

        initialized = true;
    }

    public void debug(String id, Object text) {
        log.debug(id + ": " + text);
    }

    //
    // IFileParser Interface Methods
    //

    public String getUserDisplayName() {
        return parametermap.get(HDR + DOT + USER + AT + ATTR_NAME);
    }

    public String getUserName() {
        return parametermap.get(HDR + DOT + USER + DOT + USERID);
    }

    public String getUserID() {
        return parametermap.get(HDR + DOT + USER + AT + ATTR_ID);
    }

    public String getUserIDType() {
        return parametermap.get(HDR + DOT + USER + AT + ATTR_IDTYPE);
    }

    public String getGroupName() {
        return parametermap.get(HDR + DOT + USER + DOT + GROUP);
    }

    public String getGroupID() {
        return null;
    }

    public String getDataViewName() {
        return parametermap.get(DATA + DOT + DATAVIEW + AT + ATTR_NAME);
    }

    public String getDataViewID() {
        return parametermap.get(DATA + DOT + DATAVIEW + AT + ATTR_ID);
    }

    public String[] getInputParameters() {
        // first count how many query input parameters are present
        boolean hit;
        int cnt = -1;
        do {
            cnt++;
            hit = parametermap.containsKey(DATA + DOT + DATAVIEW + DOT + INPUT + DOT + INPUTPARM + DOT + cnt);
        } while (hit);

        int pcnt = cnt;
        debug(CLASS_ID, "Found " + cnt + " general input parameters");

        String[] inputparms = null;

        if (cnt > 0) {
            inputparms = new String[cnt];
            String parm = null;
            String name = null;
            // String seq = null;
            cnt = -1;
            do {
                cnt++;
                // get the input parameter value
                parm = parametermap.get(DATA + DOT + DATAVIEW + DOT + INPUT + DOT + INPUTPARM + DOT + cnt);
                // get the name attribute value
                name = parametermap.get(DATA + DOT + DATAVIEW + DOT + INPUT + DOT + INPUTPARM + DOT + cnt + AT + ATTR_NAME);
                try {
                    if (null != name) {
                        inputparms[cnt] = URLEncoder.encode(name + "=" + parm, "UTF-8");
                        debug(CLASS_ID, name + "=" + parm);
                    } else {
                        if (cnt < pcnt) {
                            inputparms[cnt] = URLEncoder.encode("param" + (cnt + 1) + "=" + parm, "UTF-8");
                            debug(CLASS_ID, "param" + (cnt + 1) + "=" + parm);
                        }
                    }
                } catch (UnsupportedEncodingException uex) {
                }

            } while (parm != null);
        }
        return inputparms;
    }

    public String getViewType() {
        return parametermap.get(VIEW + AT + ATTR_TYPE);
    }

    public String[] getChartDimensions() throws Exception {
        // first count how many dimensions are present
        boolean hit;
        int cnt = -1;
        do {
            cnt++;
            hit = parametermap.containsKey(VIEW + DOT + DIMENSION + DOT + cnt);
        } while (hit);
        debug(CLASS_ID, "Found " + cnt + " dimensions");

        String[] dimensions = null;
        int inx;
        if (cnt > 0) {
            dimensions = new String[cnt];
            String dim = null;
            String seq = null;
            cnt = -1;
            do {
                cnt++;
                // get the dimension parameter value
                dim = parametermap.get(VIEW + DOT + DIMENSION + DOT + cnt);
                // get the sequence number
                seq = parametermap.get(VIEW + DOT + DIMENSION + DOT + cnt + AT + ATTR_SEQUENCE);
                if (null != seq) {
                    inx = Integer.parseInt(seq);
                    // store the dimension parameter value in the correct position in the return array
                    dimensions[inx - 1] = dim;
                    debug(CLASS_ID, "dim" + inx + " " + dim);
                }
            } while (dim != null);
        }
        return dimensions;
    }

    public String[] getAggregatingInfo() {
        String[] aggregatinginfo = null;
        if (parametermap.containsKey(VIEW + DOT + AGGREGATE + DOT + "0")) {
            aggregatinginfo = new String[2];
            aggregatinginfo[0] = parametermap.get(VIEW + DOT + AGGREGATE + DOT + "0" + AT + ATTR_FIELD);
            aggregatinginfo[1] = parametermap.get(VIEW + DOT + AGGREGATE + DOT + "0" + AT + ATTR_FUNC);
            debug(CLASS_ID, "Found aggregating info: fieldname=" + aggregatinginfo[0] + " function=" + aggregatinginfo[1]);
        }
        return aggregatinginfo;
    }

    public String getRGLayout() {
        return parametermap.get(VIEW + AT + ATTR_LAYOUT);
    }

    public String getChartType() {
        return parametermap.get(VIEW + AT + ATTR_CHARTT);
    }

    public String getTLLabelSwitch() {
        return parametermap.get(VIEW + AT + ATTR_LABELS);
    }

    public String[] getProcNames() {
        // first count how many procs are present
        boolean hit;
        int cnt = -1;
        do {
            cnt++;
            hit = parametermap.containsKey(DATA + DOT + DATAVIEW + DOT + PROCEDURE + DOT + cnt + AT + ATTR_NAME);
        } while (hit);
        debug(CLASS_ID, "Found " + cnt + " procedures");

        String[] procnames = null;

        if (cnt > 0) {
            procnames = new String[cnt];
            String name = null;
            for (int i = 0; i < cnt; i++) {
                // get the procs name attribute value
                name = parametermap.get(DATA + DOT + DATAVIEW + DOT + PROCEDURE + DOT + i + AT + ATTR_NAME);
                // store the dimension parameter value in the correct position in the return array
                procnames[i] = name;
                debug(CLASS_ID, "proc" + (i + 1) + "=" + name);
            }
        }
        return procnames;
    }

    public String[] getProcParameters(String procname) {
        boolean found = false;
        String pname;
        int cnt = -1;
        // see if we can find a proc that matches the name that was passed in
        do {
            cnt++;
            pname = parametermap.get(DATA + DOT + DATAVIEW + DOT + PROCEDURE + DOT + cnt + AT + ATTR_NAME);
            if (null == pname) {
               break;
            }
            if (pname.equals(procname)) {
                found = true;
                break;
            }
        } while (!found);

        if (!found) {
         return null;
      }

        // found the proc name so now we know what key to use to retrieve the
        // input parameters for the proc
        String key = DATA + DOT + DATAVIEW + DOT + PROCEDURE + DOT + cnt;
        debug(CLASS_ID, procname + " found using key " + key);

        // count how many input parameters are present for this proc
        boolean hit;
        cnt = -1;
        do {
            cnt++;
            hit = parametermap.containsKey(key + DOT + INPUTPARM + DOT + cnt);
        } while (hit);

        int pcnt = cnt;
        debug(CLASS_ID, "Found " + cnt + " input parameters for " + procname);

        // get the input parameter values and store them in the return array
        String[] procparams = null;

        if (cnt > 0) {
            procparams = new String[cnt];
            String prm = null;
            String nam = null;
            // String seq = null;
            cnt = -1;
            do {
                cnt++;
                // get the input parameter value
                prm = parametermap.get(key + DOT + INPUTPARM + DOT + cnt);
                // get the name attribute value
                nam = parametermap.get(key + DOT + INPUTPARM + DOT + cnt + AT + ATTR_NAME);
                try {
                    if (null != nam) {
                        procparams[cnt] = URLEncoder.encode(nam + "=" + prm, "UTF-8");
                        debug(CLASS_ID, nam + "=" + prm);
                    } else {
                        if (cnt < pcnt) {
                            procparams[cnt] = URLEncoder.encode("param" + (cnt + 1) + "=" + prm, "UTF-8");
                            debug(CLASS_ID, "param" + (cnt + 1) + "=" + prm);
                        }
                    }
                } catch (UnsupportedEncodingException uex) {
                }

            } while (prm != null);
        }

        return procparams;
    }

    //
    // Create a HashMap with stuff from the document.
    //
    // The keys will be of the form name.name.name.name... which basically is a
    // flattened representation of the elements of interest.
    //
    // When an element is repeated, a sequence number is appended to the key
    //
    public HashMap<String, String> parse(File f) throws Exception {
        Document svcreqdoc = db.parse(f);
        parametermap = new HashMap<String, String>();
        getHeaderInfo(svcreqdoc, parametermap);
        getDataInfo(svcreqdoc, parametermap);
        getViewInfo(svcreqdoc, parametermap);
        return parametermap;
    }

    // Return HashMap that includes all the parameters of interest collected
    // by the parse method
    public HashMap<String, String> getParameterMap() {
        return parametermap;
    }

    //
    // Supporting methods
    //

    //
    // get information from the header element
    //
    private void getHeaderInfo(Document svcreqdoc, HashMap<String, String> parametermap) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String exp = "/" + DOCNAME + "/" + HDR + "/" + USER;
        Node usernode = (Node) xpath.evaluate(exp, svcreqdoc, XPathConstants.NODE);

        if (null != usernode) {
            getAttributes(usernode, parametermap);
            Node useridnode = (Node) xpath.evaluate(USERID, usernode, XPathConstants.NODE);
            if (null != useridnode) {
                getNodeValue(useridnode, parametermap);
            }
            Node groupnode = (Node) xpath.evaluate(GROUP, usernode, XPathConstants.NODE);
            if (null != groupnode) {
                getNodeValue(groupnode, parametermap);
            }
        }
    }

    //
    // get information from the data element
    //
    private void getDataInfo(Document svcreqdoc, HashMap<String, String> parametermap) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String exp = "/" + DOCNAME + "/" + DATA + "/" + DATAVIEW;
        Node dvnode = (Node) xpath.evaluate(exp, svcreqdoc, XPathConstants.NODE);

        if (null != dvnode) {
            getAttributes(dvnode, parametermap);
            // get query input parameters and associated attributes
            Node in = (Node) xpath.evaluate(INPUT, dvnode, XPathConstants.NODE);
            if (null != in) {
                getElementsByTagName(INPUTPARM, in, parametermap);
            }

            // get procs, input parameters, and such
            NodeList nl = (NodeList) xpath.evaluate(PROCEDURE, dvnode, XPathConstants.NODESET);
            int ncount = nl.getLength();
            for (int i = 0; i < ncount; i++) {
                getAttributes(nl.item(i), i, parametermap); // get attributes for procedure
                getElementsByTagName(INPUTPARM, nl.item(i), i, parametermap); // get all the input parms
            }
        }
    }

    //
    // get information from the view element
    //
    private void getViewInfo(Document svcreqdoc, HashMap<String, String> parametermap) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String exp = "/" + DOCNAME + "/" + VIEW;
        Node viewnode = (Node) xpath.evaluate(exp, svcreqdoc, XPathConstants.NODE);

        if (null != viewnode) {
            getAttributes(viewnode, parametermap);
            // set default to table if type attribute not specified
            String key = getKeyNameFromNode(viewnode) + AT + ATTR_TYPE;
            String typeval = parametermap.get(key);
            if (null == typeval) {
                typeval = VIEWTYPE.get("table");
                parametermap.put(key, typeval); // stow back the translated value
                debug(CLASS_ID, "gVI stored: " + key + "=" + typeval + " (default)");
            }

            // get additional crap
            getElementsByTagName(DIMENSION, viewnode, parametermap);
            getElementsByTagName(AGGREGATE, viewnode, parametermap);
        }
        // set default to table if view element not specified
        else {
            parametermap.put(VIEW + AT + ATTR_TYPE, VIEWTYPE.get("table"));
            debug(CLASS_ID, "gVI stored: " + VIEW + AT + ATTR_TYPE + "=" + VIEWTYPE.get("table") + " (default)");
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
        String name = n.getNodeName();
        if (seqnum >= 0) {
         name = name + DOT + seqnum;
      }

        Node p = n.getParentNode();
        String pname = null;

        if (null != parentkey) {
            name = parentkey + DOT + name;
        } else {
            while (null != p) {
                pname = p.getNodeName();
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

    private void getAttributes(String parentkey, Node n, int seqnum, HashMap<String, String> parametermap) {
        String nodename = n.getNodeName();
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
        HashMap<String, String> wrk = null;
        for (int i = 0; i < acount; i++) {
            a = attr.item(i);
            name = a.getNodeName(); // get attribute name
            val = a.getNodeValue(); // get attribute value

            // if necessary, translate this value into something more usable
            wrk = TRXMAP.get(nodename + AT + name);
            if (null != wrk) {
                val = wrk.get(val);
            }
            parametermap.put(key + AT + name, val);
            debug(CLASS_ID, "gAt stored: " + key + AT + name + "=" + val);
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

        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nl = (NodeList) xpath.evaluate(tagname, parent, XPathConstants.NODESET);
        int ncount = nl.getLength();

        for (int i = 0; i < ncount; i++) {
            n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                debug(CLASS_ID, "gEB stored: " + parentkey + DOT + tagname + DOT + seq + "=" + n.getTextContent());
                parametermap.put(parentkey + DOT + tagname + DOT + seq, n.getTextContent());
                // get all the attributes for the element also
                getAttributes(parentkey, n, seq, parametermap);
                seq++;
            }
        }
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
        debug(CLASS_ID, "gNV stored: " + key + "=" + val);
        parametermap.put(key, val);
    }
}
