package csi.client.gwt.viz.timeline.view.measured.drawing;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.event.shared.HandlerRegistration;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.timeline.model.TimeScale;
import csi.client.gwt.viz.timeline.model.measured.MeasuredTrackProxy;
import csi.client.gwt.viz.timeline.view.drawing.TrackRenderable;
import csi.shared.core.visualization.timeline.MeasuredTrack;
import csi.shared.core.visualization.timeline.MeasuredTrackItem;
import csi.shared.core.visualization.timeline.TimelineTrack;
import csi.shared.gwt.viz.timeline.TimeUnit;

@SuppressWarnings("rawtypes")
public class MeasuredTrackRenderable extends TrackRenderable implements Comparable{
    public MeasuredTrackProxy track;
    public boolean showLabel = true;
    public boolean showDuration = true;
    private int y;
    public static final String NO_VALUE = CentrifugeConstantsLocator.get().timeline_colorLegend_noValue();
    private static final double SPACE_BETWEEN_BARS = 0;
    private boolean highlightLabel = false;
    public static double totalH=0;
    private double height = 150;
    private double yTranslate;
    //height = (int) ((Math.random()*FULL_SUMMARY_HEIGHT)*10+36)

    public MeasuredTrackRenderable(MeasuredTrackProxy trackModel){
        this.track = trackModel;
        startHandlers();
    }

    @Override
    public void render(Context2d context2d) {

        if(timelineViewport == null || track == null){
            return;
        }
        if(timelineViewport.getCurrentWidth() <= 7)  {
            return;
        }
//        y = track.getIndex() * (height) + timelineViewport.getStart();
        y = 0;
        MeasuredTrack measuredTrack = track.getTrack();
        if (measuredTrack.getIndex() == 0) {
            totalH = 0;
        }
        
        if(!isVisible()) {
            return;
        }
        
        List<MeasuredTrackItem> measures = measuredTrack.getMeasures();
        int max = 0;
        for(MeasuredTrackItem trackItem: measures){
            int num = trackItem.getValue();
            if(max < num){
                max = num;
            }
        }
        int count = 0;

        for (MeasuredTrackItem trackItem : measuredTrack.getMeasures()) {
            int integer = trackItem.getValue();
            count += integer;
        }
        height = measuredTrack.getHeight();
        yTranslate = (height + totalH);
        context2d.translate(0,yTranslate)  ;
        totalH = yTranslate;

//        if(y - height > timelineViewport.getCurrentHeight() - timelineViewport.getStart() || y < 0){
//            return;
//        }


        //drawLineMeasure(context2d, y, max);
        drawMeasure(context2d, y, max);
        if(track.getNameOverride()!=null) {
            drawLabel(context2d, track.getNameOverride(), y, highlightLabel);
        } else {
            drawLabel(context2d, track.getLabel(), y, highlightLabel);
        }
    }

    public boolean isVisible() {
        return getTrack().isVisible();
    }

