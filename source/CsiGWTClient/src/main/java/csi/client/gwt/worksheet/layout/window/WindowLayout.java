/**
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.worksheet.layout.window;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Point;
import com.sencha.gxt.core.client.util.Size;
import com.sencha.gxt.widget.core.client.Window;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.shared.chrome.panel.VizPanel;
import csi.client.gwt.viz.viewer.Viewer;
import csi.client.gwt.viz.viewer.ViewerImpl;
import csi.client.gwt.viz.viewer.ViewerPanel;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.client.gwt.widget.ui.ResizeableAbsolutePanel;
import csi.client.gwt.widget.ui.SlidingAccessPanel;
import csi.client.gwt.widget.ui.SlidingAccessPanel.State;
import csi.client.gwt.worksheet.WorksheetPresenter;
import csi.client.gwt.worksheet.WorksheetView;
import csi.client.gwt.worksheet.layout.window.events.NewVisualizationEvent;
import csi.client.gwt.worksheet.layout.window.events.NewVisualizationEventHandler;
import csi.client.gwt.worksheet.layout.window.events.VisualizationBarSelectionEvent;
import csi.client.gwt.worksheet.layout.window.events.VisualizationBarSelectionEventHandler;
import csi.client.gwt.worksheet.layout.window.events.WindowCascadeEvent;
import csi.client.gwt.worksheet.layout.window.events.WindowCascadeEventHandler;
import csi.client.gwt.worksheet.layout.window.events.WindowStateChangeHandler;
import csi.client.gwt.worksheet.layout.window.events.WindowTileEvent;
import csi.client.gwt.worksheet.layout.window.events.WindowTileEventHandler;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.worksheet.VisualizationLayoutState;
import csi.server.common.model.worksheet.WorksheetScreenLayout;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class WindowLayout extends ResizeComposite implements WorksheetView {

    private static final int OFFSET_CASCADE = 32;

    private WorksheetPresenter worksheetPresenter;
    private Multimap<VisualizationType, VisualizationWindow> windowsByType = HashMultimap.create();
    private LayoutPanel mainContainer;
    private EventBus eventBus = new SimpleEventBus();
    private WorksheetScreenLayout worksheetScreenLayout;
    private boolean loaded;

    @UiField
    ResizeableAbsolutePanel panel;
    @UiField
    SlidingAccessPanel viewerSlidingAccessPanel;
    @UiField
    SlidingAccessPanel slidingAccessPanel;
    @UiField
    VisualizationBarIconContainer vizBarContainer;
    @UiField
    ResizeableAbsolutePanel viewerSimplePanel;
    @UiField
    FullSizeLayoutPanel fslp;
    private Viewer viewer;

    interface SpecificUiBinder extends UiBinder<LayoutPanel, WindowLayout> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    @Override
    protected void onAttach() {
        super.onAttach();
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                slidingAccessPanel.setPinned(true);
                slidingAccessPanel.setState(State.REVEALED);
/*                viewerSlidingAccessPanel.setPinned(true);
                viewerSlidingAccessPanel.setState(State.REVEALED);*/
            }
        });
    }

    public WindowLayout(WorksheetPresenter worksheet) {
        this.worksheetPresenter = worksheet;
        mainContainer = uiBinder.createAndBindUi(this);
        initWidget(mainContainer);
        vizBarContainer.setEventBus(eventBus);
        slidingAccessPanel.setPinControl(vizBarContainer.getPin(), fslp);
        //TODO: seems like button should be referenced
        viewerSlidingAccessPanel.setPinControl(new Button(), panel);
        setupVisualizationBar();
        addEventBusHandlers();

        getElement().setId("worksheet-id-" + worksheet.getName().replaceAll(" ", "_")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private void setupVisualizationBar() {
        int position = 0;
        for (VisualizationType vizType : VisualizationType.values()) {
        	if(vizType == VisualizationType.CHRONOS){
        		if(WebMain.getClientStartupInfo().getFeatureConfigGWT().isShowTimeline()){
	                VisualizationBarIcon widget = new VisualizationBarIcon(vizType, this, position);
	                vizBarContainer.add(widget);
	                slidingAccessPanel.autoHideDeferOnClick(widget);
                    position++;
        		}
            } else if (vizType == VisualizationType.GEOSPATIAL_V2) {
            	if(WebMain.getClientStartupInfo().getFeatureConfigGWT().isShowMap()){
            		VisualizationBarIcon widget = new VisualizationBarIcon(vizType, this, position);
	                vizBarContainer.add(widget);
	                slidingAccessPanel.autoHideDeferOnClick(widget);
                    position++;
            	}
            }else if (vizType.isH5()) {
                VisualizationBarIcon widget = new VisualizationBarIcon(vizType, this, position);
                vizBarContainer.add(widget);
                slidingAccessPanel.autoHideDeferOnClick(widget);
                position++;
            }
        }
    }

    @Override
    public void onResize() {
        super.onResize();
        // Defer load of worksheets until the tab is first displayed. This may have implications for broadcast.
        if (getOffsetWidth() > 0 && getOffsetHeight() > 0 && !loaded) {
            worksheetScreenLayout = worksheetPresenter.getWorksheet().getWorksheetScreenLayout();
            renderVisualizations();
            loaded = true;
        }
    }

    protected void addEventBusHandlers() {
        eventBus.addHandler(NewVisualizationEvent.type, new NewVisualizationEventHandler() {

            @Override
            public void onRequest(VisualizationType type) {
                worksheetPresenter.createNewVisualization(type);
            }
        });
        eventBus.addHandler(VisualizationBarSelectionEvent.type, new VisualizationBarSelectionEventHandler() {

            @Override
            public void onSelect(Visualization visualization) {
                VisualizationWindow window = getVisualizationWindow(visualization);
                if (window.isMinimized() && !window.isMaximized()) {
                    window.expandFromMinimize(true);
                } else if (window.isMaximized()) {
                    window.fitContainer();
                } else if (window.isFullScreen()) {
                    window.fullScreenFit();
                }
                window.ensureVisibleHeader();
                window.toFront();
                
            }
        });
        eventBus.addHandler(WindowCascadeEvent.type, new WindowCascadeEventHandler() {

            @Override
            public void onCascade() {
                cascade();
            }
        });
        eventBus.addHandler(WindowTileEvent.type, new WindowTileEventHandler() {

            @Override
            public void onTile() {
                tile();
            }
        });
    }

    protected VisualizationWindow getVisualizationWindow(Visualization visualization) {
        for (VisualizationWindow window : windowsByType.get(visualization.getType())) {
            if (window.getVisualizationPanel().getVisualization().equals(visualization)) {
                return window;
            }
        }
        return null;
    }

    protected void renderVisualizations() {

        VisualizationWindow activatedWindow = null;
        for (Visualization visualization : worksheetPresenter.getVisualizations()) {
            add(visualization);
        }

        for (VisualizationWindow window : windowsByType.values()) {
            if (window.getVisualizationPanel().getVisualization().getUuid()
                    .equals(worksheetScreenLayout.getActivatedVisualizationUuid())) {
                activatedWindow = window;
            }
        }
        if (activatedWindow != null) {
            activatedWindow.toFront();
        }
    }

    public List<Visualization> getVisualizations(VisualizationType type) {
        List<Visualization> list = new ArrayList<Visualization>();
        for (VisualizationWindow window : windowsByType.get(type)) {
            list.add(window.getVisualizationPanel().getVisualization());
        }
        Collections.sort(list, new Comparator<Visualization>() {

            @Override
            public int compare(Visualization o1, Visualization o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return list;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    private VisualizationWindow createWindow(final Visualization visualization, WorksheetScreenLayout layout, final Widget container) {
        final VisualizationWindow window = new VisualizationWindow(panel);
        VizPanel vizPanel = new VizPanel(worksheetPresenter);
        if(viewer==null){
//            ViewerModal viewerModal = new ViewerModal();
//            viewerModal.setCloseVisible(true);
//            viewerModal.setTitle("Viewer");
            ViewerPanel viewerModal = new ViewerPanel(viewerSlidingAccessPanel);
            viewerModal.setHeight("100%");
            viewerModal.setWidth("100%");

            viewer = new ViewerImpl(viewerModal);
            viewerSimplePanel.getElement().addClassName("viewer-panel");
            Style style = viewerSimplePanel.getElement().getStyle();
            style.setBackgroundColor("rgba(256,256,256)");
            style.setProperty("borderWidth", "0 0 0 1px");
            style.setBorderColor("#808080");
            style.setBorderStyle(Style.BorderStyle.SOLID);
            style.setHeight(100, Style.Unit.PCT);
            style.setWidth(100, Style.Unit.PCT);

            viewerSimplePanel.add(viewerModal);
            viewerModal.add(viewer);
        }
        vizPanel.setFrameProvider(window, visualization.getName());
        vizPanel.setVisualization(visualization);
        window.setVisualizationPanel(vizPanel);
        window.hide();
        window.setPosition(-1 * VisualizationWindow.OFF_SCREEN_PROTECTION, -1 * VisualizationWindow.OFF_SCREEN_PROTECTION);
        window.disable();
        final VisualizationLayoutState state = layout.getLayout().getLayoutState(visualization.getVisualizationDef());
        
        
        Scheduler.get().scheduleDeferred(new ScheduledCommand(){

            @Override
            public void execute() {
                protectPositioning(container.getOffsetWidth(), container.getOffsetHeight(), state);
                window.setPixelSize(state.getWidth(), state.getHeight());
                window.setPosition(state.getX(), state.getY());
                if (state.isMinimized()) {
                    window.setMinimized(true);
//                    window.setPosition(state.getMinimizedX(), state.getMinimizedY());
                    window.setPosition(-10000, -10000);
                    window.setMinimizeRestorePos(new Point(state.getX(), state.getY()));
                    window.setMinimizeRestoreSize(new Size(state.getWidth(), state.getHeight()));
                } else if (state.isMaximized()) {
                    window.maximize();
                }
                window.show();
                window.enable();
            }});
        

        return window;
    }

    private void protectPositioning(int maxX, int maxY, VisualizationLayoutState state) {
        
        if(state.getWidth() > maxX){
            state.setWidth(maxX);
        }

        if(state.getHeight() > maxY){
            state.setHeight(maxY);
        }
        
        if(maxX > 0 && state.getX() + state.getWidth() > maxX){
            state.setX(maxX - state.getWidth());
        }
        
        if(maxY > 0 && state.getY() + + state.getHeight() > maxY){
            state.setY(maxY - state.getHeight());
        }
        
        if(state.getX() < 0){
            state.setX(0);
        }
        
        if(state.getY() < 0){
            state.setY(0);
        }
        
        
        
        
    }

    private void cascade() {
        List<VisualizationWindow> list = getZOrderedWindows();
        int width = panel.getOffsetWidth() - (list.size() - 1) * OFFSET_CASCADE;
        int height = panel.getOffsetHeight() - (list.size() - 1) * OFFSET_CASCADE;

        int x = 0;
        int y = 0;
        for (int i = 0; i < list.size(); i++) {
            VisualizationWindow window = list.get(i);
            VisualizationLayoutState state = worksheetScreenLayout.getLayout().getLayoutState(
                    window.getVisualizationPanel().getVisualization().getVisualizationDef());
            window.restore(false);
            x = i * OFFSET_CASCADE;
            y = i * OFFSET_CASCADE;
            window.setPosition(x, y);
            window.setPixelSize(width, height);
            state.setX(x);
            state.setY(y);
            state.setWidth(width);
            state.setHeight(height);
            state.setMaximized(false);
        }
        WindowStateChangeHandler.saveState(worksheetScreenLayout);
    }
    
    @Override
    public void saveState(){

        WindowStateChangeHandler.saveState(worksheetScreenLayout);
    }

    @Override
    public Viewer getViewer() {
        return viewer;
    }

    private void tile() {
        List<VisualizationWindow> list = getZOrderedWindows();

        int sq = (int) Math.ceil(Math.sqrt(list.size()));
        int rem = list.size() % sq;
        int windowsPerRow = rem == 1 ? sq + 1 : sq;

        int rows = (int) Math.ceil(list.size() / (double) windowsPerRow);
        int elementsInLastRow = list.size() % windowsPerRow;
        int width = panel.getOffsetWidth() / windowsPerRow;
        int lastRowWidth = elementsInLastRow == 0 ? 0 : panel.getOffsetWidth() / elementsInLastRow;
        int height = panel.getOffsetHeight() / rows;
        int x = 0, y = 0, w = 0;
        int counter = 0;
        for (int i = list.size() - 1; i >= 0; i--) {
            if (i < elementsInLastRow) {
                x = (counter % windowsPerRow) * lastRowWidth;
                w = lastRowWidth;
            } else {
                x = (counter % windowsPerRow) * width;
                w = width;
            }
            y = counter / windowsPerRow * height;

            VisualizationWindow window = list.get(i);
            VisualizationLayoutState state = worksheetScreenLayout.getLayout().getLayoutState(
                    window.getVisualizationPanel().getVisualization().getVisualizationDef());
            window.restore(false);
            window.setPosition(x, y);
            window.setPixelSize(w, height);
            state.setX(x);
            state.setY(y);
            state.setWidth(w);
            state.setHeight(height);
            state.setMaximized(false);

            counter++;
        }
        WindowStateChangeHandler.saveState(worksheetScreenLayout);
    }

    /**
     * @return List of non-minimized windows that are sorted by z.
     */
    private List<VisualizationWindow> getZOrderedWindows() {
        List<VisualizationWindow> list = new ArrayList<VisualizationWindow>();
        for (VisualizationWindow window : windowsByType.values()) {
            if (!window.isMinimized()) {
                list.add(window);
            }
        }
        Collections.sort(list, new Comparator<VisualizationWindow>() {

            @Override
            public int compare(VisualizationWindow o1, VisualizationWindow o2) {
                return Ints.compare(o1.getElement().getZIndex(), o2.getElement().getZIndex());
            }
        });
        return list;
    }

    @Override
    public String getName() {
        return worksheetPresenter.getName();
    }

    @Override
    public void add(Visualization visualization) {
        VisualizationWindow window = createWindow(visualization, worksheetScreenLayout, panel);
        windowsByType.put(visualization.getType(), window);
        new WindowStateChangeHandler(window, worksheetScreenLayout);
        int minimizedYPosition = vizBarContainer.getVizBarYPosition(visualization.getType());
        window.setMinimizedYPosition(minimizedYPosition);

        window.show();
        if (worksheetScreenLayout.getLayout().isDirty()) {
            WindowStateChangeHandler.saveState(worksheetScreenLayout);
        }
        vizBarContainer.update();
    }

    @Override
    public void remove(Visualization visualization) {
        Collection<VisualizationWindow> windows = windowsByType.get(visualization.getType());
        VisualizationWindow toRemove = null;
        for (VisualizationWindow window : windows) {
            if (window.getVisualizationPanel().getVisualization().equals(visualization)) {
                toRemove = window;
                break;
            }
        }
        if(toRemove != null){
	        windows.remove(toRemove);
	        worksheetScreenLayout.getLayout().removeVisualizationLayoutState(visualization.getUuid());
	        worksheetScreenLayout.getWorksheetDef().getVisualizations().remove(visualization.getVisualizationDef());
	        WindowStateChangeHandler.saveState(worksheetScreenLayout);
	        vizBarContainer.update();
	        toRemove.hide(); // Will remove from parent.
        }
    }

    @Override
    public boolean isMinimized(Visualization vz) {
        Collection<VisualizationWindow> windows = windowsByType.get(vz.getType());
        for (VisualizationWindow window : windows) {
            if (window.getVisualizationPanel().getVisualization() == vz) {
                return window.isMinimized();
            }
        }
        // Note: We can reach here if visualizations have not yet been loaded (i.e., this worksheet tab has not
        // yet been looked at).
        return true;
    }

    @Override
    public void add(Window w) {
        panel.add(w);
    }

    public WorksheetScreenLayout getWorksheetScreenLayout() {
        return worksheetScreenLayout;
    }

    public boolean isReadOnly() {

        return (null != worksheetPresenter) ? worksheetPresenter.isReadOnly() : true;
    }
}
