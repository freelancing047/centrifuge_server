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

import java.io.Serializable;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface ColorModel extends Serializable {

    /**
     * @param value Value for which color needs to be computed.
     * @param minValue Min value of range
     * @param maxValue Max value of range.
     * @return Color (with leading '#')
     */
    String getColor(double value, double minValue, double maxValue);

    /**
     * @param value Value for which color needs to be computed.
     * @param minValue Min value of range
     * @param maxValue Max value of range.
     * @return Color RGB
     */
    int getColorRGB(double value, double minValue, double maxValue);

    /**
     * Number of divisions in the color model (for discrete color models, this is the exact number of 
     * divisions specified, for continuous it is an arbitrary value and for single, it is 1).
     * @param dummy
     */
    int getDivisions(boolean dummy);

}
