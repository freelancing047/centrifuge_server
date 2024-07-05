package csi.client.gwt.viz.graph.window.legend;

import com.google.common.collect.Sets;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sencha.gxt.widget.core.client.ContentPanel;
import csi.client.gwt.WebMain;
import csi.client.gwt.events.CsiEvent;
import csi.client.gwt.events.CsiEventHandler;
import csi.client.gwt.util.BasicPlace;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.export.LegendExporter;
import csi.client.gwt.viz.shared.search.Searchable;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.legend.GraphLinkLegendItem;
import csi.server.business.visualization.legend.GraphNodeLegendItem;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.graph.GraphCachedState;
import csi.server.common.service.api.GraphActionServiceProtocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class GraphLegendImpl implements GraphLegend, Searchable {

    private EventBus eventBus;
    private PlaceController placeController;
    private ActivityMapper activityMapper;
    private ActivityManager activityManager;
    private ContentPanel display;
    private Graph graph;
    private View legend;
    private LegendSearchBox legendSearchBox;
    private com.github.gwtbootstrap.client.ui.base.InlineLabel closeButton;
    private LegendProxy legendProxy;
    private List<GraphNodeLegendItem> nodeLegendItems;
    private List<GraphLinkLegendItem> linkLegendItems;
    private List<GraphNodeLegendItem> matchedLegendItems;
    private List<GraphLinkLegendItem> matchedLinkLegendItems;
    private HandlerRegistration hr = null;
    private List<Element> itemsToHighlight;
    private int searchCounter = 0;
    boolean hasMultiType = false;

    //private Map<Integer, String> itemOrderMap = new HashMap<Integer,String>();
    private List<String> itemOrderList = new ArrayList<String>();
    private ClickHandler closeHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            display.setVisible(false);
        }
    };
    private CsiEventHandler legendLoadHandler = new LegendLoadHandler();

    @Override
    public Graph getGraph() {
        return graph;
    }

    public static GraphLegendImpl create(Graph graph, GraphCachedState graphCachedState) {
        return new GraphLegendImpl(graph, graphCachedState);
    }

    private GraphLegendImpl(Graph graph, GraphCachedState graphCachedState) {
        this.graph = checkNotNull(graph);
        if(graphCachedState == null || graphCachedState.getLegendOrder() == null){
            if(itemOrderList != null)
                itemOrderList.clear();
        } else if(graphCachedState.getLegendOrder() != null){
            itemOrderList = graphCachedState.getLegendOrder();
        }
        eventBus = new SimpleEventBus();
        placeController = new PlaceController(eventBus);
        activityMapper = new GraphLegendActivityMapper(this);
        activityManager = new ActivityManager(activityMapper, eventBus);
        placeController.goTo(BasicPlace.DEFAULT_PLACE);
        display = new ContentPanel();
        display.setHideCollapseTool(true);

        legendSearchBox = new LegendSearchBox(GraphLegendImpl.this);
        legendSearchBox.addRightButtonClickHandler(rightSearchHandler);
        legendSearchBox.addLeftButtonClickHandler(leftSearchHandler);


        display.addTool(legendSearchBox);


        closeButton = new com.github.gwtbootstrap.client.ui.base.InlineLabel();
        closeButton.addStyleName("legend-header-closeButton");
        closeButton.setText(" X ");
        closeButton.addDomHandler(closeHandler, ClickEvent.getType());
        display.addTool(closeButton);

        activityManager.setDisplay(display);
        legend = new GraphLegendViewImpl(this, graph.getVisualizationDef().isReadOnly());
        display.setWidget(legend);
        legend.setParent(display.getBody());
        display.setStyleName("legend");//NON-NLS
        display.setBodyBorder(false);
        display.setBodyStyle("background:none;");//NON-NLS
        display.setBodyStyleName("legend-body");//NON-NLS
        display.getHeader().addStyleName("legend-header");//NON-NLS
        display.getHeader().setBorders(false);
        display.getElement().getStyle().setProperty("boxShadow", "rgba(0, 0, 0, 0.2) 0px 5px 10px 0px");//NON-NLS
        display.getElement().getStyle().setProperty("MozBoxSizing", "border-box");//NON-NLS


        legendSearchBox.getTextBox().addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                hr = RootPanel.get().addBitlessDomHandler(new MouseDownHandler() {
                    @Override
                    public void onMouseDown(MouseDownEvent event) {
                        TextBox textBox = new TextBox();
                        RootPanel.get().add(textBox);
                        textBox.getElement().focus();
                        textBox.removeFromParent();
                    }

                }, MouseDownEvent.getType());
            }
        });
        legendSearchBox.getTextBox().addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                if (hr != null) {
                    hr.removeHandler();
                    hr = null;
                }
            }
        });



        display.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                Scheduler.get().scheduleFixedDelay(() -> {
                    int headerWidth = display.getHeader().getElement().getWidth(true);
                    legendSearchBox.getTextBox().getElement().getParentElement().getStyle().setFloat(Style.Float.LEFT);
                    legendSearchBox.getTextBox().getElement().getParentElement().getStyle().setWidth(headerWidth - 14, Style.Unit.PX);
                    legendSearchBox.getTextBox().setWidth(headerWidth - 59 + "px");
                    closeButton.getElement().getParentElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
                    closeButton.getElement().getParentElement().getStyle().setWidth(10.0, Style.Unit.PX);
                    Element headerBar = display.getHeader().getElement().getFirstChildElement().getNextSiblingElement();
                    headerBar.getStyle().setFloat(Style.Float.LEFT);
                    Element table = headerBar.getFirstChildElement();
                    table.getStyle().setWidth(headerWidth - 5, Style.Unit.PX);

                    return false;
                }, 1000);
            }
        });
    }

    @Override
    public void load() {
        legendProxy = new LegendProxy(graph.getUuid());
        legendProxy.addLoadHandler(legendLoadHandler);
    }

    public ActivityManager getActivityManager() {
        return activityManager;
    }


    private int rightOffset;

    @Override
    public void setRightOffset(int i) {
        rightOffset = i;
    }

    @Override
    public GraphLegend getLegend() {
        return this;
    }

    @Override
    public ContentPanel getLegendAsWindow() {
        return display;
    }

    public LegendProxy getLegendProxy() {
        return legendProxy;
    }

    @Override
    public Widget asWidget() {
        return legend.asWidget();
    }

    @Override
    public void hide() {
        display.setVisible(false);
    }

    @Override
    public void show() {
        display.setVisible(true);
    }

    @Override
    public boolean isHidden() {
        return display.isVisible();
    }

    @Override
    public int getRightOffset() {
        return rightOffset;
    }

    // vis items on the legend
    public Map<String, String> getVisItems(){
        return LegendExporter.getVisItems(legend.getFluidContainer());
    }
    @Override
    public void addCommonItem() {
        InCommonLegendItem item = new InCommonLegendItem();
        List<LegendItemProxy> proxies = null;
        if(itemOrderList.contains(item.getKey())) {
            proxies = legend.getNodeLegendItems(Sets.newHashSet(InCommonLegendItem.getStaticKey()));
        } else {
            itemOrderList.add(item.getKey());
        }
        if(proxies == null || proxies.size() == 0) {
            legend.addLegendItem(item);
        }
    }
    @Override
    public void addNewlyAdded() {
        NewlyAddedLegendItem item = new NewlyAddedLegendItem();

        List<LegendItemProxy> proxies = null;
        if(itemOrderList.contains(item.getKey())) {
            proxies = legend.getNodeLegendItems(Sets.newHashSet(NewlyAddedLegendItem.getStaticKey()));
        } else {
            itemOrderList.add(item.getKey());
        }

        if(proxies == null || proxies.size() == 0) {
            legend.addLegendItem(item);
        }
    }

    public void findPrevious() {

        if ((matchedLegendItems == null || matchedLegendItems.size() == 0) && (matchedLinkLegendItems == null || matchedLinkLegendItems.size() == 0)) {
            return;
        }

        if (searchCounter > 1) {
            scrollIntoViewTop(itemsToHighlight.get(searchCounter-1));
            highlightText(itemsToHighlight, "#FCFC3D");
            highlightText(itemsToHighlight.get(searchCounter-1), "#C6DEFF");
            searchCounter--;
        } else if (searchCounter == 1 ) {
            scrollIntoViewTop(itemsToHighlight.get(0));
            highlightText(itemsToHighlight, "#FCFC3D");
            highlightText(itemsToHighlight.get(0), "#C6DEFF");
            searchCounter--;
        } else {
            scrollIntoViewTop(itemsToHighlight.get(itemsToHighlight.size()-1));
            highlightText(itemsToHighlight, "#FCFC3D");
            highlightText(itemsToHighlight.get(itemsToHighlight.size()-1), "#C6DEFF");
            searchCounter = itemsToHighlight.size() - 1;
        }
    }

    public void findNext() {
        if ((matchedLegendItems == null || matchedLegendItems.size() == 0) && (matchedLinkLegendItems == null || matchedLinkLegendItems.size() == 0)) {
            return;
        }

        if (searchCounter == 0 && itemsToHighlight.size() > 1) {
            scrollIntoViewTop(itemsToHighlight.get(1));
            highlightText(itemsToHighlight, "#FCFC3D");
            highlightText(itemsToHighlight.get(1), "#C6DEFF");
            searchCounter++;
        } else if (searchCounter < (matchedLegendItems.size() + matchedLinkLegendItems.size() - 1)) {
            scrollIntoViewTop(itemsToHighlight.get(searchCounter + 1));
            highlightText(itemsToHighlight, "#FCFC3D");
            highlightText(itemsToHighlight.get(searchCounter+1), "#C6DEFF");
            searchCounter++;
        } else {
            scrollIntoViewTop(itemsToHighlight.get(0));
            highlightText(itemsToHighlight, "#FCFC3D");
            highlightText(itemsToHighlight.get(0), "#C6DEFF");
            searchCounter = 0;
        }
    }

    private ClickHandler leftSearchHandler = new ClickHandler(){

        @Override
        public void onClick(ClickEvent event) {
            findPrevious();
        }};

    private ClickHandler rightSearchHandler = new ClickHandler(){

        @Override
        public void onClick(ClickEvent event) {
            findNext();
        }};

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

        if(!text.isEmpty() && text != " ") {
            matchedLegendItems = new ArrayList<GraphNodeLegendItem>();
            matchedLinkLegendItems = new ArrayList<GraphLinkLegendItem>();
            itemsToHighlight = new ArrayList<Element>();
            text = text.toUpperCase();

            for (int i = 0; i < nodeLegendItems.size(); i++) {
                if (nodeLegendItems.get(i).getKey().toUpperCase().contains(text)) {
                    matchedLegendItems.add(nodeLegendItems.get(i));
                }
            }

            if (linkLegendItems != null && linkLegendItems.size() != 0) {
                for (int j =0; j< linkLegendItems.size(); j++) {
                    if (linkLegendItems.get(j).getKey().toUpperCase().contains(text)) {
                        matchedLinkLegendItems.add(linkLegendItems.get(j));
                    }
                }
            }


            if (matchedLegendItems.size() > 0 || matchedLinkLegendItems.size() > 0) {
                removeHighlight(deselectList);
                for (GraphNodeLegendItem matchedLegendItem : matchedLegendItems) {
                    for (Element divElement : deselectList) {
                        if (matchedLegendItem.getKey() == divElement.getInnerHTML()) {
                            itemsToHighlight.add(divElement);
                        }
                    }
                }
                for (GraphLinkLegendItem matchedLinkLegendItem : matchedLinkLegendItems) {
                    for (Element divElement : deselectList) {
                        if (matchedLinkLegendItem.getKey() == divElement.getInnerHTML()) {
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
        }
        else {
            if (matchedLegendItems != null) {
                matchedLegendItems.clear();
            }
            if (matchedLinkLegendItems != null) {
                matchedLinkLegendItems.clear();
            }
            scrollIntoViewTop(deselectList.get(0));
            removeHighlight(deselectList);
        }
    }


    private List<Element> extractRowDiv(NodeList<Element> legendItems) {
        List<Element> rowDivList = new ArrayList<Element>();
        for (int x = 0; x < legendItems.getLength(); x++) {
            if (legendItems.getItem(x).hasClassName("textDiv")) {
                rowDivList.add(legendItems.getItem(x));
            }
        }
        return rowDivList;
    }

    @Override
    public void showFind() {

    }

    @Override
    public void hideFind() {

    }


    private class LegendLoadHandler implements CsiEventHandler {


        @Override
        public void onCsiEvent(CsiEvent event) {
            try {
                legend.clear();
                getGraph().clearStyles();
                if(graph != null && graph.getModel() != null){
                    GraphCachedState state = graph.getModel().getRelGraphViewDef().getState();
                    if(state == null){
                        itemOrderList.clear();
                    }
                }
                nodeLegendItems = legendProxy.getNodeLegendItems();
                if (nodeLegendItems != null) {
                    for (GraphNodeLegendItem graphNodeLegendItem : nodeLegendItems) {
                        if (graphNodeLegendItem.typeName != null) {
                            switch (graphNodeLegendItem.typeName) {
                                case "In Common": //NON-NLS
                                    legend.addLegendItem(new InCommonLegendItem());
                                    if(!itemOrderList.contains(graphNodeLegendItem.key))
                                        itemOrderList.add(graphNodeLegendItem.key);
                                    break;
                                case "Newly Added":  //NON-NLS
                                    legend.addLegendItem(new NewlyAddedLegendItem());
                                    if(!itemOrderList.contains(graphNodeLegendItem.key))
                                        itemOrderList.add(graphNodeLegendItem.key);
                                    break;
                                case "Bundle":
                                    if(graphNodeLegendItem.key.equals("csi.internal.Bundle")){
                                        legend.addLegendItem(new BundleNodeLegendItemProxy(getGraph(), graphNodeLegendItem));
                                        if(!itemOrderList.contains(graphNodeLegendItem.key)){
                                            itemOrderList.add(graphNodeLegendItem.key);
                                        }
                                        break;
                                    }
                                default:
                                    if(!itemOrderList.contains(ObjectAttributes.NODES_OBJECT_TYPE + graphNodeLegendItem.typeName) && !graphNodeLegendItem.key.equals("csi.internal.Bundle")){
                                        itemOrderList.add(ObjectAttributes.NODES_OBJECT_TYPE + graphNodeLegendItem.typeName);
                                    }
                                    legend.addLegendItem(new NodeLegendItemProxy(getGraph(), graphNodeLegendItem));
                                    break;
                            }
                        }
                    }
                }
                hasMultiType = false;
                if ((legendProxy.getGraphLegendNodeSummary().multiTypedNodes != null) && (legendProxy.getGraphLegendNodeSummary().multiTypedNodes > 0)) {
                    legend.addLegendItem(new MultiTypeLegendItem());
                    hasMultiType = true;
                    if(!itemOrderList.contains(MultiTypeLegendItem.CSI_INTERNAL_MULTITYPE))
                        itemOrderList.add(MultiTypeLegendItem.CSI_INTERNAL_MULTITYPE);
                }
                linkLegendItems = legendProxy.getLinkLegendItems();
                if (linkLegendItems != null) {
                    for (GraphLinkLegendItem linkLegendItem : linkLegendItems) {
                        if (linkLegendItem.typeName != null) {
                            switch (linkLegendItem.typeName) {
                                case "In Common": //NON-NLS
                                    legend.addLegendItem(new InCommonLinkLegendItem());
                                    if (!itemOrderList.contains(linkLegendItem.key))
                                        itemOrderList.add(linkLegendItem.key);
                                    break;
                                case "Newly Added":  //NON-NLS
                                    legend.addLegendItem(new NewlyAddedLinkLegendItem());
                                    if (!itemOrderList.contains(linkLegendItem.key))
                                        itemOrderList.add(linkLegendItem.key);
                                    break;
                                default:

                                    legend.addLegendItem(new LinkLegendItemProxy(getGraph().getLegend(), linkLegendItem));


                                    if (!itemOrderList.contains(ObjectAttributes.EDGES_OBJECT_TYPE + linkLegendItem.key)) {
                                        itemOrderList.add(ObjectAttributes.EDGES_OBJECT_TYPE + linkLegendItem.key);
                                    }
                            }
                        }
                    }
                }
                if ((legendProxy.getGraphLegendNodeSummary().multiTypedLinks != null) && (legendProxy.getGraphLegendNodeSummary().multiTypedLinks > 0)) {
                    legend.addLegendItem(new MultiTypeLinkLegendItem());
                    hasMultiType = true;
                    if(!itemOrderList.contains(MultiTypeLinkLegendItem.CSI_INTERNAL_MULTITYPE))
                        itemOrderList.add(MultiTypeLinkLegendItem.CSI_INTERNAL_MULTITYPE);
                }
            }catch (Exception e){
                //CEN-1933 generic catch for errors in this method
            } finally {
                saveLegendCache();
                legend.updateOrder(itemOrderList);
            }
        }
    }

    private static native void scrollIntoViewTop(Element element) /*-{
        element.scrollIntoView(true);
    }-*/;

    public void saveLegendCache() {
        try {
            VortexFuture<GraphCachedState> future = WebMain.injector.getVortex().createFuture();

            future.execute(GraphActionServiceProtocol.class).saveLegendCache(legendProxy.getVizUuid(), itemOrderList, legendProxy.getNodeLegendItems(), legendProxy.getLinkLegendItems());
            future.addEventHandler(new AbstractVortexEventHandler<GraphCachedState>() {

                @Override
                public void onSuccess(GraphCachedState result) {
                    if(graph != null && graph.getModel() != null){
                        graph.getModel().getRelGraphViewDef().setState(result);
                    }
                    graph.getGraphSurface().refresh();
                }
            });
        } catch (CentrifugeException ignored) {

        }
    }

    public void updateLegendModel(int position, String key) {
        itemOrderList.remove(key);
        itemOrderList.add(position, key);
        saveLegendCache();
    }

    @Override
    public List<LegendItemProxy> getMatchingItems(Set<String> types, boolean isNode){
        if(isNode){
            return legend.getNodeLegendItems(types);
        } else {
            return legend.getLinkLegendItems(types);
        }
    }
    @Override
    public boolean hasMultiType() {
        return hasMultiType;
    }
}
