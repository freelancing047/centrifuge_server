package csi.client.gwt.dataview.export.kml;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Created by Patrick on 10/20/2014.
 */
class Initialize extends AbstractKmlActivity {
    private KmlExport kmlExport;

    public Initialize(KmlExport kmlExport) {
        this.kmlExport = kmlExport;
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        KmlExport.View view = kmlExport.getView();
        KmlExport.Model model = kmlExport.getModel();
        view.setMappings(model.getKmlMappings());
        view.setFilter(model.getFilter());
        view.setVisualization(model.getVisualization());
    }
}
