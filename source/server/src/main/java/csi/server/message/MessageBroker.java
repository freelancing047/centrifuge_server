package csi.server.message;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import csi.config.Configuration;
import csi.config.MessageBrokerConfig;
import csi.shared.gwt.vortex.VortexResponse;

public class MessageBroker {
   private static final Logger LOG = LogManager.getLogger(MessageBroker.class);

    private static MessageBroker instance;
    private ConcurrentHashMap<String, VortexResponse> lastResponseForTask;
    private volatile Cache<String, BlockingQueue<VortexResponse>> responseQueueCache;

    protected MessageBroker() {
        MessageBrokerConfig config = Configuration.getInstance().getMessageBrokerConfig();
        long size = config.getMaxCacheSize();
        long duration = config.getMaxIdleTimeDurationForQueue();
        TimeUnit unit = config.getMaxIdleTimeUnitForQueue();
        responseQueueCache = CacheBuilder.newBuilder().maximumSize(size).expireAfterAccess(duration, unit).build();
        lastResponseForTask = new ConcurrentHashMap<>();
    }

    public static MessageBroker get() {
        if (instance == null) {
            instance = new MessageBroker();
        }
        return instance;
    }

    public ArrayList<VortexResponse> getMessages(String queueName) {
        MessageBrokerConfig config = Configuration.getInstance().getMessageBrokerConfig();
        int timeout = config.getPollTimeoutDuration();
        TimeUnit timeUnit = config.getPollTimeoutUnit();
        return getMessages(queueName, timeout, timeUnit);
    }

    public ArrayList<VortexResponse> getMessages(String queueName, int timeout, TimeUnit timeUnit) {
        //defensive copy
        ArrayList<VortexResponse> responses;

        BlockingQueue<VortexResponse> responseQueue =  getResponseQueue(queueName);
        responses = new ArrayList<VortexResponse>();
        if (responseQueue.drainTo(responses) == 0) {
            //wait for a response...
            try {
                VortexResponse vortexResponse = responseQueue.poll(timeout, timeUnit);
                if (vortexResponse != null) {
                    responses.add(vortexResponse);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (VortexResponse response : responses) {
            lastResponseForTask.remove(response.getTaskId());
        }
        return responses;
    }


    public void publishObject(String queueName, VortexResponse obj) {
        if (LOG.isTraceEnabled()) {
           LOG.trace("publishing response");
        }
        BlockingQueue<VortexResponse> responseQueue =  getResponseQueue(queueName);
        {
           if (lastResponseForTask != null) {
              VortexResponse oldResponse = lastResponseForTask.get(obj.getTaskId());
              responseQueue.remove(oldResponse);
           }
        }
        if ((obj.getTaskId() == null) || (lastResponseForTask == null)) {
            return; //Null  guarding the tree...
        }
        lastResponseForTask.put(obj.getTaskId(), obj);
        if (!responseQueue.offer(obj)) {
            lastResponseForTask.remove(obj.getTaskId());
            if (LOG.isWarnEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("A single response queue has exceed its capacity. Item was not added.\n");
                sb.append("TaskId: ");
                sb.append(obj.getTaskId());
                sb.append("\n");
                sb.append("QueueName");
                sb.append(queueName);
                LOG.warn(sb.toString());
            }
        }
    }

    private BlockingQueue<VortexResponse> getResponseQueue(String queueName) {
        BlockingQueue<VortexResponse> responseQueue = responseQueueCache.getIfPresent(queueName);
        if (responseQueue == null) {
            MessageBrokerConfig config = Configuration.getInstance().getMessageBrokerConfig();
            int capacity = config.getInitialQueueSize();
            responseQueue = new ArrayBlockingQueue<VortexResponse>(capacity);
            responseQueueCache.put(queueName, responseQueue);
        }
        return responseQueue;
    }
}
