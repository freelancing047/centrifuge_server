package csi.client.gwt.viz.graph.plunk.util;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.graph.plunk.DuplicateNodeDialog;
import csi.client.gwt.viz.graph.surface.GraphSurface;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.graph.gwt.PlunkedItemsToDeleteDTO;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.service.api.GraphActionServiceProtocol;

/**
 * @author Centrifuge Systems, Inc.
 */
public class PlunkNodeUtils {

    public static void deleteItem(final GraphSurface graphSurface, final String itemKey, String objectType) {
        VortexFuture<PlunkedItemsToDeleteDTO> deleteFuture = WebMain.injector.getVortex().createFuture();
        try {
            deleteFuture.execute(GraphActionServiceProtocol.class).deletePlunkedItem(graphSurface.getVizUuid(), itemKey, objectType);
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
        deleteFuture.addEventHandler(new AbstractVortexEventHandler<PlunkedItemsToDeleteDTO>() {
            @Override
            public void onSuccess(PlunkedItemsToDeleteDTO result) {
                graphSurface.getGraph().getLegend().load();
                graphSurface.getToolTipManager().removeToolTip(itemKey);
                if(result != null) {
                    graphSurface.getGraph().getModel().getRelGraphViewDef().getPlunkedNodes().removeAll(result.getNodesToDelete());
                    graphSurface.getGraph().getModel().getRelGraphViewDef().getPlunkedLinks().removeAll(result.getLinksToDelete());
                }
            }
        });
        graphSurface.refresh(deleteFuture);
        graphSurface.getGraph().refreshTabs(deleteFuture);
    }

    public static VortexFuture<Boolean> checkForDuplicateNode(final GraphSurface graphSurface, final String name, final String type, final String nodeKey){
        VortexFuture<Boolean> future = WebMain.injector.getVortex().createFuture();
        future.execute(GraphActionServiceProtocol.class).isDuplicate(graphSurface.getVizUuid(), name, type);
        future.addEventHandler(new AbstractVortexEventHandler<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if(result){
                    new DuplicateNodeDialog(graphSurface, name, type, nodeKey).show();
                }
            }
        });
        return future;
    }
}
