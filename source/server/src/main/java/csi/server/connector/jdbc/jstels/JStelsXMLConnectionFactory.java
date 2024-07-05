package csi.server.connector.jdbc.jstels;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import csi.server.connector.jdbc.jstels.schema.Column;
import csi.server.connector.jdbc.jstels.schema.Schema;
import csi.server.connector.jdbc.jstels.schema.Table;
import csi.server.util.XmlUtil;

public class JStelsXMLConnectionFactory extends AbstractJStelsConnectionFactory {

    private static final Map<String, String> typeMap = new HashMap<String, String>();
    static {
        typeMap.put("integer", "BIGINT");
        typeMap.put("number", "DOUBLE");
        typeMap.put("string", "STRING");
        typeMap.put("datetime", "DATETIME");
        typeMap.put("date", "DATETIME");
        typeMap.put("time", "DATETIME");
    }

    @Override
    public String createConnectString(Map<String, String> propertiesMap) {
        String path = resolveFilePath(propertiesMap);

        // for xml we need full path
        File xmlFile = new File(path);
        try {
            path = xmlFile.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create connection string.", e);
        }

        try {
            File schema = generateSchema(path, propertiesMap);
            return getUrlPrefix() + schema.getCanonicalPath();
        } catch (Throwable t) {
            throw new RuntimeException("Failed to generate schema", t);
        }
    }

   private File generateSchema(String dataFile, Map<String, String> propertiesMap) {
      File schemaFile = null;

      try {
         schemaFile = File.createTempFile("jstelschema", ".xml");

         schemaFile.deleteOnExit();

         Schema schema = createSchema(dataFile, propertiesMap);

         try (FileOutputStream os = new FileOutputStream(schemaFile)) {
            writeSchemaXml(os, schema);
         }
      } catch (FileNotFoundException e) {
         throw new RuntimeException("Failed to create temp schema file", e);
      } catch (XMLStreamException e) {
         throw new RuntimeException("Failed to write temp schema file", e);
      } catch (IOException e) {
         throw new RuntimeException("Failed to create temp schema file", e);
      }
      return schemaFile;
   }

    private Schema createSchema(String dataFile, Map<String, String> propertiesMap) {
        Schema schema = new Schema();
        Table table = new Table();
        table.name = resolveTableName(propertiesMap);
        table.file = dataFile;
        table.path = propertiesMap.get(CSI_SCHEMA_XPATH);
        table.dateFormat = propertiesMap.get(CSI_SCHEMA_DATE_FORMAT);
        table.namespaces = buildNamespaces(propertiesMap);
        schema.tables.add(table);

        int colcnt = 0;
        for (Map.Entry<String,String> entry : propertiesMap.entrySet()) {
           String key = entry.getKey();

           if (!key.toLowerCase().startsWith("csi.schema.columns.")) {
                continue;
            }

            Column col = new Column();
            String rawVal = entry.getValue();
            if (rawVal == null) {
                throw new RuntimeException("Encountered null column definition");
            }

            String[] tokens = rawVal.split("\\|");
            String propertyPart = key.substring(key.lastIndexOf('.') + 1);
            try{
                col.ordinal = Integer.parseInt(tokens[0]);
            } catch (NumberFormatException e) {
                // ignore
            }



            try {
                col.name = URLDecoder.decode(propertyPart, "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                // Ignore -- if we can't get UTF-8, we're in bigger trouble than this catch can handle.
            }

            try {
                String type = URLDecoder.decode(tokens[1], "UTF-8");
                String driverType = typeMap.get(type.trim().toLowerCase());
                if (driverType == null) {
                    driverType = "STRING";
                }
                col.type = driverType;
            } catch (UnsupportedEncodingException uee) {
                // Ignore -- if we can't get UTF-8, we're in bigger trouble than this catch can handle.
            }

            try {
                col.path = URLDecoder.decode(tokens[2], "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                // Ignore -- if we can't get UTF-8, we're in bigger trouble than this catch can handle.
            }

            table.columns.add(col);
            colcnt++;
        }

        Collections.sort(table.columns, Column.ORDER_BY_ORDINAL);

        if (colcnt == 0) {
            throw new RuntimeException("No columns defined");
        }

        return schema;
    }

    private String buildNamespaces(Map<String, String> propertiesMap) {
        StringBuilder buf = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            if ((val != null) && !val.isEmpty() && key.startsWith(CSI_SCHEMA_NAMESPACE_PREFIX)) {
                if (i > 0) {
                    buf.append('|');
                }

                try {
                    String decoded = URLDecoder.decode(val.replace('=', ':'), "UTF-8");
                    buf.append(decoded);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Failed to decode namespace: " + val);
                }

                i++;
            }
        }
        if (i > 0) {
            return buf.toString();
        } else {
            return null;
        }
    }

    public void writeSchemaXml(OutputStream os, Schema schema) throws XMLStreamException {
        XMLStreamWriter xmlWriter = null;

        try {
            xmlWriter = XmlUtil.createXMLStreamWriter(os);
            xmlWriter.writeStartDocument();
            xmlWriter.writeStartElement("schema");
            for (Table t : schema.tables) {
                xmlWriter.writeStartElement("table");
                xmlWriter.writeAttribute("name", t.name);
                xmlWriter.writeAttribute("file", t.file);
                xmlWriter.writeAttribute("path", t.path);
                if ((t.dateFormat != null) && !t.dateFormat.isEmpty()) {
                    xmlWriter.writeAttribute("dateFormat", t.dateFormat);
                }

                if ((t.namespaces != null) && !t.namespaces.isEmpty()) {
                    xmlWriter.writeAttribute("namespaces", t.namespaces);
                }

                for (Column c : t.columns) {
                    xmlWriter.writeStartElement("column");
                    xmlWriter.writeAttribute("name", c.name);
                    xmlWriter.writeAttribute("type", c.type);
                    xmlWriter.writeAttribute("path", c.path);
                    xmlWriter.writeEndElement();
                }

                xmlWriter.writeEndElement();
            }
            xmlWriter.writeEndElement();
            xmlWriter.writeEndDocument();
            xmlWriter.flush();
        } finally {
            if (xmlWriter != null) {
                try {
                    xmlWriter.close();
                } catch (XMLStreamException e) {
                    // ignore
                }
            }
        }
    }

    public String resolveTableName(Map<String, String> propertiesMap) {
        try {
            String name = propertiesMap.get(CSI_SCHEMA_TABLENAME);
            if ((name == null) || name.isEmpty()) {
                String filePath = resolveFilePath(propertiesMap);
                name = (new File(filePath)).getName();
            }

            return name;
        } catch (RuntimeException e) {
            return null;
        }
    }

}
