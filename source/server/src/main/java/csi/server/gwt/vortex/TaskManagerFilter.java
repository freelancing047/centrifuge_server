/**
 *  Copyright (c) 2008-2013 Centrifuge Systems, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered
 *  into with Centrifuge Systems.
 *
 **/
package csi.server.gwt.vortex;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gwt.user.client.rpc.SerializationException;

import csi.server.common.exception.CentrifugeException;
import csi.server.task.TaskConstants;
import csi.server.task.api.TaskContext;
import csi.server.task.api.TaskController;
import csi.server.util.jogformer.Jogformer;
import csi.server.ws.async.TaskContextBuilder;
import csi.shared.gwt.vortex.VortexRequest;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TaskManagerFilter implements VortexServerFilter {
   private static final Logger LOG = LogManager.getLogger(TaskManagerFilter.class);

    @Autowired
    private BeanFactory beanFactory;

    @Inject
    private HttpAccessor httpAccessor;

    public HttpAccessor getHttpAccessor() {
        return httpAccessor;
    }

    public void setHttpAccessor(HttpAccessor httpAccessor) {
        this.httpAccessor = httpAccessor;
    }

    @Override
    public <R> R filter(VortexInvocationContext invocationContext, VortexRequest request, VortexFilterChain chain)
            throws SerializationException {
        Object[] parameters = new Object[request.getParameters().length];
        for (int i = 0; i < request.getParameters().length; i++) {
            parameters[i] = request.getParameters()[i].getValue();
        }

        final TaskContext taskContext;
        try {
            taskContext = TaskContextBuilder.buildVortexTaskContext(getHttpAccessor().getRequest(), getHttpAccessor()
                    .getResponse());
            taskContext.setTaskId(request.getTaskId());
            taskContext.setAdminTask(false);
            taskContext.setMethod(invocationContext.getMethod());
            taskContext.setMethodArgs(parameters);
            taskContext.setInterruptable(true);
            taskContext.setServicePath(null);
            taskContext.setCodec(null);
            taskContext.setType(TaskConstants.TASK_TYPE_REST);
            taskContext.setServiceClass(invocationContext.getServiceImplementation());
            taskContext.setGwtService(true);
            taskContext.setSynchronous(false);
            taskContext.setClientId(request.getClientId());
            taskContext.setResourceUuid(request.getResourceUuid());
//            if(invocationContext.getServiceImplementation().getClass().equals(FileActionsService.class )&& "getApplicationResource".equals(invocationContext.getMethod().getName())){
//                taskContext.setSynchronous(true);
//
//            }
            taskContext.setPostSessionJogformer(beanFactory.getBean("postSessionJogformer", Jogformer.class));
            taskContext.setPreSessionJogformer(beanFactory.getBean("preSessionJogformer", Jogformer.class));

            //support sub-executor pooling
            taskContext.setExecutionPoolId(request.getExecutionPoolId());

            LOG.trace(String.format("(Task ID: %1$s) Task Context Built", taskContext.getTaskId()));
        } catch (CentrifugeException e) {
            throw new RuntimeException(e);
        }
        LOG.trace(String.format("(Task ID: %1$s) Running Task", taskContext.getTaskId()));

        request.setTaskId(taskContext.getTaskId());
        TaskController.getInstance().submitTask(taskContext);

        LOG.trace(String.format("(Task ID: %1$s) Task Complete", taskContext.getTaskId()));
        return chain.<R>filter(invocationContext, request);
    }
}
