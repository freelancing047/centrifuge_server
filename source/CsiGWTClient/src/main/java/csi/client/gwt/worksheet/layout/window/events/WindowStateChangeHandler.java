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
package csi.client.gwt.worksheet.layout.window.events;

import java.util.HashMap;
import java.util.Map;

import com.sencha.gxt.core.client.util.Point;
import com.sencha.gxt.core.client.util.Size;
import com.sencha.gxt.fx.client.DragEndEvent;
import com.sencha.gxt.fx.client.DragEndEvent.DragEndHandler;
import com.sencha.gxt.widget.core.client.event.ActivateEvent;
import com.sencha.gxt.widget.core.client.event.ActivateEvent.ActivateHandler;
import com.sencha.gxt.widget.core.client.event.MaximizeEvent;
import com.sencha.gxt.widget.core.client.event.MaximizeEvent.MaximizeHandler;
import com.sencha.gxt.widget.core.client.event.MinimizeEvent;
import com.sencha.gxt.widget.core.client.event.MinimizeEvent.MinimizeHandler;
import com.sencha.gxt.widget.core.client.event.ResizeEndEvent;
import com.sencha.gxt.widget.core.client.event.ResizeEndEvent.ResizeEndHandler;
import com.sencha.gxt.widget.core.client.event.RestoreEvent;
import com.sencha.gxt.widget.core.client.event.RestoreEvent.RestoreHandler;

import csi.client.gwt.worksheet.layout.window.VisualizationWindow;
import csi.client.gwt.worksheet.layout.window.WindowBase;
import csi.client.gwt.worksheet.layout.window.WorksheetStateSaveTimer;
import csi.server.common.model.worksheet.VisualizationLayoutState;
import csi.server.common.model.worksheet.WorksheetScreenLayout;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class WindowStateChangeHandler {

    private VisualizationWindow window;
    private WorksheetScreenLayout layout;
    private VisualizationLayoutState state;
    private static Map<String, WorksheetStateSaveTimer> timersByUuid = new HashMap<String, WorksheetStateSaveTimer>();

    /**
     * @param window
     * @param uuid Visualization uuid
     * @param layout
     */
    public WindowStateChangeHandler(VisualizationWindow window, WorksheetScreenLayout layout) {
        super();
        this.window = window;
        this.layout = layout;
        state = layout.getLayout().getLayoutState(window.getVisualizationPanel().getVisualization().getVisualizationDef());
        addHandlers();

    }

    private void addHandlers() {
        window.addMaximizeHandler(new MaximizeHandler() {

            @Override
            public void onMaximize(MaximizeEvent event) {
                state.setMaximized(true);
                update();
            }
        });
        window.addMinimizeHandler(new MinimizeHandler() {

            @Override
            public void onMinimize(MinimizeEvent event) {
                state.setMinimized(true);
                Point point = window.getElement().getPosition(true);
                state.setMinimizedX(point.getX());
                state.setMinimizedY(point.getY());
                update();
            }
        });
        window.addExpandFromMinimizeEventHandler(new ExpandFromMinimizeEventHandler() {

            @Override
            public void onExpandFromMinimize(ExpandFromMinimizeEvent event) {
                state.setMinimized(false);
                state.setMinimizedX(0);
                state.setMinimizedY(0);
                if(state.getX() < 0){
                    state.setX(0);
                }
                if(state.getY() < 0){
                    state.setY(0);
                }
                update();
            }
        });

        window.addRestoreHandler(new RestoreHandler() {

            @Override
            public void onRestore(RestoreEvent event) {
                state.setMaximized(false);
                update();
            }
        });
        window.getResizable().addResizeEndHandler(new ResizeEndHandler() {

            @Override
            public void onResizeEnd(ResizeEndEvent event) {
                Point point = window.getElement().getPosition(true);
                state.setX(point.getX());
                state.setY(point.getY());
                Size size = window.getElement().getSize();
                state.setWidth(size.getWidth());
                state.setHeight(size.getHeight());
                update();
            }
        });
        window.addActivateHandler(new ActivateHandler<WindowBase>() {

            @Override
            public void onActivate(ActivateEvent<WindowBase> event) {
                layout.setActivatedVisualizationUuid(state.getVisualizationDef().getUuid());
                update();
            }
        });

        window.getDraggable().addDragEndHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                Point point = window.getElement().getPosition(true);
                state.setX(point.getX());
                state.setY(point.getY());
                update();
            }
        });
    }

    protected void update() {
        saveState(layout);
    }

    public static void saveState(WorksheetScreenLayout layout) {
        WorksheetStateSaveTimer timer = timersByUuid.get(layout.getWorksheetDef().getUuid());
        if (timer == null) {
            timer = new WorksheetStateSaveTimer();
            timersByUuid.put(layout.getWorksheetDef().getUuid(), timer);
        } else {
            if(!timer.isLocked()){
                timer.invalidate();
                timersByUuid.remove(layout.getWorksheetDef().getUuid());
                timer = new WorksheetStateSaveTimer();
                timersByUuid.put(layout.getWorksheetDef().getUuid(), timer);
            }
        }
        timer.cancel();
        timer.setLayoutState(layout);
        timer.schedule(5000);
    }
}