    public void drawMeasure(Context2d context2d, int y, int max) {
        context2d.save();
        context2d.beginPath();
        if (getTrack().getColor() == 0) {
//            context2d.setFillStyle(CssColor.make(200, 200, 200));
        } else {

            int red = (getTrack().getColor() >> 16) & 0xFF;
            int green = (getTrack().getColor() >> 8) & 0xFF;
            int blue = getTrack().getColor() & 0xFF;

            context2d.setFillStyle(CssColor.make(red, green, blue));
            context2d.setStrokeStyle(CssColor.make(red, green, blue));
        }
        //Draws the circle for the event
        context2d.setGlobalAlpha(.5);
        //context2d.arc(ox, y - 1, 2*r, 0, Math.PI * 2);    
        context2d.setGlobalAlpha(1);

        MeasuredTrack measuredTrack = track.getTrack();

        List<MeasuredTrackItem> measures = measuredTrack.getMeasures();

        drawBackground(context2d, y, y);

//        if(track.isBanding()){
//            drawShadow(context2d, y, y+height);
//        }

        TimeUnit timeUnit = track.getTimeUnit();

        TimeScale timeScale = track.getAxis().getTimeScale();

        List<Long> tics = new ArrayList<Long>();
        Long time = timeUnit.roundDown(timeScale.getStart());
        tics.add(time);
        int count = 0;
        do {
            time = timeUnit.addTo(time);
            tics.add(time);
            count++;
        } while (time < timeScale.getEnd() && count < 90000);


        for (int ii = 0; ii < measures.size() && ii < tics.size() - 1; ii++) {
            MeasuredTrackItem trackItem = measures.get(ii);
            if (trackItem.isSelected()) {
                context2d.setFillStyle(CssColor.make(255, 133, 19));
                context2d.setStrokeStyle(CssColor.make(255, 133, 19));

            } else {
                context2d.setFillStyle(CssColor.make(6, 68, 109));
                context2d.setStrokeStyle(CssColor.make(6, 68, 109));
            }
            Long startTime = tics.get(ii);
            Long endTime = tics.get(ii + 1);

            int measureValue = trackItem.getValue();
            double barPreciseHeight = ((double) measureValue) / ((double) max) * (height - 5);
            if (measureValue > 0 && barPreciseHeight < 1) {
                barPreciseHeight = 1;
            }

            int barHeight = (int) Math.ceil(barPreciseHeight);

            if (barHeight != 0 && measureValue != 0) {
                renderIndividualHistogram(context2d, barHeight, 0, y, timeScale, startTime, endTime);
            }
        }

        context2d.fill();
        context2d.stroke();
        context2d.closePath();
        context2d.restore();
    }
/*
    public void drawLineMeasure(Context2d context2d, int y, int max) {
        context2d.save();
        context2d.beginPath();
        if(getTrack().getColor() == 0){

            context2d.setFillStyle(CssColor.make(84,84,84));
            context2d.setStrokeStyle(CssColor.make(90,90,90));

        } else {

            int red = (getTrack().getColor() >> 16) & 0xFF;
            int green = (getTrack().getColor() >> 8) & 0xFF;
            int blue = getTrack().getColor() & 0xFF;

            context2d.setFillStyle(CssColor.make(red,green,blue));
            context2d.setStrokeStyle(CssColor.make(red,green,blue));
        }
        //Draws the circle for the event
        context2d.setGlobalAlpha(.5);
        //context2d.arc(ox, y - 1, 2*r, 0, Math.PI * 2);    
        context2d.setGlobalAlpha(1);
        
        MeasuredTrack measuredTrack = track.getTrack();
        
        List<Integer> measures = measuredTrack.getMeasures();
        
        drawBackground(context2d, y, y+height);

        if(track.isBanding()){
            drawShadow(context2d, y, y+height);
        }
        
//        List<Long> tics = track.getAxis().getTics();
        
        
//        for(int ii=0; ii< measures.size() && ii<tics.size()-1; ii++){
//            Integer measureValue = measures.get(ii);
//                        
//            int barHeight = (int) ((double)measureValue/(double)max * (height - 5));
//
//            if(barHeight != 0 && measureValue != 0) {
//                //renderIndividualHistogram(context2d, barHeight, height, y, timeUnit, timeScale, startTime, endTime);
//            }
//
//                
//        }

        context2d.fill();
        context2d.stroke();
        
        context2d.closePath();
        context2d.restore();
    }*/
    
    private void renderIndividualHistogram(Context2d context2d, int barHeight, int index, int y, TimeScale timeScale, Long startTime, Long endTime) {
        
        //int startX = (int) (((double)index) * bucketSize + axisOffset) ;

        
        double startX = timeScale.toNum(startTime);
        double endX = timeScale.toNum(endTime);
        double width = endX - startX;
        
        width = endX - startX - 1;
        
        //We adjust width here for spacing
        if(width > SPACE_BETWEEN_BARS * 2){
            width = width - SPACE_BETWEEN_BARS * 2;
        }
        
        double  startY = y - barHeight;
        
        //We draw a line here if the width is too small
        if(width < 2) {
            context2d.moveTo(startX, startY + barHeight);
            context2d.lineTo(startX, startY);
        } else {
            context2d.setLineWidth(2);
            context2d.setGlobalAlpha(.5*((double)barHeight/(double)(height - 5))+.5);
            drawRoundedRectangle(context2d,startX+SPACE_BETWEEN_BARS,startY,barHeight,width, Math.min(barHeight,width/8),CssColor.make(255, 255, 255),CssColor.make(6, 68, 109));
//            context2d.strokeRect(startX + SPACE_BETWEEN_BARS, startY, width, barHeight);
            context2d.setGlobalAlpha(.15* Math.pow((double) barHeight / (double) (height - 5),2));
            drawRoundedRectangle(context2d,startX+SPACE_BETWEEN_BARS,startY,barHeight,width, Math.min(barHeight,width/8),CssColor.make(6, 68, 109),CssColor.make(6, 68, 109));
            context2d.fillRect(startX + SPACE_BETWEEN_BARS, startY, width-1, barHeight-1);
            context2d.closePath();
            context2d.beginPath();
            context2d.closePath();
            context2d.setGlobalAlpha(1);
        }
    }
    
