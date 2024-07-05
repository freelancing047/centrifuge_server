package csi.client.gwt.viz.shared.export.model;

import csi.client.gwt.viz.Visualization;
import csi.server.common.model.visualization.VisualizationDef;
import csi.shared.core.imaging.ImagingRequest;

/**
 * Data necessary for exporting a Visualization.
 * @author Centrifuge Systems, Inc.
 */
public class VisualizationExportData implements VisualizationExportable {

    private final VisualizationDef visualizationDef;
    private final String dataViewUuid;
    private final Visualization visualization;
    private ImagingRequest imagingRequest;
    private String exportName;
    private String dataviewName;

    public VisualizationExportData(Visualization visualization, String dataViewUuid, String dataviewName){
        this.visualization = visualization;
        this.visualizationDef = visualization.getVisualizationDef();
        this.dataViewUuid = dataViewUuid;

        this.dataviewName = dataviewName;
        this.exportName = dataviewName + "_" + visualizationDef.getName();
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
    public VisualizationDef getData() {
        return visualizationDef;
    }

    public ImagingRequest getImagingRequest() {
        return imagingRequest;
    }

    public void setImagingRequest(ImagingRequest imagingRequest) {
        this.imagingRequest = imagingRequest;
    }

    @Override
    public Visualization getVisualization() {
        return visualization;
    }
}
