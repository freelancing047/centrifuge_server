package csi.server.ws.actions;

import java.awt.Color;

import prefuse.visual.VisualItem;
import csi.server.business.visualization.graph.renderers.EdgeRenderer;

public class ForecastLinkEdgeRenderer
    extends EdgeRenderer
{
    @Override
    protected Color getColor(VisualItem item) {
        return Color.GRAY.darker();
    }
}
