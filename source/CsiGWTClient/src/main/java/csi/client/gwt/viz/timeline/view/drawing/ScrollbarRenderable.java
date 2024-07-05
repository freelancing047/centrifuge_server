package csi.client.gwt.viz.timeline.view.drawing;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import csi.client.gwt.viz.timeline.model.Scrollbar;
import csi.client.gwt.viz.timeline.model.ViewPort;
import csi.client.gwt.widget.drawing.BaseRenderable;
import csi.client.gwt.widget.drawing.Layer;

public class ScrollbarRenderable extends BaseRenderable implements Comparable{

    private static final int MIN_SIZE = 10;
    private Layer layer;
    private Scrollbar scrollbar;
    private List<DetailedEventRenderable> searchHits = null;

    public static CssColor SCROLLBAR_COLOR = CssColor.make(240,240,240);
    public static CssColor SCROLLBAR_DRAG_COLOR = CssColor.make(200,200,200);
    public static CssColor SCROLLBAR_BUTTON_COLOR = CssColor.make(40,40,40);
    public static CssColor SCROLLBAR_HIGHLIGHT_COLOR = CssColor.make(204,204,0);
    
    
    private boolean isDragging = false;
    private int mouseY;
    private List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();
    
    private double scrollStart;
    private double scrollEnd;
    private int minOffset = 0;
    private double offsetY;
    private boolean visible = false;
    
    public ScrollbarRenderable(ViewPort timelineViewport){
        scrollbar = new Scrollbar(timelineViewport);
    }
    
