package csi.client.gwt.csi_resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import csi.client.gwt.WebMain;
import csi.server.common.dto.resource.ExportImportConstants;
import csi.server.common.dto.resource.ExportImportHelper;
import csi.server.common.dto.resource.MinResourceInfo;
import csi.server.common.enumerations.AclResourceType;

/**
 * Created by centrifuge on 5/3/2019.
 */
public class ResourceParser {

    private static final String _dataViewTag = AclResourceType.DATAVIEW.getDescriptor();
    private static final String _templateTag = AclResourceType.TEMPLATE.getDescriptor();
    private static final String _nameTag = "<name>";
    private static final String _ownerTag = "<owner>";
    private static final String _remarksTag = "<remarks>";
    private static final String _nameLimit = "</name>";
    private static final String _ownerLimit = "</owner>";
    private static final String _remarksLimit = "</remarks>";

    public ResourceParser() {
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
        AclResourceType myType = null;
        int myBase = ExportImportConstants.XML_HEADER.length + 1;

        // Determine the resource type based on the information inside the XML
        String resourceTypeTag = "<resourceType>";
        String resourceTypeTagEnd = "</resourceType>";
        int resourceTypeIndex = xmlContentsIn.indexOf(resourceTypeTag);
        int resourceTypeEndIndex = xmlContentsIn.indexOf(resourceTypeTagEnd);

        String resourceType = xmlContentsIn.substring(resourceTypeIndex + resourceTypeTag.length(), resourceTypeEndIndex);

        if (_dataViewTag.toUpperCase().equals(resourceType)) {

            myType = AclResourceType.DATAVIEW;
        } else if (_templateTag.toUpperCase().equals(resourceType)) {

            myType = AclResourceType.TEMPLATE;
        }
        if (null != myType) {

            String myName;
            String myOwner;
            String myRemarks;
            int myLimit = xmlContentsIn.indexOf(_nameLimit);

            myBase = xmlContentsIn.indexOf(_nameTag) + _nameTag.length();
            myName = xmlContentsIn.substring(myBase, myLimit);

            myLimit = xmlContentsIn.indexOf(_ownerLimit);
            myBase = xmlContentsIn.indexOf(_ownerTag) + _ownerTag.length();
            myOwner = xmlContentsIn.substring(myBase, myLimit);

            myLimit = xmlContentsIn.indexOf(_remarksLimit);
            myBase = xmlContentsIn.indexOf(_remarksTag) + _remarksTag.length();
            myRemarks = xmlContentsIn.substring(myBase, myLimit);
            Map<String, String> grabBag = ExportImportHelper.buildFileNameComponent(myType, myName, myOwner, null);
            String myFileName = buildFileName(grabBag, WebMain.getClientStartupInfo().getExportFileNameComponentOrder());

            myList.add(new MinResourceInfo(myType, myFileName, myName, null, myOwner, myRemarks));
        }

        return myList;
    }
}
