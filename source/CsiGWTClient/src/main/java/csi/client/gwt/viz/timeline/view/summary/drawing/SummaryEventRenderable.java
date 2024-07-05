package csi.client.gwt.viz.timeline.view.summary.drawing;

import java.util.List;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.event.shared.HandlerRegistration;

import csi.client.gwt.viz.timeline.model.Scrollbar;
import csi.client.gwt.viz.timeline.model.ViewPort;
import csi.client.gwt.viz.timeline.model.summary.SummaryEventProxy;
import csi.client.gwt.viz.timeline.view.drawing.EventRenderable;
import csi.client.gwt.widget.drawing.Layer;
import csi.shared.core.visualization.timeline.TimelineTrack;

public class SummaryEventRenderable extends EventRenderable{

    private static final int SUMMARY_EVENT_WIDTH = 2;
    private static final int SUMMARY_EVENT_HEIGHT = 5;
    private static final String ELLIPSIS = "\u2026";
    public static final int EVENT_HEIGHT = 6;
    private SummaryEventProxy event;
    private int labelWidth;
    public boolean showLabel = true;
    public boolean showDuration = true;
    private Layer layer;
    public boolean highlight = false;
    private List<HandlerRegistration> handlers;
    private ViewPort viewPort;

    public SummaryEventRenderable(SummaryEventProxy event){
        this.event = event;
    }

    @Override
    public void render(Context2d context2d) {
        
        if(viewPort == null || event == null){
            return;
        }
        int y = event.getY() + viewPort.getStart();
        
        if(y > viewPort.getCurrentHeight() - viewPort.getStart() || y < 0){
            drawSummaryIfPossible(context2d);
            return;
        }
        
        int ox = event.getX();//(int) (isShowLabel() ? event.getX()-2*r : event.getX());

        
        
        if(((validateEnd() && getEndX() < 0)  || !validateEnd() && ox < 0) || ox > viewPort.getCurrentWidth()){
            return;
        }

        context2d.beginPath();
        double r = getDotR();
        
        if(event.isSelected()){
            drawSelection(context2d, r);
        }

        if(isHighlight()){
            drawHighlight(context2d, r);
        }
      //matches search regex
        if(event.isSearchHit()){
            drawSearchHit(context2d, r);
        }

        //is the highlighted search hit
        if(event.isSearchHighlight()){
            drawSearchHighlight(context2d, r);
        }
        
        drawEvent(context2d, y, ox, r);

        if(showLabel){
            //TODO: why 12? dot size perhaps?
            int labelSpace= (int) (event.getSpaceToRight()-12 - 2*r);
            int sw=0;
            //TODO: label space threshold for ...'ing
            if (labelSpace>50)
            {
                String label = null;//event.getEvent().getLabel();
                if(label == null){
                    label = "";
                }
                
                int labelEndOfViewSpace = (int) (viewPort.getCurrentWidth() - ox - Scrollbar.SCROLLBAR_SIZE);
                if(labelSpace > labelEndOfViewSpace){
                    labelSpace = labelEndOfViewSpace;
                }
                
                String s= format(label, labelSpace/8, false);

                TextMetrics textMetrics = context2d.measureText(s);
                labelWidth = (int) textMetrics.getWidth() + 6;
                int n=s.indexOf(' ');
                //TODO: wtf magic nums
                int tx=(int) (event.getX()+2*r+5);
                int ty=y+4;

                if(labelWidth > labelEndOfViewSpace + 6){
                    return;
                }

                if(isHighlight() && s.contains(ELLIPSIS)){
                    drawExpandedLabel(context2d,event, label, tx, ty);
                    context2d.fillText(s,tx,ty);
                }else{
                    context2d.fillText(s,tx,ty);
                }

            }
        }
    }

