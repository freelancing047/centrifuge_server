package csi.client.gwt.viz.map;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.button.VizButtonHandler;
import csi.client.gwt.viz.map.overview.OverviewPresenter;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.map.view.MapView;
import csi.server.common.service.api.MapActionsServiceProtocol;

public class ZoomHomeHandler implements VizButtonHandler, ClickHandler {
    private MapPresenter presenter;

    public ZoomHomeHandler(MapPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onClick(ClickEvent event) {
        MapView view = presenter.getView();
        if (presenter.isUseTrackMap()) {
            OverviewPresenter overviewPresenter = presenter.getOverview();
            overviewPresenter.reset();
            WebMain.injector.getVortex().execute((Boolean refreshMap) -> view.zoomHome(), MapActionsServiceProtocol.class).setRangeHome(presenter.getDataView().getUuid(), presenter.getUuid());
        } else {
            view.zoomHome();
        }
    }

    @Override
    public void bind(Button button) {
        button.addClickHandler(this);
    }

    @Override
    public String getTooltipText() {
        return CentrifugeConstantsLocator.get().mapViewHome();
    }
}
