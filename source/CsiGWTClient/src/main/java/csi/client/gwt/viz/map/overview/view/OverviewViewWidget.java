package csi.client.gwt.viz.map.overview.view;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.map.overview.DragState;
import csi.client.gwt.viz.map.overview.OverviewPresenter;
import csi.client.gwt.viz.map.overview.view.content.CanvasOverview;
import csi.client.gwt.viz.map.overview.view.content.OverviewContent;
import csi.client.gwt.viz.map.overview.view.content.OverviewContentContainer;

import java.util.List;

/**
 * Renders an arbitrary widget within a container with zoom and pan controls.
 *
 * @author Centrifuge Systems, Inc.
 */
public class OverviewViewWidget extends Composite implements OverviewView {
    private final FlowPanel parentPanel = new FlowPanel();
    private final FlowPanel sliderDivPanel = new FlowPanel();
    private final FluidContainer fc = new FluidContainer();
    private final DragBar startBar;
    private final DragBar stopBar;
    private final TransparencyWidget transparencyWidget;
    private final OverviewContentContainer overviewContentContainer;
    private OverviewContent overviewContent;
    private HandlerRegistration singleClickHandler;
    private Cursor cursor = Cursor.POINTER;
    private Button addLeft;
    private Button moveRight;
    private Button addRight;
    private Button moveLeft;
    private OverviewPresenter presenter;

    public OverviewViewWidget(OverviewPresenter overviewPresenter) {
        this.presenter = overviewPresenter;
        parentPanel.setHeight(OVERVIEW_HEIGHT + "px");
        parentPanel.getElement().getStyle().setPosition(Style.Position.RELATIVE);
        parentPanel.getElement().getStyle().setProperty("margin", "auto");

        overviewContentContainer = new OverviewContentContainer();
        transparencyWidget = new TransparencyWidget();
        startBar = new DragBar();
        stopBar = new DragBar();

        buildUI();
        initWidget(fc);
        addHandlers();
    }

    public Widget getEventReferenceWidget() {
        return parentPanel;
    }

    @Override
    public void setOverviewContent(OverviewContent overviewContent) {
        if (overviewContent == null) {
            overviewContent = new CanvasOverview();
        }
        this.overviewContent = overviewContent;
        overviewContentContainer.clear();
        overviewContentContainer.add(overviewContent);
    }

    @Override
    public void setCategoryData(List<Integer> categoryData) {
        if (overviewContent != null) {
            overviewContent.setCategoryData(categoryData);
        }
    }

    @Override
    public Style getWidgetStyle() {
        return parentPanel.getElement().getStyle();
    }

    @Override
    public void render(DragState dragState, String s, String s1) {
        int width = getWidth();
        if (width <= 0) {
            width = 150;
        }
        String totalWidth = (width + (2 * DRAG_BAR_WIDTH)) + "px";
        parentPanel.setWidth(totalWidth);
        startBar.setPosition(presenter.getStartPosition());
        stopBar.setPosition(presenter.getEndPosition() + DRAG_BAR_WIDTH);
        transparencyWidget.setPosition(presenter.getStartPosition() + DRAG_BAR_WIDTH, presenter.getEndPosition() - presenter.getStartPosition());

        sliderDivPanel.setWidth(width + "px");
        overviewContentContainer.setWidth(width);
        if (overviewContent != null) {
            overviewContent.resize(width);
        }
        switch (dragState) {
            case NOT_DRAGGING:
                removeHighlights();
                break;
            case BOTH_BARS:
                highlightCenterBar();
                highlightStartBar();
                highlightEndBar();
                break;
            case START_BAR:
                highlightStartBar();
                break;
            case END_BAR:
                highlightEndBar();
                break;
        }
        if (s != null) {
            startBar.setTitle(s);
        }
        if (s1 != null) {
            stopBar.setTitle(s1);
        }
    }

    public void removeHighlights() {

        startBar.doHighlight(false);
        stopBar.doHighlight(false);
        transparencyWidget.doHighlight(false);
    }

    public void highlightEndBar() {
        stopBar.doHighlight(true);
    }

    public void highlightCenterBar() {
        transparencyWidget.doHighlight(true);
    }

    public void highlightStartBar() {
        startBar.doHighlight(true);
    }

    @Override
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {

        return parentPanel.addDomHandler(handler, MouseDownEvent.getType());
    }