    private void drawSummaryIfPossible(Context2d context2d) {
        int ox = event.getX();//(int) (isShowLabel() ? event.getX()-2*r : event.getX());

        if(ox < 0 || ox > viewPort.getCurrentWidth()){
            return;
        }
        
        int y = getY();
        if(y > viewPort.getCurrentHeight() - viewPort.getStart() || y < 0){
            return;
        }
        
        context2d.beginPath();
        double r = getDotR();
//        if(getTrack().hasSummary()){
//            if(getTrack().isGroupSpace()){
//                drawSummaryEvent(context2d, getTrack().getStartY()+ TrackRenderable.FULL_SUMMARY_HEIGHT/4*3 - EVENT_HEIGHT + viewPort.getStart(), ox, r);
//            } else {
//                drawSummaryEvent(context2d, getTrack().getStartY()+ TrackRenderable.EMPTY_SUMMARY_HEIGHT/4*3 - EVENT_HEIGHT + viewPort.getStart(), ox, r);
//            }
//        }
        
    }


    public void drawEvent(Context2d context2d, int y, int ox, double r) {

        context2d.beginPath();
        if(getEvent().getEvent().getColorValue() == null || getEvent().getEvent().getColorValue() == TimelineTrack.NULL_TRACK){

            context2d.setFillStyle(CssColor.make(84,84,84));
            context2d.setStrokeStyle(CssColor.make(40,40,40));   

        } else {

            int red = (getEvent().getColor() >> 16) & 0xFF;
            int green = (getEvent().getColor() >> 8) & 0xFF;
            int blue = getEvent().getColor() & 0xFF;

            context2d.setFillStyle(CssColor.make(red,green,blue));
            context2d.setStrokeStyle(CssColor.make(red,green,blue)); 
        }
        //Draws the circle for the event
        context2d.setGlobalAlpha(.5);
        //context2d.arc(ox, y - 1, 4*r, 0, Math.PI * 2);
        context2d.setGlobalAlpha(1);

        if (hasEnd() && isShowDuration() && validateEnd())
        {
            int lineY=y;
            //Draw event duration line

            context2d.strokeRect(getX(), lineY, getEndX()-getX(), 8);
            //Draws a line from event to event duration
            //context2d.strokeRect(getX() + 2*r, lineY - 4, 2, 8);

        } else {

            int lineY=y;
            //Draw event duration line

            context2d.strokeRect(getX(), lineY, 0, 8);
        }

        context2d.stroke();
        //context2d.fill();
        context2d.closePath();
    }

    public void drawSummaryEvent(Context2d context2d, int y, int ox, double r) {

        context2d.beginPath();
        if(getEvent().getEvent().getColorValue() == null || getEvent().getEvent().getColorValue() == TimelineTrack.NULL_TRACK){

            context2d.setFillStyle(CssColor.make(84,84,84));
            context2d.setStrokeStyle(CssColor.make(40,40,40));   

        } else {

            int red = (getEvent().getColor() >> 16) & 0xFF;
            int green = (getEvent().getColor() >> 8) & 0xFF;
            int blue = getEvent().getColor() & 0xFF;

            context2d.setFillStyle(CssColor.make(red,green,blue));
            context2d.setStrokeStyle(CssColor.make(red,green,blue)); 
        }
        //Draws the circle for the event
        context2d.setGlobalAlpha(1);
        //context2d.arc(ox, y - 1, 2*r, 0, Math.PI * 2);

        int lineY=y+EVENT_HEIGHT;
        if (hasEnd() && isShowDuration() && validateEnd())
        {
            //Draw event duration line


            context2d.setFillStyle(CssColor.make(84,84,84));
            context2d.setStrokeStyle(CssColor.make(40,40,40));   
            context2d.fillRect(getX(), lineY, getEndX()-getX(), 2);
            //Draws a line from event to event duration
            //context2d.fillRect(getX() + 2*r, lineY - 4, 2, 4);
        }
        if(getEvent().getEvent().getColorValue() != null && getEvent().getEvent().getColorValue() != TimelineTrack.NULL_TRACK){

            int red = (getEvent().getColor() >> 16) & 0xFF;
            int green = (getEvent().getColor() >> 8) & 0xFF;
            int blue = getEvent().getColor() & 0xFF;

            context2d.setFillStyle(CssColor.make(red,green,blue));
            context2d.setStrokeStyle(CssColor.make(red,green,blue)); 
        }
        context2d.fillRect(getX(), lineY - 6, SUMMARY_EVENT_WIDTH, SUMMARY_EVENT_HEIGHT);
//            context2d.setLineCap(LineCap.ROUND);
//            context2d.moveTo(getX() + 2, lineY);
//            context2d.setLineWidth(2);
//            context2d.lineTo(getX() - 2, lineY - 5);
//            context2d.stroke();
//
//            context2d.setLineWidth(1);
        if(event.isSelected()){
            CssColor selection = CssColor.make(255,133,10);
            context2d.setGlobalAlpha(.8);
            context2d.setStrokeStyle(selection);

            context2d.strokeRect(getX(), lineY - 6, SUMMARY_EVENT_WIDTH, SUMMARY_EVENT_HEIGHT);

            context2d.setGlobalAlpha(1);
            
        }

        context2d.fill();
        context2d.stroke();
        context2d.closePath();
    }

