package csi.client.gwt.viz.shared;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.fx.client.DragEndEvent;
import com.sencha.gxt.fx.client.Draggable;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.container.CenterLayoutContainer;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.matrix.ExpireMetrics;
import csi.client.gwt.viz.matrix.UpdateEvent;
import csi.shared.core.visualization.chart.ChartMetrics;

import java.io.Serializable;
import java.util.List;

/**
 * Created by grinv on 4/24/2018.
 *
 * Few features here:
 *          - You can disable view metrcis toggle with viewMetricsEnabled
 *          - if you have all metrics, put them in globalMetrics - that way they wont ask server on refresh
 *          - BETA -- and also in mapmetrics not here.- histogram bars, the idea is that for type breakdown you will see a proportional amoutn of background colord
 *          -- TODO : can pull more stuff out from subclasses.
 *
 *
 *
 */
public abstract class AbstractMetricsView<M extends Serializable> extends Composite implements IsWidget, DragEndEvent.DragEndHandler {
    // this is the issue for the viz anger
    public static EventBus EVENT_BUS = GWT.create(SimpleEventBus.class);
    protected ContentPanel display;
    protected List<M> globalMetrics;
    private AbstractVisualizationPresenter presenter;
    protected boolean isView = false;
    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public boolean forceRefresh = false;

    // strings
    private String WINDOW_TOGGLE_ALL = i18n.visualization_metrics_toggle_all();
    private String WINDOW_TOGGLE_VIEW = i18n.visualization_metrics_toggle_view();

    protected ClickHandler closeHandler = event->display.setVisible(false);
    private boolean viewMetricsEnabled = true;

    protected boolean anchored = true;

    public AbstractMetricsView(AbstractVisualizationPresenter presenter){
        this.presenter = presenter;

        init();
    }

    private void init(){
        // add the event bus handlers for updating and invalidating settings
        EVENT_BUS.addHandler(UpdateEvent.TYPE, event -> {
            if(event.getVisUuid().equals(presenter.getUuid())) {
                forceRefresh = event.isForce();
                if(display.isVisible()) {
                    if (isView) {
                        loadViewMetrics();
                    } else {
                        loadFullMetrics();
                    }
                }
            }
        });
        EVENT_BUS.addHandler(ExpireMetrics.TYPE, event-> {
            if(event.getVisUuid().equals(presenter.getUuid())) {
                this.globalMetrics = null;
                forceRefresh = true;

                if (!isView) {
                    if (display.isVisible()) {
                        showLoading();
                        loadFullMetrics();
                    }
                }
            }
        });

        display = new ContentPanel();
        Scheduler.get().scheduleDeferred(()->{
            display.setHeading(getWindowTitle());
//            display.getElement().getStyle().setOpacity(.9);
            applyStyles();
            createToggle();
            addCloseButton();
            Draggable draggable = presenter.getChrome().addWindowAndReturnDraggable(display);
            DragEndEvent.DragEndHandler h = (DragEndEvent.DragEndHandler) this;
            draggable.addDragEndHandler(h);
        });
    }

    private void createToggle() {
        if(this.viewMetricsEnabled) {
            XElement element = (XElement) display.getHeader().getElement().getLastChild();
            XElement space = XElement.createElement("SPAN");
            XElement all = XElement.createElement("A");
            XElement divider = XElement.createElement("SPAN");
            XElement view = XElement.createElement("A");
            space.setInnerHTML("&nbsp;");
            element.appendChild(space);
            all.setInnerHTML(getWindowViewAll());
            Event.sinkEvents(all, Event.ONCLICK);
            all.setTitle(i18n.visualization_metrics_toggle_all_tooltip());

            Event.setEventListener(all, event -> {
                if (Event.ONCLICK == event.getTypeInt()) {
                    if (event.getCtrlKey()) {
                        forceRefresh = true;
                        globalMetrics = null;
                    }
                    isView = false;
                    all.addClassName("btn-link-selected");
                    view.removeClassName("btn-link-selected");
                    showLoading();
                    loadFullMetrics();
                }
            });

            view.addClassName("btn-link");
            all.addClassName("btn-link");

            if (isView) {
                view.addClassName("btn-link-selected");
                all.removeClassName("btn-link-selected");
            } else {
                all.addClassName("btn-link-selected");
                view.removeClassName("btn-link-selected");
            }

            divider.setInnerHTML(i18n.visualization_metrics_toggle_separator());
            view.setInnerHTML(getWindowView());
            Event.sinkEvents(view, Event.ONCLICK);
            Event.setEventListener(view, event -> {
                if (Event.ONCLICK == event.getTypeInt()) {
                    forceRefresh = event.getCtrlKey();
                    isView = true;
                    view.addClassName("btn-link-selected");
                    all.removeClassName("btn-link-selected");
                    loadViewMetrics();
                }
            });
            element.appendChild(view);
            element.appendChild(divider);
            element.appendChild(all);
        }
    }

