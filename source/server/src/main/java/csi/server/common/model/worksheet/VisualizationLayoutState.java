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
package csi.server.common.model.worksheet;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.VisualizationDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class VisualizationLayoutState extends ModelObject implements Serializable {

    public VisualizationLayoutState() {
        super();
    }

    private boolean maximized, minimized;

    private int x, y, width = 640, height = 500, minimizedX, minimizedY;

    @ManyToOne(cascade = CascadeType.REMOVE)
    VisualizationDef visualizationDef;

    public boolean isMaximized() {
        return maximized;
    }

    public void setMaximized(boolean maximized) {
        this.maximized = maximized;
    }

    public boolean isMinimized() {
        return minimized;
    }

    public void setMinimized(boolean minimized) {
        this.minimized = minimized;
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

    public int getMinimizedX() {
        return minimizedX;
    }

    public void setMinimizedX(int minimizedX) {
        this.minimizedX = minimizedX;
    }

    public int getMinimizedY() {
        return minimizedY;
    }

    public void setMinimizedY(int minimizedY) {
        this.minimizedY = minimizedY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public VisualizationDef getVisualizationDef() {
        return visualizationDef;
    }

    public void setVisualizationDef(VisualizationDef visualizationDef) {
        this.visualizationDef = visualizationDef;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModelObject, S extends ModelObject, R extends ModelObject> VisualizationLayoutState clone(Map<String, T> visualizationMapIn, Map<String, S> fieldMapIn, Map<String, R> filterMapIn) {
        
        VisualizationLayoutState myClone = new VisualizationLayoutState();
        
        super.cloneComponents(myClone);

        myClone.setMaximized(isMaximized());
        myClone.setMinimized(isMinimized());
        myClone.setX(getX());
        myClone.setY(getY());
        myClone.setWidth(getWidth());
        myClone.setHeight(getHeight());
        myClone.setMinimizedX(getMinimizedX());
        myClone.setMinimizedY(getMinimizedY());
        myClone.setVisualizationDef((VisualizationDef)cloneFromOrToMap(visualizationMapIn, (T)getVisualizationDef(), fieldMapIn, filterMapIn));
        
        return myClone;
    }
}
