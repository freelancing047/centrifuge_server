package csi.client.gwt.viz.graph.window.transparency;

import csi.client.gwt.viz.graph.Graph;
import csi.server.common.model.visualization.graph.RelGraphViewDef;

public class TransparencySettingsProxy {

    private static final int MIN_TRANSPARENCY_VALUE = 0;
    private static final int MAX_TRANSPARENCY_VALUE = 255;
    private TransparencySettings settings;

    public int getNodeTransparency() {
        RelGraphViewDef relGraphViewDef = getRelGraphViewDef();
        if (relGraphViewDef == null) {
            return 0;
        }
        return relGraphViewDef.getNodeTransparency();
    }

    public void setNodeTransparency(int nodeTransparency) {
        int newTransparency = nodeTransparency;
        newTransparency = validateTransparencyValue(newTransparency);
        getRelGraphViewDef().setNodeTransparency(newTransparency);
    }

    private static int validateTransparencyValue(int value) {
        int newTransparency = value;
        if (newTransparency > MAX_TRANSPARENCY_VALUE) {
            newTransparency = MAX_TRANSPARENCY_VALUE;
        } else if (newTransparency < MIN_TRANSPARENCY_VALUE) {
            newTransparency = MIN_TRANSPARENCY_VALUE;
        }
        return newTransparency;
    }

    public int getLinkTransparency() {
        return getRelGraphViewDef().getLinkTransparency();
    }

    public void setLinkTransparency(int linkTransparency) {
        int newTransparency = linkTransparency;
        newTransparency = validateTransparencyValue(newTransparency);
        getRelGraphViewDef().setLinkTransparency(newTransparency);
    }

    public int getLabelTransparency() {
        return getRelGraphViewDef().getLabelTransparency();
    }

    public void setLabelTransparency(int labelTransparency) {
        int newTransparency = labelTransparency;
        newTransparency = validateTransparencyValue(newTransparency);
        getRelGraphViewDef().setLabelTransparency(newTransparency);
    }
    public int getMinimumNodeScaleFactor() {
        return (int) (2*(50-getRelGraphViewDef().getMinimumNodeScaleFactor()));
    }

    public void setMinimumNodeScaleFactor(int minimumNodeScaleFactor) {
        double newTransparency = 50-minimumNodeScaleFactor/2D;
        newTransparency = Math.max(1,Math.min(50,newTransparency));
        getRelGraphViewDef().setMinimumNodeScaleFactor(newTransparency);
    }

    public TransparencySettingsProxy(TransparencySettings transparencySettings) {
        this.settings = transparencySettings;
    }

    protected RelGraphViewDef getRelGraphViewDef() {
        if (settings == null) {
            throw new NullPointerException("There is no settings.");
        }
        Graph graph = settings.getGraph();
        if (graph == null) {
            throw new NullPointerException("There is no graph.");
        }
        try {
            return graph.getModel().getRelGraphViewDef();
        } catch (Exception myException) {
            throw new NullPointerException("Could not find something.");
        }
    }
}
