package csi.server.business.cachedb;

import java.util.Set;

import csi.security.Authorization;
import csi.server.common.model.dataview.DataView;


public class DataSyncContext {

    public static class Builder {
        public static Builder newBuilder() { 
            return new Builder();
        }
        
        private DataView dv;
        private Authorization authz;
        
        private Builder() {}
        
        public Builder setAuthorization( Authorization authorization ) {
            this.authz = authorization;
            return this;
        }
        
        public Builder setDataView( DataView dataview ) {
            this.dv = dataview;
            return this;
        }

        public DataSyncContext build() {
            return new DataSyncContext( authz, dv );
        }

        public DataSyncContext build(Set<DataSyncListener> listenersIn) {
            return new DataSyncContext( authz, dv, listenersIn );
        }
    }
    
    protected Authorization authorization;
    protected DataView dataView;
    protected Set<DataSyncListener> listeners;

    public DataSyncContext( Authorization auth, DataView dv ) {
        this.authorization = auth;
        this.dataView = dv;
    }

    public DataSyncContext( Authorization auth, DataView dv, Set<DataSyncListener> listenersIn) {
        this.authorization = auth;
        this.dataView = dv;
        this.listeners = listenersIn;
    }

    public Set<DataSyncListener> getListeners() {
        return listeners;
    }

    public Authorization getAuthorization() {
        return authorization;
    }

    public DataView getDataView() {
        return dataView;
    }

}
