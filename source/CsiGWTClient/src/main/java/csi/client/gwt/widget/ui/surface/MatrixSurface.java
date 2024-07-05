package csi.client.gwt.widget.ui.surface;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.sencha.gxt.widget.core.client.info.Info;
import csi.client.gwt.viz.matrix.MatrixModel;
import csi.client.gwt.viz.matrix.MatrixView;
import csi.client.gwt.viz.matrix.menu.MatrixContextMenuPresenter;
import csi.client.gwt.widget.drawing.DrawingPanel;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.shared.core.imaging.PNGImageComponent;
import csi.shared.core.visualization.matrix.Cell;

import java.util.Collection;

public class MatrixSurface extends ResizeComposite {

    private static final String IMAGE_PNG = "image/png";
    public static final String GRAY = "gray";
    private static final String NONE = "none";
    private static final String POINTER_EVENTS = "pointerEvents";
    private static final String AXIS_Y = "axisY";
    private static final String AXIS_X = "axisX";
    private static final int WIDTH_LABEL = 75;
    private static final int WIDTH_OVERVIEW = 100;
    private static final int DIM_HIT_GRID = 50;

    private DrawingPanel axisY;
    private MatrixView matrixView;
    private DrawingPanel gridCanvas;
    private DrawingPanel mainCanvas;
    private DrawingPanel axisX;
    protected MatrixMainCanvas mmCanvas;
    private MatrixModel model;
    private MatrixPositionIndicator matrixPositionIndicator;

    public MatrixSurface(MatrixView matrixView) {

        this.matrixView = matrixView;
        this.model = matrixView.getModel();
        FullSizeLayoutPanel panel = new FullSizeLayoutPanel();

        createMainCanvas();
        panel.insert(mainCanvas, 0);

        initWidget(panel);
        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                try {
                    mainCanvas.render();
                }catch (Exception e){}
                return false;
            }
        //FIxME: should be less
        }, 700);
        addContextMenuHandler();

    }
    MatrixContextMenuPresenter contextMenuPresenter;
    private void addContextMenuHandler() {

        contextMenuPresenter = new MatrixContextMenuPresenter(matrixView.presenter);

        this.sinkEvents(Event.ONCONTEXTMENU);
        this.addHandler(new ContextMenuHandler() {
            @Override
            public void onContextMenu(ContextMenuEvent event) {
                if(matrixView.getMouseX() != -1 && matrixView.getMouseY() != -1) {
                    event.preventDefault();
                    event.stopPropagation();
                    contextMenuPresenter.showMenuAt(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY(), matrixView.getMouseX(), matrixView.getMouseY(), matrixView.getHoverCell());
                }
            }
        }, ContextMenuEvent.getType());

    }

    public void refresh(){
        mmCanvas.forceRender();
        mainCanvas.render();
    }

    @Override
    public void onResize() {
        super.onResize();

        refresh();
    }

    public DrawingPanel getMainCanvas() {
        return mainCanvas;
    }

    private void createMainCanvas() {
        mainCanvas = new DrawingPanel();
        Layer layer = new MatrixMainLayer(matrixView);
        mmCanvas = new MatrixMainCanvas(matrixView);
        layer.addItem(new MatrixXAxis(matrixView));
        layer.addItem(new MatrixYAxis(matrixView));
        layer.addItem(mmCanvas);
        layer.addItem(new MatrixOverview(matrixView));
        layer.addItem(new MatrixCrosshair(matrixView));
        layer.addItem(new MatrixTitle(matrixView, mainCanvas));
        matrixPositionIndicator = new MatrixPositionIndicator(matrixView, mainCanvas);
        layer.addItem(matrixPositionIndicator);
        mainCanvas.addLayer(layer);
    }

    public PNGImageComponent getImageComponent(){
        PNGImageComponent gridPNG = new PNGImageComponent();
        gridPNG.setData(mainCanvas.getCanvas().toDataUrl(IMAGE_PNG)); //$NON-NLS-1$
        gridPNG.setX(0);
        gridPNG.setY(0);
        return  gridPNG;
    }

    public MatrixMainCanvas getMatrixMainCanvas(){
        return mmCanvas;
    }

    private void createGridCanvas() {
        gridCanvas = new DrawingPanel();
        Layer layer = new Layer();
        gridCanvas.addLayer(layer);
    }

    public void setCells(Collection<Cell> cells) {
        mmCanvas.setCells(cells);
    }

}
