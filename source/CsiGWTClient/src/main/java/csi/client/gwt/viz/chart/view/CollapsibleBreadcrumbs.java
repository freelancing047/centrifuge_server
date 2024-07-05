package csi.client.gwt.viz.chart.view;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;

public class CollapsibleBreadcrumbs extends Breadcrumbs {

    //** Currently no check, so be careful
    // number of elements to skip at the left side
    private int leftOffset = 1;
    // number of elements to skip at the right side
    private int rightOffset = 2;
// TODO: could add a manage of the visiblity from a config, something like disableMicroForItems(minLimit, maxLimit) - those values will offset the trim methods.
// also configurable states, so you can global ban states.

    private boolean isVisible = true;

    /**
     * Will attempt to fit all items to the breadcrumb bar, with either different render state or hide them.
     *
     */
    public void fitBreadcrumbs(){
        fitItems();
        // if fit worked, great. If there is still too many in the bar, start collapsing and hiding elemnts from the middle
        if(isTrim()){
            collapseMiddle();
        }
    }

    /**
     * Iterates and hides the breadcrumbs, starting at leftOffset, ending in totalElements - rightOffset
     */
    private void collapseMiddle(){
        int totalElements = this.getChildren().size();
        for (int i = leftOffset; i < totalElements - rightOffset; i++) {
            Widget child = this.getChildren().get(i);
            if (child instanceof Breadcrumb) {
                Breadcrumb tmp = (Breadcrumb) child;
                tmp.setRenderState(Breadcrumb.RenderState.HIDDEN);
            }
        }
    }

    /**
     * Sets the renderstate of every cell to RenderState.FULL
     * Will not overwrite anything.
     *
     */
    private void expandAll(){
        WidgetCollection children = this.getChildren();
        for(Widget child : children) {
            if (child instanceof Breadcrumb) {
                Breadcrumb tmp = (Breadcrumb) child;
                tmp.setRenderState(Breadcrumb.RenderState.FULL);
            }
        }
    }

    /**
     * Will try to fit all the breadcrumbs to the bar, by applying each of the render states for every cell until it fits.
     *
     * Will stop as soon as it fits, or until runs out of elements and render states.
     */
    private void fitItems() {

            for (Breadcrumb.RenderState set : Breadcrumb.RenderState.values()) {
                /// disable hidden, so we don't hide at random
                if(set != Breadcrumb.RenderState.HIDDEN) {

                    for(Widget w : this.getChildren()){
                        if (w instanceof Breadcrumb) {
                            Breadcrumb tmp = (Breadcrumb) w;
                            if (isTrim()) {
                                tmp.setRenderState(set);
                            } else {
                                return; // or should be break..
                            }
                        }
                    }
                }
            }
        }

    /**
     * this should refresh the visibility only if we have room...
     */
    public void refresh(){
        expandAll();
        resetVisibility();
    }

    /**
     *
     */
    public void resetVisibility(){
        WidgetCollection children = this.getChildren();
        for(Widget child : children){
            if(child instanceof Breadcrumb){
                ((Breadcrumb) child).setRenderState(Breadcrumb.RenderState.FULL);
            }
        }
    }

    private int getWidthOfChildren(){
        WidgetCollection children = this.getChildren();
        int totalWidth = 0;
        for(Widget child : children){
            totalWidth += child.getOffsetWidth();
        }
        // ensure last one
        return totalWidth;
    }

    /**
     * @return true if the length of the children in their current state
     *                is greater than the length of the container otherwise false.
     */
    private boolean isTrim(){
        return this.getWidthOfChildren() > this.getOffsetWidth();
    }

    public int getLeftOffset() {
        return leftOffset;
    }

    public void setLeftOffset(int leftOffset) {
        this.leftOffset = leftOffset;
    }

    public int getRightOffset() {
        return rightOffset;
    }

    public void setRightOffset(int rightOffset) {
        this.rightOffset = rightOffset;
    }

    public void hide(){
        isVisible = false;
        this.getElement().getStyle().setDisplay(Style.Display.NONE);
        this.getElement().getParentElement().getStyle().setTop(-25, Style.Unit.PX);

    }

    public void show(){
        isVisible = true;
        this.getElement().getParentElement().getStyle().setTop(0, Style.Unit.PX);
        this.getElement().getStyle().setDisplay(Style.Display.BLOCK);
    }

    @Override
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            this.getElement().getParentElement().getStyle().setTop(0, Style.Unit.PX);
        } else {
            this.getElement().getParentElement().getStyle().setTop(-25, Style.Unit.PX);

        }
        super.setVisible(visible);
    }
}


// notes:
/*we could overwrite the add, which will manage the cells and even can add a separate config for it.*/