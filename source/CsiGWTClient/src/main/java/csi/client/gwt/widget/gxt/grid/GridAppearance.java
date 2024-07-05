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
package csi.client.gwt.widget.gxt.grid;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.theme.gray.client.grid.GrayGridAppearance;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class GridAppearance extends GrayGridAppearance {

    public interface CsiGridStyle extends GrayGridStyle {

    }

    public interface CsiGridResources extends GrayGridResources {

        @Source({ "com/sencha/gxt/theme.neptune/client/base/grid/Css3Grid.gss",
                "Grid.css"})
        @Override
        CsiGridStyle css();
    }

    public GridAppearance() {
        this(GWT.<CsiGridResources> create(CsiGridResources.class));
    }

    public GridAppearance(CsiGridResources resources) {
        super(resources);
    }
}
