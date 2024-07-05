package csi.map.controller.model;

public class Feature {
    private FeatureAttributes attributes = new FeatureAttributes();
    private XYPair geometry;

    public Feature(XYPair geometry, String objectID, Integer typeId) {
        this.geometry = geometry;
        attributes.setObjectID(objectID);
        attributes.setTypeId(typeId);
    }

    public FeatureAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(FeatureAttributes attributes) {
        this.attributes = attributes;
    }

    public XYPair getGeometry() {
        return geometry;
    }

    public void setGeometry(XYPair geometry) {
        this.geometry = geometry;
    }
}
