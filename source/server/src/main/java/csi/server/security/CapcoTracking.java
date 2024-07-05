package csi.server.security;

import csi.server.common.enumerations.CapcoSource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.security.CapcoInfo;

/**
 * Created by centrifuge on 5/30/2018.
 */
public class CapcoTracking extends SecurityTracking {
   private CapcoInfo _capco;

   public CapcoTracking(DataView dataViewIn, LinkupMapDef linkupIn) {
      super(dataViewIn, linkupIn);

      if (null != _template) {
         _capco = _template.getCapcoInfo();

         if (null != _capco) {
            _mode = _capco.getMode();

            if ((CapcoSource.USER_AND_DATA == _mode) || (CapcoSource.DATA_ONLY == _mode)) {
               genFieldList(_capco.getSecurityFields());
            }
         }
      }
   }

   public String createString() {
      return (_capco == null) ? null : createString(null, _capco.getUserPortion());
   }
}
