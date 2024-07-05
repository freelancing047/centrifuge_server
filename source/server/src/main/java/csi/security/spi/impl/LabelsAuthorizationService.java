package csi.security.spi.impl;

import java.util.List;

import javax.persistence.EntityManager;

import csi.security.Authorization;
import csi.security.spi.AuthorizationContext;
import csi.security.spi.AuthorizationService;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.extension.ExtensionData;
import csi.server.common.model.extension.LabelsData;
import csi.server.dao.CsiPersistenceManager;

public class LabelsAuthorizationService implements AuthorizationService {
   @Override
   public boolean isAuthorized(AuthorizationContext context) {
      boolean authorized = false;
      String resourceId = context.getResourceId();
      LabelsData data = getLabelDataFrom(resourceId);

      if (data == null) {
         authorized = true;
      } else {
         Authorization authZ = context.getAuthorization();
         List<String> requiredRoles = data.getLabels();

         // hack for now to accomodate empty roles
         authorized = requiredRoles.isEmpty() || authZ.hasAllRoles(requiredRoles.toArray(new String[0]));
      }
      return authorized;
   }

   private static LabelsData getLabelDataFrom(String resourceId) {
      LabelsData result = null;
      String dvId = resourceId;

      // NB: do not use the other find/get methods on CsiPersistenceManager!
      // those calls result in a security check. It will
      // result in a stack overflow!!!
      EntityManager em = CsiPersistenceManager.getMetaEntityManager();
      DataView dv = em.find(DataView.class, new CsiUUID(dvId));

      if (dv != null) {
         DataViewDef def = dv.getMeta();
         result = getLabelsData(def);
      }
      return result;
   }

   private static LabelsData getLabelsData(DataViewDef def) {
      LabelsData result = null;
      List<ExtensionData> list = def.getExtensionData();

      for (ExtensionData data : list) {
         if (data instanceof LabelsData) {
            result = (LabelsData) data;
            break;
         }
      }
      return result;
   }
}
