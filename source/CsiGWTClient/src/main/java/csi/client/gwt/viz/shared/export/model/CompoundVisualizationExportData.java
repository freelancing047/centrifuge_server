package csi.client.gwt.viz.shared.export.model;

import csi.client.gwt.viz.Visualization;
import csi.server.common.model.visualization.VisualizationDef;
import csi.shared.core.imaging.ImagingRequest;

import java.util.List;

/**
 * Created by Ivan on 7/27/2017.
 */
public class CompoundVisualizationExportData implements VisualizationExportable {
    private final VisualizationDef visualizationDef;
    private final String dataViewUuid;
    private final Visualization visualization;
    private List<ImagingRequest> imagingRequests;
    private String exportName;

    public CompoundVisualizationExportData(Visualization visualization, String dataViewUuid, String dataviewName){
        this.visualization = visualization;
        this.visualizationDef = visualization.getVisualizationDef();
        this.dataViewUuid = dataViewUuid;

        this.exportName = dataviewName + "_" + visualizationDef.getName();
    }


    public List<ImagingRequest> getImagingRequests(){
        return this.imagingRequests;
    }

    public void setImagingRequests(List<ImagingRequest> req){
        this.imagingRequests = req;
    }

    @Override
    public String getName() {
        return exportName;
    }

    @Override
    public void setName(String name){
        this.exportName = name;
    }

    @Override
    public ExportableType getExportableType() {
        return ExportableType.VISUALIZATION;
    }

    @Override
    public String getDataViewUuid() {
        return dataViewUuid;
    }


    @Override
    public Visualization getVisualization() {
        return visualization;
    }

    @Override
    public VisualizationDef getData() {
        return visualizationDef;
    }
}
