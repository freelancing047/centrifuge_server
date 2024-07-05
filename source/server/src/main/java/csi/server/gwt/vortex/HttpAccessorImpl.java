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
package csi.server.gwt.vortex;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class HttpAccessorImpl implements HttpAccessor, HttpAccessorSpi {

    private ThreadLocal<HttpAccessorContainer> container = new ThreadLocal<HttpAccessorContainer>() {

        @Override
        protected HttpAccessorContainer initialValue() {
            return new HttpAccessorContainer();
        }
    };


    @Override
    public HttpServletRequest getRequest() {
        return container.get().getRequest();
    }


    @Override
    public HttpServletResponse getResponse() {
        return container.get().getResponse();
    }


    public void setRequest(HttpServletRequest request) {
        container.get().setRequest(request);
    }


    public void setResponse(HttpServletResponse response) {
        container.get().setResponse(response);
    }


    @Override
    public void clear() {
        container.remove();
    }
}
