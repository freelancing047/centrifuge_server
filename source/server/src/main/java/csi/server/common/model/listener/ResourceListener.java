package csi.server.common.model.listener;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import csi.server.common.model.Resource;

public class ResourceListener {

    @PrePersist
    public void prePersist(Resource res) {
        res.resetDates();
    }

    @PreUpdate
    public void preUpdate(Resource res) {
        res.updateLastUpdate();
    }
}
