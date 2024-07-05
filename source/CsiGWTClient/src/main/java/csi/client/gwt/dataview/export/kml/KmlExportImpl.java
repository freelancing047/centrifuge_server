package csi.client.gwt.dataview.export.kml;

import java.util.Collection;
import java.util.List;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.util.CSIActivityManager;
import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.graph.node.settings.NodeSettingsActivityMapper;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.CreateKmlRequest;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.filter.Filter;
import csi.server.common.service.api.GeoSpatialActionsServiceProtocol;

/**
 * Created by Patrick on 10/20/2014.
 */
public
class KmlExportImpl implements KmlExport {
    private final Model model;
    private final View view;
    private final EventBus eventBus;
    private final CSIActivityManager activityManager;

    private AbstractDataViewPresenter dataViewPresenter;

    public KmlExportImpl(AbstractDataViewPresenter dataViewPresenter2) {
        this.dataViewPresenter = dataViewPresenter2;
        model = new KmlExportSettings();
        CreateKmlRequest kmlRequest = dataViewPresenter2.getDataView().getMeta().getKmlRequest();
        if (kmlRequest != null) {
            model.getKmlMappings().addAll(kmlRequest.getKmlMappings());
            if (kmlRequest.getVisualizationFilter() != null) {
                for (Visualization visualization : dataViewPresenter.getVisualizations()) {
                    if (visualization.getUuid().equals(kmlRequest.getVisualizationFilter().getUuid())) {
                        model.setVisualization(visualization);
                    }
                }
            }
            model.setFilter(kmlRequest.getFilter());
        }
        view = new KmlExportDialog(this);
        eventBus = new SimpleEventBus();
        activityManager = new CSIActivityManager(new NodeSettingsActivityMapper(), eventBus);
        activityManager.setActivity(new Initialize(this));
    }

    @Override
    public void show() {
        activityManager.setActivity(new Show(this));
    }

    public View getView() {
        return view;
    }

    @Override
    public Collection<FieldDef> getDataviewFieldDefs() {
        DataModelDef modelDef = dataViewPresenter.getDataView().getMeta().getModelDef();
        List<FieldDef> fields = FieldDefUtils.getAllSortedFields(modelDef, FieldDefUtils.SortOrder.ALPHABETIC);
        return fields;
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public List<Filter> getFilters() {
        return dataViewPresenter.getDataView().getMeta().getFilters();
    }

    @Override
    public String getDataviewUUID() {
        return dataViewPresenter.getDataView().getUuid();
    }

    @Override
    public List<Visualization> getVisualizations() {
        return dataViewPresenter.getVisualizations();
    }

    @Override
    public String getExportName() {
        return dataViewPresenter.getName();
    }

    @Override
    public void updateDataviewKmlRequest(CreateKmlRequest request) {
        dataViewPresenter.getDataView().getMeta().setLastKmlRequest(request);
    }

    @Override
    public void save() {
        final VortexFuture<String> future = WebMain.injector.getVortex().createFuture();
        final CreateKmlRequest request = getModel().createRequest(getDataviewUUID());
        future.execute(GeoSpatialActionsServiceProtocol.class).saveKML(request);
        dataViewPresenter.getDataView().getMeta().setKmlRequest(request);
    }
}