    @Override
    public void render(Context2d context2d) {

        if(scrollbar == null || scrollbar.getTimelineViewport() == null){
            return;
        }
        
        if(scrollbar.getHeight() >= Math.floor(scrollbar.getTimelineViewport().getTotalHeight())){
            return;
        }
        
        if(scrollbar.getHeight() == 0){
            return;
        }
        //Scrollbackground
//        context2d.beginPath();
        context2d.setFillStyle(SCROLLBAR_COLOR);
        context2d.fillRect(scrollbar.getX(), 0, Scrollbar.SCROLLBAR_SIZE, scrollbar.getHeight());
//        context2d.save();
//        context2d.closePath();
//        context2d.clip();
        
        //Top and bottom scroll buttons
        context2d.fillRect(scrollbar.getX(), 0, Scrollbar.SCROLLBAR_SIZE, Scrollbar.SCROLLBAR_SIZE);
        context2d.fillRect(scrollbar.getX(), scrollbar.getHeight()-Scrollbar.SCROLLBAR_SIZE, Scrollbar.SCROLLBAR_SIZE, Scrollbar.SCROLLBAR_SIZE);
        context2d.beginPath();
        //Draw top button arrow
        context2d.setStrokeStyle(SCROLLBAR_BUTTON_COLOR);
        context2d.moveTo(scrollbar.getX() + Scrollbar.SCROLLBAR_SIZE * .5, scrollbar.getHeight() - Scrollbar.SCROLLBAR_SIZE * .40);
        context2d.lineTo(scrollbar.getX() + Scrollbar.SCROLLBAR_SIZE * .25, scrollbar.getHeight() - Scrollbar.SCROLLBAR_SIZE * .65);
        context2d.moveTo(scrollbar.getX() + Scrollbar.SCROLLBAR_SIZE * .5, scrollbar.getHeight() - Scrollbar.SCROLLBAR_SIZE * .40);
        context2d.lineTo(scrollbar.getX() + Scrollbar.SCROLLBAR_SIZE * .75, scrollbar.getHeight() - Scrollbar.SCROLLBAR_SIZE * .65);
        context2d.moveTo(scrollbar.getX() + Scrollbar.SCROLLBAR_SIZE * .5, scrollbar.getHeight() - Scrollbar.SCROLLBAR_SIZE * .40 + 1);
        context2d.lineTo(scrollbar.getX() + Scrollbar.SCROLLBAR_SIZE * .25, scrollbar.getHeight() - Scrollbar.SCROLLBAR_SIZE * .65 + 1);
        context2d.moveTo(scrollbar.getX() + Scrollbar.SCROLLBAR_SIZE * .5, scrollbar.getHeight() - Scrollbar.SCROLLBAR_SIZE * .40 + 1);
        context2d.lineTo(scrollbar.getX() + Scrollbar.SCROLLBAR_SIZE * .75, scrollbar.getHeight() - Scrollbar.SCROLLBAR_SIZE * .65 + 1);
        
        //Draw bottom button arrow
        context2d.moveTo(scrollbar.getX() + Scrollbar.SCROLLBAR_SIZE * .5, Scrollbar.SCROLLBAR_SIZE * .40);
        context2d.lineTo(scrollbar.getX() + Scrollbar.SCROLLBAR_SIZE * .25, Scrollbar.SCROLLBAR_SIZE * .65);
        context2d.moveTo(scrollbar.getX() + Scrollbar.SCROLLBAR_SIZE * .5, Scrollbar.SCROLLBAR_SIZE * .40);
        context2d.lineTo(scrollbar.getX() + Scrollbar.SCROLLBAR_SIZE * .75, Scrollbar.SCROLLBAR_SIZE * .65);
        context2d.moveTo(scrollbar.getX() + Scrollbar.SCROLLBAR_SIZE * .5, Scrollbar.SCROLLBAR_SIZE * .40 + 1);
        context2d.lineTo(scrollbar.getX() + Scrollbar.SCROLLBAR_SIZE * .25, Scrollbar.SCROLLBAR_SIZE * .65 + 1);
        context2d.moveTo(scrollbar.getX() + Scrollbar.SCROLLBAR_SIZE * .5, Scrollbar.SCROLLBAR_SIZE * .40 + 1);
        context2d.lineTo(scrollbar.getX() + Scrollbar.SCROLLBAR_SIZE * .75, Scrollbar.SCROLLBAR_SIZE * .65 + 1);
        context2d.stroke();
        
        context2d.closePath();
        
        //Draw scrollbar drag button
        
        double scrollRatio = (double)(scrollbar.getHeight()-Scrollbar.SCROLLBAR_SIZE * 2)/(double)scrollbar.getHeight();
        scrollStart = scrollbar.calculateStart() * scrollRatio + Scrollbar.SCROLLBAR_SIZE;
        scrollEnd = scrollbar.calculateEnd() * scrollRatio + Scrollbar.SCROLLBAR_SIZE;
        
        if(scrollEnd - scrollStart < MIN_SIZE){
            minOffset = (int) (MIN_SIZE - (scrollEnd - scrollStart));
            scrollRatio = (double)(scrollbar.getHeight()-(Scrollbar.SCROLLBAR_SIZE * 2) - minOffset)/(double)scrollbar.getHeight();

            scrollStart = scrollbar.calculateStart() * scrollRatio + Scrollbar.SCROLLBAR_SIZE;
            scrollEnd = scrollbar.calculateEnd() * scrollRatio + Scrollbar.SCROLLBAR_SIZE + minOffset;
        } else {
            minOffset = 0;
        }
        


        context2d.setFillStyle(SCROLLBAR_DRAG_COLOR);
        context2d.fillRect(scrollbar.getX(), scrollStart, Scrollbar.SCROLLBAR_SIZE, scrollEnd-scrollStart);
        
        double trueHeightRatio = scrollbar.trueHeightRatio() * scrollRatio;
        if(searchHits != null){
            for(DetailedEventRenderable renderable: searchHits){
                if(renderable.getEvent().isSearchHit()){
                    int y = renderable.getY();
                    if(renderable.getTrack().isCollapsed()){
                        y = renderable.getTrack().getStartY();
                    }
                    drawSearchHighlight(y, context2d, trueHeightRatio);
                }
            }
        }

        context2d.setGlobalAlpha(1);
        
        
//        context2d.restore();
    }

    private void drawSearchHighlight(int y, Context2d context2d, double scrollRatio) {
        context2d.setGlobalAlpha(.5);
        context2d.setFillStyle(SCROLLBAR_HIGHLIGHT_COLOR);
        context2d.fillRect(scrollbar.getX(), y * scrollRatio + Scrollbar.SCROLLBAR_SIZE, Scrollbar.SCROLLBAR_SIZE, 2);
    }

    @Override
    public boolean hitTest(double x, double y) {
        //We shortcut the drag here
        if(isDragging){
            return true;
        }
        if(x < scrollbar.getX() || y > scrollbar.getHeight()){
            return false;
        }
        
        return true;
    }

