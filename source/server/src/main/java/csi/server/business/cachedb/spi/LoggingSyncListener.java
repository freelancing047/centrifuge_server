package csi.server.business.cachedb.spi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.cachedb.DataSyncContext;
import csi.server.business.cachedb.DataSyncListener;
import csi.server.business.cachedb.MergeContext;
import csi.server.common.model.extension.Classification;

public class LoggingSyncListener implements DataSyncListener {
   private static final Logger LOG = LogManager.getLogger(LoggingSyncListener.class);

    @Override
    public void onStart(DataSyncContext context) {
       LOG.info( "start");
    }

    @Override
    public void onComplete(DataSyncContext context) {
       LOG.info( "complete");
    }

    @Override
    public void onError(DataSyncContext context) {
       LOG.error( "error" );
    }

    @Override
    public void onMergeStart(MergeContext context) {
       LOG.info( "merge.start");
    }

    @Override
    public void onMergeComplete(MergeContext context) {
       LOG.info( "merge.complete");
    }
    
    protected void log( String action ) {
       LOG.info("LoggingSyncListener: " + action );
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public boolean providesSupport(String categoryName) {

        return Classification.NAME.equals(categoryName);
    }

    @Override
    public boolean isRequired() {
        return false;
    }
    

}
