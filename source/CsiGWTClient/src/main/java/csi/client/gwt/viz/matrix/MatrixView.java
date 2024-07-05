/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.client.gwt.viz.matrix;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ButtonGroup;
import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Strings;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.TextAlign;
import com.google.gwt.canvas.dom.client.Context2d.TextBaseline;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.SecurityBanner;
import csi.client.gwt.viz.shared.BroadcastAlert;
import csi.client.gwt.viz.shared.chrome.panel.RenderSize;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Vortex;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.InfoPanel;
import csi.client.gwt.widget.ui.surface.MatrixSurface;
import csi.client.gwt.widget.ui.surface.ScrollableSurfaceRenderable;
import csi.client.gwt.widget.ui.surface.handlers.ZoomInHandler;
import csi.client.gwt.widget.ui.surface.handlers.ZoomOutHandler;
import csi.server.common.util.ValuePair;
import csi.shared.core.imaging.ImagingRequest;
import csi.shared.core.imaging.PNGImageComponent;
import csi.shared.core.visualization.matrix.*;

import static csi.client.gwt.widget.drawing.BaseRenderable.drawRoundedRectangle;

/**
 * @author Centrifuge Systems, Inc.
 */
public class MatrixView extends ResizeComposite {
    private static final String GRAY = "gray";
    public MatrixPresenter presenter;

    @UiField(provided = true)
    Canvas titleCanvas2 = Canvas.createIfSupported();
    /**
     * Panel which will display the message informing the user that the visualization item limit has been reached.
     */
    private SimplePanel limitReachedPanel;
    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    /**
     * Deck layout contextMenu ( allows only one widget to be visible at one time)
     *         holds the layoutPanel ( 0 ) , and a label for unable to display
     *
     *  If maximum number of cells limit is exceeded - a Simple HTML contextMenu will be inserted
     *  and activated to display the message to the user.
     */
    @UiField
    DeckLayoutPanel deckLayoutPanel;
    /**
     *  Holds the Matrix Visualization: surface, title, control buttons, title canvas
     */
    @UiField
    LayoutPanel layoutPanel;
    @UiField(provided = true)
    MatrixSurface surface;
    @UiField(provided = true)
    Canvas titleCanvas;
    @UiField MatrixStyle style;

    private Cell hoverCell;
    private int mouseY;
    private int mouseX;
    private boolean loaded = false;
    private boolean loadedOnce = false;
    private String vizTitle = "";
    private ButtonGroup btnGroup;

    interface MatrixStyle extends CssResource {
        String loader();
        String buttonFix();

        String noEvent();
    }
    private MatrixSearchDialog search;
    private Icon spinnerIcon = new Icon(IconType.SPINNER);

    interface SpecificUiBinder extends UiBinder<Widget, MatrixView> {}
    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public MatrixView(MatrixPresenter presenter) {
        super();

        this.presenter = presenter;
        titleCanvas = Canvas.createIfSupported();
        surface = new MatrixSurface(this);
        search = new MatrixSearchDialog(presenter);
//        resetSearchDialog();

        initWidget(uiBinder.createAndBindUi(this));
        addControlButtons();
        addClearHighlightsOnMouseOut();

        //displays the layoutPanel
        showMatrix();
    }


    public boolean validateCellVisible(Cell c){
        return surface.getMatrixMainCanvas().isInViewport(c);
    }

    public void resetSearchDialog(){
        if(search != null){
            search.resetAndHide();
        }

//        search = new MatrixSearchDialog(presenter);
//        search.show();
    }

    private void addClearHighlightsOnMouseOut() {
        // clears the highlights on mouseout.
        surface.addDomHandler(event -> {
            clearHoverCell();
            setMouseXY(-1, -1);
            surface.getMainCanvas().render();
        }, MouseOutEvent.getType());
    }