    @Override
    public void bind(Layer layer) {
        this.layer = layer;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    public Scrollbar getScrollbar() {
        return scrollbar;
    }

    public void setScrollbar(Scrollbar scrollbar) {
        this.scrollbar = scrollbar;
    }
    
    public boolean mouseMove(MouseMoveEvent event){
        
        if(!isDragging){
            return false;
        }
        
        if(event.getY() - Scrollbar.SCROLLBAR_SIZE == mouseY){
            return false;
        }
        
        mouseY = event.getY() - Scrollbar.SCROLLBAR_SIZE;
        
        int scrollY = (int) (mouseY - offsetY);
        
        int scrollHeight = scrollbar.getHeight() - Scrollbar.SCROLLBAR_SIZE * 2 - minOffset;
        
        if(scrollY < 0){
            scrollY = 0;
        } else if(scrollY + (scrollEnd - scrollStart) - minOffset > scrollHeight){
            scrollY = (int) (scrollHeight - (scrollEnd - scrollStart - minOffset));
        }
        
        if(scrollHeight == 0){
            return true;
        }
        double percent = (double)scrollY/scrollHeight;
        
        
        
        scrollbar.getTimelineViewport().setViewport(percent);
        
        return true;
    }

    public boolean mouseUp(MouseUpEvent event) {
        int y;
       if(isDragging){
           isDragging = false;
       } else {
           if(pageScroll(event.getY())){
              return true; 
           }
       }
       y = event.getY();
       boolean doUpdate = false;
       
       //Top button scroll
       if(y <= Scrollbar.SCROLLBAR_SIZE){
           if(scrollbar.calculateStart() > 0){
               scrollbar.scroll(1);
               doUpdate = true;
           }
       } 
       //Bottom button scroll
       else if(y >= scrollbar.getHeight() - Scrollbar.SCROLLBAR_SIZE){
           if(Math.abs(scrollbar.getTimelineViewport().getStart()) + scrollbar.getHeight() <= scrollbar.getTimelineViewport().getTotalHeight()){
               scrollbar.scroll(-1);
               doUpdate = true;
           }
       }
       
       
       
       return doUpdate;
    }

    private boolean pageScroll(int y) {
        //Page Up
        if(y < scrollStart && y > Scrollbar.SCROLLBAR_SIZE){
            scrollbar.page(1);
            return true;
        }
        //Page Down
        if(y > scrollEnd && y < scrollbar.getHeight() - Scrollbar.SCROLLBAR_SIZE){
            scrollbar.page(-1);
            return true;
        }
        return false;
    }

    public void deregisterHandlers(){
        
        for(HandlerRegistration handlerRegistration: handlers){
            handlerRegistration.removeHandler();
        }
    }
    
    public HandlerRegistration addMouseUpHandler(MouseUpHandler handler){
        
        HandlerRegistration handlerRegistration = super.addMouseUpHandler(handler);
        handlers.add(handlerRegistration);
        return handlerRegistration;
    }
    
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler){
        
        HandlerRegistration handlerRegistration = super.addMouseDownHandler(handler);
        handlers.add(handlerRegistration);
        return handlerRegistration;
    }

    public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler){
        
        HandlerRegistration handlerRegistration = super.addMouseMoveHandler(handler);
        handlers.add(handlerRegistration);
        return handlerRegistration;
    }

    public boolean mouseDown(MouseDownEvent event) {
        int y = event.getY();
        boolean doUpdate = false;
        mouseY = y;
        offsetY = y - scrollStart;
        
        //Middle area clicked
        if(y < scrollEnd && y > scrollStart){
            isDragging=true;
        }
        
        return doUpdate;
    }

    public boolean mouseOut(MouseOutEvent event) {
        if(isDragging){
            isDragging = false;
        } else {
            return false;
        }
        int y = event.getY();
        boolean doUpdate = false;
        
        //Top button scroll
        if(y <= Scrollbar.SCROLLBAR_SIZE){
            if(scrollbar.calculateStart() > 0){
                scrollbar.scroll(1);
                doUpdate = true;
            }
        } 
        //Bottom button scroll
        else if(y >= scrollbar.getHeight() - Scrollbar.SCROLLBAR_SIZE){
            if(Math.abs(scrollbar.getTimelineViewport().getStart()) + scrollbar.getHeight() <= scrollbar.getTimelineViewport().getTotalHeight()){
                scrollbar.scroll(-1);
                doUpdate = true;
            }
        }
        
        return doUpdate;
    }

    public List<DetailedEventRenderable> getSearchHits() {
        return searchHits;
    }

    public void setSearchHits(List<DetailedEventRenderable> searchHits) {
        this.searchHits = searchHits;
    }

    public void adjustIfNecessary() {

            if(Math.abs(scrollbar.getTimelineViewport().getStart()) + scrollbar.getHeight() > scrollbar.getTimelineViewport().getTotalHeight()){
                scrollbar.getTimelineViewport().setStart((int) (scrollbar.getTimelineViewport().getTotalHeight() - scrollbar.getHeight()) * -1);
            }
            
            if(scrollbar.getHeight() >= Math.floor(scrollbar.getTimelineViewport().getTotalHeight())){
                scrollbar.getTimelineViewport().setStart(0);
            }
    }
}