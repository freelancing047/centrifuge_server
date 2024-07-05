package csi.server.common.dto.system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.model.Resource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;

/**
 * Created by centrifuge on 7/26/2016.
 */
public abstract class AbstractPurgeRequest {
   protected static final Logger LOG = LogManager.getLogger(AbstractPurgeRequest.class);

   protected static final int _minTryCount = 1;

   protected String _resourceId;
   protected int _initialTriesLeft;

   public abstract boolean execute();

   public AbstractPurgeRequest(String _resourceIdIn, int retriesIn) {
      super();

      _resourceId = _resourceIdIn;
      _initialTriesLeft = Math.max(_minTryCount, (retriesIn + 1));
   }

   public AbstractPurgeRequest(String _resourceIdIn) {
      this(_resourceIdIn, _minTryCount);
   }

   public static AbstractPurgeRequest createRequest(Resource resourceIn) {

      if (resourceIn instanceof DataView) {

         return new DataViewPurgeRequest(resourceIn.getUuid());

      } else if (resourceIn instanceof DataViewDef) {

         return new TemplatePurgeRequest(resourceIn.getUuid());
      }
      return null;
   }
}