    public void roundRect(Context2d ctx, int x, int y, int width, int height, int radius) {
        
        ctx.beginPath();
        ctx.moveTo(x + radius, y);
        ctx.lineTo(x + width - radius, y);
        ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
        ctx.lineTo(x + width, y + height - radius);
        ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
        ctx.lineTo(x + radius, y + height);
        ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
        ctx.lineTo(x, y + radius);
        ctx.quadraticCurveTo(x, y, x + radius, y);
        ctx.closePath();
        ctx.stroke();

      }

//    public void drawSummaryEvent(Context2d context2d, int y, int ox, double r) {
//
//        context2d.beginPath();
//        if(getEvent().getEvent().getColorValue() == null || getEvent().getEvent().getColorValue() == TimelineTrack.NULL_TRACK){
//
//            context2d.setFillStyle(CssColor.make(84,84,84));
//            context2d.setStrokeStyle(CssColor.make(40,40,40));   
//
//        } else {
//
//            int red = (getEvent().getColor() >> 16) & 0xFF;
//            int green = (getEvent().getColor() >> 8) & 0xFF;
//            int blue = getEvent().getColor() & 0xFF;
//
//            context2d.setFillStyle(CssColor.make(red,green,blue));
//            context2d.setStrokeStyle(CssColor.make(red,green,blue)); 
//        }
//        //Draws the circle for the event
//        context2d.setGlobalAlpha(1);
//        //context2d.arc(ox, y - 1, 2*r, 0, Math.PI * 2);
//
//        int lineY=y+EVENT_HEIGHT;
//        if (hasEnd() && isShowDuration() && validateEnd())
//        {
//            //Draw event duration line
//
//
//            context2d.setFillStyle(CssColor.make(84,84,84));
//            context2d.setStrokeStyle(CssColor.make(40,40,40));   
//            context2d.fillRect(getX(), lineY, getEndX()-getX(), 2);
//            //Draws a line from event to event duration
//            //context2d.fillRect(getX() + 2*r, lineY - 4, 2, 4);
//        }
//        if(getEvent().getEvent().getColorValue() != null && getEvent().getEvent().getColorValue() != TimelineTrack.NULL_TRACK){
//
//            int red = (getEvent().getColor() >> 16) & 0xFF;
//            int green = (getEvent().getColor() >> 8) & 0xFF;
//            int blue = getEvent().getColor() & 0xFF;
//
//            context2d.setFillStyle(CssColor.make(red,green,blue));
//            context2d.setStrokeStyle(CssColor.make(red,green,blue)); 
//        }
//        context2d.fillRect(getX(), lineY - 6, SUMMARY_EVENT_WIDTH, SUMMARY_EVENT_HEIGHT);
////            context2d.setLineCap(LineCap.ROUND);
////            context2d.moveTo(getX() + 2, lineY);
////            context2d.setLineWidth(2);
////            context2d.lineTo(getX() - 2, lineY - 5);
////            context2d.stroke();
////
////            context2d.setLineWidth(1);
////        if(event.isSelected()){
////            CssColor selection = CssColor.make(255,133,10);
////            context2d.setGlobalAlpha(.8);
////            context2d.setStrokeStyle(selection);
////
////            context2d.strokeRect(getX(), lineY - 6, SUMMARY_EVENT_WIDTH, SUMMARY_EVENT_HEIGHT);
////
////            context2d.setGlobalAlpha(1);
////            
////        }
//
//        context2d.fill();
//        context2d.stroke();
//        context2d.closePath();
//    }
//
////    public void drawSelection(Context2d context2d, double r) {
//
//        int y = event.getY() + viewPort.getStart();
//        CssColor selection = CssColor.make(255,133,10);
//        context2d.setGlobalAlpha(.2);
//        context2d.setFillStyle(selection);
//
//        int width =  getEndX()-getX();
//
//        if(width < labelWidth){
//            width = labelWidth;
//        }
//
//        int dotWidth = (int) (r*4);
//
//        context2d.fillRect(getX() - dotWidth/2, y - EVENT_HEIGHT, width + dotWidth, EVENT_HEIGHT * 2 + 2);
//
//        context2d.setGlobalAlpha(.8);
//        context2d.fillRect(getX() - dotWidth/2 - 4, y - EVENT_HEIGHT, 3, EVENT_HEIGHT * 2 + 2);
//
//        context2d.setFillStyle(CssColor.make(0,0,0));
//        context2d.setGlobalAlpha(1);
//    }

//    public void drawHighlight(Context2d context2d, double r) {
//
//        int y = event.getY() + viewPort.getStart();
//        CssColor selection = CssColor.make(100,100,100);
//        context2d.setGlobalAlpha(.4);
//        context2d.setFillStyle(selection);
//
//        int width =  getEndX()-getX();
//
//        if(width < labelWidth){
//            width = labelWidth;
//        }
//
//        int dotWidth = (int) (r*4);
//
//        context2d.fillRect(getX() - dotWidth/2 - 1, y - EVENT_HEIGHT - 1, width + dotWidth + 2, EVENT_HEIGHT * 2 + 4);
//
//        context2d.setFillStyle(CssColor.make(0,0,0));
//        context2d.setGlobalAlpha(1);
//    }
//
//    public void drawSearchHit(Context2d context2d, double r) {
//        int y = event.getY() + viewPort.getStart();
//        CssColor selection = CssColor.make(255,255,102);
//        context2d.setGlobalAlpha(.4);
//        context2d.setFillStyle(selection);
//
//        int width =  getEndX()-getX();
//
//        if(width < labelWidth){
//            width = labelWidth;
//        }
//
//        int dotWidth = (int) (r*4);
//
//        context2d.fillRect(getX() - dotWidth/2 - 1, y - EVENT_HEIGHT - 1, width + dotWidth + 2, EVENT_HEIGHT * 2 + 4);
//
//        context2d.setFillStyle(CssColor.make(0,0,0));
//        context2d.setGlobalAlpha(1);
//    }
//    
//
//    private void drawSearchHighlight(Context2d context2d, double r) {
//        int y = event.getY() + viewPort.getStart();
//        CssColor selection = CssColor.make(51,102,255);
//        context2d.setGlobalAlpha(.3);
//        context2d.setFillStyle(selection);
//
//        int width =  getEndX()-getX();
//
//        if(width < labelWidth){
//            width = labelWidth;
//        }
//
//        int dotWidth = (int) (r*4);
//
//        context2d.fillRect(getX() - dotWidth/2, y - EVENT_HEIGHT, width + dotWidth, EVENT_HEIGHT * 2 + 2);
//
//        context2d.setGlobalAlpha(.8);
//        context2d.fillRect(getX() - dotWidth/2 - 3, y - EVENT_HEIGHT, 2, EVENT_HEIGHT * 2 + 2);
//
//        context2d.setFillStyle(CssColor.make(0,0,0));
//        context2d.setGlobalAlpha(1);
//    }

//    public boolean validateEnd() {
//        return getEndX() > getX();
//    }

