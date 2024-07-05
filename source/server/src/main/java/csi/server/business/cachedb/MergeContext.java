package csi.server.business.cachedb;

import csi.security.Authorization;
import csi.server.common.model.dataview.DataView;


public class MergeContext {

   
    public static class Builder {
        public static Builder newBuilder() {
            return new Builder();
        }
        
        private Authorization authz;
        private DataView target;
        private DataView source;
        private int gen;
        
        private Builder() { }
        
        public Builder setAuthorization( Authorization auth ) {
            this.authz = auth;
            return this;
        }
        
        public Builder setTarget( DataView dv ) {
            this.target = dv;
            return this;
        }
        
        public Builder setSource( DataView dv ) {
            this.source = dv;
            return this;
        }
        
        public Builder setGeneration( int generation ) {
            this.gen = generation;
            return this;
        }
        
        public MergeContext build() {
            return new MergeContext( authz, target, source, gen );
        }
    }
    
    private Authorization authorization;
    private DataView target;
    private DataView source;
    private int generation;
    
    public MergeContext(Authorization auth, DataView target, DataView source, int gen) {
        this.authorization = auth;
        this.target = target;
        this.source = source;
        this.generation = gen;
    }
    
    public Authorization getAuthorization() {
        return authorization;
    }

    public DataView getTarget() {
        return target;
    }
    
    public DataView getSource() {
        return source;
    }
    
    public int getGeneration() {
        return generation;
    }
    
    

}
