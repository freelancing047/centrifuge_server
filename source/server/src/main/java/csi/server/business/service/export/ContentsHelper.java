package csi.server.business.service.export;

import org.jdom.Element;

import csi.server.common.dto.resource.ExportImportConstants;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.Resource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;

/**
 * Created by centrifuge on 4/30/2019.
 */
public class ContentsHelper {
   private ContentsHelper() {
   }

   public static Element createElement(Resource resourceIn) {
      return createElement(resourceIn, null);
   }

   public static Element createElement(Resource resourceIn, String uuidIn) {
      AclResourceType type = resourceIn.getResourceType();
      Element element = new Element(getResourceTag(type));
      String name = resourceIn.getName();
      String uuid = (uuidIn == null) ? resourceIn.getUuid() : uuidIn;
      String owner = resourceIn.getOwner();

      element.setAttribute(ExportImportConstants.NAME_TAG, (name == null) ? "" : name);
      element.setAttribute(ExportImportConstants.UUID_TAG, uuid);
      element.setAttribute(ExportImportConstants.OWNER_TAG, (owner == null) ? "" : owner);

      if (AclResourceType.DATAVIEW == type) {
         element.setAttribute(ExportImportConstants.VERSION_TAG, ((DataView) resourceIn).getVersion());
      } else if (AclResourceType.TEMPLATE == type) {
         element.setAttribute(ExportImportConstants.VERSION_TAG, ((DataViewDef) resourceIn).getVersion());
      }
      return element;
   }

   public static Element createElement(AclResourceType typeIn, Integer countIn) {
      Element element = new Element(getResourceListTag(typeIn));

      if (countIn != null) {
         element.setAttribute(ExportImportConstants.COUNT_TAG, countIn.toString());
      }
      return element;
   }

   private static String getResourceTag(AclResourceType typeIn) {
      return typeIn.getDescriptor();
   }

   private static String getResourceListTag(AclResourceType typeIn) {
      return typeIn.getListTag();
   }
}
