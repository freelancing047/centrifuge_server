/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.client.gwt.dataview.broadcast;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.DataViewPresenter;
import csi.client.gwt.dataview.DataViewWidget;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.matrix.MatrixPresenter;
import csi.client.gwt.viz.shared.chrome.VizChrome;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.vortex.impl.VortextFutureImpl;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.client.gwt.worksheet.WorksheetPresenter;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.broadcast.BroadcastRequest;
import csi.server.common.model.broadcast.BroadcastRequestType;
import csi.server.common.model.broadcast.BroadcastSet;
import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.service.api.BroadcastServiceProtocol;

import java.util.List;

/**
 * Makes the appropriate request to the broadcast service.
 * @author Centrifuge Systems, Inc.
 */
public class BroadcastManager {

    private final AbstractDataViewPresenter dataViewPresenter;

    private CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private ErrorDialog dialog = new ErrorDialog(i18n.broadcastErrorMessage());
//    private HandlerRegistration worksheetSelectionHandler;

    private String broadcastFilterCleared = i18n.broadcastAlert_broadcastCleared();
    private String broadcastFilterReceived = i18n.broadcastAlert_broadcastReceived();
    private String broadcastFilterIncoming = i18n.broadcastAlert_broadcastIncoming();
    Integer numberOfSelections = null;
    private List<MapPresenter> mapsWithLegendsToUnhide = Lists.newArrayList();

    public BroadcastManager(AbstractDataViewPresenter abstractDataViewPresenter) {
        this.dataViewPresenter = abstractDataViewPresenter;
    }

    public void broadcastFilter(BroadcastRequestType filterInclude, Visualization broadcastingVisualization) {
        AbstractVortexEventHandler<Void> callback = new AbstractVortexEventHandler<Void>() {

            @Override
            public void onSuccess(Void result) {
                for (final Visualization listeningVisualization : dataViewPresenter.getVisualizations()) {
                    try {
                        String vizUuid = listeningVisualization.getUuid();
                        String broadcastVizUuid = broadcastingVisualization.getUuid();
                        boolean isSelf = vizUuid.equals(broadcastVizUuid);
                        //&& listeningVisualization.isViewLoaded()
                        if (listeningVisualization.isBroadcastListener() && !isSelf) {
                            broadcastFilter(filterInclude, broadcastingVisualization, listeningVisualization);
                            Scheduler.get().scheduleFixedDelay(() -> {
                                listeningVisualization.broadcastNotify(broadcastFilterReceived);
                                return false;
                            }, 500);
                        }
                    } catch (Exception exception) {
                        //No-op
                        //Placing this here to avoid one viz breaking and breaking all broadcasts
                    }
                }

                //unpinBroadcast(broadcastingVisualization);
            }
        };

        pinBroadcast(broadcastingVisualization, callback);
    }

    private void unpinBroadcast(Visualization broadcastingVisualization) {
        BroadcastRequest request = createBroadcastRequest(BroadcastRequestType.PIN, broadcastingVisualization, null);

        VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();

        future.execute(BroadcastServiceProtocol.class).unpinBroadcast(request);
    }

    private void pinBroadcast(Visualization broadcastingVisualization, AbstractVortexEventHandler<Void> callback) {
        BroadcastRequest request = createBroadcastRequest(BroadcastRequestType.PIN, broadcastingVisualization, null);
        //request.setBroadcasterSelection(broadcastingVisualization.getVisualizationDef().getSelection());

        //This is to help with timeline hidden tracks, need info on the vizDef
        request.setBroadcastingViz(broadcastingVisualization.getVisualizationDef());

        VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(callback);

        showIncomingBroadcasts(broadcastingVisualization);
        future.execute(BroadcastServiceProtocol.class).pinBroadcast(request);
    }

    private void showIncomingBroadcasts(Visualization broadcastingVisualization) {
        for (final Visualization listeningVisualization : dataViewPresenter.getVisualizations()) {
            try {
                String vizUuid = listeningVisualization.getUuid();
                String broadcastVizUuid = broadcastingVisualization.getUuid();
                boolean isSelf = vizUuid.equals(broadcastVizUuid);
                //&& listeningVisualization.isViewLoaded()
                if (listeningVisualization.isViewLoaded() && listeningVisualization.isBroadcastListener() && !isSelf) {
                    if (listeningVisualization.getChrome() != null) {
                        listeningVisualization.broadcastNotify(broadcastFilterIncoming);
                    }
                }
            } catch (Exception exception) {
                //No-op
                //Placing this here to avoid one viz breaking and breaking all broadcasts
            }
        }
    }

