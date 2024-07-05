package csi.client.gwt.viz.timeline.view.drawing;

import java.util.List;

import csi.client.gwt.widget.drawing.Renderable;

public class DetailedTimelineOverview extends TimelineOverview<DetailedOverviewRenderable>{
	
    public DetailedTimelineOverview(){
		super();
	}
	
	@Override
	public void setOverviewRenderables(List<DetailedOverviewRenderable> overviewRenderables) {
		eventLayer.clear();
		for(DetailedOverviewRenderable overviewRenderable: overviewRenderables){
			eventLayer.addItem((Renderable)overviewRenderable);
		}
	}

}
