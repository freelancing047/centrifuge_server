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

import java.util.ArrayList;
import java.util.List;

import com.emitrom.lienzo.client.core.shape.Group;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class CoreGroup extends Group {

    private List<WienzoComposite> managedChildren = new ArrayList<WienzoComposite>();
    private double boundingWidth, boundingHeight;
    private WienzoComposite container;

    public CoreGroup(WienzoComposite container) {
        this.container = container;
        
    }

    public double getBoundingWidth() {
        return boundingWidth;
    }

    public void setBoundingWidth(double boundingWidth) {
        this.boundingWidth = boundingWidth;
    }

    public double getBoundingHeight() {
        return boundingHeight;
    }

    public void setBoundingHeight(double boundingHeight) {
        this.boundingHeight = boundingHeight;
    }

    /**
     * Managed children are those that are not added to this group but need delta notifications when the parent is 
     * moved so that their positions can be adjusted accordingly.
     * @param composite
     */
    public void addManagedChild(WienzoComposite composite) {
        managedChildren.add(composite);
    }

    @Override
    public Group setX(double x) {
        double delta = x - getX();
        // The parent constructor calls setX before this class is initialized. Therefore at construction time,
        // managedChildren is null!
        if (managedChildren != null && !container.isDragging()) {
            for (WienzoComposite composite : managedChildren) {
                composite.setX(composite.getX() + delta);
            }
        }
        return super.setX(x);
    }

    @Override
    public Group setY(double y) {
        double delta = y - getY();
        // See comment in setX for the null check
        if (managedChildren != null && !container.isDragging()) {
            for (WienzoComposite composite : managedChildren) {
                composite.setY(composite.getY() + delta);
            }
        }
        return super.setY(y);
    }

}