    @Override
    public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
        return parentPanel.addDomHandler(handler, MouseMoveEvent.getType());
    }

    @Override
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
        return parentPanel.addDomHandler(handler, MouseOutEvent.getType());
    }

    @Override
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
        return parentPanel.addDomHandler(handler, MouseOverEvent.getType());
    }

    @Override
    public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
        return parentPanel.addDomHandler(handler, MouseUpEvent.getType());
    }

    @Override
    public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
        return parentPanel.addDomHandler(handler, MouseWheelEvent.getType());
    }

    @Override
    public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {
        return parentPanel.addDomHandler(handler, DoubleClickEvent.getType());
    }

    private void buildUI() {
        sliderDivPanel.add(overviewContentContainer);
        {
            addLeft = new Button();
            addLeft.setTitle(CentrifugeConstantsLocator.get().addLeft());
            addLeft.setIcon(IconType.PLUS);
            addLeft.setSize(ButtonSize.SMALL);
            addLeft.setType(ButtonType.LINK);
            addLeft.getElement().getStyle().setLeft(20, Style.Unit.PX);
            addLeft.getElement().getStyle().setTop(-4, Style.Unit.PX);
            addLeft.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
            addLeft.getElement().addClassName("no-underline-link");
            fc.add(addLeft);
        }
        {
            moveLeft = new Button();
            moveLeft.setTitle(CentrifugeConstantsLocator.get().moveLeft());
            moveLeft.setSize(ButtonSize.SMALL);
            moveLeft.setType(ButtonType.LINK);
            moveLeft.setIcon(IconType.CHEVRON_LEFT);
            moveLeft.getElement().getStyle().setLeft(0, Style.Unit.PX);
            moveLeft.getElement().getStyle().setTop(-4, Style.Unit.PX);
            moveLeft.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
            moveLeft.getElement().addClassName("no-underline-link");
            fc.add(moveLeft);
        }
        parentPanel.add(sliderDivPanel);
        parentPanel.add(startBar);
        parentPanel.add(stopBar);
        parentPanel.add(transparencyWidget);
        {
            addRight = new Button();
            addRight.setTitle(CentrifugeConstantsLocator.get().addRight());
            addRight.setSize(ButtonSize.SMALL);
            addRight.setType(ButtonType.LINK);
            addRight.setIcon(IconType.PLUS);
            addRight.getElement().getStyle().setRight(20, Style.Unit.PX);
            addRight.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
            addRight.getElement().getStyle().setTop(-4, Style.Unit.PX);
            addRight.getElement().addClassName("no-underline-link");
            fc.add(addRight);
        }
        {
            moveRight = new Button();
            moveRight.setTitle(CentrifugeConstantsLocator.get().moveRight());
            moveRight.setSize(ButtonSize.SMALL);
            moveRight.setType(ButtonType.LINK);
            moveRight.setIcon(IconType.CHEVRON_RIGHT);
            moveRight.getElement().getStyle().setRight(0, Style.Unit.PX);
            moveRight.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
            moveRight.getElement().getStyle().setTop(-4, Style.Unit.PX);
            moveRight.getElement().addClassName("no-underline-link");
            fc.add(moveRight);
        }

        //TODO:add this to styles
        /*
.no-underline-link:hover,.no-underline-link:focus,.no-underline-link:visited{
    text-decoration: none !important;
}
        * */
        sliderDivPanel.getElement().getStyle().setMarginLeft(DRAG_BAR_WIDTH, Style.Unit.PX);
        sliderDivPanel.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        fc.add(parentPanel);
    }

    private void addHandlers() {
//        addMouseDownHandler(new MouseDownHandler() {
//            @Override
//            public void onMouseDown(MouseDownEvent event) {
//                parentPanel.getElement().getStyle().setCursor(Style.Cursor.W_RESIZE);
//            }
//        });
        addMouseUpHandler(event -> parentPanel.getElement().getStyle().setCursor(Cursor.AUTO));

        addMouseMoveHandler(event -> parentPanel.getElement().getStyle().setCursor(cursor));
    }

    public boolean removeMouseClickHandler() {
        if (singleClickHandler != null) {
            singleClickHandler.removeHandler();
            return true;
        }
        return false;
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        removeMouseClickHandler();
        singleClickHandler = parentPanel.addDomHandler(handler, ClickEvent.getType());
        return singleClickHandler;
    }

    @Override
    public void setCursor(Cursor eResize) {
        this.cursor = eResize;
    }

    @Override
    public Button getAddLeft() {
        return addLeft;
    }

    @Override
    public Button getMoveRight() {
        return moveRight;
    }

    @Override
    public Button getAddRight() {
        return addRight;
    }

    @Override
    public Button getMoveLeft() {
        return moveLeft;
    }

    @Override
    public int getWidth() {
        //FIXME:Magic Number
        int width = 150;
        if (isAttached() && getParent().isAttached()) {
            try {
                width = getParent().getElement().getOffsetWidth();
            } catch (Exception ignored) {
            }
        }
        width -= (2 * DRAG_BAR_WIDTH);
        //FIXME:Magic Number
        width -= 80;
        return width;
    }

    public void setWidth(int width) {
        if (isAttached() && getParent().isAttached()) {
            try {
                int overviewContentWidth = width + (2 * DRAG_BAR_WIDTH);
                asWidget().setWidth(overviewContentWidth + "px");
                int parentWidth = overviewContentWidth + 80;
                getParent().setWidth(parentWidth + "px");
            } catch (Exception ignored) {
            }
        }
    }
}