    public void drawSelection(Context2d context2d, double r) {

        int y = event.getY() + viewPort.getStart();
        CssColor selection = CssColor.make(255,133,10);
        context2d.setGlobalAlpha(.2);
        context2d.setFillStyle(selection);

        int width =  getEndX()-getX();

        if(width < labelWidth){
            width = labelWidth;
        }

        int dotWidth = (int) (r*4);

        context2d.fillRect(getX() - dotWidth/2, y - EVENT_HEIGHT, width + dotWidth, EVENT_HEIGHT * 2 + 2);

        context2d.setGlobalAlpha(.8);
        context2d.fillRect(getX() - dotWidth/2 - 4, y - EVENT_HEIGHT, 3, EVENT_HEIGHT * 2 + 2);

        context2d.setFillStyle(CssColor.make(0,0,0));
        context2d.setGlobalAlpha(1);
    }

    public void drawHighlight(Context2d context2d, double r) {

        int y = event.getY() + viewPort.getStart();
        CssColor selection = CssColor.make(100,100,100);
        context2d.setGlobalAlpha(.4);
        context2d.setFillStyle(selection);

        int width =  getEndX()-getX();

        if(width < labelWidth){
            width = labelWidth;
        }

        int dotWidth = (int) (r*4);

        context2d.fillRect(getX() - dotWidth/2 - 1, y - EVENT_HEIGHT - 1, width + dotWidth + 2, EVENT_HEIGHT * 2 + 4);

        context2d.setFillStyle(CssColor.make(0,0,0));
        context2d.setGlobalAlpha(1);
    }

    public void drawSearchHit(Context2d context2d, double r) {
        int y = event.getY() + viewPort.getStart();
        CssColor selection = CssColor.make(255,255,102);
        context2d.setGlobalAlpha(.4);
        context2d.setFillStyle(selection);

        int width =  getEndX()-getX();

        if(width < labelWidth){
            width = labelWidth;
        }

        int dotWidth = (int) (r*4);

        context2d.fillRect(getX() - dotWidth/2 - 1, y - EVENT_HEIGHT - 1, width + dotWidth + 2, EVENT_HEIGHT * 2 + 4);

        context2d.setFillStyle(CssColor.make(0,0,0));
        context2d.setGlobalAlpha(1);
    }
    

