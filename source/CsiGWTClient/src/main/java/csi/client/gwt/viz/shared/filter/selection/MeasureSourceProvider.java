package csi.client.gwt.viz.shared.filter.selection;

import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.chart.selection.MeasuresSource;
import csi.client.gwt.viz.matrix.MatrixPresenter;
import csi.client.gwt.viz.shared.AbstractVisualizationPresenter;
import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.chart.MeasureDefinition;
import csi.server.common.model.visualization.matrix.MatrixMeasureDefinition;
import csi.server.common.model.visualization.matrix.MatrixSettings;
import csi.server.common.model.visualization.matrix.MatrixViewDef;

import java.util.List;

public class MeasureSourceProvider {

    ModelObject _settings;

    public MeasureSourceProvider(ModelObject vizSettings) {
        this._settings = vizSettings;
    }

    public MeasureSourceProvider(VisualizationDef vizDef) {
        if (vizDef instanceof MatrixViewDef) {
            this._settings = ((MatrixViewDef) vizDef).getMatrixSettings();
        }else if( vizDef instanceof DrillChartViewDef){
            this._settings = ((DrillChartViewDef) vizDef).getChartSettings();
        }

    }


    /**
     *
     * @return supports Chart and Matrix, otherwise will return null
     */
    public MeasuresSource getMeasureSource(){
        if(_settings instanceof ChartSettings){

            return headers -> {
                ChartSettings settings = (ChartSettings) _settings;
                if(settings != null) {
                    if (settings.isUseCountStarForMeasure() || settings.getMeasureDefinitions().size() == 0) {
                        headers.add("Count (*)");
                    } else {
                        for (MeasureDefinition measureDefinition : settings.getMeasureDefinitions()) {
                            headers.add(measureDefinition.getComposedName());
                        }
                    }
                }
            };


        }else if(_settings instanceof MatrixSettings){
            return headers -> {
                MatrixSettings sets = (MatrixSettings)_settings;
                MatrixMeasureDefinition matrixMeasureDefinition = sets.getMatrixMeasureDefinition();
                if(sets.isUseCountForMeasure()){
                    headers.add("Count (*)");
                }else {
                    headers.add(matrixMeasureDefinition.getComposedName());
                }
            };
        }else{
            return null;
        }


    }


}
