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
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sencha.gxt.theme.base.client.frame.NestedDivFrame;
import com.sencha.gxt.theme.base.client.frame.NestedDivFrame.NestedDivFrameStyle;
import com.sencha.gxt.theme.base.client.panel.FramedPanelBaseAppearance;
import com.sencha.gxt.theme.base.client.widget.HeaderDefaultAppearance;
import com.sencha.gxt.theme.base.client.widget.HeaderDefaultAppearance.HeaderResources;
import com.sencha.gxt.theme.base.client.widget.HeaderDefaultAppearance.HeaderStyle;
import com.sencha.gxt.theme.gray.client.panel.GrayFramedPanelAppearance.FramedPanelStyle;

import csi.client.gwt.worksheet.layout.window.WindowBase.WindowAppearance;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class VisualizationWindowAppearance extends FramedPanelBaseAppearance implements WindowAppearance {

    public interface VisualizationWindowFrameStyle extends NestedDivFrameStyle {

    }

    public interface GrayWindowDivFrameResources extends FramedPanelDivFrameResources, ClientBundle {

        @Source({ "com/sencha/gxt/theme/base/client/frame/NestedDivFrame.gss", "VisualizationWindowFrameStyle.gss"})
        @Override
        VisualizationWindowFrameStyle style();

        @Source("clear.gif")
        ImageResource background();

        @Override
        ImageResource topLeftBorder();

        @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
        @Override
        ImageResource topBorder();

        @Override
        @ImageOptions(repeatStyle = RepeatStyle.Both)
        ImageResource topRightBorder();

        @ImageOptions(repeatStyle = RepeatStyle.Vertical)
        @Override
        ImageResource leftBorder();

        @ImageOptions(repeatStyle = RepeatStyle.Both)
        @Override
        ImageResource rightBorder();

        @Override
        @ImageOptions(repeatStyle = RepeatStyle.Both)
        ImageResource bottomLeftBorder();

        @ImageOptions(repeatStyle = RepeatStyle.Both)
        @Override
        ImageResource bottomBorder();

        @Override
        @ImageOptions(repeatStyle = RepeatStyle.Both)
        ImageResource bottomRightBorder();
    }

    public interface VisualizationWindowStyle extends FramedPanelStyle {

        String ghost();
    }

    public interface VisualizationWindowHeaderStyle extends HeaderStyle {

    }

    public interface MyHeaderResources extends HeaderResources {

        @Source({ "com/sencha/gxt/theme/base/client/widget/Header.gss", "VisualizationWindowHeader.gss" })
        VisualizationWindowHeaderStyle style();
    }

    public interface MyWindowResources extends ContentPanelResources, ClientBundle {

        @Source({ "com/sencha/gxt/theme/base/client/panel/ContentPanel.gss",
                  "com/sencha/gxt/theme/base/client/window/Window.gss", "VisualizationWindow.gss"})
        @Override
        VisualizationWindowStyle style();

    }

    private VisualizationWindowStyle style;

    public VisualizationWindowAppearance() {
        this((MyWindowResources) GWT.create(MyWindowResources.class));
    }

    public VisualizationWindowAppearance(MyWindowResources resources) {
        super(resources, GWT.<FramedPanelTemplate> create(FramedPanelTemplate.class), new NestedDivFrame(
                GWT.<GrayWindowDivFrameResources> create(GrayWindowDivFrameResources.class)));

        this.style = resources.style();
    }

    @Override
    public HeaderDefaultAppearance getHeaderAppearance() {
        return new HeaderDefaultAppearance(GWT.<MyHeaderResources> create(MyHeaderResources.class));
    }

    @Override
    public String ghostClass() {
        return style.ghost();
    }
}
