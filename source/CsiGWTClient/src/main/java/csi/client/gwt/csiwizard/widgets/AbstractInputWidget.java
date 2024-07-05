package csi.client.gwt.csiwizard.widgets;

import java.util.Comparator;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.csiwizard.panels.AbstractWizardPanel;
import csi.client.gwt.csiwizard.support.ParameterValidator;
import csi.client.gwt.events.CarriageReturnEvent;
import csi.client.gwt.events.CarriageReturnEventHandler;
import csi.client.gwt.events.EscapeKeyEvent;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.events.ValidityReportEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.WidgetDescriptor;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.exception.CentrifugeException;

public abstract class AbstractInputWidget extends LayoutPanel implements HasHandlers, RequiresResize, ProvidesResize {

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    Label parameterPrompt = null;
    protected Widget addButton = null;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    protected static int _displayTop = 0;
    protected static int _buttonAlignmentTop = _displayTop + Dialog.intLabelHeight;

    protected ParameterValidator _validator = null;
    protected int _margin = 0;
    protected boolean _required = true;

    private int _height = 0;
    private int _width = 0;
    private int _marginCount = 2;
    private int _buttonHeight = 0;
    private int _buttonWidth = 0;
    private boolean _processingRequest = false;
    private Boolean _isValid = null;

    private HandlerManager _handlerManager;
    private AbstractWizardPanel _panel = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Abstract Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    public abstract String getText() throws CentrifugeException;
    public abstract void resetValue();
    public abstract boolean isValid();
    public abstract boolean atReset();
    public abstract void grabFocus();
    public abstract int getRequiredHeight();
    protected abstract void layoutDisplay();


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public AbstractInputWidget(AbstractWizardPanel panelIn, WidgetDescriptor buttonIn, boolean requiredIn) {

        try {

            _panel = panelIn;
            _handlerManager = new HandlerManager(this);

            if (null != buttonIn) {

                addButton = buttonIn.getWidget();
                _buttonHeight = buttonIn.getHeight();
                _buttonWidth = buttonIn.getWidth();
            }
            _required = requiredIn;

        } catch (Exception myException) {

            Dialog.showException("AbstractInputWidget", myException);
        }
    }

    public AbstractInputWidget(AbstractWizardPanel panelIn, WidgetDescriptor buttonIn) {

        this(panelIn, buttonIn, true);
    }

    public AbstractInputWidget(AbstractWizardPanel panelIn) {

        this(panelIn, null, true);
    }

    public AbstractInputWidget(WidgetDescriptor buttonIn, boolean requiredIn) {

        try {

            _handlerManager = new HandlerManager(this);

            if (null != buttonIn) {

                addButton = buttonIn.getWidget();
                _buttonHeight = buttonIn.getHeight();
                _buttonWidth = buttonIn.getWidth();
            }
            _required = requiredIn;

        } catch (Exception myException) {

            Dialog.showException("AbstractInputWidget", myException);
        }
    }

    public AbstractInputWidget(WidgetDescriptor buttonIn) {

        this(buttonIn, true);
    }

    public AbstractInputWidget(boolean requiredIn) {

        this(null, requiredIn);
    }

    public AbstractInputWidget() {

        this(null, true);
    }

    public void setRequired(boolean requiredIn) {

        _required = requiredIn;
    }

    public void placeAddWidget(WidgetDescriptor buttonIn) {

        try {

            if (null != buttonIn) {

                addButton = buttonIn.getWidget();
                if (null != addButton) {

                    _buttonHeight = buttonIn.getHeight();
                    _buttonWidth = buttonIn.getWidth();
                    add(addButton);
                }
            }

        } catch (Exception myException) {

            Dialog.showException("AbstractInputWidget", myException);
        }
    }

    //
    // Fire requested event
    //
    @Override
    public void fireEvent(GwtEvent<?> eventIn) {

        try {

            _handlerManager.fireEvent(eventIn);

        } catch (Exception myException) {

            Dialog.showException("AbstractInputWidget", myException);
        }
    }

    public HandlerRegistration addValidityReportEventHandler(ValidityReportEventHandler handler) {

        try {

            HandlerRegistration myRegistration =  _handlerManager.addHandler(ValidityReportEvent.type, handler);
            fireEvent(new ValidityReportEvent((null != _isValid) ? _isValid : false, atReset()));
            return myRegistration;

        } catch (Exception myException) {

            Dialog.showException("AbstractInputWidget", myException);
        }
        return null;
    }

    public HandlerRegistration addCarriageReturnEventHandler(CarriageReturnEventHandler handler) {

        try {

            return _handlerManager.addHandler(CarriageReturnEvent.type, handler);

        } catch (Exception myException) {

            Dialog.showException("AbstractInputWidget", myException);
        }
        return null;
    }
    
    @Override
    public void setHeight(String stringIn) {

        try {

            if (_processingRequest) {

                super.setHeight(stringIn);

            } else {

                forceDimensions(_width, decode(stringIn));
            }

        } catch (Exception myException) {

            Dialog.showException("AbstractInputWidget", myException);
        }
    }
    