    public void broadcastFilter(BroadcastSet set, Visualization broadcastingVisualization, Visualization listeningVisualization) {

        listeningVisualization.saveViewStateToVisualizationDef();
        BroadcastRequest request = createBroadcastRequest(BroadcastRequestType.FILTER_SET, broadcastingVisualization, listeningVisualization);
        request.setDataViewUuid(dataViewPresenter.getUuid());
        request.setBroadcasterVizUuid(broadcastingVisualization.getUuid());
        request.setBroadcastSet(set);
        //request.setBroadcasterSelection(broadcastingVisualization.getVisualizationDef().getSelection());
        //This is to help with timeline hidden tracks, need info on the vizDef
        if (broadcastingVisualization != null) {
            request.setBroadcastingViz(broadcastingVisualization.getVisualizationDef());
        }
        if (listeningVisualization.getChrome() != null) {
            listeningVisualization.broadcastNotify(broadcastFilterIncoming);
        }

        VortexFuture<Selection> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<Selection>() {
            @Override
            public void onSuccess(Selection selection) {
                if (selection == null) {
                    //Special Case: Broadcast was aborted due to result would be empty set.
                    //Show dialog. do not change visualization
                    new ErrorDialog(i18n.broadcastAborted(), i18n.broadcastAbortedMessage()).show();
                    return;
                }
                try {
                    if (!(selection instanceof NullSelection)) { //|| listeningVisualization.getVisualizationDef() instanceof RelGraphViewDef) {
                        listeningVisualization.saveOldSelection(selection);
                    } else {
                        if (listeningVisualization instanceof MatrixPresenter) {
                            MatrixPresenter p = (MatrixPresenter) listeningVisualization;
                            p.popOldSelection();
                        }
                    }
                    if (listeningVisualization.isViewLoaded()) {
                        listeningVisualization.reload();
                        Scheduler.get().scheduleFixedDelay(() -> {
                            listeningVisualization.broadcastNotify(broadcastFilterReceived);
                            return false;
                        }, 500);
                    }
                } catch (Exception exception) {
                    handleBroadcastException(exception, listeningVisualization.getName());
                }
            }

            @Override
            public boolean onError(Throwable t) {
                if (t instanceof CentrifugeException) {

                } else {
                    displayLoadingError(t);
                }
                return false;
            }
        });

