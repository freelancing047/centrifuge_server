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
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class InfoDialog extends NotificationPopup {

    public InfoDialog(String titleIn, String messageTextIn, ClickHandler handlerIn) {

        super(handlerIn);

        Initialize(titleIn, messageTextIn);
//        this.getActionButton().addClickHandler(handlerIn);
    }

    public InfoDialog(String titleIn, String messageTextIn, CanBeShownParent parentIn) {

        super(parentIn);

        Initialize(titleIn, messageTextIn);
    }

    public InfoDialog(String messageTextIn, CanBeShownParent parentIn) {

        super(parentIn);

        Initialize(null, messageTextIn);
    }

    public InfoDialog(String titleIn, String messageTextIn) {

        super();

        Initialize(titleIn, messageTextIn);
    }

    public InfoDialog(String messageTextIn) {

        super();

        Initialize(null, messageTextIn);
    }

    protected void Initialize(String titleIn, String messageTextIn) {
        
        String myTitle = (null != titleIn) ? titleIn : Dialog.txtInfoTitle;
        Icon myIcon = new Icon(IconType.INFO_SIGN);
        myIcon.setSize(IconSize.TWO_TIMES);
        myIcon.getElement().getStyle().setColor(txtInfoColor);
        myIcon.getElement().getStyle().setPaddingRight(10, Unit.PX);
        init(myTitle, messageTextIn, myIcon);
        this.getActionButton().setText(txtOkayButton);
    }
}
