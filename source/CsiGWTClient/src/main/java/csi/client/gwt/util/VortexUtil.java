package csi.client.gwt.util;

import java.util.Collection;

import com.google.common.collect.Lists;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.exception.CentrifugeException;

/**
 * Created by Patrick on 6/30/2014.
 */
public class VortexUtil {

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    public static <T> void afterAllFutures(final Collection<VortexFuture<T>> futures, final VortexEventHandler<Collection<T>> handler){
        final int size = futures.size();
        AbstractVortexEventHandler<T> eventHandler = new AbstractVortexEventHandler<T>() {
            int successes = 0;
            int errors = 0;
            Throwable exception = null;
            Collection<T> results = Lists.newArrayList();

            @Override
            public boolean onError(Throwable exceptionIn){

                errors++;

                if (null == exception) {

                    exception = exceptionIn;
                }
                return false;
            }

            @Override
            public void onSuccess(T result) {
                successes++;
                results.add(result);
                if (successes == size) {
                    handler.onSuccess(results);

                } else if ((successes+ errors) == size) {

                    handler.onError((null != exception) ? exception : new CentrifugeException(_constants.saveDataViewAs_UnknownError()));
                }
            }
        };
        for (VortexFuture future : futures) {

            future.addEventHandler(eventHandler);
        }

    }
}
