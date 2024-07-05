package csi.server.business.service.widget.common.data;

/**
 * Server response object
 */
public class ServerResponse {

    /** Javscript snippet to execute on client */
    private String javaScript;

    /** Response object to send to client */
    private Object response;

    public String getJavaScript() {
        return javaScript;
    }

    public void setJavaScript(String javaScript) {
        this.javaScript = javaScript;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "ServerResponse{" + "javaScript='" + javaScript + '\'' + ", response=" + response + '}';
    }
}
