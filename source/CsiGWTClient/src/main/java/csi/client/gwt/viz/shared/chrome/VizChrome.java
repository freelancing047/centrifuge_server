package csi.client.gwt.viz.shared.chrome;

import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.fx.client.Draggable;
import com.sencha.gxt.widget.core.client.ContentPanel;

import csi.client.gwt.dataview.broadcast.BroadcastManager;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.VizMessageArea;
import csi.client.gwt.viz.shared.menu.CsiMenuNav;
import csi.client.gwt.widget.boot.AbstractCsiTab;

public interface VizChrome extends IsWidget, AcceptsOneWidget {

    public EventBus getEventBus();

    public int getButtonContainerHeight();

    /**
     * 
     * @param widget to be added to button bar.
     * @return true if added, else false
     */
    // TODO: If allowing any widget, why call this addButton?
    public void addButton(IsWidget widget);

    public void addTab(Tab tab);

    public void addFullScreenWindow(String message, IconType iconType);

    public void removeFullScreenWindow();

    public void showLoadingSpinner();

    public void hideLoadingSpinner();

    public void addWindow(ContentPanel panel);
    
    public Draggable addWindowAndReturnDraggable(ContentPanel panel);

    public CsiMenuNav getMenu();

    public VizMessageArea getMessageArea();

    public Visualization getVisualization();

    public void removeVisualization();

    public void selectTab(AbstractCsiTab tab);

    public void setVisualization(Visualization visualization);

    void removeTab(AbstractCsiTab tab);

    void removeWindow(ContentPanel panel);

    void removeButton(Widget widget);

    public void setTabDrawerVisible(boolean visible);

    public void setName(String name);

    public void enableBroadcastListener();

    public void disableBroadcastListener();

    SimpleLayoutPanel getControlBar();

    void bringFloatingTabDrawerToFront();

    void removeFloatingTab();

    public void onResize();
    
    void hideButtonGroupContainer();
    
    void showButtonGroupContainer();

    void hideControlLayer();

    com.google.gwt.user.client.ui.SplitLayoutPanel getControlsLayer();

    com.google.gwt.user.client.ui.LayoutPanel getMainLP();

    public void addSearchBox(Panel flowPanel);

    void setControlLayerOpacity(double d);

    void sendTo(BroadcastManager broadcastManager, Visualization senderViz);

    void removeSendTo();

    public void adjustWidgets();
}
