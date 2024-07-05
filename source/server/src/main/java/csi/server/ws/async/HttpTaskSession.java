package csi.server.ws.async;

import javax.servlet.http.HttpSession;

import csi.server.task.api.TaskSession;

/**
 * Wraps an http session so the task can maintain
 * state between http requests.
 * 
 * Copying the contents of the session into the
 * a BusinessContext instance and putting that instance
 * into the HTTP session is overly complex.  
 * 
 * In addition it is not sufficient since HTTP session replication
 * is notified of changes to a session attribute if the
 * HttpSession.setAttribute() is called.  There is no notification
 * if the contents of the attribute value is changed.
 * 
 * For example, this will NOT notify session replication that the 
 * attribute "myobject" has been updated:
 *     BusinessContext ctx = HttpSession.getAttribute("myobject");
 *     ctx.setAttribute("blah", value);
 * 
 */
public class HttpTaskSession implements TaskSession {

    HttpSession httpSession = null;
    private boolean validSession;

    public HttpTaskSession(HttpSession httpSession) {
        super();
        this.httpSession = httpSession;
        this.validSession = true;
    }

    @Override
    public String getId() {
        return this.httpSession.getId();
    }

    @Override
    public Object getAttribute(String key) {
        synchronized (this.httpSession) {
            try {
                return this.httpSession.getAttribute(key);
            } catch (IllegalStateException ise) {
                this.validSession = false;
                return null;
            }
        }
    }

    @Override
    public void removeAttribute(String key) {
        synchronized (this.httpSession) {
            try {
                this.httpSession.removeAttribute(key);
            } catch (IllegalStateException ise) {
                this.validSession = false;
            }
        }
    }

    @Override
    public void setAttribute(String key, Object value) {
        synchronized (this.httpSession) {
            try {
                this.httpSession.setAttribute(key, value);
            } catch (IllegalStateException ise) {
                this.validSession = false;
            }
        }
    }

    @Override
    public <T> T setAttributeIfAbsent(String key, T value) {
        synchronized (this.httpSession) {
            try {
                T current = (T) this.httpSession.getAttribute(key);
                if (current == null) {
                    this.httpSession.setAttribute(key, value);
                    return value;
                } else {
                    return current;
                }
            } catch (IllegalStateException ise) {
                this.validSession = false;
                return value;
            }
        }
    }

    @Override
    public void removeAttribute(String key, Object value) {
        synchronized (this.httpSession) {
            try {
                Object current = this.httpSession.getAttribute(key);
    
                if (current != null && current.equals(value)) {
                    this.httpSession.removeAttribute(key);
                }
            } catch (IllegalStateException ise) {
                this.validSession = false;
            }
        }
    }

    public boolean isValidSession() {
        if (validSession) {
            getAttribute("DoubleCheck");
        }
        return validSession;
    }

    public void setValidSession(boolean validSession) {
        this.validSession = validSession;
    }
}