    public void setLoaded(boolean isLoaded) {
        this.loaded = isLoaded;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public Vortex getVortex(){
        return presenter.getVortex();
    }

    public String getMatrixTitle() {
        String measureName =  presenter.getVisualizationDef().getMatrixSettings().getMatrixMeasureDefinition().getComposedName();
        if(presenter.getVisualizationDef().getMatrixSettings().isUseCountForMeasure()){
            measureName = "Count(*)";
        }
        if (Strings.isNullOrEmpty(getVizTitle())) {
            return measureName;
        }
        return getVizTitle() + " \u2014 " + measureName ;
    }


    /**
     * Any cell that is in the response, as long as its currently visible.
     * @param x
     * @param y
     */
    public void setHoverCell(int x, int y) {
        hoverCell = getModel().getMatrixDataResponse().getCell(x, y);
    }

    public Cell getHoverCell() {
        return hoverCell;
    }

    public void clearHoverCell() {
        hoverCell = null;
    }

    public void setMouseXY(int x, int y) {
        mouseX = x;
        mouseY = y;
    }

    public int getMouseY() {
        return mouseY;
    }

    public int getMouseX() {
        return mouseX;
    }

    public void fetchData() {
        MatrixModel model = getModel();
        surface.getMainCanvas().getElement().getStyle().setProperty("pointerEvents",  "none");
        loadedOnce = false;

        VortexFuture<MatrixWrapper> matrixWrapperVortexFuture = presenter.showInfoForRegion((int) model.getX(), (int) model.getY(), (int) (model.getX() + model.getWidth()), (int) (model.getY() + model.getHeight()));
        matrixWrapperVortexFuture.addEventHandler(new AbstractVortexEventHandler<MatrixWrapper>() {
                                                          @Override
            public void onSuccess(MatrixWrapper result) {
                MatrixMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(getModel().getVisualizationUuid()));
                surface.getMainCanvas().getElement().getStyle().setProperty("pointerEvents", "auto");
            }
        });
    }

    /**
     * soft loading spinner
     * @param toggle
     */
    public void setLoadingIndicator(boolean toggle){
        if(toggle) {
            presenter.getChrome().showLoadingSpinner();
        }else{
            presenter.getChrome().hideLoadingSpinner();
        }
    }

    private void addControlButtons() {
        btnGroup = new ButtonGroup();
        {
            Button button = createButton();
            button.setTitle(_constants.matrix_view_controlButtons_fit());
            button.setIcon(IconType.RESIZE_FULL);
            button.addClickHandler(event -> presenter.fullScreen(true));
            btnGroup.add(button);
        }
        {
            Button button = createButton();
            button.setIcon(IconType.ZOOM_IN);
            button.setTitle(_constants.matrix_view_controlButtons_zoom_in());
            btnGroup.add(button);
            ZoomInHandler zoomInHandler = new ZoomInHandler(presenter);
            zoomInHandler.bind(button);

        }
        {
            Button button = createButton();
            button.setIcon(IconType.ZOOM_OUT);
            button.setTitle(_constants.matrix_view_controlButtons_zoom_out());
            btnGroup.add(button);
            ZoomOutHandler handler = new ZoomOutHandler(presenter);
            handler.bind(button);
        }
        {
            Button button = createButton();
            button.setTitle(_constants.matrix_view_controlButtons_search());
            button.setIcon(IconType.SEARCH);
            ClickHandler click = event -> search.show(btnGroup.getAbsoluteTop(), btnGroup.getAbsoluteLeft());
            button.addClickHandler(click);
            btnGroup.add(button);
        }

        presenter.getChrome().addButton(btnGroup);
    }

    private Button createButton(){
        Button button = new Button();
        button.addStyleName("overlay-clear");//NON-NLS
        button.addStyleName("rightControlButton");//NON-NLS
        button.setType(ButtonType.DEFAULT);
        return button;
    }


    @Override
    public void onResize() {
        super.onResize();
        displayColorScale();
        titleCanvas.setCoordinateSpaceWidth(titleCanvas.getOffsetWidth());
        titleCanvas.setCoordinateSpaceHeight(titleCanvas.getOffsetHeight());
        search.updateDialogPosition(btnGroup.getAbsoluteTop(), btnGroup.getAbsoluteLeft());
        if (presenter.getMetrics() != null) {
            presenter.getMetrics().positionLegend();
        }
    }

    public void refresh() {
        surface.refresh();
    }

    public MatrixPresenter getPresenter() { return this.presenter; }

    public void displayCells(MatrixDataResponse response) {
        _displayCells(response);
    }

    public void showMatrix() {
        presenter.getChrome().removeFullScreenWindow();
        surface.getMainCanvas().getElement().getStyle().setProperty("pointerEvents", "auto");
        deckLayoutPanel.showWidget(0);
        deckLayoutPanel.getVisibleWidget().setVisible(true);

    }

    public void hideMatrix(){
        deckLayoutPanel.getVisibleWidget().setVisible(false);
    }

    // this should go away.
    public void showNoDataAvailable() {
        surface.getMainCanvas().getElement().getStyle().setProperty("pointerEvents",  "none");
        presenter.getChrome().addFullScreenWindow(InfoPanel.MESSAGE, InfoPanel.DEFAULT_ICON_TYPE);
    }

