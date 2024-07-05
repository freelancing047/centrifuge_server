package csi.client.gwt.csi_resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import csi.client.gwt.WebMain;
import csi.server.common.dto.resource.ExportImportConstants;
import csi.server.common.dto.resource.ExportImportHelper;
import csi.server.common.dto.resource.MinResourceInfo;
import csi.server.common.enumerations.AclResourceType;

/**
 * Created by centrifuge on 4/19/2019.
 */
public class ImportParser {

    private static Map<String, AclResourceType> _resourceMap;

    static {
        _resourceMap = new TreeMap<String, AclResourceType>();
        _resourceMap.put(AclResourceType.DATAVIEW.getDescriptor(), AclResourceType.DATAVIEW);
        _resourceMap.put(AclResourceType.TEMPLATE.getDescriptor(), AclResourceType.TEMPLATE);
        _resourceMap.put(AclResourceType.GRAPH_THEME.getDescriptor(), AclResourceType.GRAPH_THEME);
        _resourceMap.put(AclResourceType.MAP_THEME.getDescriptor(), AclResourceType.MAP_THEME);
        _resourceMap.put(AclResourceType.MAP_BASEMAP.getDescriptor(), AclResourceType.MAP_BASEMAP);
    }

    private static String buildFileName(Map<String, String> grabBag, List<String> order) {
        List<String> componentsToUse = new ArrayList<>();
        for(String key : order) {
            if(grabBag.containsKey(key)) {
                componentsToUse.add(grabBag.get(key));
            }
        }
        return componentsToUse.stream().collect(Collectors.joining("_"));
    }

    public static List<MinResourceInfo> buildResourceList(String xmlContentsIn) {

        List<MinResourceInfo> myList = new ArrayList<MinResourceInfo>();

        for (int myIndex = xmlContentsIn.indexOf(ExportImportConstants.RESOURCE_MARKER);
             0 <= myIndex;
             myIndex = xmlContentsIn.indexOf(ExportImportConstants.RESOURCE_MARKER, myIndex)) {

            myIndex += ExportImportConstants.RESOURCE_MARKER.length();

            AclResourceType myType = extractType(xmlContentsIn, myIndex);
            String myName = extractValue(ExportImportConstants.NAME_MARKER, xmlContentsIn, myIndex);
            String myUuid = extractValue(ExportImportConstants.UUID_MARKER, xmlContentsIn, myIndex);
            String myOwner = extractValue(ExportImportConstants.OWNER_MARKER, xmlContentsIn, myIndex);
            Map<String, String> grabBag = ExportImportHelper.buildFileNameComponent(myType, myName, myOwner, myUuid);
            String myFileName = buildFileName(grabBag, WebMain.getClientStartupInfo().getExportFileNameComponentOrder());

            myList.add(new MinResourceInfo(myType, myFileName, myName, myUuid, myOwner, null));
        }
        return (0 < myList.size()) ? myList : null;
    }

    private static AclResourceType extractType(String xmlContentsIn, int indexIn) {

        int myLimit = xmlContentsIn.indexOf(' ', indexIn);
        String myTypeKey = xmlContentsIn.substring(indexIn, myLimit);

        indexIn = myLimit;
        return _resourceMap.get(myTypeKey);
    }

    private static String extractValue(String markerIn, String xmlContentsIn, int indexIn) {

        int myBase = xmlContentsIn.indexOf(markerIn, indexIn) + markerIn.length();
        int myLimit = xmlContentsIn.indexOf('"', myBase);
        String myValue = xmlContentsIn.substring(myBase, myLimit);

        indexIn = myLimit;

        myValue = myValue.replace(ExportImportConstants.AMPERSAND, "&");
        myValue = myValue.replace(ExportImportConstants.LESS_THAN, "<");
        myValue = myValue.replace(ExportImportConstants.MORE_THAN, ">");
        myValue = myValue.replace(ExportImportConstants.QUOTE, "\"");
        myValue = myValue.replace(ExportImportConstants.APOSTROPHE, "'");
        return myValue;
    }
}
