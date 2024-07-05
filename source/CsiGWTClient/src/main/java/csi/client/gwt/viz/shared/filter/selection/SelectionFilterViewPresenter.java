package csi.client.gwt.viz.shared.filter.selection;

import com.github.gwtbootstrap.client.ui.FluidRow;
import com.google.common.collect.Lists;
import com.sencha.gxt.widget.core.client.info.Info;
import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.chart.selection.ChartSelectionCard;
import csi.client.gwt.viz.chart.selection.MeasuresSource;
import csi.client.gwt.viz.chart.selection.ValidationListener;
import csi.client.gwt.viz.matrix.MatrixPresenter;
import csi.client.gwt.viz.shared.AbstractVisualizationPresenter;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.visualization.chart.*;
import csi.server.common.model.visualization.matrix.MatrixMeasureDefinition;
import csi.server.common.model.visualization.matrix.MatrixSettings;
import csi.server.common.model.visualization.selection.ChartSelectionState;
import csi.server.common.model.visualization.selection.DrillCategory;
import csi.server.common.service.api.ChartActionsServiceProtocol;
import csi.server.common.service.api.MatrixActionsServiceProtocol;
import csi.shared.core.visualization.matrix.MatrixWrapper;
import csi.shared.gwt.viz.chart.StatisticsHolder;

import java.util.*;

public class SelectionFilterViewPresenter implements ValidationListener  {

    AbstractVisualizationPresenter presenter;
    SelectionFilterView dialog;

    public SelectionFilterViewPresenter(SelectionFilterView view, AbstractVisualizationPresenter presenter) {
        this.presenter = presenter;
        dialog = view;
    }

    // this could be a generic to return how to get the measures;
    public MeasuresSource getMeasureSource(){

        MeasureSourceProvider provider = new MeasureSourceProvider(presenter.getVisualizationDef());
        return provider.getMeasureSource();

    }

    //TODO write spec for this stuff.

    public void requestSelection(List<ChartCriterion> criterions, boolean select, boolean replace){

        if(presenter instanceof MatrixPresenter) {
            MatrixPresenter p = (MatrixPresenter) presenter;
            if(replace){
                p.getModel().clearSelectedCells();
            }
            p.showLoading();
        }

        VortexFuture<MatrixWrapper> future = presenter.getVortex().createFuture();

        future.addEventHandler(new VortexEventHandler<MatrixWrapper>() {
            @Override
            public void onSuccess(MatrixWrapper result) {
                if(presenter instanceof MatrixPresenter){
                    MatrixPresenter p = (MatrixPresenter) presenter;
                    result.getData().getCells().forEach(cell -> {
                        if(select) {
                            p.getModel().selectCell(cell);
                        }else{
                            p.getModel().deselectCell(cell);
                        }
                    });

                    p.getView().setLoadingIndicator(false);
                    p.getView().refresh();
                }

            }

            @Override
            public boolean onError(Throwable t) {
                return false;
            }

            @Override
            public void onUpdate(int taskProgess, String taskMessage) {

            }

            @Override
            public void onCancel() {

            }
        });
        future.execute(MatrixActionsServiceProtocol.class).selectCells(criterions, presenter.getUuid(),
                presenter.getDataViewUuid());

    }

    @Override
    public void checkCriteriaValidity() {

    }
}
