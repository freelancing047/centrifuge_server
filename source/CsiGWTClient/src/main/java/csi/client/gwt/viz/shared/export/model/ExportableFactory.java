package csi.client.gwt.viz.shared.export.model;

import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.map.presenter.MapLegendContainer;
import csi.client.gwt.viz.matrix.MatrixPresenter;
import csi.client.gwt.viz.shared.AbstractVisualizationPresenter;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationType;

/**
 * Creates the appropriate Exportable instance.
 * @author Centrifuge Systems, Inc.
 */
public class ExportableFactory {

    private ExportableFactory(){}

    public static <V extends Visualization> VisualizationExportable createVisualizationExportable(V presenter) {

        AbstractVisualizationPresenter tmp = (AbstractVisualizationPresenter) presenter;
        VisualizationType type = presenter.getVisualizationDef().getType();
        if (type == VisualizationType.CHRONOS) {
            TimelinePresenter tp = (TimelinePresenter) presenter;
            if (tp.isLegendVisible()) {
                return createCompoundVisualizationExportable(presenter);
            }
        }
        VisualizationExportData exportData = new VisualizationExportData(presenter, presenter.getDataViewUuid(), tmp.getDataView().getName());
        exportData.setImagingRequest(presenter.getImagingRequest());
        return exportData;
    }



    public static <V extends Visualization> VisualizationExportable createCompoundVisualizationExportable(V presenter) {
        if(presenter instanceof TimelinePresenter){
            TimelinePresenter tp = (TimelinePresenter) presenter;
            if(tp.isLegendVisible()) {
                return getCompoundTimelineExportable(tp);
            }
        }else if(presenter instanceof MapLegendContainer){
            //get the map one.
        }

        return createVisualizationExportable(presenter);
    }

    private static CompoundVisualizationExportData getCompoundTimelineExportable(TimelinePresenter presenter){
        CompoundVisualizationExportData exportData = new CompoundVisualizationExportData(presenter, presenter.getDataViewUuid(), presenter.getDataView().getName());
        exportData.setImagingRequests(presenter.getBundledImagingRequest());
        return exportData;
    }


    public static Exportable createDataViewExportable(DataView dataView) {
        return new DataViewExportData(dataView);
    }

    public static Exportable createDataViewExportable(String dvUuid, String dvName) {
        return new DataViewExportData(dvUuid, dvName);
    }

}
