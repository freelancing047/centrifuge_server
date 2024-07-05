package csi.server.common.dto.user;

import com.google.gwt.user.client.rpc.IsSerializable;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.exception.CentrifugeException;
import org.hibernate.annotations.*;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Created by centrifuge on 11/13/2017.
 */
@Entity
@Table(
        name="recentaccess",
        indexes = {
                @Index(columnList = "logonid", name = "idx_recentaccess_logonid", unique = false),
                @Index(columnList = "resourceid", name = "idx_recentaccess_resourceid", unique = false)
        })
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RecentAccess implements IsSerializable {

    @Id
    private String key;
    private String logonId;
    private String resourceId;
    private String name;
    private Date lastAccess;

    public RecentAccess() {

    }

    public RecentAccess(String logonIdIn, String resourceIdIn, String nameIn) throws CentrifugeException {

        if ((null != logonIdIn) && (null != resourceIdIn)) {

            logonId = logonIdIn;
            resourceId = resourceIdIn;
            key = resourceId.toLowerCase() + ":" + logonId.toLowerCase();
            name = nameIn;
            lastAccess = new Date();
        } else {

            throw new CentrifugeException("Record Access request missing necessary information.");
        }
    }

    public String getKey() {

        return key;
    }

    public void setKey(String keyIn) {

        key = keyIn;
    }

    public String getLogonId() {

        return logonId;
    }

    public void setLogonId(String logonIdIn) {

        logonId = logonIdIn;
    }

    public String getResourceId() {

        return resourceId;
    }

    public void setResourceId(String resourceIdIn) {

        resourceId = resourceIdIn;
    }

    public String getName() {

        return name;
    }

    public void setName(String nameIn) {

        name = nameIn;
    }

    public Date getLastAccess() {

        return lastAccess;
    }

    public void setLastAccess(Date lastAccessIn) {

        lastAccess = lastAccessIn;
    }
}
