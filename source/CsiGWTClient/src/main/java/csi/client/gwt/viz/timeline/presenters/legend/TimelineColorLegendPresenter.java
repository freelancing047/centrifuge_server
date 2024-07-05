package csi.client.gwt.viz.timeline.presenters.legend;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.sencha.gxt.widget.core.client.ContentPanel;
import csi.client.gwt.WebMain;
import csi.client.gwt.viz.graph.window.legend.LegendSearchBox;
import csi.client.gwt.viz.map.legend.MapLegendImpl;
import csi.client.gwt.viz.shared.menu.MenuKey;
import csi.client.gwt.viz.shared.search.Searchable;
import csi.client.gwt.viz.timeline.NullLegendItem;
import csi.client.gwt.viz.timeline.model.LegendItem;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;
import csi.client.gwt.viz.timeline.view.TimelineLegendView;
import csi.shared.core.visualization.timeline.TimelineTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimelineColorLegendPresenter implements Searchable {

    private TimelineLegendView legend;
    private ContentPanel contentPanel;
    private TimelinePresenter presenter;
    private List<String> itemOrderList = new ArrayList<>();
    private LegendSearchBox legendSearchBox;
    private com.github.gwtbootstrap.client.ui.base.InlineLabel closeButton;
    private List<LegendItem> timelineLegendItems;
    private List<LegendItem> matchedLegendItems;
    private HandlerRegistration hr = null;
    private List<Element> itemsToHighlight;
    private int searchCounter = 0;

    private int limit;


    public TimelineColorLegendPresenter(TimelinePresenter presenter) {
        this.presenter = presenter;
        limit = WebMain.getClientStartupInfo().getTimelineTypeLimit();

        setupContentPanel();
    }

    private static native void scrollIntoViewTop(Element element) /*-{
        element.scrollIntoView(true);
    }-*/;

    public List<String> getVisItems() {
        return legend.getVisItems();
    }

    private void setupContentPanel() {
        this.contentPanel = new ContentPanel();
        contentPanel.setHideCollapseTool(true);

        legendSearchBox = new LegendSearchBox(TimelineColorLegendPresenter.this);
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

        contentPanel.addTool(legendSearchBox);

        closeButton = new com.github.gwtbootstrap.client.ui.base.InlineLabel();
        closeButton.addStyleName("legend-header-closeButton");
        closeButton.setText(" X ");
        closeButton.addDomHandler(event -> contentPanel.setVisible(false), ClickEvent.getType());

        contentPanel.addTool(closeButton);

        contentPanel.setStyleName("legend");//NON-NLS

        contentPanel.setBodyBorder(false);
        contentPanel.setBodyStyle("background:none;");//NON-NLS
        // attempt to remove the background bellow is not honored
        contentPanel.setBodyStyleName("legend-body");//NON-NLS

        contentPanel.getHeader().addStyleName("legend-header");//NON-NLS
        contentPanel.getHeader().setBorders(false);

        contentPanel.getElement().getStyle().setProperty("boxShadow", "rgba(0, 0, 0, 0.2) 0px 5px 10px 0px");//NON-NLS
        contentPanel.getElement().getStyle().setProperty("MozBoxSizing", "border-box");//NON-NLS

        contentPanel.addResizeHandler(event -> Scheduler.get().scheduleFixedDelay(() -> {
            int headerWidth = contentPanel.getHeader().getElement().getWidth(true);
            MapLegendImpl.setupLegendSearchBox(headerWidth, legendSearchBox);
            MapLegendImpl.setupHeaderBar(headerWidth, closeButton, contentPanel);
            return false;
        }, 1000));
    }

    public void createLegend(boolean readOnly) {
        if (legend == null)
            legend = new TimelineLegendView(this, readOnly);

        contentPanel.setWidget(legend);

        legend.setParent(contentPanel.getBody());
        ContentPanel contentPanel = getDisplay();
        contentPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
        contentPanel.getElement().getStyle().setVisibility(Visibility.HIDDEN);

    }

    public void addItem(IsWidget widget) {
        legend.addLegendItem(widget);
    }

    public ContentPanel getDisplay() {
        return contentPanel;
    }

    public void populateLegend(Map<String, Integer> colors) {
        timelineLegendItems = new ArrayList<>();
        List<String> items = TimelinePresenter.asSortedList(colors.keySet(), getPresenter().getSort());
        if (items.size() > limit) {
            items = items.subList(0, limit);
        }
        for (String key : items) {
            if (itemOrderList.contains(key)) {
                continue;
            }
            LegendItem item;
            if (key.equals(TimelineTrack.EMPTY_TRACK)) {
                item = new NullLegendItem();
                timelineLegendItems.add(item);
            } else {
                item = new LegendItem(colors.get(key), key);
                timelineLegendItems.add(item);
            }
            itemOrderList.add(key);
            addItem(item);
        }
    }

    public void clearLegend() {
        if (legend == null) {
            return;
        }

        itemOrderList = new ArrayList<>();
        legend.clear();
    }

    public void selectByText(String text, boolean removeExistingSelection) {
        presenter.selectByText(text, removeExistingSelection);
    }

    public void deselectByText(String text) {
        presenter.deselectByText(text);
    }

    public void show(boolean force) {
        if (legend == null) {
            return;
        }
        Scheduler.get().scheduleFixedDelay(() -> {
            if (legend.hasItems() || force) {
                contentPanel.show();
                presenter.getMenuManager().enable(MenuKey.HIDE_LEGEND);
                presenter.getMenuManager().hide(MenuKey.SHOW_LEGEND);
            } else {
                contentPanel.setVisible(false);
                presenter.getMenuManager().enable(MenuKey.SHOW_LEGEND);
                presenter.getMenuManager().hide(MenuKey.HIDE_LEGEND);
            }
            return false;
        }, 250);

    }

    public TimelinePresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(TimelinePresenter presenter) {
        this.presenter = presenter;
    }

    public boolean isVisible() {
        return contentPanel.isVisible();
    }

    public void hide() {
        contentPanel.setVisible(false);
        contentPanel.hide();
    }

    public void reset() {
        show(true);
        ContentPanel legendAsWindow = presenter.getLegendPresenter().getDisplay();
        //Widget parent = legendAsWindow.getParent();
        //((AbsolutePanel) parent).setWidgetPosition(legendAsWindow, parent.getElement().getOffsetWidth() - 64- 25-legendAsWindow.getOffsetWidth(), 25);
        legendAsWindow.getElement().getStyle().setRight(25, Unit.PX);
        legendAsWindow.getElement().getStyle().setTop(35, Unit.PX);
    }

    public void updateLegendModel(int position, String key) {
        itemOrderList.remove(key);
        itemOrderList.add(position, key);
        presenter.updateFromColorLegend(key, itemOrderList);
    }

    private void findPrevious() {

        if (matchedLegendItems == null || matchedLegendItems.size() == 0) {
            return;
        }

        if (searchCounter > 1) {
            scrollIntoViewTop(matchedLegendItems.get(searchCounter - 1).getElement());
            highlightText(itemsToHighlight, "#FCFC3D");
            highlightText(itemsToHighlight.get(searchCounter - 1), "#C6DEFF");
            searchCounter--;
        } else if (searchCounter == 1) {
            scrollIntoViewTop(matchedLegendItems.get(0).getElement());
            highlightText(itemsToHighlight, "#FCFC3D");
            highlightText(itemsToHighlight.get(0), "#C6DEFF");
            searchCounter--;
        } else {
            scrollIntoViewTop(matchedLegendItems.get(matchedLegendItems.size() - 1).getElement());
            highlightText(itemsToHighlight, "#FCFC3D");
            highlightText(itemsToHighlight.get(matchedLegendItems.size() - 1), "#C6DEFF");
            searchCounter = matchedLegendItems.size() - 1;
        }
    }

    private void findNext() {

        if (matchedLegendItems == null || matchedLegendItems.size() == 0) {
            return;
        }

        if (searchCounter < matchedLegendItems.size() - 1) {
            scrollIntoViewTop(matchedLegendItems.get(searchCounter + 1).getElement());
            highlightText(itemsToHighlight, "#FCFC3D");
            highlightText(itemsToHighlight.get(searchCounter + 1), "#C6DEFF");
            searchCounter++;
        } else {
            scrollIntoViewTop(matchedLegendItems.get(0).getElement());
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
        NodeList<Element> legendItems = contentPanel.getElement().getElementsByTagName("div");
        List<Element> deselectList = MapLegendImpl.extractRowDiv(legendItems);

        if (!text.isEmpty() && !text.equals(" ")) {
            matchedLegendItems = new ArrayList<>();
            itemsToHighlight = new ArrayList<>();
            text = text.toUpperCase();

            for (LegendItem timelineLegendItem : timelineLegendItems) {
                if (timelineLegendItem.getKey().toUpperCase().contains(text)) {
                    matchedLegendItems.add(timelineLegendItem);
                }
            }

            if (matchedLegendItems.size() > 0) {
                removeHighlight(deselectList);
                for (LegendItem matchedLegendItem : matchedLegendItems) {
                    for (Element divElement : deselectList) {
                        if (matchedLegendItem.getText().equals(divElement.getInnerHTML())) {
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
}
