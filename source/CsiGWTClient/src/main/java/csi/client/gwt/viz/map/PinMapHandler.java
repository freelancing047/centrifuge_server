package csi.client.gwt.viz.map;

import com.emitrom.lienzo.client.core.util.Console;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.button.VizButtonHandler;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.server.common.service.api.MapActionsServiceProtocol;

public class PinMapHandler implements VizButtonHandler, ClickHandler {
    private static final CentrifugeConstants centrifugeConstants = CentrifugeConstantsLocator.get();
    private MapPresenter presenter;
    private Button button;
    private boolean mapPinned;

    public PinMapHandler(MapPresenter presenter, String dataViewUuid, String vizDefUuid) {
        this.presenter = presenter;
        WebMain.injector.getVortex().execute((Boolean value) -> setMapPinned(value), MapActionsServiceProtocol.class).isMapPinned(dataViewUuid, vizDefUuid);
    }

    @Override
    public void onClick(ClickEvent event) {
        mapPinned = !mapPinned;
        presenter.setMapPinned(mapPinned);
        updateLookAndTitle();
    }

    private void updateLookAndTitle() {
        Console.log("PinMapHandler.updateLookAndTitle");
        if (button != null) {
            if (mapPinned) {
                button.setTitle(centrifugeConstants.mapViewUnpinMap());
                button.setIcon(IconType.LOCK);
            } else {
                button.setTitle(centrifugeConstants.mapViewPinMap());
                button.setIcon(IconType.UNLOCK);
            }
        }
    }

    @Override
    public void bind(Button button) {
        this.button = button;
        button.addClickHandler(this);
        updateLookAndTitle();
    }

    @Override
    public String getTooltipText() {
        return centrifugeConstants.mapViewPinMap();
    }

    public boolean isMapPinned() {
        return mapPinned;
    }

    public void setMapPinned(boolean mapPinned) {
        this.mapPinned = mapPinned;
        updateLookAndTitle();
    }
}
