package csi.client.gwt.viz.shared.export.model;

import csi.client.gwt.viz.Visualization;
import csi.server.common.model.visualization.VisualizationDef;

/**
 * Created by Ivan on 7/28/2017.
 */
public interface VisualizationExportable extends Exportable {

        Visualization getVisualization();
        VisualizationDef getData();
        String getName();
        void setName(String name);
}

