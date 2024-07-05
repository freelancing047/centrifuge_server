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

import java.util.ArrayList;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;

import csi.server.message.MessageBroker;
import csi.server.task.api.TaskContext;
import csi.server.task.api.TaskController;
import csi.server.task.api.TaskStatus;
import csi.server.util.DependencyInjector;
import csi.server.util.PerfTimer;
import csi.shared.gwt.vortex.VortexRequest;
import csi.shared.gwt.vortex.VortexResponse;
import csi.shared.gwt.vortex.VortexRpcDispatcher;
import csi.shared.gwt.vortex.impl.SerializableValueImpl;

/**
 * The core servlet that handles all RPC requests using the Vortex design.
 * @author Centrifuge Systems, Inc.
 */
@SuppressWarnings("serial")
public class VortexServlet extends RemoteServiceServlet implements VortexRpcDispatcher {
   private static final Logger LOG = LogManager.getLogger(VortexServlet.class);

    @Inject
    private VortexServiceRegistry rpcServiceRegistry;

    public VortexServiceRegistry getRpcServiceRegistry() {
        return rpcServiceRegistry;
    }

    public void setRpcServiceRegistry(VortexServiceRegistry rpcServiceRegistry) {
        this.rpcServiceRegistry = rpcServiceRegistry;
    }

    @Inject
    private HttpAccessorSpi httpAccessor;

    public HttpAccessorSpi getHttpAccessor() {
        return httpAccessor;
    }

    public void setHttpAccessor(HttpAccessorSpi httpAccessor) {
        this.httpAccessor = httpAccessor;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = config.getServletContext();
        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
        ((DependencyInjector) wac.getBean("di")).bind(this);
    }

    @Override
    protected SerializationPolicy doGetSerializationPolicy(HttpServletRequest request, String moduleBaseURL,
            String strongName) {

        return super.doGetSerializationPolicy(request, moduleBaseURL, strongName);
    }

    @Override
    protected void doUnexpectedFailure(Throwable e) {
        super.doUnexpectedFailure(e);
        try {
            getHttpAccessor().clear();
        } catch (IllegalStateException ise) {
           LOG.error(ise.getMessage(), ise);
        }
        LOG.error(e.getMessage(), e);
    }

    @Override
    public void log(String s, Throwable e){

    }

    @Override
    public VortexResponse dispatch(VortexRequest request) throws SerializationException {
        VortexInvocationContext context = getRpcServiceRegistry().getInvocationContextsByMethodSignature().get(
                request.getMethodSignature());

//        if(context.getServiceImplementation().getClass().equals(FileActionsService.class)&& "getApplicationResource".equals(context.getMethod().getName())){
//            return dispatchOld(request);
//        }

        if (context == null) {
            throw new RuntimeException("Unknown invocation passed: " + request.getMethodSignature());
        }

        LOG.trace(String.format("Recieved Vortex call: %1$s", request.getMethodSignature()));
        VortexFilterChainImpl chain = new VortexFilterChainImpl();
        chain.setFilters(getRpcServiceRegistry().getFilters());

        getHttpAccessor().setRequest(getThreadLocalRequest());
        getHttpAccessor().setResponse(getThreadLocalResponse());

        try {
            chain.filter(context, request);
        } catch (RuntimeException e) {
            Throwable t = ExceptionUtils.getRootCause(e);
            t = (t == null) ? e : t;
            if (t instanceof SerializationException) {
                throw (SerializationException) t;
            } else {
                // Get the message and throwable explicitly to aid debugging.
                String errorMessage = ExceptionUtils.getRootCauseMessage(e);
                LOG.trace(
                        String.format("Error executing Vortex call: %1$s (%2$s)", request.getMethodSignature(),
                                t.getMessage()), t
                );
                throw new SerializationException(errorMessage, t);
            }
        } finally {
            getHttpAccessor().clear();
        }
        VortexResponse response = formatResponse(request);
        return response;
    }

    @Override
    public ArrayList<VortexResponse> getMessages(String clientId) throws SerializationException {
        return  MessageBroker.get().getMessages(clientId);

    }

    @Override
    public void cancelTask(String taskId) throws SerializationException {
        TaskController.getInstance().cancelTask(taskId);
    }

    public VortexResponse dispatchOld(VortexRequest request) throws SerializationException {
        VortexInvocationContext context = getRpcServiceRegistry().getInvocationContextsByMethodSignature().get(
                request.getMethodSignature());

        if (context == null) {
            throw new RuntimeException("Unknown invocation passed: " + request.getMethodSignature());
        }

        LOG.trace(String.format("Recieved Vortex call: %1$s", request.getMethodSignature()));
        VortexFilterChainImpl chain = new VortexFilterChainImpl();
        chain.setFilters(getRpcServiceRegistry().getFilters());

        getHttpAccessor().setRequest(getThreadLocalRequest());
        getHttpAccessor().setResponse(getThreadLocalResponse());

        PerfTimer timer = new PerfTimer().start(request.getMethodSignature());

        try {
            chain.filter(context, request);
        } catch (RuntimeException e) {
            Throwable t = ExceptionUtils.getRootCause(e);
            t = (t == null) ? e : t;
            if (t instanceof SerializationException) {
                throw (SerializationException) t;
            } else {
                // Get the message and throwable explicitly to aid debugging.
                String errorMessage = ExceptionUtils.getRootCauseMessage(e);
                LOG.trace(
                        String.format("Error executing Vortex call: %1$s (%2$s)", request.getMethodSignature(),
                                t.getMessage()), t
                );
                throw new SerializationException(errorMessage, t);
            }
        } finally {
            getHttpAccessor().clear();
        }
        VortexResponse response = formatResponseOld(request);

        timer.stopAndLog(String.format("Vortex call: %1$s", request.getMethodSignature()));

        return response;
    }

    private VortexResponse formatResponse(VortexRequest requestIn) {
        VortexResponse myResponse = new VortexResponse();
        SerializableValueImpl myValue = new SerializableValueImpl();
        myValue.setValue(null);
        myResponse.setResponse(myValue);
        myResponse.setException(null);
        return myResponse;
    }

    private VortexResponse formatResponseOld(VortexRequest requestIn) {

        VortexResponse myResponse = new VortexResponse();

        final String myTaskId = requestIn.getTaskId();
        final TaskController myTaskControler = TaskController.getInstance();
        final TaskContext myTaskContext = myTaskControler.getTaskContext(myTaskId);
        final TaskStatus myTaskStatus = myTaskContext.getStatus();

        synchronized (myTaskStatus) {

            final Object myTaskResultData = myTaskStatus.getResultData();
            SerializableValueImpl myValue = new SerializableValueImpl();

            myValue.setValue(myTaskResultData);
            myResponse.setResponse(myValue);
            myResponse.setException(myTaskStatus.getException());
        }

        return myResponse;
    }

}
