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
import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.XDOM;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.widget.WatchBox;
import csi.client.gwt.widget.WatchBoxInterface;
import csi.client.gwt.widget.ui.DialogHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class CsiModal extends Modal implements CanBeShownParent {

    public interface OnHideCallBack{

        public void onHide(CsiModal sourceIn);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected CanBeShownParent parent = null;
    protected CsiModal _this = this;

    private Widget backdrop;
    private WatchBoxInterface watchBox = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static int _satisfiedCount = 10;
    private static int _millisecondWait = 2000;

    private static Scheduler _scheduler = null;
    private static int _deltaCount = 0;
    private static int _delta = 0;
    private static List<CsiModal> _dialogList = new ArrayList<CsiModal>();
    private static List<CsiModal> _alertList = new ArrayList<CsiModal>();
    private static List<Integer> _zOrderList = new ArrayList<Integer>();

    private boolean _displayCloseButton = true;
    private String _dialogTitle = null;
    private String _helpContext = null;
    private Callback<CsiModal> _helpCallback = null;
    private OnHideCallBack onHideCallBack = null;
    private boolean _isShowing =  false;
    private boolean _isAlert =  false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Public Static Methods                                 //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

	public static void setPopupConstants(int satisfiedCountIn, int millisecondWaitIn) {

        _satisfiedCount = satisfiedCountIn;
        _millisecondWait = millisecondWaitIn;
	}
	
    public static int decode(String stringIn) {

        int myValue = 0;

        if ((null != stringIn) && (0 < stringIn.length())) {

            for (int i = 0; stringIn.length() > i; i++) {

                int myDigit = stringIn.charAt(i) - (int)'0';

                if ((0 <= myDigit) && (9 >= myDigit)) {

                    myValue = (myValue * 10) + myDigit;

                } else {

                    break;
                }
            }
        }
        return myValue;
    }

    public static void clearAll() {

        for (int i = (null != _dialogList) ? _dialogList.size() - 1 : -1;
             0 <= i;
             i = (null != _dialogList) ? _dialogList.size() - 1 : -1) {

            CsiModal myModal =_dialogList.get(i);

            if (null != myModal) {

                myModal.hide();
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Private Static Methods                                //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static void addToList(CsiModal modalIn, Integer zOrderIn) {

        _alertList.add(modalIn);
        _zOrderList.add(zOrderIn);
        if (null == _scheduler) {

            _scheduler = Scheduler.get();
            _scheduler.scheduleFixedDelay(new Scheduler.RepeatingCommand() {

                @Override
                public boolean execute() {

                    return CsiModal.popList();
                }

            }, _millisecondWait);
        }
    }

    private static int addToList(CsiModal modalIn) {

        int myZorder = XDOM.getTopZIndex(0);

        _dialogList.add(modalIn);
        return myZorder;
    }

    private static void removeFromList(CsiModal modalIn, boolean isAlertIn) {

        if (isAlertIn) {

            int myIndex = _alertList.indexOf(modalIn);

            _alertList.remove(modalIn);
            _zOrderList.remove(myIndex);

        } else {

            _dialogList.remove(modalIn);
        }
    }

    private static boolean popList() {

        boolean myActiveFlag = false;
        int myCount = _alertList.size();

        if (0 < myCount) {

            if (_satisfiedCount > _deltaCount) {

                int myDeltaCount = _deltaCount;
                int myOldDelta = _delta;

                int myLastItem = myCount - 1;
                int myNextZorder = XDOM.getTopZIndex();
                int myLastZorder = _zOrderList.get(myLastItem);
                int myDelta = myNextZorder - myLastZorder;

                if (myOldDelta != myDelta) {

                    myDeltaCount = 0;
                    myOldDelta = myDelta;

                    int myBase = XDOM.getTopZIndex() - _zOrderList.get(0);
                    _delta = myOldDelta;
                    for (int i = 0; _zOrderList.size() > i; i++) {

                        CsiModal myModal = _alertList.get(i);
                        int myZorder = _zOrderList.get(i) + myBase;

                        myModal.getElement().getStyle().setZIndex(myZorder);
                        _zOrderList.set(i, myZorder);
                    }

                } else {

                    _delta = myOldDelta + 1;
                }
                _deltaCount = myDeltaCount + 1;
            }
            myActiveFlag = true;
        }
        return myActiveFlag;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CsiModal(CanBeShownParent parentIn) {
        super();
        parent = parentIn;
        init();
    }

    public CsiModal(boolean animated, CanBeShownParent parentIn) {
        super(animated);
        parent = parentIn;
        init();
    }

    public CsiModal() {
        super();
        init();
    }

    public CsiModal(boolean animated) {
        super(animated);
        init();
    }

    public void identifyAsAlert() {

        _isAlert = true;
    }

    @Override
    public void show() {
        
        if (!_isShowing) {

            int myZorder;

            backdrop = (((null != _alertList) && (0 < _alertList.size()))
                        || ((null != _dialogList) && (0 < _dialogList.size())))
                            ? new DivWidget("modal-backdrop-secondary")
                            : new DivWidget("modal-backdrop in");

            backdrop.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
            RootPanel.get().add(backdrop);
            myZorder = XDOM.getTopZIndex();
            getElement().getStyle().setZIndex(myZorder);

            super.show();
            if (_isAlert) {

                addToList(this, myZorder);

            } else {

                addToList(this);
            }
            _isShowing = true;

            if (null != parent) {

                parent.hide();
            }
        }
        // Bootstrap folks hardcoded the width and never bothered to center their dialog horizontally for variable
        // width.
        getElement().getStyle().setMarginLeft(-1 * getElement().getOffsetWidth() / 2, Unit.PX);
        getElement().getStyle().setMarginTop(-1 * getElement().getOffsetHeight() / 2, Unit.PX);
    }

    public void show(CanBeShownParent parentIn) {

        parent = parentIn;
        show();
    }

    public void showWithResults(KnowsParent childIn) {

        retrieveResults(childIn);
        childIn.destroy();
        show();
    }

    public WatchBoxInterface getWatchBox() {

        return (null != parent) ? parent.getWatchBox() : getLocalWatchBox();
    }

    public void showWatchBox() {

        getWatchBox().show();
    }

    public void showWatchBox(String messageIn) {

        getWatchBox().show(messageIn);
    }

    public void showWatchBox(String titleIn, String messageIn) {

        getWatchBox().show(titleIn, messageIn);
    }

    public void hideWatchBox() {

        getWatchBox().hide();
    }

    public boolean watchBoxShowing() {

        return getWatchBox().active();
    }

    public void hideTitleCloseButton() {
        _displayCloseButton = false;
        createHeader();
    }

    @Override
    public void setCloseVisible(boolean isVisibleIn) {
        _displayCloseButton = isVisibleIn;
        createHeader();
    }

    @Override
    public void hide() {
        
        if (_isShowing) {

            super.hide();
        }
    }
    
    //
    // Redefine the dialog header with a new title
    //
    @Override
    public void setTitle(String dialogTitleIn) {

        if ((null == _dialogTitle) || (!_dialogTitle.equals(dialogTitleIn))) {

            _dialogTitle = dialogTitleIn;
            createHeader();
        }
   }
    
    public String getTitle() {
        
        return _dialogTitle;
    }

    //
    // Define the dialog header with a title, help button and close button
    //
    public void defineHeader(String dialogTitleIn, String helpContextIn, boolean displayCloseButtonIn) {
        
        _dialogTitle = dialogTitleIn;
        _helpContext = helpContextIn;
        _helpCallback = null;
        _displayCloseButton = displayCloseButtonIn;
        createHeader();
    }

    //
    // Define the dialog header with a title, help button and close button
    //
    public void defineHeader(String dialogTitleIn, Callback<CsiModal> helpCallbackIn, boolean displayCloseButtonIn) {
        
        _dialogTitle = dialogTitleIn;
        _helpCallback = helpCallbackIn;
        _helpContext = null;
        _displayCloseButton = displayCloseButtonIn;
        createHeader();
    }
    
    //
    // Add a help button to the dialog header
    //
    public void setHelpCallback(Callback<CsiModal> helpCallbackIn) {
        
        _helpCallback = helpCallbackIn;
        _helpContext = null;
        createHeader();
    }
    
    //
    // Add a help button to the dialog header
    //
    public void setHelpContext(String helpContextIn) {
        
        _helpContext = helpContextIn;
        _helpCallback = null;
        createHeader();
    }
    
    public Callback<CsiModal> getHelpCallback() {
        
        return _helpCallback;
    }
    
    public String getHelpContext() {
        
        return _helpContext;
    }

    public void setOnHideCallBack(OnHideCallBack callbackIn) {

        onHideCallBack = callbackIn;
    }

    public Widget getBackdrop() {
	    return backdrop;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Protected Methods                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void retrieveResults(KnowsParent childIn) {

        // DO NOTHING -- Overriding method required to perform retrieval
    }

    protected ClickHandler handleCloseButtonClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {
            onHeaderCloseClick();
        }
    };

    @Override
    protected void onHide(Event e) {

        boolean myContinueFlag = true;

        if (null != onHideCallBack) {

            onHideCallBack.onHide(this);
        }
        super.onHide(e);
        backdrop.removeFromParent();
        removeFromParent();
        if (null != parent) {
            parent.show();
        }
        if (_isShowing) {

            removeFromList(_this, _isAlert);
            _isShowing = false;
        }
    }

    /**
     * Subclasses are expected to override this.
     */
    protected void onHeaderCloseClick() {
        hide();
        if (null != parent) {
            parent.show();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Private Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void init() {
        setHideOthers(false);
        setDynamicSafe(true);
        setBackdrop(BackdropType.NONE);
    }

    private void createHeader() {

        super.setTitle(null);
        addToHeader(new DialogHeader(this, _dialogTitle, _helpContext, (_displayCloseButton ? handleCloseButtonClick : null), 3));
    }

    private WatchBoxInterface getLocalWatchBox() {

        if (null == watchBox) {

            watchBox = WatchBox.getInstance();
        }
        return watchBox;
    }
}
