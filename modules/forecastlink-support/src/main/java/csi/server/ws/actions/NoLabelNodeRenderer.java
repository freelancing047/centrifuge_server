package csi.server.ws.actions;

import prefuse.visual.VisualItem;
import csi.server.business.visualization.graph.renderers.NodeRenderer;

public class NoLabelNodeRenderer extends NodeRenderer
{

    @Override
    protected String getText(VisualItem item) {
        return null;
    }
    

}
