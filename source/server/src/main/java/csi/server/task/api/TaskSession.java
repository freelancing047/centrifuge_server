package csi.server.task.api;

/**
 * Represents the session data for a task.  This allows a task to 
 * maintain some state between task execution.  
 * 
 * 
 */
public interface TaskSession {

    public String getId();

    public Object getAttribute(String key);

    public void setAttribute(String key, Object value);

    public void removeAttribute(String key);

    public <T> T setAttributeIfAbsent(String key, T value);

    public void removeAttribute(String key, Object value);

    public boolean isValidSession();
}
