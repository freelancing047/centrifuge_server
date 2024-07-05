package csi.client.gwt.viz.map;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.button.VizButtonHandler;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.map.view.MapView;
import csi.client.gwt.viz.shared.chrome.VizChrome;
import csi.client.gwt.viz.shared.chrome.panel.VizPanel;

public class BackToBundleHandler implements VizButtonHandler, ClickHandler {
    private MapPresenter presenter;
    private Button button;
    private VizChrome chrome;
    private boolean isVisible = false;

    public BackToBundleHandler(MapPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onClick(ClickEvent event) {
        MapView view = presenter.getView();
        view.backToBundle();
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
        return CentrifugeConstantsLocator.get().mapViewBack();
    }

    public void setVizChrome(VizChrome chrome) {
        this.chrome = chrome;
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
            VBoxLayoutContainer buttonGroupContainer = ((VizPanel) chrome).getButtonGroupContainer();
            if (buttonGroupContainer != null) {
                buttonGroupContainer.forceLayout();
            }
        }
    }
}
