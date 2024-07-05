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
package csi.shared.gwt.vortex;


/**
 * Marker interface that all interfaces that want to be "RPC enabled" should implement.
 * Limitations: Service methods cannot have native java arrays as return types (e.g. String[]). GWT's serializer 
 * incorrectly attempts to see if [Ljava.lang.String; is serializable and fails the check!
 * 
 * @author Centrifuge Systems, Inc.
 */
public interface VortexService {

}