    private void _displayCells(MatrixDataResponse response) {
        this.setLoadingIndicator(false);
        surface.setCells(response.getCells());
        titleCanvas.setCoordinateSpaceWidth(titleCanvas.getOffsetWidth());
        titleCanvas.setCoordinateSpaceHeight(titleCanvas.getOffsetHeight());

    }

    public MatrixModel getModel(){
        return presenter.getModel();
    }

    private List<ScrollableSurfaceRenderable> responseToRenderables(List<Cell> cells) {
        List<ScrollableSurfaceRenderable> renderables = new ArrayList<ScrollableSurfaceRenderable>();

        for (Cell cell : cells) {
            MatrixRenderable renderable = null;
            // summary is going to be a special case.. i could merge that into with matrix type, but i don't htink its a good place for it.
            if (getModel().isSummary()) {
//                SummaryCell summary = (SummaryCell) cell;
                renderable = new SummaryCell(getModel().getSettings().isShowLabel(), presenter );
            } else {
                switch (getModel().getSettings().getMatrixType()) {
                    case BUBBLE:
                        renderable = new Bubble(getModel().getSettings().isShowLabel());
                        break;
                    case HEAT_MAP:
                        renderable = new HeatCell(getModel().getSettings().isShowLabel());
                        break;
                    case CO_OCCURRENCE:
                        renderable = new HeatCell(getModel().getSettings().isShowLabel());
                        break;
                    case CO_OCCURRENCE_DIR:
                        renderable = new HeatCell(getModel().getSettings().isShowLabel());
                        break;
                }
            }

            renderable.setCell(cell);
            renderable.setSelected(getModel().isSelected(cell));

            renderables.add(renderable);
        }

        return renderables;
    }

    MatrixDataRequest getViewMetricsRequest(){
        MatrixDataRequest viewMetricsRequest = getModel().getViewMetricsRequest();
        viewMetricsRequest.setDvUuid(presenter.getDataViewUuid());
        return viewMetricsRequest;
    }

    List<MatrixMetrics> calculateViewMetrics(){

        MatrixMetrics metrics = new MatrixMetrics();
        double x = getModel().getX();
        double y = getModel().getY();
        double height = y + getModel().getHeight() - 1;
        double width =  x  + getModel().getWidth() - 1;

        MatrixDataRequest req = new MatrixDataRequest();
        req.setVizUuid(getModel().getVisualizationUuid());
        req.setDvUuid(presenter.getDataViewUuid());
        req.setExtent((int)x, (int)width, (int)y, (int)height);

        // i guess i need to go to server..

        ArrayList<MatrixMetrics> matrixMetrics = new ArrayList<>();
        matrixMetrics.add(metrics);
        return matrixMetrics;
    }

    /**
     * This has a bug that sometimes the values come in as 1's for min/max, will have to look at it later
     */
    public void displayColorScale() {
        if(getModel().getMetrics() != null)
            drawColorScale(getModel().getMetrics().getMinValue(), getModel().getMetrics().getMaxValue());
    }

    private void drawColorScale(double minValue, double maxValue) {
        final int height = titleCanvas.getOffsetHeight() - presenter.getChrome().getButtonContainerHeight()/2;
        final int width = titleCanvas.getOffsetWidth();
        final int barHeight = calculateBarHeight(height);
        final int barStart = (height - barHeight) / 2;
        final Image image = new Image();
        image.addLoadHandler(new LoadHandler() {

            @Override
            public void onLoad(LoadEvent event) {
                if (matrixIsNotRendered()) {
                    return;
                }
                drawImage();
            }

            private boolean matrixIsNotRendered() {
                return width <= 0 || height <= 0;
            }

            private void drawImage() {
                Context2d ctx = titleCanvas.getContext2d();
                ctx.clearRect(0,0, titleCanvas.getOffsetWidth(), titleCanvas.getOffsetHeight());
                ctx.save();
                ctx.setGlobalAlpha(.75);
                ctx.drawImage(ImageElement.as(image.getElement()), width - 25, barStart, 20, barHeight);
                ctx.restore();
                drawColorScaleLables(minValue, maxValue, width, barHeight, barStart);
                image.removeFromParent();
            }

        });
        if(getModel().getColorScale() != null) {
            image.setUrl(getModel().getColorScale());
        }
        RootPanel.get().add(image);

        //Make sure we force image off screen if it tries to stick around
        Style style = image.getElement().getStyle();
        style.setPosition(Position.ABSOLUTE);
        style.setTop(-10000, Unit.PX);
        style.setLeft(-10000, Unit.PX);


    }

