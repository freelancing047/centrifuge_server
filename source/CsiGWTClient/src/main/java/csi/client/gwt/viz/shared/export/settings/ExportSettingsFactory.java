package csi.client.gwt.viz.shared.export.settings;

import csi.client.gwt.viz.shared.export.model.ExportType;
import csi.client.gwt.viz.shared.export.model.Exportable;
import csi.client.gwt.viz.shared.export.model.VisualizationExportData;
import csi.server.common.model.visualization.chart.DrillChartViewDef;

/**
 * Builds the appropriate ExportSettings depending on the ExportType.
 * @author Centrifuge Systems, Inc.
 */
public class ExportSettingsFactory {

    public static ExportSettings createExportSettings(Exportable exportable, ExportType exportType){
        String name = exportable.getName();
        switch (exportType){
            case PNG:
                VisualizationExportData visualizationExportData = (VisualizationExportData)exportable;
                if(visualizationExportData.getData() instanceof DrillChartViewDef)
                    return new ExportSettings(name, exportType);
                break;
            case CSV:
            case ANX:
                return new ExportUseSelectionSettings(name, exportType);
        }
        return new ExportSettings(name, exportType);
    }
}
