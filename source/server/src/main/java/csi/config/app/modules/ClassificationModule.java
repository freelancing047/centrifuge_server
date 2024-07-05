package csi.config.app.modules;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import csi.server.business.cachedb.DataSyncListener;
import csi.server.business.cachedb.spi.USStandardClassificationListener;

public class ClassificationModule
    extends AbstractModule
{

    @Override
    protected void configure() {
        Multibinder<DataSyncListener> binder = Multibinder.newSetBinder(binder(), DataSyncListener.class);
        binder.addBinding().to(USStandardClassificationListener.class);
    }

}
