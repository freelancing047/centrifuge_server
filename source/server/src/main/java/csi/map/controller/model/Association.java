package csi.map.controller.model;

import java.util.ArrayList;
import java.util.List;

public class Association {
    private Integer typeId;
    private List<Long> sourceObjectIds = new ArrayList<>();
    private List<Long> destinationObjectIds = new ArrayList<>();

    public Association(Integer typeId) {
        this.typeId = typeId;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public List<Long> getSourceObjectIds() {
        return sourceObjectIds;
    }

    public void setSourceObjectIds(List<Long> sourceObjectIds) {
        this.sourceObjectIds = sourceObjectIds;
    }

    public List<Long> getDestinationObjectIds() {
        return destinationObjectIds;
    }

    public void setDestinationObjectIds(List<Long> destinationObjectIds) {
        this.destinationObjectIds = destinationObjectIds;
    }

    public void addSegment(Long sourceObjectId, Long destinationObjectId) {
        sourceObjectIds.add(sourceObjectId);
        destinationObjectIds.add(destinationObjectId);
    }
}
