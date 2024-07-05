package csi.client.gwt.viz.timeline.view.drawing;


import java.util.ArrayList;
import java.util.List;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.timeline.model.TimelineTrackModel;
import csi.client.gwt.viz.timeline.model.ViewPort;
import csi.client.gwt.viz.timeline.view.drawing.layer.ComplexLayer;
import csi.client.gwt.widget.drawing.BaseRenderable;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Renderable;

@SuppressWarnings("rawtypes")
public abstract class TrackRenderable extends BaseRenderable implements Comparable, Renderable {

	public static final int LABEL_START_X = 2;
    private static final double SHADOW_ALPHA = .25;
    protected static final int DASH_WIDTH = 2;
    protected static final CssColor SEPARATOR_COLOR = CssColor.make(225,225,225);
    protected static final CssColor DEFAULT_LABEL_COLOR = CssColor.make(60,60,60);
    protected static final CssColor HIGHLIGHT_SUMMARY_COLOR = CssColor.make(144,180,212);
    protected static final CssColor HIGHLIGHT_SELECTION_COLOR = CssColor.make(255,133,10);
    protected static final CssColor STRIPE_COLOR = CssColor.make(240,240,240);
    protected static final String DEFAULT_FONT = "12pt serif";
    protected static final String HIGHLIGHT_FONT = "800 12pt serif";
    protected static final String NO_VALUE_FONT = "italic 12pt serif";
    public static final String NO_VALUE = CentrifugeConstantsLocator.get().timeline_trackRenderable_noValue();
    
    public static final int FULL_SUMMARY_HEIGHT = TimelineTrackModel.CELL_HEIGHT *2;
    public static final int EMPTY_SUMMARY_HEIGHT = TimelineTrackModel.CELL_HEIGHT;

    protected ComplexLayer layer;
    protected ViewPort timelineViewport;

    private boolean selectHover = false;

    protected List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();
    protected int labelStartY = 0;
    private double labelWidth;
    
	public TrackRenderable(){
        //startHandlers();
	}

    public void startHandlers() {
        handlers.add(this.addClickHandler(clickHandler));
    }

	@Override
	public void render(Context2d context2d) {
//	    
//	    if(timelineViewport == null){
//	        return;
//	    }
//	   		
//			String label = trackModel.getLabel();
//			//TODO: don't hardcode font
//			context2d.setFont(DEFAULT_FONT);
//			// draw background.
//			TextMetrics textMetrics = context2d.measureText(label);
//            
//            context2d.setStrokeStyle(CssColor.make(237,237,237));
////            if(background)
//                drawBackground(context2d);
//            
//            if(trackModel.isShadow()){
//                drawShadow(context2d);
//            }

//            context2d.setGlobalAlpha(1);
//            int summaryY = y + TrackRenderable.FULL_SUMMARY_HEIGHT + 2;
//            if(!trackModel.isCollapsed() && trackModel.isAllowCollapse()){
//                if(trackModel.isGroupSpace() && trackModel.hasSummary())
//                    drawDashedLine(context2d, summaryY, layer.getWidth()+10);
//                else 
//                    drawDashedLine(context2d, summaryY - TrackRenderable.EMPTY_SUMMARY_HEIGHT, layer.getWidth()+10);
//            }
            
//            if(trackColor == 0){
//                context2d.setFillStyle(DEFAULT_LABEL_COLOR);
//            } else {
//
//                setFillFromTrackColor(context2d);
//            }
////            context2d.setGlobalAlpha(.9);
//            if(label.equals(TimelineTrack.NULL_TRACK)){
////
////                context2d.setFont(NO_VALUE_FONT);
////                context2d.fillText(NO_VALUE, 2, summaryY - 5 - DeferredLayoutCommand.FULL_SUMMARY_HEIGHT/2);
//            } else {
//                if(label.isEmpty()){
//
//                    drawEmptyLabel(context2d);
//                } else {
//                    //if(trackModel.isGroupSpace() && trackModel.hasSummary()){
//                    //    drawLabel(context2d, label, y);
//                   // } else {
//                   //     context2d.fillText(label, 2, summaryY - 2 - DeferredLayoutCommand.EMPTY_SUMMARY_HEIGHT/2);
//                   // }
//                }
//			}


			// if we have the hover, red dye the whole thing
//            if (isBeforeSelectHover()) {
//                context2d.setGlobalAlpha(.3);
//                context2d.setFillStyle(HIGHLIGHT_SELECTION_COLOR);
//
//                if(trackModel.isGroupSpace() && trackModel.hasSummary())
//                    context2d.fillRect(-5, summaryY-TrackRenderable.FULL_SUMMARY_HEIGHT - 1, layer.getWidth()+10, TrackRenderable.FULL_SUMMARY_HEIGHT-1);
//                else {
//                    context2d.fillRect(-5, summaryY-TrackRenderable.FULL_SUMMARY_HEIGHT - 1, layer.getWidth()+10, TrackRenderable.EMPTY_SUMMARY_HEIGHT);
//                }
//            }
//            if (isHighlight() && trackModel.isAllowCollapse() && !isBeforeSelectHover()){
//                context2d.setGlobalAlpha(.05);
//                context2d.setFillStyle(HIGHLIGHT_SUMMARY_COLOR);
//
//
//                if(trackModel.isGroupSpace() && trackModel.hasSummary())
//                    context2d.fillRect(-5, summaryY-TrackRenderable.FULL_SUMMARY_HEIGHT - 1, layer.getWidth()+10, TrackRenderable.FULL_SUMMARY_HEIGHT-1);
//                else {
//                    context2d.fillRect(-5, summaryY-TrackRenderable.FULL_SUMMARY_HEIGHT - 1, layer.getWidth()+10, TrackRenderable.EMPTY_SUMMARY_HEIGHT);
//                }
//            }


            
            context2d.setGlobalAlpha(1);
		}

