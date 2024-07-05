package csi.client.gwt.viz.graph.controlbar;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class TimeScrubber implements IsWidget {
    private final Button button;
    //    private final Draggable draggable;
    private GraphControlBarView graphControlBarView;

    public TimeScrubber() {
        button = new Button();
        setWidth("10px");//NON-NLS
        button.addStyleName("graph-time-scrubber");//NON-NLS
        preventTransitionWhileDragging();
    }

    private void preventTransitionWhileDragging() {
        button.addMouseDownHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
//                button.getElement().getStyle().setProperty("transition", "none");
                button.addStyleName("transition-clear");//NON-NLS

            }
        });
    }

    public void setGraphControlBarView(GraphControlBarView graphControlBarView) {
        this.graphControlBarView = graphControlBarView;
    }


    public Button getControlButton(){

        return button;
    }


    private void setWidth(String width) {
        button.setWidth(width);
    }

    @Override
    public Widget asWidget() {
        return button;
    }

    public void setVisible(boolean show) {
        button.setVisible(show);
    }
}
