package csi.server.common.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.sql.Timestamp;
import java.util.Date;

public class ReportsDisplay implements IsSerializable {

    private long id;
    private Date date;
    private Integer activeUsers;
    private Integer concurrentUsers;
    private Integer maxLoginFailed;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() { return date; }

    public void setDate(Date date) { this.date = date; }

    public Integer getActiveUsers() { return activeUsers; }

    public void setActiveUsers(Integer activeUsers) { this.activeUsers = activeUsers; }

    public Integer getConcurrentUsers() { return concurrentUsers; }

    public void setConcurrentUsers(Integer concurrentUsers) { this.concurrentUsers = concurrentUsers; }

    public Integer getMaxLoginFailed() { return maxLoginFailed; }

    public void setMaxLoginFailed(Integer maxLoginFailed) { this.maxLoginFailed = maxLoginFailed; }
}
