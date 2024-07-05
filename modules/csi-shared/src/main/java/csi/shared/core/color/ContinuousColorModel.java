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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import csi.shared.core.color.ColorUtil.HSL;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@XStreamAlias("ContinuousColorModel")
public class ContinuousColorModel extends AbstractColorModel {

    int startX = 15, startY = 160, endX = 367, endY = 234;

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public int getEndX() {
        return endX;
    }

    public void setEndX(int endX) {
        this.endX = endX;
    }

    public int getEndY() {
        return endY;
    }

    public void setEndY(int endY) {
        this.endY = endY;
    }

    /**
     * @param minRange Min value of the range
     * @param maxRange Max value of the range
     * @param value Actual value instance within the range.
     * @return Color that corresponds to that value instance.
     */
    public int getInterpolatedRGBColor(double minRange, double maxRange, double value) {
        if (Math.abs(maxRange - minRange) < 0.001) {
            HSL start = getHSL(getStartX(), getStartY());
            return ColorUtil.HSL2RGB(start);
        } else {
            HSL start = getHSL(getStartX(), getStartY());
            HSL end = getHSL(getEndX(), getEndY());
            // Note: We need to account for cases where starting hue (or lum) is greater than ending hue (or lum) and
            // interpolate accordingly.
            double hue = 0;
            double dh = end.getH() - start.getH();
            double normalizedValue = (value - minRange) / (maxRange - minRange);
            hue = start.getH() + normalizedValue * (dh);

            double dl = end.getL() - start.getL();
            double lum = 0;
            lum = start.getL() + normalizedValue * (dl);

            return ColorUtil.HSL2RGB(new HSL(hue, 0.5, lum));
        }
    }

    @Override
    public String getColor(double value, double minValue, double maxValue) {
        return ColorUtil.toColorString(getInterpolatedRGBColor(minValue, maxValue, value));
    }
    
    @Override
    public int getColorRGB(double value, double minValue, double maxValue) {
        return getInterpolatedRGBColor(minValue, maxValue, value);
    }
    
    @Override
    public int getDivisions(boolean dummy) {
        return 4;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + endX;
		result = prime * result + endY;
		result = prime * result + startX;
		result = prime * result + startY;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContinuousColorModel other = (ContinuousColorModel) obj;
		if (endX != other.endX)
			return false;
		if (endY != other.endY)
			return false;
		if (startX != other.startX)
			return false;
		if (startY != other.startY)
			return false;
		return true;
	}
}
