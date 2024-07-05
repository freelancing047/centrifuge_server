package csi.server.security;

import csi.server.common.enumerations.CapcoSource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.security.SecurityTagsInfo;

/**
 * Created by centrifuge on 5/30/2018.
 */
public class SecurityTagTracking extends SecurityTracking {
   SecurityTagsInfo _tags = null;
   String _prefix = null;
   String _postfix = null;

   public SecurityTagTracking(DataView dataViewIn, LinkupMapDef linkupIn) {
      super(dataViewIn, linkupIn);

      if (null != _template) {
         _tags = _template.getSecurityTagsInfo();

         if (null != _tags) {
            _mode = _tags.getMode();

            if ((CapcoSource.USER_AND_DATA == _mode) || (CapcoSource.DATA_ONLY == _mode)) {
               StringBuilder myBuffer = new StringBuilder();
               String myBaseTags = _tags.getBaseTagString();
               String myIgnoredTags = _tags.getIgnoredTagString();

               genFieldList(_tags.getColumnList());
               _prefix = _tags.getDelimiterString();

               if (!_tags.getOrTags()) {
                  myBuffer.append('+');
               }
               myBuffer.append('[');

               if ((null != myBaseTags) && (0 < myBaseTags.length())) {

                  myBuffer.append(myBaseTags);
               }
               myBuffer.append(']');
               myBuffer.append('[');

               if ((null != myIgnoredTags) && (0 < myIgnoredTags.length())) {
                  myBuffer.append(myIgnoredTags);
               }
               myBuffer.append(']');
               _postfix = myBuffer.toString();
            }
         }
      }
   }

   public String createString() {
      return (_tags == null) ? null : createString(_prefix, _postfix);
   }
}
