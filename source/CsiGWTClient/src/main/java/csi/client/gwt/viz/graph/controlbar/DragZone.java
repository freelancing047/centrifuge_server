package csi.client.gwt.viz.graph.controlbar;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.fx.client.DragCancelEvent;
import com.sencha.gxt.fx.client.DragEndEvent;
import com.sencha.gxt.fx.client.DragHandler;
import com.sencha.gxt.fx.client.DragMoveEvent;
import com.sencha.gxt.fx.client.DragStartEvent;
import com.sencha.gxt.fx.client.Draggable;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;

public class DragZone implements IsWidget {

    private HorizontalLayoutContainer container;
    private LayoutPanel layoutPanel;
    private HTMLPanel leftzone;
    private HTMLPanel dragArea;
    private HTMLPanel rightzone;
    private double right;
    private double left;
    private Draggable draggable;
    private List<DragHandler> dragHandlers = Lists.newArrayList();

    DragZone() {
        layoutPanel = new LayoutPanel();
        layoutPanel.setHeight("100%");
        layoutPanel.setWidth("100%");
        //A few attempt to keep this invisible
        layoutPanel.getElement().getStyle().setZIndex(-1);
        layoutPanel.getElement().getStyle().setOpacity(0);
        layoutPanel.addStyleName("overlay");//NON-NLS

        container = new HorizontalLayoutContainer();
        layoutPanel.add(container);

        {
            HorizontalLayoutContainer.HorizontalLayoutData data = new HorizontalLayoutContainer.HorizontalLayoutData(1, 1);
            leftzone = new HTMLPanel("");
            leftzone.getElement().getStyle().setBackgroundColor("red");//NON-NLS
            container.add(leftzone, data);
        }
        {
            HorizontalLayoutContainer.HorizontalLayoutData data = new HorizontalLayoutContainer.HorizontalLayoutData(1, 1);
            dragArea = new HTMLPanel("");
            dragArea.getElement().getStyle().setBackgroundColor("green");//NON-NLS
            container.add(dragArea, data);
        }
        {
            HorizontalLayoutContainer.HorizontalLayoutData data = new HorizontalLayoutContainer.HorizontalLayoutData(1, 1);
            rightzone = new HTMLPanel("");
            rightzone.getElement().getStyle().setBackgroundColor("red");//NON-NLS
            container.add(rightzone, data);
        }

        setLeft(0);

        setRight(1);
    }

    private void setRight(double right) {
        this.right = right;
        updateZones();

    }

    private void updateZones() {
        rightzone.asWidget().setLayoutData(new HorizontalLayoutContainer.HorizontalLayoutData(1 - right, 1));
        leftzone.asWidget().setLayoutData(new HorizontalLayoutContainer.HorizontalLayoutData(left, 1));
        dragArea.asWidget().setLayoutData(new HorizontalLayoutContainer.HorizontalLayoutData(1 - left - (1 - right), 1));
    }

    private void setLeft(double left) {
        this.left = left;
        updateZones();

    }


    @Override
    public Widget asWidget() {
        return layoutPanel;
    }

    public void add(final IsWidget widget) {
        draggable = new Draggable(widget.asWidget());
        draggable.setContainer(dragArea);
        draggable.setUseProxy(false);
        draggable.setEnabled(true);
        draggable.setConstrainVertical(true);
        draggable.addDragHandler(new DragHandler() {
            @Override
            public void onDragCancel(DragCancelEvent event) {
                for (DragHandler dragHandler : dragHandlers) {
                    dragHandler.onDragCancel(event);
                }
            }

            @Override
            public void onDragEnd(DragEndEvent event) {
                for (DragHandler dragHandler : dragHandlers) {

                    dragHandler.onDragEnd(event);
                }
            }

            @Override
            public void onDragMove(DragMoveEvent event) {
                for (DragHandler dragHandler : dragHandlers) {

                    dragHandler.onDragMove(event);
                }

            }

            @Override
            public void onDragStart(DragStartEvent event) {
                for (DragHandler dragHandler : dragHandlers) {

                    dragHandler.onDragStart(event);
                }

            }
        });
    }

    public Draggable getDraggable() {
        return draggable;
    }

    public void addDragHandler(DragHandler handler){
        dragHandlers.add(handler);
    }
}
