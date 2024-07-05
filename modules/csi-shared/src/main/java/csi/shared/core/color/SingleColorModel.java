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
package csi.shared.core.color;

import com.google.common.base.MoreObjects;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import csi.shared.core.color.ColorUtil.HSL;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@XStreamAlias("SingleColorModel")
public class SingleColorModel extends AbstractColorModel {

    @XStreamAsAttribute
    private String color = "#000000";

    public SingleColorModel() {
        super();
    }

    public SingleColorModel(String color) {
        this.color = color.startsWith("#") ? color : "#" + color;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setColorCoords(int x, int y) {
        HSL hsl = getHSL(x, y);
        setColor(ColorUtil.toColorString(hsl));
    }

    @Override
    public String getColor(double value, double minValue, double maxValue) {
        return color;
    }
    
    @Override
    public int getColorRGB(double value, double minValue, double maxValue) {
        if(color == null || color.isEmpty())
            color = "#3498db";

        return ColorUtil.toRGB(color);
    }

    @Override
    public int getDivisions(boolean dummy) {
        return 1;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this) //
                .add("color", getColor()) //
                .toString();
    }

}
