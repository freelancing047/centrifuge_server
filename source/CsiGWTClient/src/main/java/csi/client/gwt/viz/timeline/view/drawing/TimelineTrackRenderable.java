package csi.client.gwt.viz.timeline.view.drawing;


import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import csi.client.gwt.viz.timeline.model.TimelineTrackModel;
import csi.client.gwt.viz.timeline.model.ViewPort;
import csi.client.gwt.viz.timeline.view.drawing.layer.ComplexLayer;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Renderable;
import csi.shared.core.visualization.timeline.TimelineTrack;

@SuppressWarnings("rawtypes")
public class TimelineTrackRenderable extends TrackRenderable implements Comparable, Renderable {

	
    private boolean dirty = false;
    boolean background = true;
    private int width;
    private int trackColor;

    private TimelineTrackModel trackModel;

    private boolean selectHover = false;

    
	public TimelineTrackRenderable(TimelineTrackModel trackModel){
		this.trackModel = trackModel;
		
        startHandlers();
	}


	@Override
	public void render(Context2d context2d) {
	    
	    if(timelineViewport == null){
	        return;
	    }
	    if(!trackModel.isVisible()){
	        return;
	    }
	    int y = trackModel.getStartY() + timelineViewport.getStart();
	    int endY = trackModel.getEndY() + timelineViewport.getStart();
	    if(endY < 0 || y > timelineViewport.getCurrentHeight()){
	        return;
	    }
		
		if (Math.abs(endY-y)>12)
		{				
			String label = trackModel.getLabel();
			//TODO: don't hardcode font
			context2d.setFont(DEFAULT_FONT);
			// draw background.
			TextMetrics textMetrics = context2d.measureText(label);
            
            context2d.setStrokeStyle(CssColor.make(237,237,237));
            if(background)
                drawBackground(context2d, y, endY);

            if(trackModel.isShadow()){
                drawShadow(context2d, y, endY);
            }

            context2d.setGlobalAlpha(1);
            int summaryY = y + TrackRenderable.FULL_SUMMARY_HEIGHT + 2;
            if(!trackModel.isCollapsed() && trackModel.isAllowCollapse()){
                if(trackModel.isGroupSpace() && trackModel.hasSummary())
                    drawDashedLine(context2d, summaryY, layer.getWidth()+10);
                else 
                    drawDashedLine(context2d, summaryY - TrackRenderable.EMPTY_SUMMARY_HEIGHT, layer.getWidth()+10);
            }
            
            if(trackColor == 0){
                context2d.setFillStyle(DEFAULT_LABEL_COLOR);
            } else {

                context2d.setFillStyle(DEFAULT_LABEL_COLOR);
            }
            context2d.setGlobalAlpha(.9);
            if(label.equals(TimelineTrack.NULL_TRACK)){
//
//                context2d.setFont(NO_VALUE_FONT);
//                context2d.fillText(NO_VALUE, 2, summaryY - 5 - DeferredLayoutCommand.FULL_SUMMARY_HEIGHT/2);
            } else {
                if(label.isEmpty()){

                    context2d.setFont(NO_VALUE_FONT);
                    context2d.fillText(NO_VALUE, 2, summaryY - 5 - TrackRenderable.FULL_SUMMARY_HEIGHT/2);
                } else {
                    //if(trackModel.isGroupSpace() && trackModel.hasSummary()){
                        context2d.fillText(label, 2, summaryY - 5 - TrackRenderable.FULL_SUMMARY_HEIGHT/2);
                   // } else {
                   //     context2d.fillText(label, 2, summaryY - 2 - DeferredLayoutCommand.EMPTY_SUMMARY_HEIGHT/2);
                   // }
                }
			}


			// if we have the hover, red dye the whole thing
            if (isBeforeSelectHover()) {
                context2d.setGlobalAlpha(.3);
                context2d.setFillStyle(HIGHLIGHT_SELECTION_COLOR);

                if(trackModel.isGroupSpace() && trackModel.hasSummary())
                    context2d.fillRect(-5, summaryY-TrackRenderable.FULL_SUMMARY_HEIGHT - 1, layer.getWidth()+10, TrackRenderable.FULL_SUMMARY_HEIGHT-1);
                else {
                    context2d.fillRect(-5, summaryY-TrackRenderable.FULL_SUMMARY_HEIGHT - 1, layer.getWidth()+10, TrackRenderable.EMPTY_SUMMARY_HEIGHT);
                }
            }
            if (isHighlight() && trackModel.isAllowCollapse() && !isBeforeSelectHover()){
                context2d.setGlobalAlpha(.05);
                context2d.setFillStyle(HIGHLIGHT_SUMMARY_COLOR);


                if(trackModel.isGroupSpace() && trackModel.hasSummary())
                    context2d.fillRect(-5, summaryY-TrackRenderable.FULL_SUMMARY_HEIGHT - 1, layer.getWidth()+10, TrackRenderable.FULL_SUMMARY_HEIGHT-1);
                else {
                    context2d.fillRect(-5, summaryY-TrackRenderable.FULL_SUMMARY_HEIGHT - 1, layer.getWidth()+10, TrackRenderable.EMPTY_SUMMARY_HEIGHT);
                }
            }


            
            context2d.setGlobalAlpha(1);
		}


	}

   
	@Override
	public boolean hitTest(double x, double y) {
	    setHighlight(false);
	    if(!trackModel.isVisible()){
	        return false;
	    }
	    if(timelineViewport == null){
	        return false;
	    }
	    
	    if(!layer.isEventPassingEnabled()){
	        return false;
	    }
	    
		int startY = trackModel.getStartY() + timelineViewport.getStart();
        int endY = trackModel.getEndY() + timelineViewport.getStart();
        if(endY < 0 || startY > timelineViewport.getCurrentHeight()){
            return false;
        }
        
        if(x <= layer.getWidth()){
            int summaryY;
            if(this.getTrackModel().isGroupSpace() && this.getTrackModel().hasSummary()){
                summaryY = startY + TrackRenderable.FULL_SUMMARY_HEIGHT + 2;
            } else {
                summaryY = startY + TrackRenderable.EMPTY_SUMMARY_HEIGHT + 2;
            }
            if(y > startY && y < summaryY){
                return true;
            }
        }
        
        
		return false;
	}

