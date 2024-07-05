package csi.server.task.api.callback;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import csi.server.task.core.StatusUpdateMonitor;


/**
 * Keeps the registered <code>CallbackContext</code> and <code>CallbackConsumer</code> entities.
 * The scope for a registered <code>CallbackContext</code> is the associated task execution time.
 * The scope for a registered <code>CallbackConsumer</code> is the <code>TaskStatusServlet</code> lifetime.
 *  
 * @author dorel.matei
 * 
 */
public class CallbackRegistry {

    private StatusUpdateMonitor statusUpdateMonitor;
    private Map<String, CallbackContext> callbackContexts = new ConcurrentHashMap<String, CallbackContext>();
    private Map<String, CallbackConsumer> callbackConsumers = new ConcurrentHashMap<String, CallbackConsumer>();
   
    private static CallbackRegistry callbackRegistry = new CallbackRegistry();

    private CallbackRegistry() {
       this.statusUpdateMonitor = new StatusUpdateMonitor(); 
    }
    
    public static CallbackRegistry getInstance() {
        return callbackRegistry;
    }
    
    public StatusUpdateMonitor getStatusUpdateMonitor() {
        return statusUpdateMonitor;
    }
    
    /**
     * Once a new <code>CallbackContext</code> is registered, the interested parties 
     * like <code>PooledResultProcessor</code> are notified about its existence.
     */
    public void registerCallbackContext(String clientId, CallbackContext callbackContext) {
        callbackContexts.put(clientId, callbackContext);
        synchronized (statusUpdateMonitor) {
            statusUpdateMonitor.incrementUpdateCount();
            statusUpdateMonitor.notify();
        }
    }

    public CallbackContext unregisterCallbackContext(String clientId) {
        return callbackContexts.remove(clientId);
    }
    
    public Set<String> getRegisteredClientIds() {
        return callbackContexts.keySet();
    }
    
    public void registerCallbackConsumer(String taskType, CallbackConsumer callbackConsumer) {
        callbackConsumers.put(taskType, callbackConsumer);
    }
    
    public CallbackConsumer unregisterCallbackConsumer(String taskType) {
        return callbackConsumers.remove(taskType); 
    }
    
    public CallbackConsumer getCallbackConsumer(String taskType) {
        return callbackConsumers.get(taskType);
    }
}
