package csi.client.gwt.viz.graph.controlbar;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class StartControl implements IsWidget {


    private final Button startButton;
//    private final Draggable draggable;
    private GraphControlBarView graphControlBarView;

    StartControl() {
        startButton = new Button("", IconType.CHEVRON_DOWN);
//        draggable = new Draggable(startButton);
        initButton();
    }

    public void setGraphControlBarView(GraphControlBarView graphControlBarView) {
        this.graphControlBarView = graphControlBarView;
//        initDraggable();
    }


    private void initButton() {
        addButtonHandlers();
        styleButton();
    }

    private void styleButton() {
        startButton.setType(ButtonType.LINK);
        startButton.addStyleName("graph-time-start");//NON-NLS
    }

    private void addButtonHandlers() {
        startButton.addMouseMoveHandler(new MyMouseMoveHandler());
        startButton.addMouseOverHandler(new MyMouseOverHandler());
        startButton.addMouseOutHandler(new MyMouseOutHandler());
    }

//    private void initDraggable() {
//        draggable.setContainer(this.graphControlBarView.getControlPanel());
//        draggable.setUseProxy(false);
//        draggable.setEnabled(true);
//        draggable.setConstrainVertical(true);
//        draggable.setXConstraint(0, 450);
//    }

    @Override
    public Widget asWidget() {
        return startButton;
    }

    private void updateTooltipPosition(Button button1) {
        graphControlBarView.setStartTooltipX(button1.getAbsoluteLeft());

    }

    private class MyMouseOutHandler implements MouseOutHandler {
        @Override
        public void onMouseOut(MouseOutEvent event) {
            if (graphControlBarView != null) {

                graphControlBarView.showStartTooltip(false);
            }
        }
    }

    private class MyMouseOverHandler implements MouseOverHandler {
        @Override
        public void onMouseOver(MouseOverEvent event) {
            graphControlBarView.showStartTooltip(true);
            updateTooltipPosition(startButton);
        }
    }

    private class MyMouseMoveHandler implements MouseMoveHandler {
        @Override
        public void onMouseMove(MouseMoveEvent event) {
            updateTooltipPosition(startButton);
        }
    }

}
