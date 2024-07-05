package csi.client.gwt.viz.timeline.view.drawing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.TextMetrics;

import csi.client.gwt.widget.drawing.BaseRenderable;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Renderable;
import csi.shared.core.visualization.timeline.Tooltip;

public class TooltipRenderable extends BaseRenderable {
    
       
    
    private Tooltip tooltip;
    private Renderable event;
    private Layer layer;
    
    private boolean right = true;
    private int maxHeight = 0;
    private int maxWidth = 0;
    
    private static final int MAX_WIDTH = 500;
    private static final int TOOLTIP_TEXT_HEIGHT = 14;
    public static final int TOOLTIP_PADDING = 6;

    public TooltipRenderable(Tooltip tooltip, DetailedEventRenderable eventRenderable){
        this.tooltip = tooltip;
        this.event = eventRenderable;
    }

    public TooltipRenderable(Renderable renderable) {
        this.event = renderable;
    }

    @Override
    public void render(Context2d context2d) {

        if(!(event instanceof DetailedEventRenderable)){
            layer.remove(this);
            return;
        }
        
        DetailedEventRenderable eventRenderable = (DetailedEventRenderable) event;
        
        //We only render tooltips when the user is hovering
        if(!((DetailedEventRenderable) eventRenderable).isHighlight()){
            layer.remove(this);
            return;
        }
      //check what has most room, right/left of object
        int totalWidth = eventRenderable.getX()+eventRenderable.getLabelWidth()+ TooltipRenderable.TOOLTIP_PADDING;
        if(maxWidth - totalWidth < eventRenderable.getX())
            this.setRight(false);
        
        HashMap<String, String> fields = tooltip.getFieldValuePairs();
        context2d.beginPath();
        
        double tooltipWidth = 0;
        double tooltipHeight = 0;

        context2d.setFont("normal normal normal 12px sans-serif");
        List<DrawingCommand> drawFields = new ArrayList<DrawingCommand>();
        for(String field: fields.keySet()){
            String value = fields.get(field);
            
            String displayString = field + ": " + value;
            TextMetrics textMetrics = context2d.measureText(displayString);
            double width = textMetrics.getWidth();
            
            if(width > MAX_WIDTH){
                width = MAX_WIDTH;
            }
            
            if(width > tooltipWidth){
                tooltipWidth = width;
            }
            
            DrawingCommand drawingCommand = new DrawingCommand();
            drawingCommand.setText(displayString);
            if(right){
                drawingCommand.setX(eventRenderable.getX()+eventRenderable.getLabelWidth()+TOOLTIP_PADDING);
            } else {
                drawingCommand.setX(eventRenderable.getX() - TOOLTIP_PADDING);
            }
            drawingCommand.setY((int) (tooltipHeight));
            drawFields.add(drawingCommand);
            
            tooltipHeight += TOOLTIP_TEXT_HEIGHT;
            
        }
        
        //Don't draw an empty tooltip for now, it's ugly
        if(drawFields.isEmpty()){
            return;
        }
        
        int start = eventRenderable.getOffsetY();
        if(tooltipHeight + eventRenderable.getOffsetY() > maxHeight){
            double diff = tooltipHeight + eventRenderable.getOffsetY() - maxHeight;
            start = (int) (eventRenderable.getOffsetY() - diff);
        }

        //context2d.setFillStyle(CssColor.make(250,250,250));
        //context2d.fillRect(event.getX() + event.getLabelWidth(), event.getOffsetY() - TOOLTIP_TEXT_HEIGHT, tooltipWidth, tooltipHeight);

        context2d.setFillStyle(CssColor.make(20,20,20));
        //context2d.strokeRect(event.getX() + event.getLabelWidth(), event.getOffsetY() - TOOLTIP_TEXT_HEIGHT, tooltipWidth, tooltipHeight);
        if(right){
            drawRoundedRectangle(context2d, eventRenderable.getX() + eventRenderable.getLabelWidth(), start - TOOLTIP_TEXT_HEIGHT, tooltipHeight + TOOLTIP_PADDING, tooltipWidth + TOOLTIP_PADDING * 2, 8);
        } else {
            drawRoundedRectangle(context2d, eventRenderable.getX() - eventRenderable.getDotR()*2 - tooltipWidth - TOOLTIP_PADDING * 2, start - TOOLTIP_TEXT_HEIGHT, tooltipHeight + TOOLTIP_PADDING, tooltipWidth + TOOLTIP_PADDING * 2, 8);   
        }
        for(DrawingCommand drawingCommand: drawFields){
            
            if(!right){
                drawingCommand.setX((int) (drawingCommand.getX() - tooltipWidth - eventRenderable.getDotR()*2));
            }
            
            drawingCommand.setY(drawingCommand.getY() + start);
            drawingCommand.draw(context2d);
            
        }
        context2d.stroke();
        
        
    }
    
    public class DrawingCommand{
        
        private String text;
        private int x;
        private int y;
        
        
        public String getText() {
            return text;
        }
        public void setText(String text) {
            this.text = text;
        }
        public int getX() {
            return x;
        }
        public void setX(int x) {
            this.x = x;
        }
        public int getY() {
            return y;
        }
        public void setY(int y) {
            this.y = y;
        }
        
        public void draw(Context2d context2d){

            context2d.fillText(text, x, y);
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

    public Tooltip getTooltip() {
        return tooltip;
    }

    public void setTooltip(Tooltip tooltip) {
        this.tooltip = tooltip;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public void setMaxHeight(int height) {
        this.maxHeight = height;
    }

    public void setMaxWidth(int width) {
        this.maxWidth  = width;
    }

}
