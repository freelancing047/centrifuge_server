/**
 * Copyright 2013 Centrifuge Systems, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package csi.client.gwt.vortex.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.ExceptionHandler;
import csi.client.gwt.vortex.Vortex;
import csi.client.gwt.vortex.VortexFuture;
import csi.shared.gwt.vortex.VortexService;
import csi.shared.gwt.vortex.impl.SerializableValueImpl;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class VortexShell implements Vortex {

    private Map<String, SerializableValueImpl> meta = new HashMap<String, SerializableValueImpl>();
    private VortexSpi vortex;

    public VortexShell(VortexSpi vortex) {
        super();
        this.vortex = vortex;
    }

    @Override
    public Vortex withMeta(String name, Serializable value) {
        SerializableValueImpl impl = new SerializableValueImpl();
        impl.setValue(value);
        meta.put(name, impl);
        return this;
    }

    @Override
    public <R> VortexFuture<R> createFuture() {
        VortexFuture<R> future = ((Vortex) vortex).createFuture();
        ((VortexFutureSpi<R>) future).setMeta(meta);
        return future;
    }

    @Override
    public <V extends VortexService> V execute(Class<V> clz) {
        return execute(new Callback<Object>() {

            @Override
            public void onSuccess(Object result) {
                // noop
            }
        }, clz);
    }

    @Override
    public <V extends VortexService, R> V execute(Callback<R> callback, Class<V> clz) {
        return execute(new ExceptionHandler() {

            @Override
            public boolean handle(Throwable t) {
                return true;
            }
        }, callback, clz);
    }

    @Override
    public <V extends VortexService, R> V execute(ExceptionHandler handler, Callback<R> callback, Class<V> clz) {
        return vortex.execute(meta, handler, callback, clz);
    }

}
