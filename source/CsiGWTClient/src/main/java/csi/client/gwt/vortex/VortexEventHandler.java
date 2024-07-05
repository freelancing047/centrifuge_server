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

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface VortexEventHandler<R> {

    public void onSuccess(R result);

    /**
     * @return true to let the default error handler process this as well, false to prevent default error handler from
     * handling this.
     */
    public boolean onError(Throwable t);

    public void onUpdate(int taskProgess, String taskMessage);
    
    public void onCancel();
}
