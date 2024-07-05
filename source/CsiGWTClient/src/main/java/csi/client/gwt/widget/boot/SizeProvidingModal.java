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
package csi.client.gwt.widget.boot;

import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * A modal dialog that provides the bodyWidth and bodyHeight attributes given to it to its body element.
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public class SizeProvidingModal extends CsiModal {

    private String bodyWidth = "300";
    private String bodyHeight = "300";

    public SizeProvidingModal(CanBeShownParent parentIn) {
        super(parentIn);
    }

    public SizeProvidingModal(boolean animated, CanBeShownParent parentIn) {
        super(animated, parentIn);
    }

    public SizeProvidingModal() {
        super();
    }

    public SizeProvidingModal(boolean animated) {
        super(animated);
    }

    public String getBodyWidth() {
        return bodyWidth ;
    }

    public void setBodyWidth(String bodyWidth) {
        this.bodyWidth = bodyWidth;
    }

    public String getBodyHeight() {
        return bodyHeight;
    }

    public void setBodyHeight(String bodyHeight) {
        this.bodyHeight = bodyHeight;
    }

    @Override
    public void show() {
        setBodySize();
        super.show();
    }

    public void showWithoutSettingMargin() {
        setBodySize();
        super.show();
        this.getElement().getStyle().clearMarginTop();
        this.getElement().getStyle().clearMarginLeft();
    }

    private void setBodySize() {
        for (Widget widget : getChildren()) {
            if (widget instanceof DivWidget && widget.getStyleName().equals("modal-body")) {
                DivWidget bodyWidget = (DivWidget) widget;
                // Apply body width and height to first child.
                Widget firstChild = bodyWidget.getWidget(0);
                firstChild.setWidth(getBodyWidth());
                firstChild.setHeight(getBodyHeight());
                break;
            }
        }
    }
}
