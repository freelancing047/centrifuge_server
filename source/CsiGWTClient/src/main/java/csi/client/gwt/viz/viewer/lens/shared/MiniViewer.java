package csi.client.gwt.viz.viewer.lens.shared;

import com.github.gwtbootstrap.client.ui.FluidRow;

public class MiniViewer extends ExpandableItem {

    public MiniViewer(String label) {
        super(label);
        myRow.getElement().getStyle().setBackgroundColor("#DDD");


    }
    void buildMyRow() {
        myRow = new FluidRow();
//        buildExpandButton();
//        buildLabel();
        myContainer.add(myRow);
    }
}
