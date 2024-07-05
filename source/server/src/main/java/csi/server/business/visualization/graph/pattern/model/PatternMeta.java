package csi.server.business.visualization.graph.pattern.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.dto.graph.pattern.PatternResult;
import csi.server.common.model.CsiUUID;

/**
 * @author Centrifuge Systems, Inc.
 */
public class PatternMeta implements IsSerializable {
    private PatternResult pattern;
    private CsiUUID uuid = new CsiUUID();

    public PatternResult getPattern() {
        return pattern;
    }

    public void setPattern(PatternResult pattern) {
        this.pattern = pattern;
    }

    public CsiUUID getUuid() {
        return uuid;
    }

    public void setUuid(CsiUUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object object){
        if(object instanceof PatternMeta){
            return uuid.equals(((PatternMeta) object).getUuid());
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
    
    
}
