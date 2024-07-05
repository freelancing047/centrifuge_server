package csi.client.gwt.viz.graph.controlbar;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.fx.client.Draggable;

public class EndControl implements IsWidget {
    private GraphControlBarView graphControlBarView;
    private final Button endButton;
    private final Draggable draggable;

    public EndControl() {

        endButton = new Button("", IconType.CHEVRON_DOWN);
        styleButton();
        endButton.setType(ButtonType.LINK);
        draggable = new Draggable(endButton);
    }

    private void styleButton() {
        endButton.addStyleName("graph-time-end");//NON-NLS
    }

    public void setGraphControlBarView(GraphControlBarView graphControlBarView) {
        this.graphControlBarView = graphControlBarView;
        initDraggable();
    }

    private void initDraggable() {
        draggable.setContainer(graphControlBarView.getControlPanel());
        draggable.setEnabled(true);
        draggable.setUseProxy(false);
        draggable.setConstrainVertical(true);
    }


    @Override
    public Widget asWidget() {
        return endButton;
    }
}
