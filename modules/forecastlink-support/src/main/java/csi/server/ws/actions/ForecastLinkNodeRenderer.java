package csi.server.ws.actions;

import csi.server.business.visualization.graph.renderers.CompositeRenderer;
import csi.server.business.visualization.graph.renderers.RestrictedLabelRenderer;

public class ForecastLinkNodeRenderer extends CompositeRenderer
{
    public ForecastLinkNodeRenderer() {
        super();
        
        iconShapeRenderer = new NoLabelNodeRenderer();
        labelRenderer = new RestrictedLabelRenderer( iconShapeRenderer );
    }
}
