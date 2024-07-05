package csi.client.gwt.viz.timeline.view.measured.drawing;

import java.util.List;

import csi.client.gwt.viz.timeline.view.drawing.OverviewRenderable;
import csi.client.gwt.viz.timeline.view.drawing.TimelineOverview;
import csi.client.gwt.widget.drawing.Renderable;

public class HistogramTimelineOverview extends TimelineOverview<OverviewRenderable>{
	
    public HistogramTimelineOverview(){
		super();
	}
	
	

    @Override
    public void setOverviewRenderables(List<OverviewRenderable> summaryRenderables) {
        eventLayer.clear();
        for(OverviewRenderable overviewRenderable: summaryRenderables){
            eventLayer.addItem((Renderable) overviewRenderable);
        }
    }

}
