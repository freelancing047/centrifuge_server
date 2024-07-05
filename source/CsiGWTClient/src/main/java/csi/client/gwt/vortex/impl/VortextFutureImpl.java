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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

import csi.client.gwt.vortex.MetaCapable;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.shared.gwt.vortex.VortexResponse;
import csi.shared.gwt.vortex.VortexService;
import csi.shared.gwt.vortex.impl.SerializableValueImpl;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class VortextFutureImpl<R> implements VortexFuture<R>, VortexFutureSpi<R> {

    private VortexSpi vortex;
    private String taskId;
    private String dataViewUuid;
    @SuppressWarnings("rawtypes")
    private List<VortexEventHandler> handlers = new ArrayList<VortexEventHandler>();
    private Map<String, SerializableValueImpl> meta = new HashMap<String, SerializableValueImpl>();

    // API interface

    @Override
    public void addEventHandler(VortexEventHandler<R> eventHandler) {
        handlers.add(eventHandler);
    }

    @Override
    public <V extends VortexService> V execute(Class<V> clz) {
        return vortex.executeForFuture(meta, this, clz);
    }
    


    @Override
    public <V extends VortexService> V execute(Class<V> clz, String resourceUuid) {
        return vortex.executeForFuture(meta, this, clz, resourceUuid);
    }

    // SPI interface

    @Override
    public void setVortex(VortexSpi vortex) {
        this.vortex = vortex;
    }

    @Override
    public void setMeta(Map<String, SerializableValueImpl> meta) {
        this.meta = meta;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fireSuccess(R result) {
        for (VortexEventHandler<R> handler : handlers) {
            // Delay setting metadata so that handlers can potentially process metadata set by filters.
            setEventHandlerMeta(handler);
            handler.onSuccess(result);
        }
    }

    @Override
    public boolean fireFailure(Throwable t) {
        boolean continueFailureHandling = true;
        for (VortexEventHandler<?> handler : handlers) {
            setEventHandlerMeta(handler);
            continueFailureHandling = continueFailureHandling & handler.onError(t);
        }
        return continueFailureHandling;
    }

    @Override
    public void fireUpdate(VortexResponse response) {
        for (VortexEventHandler<R> handler : handlers) {
            // Delay setting metadata so that handlers can potentially process metadata set by filters.
            setEventHandlerMeta(handler);
            handler.onUpdate(response.getTaskProgess(), response.getTaskMessage());
        }
    }

    private void setEventHandlerMeta(VortexEventHandler<?> eventHandler) {
        if (eventHandler instanceof MetaCapable) {
            MetaCapable mc = (MetaCapable) eventHandler;
            Map<String, Serializable> map = new HashMap<String, Serializable>();
            for (String key : meta.keySet()) {
                map.put(key, meta.get(key).<Serializable> getValue());
            }
            mc.setMeta(map);
        }
    }

	@Override
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	@Override
	public void cancel(AsyncCallback<Void> callback) {
        vortex.cancel(taskId, callback);
    }
	
	@Override
	public void fireCancel() {
		for (VortexEventHandler<R> handler : handlers) {
            // Delay setting metadata so that handlers can potentially process metadata set by filters.
            setEventHandlerMeta(handler);
            handler.onCancel();
        }
	}

}