	private final ClickHandler clickHandler = new ClickHandler(){

        public void onClick(ClickEvent event){
//            toggleCollapse();
        }
    };
    
    public void deregisterHandlers(){
        if(handlers == null){
            return;
        }

        for(HandlerRegistration handlerRegistration: handlers){
            handlerRegistration.removeHandler();
        }
    }
	
	@Override
	public void bind(Layer layer) {
        this.layer = (ComplexLayer) layer;
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return dirty;
	}

	@Override
	public int compareTo(Object o) {
		return compareTo((TimelineTrackRenderable)o);
	}

	public int compareTo(TimelineTrackRenderable o) {
		return o.size()-size();
	}

	private int size() {
		return trackModel.getEvents().size();
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

    public void setTrackColor(int color) {
        this.trackColor = color;
    }

    public int getTrackColor() {
        return trackColor;
    }

    public boolean isBackground() {
        return background;
    }

    public void setBackground(boolean background) {
        this.background = background;
    }

    public void setViewport(ViewPort timelineViewport) {
        this.timelineViewport = timelineViewport;
    }

    public void registerHandler(HandlerRegistration handlerRegistration) {
        handlers.add(handlerRegistration);
    }

    public TimelineTrackModel getTrackModel() {
        return trackModel;
    }

    public void setTrackModel(TimelineTrackModel trackModel) {
        this.trackModel = trackModel;
    }

    public void setDirty(boolean dirty) {
        this.dirty = false;
    }

    public void toggleCollapse() {
        if(!trackModel.isAllowCollapse())
            return;
        
        trackModel.setCollapsed(!trackModel.isCollapsed());
        // Stop event, prevent highlighting of canvas
        dirty = true;
        //event.stopPropagation();
    }

    public void forceExpand(){
        if(!trackModel.isAllowCollapse()){
            return;
        }
        trackModel.setCollapsed(false);
        dirty = true;
    }


    public boolean isHighlight() {
        return trackModel.isHighlight();
    }

    public void setHighlight(boolean highlight) {
        trackModel.setHighlight(highlight);
    }
    
    public boolean isShadow() {
        return trackModel.isShadow();
    }

    public void setShadow(boolean highlight) {
        trackModel.setShadow(highlight);
    }

    public void setSelectHover(boolean isHover){
        selectHover = isHover;
    }

    public boolean isBeforeSelectHover(){
        return selectHover;
    }
}