    @Override
    public boolean hitTest(double x, double y) {
        
        return x <= MeasuredTrackRenderable.LABEL_START_X + Math.max(40,getLabelWidth()) && x >= MeasuredTrackRenderable.LABEL_START_X &&
                y >= yTranslate-height && y <= yTranslate;
//
//        int myY = event.getY() + viewPort.getStart();
//        if(x  >= getX() - getDotR() *4 && (x <= getEndX() || x <= getX() + labelWidth)){
//            if(y >= myY - EVENT_HEIGHT/2 && y <= (myY + EVENT_HEIGHT)){
//                return true;
//            }
//
//        }

    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public int compareTo(Object o) {
        return compareTo((MeasuredTrackRenderable)o);
    }

    public int compareTo(MeasuredTrackRenderable trackRenderable){

        if (this==trackRenderable)
            return 0;
        if (trackRenderable==null || trackRenderable.getTrack() == null)
            return 1;
        if (this.getTrack() == null){
            return -1;
        }
        
        
        return trackRenderable.getTrack().compareTo(this.getTrack());
    }

    public void deregisterHandlers(){
        if(handlers == null){
            return;
        }

        for(HandlerRegistration handlerRegistration: handlers){
            handlerRegistration.removeHandler();
        }
    }

//    public Long getEndTime(){
//        return event.getEndTime();
//    }
//
//    public Long getStartTime() {
//        return event.getStartTime();
//    }
//
//    public TimelineEventProxy getEvent() {
//        return event;
//    }
//
//    public void setEvent(TimelineEventProxy event) {
//        this.event = event;
//    }
//
//    public int getX() {
//        return event.getX();
//    }
//
//    public void setX(int x) {
//        event.setX(x);
//    }
//
//    public int getY() {
//        return event.getY();
//    }
//    
//    public int getOffsetY(){
//        return event.getY() + viewPort.getStart();
//    }
//
//    public void setY(int y) {
//        event.setY(y);
//    }
//    
//    public int getTrackStartY() {
//        return event.getTrack().getStartY();
//    }
//
//
//    public int getEndX() {
//        return event.getEndX();
//    }

//    public double getDotR()
//    {
//        return Math.max(1.0, Math.abs(event.getDrawableSize()))/2;
//    }
    public boolean isShowLabel() {
        return showLabel;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
    }

//    public boolean hasEnd(){
//        if(event.getEndTime() == null){
//            return false;
//        }
//        return true;
//    }
    public boolean isShowDuration() {
        return showDuration;
    }

    public void setShowDuration(boolean showDuration) {
        this.showDuration = showDuration;
    }

//    public void setSelected(boolean b) {
//        event.setSelected(b);
//    }
//
//    public boolean isSelected() {
//        return event.isSelected();
//    }
//
//    public boolean isHighlight() {
//        return highlight;
//    }
//
//    public void setHighlight(boolean highlight) {
//        this.highlight = highlight;
//    }
    public void setHandlers(List<HandlerRegistration> handlers) {
        this.handlers = handlers;
    }

    public MeasuredTrackProxy getTrack() {
        return track;
    }

    public void setTrack(MeasuredTrackProxy track) {
        this.track = track;
    }

    public void setHighlight(boolean b) {
        this.highlightLabel = b;
    }
//    public TimelineTrackModel getTrack() {
//        return event.getTrack();
//    }
//
//    public void setTrack(TimelineTrackModel track) {
//        event.setTrack(track);
//    }

    public static void drawRoundedRectangle(Context2d context2d, double x, double y, double height, double width, double curve, CssColor fillStyle, CssColor strokeStyle){
        context2d.save();
        context2d.beginPath();
        context2d.setMiterLimit(0);
        FillStrokeStyle savedStyle = context2d.getFillStyle();
        context2d.moveTo(x, y+height);//bottom-left
        context2d.lineTo(x, y+curve);//top-left
        context2d.quadraticCurveTo(x, y, x+curve, y);
//        context2d.moveTo(x+curve, y);//top-left
        context2d.lineTo(x+width-curve, y);//top-right
        context2d.quadraticCurveTo(x+width, y, x+width, y+curve);//top-right-curve
        context2d.lineTo(x+width, y+height);//bottom-right
//        context2d.quadraticCurveTo(x+width, y+height, x+width-curve, y+height);
//        context2d.quadraticCurveTo(x, y+height, x, y+height-curve);
//        context2d.closePath();
        context2d.stroke();
        context2d.setFillStyle(fillStyle);
        context2d.setStrokeStyle(strokeStyle);
        context2d.clip();
//        context2d.fill();

//        context2d.fillRect(x, y, width, height);

//        context2d.strokeRect(x, y, width, height);
        context2d.restore();
        context2d.setFillStyle(savedStyle);
    }
}
