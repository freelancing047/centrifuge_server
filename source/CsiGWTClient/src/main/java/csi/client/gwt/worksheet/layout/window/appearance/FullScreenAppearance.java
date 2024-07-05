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

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.widget.core.client.button.IconButton.IconConfig;
import com.sencha.gxt.widget.core.client.button.ToolButton;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class FullScreenAppearance {

    public interface FullScreenButtonStyle extends CssResource {

        String fullScreen();

        String fullScreenOver();

        String restoreFullScreen();

        String restoreFullScreenOver();
    }

    public interface FullScreenButtonResource extends ClientBundle {

        @Source("fullScreenButtonStyle.css")
        FullScreenButtonStyle style();

        ImageResource fullScreenIcon();

        ImageResource fullScreenOverIcon();

        ImageResource restoreFullScreenIcon();

        ImageResource restoreFullScreenOverIcon();
    }

    private static final FullScreenButtonResource fullScreenButtonResource = GWT.create(FullScreenButtonResource.class);

    public FullScreenAppearance() {
        super();
        fullScreenButtonResource.style().ensureInjected();
    }

    public ToolButton getFullScreenButton() {
        return new ToolButton(new IconConfig(fullScreenButtonResource.style().fullScreen(),
                fullScreenButtonResource.style().fullScreenOver()));
    }

    public ToolButton getRestoreFullScreenButton() {
        return new ToolButton(new IconConfig(fullScreenButtonResource.style().restoreFullScreen(),
                fullScreenButtonResource.style().restoreFullScreenOver()));
    }
}
