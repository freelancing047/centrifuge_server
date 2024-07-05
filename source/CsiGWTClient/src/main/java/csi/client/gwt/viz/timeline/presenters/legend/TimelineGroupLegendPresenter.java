package csi.client.gwt.viz.timeline.presenters.legend;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.matrix.UpdateEvent;
import csi.client.gwt.viz.shared.menu.MenuKey;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;
import csi.client.gwt.viz.timeline.view.TimelineGroupingView;
import csi.client.gwt.viz.timeline.view.metrics.TimelineMetricsView;

public class TimelineGroupLegendPresenter{
    
    private ContentPanel contentPanel;
    private TimelinePresenter presenter;
    private TimelineGroupingView view;
    private CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public TimelineGroupLegendPresenter(TimelinePresenter presenter) {
        this.presenter = presenter;

        this.contentPanel = new ContentPanel();
        contentPanel.setCollapsible(true);
        contentPanel.setStyleName("legend");//NON-NLS
        contentPanel.setBodyBorder(false);
        contentPanel.setBodyStyle("background:none;");//NON-NLS
        // attempt to remove the background bellow is not honored
        contentPanel.setBodyStyleName("legend-body");//NON-NLS
        contentPanel.getHeader().addStyleName("legend-header");//NON-NLS
        contentPanel.getHeader().setBorders(false);
        contentPanel.setHeading(i18n.timelineGroupingLegendTitle());
        // contentPanel.getHeader().setStyleName("background:rba(180,180,180,.4)");
        contentPanel.getElement().getStyle().setProperty("boxShadow", "rgba(0, 0, 0, 0.2) 0px 5px 10px 0px");//NON-NLS
        contentPanel.getElement().getStyle().setProperty("MozBoxSizing", "border-box");//NON-NLS

        XElement element = (XElement) contentPanel.getHeader().getElement().getLastChild();

        //create a bunch of elements and
        XElement space = XElement.createElement("SPAN");
        space.setInnerHTML("&nbsp;");
        element.appendChild(space);

        XElement all = XElement.createElement("A");
        all.setInnerHTML(i18n.timelineGroupingLegendAllButton());
        all.addClassName("btn-link");
        Event.sinkEvents(all, Event.ONCLICK);
        Event.setEventListener(all, event -> {
            if (Event.ONCLICK == event.getTypeInt()) {
                //TODO if we want to add the select/deselect all from the header it would be very easy.
                if (event.getShiftKey() && !event.getCtrlKey()) {
                    // deselect all
                } else if (!event.getShiftKey() && event.getCtrlKey()) {
                    //select all
                } else {
                    raiseAll();
                    Scheduler.get().scheduleDeferred(() -> TimelineMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(presenter.getUuid())));
                }
            }
        });
        element.appendChild(all);

        XElement divider = XElement.createElement("SPAN");
        divider.setInnerHTML("/");
        element.appendChild(divider);

        XElement none = XElement.createElement("A");
        //element.setInnerHTML("<B>Groups</B>");
        none.setInnerHTML(i18n.timelineGroupingLegendNoneButton());
        Event.sinkEvents(none, Event.ONCLICK);
        Event.setEventListener(none, event -> {
            if (Event.ONCLICK == event.getTypeInt()) {
                collapseAll();
                TimelineMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(presenter.getUuid()));
            }
        });
        element.appendChild(none);

//        SafeHtmlBuilder sb = new SafeHtmlBuilder();
//        this.appearance.render(sb);
//    
//        setElement((Element) XDOM.create(sb.toSafeHtml()));
        ToolButton closeButton = new ToolButton(ToolButton.CLOSE);
        closeButton.setTitle(CentrifugeConstantsLocator.get().graphLegend_closeTooltip());
        closeButton.addDomHandler(closeHandler, ClickEvent.getType());
        contentPanel.addTool(closeButton);
        Scheduler.get().scheduleFixedDelay(() -> {
            try {
                if (contentPanel.getHeader().getToolCount() > 1) {
                    Widget collapseButton = contentPanel.getHeader().getTool(1);
                    collapseButton.setTitle(i18n.timeline_hide());
                    return false;
                } else {
                    return true;
                }
            } catch (Exception e) {
                return true;
            }
        }, 1000);
        contentPanel.addHideHandler(event -> {
            getPresenter().getMenuManager().hide(MenuKey.HIDE_GROUPS);
            getPresenter().getMenuManager().enable(MenuKey.SHOW_GROUPS);
        });
    }


    public boolean isUserClosed = false;

    private ClickHandler closeHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            isUserClosed = true;
            contentPanel.setVisible(false);
        }
    };

    public void addGroup(IsWidget widget){
        view.addItem(widget);
    }
    
    public TimelineGroupingView create(){
        if(view == null)
            view = new TimelineGroupingView(this);

        contentPanel.setWidget(view);

        ContentPanel contentPanel = getDisplay();
        contentPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
        contentPanel.getElement().getStyle().setVisibility(Visibility.HIDDEN);
        return view;
        
    }

    public void show(boolean force) {
        if(isUserClosed){
            force = false;
        }

        if((view != null && view.hasItems()) && !isUserClosed || force){
            contentPanel.show();
            presenter.getMenuManager().enable(MenuKey.HIDE_GROUPS);
            presenter.getMenuManager().hide(MenuKey.SHOW_GROUPS);
        } else {
            contentPanel.setVisible(false);
            presenter.getMenuManager().enable(MenuKey.SHOW_GROUPS);
            presenter.getMenuManager().hide(MenuKey.HIDE_GROUPS);
        }
    }
    
    public void raiseAll(){
        presenter.makeGroupsVisible(true);
    }
    
    public void collapseAll(){
        presenter.makeGroupsVisible(false);
    }

    public void hide() {
        contentPanel.setVisible(false);
        contentPanel.hide();
        presenter.getMenuManager().hide(MenuKey.HIDE_GROUPS);
        presenter.getMenuManager().enable(MenuKey.SHOW_GROUPS);
    }

    public void reset() {
        show(true);
        ContentPanel legendAsWindow = presenter.getLegendPresenter().getDisplay();
        Widget parent = legendAsWindow.getParent();
        //((AbsolutePanel) parent).setWidgetPosition(legendAsWindow, parent.getElement().getOffsetWidth() - 64- 25-legendAsWindow.getOffsetWidth(), 25);
        legendAsWindow.getElement().getStyle().setRight(25, Unit.PX);
        legendAsWindow.getElement().getStyle().setTop(35, Unit.PX);
    }
    
    public void clear() {
        if(view != null)
            view.clear();
    }
    
    public ContentPanel getDisplay() {
        return contentPanel;
    }

    public boolean toggleTrack(String text) {
        return presenter.toggleTrack(text);
    }
    
    public boolean isTrackVisibile(String text){
        return presenter.isTrackVisible(text);
    }

    public TimelinePresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(TimelinePresenter presenter) {
        this.presenter = presenter;
    }

   

}
