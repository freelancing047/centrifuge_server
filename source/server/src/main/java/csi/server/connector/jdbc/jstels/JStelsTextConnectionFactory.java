package csi.server.connector.jdbc.jstels;

import java.io.File;
import java.util.Map;
import java.util.Properties;

public class JStelsTextConnectionFactory extends AbstractJStelsConnectionFactory {

    @Override
    public Properties toNativeProperties(Map<String, String> propMap) {
        Properties props = super.toNativeProperties(propMap);

        String hasHeaders = propMap.get(CSI_SCHEMA_HASHEADERS);
        if (hasHeaders != null) {
            if (Boolean.parseBoolean(hasHeaders)) {
                props.put("suppressHeaders", "false");
            } else {
                props.put("suppressHeaders", "true");
            }
        }

        String rowDelim = translateDelims(propMap.get(CSI_SCHEMA_ROWDELIM));
        if (rowDelim != null) {
            props.put("rowDelimiter", rowDelim);
        }

        // default is tab
        String colDelim = translateDelims(propMap.get(CSI_SCHEMA_CELLDELIM));
        if (colDelim != null) {
            props.put("separator", colDelim);
        }

        // default fileExtension is ".txt"
        String remoteFilePath = propMap.get(CSI_REMOTEFILEPATH);
        if ((remoteFilePath != null) && !remoteFilePath.trim().endsWith(".txt")) {
            String fileExtension = remoteFilePath.substring(remoteFilePath.lastIndexOf("."));
            props.put("fileExtension", fileExtension);
        }

        // default is jvm's charset
        String charset = propMap.get(CSI_SCHEMA_CHARSET);
        if ((charset != null) && !charset.trim().equalsIgnoreCase("Default")) {
            props.put("charset", charset);
        }

        return props;
    }

    public String createConnectString(Map<String, String> propertiesMap) {
        // String path = propertiesMap.get( CSI_FILEPATH );
        // if (path == null || path.isEmpty()) {
        // throw new RuntimeException("Missing required property " + CSI_FILEPATH);
        // }
        String path = resolveFilePath(propertiesMap);

        return getUrlPrefix() + (new File(path)).getParent();

    }

    protected String translateDelims(String delimiter) {
        if (delimiter == null) {
            return null;
        }

        String prepped = delimiter.trim();
        if (prepped.isEmpty()) {
            return null;
        }

        if (prepped.equalsIgnoreCase("CR")) {
            return "\r";
        } else if (prepped.equalsIgnoreCase("LF")) {
            return "\n";
        } else if (prepped.equalsIgnoreCase("CR-LF")) {
            return "\r\n";
        } else if (prepped.equalsIgnoreCase("TAB")) {
            return "\t";
        } else {
            return delimiter;
        }
    }

    @Override
    public String resolveTableName(Map<String, String> propertiesMap) {
        try {
            String filePath = resolveFilePath(propertiesMap);
            return (new File(filePath)).getName();
        } catch (RuntimeException e) {
            return null;
        }
    }

}
