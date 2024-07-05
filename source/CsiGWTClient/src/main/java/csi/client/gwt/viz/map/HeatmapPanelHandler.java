package csi.client.gwt.viz.map;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.button.VizButtonHandler;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.map.view.MapView;

public class HeatmapPanelHandler implements VizButtonHandler, ClickHandler {
    private MapPresenter presenter;
    private Button button;
    private boolean isVisible = false;

    public HeatmapPanelHandler(MapPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onClick(ClickEvent event) {
        MapView view = presenter.getView();

        view.heatmapPanel();
    }

    @Override
    public void bind(Button button) {
        this.button = button;
        button.addClickHandler(this);
        if (isVisible) {
            showButton();
        } else {
            hideButton();
        }
    }

    @Override
    public String getTooltipText() {
        return CentrifugeConstantsLocator.get().mapViewConfigureHeatmap();
    }

    public void hideButton() {
        isVisible = false;
        if (button != null) {
            button.setVisible(false);
        }
    }

    public void showButton() {
        isVisible = true;
        if (button != null) {
            button.setVisible(true);
        }
    }
}
