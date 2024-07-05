package csi.map.controller.model;

public class FeatureAttributes {
    private String objectID;
    private Integer typeId;
    private int size = 1;
    private double hits = 0.0;
    private String label = "";

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double getHits() {
        return hits;
    }

    public void setHits(double hits) {
        this.hits = hits;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
