package csi.client.gwt.dataview.export.kml;

import java.util.Collection;
import java.util.List;

import csi.client.gwt.viz.Visualization;
import csi.server.common.dto.CreateKmlRequest;
import csi.server.common.model.FieldDef;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.kml.KmlMapping;

public interface KmlExport {
    void show();

    View getView();

    Collection<FieldDef> getDataviewFieldDefs();

    Model getModel();

    List<Filter> getFilters();

    String getDataviewUUID();

    List<Visualization> getVisualizations();

    String getExportName();

    void updateDataviewKmlRequest(CreateKmlRequest request);

    void save();

    interface View {
        void show();

        void setPresenter(Presenter presenter);

        void setFilter(Filter filter);

        void updateFilters();

        void addMapping(KmlMapping kmlMapping);

        void setVisualization(Visualization visualization);

        void setMappings(List<KmlMapping> kmlMappings);

        void removeMapping(KmlMapping kmlMapping);
    }

    interface Model {
        Filter getFilter();

        void setFilter(Filter filter);

        Visualization getVisualization();

        void setVisualization(Visualization visualization);

        List<KmlMapping> getKmlMappings();

        CreateKmlRequest createRequest(String dataviewUUID);
    }

    interface Presenter {

        void newMapping();

        void createFilter();

        void setFilter(Filter filter);

        void setVisualization(Visualization visualization);

        void editFilter(KmlMapping kmlMapping);

        void createKML();

        void removeMapping(KmlMapping kmlMapping);
    }
}
