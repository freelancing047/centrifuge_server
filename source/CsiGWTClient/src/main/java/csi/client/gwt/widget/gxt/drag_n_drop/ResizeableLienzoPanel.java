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
package csi.client.gwt.widget.gxt.drag_n_drop;

import java.util.ArrayList;
import java.util.List;

import com.emitrom.lienzo.client.core.shape.Layer;
import com.emitrom.lienzo.client.widget.LienzoPanel;
import com.emitrom.lienzo.shared.core.types.ColorName;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.RequiresResize;

import csi.client.gwt.edit_sources.DataSourceEditorView;
import csi.client.gwt.events.CsiDropEvent;
import csi.client.gwt.events.CsiDropEventHandler;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ResizeableLienzoPanel extends LienzoPanel implements RequiresResize {

    private List<Layer> layers = new ArrayList<Layer>();
    private int _width = 2000;
    private int _height = 2000;
    private LienzoPanelDropTarget _target;

    public ResizeableLienzoPanel(DataSourceEditorView totalViewIn) {
        super(100, 100);
        super.setBackgroundColor(ColorName.WHITE);
        // Disable the resize timer. We rely on the resize event instead.
        setResizeCheckRepeatInterval(Integer.MAX_VALUE);
        getViewport().getTransform().translate(0.5, 0.5);
        _target = new LienzoPanelDropTarget(this, totalViewIn);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        // The LienzoPanel's constructor requires a width and height to be specified. We arbitrarily set that in the
        // default constructor to be 100x100. We want the panel to take the size of its container. So we add this hook
        // to onLoad() to accomplish that.
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                onResize();
            }
        });
    }
    
    public void setDimmensions(int widthIn, int heightIn) {
        
        _width = widthIn;
        _height = heightIn;
        
        onResize();
    }

    public HandlerRegistration addCsiDropEventHandler(CsiDropEventHandler handlerIn) {

        return addHandler(handlerIn, CsiDropEvent.type);
    }

    @Override
    public void onResize() {
        
        int myWidth = Math.max(_width, getParent().getOffsetWidth());
        int myHeight = Math.max(_height, getParent().getOffsetHeight());
        
        setPixelSize(myWidth, myHeight);
        getViewport().draw();
    }

    @Override
    public LienzoPanel add(Layer layer) {
        layers.add(layer);
        return super.add(layer);
    }

    @Override
    public LienzoPanel remove(Layer layer) {
        layers.remove(layer);
        return super.remove(layer);
    }

    public List<Layer> getLayers() {
        return layers;
    }

    @Override
    public void clear() {
        List<Layer> copy = new ArrayList<Layer>(getLayers());
        for (Layer layer : copy) {
            remove(layer);
        }
        super.clear();
    }
}
