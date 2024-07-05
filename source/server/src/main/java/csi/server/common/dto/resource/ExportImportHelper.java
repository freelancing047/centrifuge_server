package csi.server.common.dto.resource;

import java.util.HashMap;
import java.util.Map;

import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.Resource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.map.Basemap;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.map.MapTheme;

/**
 * Created by centrifuge on 4/9/2019.
 */
public class ExportImportHelper {

    public static Map<String, String> buildFileNameComponent(Resource resourceIn) {

        return buildFileNameComponent(resourceIn, null);
    }

    public static Map<String, String> buildFileNameComponents(Resource resourceIn) {
        Map<String, String> grabBag = new HashMap<>();

        if (null != resourceIn) {
            AclResourceType myType = getResourceType(resourceIn);
            String myOwner = resourceIn.getOwner();
            String myName = resourceIn.getName();

            grabBag.put("owner", myOwner);
            grabBag.put("name", myName);
            grabBag.put("type", (myType == null) ? "" : myType.getFileTag());

            return grabBag;

        }
        return null;
    }

    public static Map<String, String> buildFileNameComponent(Resource resourceIn, String uuidIn) {

        if (null != resourceIn) {

            AclResourceType myType = getResourceType(resourceIn);
            String myUuid = (null != uuidIn) ? uuidIn : resourceIn.getUuid();
            String myOwner = resourceIn.getOwner();
            String myName = resourceIn.getName();

            return buildFileNameComponent(myType, myName, myOwner, myUuid);
        }
        return null;
    }

    public static Map<String, String> buildErrorNameComponent(AclResourceType typeIn, String nameIn, String ownerIn, String uuidIn) {

        Map<String, String> grabBag = new HashMap<>();

        if (null != typeIn) {

            if (AclResourceType.ICON == typeIn) {

                grabBag.put("type", getListTag(typeIn));

            } else if (AclResourceType.MAP_BASEMAP == typeIn) {

                grabBag.put("type", getListTag(typeIn));

            } else if (null != uuidIn) {
                grabBag.put("type", typeIn.getDescriptor());
                grabBag.put("name", (nameIn == null) ? "" : nameIn);
                grabBag.put("owner", (ownerIn == null) ? "" : ownerIn);
                grabBag.put("uuid", uuidIn);
            }
            return grabBag;
        }
        return null;
    }

    public static Map<String, String> buildFileNameComponent(AclResourceType typeIn, String nameIn, String ownerIn, String uuidIn) {

        Map<String, String> grabBag = new HashMap<>();

        if (null != typeIn) {

            if (AclResourceType.ICON == typeIn) {

                grabBag.put("type", getListTag(typeIn));

            } else if (null != uuidIn) {

                grabBag.put("type", typeIn.getDescriptor());
                grabBag.put("name", nameIn);
                grabBag.put("owner", ownerIn);
                grabBag.put("uuid", uuidIn);
            }
            return grabBag;
        }
        return null;
    }

    private static String getListTag(AclResourceType typeIn){

        return typeIn.getDescriptor() + "List";
    }

    public static String formatEscapeString(String stringIn) {

        StringBuilder myBuffer = new StringBuilder();

        if ((null != stringIn) && (0 < stringIn.length())) {

            for (char myCharacter : stringIn.toCharArray()) {

                if (ExportImportConstants.URL_CHAR_MAP.length > myCharacter) {

                    myBuffer.append(ExportImportConstants.URL_CHAR_MAP[myCharacter]);

                } else {

                    myBuffer.append(myCharacter);
                }
            }
        }
        return myBuffer.toString();
    }

    private static AclResourceType getResourceType (Resource resourceIn) {
        if (resourceIn instanceof DataView) {
            return AclResourceType.DATAVIEW;
        } else if (resourceIn instanceof DataViewDef) {
            return AclResourceType.TEMPLATE;
        } else if (resourceIn instanceof GraphTheme) {
            return AclResourceType.GRAPH_THEME;
        } else if (resourceIn instanceof MapTheme) {
            return AclResourceType.MAP_THEME;
        } else if (resourceIn instanceof Basemap) {
            return AclResourceType.MAP_BASEMAP;
        }
        return null;
    }

    public static Map<String, String> buildIconListFileName() {
        Map<String, String> grabBag = new HashMap<>();
        grabBag.put("type", getListTag(AclResourceType.ICON));
        return grabBag;
    }
}
