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

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.widget.buttons.Button;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ErrorDialog extends NotificationPopup {

    private final static int minMessage = 10;
    private final static int maxMessage = 200;

    private boolean _isOnlyWarning = false;
    private boolean _expand = false;

    FluidRow _fullMessageRow = null;
    Button _fullMessageButton = null;

    ClickHandler myClickHandler = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {

            boolean myTraceVisible = _fullMessageRow.isVisible();

            expandDisplay(!myTraceVisible);
        }
    };

    public ErrorDialog(String titleIn, String messageTextIn, boolean stripFirstLineIn) {

        super();

        initialize(titleIn, messageTextIn, stripFirstLineIn);
    }

    public ErrorDialog(String titleIn, String messageTextIn, boolean stripFirstLineIn, boolean isOnlyWarningIn, boolean expandIn) {

        super();

        _expand = expandIn;
        _isOnlyWarning = isOnlyWarningIn;
        initialize(titleIn, messageTextIn, stripFirstLineIn);
    }

    public ErrorDialog(String messageTextIn, boolean stripFirstLineIn) {

        super();

        initialize(null, messageTextIn, stripFirstLineIn);
    }

    public ErrorDialog(String messageTextIn, boolean stripFirstLineIn, boolean isOnlyWarningIn) {

        super();

        _isOnlyWarning = isOnlyWarningIn;
        initialize(null, messageTextIn, stripFirstLineIn);
    }

    public ErrorDialog(String titleIn, String messageTextIn) {

        super();

        initialize(titleIn, messageTextIn, false);
    }

    public ErrorDialog(String titleIn, String messageTextIn, boolean stripFirstLineIn, ClickHandler handlerIn) {

        super();

        initialize(titleIn, messageTextIn, stripFirstLineIn);
        if (null != handlerIn) {

            getCancelButton().addClickHandler(handlerIn);
        }
    }

    public ErrorDialog(String titleIn, String messageTextIn, ClickHandler handlerIn) {

        super();

        initialize(titleIn, messageTextIn, false);
        if (null != handlerIn) {

            getCancelButton().addClickHandler(handlerIn);
        }
    }

    public ErrorDialog(String messageTextIn) {

        super();

        initialize(null, messageTextIn, false);
    }

    public ErrorDialog(String messageTextIn, boolean stripFirstLineIn, ClickHandler handlerIn) {

        super();

        initialize(null, messageTextIn, stripFirstLineIn);
        if (null != handlerIn) {

            getCancelButton().addClickHandler(handlerIn);
        }
    }

    public ErrorDialog(String messageTextIn, ClickHandler handlerIn) {

        super();

        initialize(null, messageTextIn, false);
        if (null != handlerIn) {

            getCancelButton().addClickHandler(handlerIn);
        }
    }

    public ErrorDialog(String titleIn, String messageTextIn, boolean stripFirstLineIn, CanBeShownParent parentIn) {

        super(parentIn);

        initialize(titleIn, messageTextIn, stripFirstLineIn);
    }

    public ErrorDialog(String messageTextIn, boolean stripFirstLineIn, CanBeShownParent parentIn) {

        super(parentIn);

        initialize(null, messageTextIn, stripFirstLineIn);
    }

    public ErrorDialog(String titleIn, String messageTextIn, CanBeShownParent parentIn) {

        super(parentIn);

        initialize(titleIn, messageTextIn, false);
    }

    public ErrorDialog(String messageTextIn, CanBeShownParent parentIn) {

        super(parentIn);

        initialize(null, messageTextIn, false);
    }

    protected void initialize(String titleIn, String messageTextIn, boolean stripFirstLineIn) {

        String myTitle = (null != titleIn) ? titleIn : txtErrorTitle;
        String myMessage = (null != messageTextIn) ? messageTextIn : txtUnknownError;
        Icon myIcon = _isOnlyWarning ? new Icon(IconType.WARNING_SIGN) : new Icon(IconType.REMOVE_SIGN);

        myIcon.setSize(IconSize.TWO_TIMES);
        myIcon.getElement().getStyle().setColor(_isOnlyWarning ? txtWarningColor : txtErrorColor);
        myIcon.getElement().getStyle().setPaddingRight(10, Unit.PX);


        if (stripFirstLineIn || (500 < myMessage.length())) {
            Column myColumn = new Column(12);
            TextArea myTextArea = new TextArea();
            int myLineEnd = myMessage.indexOf("\n");
            int myLimit = ((minMessage < myLineEnd) && (maxMessage > myLineEnd)) ? myLineEnd : minMessage;
            String myFullMessage = (stripFirstLineIn && (minMessage < myLineEnd) && (maxMessage > myLineEnd))
                                        ? myMessage.substring(myLineEnd + 1) : myMessage;

            _fullMessageRow = new FluidRow();
            myMessage = myMessage.substring(0,myLimit) + "...";
            myTextArea.getElement().setAttribute("wrap", "on");
            myTextArea.setSize("400px", "100px");
            myTextArea.setText(myFullMessage);
            myColumn.getElement().getStyle().setTextAlign(Style.TextAlign.LEFT);
            myColumn.add(myTextArea);
            _fullMessageRow.add(myColumn);
            _fullMessageButton = new Button("");
            _fullMessageButton.setEnabled(true);
            _fullMessageButton.setVisible(true);
            _fullMessageButton.addClickHandler(myClickHandler);
            expandDisplay(_expand);
        }
        init(myTitle, myMessage, myIcon);

        if (null != _fullMessageRow) {

            add(_fullMessageRow);
        }

        if (null != _fullMessageButton) {

            addRightControl(_fullMessageButton);
        }
    }
    private void expandDisplay(boolean expandIn) {

        _fullMessageButton.setText(expandIn ? Dialog.txtHideFullMessageButton : Dialog.txtShowFullMessageButton);
        _fullMessageRow.setVisible(expandIn);
    }
}
