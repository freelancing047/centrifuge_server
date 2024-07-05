package csi.client.gwt.viz.map.legend;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.DragSource;
import com.sencha.gxt.dnd.core.client.DropTarget;
import csi.client.gwt.viz.graph.window.legend.DropCompleteEvent;
import csi.client.gwt.viz.graph.window.legend.DropCompleteEventHandler;

/**
 * Drag and Drop Fluid Container class.
 * <p>
 * Used for drag and drop legend logic on the map and relgraph.
 */
public class MapDndFluidContainer extends FluidContainer {

    private static final int INVALID_DROP_INDEX = -1;
    private static final int SCROLL_INCREMENT = 25;
    private int mouseY = 0;
    private int scrollY = 0;
    private DropCompleteEventHandler dropHandler = null;
    private boolean dragging = false;
    private XElement parent;
    private RepeatingCommand scrollUp = null;
    private RepeatingCommand scrollDown = null;
    private boolean isLinkMessageUp = false;

    MapDndFluidContainer() {
        super();

        this.addDomHandler(event -> {
            mouseY = event.getRelativeY(MapDndFluidContainer.this.getElement());
            if (dragging && parent != null) {
                scrollY = event.getRelativeY(parent);
                if (scrollY <= SCROLL_INCREMENT) {
                    scrollTop();
                } else if (scrollY >= parent.getOffsetHeight() - SCROLL_INCREMENT) {
                    scrollDown();
                }
            }
        }, MouseMoveEvent.getType());

        DropTarget target = new DropTarget(this) {
            @Override
            protected void onDragDrop(DndDropEvent event) {
                if (isDragging()) {
                    dragging = false;
                    super.onDragDrop(event);

                    Widget dragWidget = ((Widget) (event.getData())).asWidget();
                    int location = MapDndFluidContainer.this.addItemAtMouseLocation(dragWidget);

                    // verifyUniqueContainer(dragWidget, location);

                    if (dropHandler != null && location != INVALID_DROP_INDEX) {
                        DropCompleteEvent dropCompleteEvent = new DropCompleteEvent(dragWidget, location);
                        dropHandler.onDropComplete(dropCompleteEvent);
                    }
                }
            }
        };

        target.setAllowSelfAsSource(true);
    }

    void setLinkMessageUp(boolean value) {
        isLinkMessageUp = value;
    }

    /**
     * determine the index of the drop, based on the mouse position and top of
     * the element.
     *
     * @param dragWidget
     * @return Index of the location dropped, or INVALID_DROP_INDEX
     */
    private int addItemAtMouseLocation(Widget dragWidget) {
        int widgetCount = this.getWidgetCount();
        for (int ii = widgetCount - 1; ii >= 0; ii--) {
            Widget widget = this.getWidget(ii);
            int top = widget.getElement().getOffsetTop();
            if (top < mouseY) {
                // last element special case
                if (top + widget.getElement().getOffsetHeight() < mouseY) {
                    addAtIndex(dragWidget.asWidget(), widgetCount);
                    return ii;
                }
                if (isLinkMessageUp && ii == 0)
                    ii = 1;
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
    private void addAtIndex(Widget w, int index) {
        insert(w, this.getElement(), index, true);
    }

    void addNoHandler(Widget w) {
        setDragging(false);
        super.add(w);
    }

    @Override
    public void add(final Widget w) {
        setDragging(false);
        super.add(w);
        DragSource dragSource = new DragSource(w) {
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
        this.dropHandler = dropHandler;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    void setScrollParent(XElement display) {
        this.parent = display;
    }

    private void scrollTop() {
        if (scrollUp == null) {
            scrollUp = () -> {
                try {
                    int scrollTop = MapDndFluidContainer.this.getElement().getScrollTop() - SCROLL_INCREMENT;
                    if (scrollTop < 0) {
                        scrollTop = 0;
                    }
                    MapDndFluidContainer.this.getElement().setScrollTop(scrollTop);
                } catch (Exception ignored) {
                } finally {
                    scrollUp = null;
                }
                return isDragging() && scrollY <= SCROLL_INCREMENT;
            };
            Scheduler.get().scheduleFixedDelay(scrollUp, 50);
        }
    }

    private void scrollDown() {
        if (scrollDown == null) {
            scrollDown = () -> {
                try {
                    int scrollTop = MapDndFluidContainer.this.getElement().getScrollTop() + SCROLL_INCREMENT;
                    MapDndFluidContainer.this.getElement().setScrollTop(scrollTop);
                } catch (Exception ignored) {
                } finally {
                    scrollDown = null;
                }
                return isDragging() && scrollY >= parent.getOffsetHeight() - SCROLL_INCREMENT;
            };
            Scheduler.get().scheduleFixedDelay(scrollDown, 50);
        }
    }
}
