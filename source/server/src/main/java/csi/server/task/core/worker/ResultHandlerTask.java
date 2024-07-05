package csi.server.task.core.worker;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.log.LogThreadContextUtil;
import csi.server.common.codec.CodecType;
import csi.server.task.api.TaskContext;
import csi.server.task.api.TaskController;
import csi.server.task.api.TaskStatus;
import csi.server.task.api.callback.CallbackConsumer;
import csi.server.task.api.callback.CallbackContext;
import csi.server.task.api.callback.CallbackRegistry;
import csi.server.task.exception.TaskException;
import csi.server.ws.async.AsyncConstants;

/**
 * Extracts the <code>TaskContext</code> from client result queue, 
 * delegates the consuming to a <code>CallbackConsumer</code>, 
 * in case of consuming failure uses the fail-over mechanims by adding 
 * the <code>TaskContext</code> again in the queue.
 * 
 * @author dorel.matei
 *
 */
public class ResultHandlerTask implements Runnable {
   private static final Logger LOG = LogManager.getLogger(ResultHandlerTask.class);

    private static final int RETRY_TIMES = 5;
    private String clientId;

    private CallbackContext callbackContext;

    public ResultHandlerTask(String clientId, CallbackContext callbackContext) {
        this.clientId = clientId;
        this.callbackContext = callbackContext;
    }

    public void run() {
        TaskContext ctx = null;
        try {

            // Attempts to get a <code>TaskContext</code> from the client queue, without waiting if none is found.
            ctx = TaskController.getInstance().waitForUpdate(clientId, 0);
            try {
                if (ctx != null) {
                    LogThreadContextUtil.putContextThreadContext(ctx);

                    TaskStatus status = ctx.getStatus();
                    synchronized (status) {

                        // Clears remaining duplicates of this TaskStatus after getting it from Queue and before trying to send it
                        TaskController.getInstance().clearDuplicates2(ctx);

                        CallbackConsumer callbackConsumer = CallbackRegistry.getInstance().getCallbackConsumer(
                                ctx.getType());
                        if (callbackConsumer == null) {
                            throw new TaskException("No CallbackConsumer registered for type: " + ctx.getType());
                        }
                        boolean successfullyConsumed = callbackConsumer.consume(callbackContext, ctx);
                        if (successfullyConsumed) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Response sent for TaskId: " + status.getTaskId() + " and clientId: "
                                        + this.clientId);
                            }
                        } else {
                            ctx.setRetryCount(ctx.getRetryCount() + 1);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Could not send TaskStatus for TaskId: " + status.getTaskId()
                                        + " and clientId: " + this.clientId);
                            }

                            // If the retries number is exceeded the result is discarded
                            if (ctx.getRetryCount() <= RETRY_TIMES) {

                                // Adds <code>TaskStatus</code> back to Queue, and it will be again eligible for sending
                                TaskController.getInstance().queueUpdate(ctx);
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Retry Number: " + ctx.getRetryCount() + "for TaskId: "
                                            + status.getTaskId() + " and clientId: " + this.clientId);
                                }
                            } else {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Retry number exceeded. Task: " + status.getTaskId() + " is discarded.");
                                }
                            }
                        }

                    }
                }
            } finally {
                callbackContext.markComplete();
            }
        } catch (InterruptedException e) {
            if (ctx != null) {
                LOG.warn(
                        "Interrupted processor for clientId: " + clientId + ".  Dropped status for task:"
                                + ctx.getServicePath() + "/" + ctx.getMethodName(), e);
            } else {
                LOG.debug("Interrupted processor for clientId: " + clientId + ".  No status lost.");
            }
        } catch (Exception e) {
            LOG.error("Exception in TaskStatusWorker.run: ", e);
        } finally {
            LogThreadContextUtil.clearThreadContext();
        }
    }

    // TODO this method must be moved to a specific client task result consumer.

    public static void sendSynchronous(TaskContext task) throws TaskException {
        if (task.isGwtService()) {
            return;
        }

        if (task.isSynchronous()) {
            HttpServletResponse response = task.getSynchronousHttpResponse();
            if (response != null) {
                if (!response.isCommitted()) {
                	if (task.getServicePath() == null && task.getCodec() == null) {
                		return;
                	} else {
	                    response.setStatus(HttpServletResponse.SC_OK);
	                    response.setContentType(task.getCodec().getContentType());
	
	                    response.setCharacterEncoding(AsyncConstants.HTTP_HEADER_CHARACTER_ENCODING_UTF8);
	                    response.setHeader(AsyncConstants.HTTP_HEADER_CACHE_CONTROL, "no-cache");
	                    response.setHeader(AsyncConstants.HTTP_HEADER_PRAGMA, "no-cache");
	                    response.setDateHeader(AsyncConstants.HTTP_HEADER_EXPIRES, 0);
	                    response.setHeader(AsyncConstants.HTTP_HEADER_CLIENT_ID, task.getStatus().getClientId());
	                    response.setHeader(AsyncConstants.HTTP_HEADER_TASK_ID, task.getStatus().getTaskId());
	                    response.setHeader(AsyncConstants.HTTP_HEADER_TASK_STATUS, task.getStatus().getTaskStatus()
	                            .toString());
	
	                    OutputStream outs = null;
	                    try {
	                        outs = response.getOutputStream();
	                        Writer writer = new OutputStreamWriter(outs, "UTF-8");
	                        CodecType type = task.getCodec().getType();
	                        if (type != null && type != CodecType.JETTISON && type.isXml()) {
	                            writer.write(AsyncConstants.XML_PROLOG);
	                        }
	                        task.getCodec().marshal(task.getStatus(), writer);
	
	                        writer.flush();
	                        outs.flush();
	                        response.flushBuffer();
	                    } catch (IOException e) {
	                        throw new TaskException("Failed to send response", e);
	                    }
                    }
                }
            } else {
                throw new TaskException("No response found for synchronous task");
            }
        } else {
            throw new TaskException("Invalid action requested for asynchronous task");
        }
    }

}
