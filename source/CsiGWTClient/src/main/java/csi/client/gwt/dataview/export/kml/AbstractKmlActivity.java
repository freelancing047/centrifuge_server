package csi.client.gwt.dataview.export.kml;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.viz.Visualization;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.kml.KmlMapping;

/**
 * Created by Patrick on 10/20/2014.
 */
abstract class AbstractKmlActivity implements Activity, KmlExport.Presenter {
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

    }

    @Override
    public void newMapping() {

    }

    @Override
    public void createFilter() {

    }

    @Override
    public void setFilter(Filter filter) {

    }

    @Override
    public void setVisualization(Visualization visualization) {

    }

    @Override
    public void editFilter(KmlMapping kmlMapping) {

    }

    @Override
    public void createKML() {

    }

    @Override
    public void removeMapping(KmlMapping kmlMapping) {

    }
}
