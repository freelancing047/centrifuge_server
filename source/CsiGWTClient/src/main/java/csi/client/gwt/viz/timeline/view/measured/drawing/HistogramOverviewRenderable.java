package csi.client.gwt.viz.timeline.view.measured.drawing;

import java.util.List;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;

import csi.client.gwt.viz.timeline.model.TimeScale;
import csi.client.gwt.viz.timeline.view.drawing.OverviewRenderable;
import csi.client.gwt.widget.drawing.BaseRenderable;
import csi.client.gwt.widget.drawing.Layer;
import csi.shared.core.visualization.timeline.OverviewTrack;
import csi.shared.gwt.viz.timeline.TimeUnit;

public class HistogramOverviewRenderable extends BaseRenderable implements OverviewRenderable {

    private static final int SPACE_BETWEEN_BARS = 1;
    private Layer layer;
    private TimeScale timeScale;
    private OverviewTrack overviewTrack;

    @Override
    public void render(Context2d context2d) {
        context2d.save();
        
        context2d.beginPath();
        context2d.setFillStyle(CssColor.make(50,50,50));

        context2d.setStrokeStyle(CssColor.make(40,40,40));
        
        int max = 0;
        List<Integer> measures = overviewTrack.getMeasures();
        for(Integer measure :measures){
            if(measure > max){
                max = measure;
            }
        }
        
        
        int totalMeasures = measures.size();
        
        if(totalMeasures == 0 || max == 0){
            return;
        }
        
        int layerHeight = layer.getHeight();
        
        layerHeight--; layerHeight--;
        long start = timeScale.getStart();
        TimeUnit unit = overviewTrack.getTimeUnit();
        Long roundedTime = unit.roundDown(start);
        
        int x = timeScale.toInt(roundedTime);
        Long nextRoundedTime = unit.addTo(roundedTime);
        int endX = timeScale.toInt(nextRoundedTime);
        
        for(Integer measure: measures){
            
            int barHeight = (int) ((double)measure/(double)max * layerHeight);
            
            renderIndividualHistogram(context2d, barHeight, x, endX, layerHeight);
            
            roundedTime = nextRoundedTime;
            nextRoundedTime = unit.addTo(roundedTime);
            x = timeScale.toInt(roundedTime);
            endX = timeScale.toInt(nextRoundedTime);
        }
        
        //g.fillRect(x-1,y-EVENT_RADIUS,2*EVENT_RADIUS,3);
//        if (event.hasEnd() && event.validateEnd())
//        {
//
//            context2d.moveTo(x, y+EVENT_RADIUS);
//            context2d.lineTo(timeScale.toInt(event.getEndTime()), y+EVENT_RADIUS);
//            context2d.stroke();
//            //g.drawLine(x,y,event.getEndX(),y);
//        }
        context2d.stroke();
        context2d.fill();
        context2d.closePath();
        context2d.restore();
    }

    private void renderIndividualHistogram(Context2d context2d, int barHeight, int x, int endX, int layerHeight) {
        
        int startX = x;
        
        int width = endX - x;
        int startY = 1 + layerHeight - barHeight;
        
        context2d.strokeRect(startX + SPACE_BETWEEN_BARS, startY, width - SPACE_BETWEEN_BARS * 2, barHeight);
        context2d.fillRect(startX + SPACE_BETWEEN_BARS, startY, width - SPACE_BETWEEN_BARS * 2, barHeight);
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

    public void setTrack(OverviewTrack track){
        this.overviewTrack = track;
    }

    public TimeScale getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(TimeScale timeScale) {
        this.timeScale = timeScale;
    }
}
