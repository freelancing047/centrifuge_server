package csi.server.business.visualization.map.storage;

class OutOfBandResourcesNode {
    String key;
    OutOfBandResources value;
    OutOfBandResourcesNode pre;
    OutOfBandResourcesNode next;

    public OutOfBandResourcesNode(String key, OutOfBandResources value) {
        this.key = key;
        this.value = value;
    }
}
