package csi.client.gwt.viz.map;

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

public class SelectionModeHandler implements VizButtonHandler, ClickHandler {

    private static final CentrifugeConstants centrifugeConstants = CentrifugeConstantsLocator.get();
    private MapPresenter presenter;
    private Button button;

    public enum SelectionMode {
        PAN,
        RECTANGLE,
        CIRCLE,
        POLYGON;

        public SelectionMode getNext() {
            int index = SelectionMode.values().length - 1;
            if (this.ordinal() < index) {
                SelectionMode sm = SelectionMode.values()[this.ordinal() + 1];
                return sm;
            } else {
                SelectionMode sm = SelectionMode.values()[0];
                return sm;
            }
//            return this.ordinal() < SelectionMode.values().length - 1
//                    ? SelectionMode.values()[this.ordinal() + 1]
//                    : SelectionMode.values()[0];
        }
    }

    private SelectionMode selectionMode = SelectionMode.PAN;

    public SelectionModeHandler(MapPresenter presenter,  String dataViewUuid, String vizDefUuid) {
        this.presenter = presenter;
        WebMain.injector.getVortex().execute((Integer value) -> setSelectionMode(value), MapActionsServiceProtocol.class).getSelectionMode(dataViewUuid, vizDefUuid);

    }


    @Override
    public void onClick(ClickEvent event) {
        selectionMode = selectionMode.getNext();
        switch (selectionMode) {
            case PAN:
                presenter.getView().setSelectionModePan();
                presenter.setSelectionMode(SelectionMode.valueOf("PAN").ordinal());
                break;
            case RECTANGLE:
                presenter.getView().setSelectionModeRectangle();
                presenter.setSelectionMode(SelectionMode.valueOf("RECTANGLE").ordinal());
                break;
            case CIRCLE:
                presenter.getView().setSelectionModeCircle();
                presenter.setSelectionMode(SelectionMode.valueOf("CIRCLE").ordinal());
                break;
            case POLYGON:
                presenter.getView().setSelectionModePolygon();
                presenter.setSelectionMode(SelectionMode.valueOf("POLYGON").ordinal());
                break;
        }
        updateLookAndTitle();
    }

    @Override
    public void bind(Button button) {
        this.button = button;
        button.addClickHandler(this);
        updateLookAndTitle();
    }

    @Override
    public String getTooltipText() {
        switch (selectionMode) {
            case PAN:
                return centrifugeConstants.mapViewSelectionModePan();
            case RECTANGLE:
                return centrifugeConstants.mapViewSelectionModeRectangle();
            case CIRCLE:
                return centrifugeConstants.mapViewSelectionModeCircle();
            case POLYGON:
                return centrifugeConstants.mapViewSelectionModePolygon();
        }
        return null;
    }

    private void updateLookAndTitle() {
        if (button != null) {
            switch (selectionMode) {
                case PAN:
                    button.setTitle(centrifugeConstants.mapViewSelectionModePan());
                    button.setIcon(IconType.MOVE);
                    break;
                case RECTANGLE:
                    button.setTitle(centrifugeConstants.mapViewSelectionModeRectangle());
                    button.setIcon(IconType.STOP);
                    break;
                case CIRCLE:
                    button.setTitle(centrifugeConstants.mapViewSelectionModeCircle());
                    button.setIcon(IconType.CIRCLE);
                    break;
                case POLYGON:
                    button.setTitle(centrifugeConstants.mapViewSelectionModePolygon());
                    button.setIcon(IconType.STAR);
                    break;
                }
        }
    }

    public int getSelectionMode() {
        return selectionMode.ordinal();
    }

    public void setSelectionMode(int selectionType) {
        this.selectionMode = selectionMode.values()[selectionType];
        updateLookAndTitle();
    }

}