    private void drawColorScaleLables(double minValue, double maxValue, int width, int barHeight, int barStart) {
        int parts = getModel().getColorDivision();
        Context2d ctx = titleCanvas.getContext2d();
        ctx.save();
        ctx.beginPath();

        ctx.setFillStyle(GRAY); //$NON-NLS-1$
        ctx.setStrokeStyle(GRAY); //$NON-NLS-1$
        ctx.setFont("8pt Helvetica"); //$NON-NLS-1$
        ctx.setTextAlign(TextAlign.RIGHT);
        ctx.setTextBaseline(TextBaseline.MIDDLE);

        int x = width - 5;
        //We do a check here to see if the max and min are close
        //We ignore parts if the values are too close,
        //and we don't draw both values if they are the same.
        //Keep in mind decimal values are valid.
        if (maxValue - minValue <= .001) {
            minValue = Math.round(minValue * 100) / 100.0;
            ctx.moveTo(x, barStart);
            ctx.lineTo(x, barStart);
            ctx.fillText(minValue + "", width - 30, barStart); //$NON-NLS-1$

            maxValue = Math.round(maxValue * 100) / 100.0;
            if (minValue != maxValue && maxValue != Double.MAX_VALUE) {
                ctx.moveTo(x, barStart + barHeight);
                ctx.lineTo(x, barStart + barHeight);

                    ctx.fillText(NumberFormat.getScientificFormat().format(maxValue), width - 30, barStart + barHeight); //$NON-NLS-1$
            }

        } else {

            double d = (maxValue - minValue) / (double) parts;
            double dy = barHeight / (double) parts;
            for (int i = 0; i <= parts; i++) {
                double v = minValue + i * d;
                v = Math.round(v * 100) / 100.0;
                double y = barStart + barHeight - dy * i;
                ctx.moveTo(x, y);
                ctx.lineTo(width - 25, y);
                ctx.fillText(v + "", width - 30, y); //$NON-NLS-1$
            }
            ctx.stroke();
            ctx.restore();
        }
    }

    /**
     * this will not show the callout when all the values are the same ( only 1 as value)
     */
    public void renderColorScaleHoverMarker() {
        MatrixMetrics metrics = getModel().getMetrics();

        if(metrics == null){
            return;
        }

        double minValue = metrics .getMinValue();
        double maxValue = metrics.getMaxValue();

        titleCanvas2.setCoordinateSpaceWidth(titleCanvas2.getOffsetWidth());
        titleCanvas2.setCoordinateSpaceHeight(titleCanvas2.getOffsetHeight());
        final int height = titleCanvas2.getOffsetHeight() - 55;
        final int width = titleCanvas2.getOffsetWidth();
        final int barHeight = calculateBarHeight(height);
        final int barStart = (height - barHeight) / 2;
        int x = width - 5;

        Context2d ctx = titleCanvas2.getContext2d();
        ctx.clearRect(0,0,titleCanvas2.getOffsetWidth(),titleCanvas2.getOffsetHeight());
        if(hoverCell != null) {
            ctx.save();
            double v = hoverCell.getValue().doubleValue();
            v = Math.round(v * 100) / 100.0;
            double v1 = maxValue - minValue;
            if(v1!=0) {
                double y = (barHeight)-((v - minValue) * (barHeight)/ v1)+barStart;
                ctx.save();
                ctx.setFillStyle(CssColor.make("black"));
                ctx.setFont(11 + "pt Arial Narrow");
                String label = v+"";
                double textLength = ctx.measureText(label).getWidth();
                drawRoundedRectangle(ctx, width-textLength-11 -30, y - 7, 9+ 8, textLength + 11, 11/ 4);
                ctx.setLineWidth(.5);
                ctx.fillText(label, width-textLength-6 -30, y + 6 );
                ctx.stroke();
                ctx.restore();

                ctx.save();
                ctx.moveTo(x, y);
                ctx.lineTo(width - 30, y);
                ctx.stroke();
                ctx.restore();

            }else{
                double y = barStart;
                ctx.setFillStyle(CssColor.make("black"));
                ctx.setFont(11 + "pt Arial Narrow");
                String label = v+"";
                double textLength = ctx.measureText(label).getWidth();

                drawRoundedRectangle(ctx, width-textLength-11-30, y - 7 , 9 + 8, textLength + 11, 11/ 4);
                ctx.setLineWidth(.5);
                ctx.fillText(label, width-textLength-6 -30, y + 6 );
                ctx.stroke();
                ctx.restore();

                ctx.save();
//                ctx.setStrokeStyle(CssColor.make("red"));
                ctx.moveTo(x, y);
                ctx.lineTo(width - 30, y);
                ctx.stroke();
                ctx.restore();
            }
            ctx.restore();
        }
    }

