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
package csi.client.gwt.widget.ui.surface;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DrawContext {

    private double categoryWidth, categoryHeight;
    private int totalElements;
    private double zoomLevel;

    private Map<String, Object> dataMap = new HashMap<String, Object>();

    /**
     * @return Width of the box (grid box) centered around cx that "belongs" to this renderable. 
     */
    public double getCategoryWidth() {
        return categoryWidth;
    }

    public void setCategoryWidth(double categoryWidth) {
        this.categoryWidth = categoryWidth;
    }

    /**
     * @return Height of the box (grid box) centered around cy that "belongs" to this renderable.
     */
    public double getCategoryHeight() {
        return categoryHeight;
    }

    public void setCategoryHeight(double categoryHeight) {
        this.categoryHeight = categoryHeight;
    }

    /**
     * @return Number of renderables to draw (this may influence level of detail to optimize for rendering speed).
     */
    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public double getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(double zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    public void put(String key, Object data) {
        dataMap.put(key, data);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) dataMap.get(key);
    }
}
