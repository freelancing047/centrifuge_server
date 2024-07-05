package csi.server.business.service.widget.common.data;

/**
 * Client request data object.
 * Data object populated based on reqeust from the client widget
 */
public class ClientRequest {

    /** Action to process on content */
    private String action;

    /** Actual request content */
    private Object content;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
