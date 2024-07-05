package csi.server.common.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.sql.Timestamp;
import java.util.Date;

public class EventsDisplay implements IsSerializable {

    private long id;
    private String timestamp;
    private String userId;
    private String event;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
