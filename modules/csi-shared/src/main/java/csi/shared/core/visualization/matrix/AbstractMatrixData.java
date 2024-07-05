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
package csi.shared.core.visualization.matrix;

import java.io.Serializable;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractMatrixData implements Serializable {

    private int startX, endX; // starting and ending x index (0-based)
    private int startY, endY; // starting and ending y index (0-based)

    private int maxX, maxY; // this will be the absolute max index for the matrix

    public AbstractMatrixData() {
        super();
    }

    public AbstractMatrixData(int startX, int endX, int startY, int endY) {
        super();
        this.startX = startX;
        this.endX = endX;
        this.startY = startY;
        this.endY = endY;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    /**
     * @return String that uniquely represents this data request.
     */
    public String getRequestHash() {
        return startX + "," + endX + "," + startY + "," + endY;
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getEndX() {
        return endX;
    }

    public void setEndX(int endX) {
        this.endX = endX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public int getEndY() {
        return endY;
    }

    public void setEndY(int endY) {
        this.endY = endY;
    }

}
