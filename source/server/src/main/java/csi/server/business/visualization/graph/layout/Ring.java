package csi.server.business.visualization.graph.layout;

import csi.server.business.visualization.graph.base.NodeStore;

public class Ring {

    private double xCenter;
    private double yCenter;
    private int ring;
    private NodeStore ringMaster;
    private int numMembers;
    private int dummy;

    public int getDummy() {
        return dummy;
    }

    public void setDummy(int dummy) {
        this.dummy = dummy;
    }

    public int getNumMembers() {
        return numMembers;
    }

    public void setNumMembers(int numMembers) {
        this.numMembers = numMembers;
    }

    public int getRing() {
        return ring;
    }

    public void setRing(int ring) {
        this.ring = ring;
    }

    public NodeStore getRingMaster() {
        return ringMaster;
    }

    public void setRingMaster(NodeStore ringMaster) {
        this.ringMaster = ringMaster;
    }

    public double getXCenter() {
        return xCenter;
    }

    public void setXCenter(double center) {
        xCenter = center;
    }

    public double getYCenter() {
        return yCenter;
    }

    public void setYCenter(double center) {
        yCenter = center;
    }
}
