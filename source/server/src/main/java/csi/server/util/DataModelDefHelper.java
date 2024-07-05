package csi.server.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.thoughtworks.xstream.XStream;

import csi.server.business.cachedb.script.CsiScriptRunner;
import csi.server.business.cachedb.script.ecma.EcmaScriptRunner;
import csi.server.common.codec.xstream.XStreamHelper;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.GoogleMapsViewDef;
import csi.server.common.model.SketchViewDef;
import csi.server.common.model.visualization.VisualizationDef;

public class DataModelDefHelper {
   public DataModelDefHelper() {
   }

   public static SketchViewDef getSketchViewDef(DataModelDef modelDef) {
      return getVisualizationDef(modelDef, DataModelDef.DEFAULT_SKETCH_VIEW, SketchViewDef.class);
   }

   public static GoogleMapsViewDef getGoogleMapsViewDef(DataModelDef modelDef) {
      return getVisualizationDef(modelDef, DataModelDef.DEFAULT_GOOGLE_MAPS, GoogleMapsViewDef.class);
   }

   @SuppressWarnings("unchecked")
   public static <T extends VisualizationDef> T getVisualizationDef(DataModelDef modelDef, String name, Class<T> clazz) {
      T result = null;

      for (VisualizationDef v : modelDef.getVisualizations()) {
         if (clazz.isAssignableFrom(v.getClass()) && v.getName().equalsIgnoreCase(name)) {
            result = (T) v;
            break;
         }
      }
      return result;
   }

    public static List<String> listFieldReferences(FieldListAccess modelDef, FieldDef field) throws CentrifugeException {
//        List<FieldDef> fieldList = new ArrayList<FieldDef>();
//        fieldList.add(field);
//        Map<String, List<String>> map = listFieldReferences(modelDef, fieldList);
//        return map.get(field.getUuid());
        return new ArrayList<String>();
    }

    private static Map<String, List<String>> listFieldReferences(FieldListAccess modelDef, List<FieldDef> fields) throws CentrifugeException {
        Map<String, List<String>> refMap = new HashMap<String, List<String>>();

        XPathFactory xpathFac = XPathFactory.newInstance();
        XStream xs = XStreamHelper.getModelRefCodec();
        ByteArrayOutputStream xmlOutStream = new ByteArrayOutputStream();
        xs.toXML(modelDef, xmlOutStream);
        InputSource source = new InputSource(new ByteArrayInputStream(xmlOutStream.toByteArray()));

        for (FieldDef field : fields) {
            List<String> refs = listFieldReference(modelDef, xpathFac, source, field);
            refMap.put(field.getUuid(), refs);
        }

        return refMap;
    }

    private static List<String> listFieldReference(FieldListAccess modelDef, XPathFactory xpathFac, InputSource source, FieldDef field) throws CentrifugeException {
        List<String> refs = new ArrayList<String>();

        // check script references
        List<FieldDef> list = modelDef.getDependentFieldDefs();
        for (FieldDef sf : list) {
            CsiScriptRunner runner = new EcmaScriptRunner();
            if (runner.referencesField(sf.getScriptText(), field.getFieldName())) {
                refs.add("/DataModelDef/modelDef/fieldDefs/FieldDef[uuid = '" + field.getUuid() + "']");
            }
        }

        // check all other references in the model;

        try {
            String searchFieldPath = "uuid[ . = '" + field.getUuid() + "']";
            XPath xpath = xpathFac.newXPath();
            XPathExpression searchExpr = xpath.compile("//" + searchFieldPath);
            NodeList nodes = (NodeList) searchExpr.evaluate(source, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node n = nodes.item(i);

                StringBuilder pathBuf = new StringBuilder();
                Node parent = n.getParentNode();
                while ((parent != null) && (parent.getNodeType() != Node.DOCUMENT_NODE)) {
                    NodeList nodeList = parent.getChildNodes();
                    for (int j = 0; j < nodeList.getLength(); j++) {
                        Node childNode = nodeList.item(j);
                        String name = childNode.getLocalName();
                        if ((name != null) && name.equals("uuid")) {
                            pathBuf.insert(0, "[uuid='" + childNode.getTextContent() + "']");
                            break;
                        }
                    }

                    pathBuf.insert(0, parent.getLocalName());
                    pathBuf.insert(0, '/');
                    parent = parent.getParentNode();
                }

                String nodePath = pathBuf.toString();

                // ignore if this is the field that we're
                // finding references for
                if (!nodePath.matches("/DataModelDef.*/fieldDefs/FieldDef\\[uuid='" + field.getUuid() + "'\\]")) {
                    refs.add(nodePath);
                }
            }
        } catch (XPathExpressionException e) {
            throw new CentrifugeException("Failed to find object references.", e);
        }
        return refs;
    }
}
