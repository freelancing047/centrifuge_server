package csi.client.gwt.viz.graph.plunk;

import csi.client.gwt.WebMain;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.business.visualization.legend.GraphLegendInfo;
import csi.server.common.dto.graph.gwt.PlunkNodeDTO;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.PlunkedNode;
import csi.server.common.service.api.GraphActionServiceProtocol;

/**
 * Contains the location of the click so that we can place the node in the appropriate location.
 * @author Centrifuge Systems, Inc.
 */
public class PlunkNodeModel {

    private final String vizUuid;
    private final int x;
    private final int y;

    public PlunkNodeModel(String vizUuid, int x, int y) {
        this.vizUuid = vizUuid;
        this.x = x;
        this.y = y;
    }

    public VortexFuture<GraphLegendInfo> getLegendItems(){
        VortexFuture<GraphLegendInfo> future = WebMain.injector.getVortex().createFuture();
        try {
            future.execute(GraphActionServiceProtocol.class).legendData(vizUuid);
        } catch (CentrifugeException ignored) {
        }
        return future;
    }

    public VortexFuture<PlunkedNode> plunkNewNode(String name, String type, NodeDef nodeDef){
        VortexFuture<PlunkedNode> future = WebMain.injector.getVortex().createFuture();
        PlunkNodeDTO plunkNodeDTO = PlunkNodeDTO.create(vizUuid, name, type, x, y, nodeDef);
        try {
            future.execute(GraphActionServiceProtocol.class).plunkNewNode(plunkNodeDTO);
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
        return future;
    }

}
