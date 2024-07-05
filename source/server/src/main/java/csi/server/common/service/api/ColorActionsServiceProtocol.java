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
package csi.server.common.service.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import csi.shared.core.color.BrewerColorSet;
import csi.shared.core.color.ColorModel;
import csi.shared.gwt.vortex.VortexService;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface ColorActionsServiceProtocol extends VortexService {

    public enum RangeDirection implements Serializable {
        HORIZONTAL, VERTICAL
    }

    /**
     * @return Map of category to map of size to color sets of that size.
     */
    public Map<String, Map<Integer, List<BrewerColorSet>>> getBrewerColors();

    /**
     * @param width Width of the sample image
     * @param height Height of the sample image
     * @param model Color model.
     * @return Base64 encoded image.
     */
    public String getColorRangeSample(int width, int height, ColorModel model, RangeDirection direction);
}
