package csi.client.gwt.viz.graph.window.legend;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.dnd.core.client.*;


/**
 * Drag and Drop Fluid Container class.
 *
 * Used for drag and drop legend logic on the map and relgraph.
 *
 */
public class DndFluidContainer extends FluidContainer {

    private static final int INVALID_DROP_INDEX = -1;
    protected static final int SCROLL_INCREMENT = 25;
    private int mouseY = 0;
    private int scrollY = 0;
    private DropCompleteEventHandler dropHandler = null;
    private boolean dragging = false;
    private XElement parent;
    private RepeatingCommand scrollUp = null;
    private RepeatingCommand scrollDown = null;
    
    public DndFluidContainer(){
        super();
        
        this.addDomHandler(new MouseMoveHandler() {
            
            @Override
            public void onMouseMove(MouseMoveEvent event) {
                
                mouseY = event.getRelativeY(DndFluidContainer.this.getElement());
                
                if(dragging && parent != null){
                    scrollY = event.getRelativeY(parent);
                    if(scrollY <= SCROLL_INCREMENT){
                        scrollTop();
                    } else if(scrollY >= parent.getOffsetHeight() - SCROLL_INCREMENT){
                        scrollDown();
                    }
                }
            }
        }, MouseMoveEvent.getType());
        
        DropTarget target = new DropTarget(this){


            @Override
            protected void onDragDrop(DndDropEvent event) {
                if(isDragging()){
                    dragging = false;
                    super.onDragDrop(event);
                                    
                    Widget dragWidget = ((Widget)(event.getData())).asWidget();
                    int location = DndFluidContainer.this.addItemAtMouseLocation(dragWidget);
                    
                    //verifyUniqueContainer(dragWidget, location);
                    
                    if(dropHandler != null && location != INVALID_DROP_INDEX){
                        DropCompleteEvent dropCompleteEvent = new DropCompleteEvent(dragWidget, location);
                        dropHandler.onDropComplete(dropCompleteEvent);
                    }
                }
            }


            
        };

        target.setAllowSelfAsSource(true);
    }


    /**
     * determine the index of the drop, based on the mouse position and top of the element.
     *
     * @param dragWidget
     * @return Index of the location dropped, or INVALID_DROP_INDEX
     */
    public int addItemAtMouseLocation(Widget dragWidget){
        int widgetCount = this.getWidgetCount();
        
        for(int ii = widgetCount-1; ii >= 0; ii--){
            Widget widget = this.getWidget(ii);
            int top = widget.getElement().getOffsetTop();
            if(top < mouseY){
                // last element special case
                if(top + widget.getElement().getOffsetHeight() < mouseY){
                    addAtIndex(dragWidget.asWidget(), widgetCount);
                    return ii;
                }

                addAtIndex(dragWidget.asWidget(), ii);
                return ii;
            }
        }
        
        return INVALID_DROP_INDEX;
    }
    
    /**
     * Adds a new child widget to the panel.
     *
     * @param w the widget to be added
     */
    public void addAtIndex(Widget w, int index) {
        insert(w, this.getElement(), index, true);
    }
    
    public void addNoHandler(Widget w){
        setDragging(false);
        super.add(w);
    }
    

    @Override
    public void add(final Widget w){
        setDragging(false);
        super.add(w);
        DragSource dragSource = new DragSource(w){
            @Override
            protected void onDragStart(DndDragStartEvent event) {
                super.onDragStart(event);
                event.setData(w);
                Composite w1 = (Composite) w;
                event.getStatusProxy().update(SafeHtmlUtils.fromString(w1.getTitle()));
                setDragging(true);
            }
        };
    }

    public void addDropHandler(DropCompleteEventHandler dropHandler) {
        this.dropHandler  = dropHandler;
    }


    public boolean isDragging() {
        return dragging;
    }


    
    public void setScrollParent(XElement display){
        this.parent = display;
    }
    
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }


    public void scrollTop() {
        
        if(scrollUp == null){
            scrollUp = new RepeatingCommand() {
                
                @Override
                public boolean execute() {
                    try{
                    int scrollTop = 0;
                    scrollTop = DndFluidContainer.this.getElement().getScrollTop();
                    scrollTop = scrollTop - SCROLL_INCREMENT;
                    if(scrollTop < 0){
                        scrollTop = 0;
                    }
                    DndFluidContainer.this.getElement().setScrollTop(scrollTop);
                    } catch(Exception e){
                        
                    } finally {
                        scrollUp = null;
                    }
                    if(isDragging() && scrollY <= SCROLL_INCREMENT){
                        return true;
                    }
                    return false;
                }
            };
        Scheduler.get().scheduleFixedDelay(scrollUp, 50);
        }
        
    }

    public void scrollDown() {
    
    if(scrollDown == null){
        scrollDown = new RepeatingCommand() {
            
            @Override
            public boolean execute() {
                try{
                int scrollTop = 0;
                scrollTop = DndFluidContainer.this.getElement().getScrollTop();
                scrollTop = scrollTop + SCROLL_INCREMENT;
                DndFluidContainer.this.getElement().setScrollTop(scrollTop);
                } catch(Exception e){
                    
                } finally {
                    scrollDown = null;
                }
                
                if(isDragging() && scrollY >= parent.getOffsetHeight() - SCROLL_INCREMENT){
                    return true;
                }

                return false;
            }
        };
    Scheduler.get().scheduleFixedDelay(scrollDown, 50);
    }
    
}

}
