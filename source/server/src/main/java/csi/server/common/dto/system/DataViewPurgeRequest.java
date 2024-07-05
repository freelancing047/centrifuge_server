package csi.server.common.dto.system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.helper.DataViewHelper;
import csi.server.business.helper.RecoveryHelper;
import csi.server.business.helper.SharedDataSourceHelper;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.dataview.DataView;
import csi.server.common.util.Format;
import csi.server.dao.CsiPersistenceManager;

/**
 * Created by centrifuge on 7/22/2016.
 */
public class DataViewPurgeRequest extends AbstractPurgeRequest {
   private static final Logger LOG = LogManager.getLogger(TemplatePurgeRequest.class);

    private int _releaseInstalledTablesTriesLeft;
    private int _removeCacheDataTriesLeft;
    private int _removeVizDataTriesLeft;
    private int _removeFileArtifactsTriesLeft;
    private int _removeDataViewTriesLeft;

    public DataViewPurgeRequest(String _resourceIdIn) {

        super(_resourceIdIn);

        initialize();
    }

    public DataViewPurgeRequest(String _resourceIdIn, int retriesIn) {

        super(_resourceIdIn, retriesIn);

        initialize();
    }

    public void initialize() {

        _releaseInstalledTablesTriesLeft = _initialTriesLeft;
        _removeCacheDataTriesLeft = _initialTriesLeft;
        _removeVizDataTriesLeft = _initialTriesLeft;
        _removeFileArtifactsTriesLeft = _initialTriesLeft;
        _removeDataViewTriesLeft = _initialTriesLeft;
    }

    public boolean execute(){

        DataView myDataView = null;

        try {

            CsiPersistenceManager.begin();
            myDataView = CsiPersistenceManager.findForSystem(DataView.class, _resourceId);

        } catch (Exception ignore) {}

        if (null != myDataView) {

            if (releaseInstalledTables(myDataView)) {

                if (removeCacheData(myDataView)) {

                    if (removeVizData(myDataView)) {

                        if (removeFileArtifacts(myDataView)) {

                            return removeDataView(myDataView);
                        }
                    }
                }
            }

        } else {

            CsiPersistenceManager.rollback();
            LOG.error("Unable to find DataView " + Format.value(_resourceId));
            return (0 >= (--_initialTriesLeft));
        }
        return false;
    }

    private boolean releaseInstalledTables(DataView dataViewIn) {

        if (0 < _releaseInstalledTablesTriesLeft--) {

            try {

                SharedDataSourceHelper.releaseInstalledTables(dataViewIn.clearInstalledTables());
                CsiPersistenceManager.commit();
                _releaseInstalledTablesTriesLeft = 0;

            } catch (Exception myException) {

                CsiPersistenceManager.rollback();
                LOG.error("Caught exception releasing Installed Tables for DataView "
                        + Format.value(dataViewIn) + ":\n"
                        + Format.value(myException));
                return false;
            }
        }
        return true;
    }

    private boolean removeCacheData(DataView dataViewIn) {

        if (0 < _removeCacheDataTriesLeft--) {

            try {

                RecoveryHelper.dropRelatedCacheItems(dataViewIn);
                CsiPersistenceManager.commit();
                _removeCacheDataTriesLeft = 0;

            } catch (Exception myException) {

                CsiPersistenceManager.rollback();
                LOG.error("Caught exception removing cache for DataView "
                        + Format.value(dataViewIn) + ":\n"
                        + Format.value(myException));
                return false;
            }
        }
        return true;
    }

    private boolean removeVizData(DataView dataViewIn) {

        if (0 < _removeVizDataTriesLeft--) {

            try {

                CsiPersistenceManager.begin();
                if (DataViewHelper.deleteVizData(dataViewIn, true)) {

                    CsiPersistenceManager.merge(dataViewIn);
                    CsiPersistenceManager.commit();
                    _removeVizDataTriesLeft = 0;
                }

            } catch (Exception myException) {

                CsiPersistenceManager.rollback();
                LOG.error("Caught exception removing viz data for DataView "
                        + Format.value(dataViewIn) + ":\n"
                        + Format.value(myException));
                return false;
            }
        }
        return true;
    }

    private boolean removeFileArtifacts(DataView dataViewIn) {

        if (0 < _removeFileArtifactsTriesLeft--) {

            try {

                CsiPersistenceManager.begin();
                if (DataViewHelper.deleteFileArtifacts(dataViewIn.getUuid())) {

                    _removeFileArtifactsTriesLeft = 0;
                    CsiPersistenceManager.merge(dataViewIn);
                    CsiPersistenceManager.commit();
                }

            } catch (Exception myException) {

                CsiPersistenceManager.rollback();
                LOG.error("Caught exception removing file artifacts for DataView "
                        + Format.value(dataViewIn) + ":\n"
                        + Format.value(myException));
                return false;
            }
        }
        return true;
    }

    private boolean removeDataView(DataView dataViewIn) {

        if (0 < _removeDataViewTriesLeft--) {

            try {

                CsiPersistenceManager.begin();
                CsiPersistenceManager.deleteForSystem(dataViewIn);
                CsiPersistenceManager.commit();
                _removeDataViewTriesLeft = 0;

            } catch (Exception myException) {

                CsiPersistenceManager.rollback();
                LOG.error("Caught exception deleting DataView "
                        + Format.value(dataViewIn) + ":\n"
                        + Format.value(myException));
                return false;
            }

        } else {

            dataViewIn.setResourceType(AclResourceType.CONNECTION.BROKEN);
            CsiPersistenceManager.merge(dataViewIn);
            CsiPersistenceManager.commit();
        }
        return true;
    }
}
