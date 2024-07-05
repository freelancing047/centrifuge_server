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
package csi.shared.core.imaging;

import java.io.Serializable;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
public abstract class ImageComponent implements Serializable {

    private String data;
    private int width, height;
    private int x, y;

    private String name = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    /**
     * @return Width of this component.
     */
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return Height of this component.
     */
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return X position in overall image where this should be rendered.
     */
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return Y position in overall image where this should be rendered.
     */
    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

}
