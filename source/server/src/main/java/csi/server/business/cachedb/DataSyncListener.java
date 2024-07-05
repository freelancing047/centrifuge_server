package csi.server.business.cachedb;

import csi.config.ConfigurationException;

/**
 * Listener interface for receiving notifications of the lifecycle steps
 * while synchronizing data for a DataView/Dataset.  
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public interface DataSyncListener {
    
    /**
     * Provides an internal naming mechansim for this listener.
     * @return
     */
    String getName();
    
    /**
     * Identifies which category of processing this listener supports.  The UI should be leveraging this
     * for determining whether 
     * @return
     */
    boolean providesSupport(String categoryName);

    /**
     * Identifies whether this listener is required to be configured.  Each listener is responsible for actively 
     * enforcing this constraint.  The ideal location is in the callback methods onStart and onMergeStart.
     * <p>
     * Listeners that need to flag a run-time error should throw a {@link ConfigurationException}.
     * 
     * @return
     */
    boolean isRequired();
    
    /**
     * Callback method to notify this listener when the initial phase of synchronizing data is started.
     * @param context
     */
    void onStart( DataSyncContext context);
    
    /**
     * Callback method to notify this listener when the data for a particular Dataview is synchronized
     * and present in the local data cache.
     * @param context
     */
    void onComplete( DataSyncContext context );
    
    /**
     * Callback method to notify this listener if there is an error during the synchronization process.
     * @param context
     */
    void onError( DataSyncContext context );
    
    /**
     * Callback method to notify this listener that a merge of two Dataviews is about to commence.
     * @param context
     */
    void onMergeStart( MergeContext context );
    
    /**
     * Callback method to notify this listener that a merge of two Dataviews is complete.
     * @param context
     */
    void onMergeComplete( MergeContext context );

}
