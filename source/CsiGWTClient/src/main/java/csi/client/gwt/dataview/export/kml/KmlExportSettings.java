package csi.client.gwt.dataview.export.kml;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Document;

import csi.client.gwt.viz.Visualization;
import csi.server.common.dto.CreateKmlRequest;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.kml.KmlMapping;
import csi.server.common.model.visualization.VisualizationDef;

/**
 * Created by Patrick on 10/20/2014.
 */
public class KmlExportSettings implements KmlExport.Model {
    private List<KmlMapping> kmlMappings = Lists.newArrayList();
    private Filter filter;
    private Visualization visualizationFilterBy;

    KmlExportSettings() {
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public void setFilter(Filter filter) {

        this.filter = filter;
    }

    @Override
    public Visualization getVisualization() {
        return visualizationFilterBy;
    }

    public void setVisualization(Visualization visualization) {
        this.visualizationFilterBy = visualization;
    }

    @Override
    public List<KmlMapping> getKmlMappings() {
        return kmlMappings;
    }

    @Override
    public CreateKmlRequest createRequest(String dataviewUUID) {
        final CreateKmlRequest request = new CreateKmlRequest();
        request.setBaseURL(Document.get().getURL());
        request.setDataviewUuid(dataviewUUID);
        request.getKmlMappings().addAll(getKmlMappings());
        request.setFilter(getFilter());
        Visualization visualization = getVisualization();
        VisualizationDef visualizationDef = null;
        if (visualization != null) {
            visualizationDef = visualization.getVisualizationDef();
        }
        request.setVisualizationFilter(visualizationDef);
        return request;
    }
}
