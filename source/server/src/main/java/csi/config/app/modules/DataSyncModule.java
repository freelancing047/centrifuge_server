/**
 * 
 */
package csi.config.app.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Stage;
import com.google.inject.multibindings.Multibinder;

import csi.server.business.cachedb.DataSyncListener;
import csi.server.business.cachedb.spi.LoggingSyncListener;
import csi.server.business.cachedb.spi.SecurityLabelsListener;
import csi.server.business.cachedb.spi.USStandardClassificationListener;

/**
 * @author Centrifuge Systems, Inc.
 * 
 */
public class DataSyncModule
    extends AbstractModule
{

    /*
     * (non-Javadoc)
     * 
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {

        // bind in our simple logger only in dev modes
        Multibinder<DataSyncListener> binder = Multibinder.newSetBinder(binder(), DataSyncListener.class);
        if (currentStage() == Stage.TOOL || currentStage() == Stage.DEVELOPMENT) {
            binder.addBinding().to(LoggingSyncListener.class);
        }

        binder.addBinding().to(USStandardClassificationListener.class);
        binder.addBinding().to(SecurityLabelsListener.class);
        

        /*
         * configure the classes for this module.
         */
        


    }

}
