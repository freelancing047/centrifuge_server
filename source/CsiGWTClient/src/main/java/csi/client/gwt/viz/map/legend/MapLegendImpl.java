package csi.client.gwt.viz.map.legend;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.fx.client.DragEndEvent;
import com.sencha.gxt.fx.client.DragEndEvent.DragEndHandler;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.CenterLayoutContainer;
import csi.client.gwt.viz.graph.window.legend.LegendSearchBox;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.shared.export.LegendExporter;
import csi.client.gwt.viz.shared.search.Searchable;
import csi.server.business.visualization.legend.AssociationLegendItem;
import csi.server.business.visualization.legend.PlaceLegendItem;
import csi.server.business.visualization.legend.TrackLegendItem;
import csi.shared.core.visualization.map.MapConstants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MapLegendImpl implements MapLegend, DragEndHandler, MouseEventResponder, Searchable {
    private ContentPanel display;
    private View legend;
    private MapPresenter mapPresenter;
    private MapLegendProxy legendProxy;
    private boolean mouseSensitive = true;
    private CombinedPlaceLegendItemProxy combinedPlaceLegend;
    private int legendPositionLeft;
    private int legendPositionTop;
    private boolean legendPositionAnchored = true;
    private Integer sequenceNumber = 0;
    private List<PlaceLegendItem> placeLegendItems;
    private List<PlaceLegendItem> matchedLegendItems;
    private List<TrackLegendItem> trackLegendItems;
    private List<TrackLegendItem> matchedTrackLegendItems;
    private HandlerRegistration hr = null;
    private List<Element> itemsToHighlight;
    private int searchCounter = 0;
    private LegendSearchBox legendSearchBox;
    private com.github.gwtbootstrap.client.ui.base.InlineLabel closeButton;

    private MapLegendImpl(MapPresenter mapPresenter) {
        this.mapPresenter = mapPresenter;
        setupDisplay();
        setupActivityMapper();
        setupLegend();
    }

    public static MapLegendImpl create(MapPresenter mapPresenter) {
        return new MapLegendImpl(mapPresenter);
    }

    public static void setupLegendSearchBox(int headerWidth, LegendSearchBox legendSearchBox) {
        legendSearchBox.getTextBox().getElement().getParentElement().getStyle().setFloat(Style.Float.LEFT);
        legendSearchBox.getTextBox().getElement().getParentElement().getStyle().setWidth(headerWidth - 14, Style.Unit.PX);
        legendSearchBox.getTextBox().setWidth(headerWidth - 59 + "px");
    }

    public static void setupHeaderBar(int headerWidth, InlineLabel closeButton, ContentPanel display) {
        closeButton.getElement().getParentElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
        closeButton.getElement().getParentElement().getStyle().setWidth(10.0, Style.Unit.PX);
        Element headerBar = display.getHeader().getElement().getFirstChildElement().getNextSiblingElement();
        headerBar.getStyle().setFloat(Style.Float.LEFT);
        Element table = headerBar.getFirstChildElement();
        table.getStyle().setWidth(headerWidth - 5, Style.Unit.PX);
    }

    public static List<Element> extractRowDiv(NodeList<Element> legendItems) {
        List<Element> rowDivList = new ArrayList<>();
        for (int x = 0; x < legendItems.getLength(); x++) {
            if (legendItems.getItem(x).hasClassName("textDiv")) {
                rowDivList.add(legendItems.getItem(x));
            }
        }
        return rowDivList;
    }

    private static native void scrollIntoViewTop(Element element) /*-{
        element.scrollIntoView(true);
    }-*/;

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    private void setupDisplay() {
        display = new ContentPanel();
        display.setHideCollapseTool(true);

        legendSearchBox = new LegendSearchBox(MapLegendImpl.this);
        legendSearchBox.addRightButtonClickHandler(event -> findNext());
        legendSearchBox.addLeftButtonClickHandler(event -> findPrevious());

        legendSearchBox.getTextBox().addFocusHandler(event -> hr = RootPanel.get().addBitlessDomHandler(event1 -> {
            TextBox textBox = new TextBox();
            RootPanel.get().add(textBox);
            textBox.getElement().focus();
            textBox.removeFromParent();
        }, MouseDownEvent.getType()));

        legendSearchBox.getTextBox().addBlurHandler(event -> {
            if (hr != null) {
                hr.removeHandler();
                hr = null;
            }
        });

        display.addTool(legendSearchBox);

        closeButton = new com.github.gwtbootstrap.client.ui.base.InlineLabel();
        closeButton.addStyleName("legend-header-closeButton");
        closeButton.setText(" X ");
        closeButton.addDomHandler(event -> {
            mapPresenter.setLegendShown(false);
            mapPresenter.hideLegend();
        }, ClickEvent.getType());

        display.addTool(closeButton);

        display.setStyleName("legend");

        display.setBodyBorder(false);
        display.setBodyStyleName("legend-body");
        display.setBodyStyle("background:none;");// NON-NLS

        display.getHeader().addStyleName("legend-header");
        display.getHeader().setBorders(false);

        display.getElement().getStyle().setProperty("boxShadow", "rgba(0, 0, 0, 0.2) 0px 5px 10px 0px");
        display.getElement().getStyle().setProperty("MozBoxSizing", "border-box");

        display.addResizeHandler(event -> Scheduler.get().scheduleFixedDelay(() -> {
            int headerWidth = display.getHeader().getElement().getWidth(true);
            if (headerWidth > 59) {
                setupLegendSearchBox(headerWidth, legendSearchBox);
                setupHeaderBar(headerWidth, closeButton, display);
            }
            return false;
        }, 1000));

        display.setEnabled(true);
        display.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
    }

    private void setupActivityMapper() {
        ActivityMapper activityMapper = new MapLegendActivityMapper(this);
        EventBus eventBus = new SimpleEventBus();
        ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setDisplay(display);
    }

    private void setupLegend() {
        legend = new MapLegendViewImpl(this, mapPresenter.isReadOnly());
        legend.setScrollParent(display.getBody());
        display.setWidget(legend);
    }

    @Override
    public MapLegend getLegend() {
        return this;
    }

    @Override
    public void load() {
        if (legendProxy != null) {
            legendProxy.removeLoadHandler();
        }
        showLoading();
        legendProxy = new MapLegendProxy(mapPresenter.getDataViewUuid(), mapPresenter.getUuid(), sequenceNumber);
        legendProxy.addLoadHandler(event -> {
            try {
                legend.clear();
                if (mapPresenter.isUseTrackMap()) {
                    trackLegendItems = legendProxy.getTrackLegendItems();
                    if (trackLegendItems != null) {
                        for (TrackLegendItem trackLegendItem : trackLegendItems) {
                            legend.addLegendItem(new TrackLegendItemProxy(trackLegendItem), true);
                        }
                    }
                    addPlaceLegends(trackLegendItems == null || trackLegendItems.isEmpty());
                    addCombinedPlaceLegend();
                } else {
                    legend.showLinkLimitReachedMessage(legendProxy.isLinkLimitReached());
                    addPlaceLegends(true);
                    addCombinedPlaceLegend();
                    addAssociationLegends();
                    addNewPlaceLegend();
                    addUpdatedPlaceLegend();
                }
            } catch (Exception e) {
                // CEN-1933 generic catch for errors in this method
            }
            // mapPresenter.hideProgressIndicator();
            // mapPresenter.showData();
            setViewData();
        });
    }

    public void showLoading(){
        if (display.getWidgetCount() == 1) {
            display.remove(0);
        }

        CenterLayoutContainer cont = new CenterLayoutContainer();
        Icon i = new Icon(IconType.SPINNER);
        i.setSpin(true);

        i.setIconSize(IconSize.FOUR_TIMES);
        cont.add(i);
        display.setWidget(cont);
        display.forceLayout();
    }

    public void setViewData() {
        if(display != null) {
            if (display.getWidgetCount() == 1) {
                display.remove(0);
            }

            display.setWidget(legend);
            display.forceLayout();
        }
    }

    private void addPlaceLegends(boolean draggable) {
        placeLegendItems = legendProxy.getPlaceLegendItems();
        if (placeLegendItems != null) {
            placeLegendItems.sort(Comparator.comparing(PlaceLegendItem::getKey));
            for (PlaceLegendItem placeLegendItem : placeLegendItems) {
                if (placeLegendItem.typeName != null) {
                    if (placeLegendItem.typeName.equals(MapConstants.NULL_TYPE_NAME)) {
                        legend.addLegendItem(new NullPlaceLegendItemProxy(placeLegendItem, mapPresenter), draggable);
                    } else if (placeLegendItem.typeName.equals(MapConstants.EMPTY_TYPE_NAME)) {
                        legend.addLegendItem(new EmptyPlaceLegendItemProxy(placeLegendItem, mapPresenter), draggable);
                    } else {
                        legend.addLegendItem(new PlaceLegendItemProxy(placeLegendItem, mapPresenter, legendProxy.isPlaceTypenameUnique(placeLegendItem.typeName)), draggable);
                    }
                }
            }
        }
    }

    private void addCombinedPlaceLegend() {
        if ((legendProxy.getCombinedPlaceLegendItem() != null)) {
            combinedPlaceLegend = new CombinedPlaceLegendItemProxy(legendProxy.getCombinedPlaceLegendItem());
            legend.addLegendItem(combinedPlaceLegend, false);
        }
    }

    private void addAssociationLegends() {
        List<AssociationLegendItem> associationLegendItems = legendProxy.getAssociationLegendItems();
        if (associationLegendItems != null) {
            for (AssociationLegendItem associationLegendItem : associationLegendItems) {
                legend.addLegendItem(new AssociationLegendItemProxy(associationLegendItem), true);
            }
        }
    }

    private void addNewPlaceLegend() {
        if ((legendProxy.getNewPlaceLegendItem() != null)) {
            NewPlaceLegendItemProxy newPlaceLegend = new NewPlaceLegendItemProxy(legendProxy.getNewPlaceLegendItem());
            legend.addLegendItem(newPlaceLegend, false);
        }
    }

    private void addUpdatedPlaceLegend() {
        if ((legendProxy.getUpdatedPlaceLegendItem() != null)) {
            UpdatedPlaceLegendItemProxy updatedPlaceLegend = new UpdatedPlaceLegendItemProxy(legendProxy.getUpdatedPlaceLegendItem());
            legend.addLegendItem(updatedPlaceLegend, false);
        }
    }

    @Override
    public void hide() {
        if (display.isVisible()) {
            display.setVisible(false);
        }
    }

    @Override
    public void show() {
        display.setVisible(true);

        Scheduler.get().scheduleFixedDelay(() -> {
            int headerWidth = display.getHeader().getElement().getWidth(true);
            setupLegendSearchBox(headerWidth, legendSearchBox);
            legendSearchBox.getTextBox().getElement().getStyle().clearPadding();
            legendSearchBox.getTextBox().getElement().getStyle().clearMargin();
            setupHeaderBar(headerWidth, closeButton, display);
            return false;
        }, 250);
    }

    private void findPrevious() {
        if ((matchedLegendItems == null || matchedLegendItems.size() == 0) && (matchedTrackLegendItems == null || matchedTrackLegendItems.size() == 0)) {
            return;
        }

        if (searchCounter > 1) {
            scrollIntoViewTop(itemsToHighlight.get(searchCounter - 1));
            highlightText(itemsToHighlight, "#FCFC3D");
            highlightText(itemsToHighlight.get(searchCounter - 1), "#C6DEFF");
            searchCounter--;
        } else if (searchCounter == 1) {
            scrollIntoViewTop(itemsToHighlight.get(0));
            highlightText(itemsToHighlight, "#FCFC3D");
            highlightText(itemsToHighlight.get(0), "#C6DEFF");
            searchCounter--;
        } else {
            scrollIntoViewTop(itemsToHighlight.get(itemsToHighlight.size() - 1));
            highlightText(itemsToHighlight, "#FCFC3D");
            highlightText(itemsToHighlight.get(itemsToHighlight.size() - 1), "#C6DEFF");
            searchCounter = itemsToHighlight.size() - 1;
        }
    }

    private void findNext() {
        if ((matchedLegendItems == null || matchedLegendItems.size() == 0) && (matchedTrackLegendItems == null || matchedTrackLegendItems.size() == 0)) {
            return;
        }

        if (searchCounter == 0 && itemsToHighlight.size() > 1) {
            scrollIntoViewTop(itemsToHighlight.get(1));
            highlightText(itemsToHighlight, "#FCFC3D");
            highlightText(itemsToHighlight.get(1), "#C6DEFF");
            searchCounter++;
        } else if (searchCounter < (matchedLegendItems.size() + matchedTrackLegendItems.size() - 1)) {
            scrollIntoViewTop(itemsToHighlight.get(searchCounter + 1));
            highlightText(itemsToHighlight, "#FCFC3D");
            highlightText(itemsToHighlight.get(searchCounter + 1), "#C6DEFF");
            searchCounter++;
        } else {
            scrollIntoViewTop(itemsToHighlight.get(0));
            highlightText(itemsToHighlight, "#FCFC3D");
            highlightText(itemsToHighlight.get(0), "#C6DEFF");
            searchCounter = 0;
        }
    }

    private void removeHighlight(List<Element> elements) {
        for (Element element : elements) {
            element.getStyle().clearBackgroundColor();
        }
    }

    private void highlightText(List<Element> elements, String color) {
        for (Element element : elements) {
            element.getStyle().setProperty("backgroundColor", color);
        }
    }

    private void highlightText(Element element, String color) {
        element.getStyle().setProperty("backgroundColor", color);
    }

    @Override
    public void searchText(String text) {
        NodeList<Element> legendItems = display.getElement().getElementsByTagName("div");
        List<Element> deselectList = extractRowDiv(legendItems);

        if (!text.isEmpty() && !text.equals(" ")) {
            matchedLegendItems = new ArrayList<>();
            matchedTrackLegendItems = new ArrayList<>();
            itemsToHighlight = new ArrayList<>();
            text = text.toUpperCase();

            if (placeLegendItems != null && placeLegendItems.size() != 0) {
                for (PlaceLegendItem placeLegendItem : placeLegendItems) {
                    if (placeLegendItem.getKey().toUpperCase().contains(text)) {
                        matchedLegendItems.add(placeLegendItem);
                    }
                }
            }

            if (trackLegendItems != null && trackLegendItems.size() != 0) {
                for (TrackLegendItem trackLegendItem : trackLegendItems) {
                    if (trackLegendItem.getKey().toUpperCase().contains(text)) {
                        matchedTrackLegendItems.add(trackLegendItem);
                    }
                }
            }

            if (matchedLegendItems.size() > 0 || matchedTrackLegendItems.size() > 0) {
                removeHighlight(deselectList);
                for (TrackLegendItem matchedTrackLegendItem : matchedTrackLegendItems) {
                    for (Element divElement : deselectList) {
//                        if (matchedTrackLegendItem.getKey().contains(divElement.getInnerHTML())) {
                        if (divElement.getInnerHTML().contains(matchedTrackLegendItem.getKey())) {
                            itemsToHighlight.add(divElement);
                        }
                    }
                }
                for (PlaceLegendItem matchedLegendItem : matchedLegendItems) {
                    for (Element divElement : deselectList) {
//                        if (matchedLegendItem.getKey().equals(divElement.getInnerHTML())) {
                        if (divElement.getInnerHTML().contains(matchedLegendItem.getKey())) {
                            itemsToHighlight.add(divElement);
                        }
                    }
                }
                scrollIntoViewTop(itemsToHighlight.get(0));
                highlightText(itemsToHighlight, "#FCFC3D");
                highlightText(itemsToHighlight.get(0), "#C6DEFF");
            } else {
                removeHighlight(deselectList);
            }
        } else {
            if (matchedLegendItems != null) {
                matchedLegendItems.clear();
            } else if (matchedTrackLegendItems != null) {
                matchedTrackLegendItems.clear();
            }
            scrollIntoViewTop(deselectList.get(0));
            removeHighlight(deselectList);
        }
    }

    @Override
    public void showFind() {

    }

    @Override
    public void hideFind() {

    }

    @Override
    public void sensitize() {
        if (!mouseSensitive) {
            com.google.gwt.dom.client.Style style = display.getElement().getStyle();
            style.setProperty("pointerEvents", "all");
            mouseSensitive = true;
        }
    }

    @Override
    public void desensitize() {
        if (mouseSensitive) {
            com.google.gwt.dom.client.Style style = display.getElement().getStyle();
            style.setProperty("pointerEvents", "none");
            mouseSensitive = false;
        }
    }

    @Override
    public boolean isVisible() {
        return display.isVisible();
    }

    @Override
    public ContentPanel getLegendAsWindow() {
        return display;
    }

    @Override
    public MapPresenter getMapPresenter() {
        return mapPresenter;
    }

    @Override
    public Widget asWidget() {
        return legend.asWidget();
    }

    @Override
    public void setLegendPositionAnchored(boolean value) {
        legendPositionAnchored = value;
    }

    @Override
    public int[] getLegendPosition() {
        int left = display.getElement().getLeft();
        int top = display.getElement().getTop();
        int right = display.getElement().getRight(true);
        int bottom = display.getElement().getAbsoluteBottom();

        int[] legendPosition = new int[]{left, top, right, bottom};
        StringBuilder sb = new StringBuilder();
        sb.append(left);
        sb.append(", ");
        sb.append(top);
        sb.append("," );
        sb.append(right);
        sb.append(", ");
        sb.append(bottom);

        return legendPosition;
    }

    @Override
    public void showAndPositionLegend() {
        show();
        positionLegend();
    }

    @Override
    public void positionLegend() {
        Widget parent = display.getParent();
        if (legendPositionAnchored) {
            revertToAnchoredPosition(display, parent);
        }
        ((AbsolutePanel) parent).setWidgetPosition(display, legendPositionLeft, legendPositionTop);
    }

    private void revertToAnchoredPosition(ContentPanel legendAsWindow, Widget parent) {
        legendPositionLeft = parent.getElement().getOffsetWidth() - 64 - 25 - legendAsWindow.getOffsetWidth();
        legendPositionTop = 25;
    }

    @Override
    public void onDragEnd(DragEndEvent event) {
        determineIfLegendPositionAnchored();
    }

    private void determineIfLegendPositionAnchored() {
        Widget parent = display.getParent();
        int left = display.getAbsoluteLeft() - parent.getAbsoluteLeft();
        int top = display.getAbsoluteTop() - parent.getAbsoluteTop();
        if (left != legendPositionLeft || top != legendPositionTop) {
            legendPositionLeft = left;
            legendPositionTop = top;
            legendPositionAnchored = false;
        }
    }

    public Map<String, String> getVisItems() {
        return LegendExporter.getVisItems(legend.getFluidContainer());
    }

    @Override
    public void updateCombinedPlaceIconStatus(boolean isVisible) {
        combinedPlaceLegend.updateCombinedPlaceIconStatus(isVisible);
    }

    @Override
    public void clear() {
        legend.clear();
    }
}