        future.execute(BroadcastServiceProtocol.class).broadcastFilter(request);
    }

    private void broadcastFilter(BroadcastRequestType filterInclude, Visualization broadcastingVisualization, Visualization listeningVisualization) {

        listeningVisualization.saveViewStateToVisualizationDef();
        BroadcastRequest request = createBroadcastRequest(filterInclude, broadcastingVisualization, listeningVisualization);

        if (listeningVisualization != null) {
            request.setListeningVizUuid(listeningVisualization.getUuid());
            request.setListeningVizSelection(listeningVisualization.getVisualizationDef().getSelection());
        }

        VortexFuture<Selection> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<Selection>() {
            @Override
            public void onSuccess(Selection selection) {
                if (selection == null) {
                    Dialog m = new Dialog();
                    m.setCloseVisible(false);
                    m.setTitle(i18n.broadcastAborted());
                    InlineLabel text = new InlineLabel(i18n.broadcastAbortedMessage());

                    FluidContainer container = new FluidContainer();
                    FluidRow w = new FluidRow();
                    w.add(text);
                    container.add(w);

                    {
                        m.getCancelButton().setVisible(false);
                        m.getActionButton().setText("OK");
                        m.hideOnAction();
                    }


                    m.add(container);
                    m.show();
                    return;
                }
                try {
                    if (!(selection instanceof NullSelection)) {
                        listeningVisualization.saveOldSelection(selection);
                    } else {
                        if (listeningVisualization instanceof MatrixPresenter) {
                            MatrixPresenter p = (MatrixPresenter) listeningVisualization;
                            p.popOldSelection();
                        }
                    }
                    if (listeningVisualization.isViewLoaded()) {
                        listeningVisualization.reload();
                        listeningVisualization.broadcastNotify(broadcastFilterReceived);
                    }
                } catch (Exception exception) {
                    handleBroadcastException(exception, listeningVisualization.getName());
                }
            }

            @Override
            public boolean onError(Throwable t) {
                if (t instanceof CentrifugeException) {

                } else
                    displayLoadingError(t);
                return false;
            }
        });

        future.execute(BroadcastServiceProtocol.class).broadcastFilter(request);

    }

    private void displayLoadingError(Throwable e) {

        if (dialog == null) {
            dialog = new ErrorDialog(i18n.broadcastErrorTitle(), e.getMessage());
        }
        dialog.setTitle(i18n.broadcastErrorTitle());
        dialog.show();
    }

    public void broadcastSelection(BroadcastRequestType filterInclude, Visualization broadcastingVisualization) {

        AbstractVortexEventHandler<Void> callback = new AbstractVortexEventHandler<Void>() {

            @Override
            public void onSuccess(Void result) {
                for (final Visualization visualization : dataViewPresenter.getVisualizations()) {
                    try {
                        String vizUuid = visualization.getUuid();
                        String broadcastVizUuid = broadcastingVisualization.getUuid();
                        boolean isSelf = vizUuid.equals(broadcastVizUuid);
                        if (visualization.isBroadcastListener() && !isSelf && visualization.isViewLoaded()) {
                            broadcastSelection(filterInclude, broadcastingVisualization, visualization);
                            visualization.broadcastNotify(broadcastFilterReceived);
                        }
                    } catch (Exception e) {
                        handleBroadcastException(e, visualization.getName());
                    }
                }

                //unpinBroadcast(broadcastingVisualization);
            }
        };

        pinBroadcast(broadcastingVisualization, callback);
    }

    public void broadcastSelection(BroadcastSet set, Visualization broadcastingVisualization, Visualization visualization) {
        visualization.saveViewStateToVisualizationDef();
        BroadcastRequest request = new BroadcastRequest();
        request.setDataViewUuid(dataViewPresenter.getUuid());
        request.setBroadcasterVizUuid(broadcastingVisualization.getUuid());
        request.setBroadcastSet(set);
        request.setBroadcastRequestType(BroadcastRequestType.SELECT_SET);
        //request.setBroadcasterSelection(broadcastingVisualization.getVisualizationDef().getSelection());
        //This is to help with timeline hidden tracks, need info on the vizDef
        if (broadcastingVisualization != null) {
            request.setBroadcastingViz(broadcastingVisualization.getVisualizationDef());
        }
        if (visualization != null) {
            request.setListeningVizUuid(visualization.getUuid());
            request.setListeningVizSelection(visualization.getVisualizationDef().getSelection());
            if (visualization.isViewLoaded()) {
                visualization.broadcastNotify(broadcastFilterIncoming);
            }
        }

        VortexFuture<Selection> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<Selection>() {
            @Override
            public void onSuccess(Selection selection) {
                try {
                    visualization.applySelection(selection);
                    visualization.broadcastNotify(broadcastFilterReceived);
                } catch (Exception e) {
                    handleBroadcastException(e, visualization.getName());
                }
            }

            @Override
            public boolean onError(Throwable t) {
                displayLoadingError(t);
                return false;
            }
        });

        future.execute(BroadcastServiceProtocol.class).broadcastSelection(request);
    }

    private void broadcastSelection(BroadcastRequestType filterInclude, Visualization broadcastingVisualization, Visualization visualization) {
        visualization.saveViewStateToVisualizationDef();
        BroadcastRequest request = createBroadcastRequest(filterInclude, broadcastingVisualization, visualization);

        VortexFuture<Selection> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<Selection>() {
            @Override
            public void onSuccess(Selection selection) {
                try {
                    visualization.applySelection(selection);
                    visualization.broadcastNotify(broadcastFilterReceived);
                } catch (Exception e) {
                    handleBroadcastException(e, visualization.getName());
                }
            }

            @Override
            public boolean onError(Throwable t) {
                displayLoadingError(t);
                return false;
            }
        });

        future.execute(BroadcastServiceProtocol.class).broadcastSelection(request);
    }

    public void clearFilter(final Visualization visualization) {
        //    	if (!visualization.isViewLoaded()) return;

        visualization.saveViewStateToVisualizationDef();

        BroadcastRequest request = createBroadcastRequest(BroadcastRequestType.CLEAR, visualization, null);
        request.setBroadcasterSelection(visualization.getVisualizationDef().getSelection());

        WebMain.injector.getVortex().execute((Callback<Selection>) selection -> {
            visualization.clearBroadcastNotification();
            if (visualization instanceof MapPresenter) {
                visualization.reload();
                Scheduler.get().scheduleFixedDelay(() -> {
                    visualization.broadcastNotify(broadcastFilterCleared);
                    return false;
                }, 500);
            } else {
                if (selection == null) {
                    return;
                }

                // if(visualization.isViewLoaded()){
                if (visualization instanceof Graph) {
                    VortexFuture<Void> future = ((Graph) visualization).getModel().setShadowSelection();
                    future.addEventHandler(new AbstractVortexEventHandler<Void>() {

                        @Override
                        public void onSuccess(Void result) {

                            visualization.reload();
                            visualization.broadcastNotify(broadcastFilterCleared);
                        }
                    });
                } else {
                    if (!(selection instanceof NullSelection)) {// || visualization.getVisualizationDef() instanceof RelGraphViewDef) {
                        visualization.saveOldSelection(selection);
                    } else {
                        if (visualization instanceof MatrixPresenter) {
                            MatrixPresenter p = (MatrixPresenter) visualization;
                            p.popOldSelection();
                        }
                    }
                    if (visualization.isViewLoaded()) {
                        visualization.reload();
                        visualization.broadcastNotify(broadcastFilterCleared);
                    }
                }
                // }
            }
        }, BroadcastServiceProtocol.class).clearBroadcast(request);
    }

    public static class Counter{
        int x = 0;

        public void increment(){
            x++;
        }
        public void decrement(){
            x--;
        }
        public int get() {
            return x;
        }
    }

    public void clearEverything(Visualization broadcastingViz) {
        //We want to clear selections first as there are processes associated with clearing broadcasts
        // that we don't want to run before clearing selections
        VortexFuture<Void> voidVortexFuture = clearAllSelection(broadcastingViz);
        voidVortexFuture.addEventHandler(new AbstractVortexEventHandler<Void>() {
            @Override
            public void onSuccess(Void result) {
                clearAllFilters();
            }
        });
    }

    public void clearAllFilters() {
        for (final Visualization visualization : dataViewPresenter.getVisualizations()) {
            if (visualization.isBroadcastListener()) {
                clearFilter(visualization);
                if (visualization.getChrome() != null) {
                    visualization.broadcastNotify(broadcastFilterCleared);
                }
            }
        }
    }

    private BroadcastRequest createBroadcastRequest(BroadcastRequestType broadcastRequest, Visualization broadcastingVisualization, Visualization listeningVisualization) {
        BroadcastRequest request = new BroadcastRequest();
        request.setDataViewUuid(dataViewPresenter.getUuid());
        request.setBroadcasterVizUuid(broadcastingVisualization.getUuid());
        request.setBroadcastRequestType(broadcastRequest);

        if (listeningVisualization != null) {
            request.setListeningVizUuid(listeningVisualization.getUuid());
            request.setListeningVizSelection(listeningVisualization.getVisualizationDef().getSelection());
        }
        return request;
    }

    public VortexFuture<Void> clearAllSelection(Visualization broadcastingViz) {
        VortexFuture<Void> out = new VortextFutureImpl<Void>();
        Counter counter = new Counter();
        for (final Visualization visualization : dataViewPresenter.getVisualizations()) {
            try {
                if (visualization.isBroadcastListener() && visualization.isViewLoaded()) {
                    visualization.saveViewStateToVisualizationDef();
                    BroadcastRequest request = createBroadcastRequest(BroadcastRequestType.SELECTION_REPLACE, broadcastingViz, visualization);
                    VortexFuture<Selection> future = WebMain.injector.getVortex().createFuture();
                    future.addEventHandler(new AbstractVortexEventHandler<Selection>() {
                        @Override
                        public void onSuccess(Selection selection) {
                            try {
                                visualization.getVisualizationDef().getSelection().clearSelection();
                                visualization.getVisualizationDef().getSelection().setFromSelection(NullSelection.instance);
                                visualization.applySelection(visualization.getVisualizationDef().getSelection());
                                visualization.broadcastNotify(broadcastFilterCleared);
                            } catch (Exception e) {
                                handleBroadcastException(e, visualization.getName());
                            }
                            counter.decrement();
                            if (counter.get() ==0) {
                                out.fireSuccess(null);
                            }
                        }
                        @Override
                        public boolean onError(Throwable t) {
                            displayLoadingError(t);
                            return false;
                        }
                    });
                    future.execute(BroadcastServiceProtocol.class).clearSelection(request);
                    counter.increment();
                }
            } catch (Exception e) {
                handleBroadcastException(e, visualization.getName());
            }
        }
        if (counter.get() == 0) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    out.fireSuccess(null);
                }
            });
        }
        return out;
    }

    public void clearSelection(final Visualization visualization) {
        if (visualization.isViewLoaded()) {
            visualization.saveViewStateToVisualizationDef();
            BroadcastRequest request = createBroadcastRequest(BroadcastRequestType.SELECTION_REPLACE, visualization, visualization);

            WebMain.injector.getVortex().execute((Callback<Void>) result -> {
                visualization.getVisualizationDef().getSelection().clearSelection();
                visualization.getVisualizationDef().getSelection().setFromSelection(NullSelection.instance);
                visualization.applySelection(visualization.getVisualizationDef().getSelection());
            }, BroadcastServiceProtocol.class).clearSelection(request);
        }
    }

    private void handleBroadcastException(Exception exception, String viz) {
        //        Native.log(exception.getLocalizedMessage());
        ErrorDialog dialog = new ErrorDialog(exception.getLocalizedMessage(), i18n.broadcastExecutionErrorMessage() + viz);
        dialog.show();
    }

    public void invalidateDataviewBroadcast(String dataViewUuid) {

        WebMain.injector.getVortex().execute((Callback<Void>) result -> {
        }, BroadcastServiceProtocol.class).invalidateDataviewBroadcast(dataViewUuid);
    }

    /**
     *
     * @param viz
     * @param callback you will get either true or false on return of a check, do whatever you want with it.
     */
    public void isBroadcast(Visualization viz, Callback<Boolean> callback) {
        WebMain.injector.getVortex().execute(callback, BroadcastServiceProtocol.class).isBroadcast(viz.getUuid());
    }

    public void sendTo(Visualization senderViz) {
        mapsWithLegendsToUnhide.clear();
        for (final Visualization visualization : dataViewPresenter.getVisualizations()) {
            VizChrome chrome = visualization.getChrome();
            if (chrome != null) {
                chrome.sendTo(this, senderViz);
                if (visualization instanceof MapPresenter) {
                    MapPresenter mapPresenter = (MapPresenter) visualization;
                    if (mapPresenter.getMapLegend().isVisible()) {
                        mapsWithLegendsToUnhide.add(mapPresenter);
                        mapPresenter.getMapLegend().getLegendAsWindow().getElement().getStyle().setDisplay(Style.Display.NONE);
                    }
                }
            }
        }

        if (dataViewPresenter instanceof DataViewPresenter) {
            DataViewWidget dataViewWidget = (DataViewWidget) dataViewPresenter.getView();
            for (WorksheetPresenter worksheetPresenter : ((DataViewPresenter) dataViewPresenter).getWorksheetPresenters()) {
                for (Visualization visualization : worksheetPresenter.getVisualizations()) {
                    if (visualization.getChrome() == null) {
                        dataViewWidget.disableWorksheet(worksheetPresenter);
                    }
                }
            }
        }

    }

    public void endSendTo() {
        for (final Visualization visualization : dataViewPresenter.getVisualizations()) {
            try {
                visualization.getChrome().removeSendTo();
                if (visualization instanceof MapPresenter) {
                    MapPresenter mapPresenter = (MapPresenter) visualization;
                    if (mapsWithLegendsToUnhide.contains(mapPresenter)) {
                        mapPresenter.getMapLegend().getLegendAsWindow().getElement().getStyle().setDisplay(Style.Display.BLOCK);
                    }
                }
            } catch (Exception ignored) {
                //blind catch
            }

        }
        if (dataViewPresenter instanceof DataViewPresenter) {
            DataViewWidget dataViewWidget = (DataViewWidget) dataViewPresenter.getView();
            for (WorksheetPresenter worksheetPresenter : ((DataViewPresenter) dataViewPresenter).getWorksheetPresenters()) {
                dataViewWidget.enableWorksheet(worksheetPresenter);
            }
        }

        mapsWithLegendsToUnhide.clear();
    }

}
