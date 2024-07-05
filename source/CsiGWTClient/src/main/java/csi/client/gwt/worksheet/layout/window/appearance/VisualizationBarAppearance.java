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
package csi.client.gwt.worksheet.layout.window.appearance;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface VisualizationBarAppearance extends ClientBundle {

    ImageResource windowTile();
    
    ImageResource windowCascade();
    
    @Source("barStyle.css")
    BarStyle style();
    
    public interface BarStyle extends CssResource {

        String iconStyle();
        
    }
}
