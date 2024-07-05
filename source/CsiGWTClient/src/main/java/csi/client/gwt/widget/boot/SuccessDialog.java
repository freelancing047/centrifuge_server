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

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.dom.client.Style.Unit;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SuccessDialog extends NotificationPopup {

    public SuccessDialog(String titleIn, String messageTextIn) {

        super();

        initialize(titleIn, messageTextIn);
    }

    public SuccessDialog(String messageTextIn) {

        super();

        initialize(null, messageTextIn);
    }

    public SuccessDialog(String titleIn, String messageTextIn, CanBeShownParent parentIn) {

        super(parentIn);

        initialize(titleIn, messageTextIn);
    }

    public SuccessDialog(String messageTextIn, CanBeShownParent parentIn) {

        super(parentIn);

        initialize(null, messageTextIn);
    }

    protected void initialize(String titleIn, String messageTextIn) {
        
        Icon myIcon = new Icon(IconType.OK_SIGN);
        
        myIcon.setSize(IconSize.TWO_TIMES);
        myIcon.getElement().getStyle().setColor(txtSuccessColor);
        myIcon.getElement().getStyle().setPaddingRight(10, Unit.PX);
        
        init(titleIn, messageTextIn, myIcon);
    }
}
