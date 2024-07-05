/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.edit_sources.center_panel.shapes;

import com.emitrom.lienzo.client.core.event.NodeDragEndEvent;
import com.emitrom.lienzo.client.core.shape.Group;
import com.emitrom.lienzo.client.core.shape.Layer;
import com.emitrom.lienzo.client.core.shape.Text;
import com.emitrom.lienzo.shared.core.types.TextAlign;
import com.emitrom.lienzo.shared.core.types.TextBaseLine;

import csi.client.gwt.edit_sources.center_panel.ConfigurationPresenter;
import csi.server.common.model.DataSetOp;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class WienzoDualChildComposite extends WienzoComposite {

    protected abstract double placeChildren(double deltaX, double deltaY);

    private String name;
    private WienzoComposite child1, child2;
    private Layer child1Layer, child2Layer;

    public WienzoDualChildComposite(DataSetOp dso, WienzoComposite child1, WienzoComposite child2, String name,
                                    ConfigurationPresenter configurationPresenter, String backgroundColorIn,
                                    String attentionColorIn, String attentionStrokeIn) {
        super(dso, configurationPresenter, backgroundColorIn, attentionColorIn, attentionStrokeIn);
        this.name = name;

        this.child1 = child1;
        this.child2 = child2;
        this.child1.setWienzoParent(this);
        this.child2.setWienzoParent(this);

        init();
    }

    private void init() {
        title = new Text(name);
        title.setFontSize(10);
        title.setTextAlign(TextAlign.LEFT);
        title.setTextBaseLine(TextBaseLine.HANGING);
        title.setX(DIM_TITLE_OFFSET);
        title.setY(DIM_TITLE_OFFSET);
    }

    protected WienzoComposite getChild1() {
        return child1;
    }

    protected WienzoComposite getChild2() {
        return child2;
    }

    public Layer getChild1Layer() {
        return child1Layer;
    }

    public void setChild1Layer(Layer child1Layer) {
        this.child1Layer = child1Layer;
    }

    public Layer getChild2Layer() {
        return child2Layer;
    }

    public void setChild2Layer(Layer child2Layer) {
        this.child2Layer = child2Layer;
    }

    @Override
    public Group setX(double x) {
        if (getChild1() != null && !isDragging()) {
            getChild1().setX(getChild1().getX() + x - getX());
        }
        if (getChild2() != null && !isDragging()) {
            getChild2().setX(getChild2().getX() + x - getX());
        }

        return super.setX(x);
    }

    @Override
    public Group setY(double y) {
        if (getChild1() != null && !isDragging()) {
            getChild1().setY(getChild1().getY() + y - getY());
        }
        if (getChild2() != null && !isDragging()) {
            getChild2().setY(getChild2().getY() + y - getY());
        }
        return super.setY(y);
    }

    @Override
    public void onDragStart(boolean primary) {
        super.onDragStart(primary);
        
        // Left child - add to group and remove from its layer.
        getChild1().setX(getChild1().getX() - getX());
        getChild1().setY(getChild1().getY() - getY());
        setChild1Layer(getChild1().getLayer());
        getChild1Layer().remove(getChild1());
        add(getChild1());
        getChild1Layer().draw();

        // Right child - add to group and remove from its layer.
        getChild2().setX(getChild2().getX() - getX());
        getChild2().setY(getChild2().getY() - getY());
        setChild2Layer(getChild2().getLayer());
        getChild2Layer().remove(getChild2());
        add(getChild2());
        getChild2Layer().draw();

        getChild1().onDragStart(false);
        getChild2().onDragStart(false);
        
        if (primary && getWienzoParent() == null) {
            // We are being dragged - perhaps to attach.
//            setAlpha(0.5);
        }
    }

    @Override
    public void onDragEnd(NodeDragEndEvent eventIn, boolean primary) {

        if (!fullRedrawRequired(eventIn, primary)) {

            // Remove left child from group, restore to its own layer and set its x,y
            remove(getChild1());
            getChild1Layer().add(getChild1());
            getChild1().setX(getChild1().getX() + getX());
            getChild1().setY(getChild1().getY() + getY());

            // The child's layer needs to redraw so that the child's hit-test layer gets drawn. Otherwise
            // at this point, the child is not responsive to mouse interaction.
            getChild1Layer().draw();
            getLayer().draw();

            // Remove right child from group, restore to its own layer and set its x,y
            remove(getChild2());
            getChild2Layer().add(getChild2());
            getChild2().setX(getChild2().getX() + getX());
            getChild2().setY(getChild2().getY() + getY());
            getChild2Layer().draw();
            getLayer().draw();

            getChild1().onDragEnd(eventIn, false);
            getChild2().onDragEnd(eventIn, false);
        }
        super.onDragEnd(eventIn, primary);
    }

    @Override
    public void handleDetach(DataSetOp dsoToDetach) {
        // TODO: Dependency check.
        getConfigurationPresenter().detachChild(dsoToDetach, true);
    }
}
