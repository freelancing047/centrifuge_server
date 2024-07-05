package csi.client.gwt.dataview.export.kml;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.export.kml.mapping.KmlMappingEditor;
import csi.client.gwt.dataview.export.kml.mapping.KmlMappingEditorImpl;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.DownloadHelper;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.shared.filter.CreateEditFilterDialog;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.server.common.dto.CreateKmlRequest;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.kml.KmlMapping;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.service.api.GeoSpatialActionsServiceProtocol;

/**
 * Created by Patrick on 10/20/2014.
 */
class Show extends AbstractKmlActivity {
    public static final String KML_FILE_EXTENTION = ".kml"; //$NON-NLS-1$
    private KmlExport kmlExport;
    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public Show(KmlExport kmlExport) {
        this.kmlExport = kmlExport;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        KmlExport.View view = kmlExport.getView();
        view.setPresenter(this);
        view.show();

    }

    @Override
    public void newMapping() {
        KmlMappingEditor kmlMappingEditor = new KmlMappingEditorImpl(kmlExport);
        kmlMappingEditor.addSaveHandler(new KmlMappingEditor.KmlMappingSaveHandler() {
            @Override
            public void onSave(KmlMapping kmlMapping) {
                kmlExport.getView().addMapping(kmlMapping);
                kmlExport.getModel().getKmlMappings().add(kmlMapping);
            }
        });

        kmlMappingEditor.show();
    }

	@Override
	public void createFilter() {
		new CreateEditFilterDialog(null, null, kmlExport.getDataviewUUID(),
				new CreateEditFilterDialog.FilterSaveCallback() {
					@Override
					public void onSave(Filter filter) {
						kmlExport.getView().updateFilters();
						setFilter(filter);
					}
				}, new CreateEditFilterDialog.FilterCancelCallback() {
					@Override
					public void onCancel() {
						setFilter(null);
					}
				}).show();
	}

    @Override
    public void setFilter(Filter filter) {
        KmlExport.Model model = kmlExport.getModel();
        KmlExport.View view = kmlExport.getView();
        model.setFilter(filter);
        view.setFilter(model.getFilter());
    }

    @Override
    public void setVisualization(Visualization visualization) {
        kmlExport.getModel().setVisualization(visualization);
        kmlExport.getView().setVisualization(kmlExport.getModel().getVisualization());
    }

    @Override
    public void createKML() {
        if (validateKML()) {

            final VortexFuture<String> future = WebMain.injector.getVortex().createFuture();
            final CreateKmlRequest request = new CreateKmlRequest();
            request.setBaseURL(Document.get().getURL());
            request.setDataviewUuid(kmlExport.getDataviewUUID());
            request.getKmlMappings().addAll(kmlExport.getModel().getKmlMappings());
            request.setFilter(kmlExport.getModel().getFilter());
            Visualization visualization = kmlExport.getModel().getVisualization();
            VisualizationDef visualizationDef = null;
            if (visualization != null) {
                visualizationDef = visualization.getVisualizationDef();
            }
            request.setVisualizationFilter(visualizationDef);
            if (visualization != null) {
                VortexFuture<Void> voidVortexFuture = visualization.saveSettings(false, false);
                voidVortexFuture.addEventHandler(new AbstractVortexEventHandler<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        future.execute(GeoSpatialActionsServiceProtocol.class).createKML(request);
                    }
                });
            } else {
                future.execute(GeoSpatialActionsServiceProtocol.class).createKML(request);
            }
            future.addEventHandler(new AbstractVortexEventHandler<String>() {
                @Override
                public void onSuccess(String result) {
                    DownloadHelper.download(kmlExport.getExportName(), KML_FILE_EXTENTION, result);
                    kmlExport.updateDataviewKmlRequest(request);
                }
            });
        }
    }

    private boolean validateKML() {
        if (kmlExport.getModel().getKmlMappings().isEmpty()) {
            new ErrorDialog(i18n.ShowKmlExportErrorTitle(), i18n.ShowKmlExportErrorMessage()).show(); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        return true;
    }

    @Override
    public void removeMapping(KmlMapping kmlMapping) {
        kmlExport.getModel().getKmlMappings().remove(kmlMapping);
        kmlExport.getView().removeMapping(kmlMapping);
    }

    @Override
    public void editFilter(KmlMapping kmlMappingModel) {
        KmlMappingEditor kmlMappingEditor = new KmlMappingEditorImpl(kmlExport, kmlMappingModel);
        kmlMappingEditor.addSaveHandler(new KmlMappingEditor.KmlMappingSaveHandler() {
            @Override
            public void onSave(KmlMapping kmlMapping) {
                kmlExport.getView().setMappings(kmlExport.getModel().getKmlMappings());
            }
        });

        kmlMappingEditor.show();
    }
}
