package csi.security.monitors;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.PrePersist;
import javax.persistence.PreRemove;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.security.ACL;
import csi.security.Authorization;
import csi.security.CsiSecurityManager;
import csi.security.jaas.JAASRole;
import csi.security.queries.AclRequest;
import csi.server.common.dto.CsiMap;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.Resource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.icons.Icon;
import csi.server.common.model.map.Basemap;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.model.themes.Theme;
import csi.server.common.util.Format;
import csi.server.common.util.ValuePair;

public class ResourceACLMonitor {
   private static final Logger LOG = LogManager.getLogger(ResourceACLMonitor.class);

   private static boolean active = true;

   public static void activate() {
      active = true;
   }

   public static void deactivate() {
      active = false;
   }

   @PrePersist
   public void resourceAdded(Resource resourceIn) {
      if (active) {
         try {
            if (needsAcl(resourceIn)) {
               Authorization myAuthorization = CsiSecurityManager.getAuthorization();
               String myOwner = null;

               if (LOG.isDebugEnabled()) {
                  LOG.debug("Adding ACL for new resource: " + Format.value(resourceIn.getName()));
               }
               if (myAuthorization != null) { // Resources added during runtime
                  // For now, we give admin the icon and let everyone read it until we want full icon acl's
                  if (resourceIn instanceof Icon) {
                     List<ValuePair<AclControlType,String>> myEntries = new ArrayList<ValuePair<AclControlType,String>>();

                     myEntries.add(new ValuePair<AclControlType,String>(AclControlType.READ, JAASRole.EVERYONE_ROLE_NAME));

                     myOwner = establishACL(resourceIn, JAASRole.ADMIN_ROLE_NAME, myEntries);
                  } else {
                     myOwner = establishACL(resourceIn, myAuthorization.getName(), null);
                  }

               } else { // Resources added during start-up: Sample Templates
                  List<ValuePair<AclControlType,String>> myEntries = new ArrayList<ValuePair<AclControlType,String>>();

                  if (AclResourceType.ADMIN_TOOL == resourceIn.getResourceType()) {
                     myOwner = JAASRole.META_VIEWER_ROLE_NAME;
                  } else {
                     myOwner = JAASRole.ADMIN_ROLE_NAME;

                     myEntries.add(new ValuePair<AclControlType,String>(AclControlType.READ, JAASRole.EVERYONE_ROLE_NAME));
                  }
                  establishACL(resourceIn, myOwner, myEntries);
               }
               resourceIn.setOwner(myOwner);

               if (LOG.isDebugEnabled()) {
                  if (myAuthorization != null) {
                     LOG.debug("ACL added for " + Format.value(resourceIn.getName()) + ", current owner is " + Format.value(myOwner));
                  } else {
                     LOG.info(Format.value(resourceIn.getName()) + " installed with owner " + Format.value(myOwner) +
                              " for use by " + Format.value(JAASRole.EVERYONE_GROUP_NAME));
                  }
               }
            }
         } catch (Exception myException) {
            LOG.error(String.format("While adding ACL for %s %s, uuid %s; caught exception: ",
                  resourceIn.getClass().getName(), Format.value(resourceIn.getName()),
                  Format.value(resourceIn.getUuid())) + Format.value(myException));
         }
      }
   }

   @PreRemove
   public void resourceRemoved(Resource resourceIn) {
      if (active) {
         try {
            AclRequest.removeAcl(resourceIn.getUuid());
            LOG.info(String.format("Removed ACL for %s '%s', uuid %s", resourceIn.getClass().getName(),
                     resourceIn.getName(), resourceIn.getUuid()));
         } catch (Exception myException) {
            LOG.error(String.format("While removing ACL for %s %s, uuid %s; caught exception: ",
                  resourceIn.getClass().getName(), Format.value(resourceIn.getName()),
                  Format.value(resourceIn.getUuid())) + Format.value(myException));
         }
      }
   }

   private boolean needsAcl(Resource resourceIn) {
      boolean myAclFlag = false;

      if (resourceIn instanceof DataView) {
         myAclFlag = true;
         CsiMap<String,String> properties = resourceIn.getClientProperties();

         if (properties != null) {
            String prebuildFlag = properties.get("prebuild");

            try {
               if (Boolean.parseBoolean(prebuildFlag)) {
                  // We don't acl prebuilds
                  myAclFlag = false;
               }
            } catch (Exception parseException) {
               // Do Nothing
            }
         }
      } else if (((resourceIn instanceof DataViewDef) && (AclResourceType.SAMPLE == resourceIn.getResourceType())) ||
                 (AclResourceType.ADMIN_TOOL == resourceIn.getResourceType()) ||
                 (resourceIn.isTemplate() &&
                  ((AclResourceType.TEMPLATE == resourceIn.getResourceType()) ||
                   ((AclResourceType.TEMPLATE == resourceIn.getPriorType()) &&
                    (AclResourceType.UNKNOWN == resourceIn.getResourceType()))))) {
         myAclFlag = true;
      } else if ((resourceIn instanceof InstalledTable) || (resourceIn instanceof Theme) ||
                 (resourceIn instanceof Icon) || (resourceIn instanceof Basemap)) {
         myAclFlag = true;
      }
      return myAclFlag && !AclRequest.hasACL(resourceIn.getUuid());
   }

   private String establishACL(Resource resourceIn, String ownerIn, List<ValuePair<AclControlType,String>> entriesIn) {
      resourceIn.setOwner(ownerIn);

      ACL myAcl = AclRequest.createACL(resourceIn, null, entriesIn);

      resourceIn.setAclId(myAcl.getId());
      return ownerIn;
   }
}