    // what in the world.
    private int calculateBarHeight(int height) {
        int barStart = 11; //Just a recommended start for now
        int value = 0;
        while (value < 100) {
            value = height - barStart * 2;
            if (barStart > 10) {
                barStart = barStart / 2;
            } else {
                //There is zero room for this bad boy
//                value = 0;
                break;
            }
        }
        return value;
    }

    public ImagingRequest getImagingRequest() {
        //fixme:
//         return surface.getImagingRequest(titleCanvas.toDataUrl("image/png"));
        Boolean enableSecurity = WebMain.injector.getMainPresenter().getUserInfo().getDoCapco();

        ImagingRequest request = new ImagingRequest();
        request.setWidth(getOffsetWidth()); // add the extra space needed for the overview and the stats.
        request.setHeight(getOffsetHeight());

        String text = SecurityBanner.getBannerText();

        if(text != null && enableSecurity){
            request.setHeight(getOffsetHeight() + 50);
            Canvas tempCanvas = Canvas.createIfSupported();
            tempCanvas.setWidth(String.valueOf(request.getWidth()));
            tempCanvas.setCoordinateSpaceWidth(request.getWidth());
            tempCanvas.setHeight(25 + "px");
            tempCanvas.setCoordinateSpaceHeight(25);

            Context2d ctx = tempCanvas.getContext2d();
            ValuePair<String, String> colors = SecurityBanner.getColors(text, text);
            ctx.setFillStyle(colors.getValue2());
            ctx.fillRect(0,0, request.getWidth(), 25);
            ctx.setFillStyle(colors.getValue1());
            ctx.setFont("normal 400 13px Arial");
            ctx.setTextAlign(TextAlign.CENTER);
            while(ctx.measureText(text).getWidth() >= request.getWidth()) {
                createNewFont(ctx, ctx.getFont(), text, request.getWidth());
            }
            ctx.fillText(text, request.getWidth()/2, 17 );
            String dataUrl = tempCanvas.toDataUrl("image/png");

            PNGImageComponent topBanner = new PNGImageComponent();
            topBanner.setData(dataUrl);
            topBanner.setX(0);
            topBanner.setY(0);
            PNGImageComponent bottomBanner = new PNGImageComponent();
            bottomBanner.setData(dataUrl);
            bottomBanner.setY(getOffsetHeight() + 25);
            bottomBanner.setX(0);
            request.addComponent(topBanner);
            request.addComponent(bottomBanner);
        }

        PNGImageComponent imageComponent = surface.getImageComponent();
        PNGImageComponent legend = new PNGImageComponent();
        if(enableSecurity) {
            imageComponent.setY(25);
            legend.setY(25);
        }else {
            legend.setY(0);
        }
        request.addComponent(imageComponent);
        legend.setData(titleCanvas.toDataUrl());
        legend.setX(0);

        request.addComponent(legend);

        return request;
    }

    private void createNewFont(Context2d ctx, String font, String text, int width) {
        String[] fontValues = font.split(" ");
        String fontSize = fontValues[2];
        String fontSizeValues[] = fontSize.split("p");
        int fontSizeInt = Integer.parseInt(fontSizeValues[0]);
        String newFont = fontValues[0] + " " + fontValues[1] + " " + fontSizeInt + "px"
                + " " + fontSizeValues[3];
        ctx.setFont(newFont);
        if(ctx.measureText(text).getWidth() >= width){
            createNewFont(ctx, ctx.getFont(), text, width);
        }
    }

    public void onRenderSizeChange(RenderSize renderSize) {
        // When the layout contextMenu is resized, the surface's size changes. However, when the surface's size is changed
        // using the layoutPanel.setWidgetXY() style method, it doesn't trigger a resize on the surface. As an
        // example, when transitioning from COMPACT to NORMAL, the surface is not told that its width is now reduced
        // due to the introduction of the control button container. Therefore we make this explicit call to resize
        // the surface. The call needs to be made deferred so that it happens after the DOM changes have been effected.
        Scheduler.get().scheduleDeferred(() -> surface.onResize());

    }

    public void broadcastNotify(String text) {
        BroadcastAlert broadcastAlert = new BroadcastAlert(text);
        layoutPanel.add(broadcastAlert);
    }
    public void clear() {
        //fixme:
//        surface.clear();
    }
    
    public String getVizTitle() {
        return vizTitle;
    }

    public void setVizTitle(String vizTitle) {
        this.vizTitle = vizTitle;
    }
}
