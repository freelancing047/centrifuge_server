package csi.client.gwt.widget.ui.surface.handlers;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Timer;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.button.VizButtonHandler;
import csi.client.gwt.viz.matrix.MatrixModel;
import csi.client.gwt.viz.matrix.MatrixPresenter;

public class ZoomOutHandler implements VizButtonHandler, MouseUpHandler, MouseDownHandler, ClickHandler {
    private MatrixPresenter presenter;

    private static final double ZOOM_AMOUNT_ON_HOLD = 1.1;
    private static final int ZOOM_BUTTON_MOUSE_HOLD_TIME = 200;
    private static final double ZOOM_AMOUNT_ON_CLICK = 1.3;
    protected boolean stopZoom = false;
    protected boolean noClick = false;

    double zoomAmount =  1;
    UpdateTimer timer;


    public class UpdateTimer extends Timer {
        @Override
        public void run() {
            presenter.getView().fetchData();
        }
    }


    private RepeatingCommand zoomMoreCommand = new RepeatingCommand() {

        @Override
        public boolean execute() {
            noClick = true;
            if(!presenter.getView().isLoaded()){
                return false;
            }
            if (!stopZoom) {
                MatrixModel model = presenter.getModel();
                double pow = Math.pow(2, .25);
                double width = model.getWidth() * pow;
                double height = model.getHeight() * pow;

                width = Math.max(2, width);
                height = Math.max(2, height);

                model.setCurrentView(model.getX() + (model.getWidth() - width) / 2.0,model.getY() + (model.getHeight() - height) / 2.0, width, height );

                presenter.getView().refresh();
            }
            return !stopZoom;
        }
    };

    public ZoomOutHandler(MatrixPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onClick(ClickEvent event) {
        if(!presenter.getView().isLoaded()){
            return;
        }

        if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
            if (!noClick) {
                MatrixModel model = presenter.getModel();
                double pow = Math.pow(2, .25);
                double width = model.getWidth() * pow;
                double height = model.getHeight() * pow;

                width = Math.max(2, width);
                height = Math.max(2, height);

                model.setCurrentView(model.getX() + (model.getWidth() - width) / 2.0,model.getY() + (model.getHeight() - height) / 2.0, width, height );


                if(timer == null){
                    timer = new UpdateTimer();
                }else{
                    timer.cancel();
                }
                timer.schedule(500);

            }
        }
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {
        if(!presenter.getView().isLoaded()){
            return;
        }
        if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
            stopZoom = false;// reset
            noClick = false; // reset
            Scheduler.get().scheduleFixedPeriod(zoomMoreCommand, ZOOM_BUTTON_MOUSE_HOLD_TIME);
        }
    }

    @Override
    public String getTooltipText() {
        return CentrifugeConstantsLocator.get().zoomIn();
    }

    @Override
    public void onMouseUp(MouseUpEvent event) {
        if(!presenter.getView().isLoaded()){
            return;
        }
        if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
            mouseUpAction();
        }
    }

    private void mouseUpAction(){
        stopZoom = true;
        if (noClick) {
            if(timer == null){
                timer = new UpdateTimer();
            }else{
                timer.cancel();
            }
            timer.schedule(200);

        }
    }

    @Override
    public void bind(Button button) {
        button.addClickHandler(this);
        button.addMouseDownHandler(this);
        button.addMouseUpHandler(this);
        button.addDragHandler(event -> {
            mouseUpAction();
        });
    }
}