package csi.config.app.modules;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import csi.server.business.cachedb.DataSyncListener;
import csi.server.business.cachedb.spi.LoggingSyncListener;
import csi.server.business.helper.DataCacheHelper;

public class LoggingModule
    extends AbstractModule
{

    @Override
    protected void configure() {
        log("Dynamic Logging Module configuration starting.");
        Multibinder<DataSyncListener> binder = Multibinder.newSetBinder(binder(), DataSyncListener.class);
        binder.addBinding().to(LoggingSyncListener.class);
        requestStaticInjection(DataCacheHelper.class);

    }

    private void log(String msg) {
        System.out.println(msg);
    }

}
