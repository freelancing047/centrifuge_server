/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.client.gwt.viz.matrix;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.ProgressBar;
import com.github.gwtbootstrap.client.ui.base.ProgressBarBase;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Panel;
import com.sencha.gxt.core.client.dom.XDOM;

import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.matrix.menu.MatrixMenuManager;
import csi.client.gwt.viz.shared.AbstractVisualizationPresenter;
import csi.client.gwt.viz.shared.chrome.panel.RenderSize;
import csi.client.gwt.viz.shared.filter.FilterCapableVisualizationPresenter;
import csi.client.gwt.viz.shared.menu.AbstractMenuManager;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Vortex;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.visualization.matrix.MatrixViewDef;
import csi.server.common.model.visualization.selection.MatrixCellSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.service.api.MatrixActionsServiceProtocol;
import csi.shared.core.imaging.ImagingRequest;
import csi.shared.core.visualization.matrix.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class MatrixPresenter extends AbstractVisualizationPresenter<MatrixViewDef, MatrixView> implements
        FilterCapableVisualizationPresenter {

    private static final int MINIMUM_ALLOWABLE_CATEGORIES = 1;
    private MatrixModel model;
    private MatrixView view;
    private Alert progressIndicator;
    private ProgressBar progressBar;

    public UpdateTimer updater;

    private MatrixMetricsView metrics;

    private Timer loadBarTimeout = null;

    public MatrixPresenter(AbstractDataViewPresenter dvPresenterIn, MatrixViewDef visualizationDef) {
        super(dvPresenterIn, visualizationDef);
        model = new MatrixModel();
    }


    public MatrixMetricsView getMetrics() {
        return metrics;
    }

    public void showMetrics(){
        if(metrics == null){
            metrics = new MatrixMetricsView(this);
        }
        Scheduler.get().scheduleDeferred(() ->{
            metrics.show();
        });
    }


    public MatrixModel getModel() {
        return model;
    }

    @Override
    public boolean hasSelection() {
        return getModel().getSelectedCells().size() > 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Visualization> AbstractMenuManager<V> createMenuManager() {
        return (AbstractMenuManager<V>) new MatrixMenuManager(this);
    }

    public void hideProgressIndicator() {
        getView().setLoadingIndicator(false);
        getView().setLoaded(true);
        if (progressIndicator != null) {
            if (progressIndicator.isAttached()) {
                getChrome().getMainLP().remove(progressIndicator);
                progressBar.clear();
                progressBar = null;
                progressIndicator.close();
                progressIndicator = null;
            }
        }
    }

    public void showProgressIndicator() {
//        getView().setLoadingIndicator(true);
        hideProgressIndicator();
        getView().setLoaded(false);
        if (getChrome() != null) {
            createProgressIndicator(getChrome().getMainLP());
        }
    }

    public void createProgressIndicator(Panel panel) {
        if (progressIndicator == null) {
            progressIndicator = new Alert(CentrifugeConstantsLocator.get().progressBar_loading(), AlertType.INFO);
            panel.add(progressIndicator);
            progressBar = new ProgressBar(ProgressBarBase.Style.ANIMATED);
            progressBar.setPercent(100);
            progressIndicator.setClose(false);
            progressIndicator.add(progressBar);
            progressIndicator.setHeight("50px");//FIXME: set using stylename
            progressIndicator.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
        }
    }

    public void showLoading() {
        getView().setLoadingIndicator(true);
    }

    public void hideLoading() {
        getView().setLoadingIndicator(false);
    }

    public VortexFuture<MatrixWrapper> showInfoForRegion(int x, int y, int dx, int dy) {
        showLoading();

        MatrixDataRequest mdr = new MatrixDataRequest();
        mdr.setDvUuid(getDataViewUuid());
        mdr.setVizUuid(getVisualizationDef().getUuid());

        mdr.setStartX(x);
        mdr.setEndX(dx);
        mdr.setStartY(y);
        mdr.setEndY(dy);

        mdr.setSummarizationPolicy(MatrixDataRequest.REQUEST_SUMMARIZATION_POLICY.ALLOW_SUMMARY);

        VortexFuture<MatrixWrapper> vortexFuture = getVortex().createFuture();
        executeFuture(vortexFuture);
        vortexFuture.execute(MatrixActionsServiceProtocol.class).getRegion(mdr);
        return vortexFuture;

    }

    public void preserveSelection() {
        updateRowsSelection();
        saveOldSelection(getVisualizationDef().getSelection());
    }

    public void requestUpdate(MatrixDataRequest req) {
        showLoading();

//        Info.display("Selection save ", "Is : " + getVisualizationDef().getSelection().getSelectedItems().length );
        updateRowsSelection();
        saveOldSelection(getVisualizationDef().getSelection());

        req.setVizUuid(this.getUuid());
        req.setDvUuid(this.getDataViewUuid());

        debounceReq(req);
    }

    public void fullScreen(boolean refresh) {
        if(!getView().isLoaded()){
            return;
        }
        getView().hideMatrix();
        preserveSelection();

        getModel().setCurrentView(0, 0, getModel().getMetrics().getAxisXCount(), getModel().getMetrics().getAxisYCount());


        if(refresh) {
            getView().fetchData();
        }else{
            getView().showMatrix();
        }
    }

    public void selectCell(Cell cell, boolean shift) {
        Cell hoverCell = cell;
        if (model.isSummary()) {
            MatrixDataRequest dr = new MatrixDataRequest();
            dr.setVizUuid(getVisualizationDef().getUuid());
            dr.setDvUuid(getDataViewUuid());
            dr.setSummarizationPolicy(MatrixDataRequest.REQUEST_SUMMARIZATION_POLICY.DISALLOW_SUMMARY);

            int bucketX = model.getBinCountForAxis((int) model.getWidth());
            int bucketY = model.getBinCountForAxis((int) model.getHeight());

            int sX = hoverCell.getX() - bucketX / 2;
            int sY = hoverCell.getY() - bucketY / 2;

            int eX = sX + bucketX - 1;
            int eY = sY + bucketY - 1;

            dr.setExtent(sX, eX, sY, eY);
            Vortex vortex = getVortex();
            VortexFuture<MatrixWrapper> vortexFuture = vortex.createFuture();

            if (shift || model.isSelected(hoverCell)) {
                model.getSelectedCells().removeIf(c -> c.getX() >= sX && c.getX() <= eX && c.getY() >= sY && c.getY() <= eY);
                getView().refresh();
            } else {
                vortexFuture.addEventHandler(new AbstractVortexEventHandler<MatrixWrapper>() {
                    @Override
                    public void onSuccess(MatrixWrapper result) {
                        result.getData().getCells().forEach(cell -> {
                            getModel().selectCell(cell);
                        });
                        getView().setLoadingIndicator(false);
                        getView().refresh();
                    }
                });
                vortexFuture.execute(MatrixActionsServiceProtocol.class).getCellsInRegionForSelection(dr);
                getView().setLoadingIndicator(true);

            }

        } else {
            if (hoverCell != null) {
                if (model.isSelected(hoverCell)) {
                    model.deselectCell(hoverCell);
                } else {
                    model.selectCell(hoverCell);
                }
                getView().refresh();
            }
        }
    }

    public class UpdateTimer extends Timer {
        MatrixDataRequest dataReq;

        public UpdateTimer(MatrixDataRequest req) {
            this.dataReq = req;
        }

        public MatrixDataRequest getReq() {
            return dataReq;
        }

        @Override
        public void run() {
            VortexFuture<MatrixWrapper> vortexFuture = getVortex().createFuture();
            vortexFuture.execute(MatrixActionsServiceProtocol.class).getRegion(dataReq);
            executeFuture(vortexFuture);
            updater = null;
        }

        public void updateRequest(MatrixDataRequest req) {
            this.dataReq = req;
        }
    }

    public void debounceReq(MatrixDataRequest req) {
        if (updater == null) {
            updater = new UpdateTimer(req);
        } else {
            updater.cancel();
            if (!model.getMatrixDataResponse().getXYPair().isSameRegion(req)) {
                updater.updateRequest(req);
            }
        }

        updater.schedule(1000);
    }

    public void executeFuture(VortexFuture<MatrixWrapper> vortexFuture) {
        vortexFuture.addEventHandler(new AbstractVortexEventHandler<MatrixWrapper>() {
            @Override
            public void onSuccess(MatrixWrapper result) {
                MatrixMetrics m = result.getMetrics();
                MatrixCategoryResponse r = result.getCategories();
                MatrixDataResponse dr = result.getData();

                getModel().setSummary(dr.isSummary());
                clearAndShowMatrix();

                getModel().setMetrics(m);
                getModel().setVisualizationUuid(getVisualizationDef().getUuid());
                getModel().setCategoryResponseAndSettings(r, getVisualizationDef().getMatrixSettings());
                getModel().setMatrixDataResponse(dr);

                getView().displayCells(dr);

                MatrixMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(model.getVisualizationUuid()));
                getView().refresh();
                getView().onResize();


            }

            public boolean onError(Throwable t) {
                hideProgressIndicator();
                getView().showNoDataAvailable();
                displayLoadingError(t);
                return true;
            }

        });
    }

    @Override
    public void loadVisualization() {
        showProgressIndicator();

        this.appendNotificationText(NotificationLabel.FILTER, getVisualizationDef().getFilter() != null);
        VortexFuture<MatrixWrapper> vortexFuture = getVortex().createFuture();

        vortexFuture.execute(MatrixActionsServiceProtocol.class).getMatrix(getVisualizationDef(), getDataViewUuid());

        vortexFuture.addEventHandler(new AbstractVortexEventHandler<MatrixWrapper>() {
            @Override
            public void onSuccess(MatrixWrapper result) {
                loadFromMatrixWrapper(result);
            }

            @Override
            public boolean onError(Throwable t) {
                hideProgressIndicator();
                hideLoading();
                getView().showNoDataAvailable();
                displayLoadingError(t);
                return true;
            }
        });

        appendNotificationText(NotificationLabel.SELECTION, getVisualizationDef().getMatrixSettings().getFilterCriteria().size() > 0);
    }

    @Override
    public void reload() {
        getModel().getSelectedCells().clear();

        if(hasOldSelection()) {
            MatrixCellSelection selection = (MatrixCellSelection) popOldSelection();
            if (selection != null) {
                Set<Cell> newSelection = new HashSet<Cell>();
                for (MatrixCellSelection.CellPosition cellPosition : selection.getSelectedCells()) {
                    newSelection.add(new Cell(cellPosition.getX(), cellPosition.getY(), -1));
                }
                getModel().getSelectedCells().addAll(newSelection);
            }
        }
        super.reload();
    }


    protected void loadFromMatrixWrapper(MatrixWrapper result) {
        if (result.isEmpty() || result.getData().getCells().size() == 0) {
            hideLoading();
            hideProgressIndicator();
            getView().hideMatrix();
            getView().showNoDataAvailable();
            return;
        }

        MatrixMetrics m = result.getMetrics();
        MatrixCategoryResponse catResp = result.getCategories();
        MatrixDataResponse dr = result.getData();

        getModel().setSummary(dr.isSummary());

        if (m.getAxisXCount() < MINIMUM_ALLOWABLE_CATEGORIES || m.getAxisYCount() < MINIMUM_ALLOWABLE_CATEGORIES) {
            getView().clear();
            getView().showNoDataAvailable();
            hideLoading();
            hideProgressIndicator();
        } else {
            getModel().setMetrics(m);
            getModel().setVisualizationUuid(getVisualizationDef().getUuid());
            getModel().setCategoryResponseAndSettings(catResp, getVisualizationDef().getMatrixSettings());
            getModel().setMatrixDataResponse(dr);

            clearAndShowMatrix();

            drawCategories(catResp);

            getView().displayCells(dr);

            Scheduler.get().scheduleDeferred(() -> {
                stopDelayedProgressBar();
                getView().setLoaded(true);
                getView().displayColorScale();
                getView().refresh(); // i think this doesnt' need to be here.o
                fullScreen(false);
                MatrixMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(getModel().getVisualizationUuid()));
            });
        }
    }

    public void clearAndShowMatrix() {
        getView().clear();
        getView().showMatrix();
    }

    /**
     *  Will make sure that we have needed items in the categories, and then displays them.
     *
     * @param result CategoryRespObject from which we are drawing
     */
    public void drawCategories(MatrixCategoryResponse result) {
        getModel().setResult(result, getVisualizationDef().getMatrixSettings());
    }

    @Override
    public void applySelection(Selection selection) {
        // wtf
        saveOldSelection(selection);
        if (selection instanceof MatrixCellSelection) {
            getModel().updateVisualSelection((MatrixCellSelection) selection);
        }
        updateRowsSelection();
        if(model.isSummary()) {
            model.setupSummarySelection();
        }
        getView().refresh();
    }

    public void invalidateCache() {
        getVortex().execute(result -> {
        }, MatrixActionsServiceProtocol.class).invalidateCache(getUuid());
    }

    @Override
    public MatrixView createView() {
        view = new MatrixView(this);
        return view;
    }

    @Override
    public ImagingRequest getImagingRequest() {
        return getView().getImagingRequest();
    }


    @Override
    public void saveViewStateToVisualizationDef() {
        updateRowsSelection();
        saveOldSelection(getVisualizationDef().getSelection());
    }


    public void deselectAll() {
        getModel().clearSelectedCells();
        getVisualizationDef().getSelection().clearSelection();
        getView().refresh();
    }

    public void getFullMetrics(AbstractVortexEventHandler handler) {
        VortexFuture<MatrixMetrics> vortexFuture = getVortex().createFuture();
        vortexFuture.execute(MatrixActionsServiceProtocol.class).getFullMatrixMetrics(getVisualizationDef().getUuid());
        vortexFuture.addEventHandler(handler);

    }


    public void doCategorySelection(MatrixSelectionRequest req) {
        //FIXME:
/*        VortexFuture<IntCollection> vortexFuture = getVortex().createFuture();
        vortexFuture.execute(MatrixActionsServiceProtocol.class).getSelectionIdsFromCache(req);

        vortexFuture.addEventHandler(new AbstractVortexEventHandler<IntCollection>() {
            @Override
            public void onSuccess(IntCollection result) {
                if(result.size() > 0 ){
                    int[] ids = result.toIntArray();
                    getVisualizationDef().getSelection().addToSelection(ids);
                    getModel().updateVisualSelection(getVisualizationDef().getSelection());
                    getView().refresh();
                }
            }
        });*/
    }


    public void updateRowsSelection() {
        getVisualizationDef().getSelection().clearSelection();
        for (Cell cell : model.getSelectedCells()) {
            getVisualizationDef().getSelection().select(cell.getX(), cell.getY());
        }
    }

    public static int[] merge(int[] ids, int[] ids2, int position) {
        System.arraycopy(ids2, 0, ids, position, ids2.length);
        return ids;
    }

    @Override
    protected void onRenderSizeChange(RenderSize renderSize) {
        getView().onRenderSizeChange(renderSize);
    }

    @Override
    public void broadcastNotify(String text) {
        getView().broadcastNotify(text);
        appendBroadcastIcon();
    }

    private void cancelTimer() {
        if (loadBarTimeout != null) {
            loadBarTimeout.cancel();
            loadBarTimeout = null;
        }
    }

    public void stopDelayedProgressBar() {
        cancelTimer();
        hideProgressIndicator();

        hideLoading();
    }


}
