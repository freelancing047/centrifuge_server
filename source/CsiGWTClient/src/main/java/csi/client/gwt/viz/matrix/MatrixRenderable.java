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
package csi.client.gwt.viz.matrix;

import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.event.dom.client.ClickEvent;

import csi.client.gwt.widget.ui.surface.AbstractScrollableSurfaceRenderable;
import csi.shared.core.visualization.matrix.Cell;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class MatrixRenderable extends AbstractScrollableSurfaceRenderable {

    protected static CssColor SELECTION_COLOR = CssColor.make(255,135,10);
    private MatrixModel model;
    private Cell cell;
    private boolean showValue;

    public MatrixRenderable(boolean showValue) {
        super();
        this.showValue = showValue;
    }

    public boolean isShowValue() {
        return showValue;
    }

    public MatrixModel getModel() {
        return model;
    }

    public void setModel(MatrixModel model) {
        this.model = model;
    }

    public Cell getCell() {
        return cell;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    @Override
    public double getValue() {
        return getCell().getValue().doubleValue();
    }

    @Override
    public int getX() {
        return cell.getX();
    }

    @Override
    public int getY() {
        return cell.getY();
    }

    @Override
    public void onClick(ClickEvent event) {
        toggleSelection();
    }

    public boolean isSelected() {
        return getModel().isSelected(cell);
    }

    public void setSelected(boolean selected) {
        getModel().modifySelection(cell, selected);
    }

    protected String getColorForBubble() {
        
        return getModel().getColor(getValue());
        
    }
    
   
}
