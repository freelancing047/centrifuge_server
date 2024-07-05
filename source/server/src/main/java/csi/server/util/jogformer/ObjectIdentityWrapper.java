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
package csi.server.util.jogformer;

import org.apache.commons.lang.ObjectUtils;

/**
 * Handles getting a memory handle to an object.
 * @author Centrifuge Systems, Inc.
 *
 */
public class ObjectIdentityWrapper {

    private Object reference;

    public Object getReference() {
        return reference;
    }

    public void setReference(Object reference) {
        this.reference = reference;
    }

    @Override
    public int hashCode() {
        return ObjectUtils.identityToString(getReference()).hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof ObjectIdentityWrapper
                && getReference() == ((ObjectIdentityWrapper) obj).getReference();
    }

}
