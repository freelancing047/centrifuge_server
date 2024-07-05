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
package csi.client.gwt.vortex;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractVortexEventHandler<R> implements VortexEventHandler<R>, MetaCapable {

    private Map<String, Serializable> meta;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Serializable> T getMeta(String key) {
        return (T) meta.get(key);
    }

    @Override
    public void setMeta(Map<String, Serializable> meta) {
        this.meta = meta;
    }

    @Override
    public boolean onError(Throwable t) {
        return true;
    }

    @Override
    public void onUpdate(int taskProgess, String taskMessage) {
        //no-op
    }

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
		
	}
}