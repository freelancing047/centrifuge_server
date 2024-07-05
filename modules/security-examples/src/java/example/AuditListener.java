package example;

import org.apache.log4j.Logger;

import csi.security.Authorization;
import csi.server.business.cachedb.DataSyncContext;
import csi.server.business.cachedb.DataSyncListener;
import csi.server.business.cachedb.MergeContext;
import csi.server.common.model.dataview.DataView;

/**
 * 
 * This class provides a sample implementation of a DataSyncListener.
 * 
 * The DataSyncListener interface enables hooking into the data synchronization
 * phase. Instances of this class are notified at two times during the data
 * synchronization phase: at the beginning and at the end. The <i>end</i> phase
 * is segregated into success and failure conditions (i.e. onComplete and
 * onError).
 * <p>
 * In addition to the standard processing for a Dataview, the DataSyncListener
 * interface contains methods for notification of merge requests.
 * <p>
 * 
 * 
 * 
 * @author Centrifuge Systems, Inc.
 */
public class AuditListener
    implements DataSyncListener
{

    Logger log = Logger.getLogger(AuditListener.class);

    @Override
    public String getName() {
        return "Audit Reporting Listener";
    }

    @Override
    public String getCategoryName() {
        return "audit";
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public void onStart(DataSyncContext context) {
        Authorization auth = context.getAuthorization();
        DataView dv = context.getDataView();
        String msg = String.format("%1$s is creating or refreshing %2$s", auth.getName(), dv.getName());
        log.info(msg);
    }

    @Override
    public void onComplete(DataSyncContext context) {
        Authorization auth = context.getAuthorization();
        DataView dv = context.getDataView();
        String msg = String.format("Query complete for %1$s acessing Dataview: %2$s", auth.getName(), dv.getName());
        log.info(msg);
    }

    @Override
    public void onError(DataSyncContext context) {
        Authorization auth = context.getAuthorization();
        DataView dv = context.getDataView();
        String msg = String.format("Query failed for %1$s acessing Dataview: %2$s", auth.getName(), dv.getName());
        log.info(msg);
    }

    @Override
    public void onMergeStart(MergeContext context) {
        // skipping the start of a merge.
    }

    @Override
    public void onMergeComplete(MergeContext context) {
        Authorization auth = context.getAuthorization();
        DataView source = context.getSource();
        DataView target = context.getTarget();
        String msg = String.format("Finished merging %1$s into %2$s for user %3$s", source.getName(), target.getName(), auth.getName());
        log.info(msg);
    }

}