    public void drawEmptyLabel(Context2d context2d, int y) {
        context2d.setFont(NO_VALUE_FONT);
        context2d.fillText(NO_VALUE, 2, y - 5 - TrackRenderable.FULL_SUMMARY_HEIGHT/2);
    }

    public void drawLabel(Context2d context2d, String label, int y, boolean highlightLabel) {

        context2d.save();

        if(highlightLabel) {
            context2d.setFont(HIGHLIGHT_FONT);
            context2d.setFillStyle(CssColor.make("#216893"));
        } else {
            context2d.setFont(DEFAULT_FONT);
            context2d.setFillStyle(DEFAULT_LABEL_COLOR);
        }
        TextMetrics textMetrics = context2d.measureText(label);
        setLabelWidth(textMetrics.getWidth());
        labelStartY = y - TrackRenderable.FULL_SUMMARY_HEIGHT/2;
        context2d.setStrokeStyle(CssColor.make("#FFFFFF"));
        context2d.setLineWidth(5);
        context2d.setLineCap(Context2d.LineCap.ROUND);
        context2d.setMiterLimit(2);
        context2d.strokeText(label, LABEL_START_X, labelStartY);
        context2d.fillText(label, LABEL_START_X, labelStartY);
        context2d.restore();
    }

    public void setFillFromTrackColor(Context2d context2d, int trackColor) {
        int red = (trackColor >> 16) & 0xFF;
        int green = (trackColor >> 8) & 0xFF;
        int blue = trackColor & 0xFF;

        context2d.setFillStyle(CssColor.make(red,green,blue));
    }

    public void drawBackground(Context2d context2d, int y, int endY) {
        //magic numbers are to ensure we draw full viewport colors
        context2d.save();
        context2d.setFillStyle(CssColor.make(84,84,84));
        context2d.setStrokeStyle(CssColor.make(40,40,40));
        context2d.setLineWidth(.3);
        context2d.strokeRect(-5, y, timelineViewport.getCurrentWidth()+10, endY - y);
        context2d.restore();
    }

    public void drawShadow(Context2d context2d, int y, int endY) {
        context2d.save();
        context2d.setGlobalAlpha(SHADOW_ALPHA);
        context2d.setFillStyle(STRIPE_COLOR);

        //magic numbers are to ensure we draw full viewport colors
        context2d.fillRect(-5, y, timelineViewport.getCurrentWidth()+10, endY - y);
        context2d.restore();
    }


    public void drawDashedLine(Context2d context2d, int y, int width) {
        context2d.setStrokeStyle(SEPARATOR_COLOR);
        int x = 0;

        context2d.beginPath();
        context2d.moveTo(x, y);
        boolean line = true;
        while(x < width){

            x=x+DASH_WIDTH;
            
            if(line)
                context2d.lineTo(x, y);
            else
                context2d.moveTo(x, y);
            line = !line;

        }

        context2d.stroke();
        context2d.closePath();
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



	
    public void setViewport(ViewPort timelineViewport) {
        this.timelineViewport = timelineViewport;
    }

    public void registerHandler(HandlerRegistration handlerRegistration) {
        handlers.add(handlerRegistration);
    }

    public void setSelectHover(boolean isHover){
        selectHover = isHover;
    }

    public boolean isBeforeSelectHover(){
        return selectHover;
    }

    public double getLabelWidth() {
        return labelWidth;
    }

    public void setLabelWidth(double labelWidth) {
        this.labelWidth = labelWidth;
    }
}
