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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@XStreamAlias("DiscreteColorModel")
public class DiscreteColorModel extends AbstractColorModel {

    @XStreamAsAttribute
    private String category;

    private List<String> colors = new ArrayList<String>();

    public DiscreteColorModel() {
        super();
        setCategory("Diverging");
        getColors().add("#d7191c");
        getColors().add("#fdae61");
        getColors().add("#ffffbf");
        getColors().add("#abdda4");
        getColors().add("#2b83ba");
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    @Override
    public String getColor(double value, double minValue, double maxValue) {
        if (Math.abs(maxValue - minValue) < 0.00001 || value > maxValue) {
            return colors.get(colors.size() - 1);
        } else if(value < minValue){
        	return colors.get(0);
        }
        else {
            double normalized = (value - minValue) / (maxValue - minValue);
            int index = (int) Math.floor(normalized * colors.size());
            // When value == maxValue, index will be one more than colors size. So reduce by 1.
            if (index == colors.size()) {
                index = colors.size() - 1;
            }
            return colors.get(index);
        }
    }

    @Override
    public int getColorRGB(double value, double minValue, double maxValue) {
        return ColorUtil.toRGB(getColor(value, minValue, maxValue));
    }
    
    @Override
    public int getDivisions(boolean dummy) {
        return colors.size();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this) //
                .add("category", getCategory()) //
                .add("colors", getColors()) //
                .toString();
    }
}