    private void drawSearchHighlight(Context2d context2d, double r) {
        int y = event.getY() + viewPort.getStart();
        CssColor selection = CssColor.make(51,102,255);
        context2d.setGlobalAlpha(.3);
        context2d.setFillStyle(selection);

        int width =  getEndX()-getX();

        if(width < labelWidth){
            width = labelWidth;
        }

        int dotWidth = (int) (r*4);

        context2d.fillRect(getX() - dotWidth/2, y - EVENT_HEIGHT, width + dotWidth, EVENT_HEIGHT * 2 + 2);

        context2d.setGlobalAlpha(.8);
        context2d.fillRect(getX() - dotWidth/2 - 3, y - EVENT_HEIGHT, 2, EVENT_HEIGHT * 2 + 2);

        context2d.setFillStyle(CssColor.make(0,0,0));
        context2d.setGlobalAlpha(1);
    }

    public boolean validateEnd() {
        return getEndX() > getX();
    }

    @Override
    public boolean hitTest(double x, double y) {

        int myY = event.getY() + viewPort.getStart();
        if(x  >= getX() - getDotR() *4 && (x <= getEndX() || x <= getX() + labelWidth)){
            if(y >= myY - EVENT_HEIGHT/2 && y <= (myY + EVENT_HEIGHT)){
                return true;
            }

        }

        return false;
    }

    @Override
    public void bind(Layer layer) {
        this.layer = layer;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public int compareTo(Object o) {
        return compareTo((SummaryEventRenderable)o);
    }

    public int compareTo(SummaryEventRenderable eventRenderable){

        if (this==eventRenderable)
            return 0;
        if (eventRenderable==null || eventRenderable.getStartTime() == null)
            return 1;
        if (this.getStartTime() == null){
            return -1;
        }
        long dt= getStartTime() - eventRenderable.getStartTime();
        if (dt==0)
            return 0;
        if (dt>0)
            return 1;
        return -1;
    }


    public int compareEnd(SummaryEventRenderable eventRenderable){
        if (this==eventRenderable)
            return 0;
        if (eventRenderable==null || eventRenderable.getEndTime() == null)
            return 1;
        if (this.getEndTime() == null){
            return -1;
        }
        long dt= getEndTime() - eventRenderable.getEndTime();
        if (dt==0)
            return 0;
        if (dt>0)
            return 1;
        return -1;
    }

    public void deregisterHandlers(){
        if(handlers == null){
            return;
        }

        for(HandlerRegistration handlerRegistration: handlers){
            handlerRegistration.removeHandler();
        }
    }

    public Long getEndTime(){
        return event.getEndTime();
    }

    public Long getStartTime() {
        return event.getStartTime();
    }

    public SummaryEventProxy getEvent() {
        return event;
    }

    public void setEvent(SummaryEventProxy event) {
        this.event = event;
    }

    public int getX() {
        return event.getX();
    }

    public void setX(int x) {
        event.setX(x);
    }

    public int getY() {
        return event.getY();
    }
    
    public int getOffsetY(){
        return event.getY() + viewPort.getStart();
    }

    public void setY(int y) {
        event.setY(y);
    }
    


    public int getEndX() {
        return event.getEndX();
    }

    public double getDotR()
    {
        return Math.max(1.0, Math.abs(event.getDrawableSize()))/2;
    }


    public boolean isShowLabel() {
        return showLabel;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
    }

    public boolean hasEnd(){
        if(event.getEndTime() == null){
            return false;
        }
        return true;
    }

    public boolean isShowDuration() {
        return showDuration;
    }

    public void setShowDuration(boolean showDuration) {
        this.showDuration = showDuration;
    }

    public void setSelected(boolean b) {
        event.setSelected(b);
    }

    public boolean isSelected() {
        return event.isSelected();
    }

    public boolean isHighlight() {
        return highlight;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }



    public void setHandlers(List<HandlerRegistration> handlers) {
        this.handlers = handlers;
    }



    public int getLabelWidth() {
        return labelWidth;
    }



    public void setLabelWidth(int labelWidth) {
        this.labelWidth = labelWidth;
    }

    public void setViewport(ViewPort timelineViewport) {
        this.viewPort = timelineViewport;
    }

}