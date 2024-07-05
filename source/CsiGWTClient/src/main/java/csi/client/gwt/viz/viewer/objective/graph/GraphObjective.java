package csi.client.gwt.viz.viewer.objective.graph;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.graph.Graph;
import csi.server.common.model.visualization.viewer.LinkObjective;
import csi.server.common.model.visualization.viewer.Objective;
import csi.server.common.model.visualization.viewer.NodeObjective;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.graph.gwt.FindItemDTO;


public class GraphObjective{

    public static VortexFuture<Objective> create(Graph graph, int x, int y) {
        final VortexFuture<Objective> objectiveVortexFuture = WebMain.injector.getVortex().createFuture();

        {//find Item Code
            VortexFuture<FindItemDTO> findItemAt = graph.getGraphSurface().getGraph().getModel().findItemAt(x, y, true);
            findItemAt.addEventHandler(new AbstractVortexEventHandler<FindItemDTO>() {

                @Override
                public void onSuccess(FindItemDTO result) {
                    Objective objective = null;
                    try{
                        if (result != null) {
//                                new ToolTip(surface, result, x, y, null, false);
                            if (result.getItemType().equals("node")) {//NON-NLS
                                objective =  new NodeObjective(result, graph.getDataviewUuid(), graph.getUuid(), graph.getName());
                            }
                            else if (result.getItemType().equals("link")) {//NON-NLS
                                objective =  new LinkObjective(result, graph.getDataviewUuid(), graph.getUuid());
                            }
                        }
                    } catch(Exception exception){

                    }
                    objectiveVortexFuture.fireSuccess(objective);
                }

                @Override
                public boolean onError(Throwable t) {
                    return false;
                }
            });
        }
        return objectiveVortexFuture;
    }
}
