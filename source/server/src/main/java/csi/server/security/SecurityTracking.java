package csi.server.security;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import csi.security.queries.AclRequest;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.CapcoSource;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.linkup.LooseMapping;

/**
 * Created by centrifuge on 5/30/2018.
 */
public class SecurityTracking {
   protected CapcoSource _mode;
   protected DataViewDef _template;
   protected LinkupMapDef _linkup;
   protected FieldListAccess _sourceAccess;
   protected FieldListAccess _targetAccess;
   protected List<String> _targetFieldList;

   public SecurityTracking(DataView dataViewIn, LinkupMapDef linkupIn) {
      _linkup = linkupIn;
      _template = (linkupIn == null)
                     ? null
                     : locateTemplate(linkupIn.getTemplateUuid(), linkupIn.getTemplateName(), linkupIn.getTemplateOwner());

      if (_template != null) {
         DataViewDef myMeta = (dataViewIn == null) ? null : dataViewIn.getMeta();

         if (myMeta != null) {
            _targetAccess = myMeta.getFieldListAccess();
         }
         _sourceAccess = _template.getFieldListAccess();
      }
   }

   protected void genFieldList(List<String> fieldListIn) {
      _targetFieldList = new ArrayList<>();

      if (fieldListIn != null) {
         for (String mySourceFieldId : fieldListIn) {
            FieldDef mySourceField = _sourceAccess.getFieldDefByAnyKey(mySourceFieldId);
            LooseMapping myMapping = _linkup.getMappingByMappingLocalId(mySourceField.getLocalId());

            if (myMapping == null) {
               myMapping = _linkup.getMappingByMappingName(mySourceField.getFieldName());
            }
            if (myMapping != null) {
               _targetFieldList.add(myMapping.getMappedLocalId());
            }
         }
      }
      if (_targetFieldList.isEmpty()) {
         _targetFieldList = null;
      }
   }

   private DataViewDef locateTemplate(String templateIdIn, String templateNameIn, String templateOwnerIn) {
      DataViewDef myTemplate =
         (DataViewDef) AclRequest.getResourceByUuidAvoidingSecurity(templateIdIn, AclResourceType.TEMPLATE);

      if (myTemplate == null) {
         myTemplate =
            (DataViewDef) AclRequest.getOwnedResourceByNameAvoidingSecurity(templateNameIn, templateOwnerIn, AclResourceType.TEMPLATE);
      }
      return myTemplate;
   }

   protected String createString(String prefixIn, String postfixIn) {
      StringBuilder buffer = new StringBuilder(_mode.toString());

      if (prefixIn != null) {
         buffer.append(prefixIn);
      }
      buffer.append(_targetFieldList.stream().collect(Collectors.joining("|", "[", "]")));

      if (postfixIn != null) {
         buffer.append(postfixIn);
      }
      return (buffer.length() > 0) ? buffer.toString() : null;
   }
}
