package csi.server.common.model.visualization.timeline;

import java.io.Serializable;

import csi.shared.core.util.HasLabel;

public enum TimeType implements Serializable, HasLabel{

    DATE("Date"), DURATION("Duration"), Time("Time");
    
    private String label;

    private TimeType(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