    @Override
    public void setWidth(String stringIn) {

        try {

            if (_processingRequest) {

                super.setWidth(stringIn);

            } else {

                forceDimensions(decode(stringIn), _height);
            }

        } catch (Exception myException) {

            Dialog.showException("AbstractInputWidget", myException);
        }
    }
    
    @Override
    public void setPixelSize(int widthIn, int heightIn) {

        try {

            if (_processingRequest) {

                super.setPixelSize(widthIn, heightIn);
            } else {

                forceDimensions(widthIn, heightIn);
            }

        } catch (Exception myException) {

            Dialog.showException("AbstractInputWidget", myException);
        }
    }

    public void setMarginCount(int marginCountIn) {

        _marginCount = marginCountIn;
    }
    
    public int getRequestedHeight() {

        return getRequiredHeight() + (_marginCount * Dialog.intMargin);
    }

    public void suspendMonitoring() {
        
    }
    
    public void beginMonitoring() {

    }

    public void destroy() {
        
        suspendMonitoring();
    }

    public ParameterValidator getValidator() {
        
        return _validator;
    }

    public void setValue(String valueIn) {
        
    }

    public AbstractWizardPanel getPanel() {

        return _panel;
    }

    public void setPanel(AbstractWizardPanel panelIn) {

        _panel = panelIn;
    }

    public void handleCarriageReturn() {

        try {

            fireEvent(new CarriageReturnEvent(isValid()));

        } catch (Exception myException) {

            Dialog.showException("AbstractInputWidget", myException);
        }
    }

    public void handleEscapeKey() {

        try {

            fireEvent(new EscapeKeyEvent());

        } catch (Exception myException) {

            Dialog.showException("AbstractInputWidget", myException);
        }
    }

    public void clearAlert() {

        if (null != _panel) {

            _panel.clearAlert();
        }
    }

    public void setAlert(String textIn, String colorIn) {

        try {

            if (null != _panel) {

                _panel.setAlert(textIn, colorIn);
            }

        } catch (Exception myException) {

            Dialog.showException("AbstractInputWidget", myException);
        }
    }

    public Comparator<String> getComparator() {

        return null;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void reportValidity(boolean isValidIn, boolean atResetIn) {

        fireEvent(new ValidityReportEvent(isValidIn, atResetIn));
    }

    protected void reportValidity(boolean isValidIn) {

        boolean myValid = isValidIn || ((!_required) && atReset());

        /* if ((null == _isValid) || (_isValid != myValid)) */
        {

            _isValid = myValid;
            fireEvent(new ValidityReportEvent(myValid, atReset()));
        }
    }

    protected void reportValidity() {

        boolean myValid = (!_required) && atReset();

        if (!myValid) {

            myValid = isValid();
        }

        /* if ((null == _isValid) || (_isValid != myValid)) */
        {

            _isValid = myValid;
            fireEvent(new ValidityReportEvent(myValid, atReset()));
        }
    }

    protected void reportCarriageReturn(boolean isValidIn) {
        
        boolean myValid = isValidIn || ((!_required) && atReset());

        fireEvent(new CarriageReturnEvent(myValid));
    }
    
    protected int getRightMargin() {
        
        return (null != addButton) ? (_buttonWidth + 10) : 0;
    }
    
    protected int getWidth() {
        
        return (null != addButton) ? (_width - _buttonWidth - 10) : _width;
    }
    
    protected int getHeight() {
        
        return _height;
    }
    
    protected int getButtonWidth() {
        
        return _buttonWidth;
    }
    
    protected int getButtonHeight() {
        
        return _buttonHeight;
    }
    
    protected void centerAddButton() {
        
        if (null != addButton) {
            
            int myTop = (_height - _buttonHeight - _buttonAlignmentTop) / 2;
            setWidgetTopHeight(addButton, myTop, Unit.PX, _buttonHeight, Unit.PX);
            setWidgetRightWidth(addButton, 0, Unit.PX, _buttonWidth, Unit.PX);
        }
    }

    protected int decode(String valueIn) {

        int myValue = 0;

        for (int i = 0; valueIn.length() > i; i++) {

            int myDigit = (int)valueIn.charAt(i) - (int)('0');

            if ((0 <= myDigit) && (9 >= myDigit)) {

                myValue = (myValue * 10) + myDigit;

            } else {

                break;
            }
        }
        return myValue;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    private void forceDimensions(int widthIn, int heightIn) {
        
        _processingRequest = true;
        _width = widthIn;
        _height = heightIn;
        super.setPixelSize(_width, _height);
        _processingRequest = false;
        
        if ((0 < _width) && (0 < _height)) {
            
            _margin = (0 < _marginCount)
                            ? (getRequestedHeight() <= _height)
                                    ? Dialog.intMargin
                                    : ((_height - getRequiredHeight()) / _marginCount)
                            : 0;
            
            if (null != parameterPrompt) {
                
                _buttonAlignmentTop = _displayTop + Dialog.intLabelHeight;
            }
            
            layoutDisplay();
            
            if (null != addButton) {
                
                centerAddButton();
            }
        }
    }
}
