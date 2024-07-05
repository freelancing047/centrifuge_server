package csi.client.gwt.viz.timeline.view.drawing;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;

import csi.client.gwt.viz.timeline.model.TimeScale;
import csi.client.gwt.viz.timeline.model.ViewPort;
import csi.client.gwt.widget.drawing.BaseRenderable;
import csi.client.gwt.widget.drawing.Layer;

public class DetailedOverviewRenderable extends BaseRenderable implements OverviewRenderable{

	private Layer layer;
	private TimeScale timeScale;
	private DetailedEventRenderable event;
	private int HEIGHT = 30;
	private int EVENT_RADIUS = 2;
    private ViewPort viewport;

	public DetailedOverviewRenderable(DetailedEventRenderable event){
		this.event = event;
	}
	
	@Override
	public void render(Context2d context2d) {
//		if (!v.isVisible())
//			continue;
		if(event.isVisible()){
		    return;
		}
		if(event == null || event.getStartTime() == null){
		    return;
		}
		
		//g.setColor(v.getColor());
		context2d.setFillStyle(CssColor.make(50,50,50));

		context2d.setStrokeStyle(CssColor.make(50,50,50));
		int x= timeScale.toInt(event.getStartTime());

		int y = event.getY();
		//y=(int) (EVENT_RADIUS+(int)(y*HEIGHT)/(scaleHeight-2*EVENT_RADIUS));
		double totalHeight = viewport.getTotalHeight();
		if(totalHeight < viewport.getCurrentHeight()){
		    totalHeight = viewport.getCurrentHeight();
		}
		if(totalHeight == 0){
		    return;
		}
		y= (int)(HEIGHT*(y/totalHeight)) + EVENT_RADIUS;
		if(y > HEIGHT){
		    y = y % HEIGHT;
		}
		
		context2d.fillRect(x, y, 2*EVENT_RADIUS, 3);
		
		
		//g.fillRect(x-1,y-EVENT_RADIUS,2*EVENT_RADIUS,3);
		if (event.hasEnd() && event.validateEnd())
		{

			context2d.beginPath();
			context2d.moveTo(x, y+EVENT_RADIUS);
			context2d.lineTo(timeScale.toInt(event.getEndTime()), y+EVENT_RADIUS);
			context2d.stroke();
			//g.drawLine(x,y,event.getEndX(),y);
		}
	}

	@Override
	public boolean hitTest(double x, double y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void bind(Layer layer) {
        this.layer = layer;
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}


	public TimeScale getTimeScale() {
		return timeScale;
	}

	public void setTimeScale(TimeScale timeScale) {
		this.timeScale = timeScale;
	}

    public void setViewport(ViewPort timelineViewport) {
        this.viewport = timelineViewport;
    }

    public DetailedEventRenderable getEvent() {
        return event;
    }

}
