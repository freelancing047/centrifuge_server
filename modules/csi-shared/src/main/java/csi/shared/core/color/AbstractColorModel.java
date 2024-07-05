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

import csi.shared.core.color.ColorUtil.HSL;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractColorModel implements ColorModel {

    public static int BOX_MODEL_WIDTH = 540;
    public static int BOX_MODEL_HEIGHT = 300;

    protected HSL getHSL(int x, int y) {
        double sat = 0.5;
        double hue = x / (double) BOX_MODEL_WIDTH;
        hue = Math.floor(hue * 20.0) / 20.0;
        double lum = (BOX_MODEL_HEIGHT - y) / (double) BOX_MODEL_HEIGHT * 0.8 + 0.2;
        lum = Math.floor(lum * 20.0) / 20.0;
        return new HSL(hue, sat, lum);
    }
}
