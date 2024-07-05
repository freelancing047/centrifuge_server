package csi.server.business.visualization.graph;

import csi.server.task.api.InvokeListener;

public class ClearGraphContextListener
    implements InvokeListener
{

    @Override
    public void onEvent() {
        GraphContext.Current.remove();
    }

}
