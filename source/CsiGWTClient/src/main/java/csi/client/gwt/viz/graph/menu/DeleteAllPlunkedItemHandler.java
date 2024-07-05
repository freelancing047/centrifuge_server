package csi.client.gwt.viz.graph.menu;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.WarningDialog;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.service.api.GraphActionServiceProtocol;

/**
 * @author Centrifuge Systems, Inc.
 */
public class DeleteAllPlunkedItemHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public DeleteAllPlunkedItemHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        if(plunkedItemsDoNotExist()) {
            return;
        }

        WarningDialog dialog = new WarningDialog(i18n.plunking_DeleteAll_Dialog_Title(), i18n.plunking_DeleteAll_Dialog_Message());
        dialog.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                deleteAllPlunkedItems();
            }
        });
        dialog.show();
    }

    private void deleteAllPlunkedItems() {
        VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
        try {
            future.execute(GraphActionServiceProtocol.class).deleteAllPlunkedItems(getPresenter().getUuid());
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
        future.addEventHandler(new AbstractVortexEventHandler<Void>() {
            @Override
            public void onSuccess(Void result) {
                getPresenter().getLegend().load();
                getPresenter().getGraphSurface().getToolTipManager().removeAllToolTips();
                getPresenter().getModel().getRelGraphViewDef().getPlunkedNodes().clear();
                getPresenter().getModel().getRelGraphViewDef().getPlunkedLinks().clear();
            }
        });
        getPresenter().getGraphSurface().refresh(future);
        getPresenter().refreshTabs(future);
    }

    private boolean plunkedItemsDoNotExist() {
        RelGraphViewDef relGraphViewDef = getPresenter().getModel().getRelGraphViewDef();
        return relGraphViewDef.getPlunkedLinks().isEmpty() && relGraphViewDef.getPlunkedNodes().isEmpty();
    }

}



