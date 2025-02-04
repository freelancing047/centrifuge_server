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
package csi.shared.gwt.vortex.impl;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.shared.gwt.vortex.SerializableValue;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SerializableValueImpl implements SerializableValue, IsSerializable {

    private Serializable serializable;
    private IsSerializable legacy;

    @Override
    public void setValue(Object value) {
        if (value instanceof Serializable) {
            serializable = (Serializable) value;
        } else if (value instanceof IsSerializable) {
            legacy = (IsSerializable) value;
        } else if (value != null) {
            throw new RuntimeException(value.getClass() + " is neither Serializable nor IsSerializable");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        if (legacy != null) {
            return (T) legacy;
        } else {
            return (T) serializable;
        }
    }
}
