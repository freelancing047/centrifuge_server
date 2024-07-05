package csi.server.common.dto.system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.util.Format;
import csi.server.dao.CsiPersistenceManager;

/**
 * Created by centrifuge on 7/26/2016.
 */
public class TemplatePurgeRequest extends AbstractPurgeRequest {
   private static final Logger LOG = LogManager.getLogger(TemplatePurgeRequest.class);

    private int _removeTemplateTriesLeft;

    public TemplatePurgeRequest(String _resourceIdIn) {

        super(_resourceIdIn);

        _removeTemplateTriesLeft = _initialTriesLeft;
    }

    public TemplatePurgeRequest(String _resourceIdIn, int retriesIn) {

        super(_resourceIdIn, retriesIn);

        _removeTemplateTriesLeft = _initialTriesLeft;
    }

    public boolean execute() {

        DataViewDef myTemplate = null;

        try {

            CsiPersistenceManager.begin();
            myTemplate = CsiPersistenceManager.findForSystem(DataViewDef.class, _resourceId);

        } catch (Exception ignore) {}

        if (null != myTemplate) {

            return removeTemplate(myTemplate);

        } else {

            CsiPersistenceManager.rollback();
            LOG.error("Unable to find DataView " + Format.value(_resourceId));
            return (0 >= (--_initialTriesLeft));
        }
    }

    private boolean removeTemplate(DataViewDef templateIn) {

        if (0 < _removeTemplateTriesLeft--) {

            try {

                CsiPersistenceManager.deleteForSystem(templateIn);
                CsiPersistenceManager.commit();
                _removeTemplateTriesLeft = 0;

            } catch (Exception myException) {

                CsiPersistenceManager.rollback();
                LOG.error("Caught exception deleting Template "
                        + Format.value(templateIn) + ":\n"
                        + Format.value(myException));
                return false;
            }

        } else {

            templateIn.setResourceType(AclResourceType.CONNECTION.BROKEN);
            CsiPersistenceManager.merge(templateIn);
            CsiPersistenceManager.commit();
        }
        return true;
    }
}
