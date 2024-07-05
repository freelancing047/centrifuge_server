package csi.client.gwt.viz.timeline.view.drawing;

import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.viz.timeline.model.Axis;
import csi.client.gwt.widget.drawing.DrawingPanel;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Renderable;

public class TimelineAxis extends DrawingPanel {
		
	private Layer layer;
    
	
	public TimelineAxis(){
		layer = new Layer();
		addLayer(layer);
	}

	public void setAxes(List<Axis> axes){
	    for(Renderable renderable: layer.getRenderables()){
            if(renderable instanceof DetailedEventRenderable){
                ((Axis) renderable).deregisterHandlers();
            }
        }
		layer.removeAll();
		Axis previous = null;
		for(Axis axis: axes){
		    if(previous == null || previous.getUnit().getCalendarCode() != axis.getUnit().getCalendarCode()){
    			layer.addItem(axis);
    			axis.startHandlers();
    			previous = axis;
		    }
		}
	}
	
	public int renderAxis(){
		super.render();
		Widget parent = this.getParent();
		if(parent instanceof LayoutPanel){
		    ((LayoutPanel)parent).setWidgetBottomHeight(this, 30, Unit.PX, getAxisHeight(), Unit.PX);
		}
		return this.getHeight();
	}

	public int getAxisHeight(){
	    return Axis.HEIGHT * layer.getRenderables().size();    
	}

    public void reset() {
        layer.clear();
    }
}