    private void applyStyles() {
        display.setCollapsible(true);
        display.addStyleName("legend");
        display.getHeader().setBorders(false);
        display.getHeader().addStyleName("lengend-header");
        display.getElement().getStyle().setProperty("MozBoxSizing", "border-box");
        display.setBodyBorder(false);
    }

    private void addCloseButton() {
        ToolButton closeButton = new ToolButton(ToolButton.CLOSE);
        closeButton.addDomHandler(closeHandler, ClickEvent.getType());
        display.addTool(closeButton);
        display.setAllowTextSelection(false);
        display.setVisible(true);

        display.addResizeHandler(event -> {
            if (event.getWidth() < 190) {
                display.setHeading(getWindowTitleShort());
            } else {
                display.setHeading(getWindowTitle());
            }

            createToggle();
        });
    }


    public void show() {
        display.setVisible(true);
        display.setSize("220px", "200px");
        EVENT_BUS.fireEvent(new UpdateEvent(presenter.getUuid()));
        Widget parent = display.getParent();
        ((AbsolutePanel) parent).setWidgetPosition(display, parent.getElement().getOffsetWidth() - 64- 25-display.getOffsetWidth(), 25);
    }

    /**
     * will remove the widgets and load a spinner to the view.
     */
    public void showLoading(){
        if (display.getWidgetCount() == 1) {
            display.remove(0);
        }

        CenterLayoutContainer cont = new CenterLayoutContainer();
        Icon i = new Icon(IconType.SPINNER);
        i.setSpin(true);

        i.setIconSize(IconSize.FOUR_TIMES);
        cont.add(i);
        display.add(cont);
        display.forceLayout();
    }

    /**
     * clears the panel, add the arg panel to view
     * @param panel
     */
    public void setViewData(ScrollPanel panel) {
        display.getBody().getStyle().setOpacity(.9);
        panel.getElement().getStyle().setOpacity(.9);
        if(display != null) {
            if (display.getWidgetCount() == 1) {
                display.remove(0);
            }

            display.add(panel);
            display.forceLayout();
        }
    }

    private String getWindowViewAll(){
        return WINDOW_TOGGLE_ALL;
    }

    private String getWindowView(){
        return WINDOW_TOGGLE_VIEW;
    }

    protected TextColumn<MetricsDisplay> getNameValueProvider() {
        return new TextColumn<MetricsDisplay>() {
            @Override
            public String getValue(MetricsDisplay object) {
                if(object.getValue() == "-1"){
                    return object.getName().toUpperCase();
                }else {
                    return object.getName();
                }
            }
        };
    }

    protected TextColumn<MetricsDisplay> getValueProvider() {
        return new TextColumn<MetricsDisplay>() {
            @Override
            public String getValue(MetricsDisplay object) {
                if (object.getValue() == "-1") {
                    return "";
                }
                return object.getValue() + "";
            }
        };
    }

    public boolean isViewMetricsEnabled() {
        return viewMetricsEnabled;
    }

    public void setViewMetricsEnabled(boolean viewMetricsEnabled) {
        this.viewMetricsEnabled = viewMetricsEnabled;
    }

    abstract protected String getWindowTitleShort();

    abstract protected String getWindowTitle();

    abstract protected ScrollPanel buildView(List<M> metrics);

    abstract protected void loadViewMetrics();

    abstract protected void loadFullMetrics();

    public AbstractVisualizationPresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(AbstractVisualizationPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onDragEnd(DragEndEvent dragEndEvent) {
        anchored = false;
    }

    public void revertToAnchoredPosition(){
        anchored = true;
        Widget parent = display.getParent();
        int offsetWidth = display.getElement().getWidth(true);

        ((AbsolutePanel) parent).setWidgetPosition(display, parent.getElement().getOffsetWidth() - 290 , 25);
    }

    public void positionLegend() {
        if (anchored) {
            revertToAnchoredPosition();
        }
    }

    public boolean isForceRefresh() {
        return forceRefresh;
    }

    public void setForceRefresh(boolean forceRefresh) {
        this.forceRefresh = forceRefresh;
    }
}


