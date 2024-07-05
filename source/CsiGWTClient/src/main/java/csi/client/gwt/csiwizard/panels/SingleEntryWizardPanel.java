package csi.client.gwt.csiwizard.panels;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.csiwizard.widgets.BooleanInputWidget;
import csi.client.gwt.csiwizard.widgets.FilteredPasswordTextBox;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.events.ValidityReportEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.buttons.SimpleButton;
import csi.server.common.exception.CentrifugeException;


////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                        //
//                          Basic text entry panel for CsiWizard                          //
//                                                                                        //
////////////////////////////////////////////////////////////////////////////////////////////

public class SingleEntryWizardPanel extends AbstractWizardPanel {
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    protected VerticalPanel infoPanel;
    protected Label parameterLabel = null;
    protected AbstractInputWidget parameterInput = null;
    HorizontalPanel buttonPanel = null;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected final int _requiredInfoHeight = Dialog.intLabelHeight + Dialog.intMargin;
    protected final int _requestedInfoHeight = (3 * Dialog.intLabelHeight) + Dialog.intMargin;
    protected int _infoTop = 0;
    protected int _infoHeight = _requestedInfoHeight;
    protected int _boxHeight = getRequestedExtendedHeight();
    protected int _boxTop = _height - _boxHeight;
    protected int _boxWidth = _width - (2 * Dialog.intMargin);
    protected int _inputTop = _infoTop + _infoHeight;
    protected int _inputHeight = _boxTop - _inputTop;

    protected String _currentValue = null;
    protected String _defaultValue = null;


    protected boolean __isValid = false;
    protected boolean __atReset = true;
    protected boolean __isRequired;

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Abstract Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ValidityReportEventHandler handleValidityReportEvent
    = new ValidityReportEventHandler() {

        @Override
        public void onValidityReport(ValidityReportEvent eventIn) {
        
            fireEvent(new ValidityReportEvent(eventIn.isValid()));
       }
    };

    protected ClickHandler getDefaultButtonClickHandler() {

        return new ClickHandler() {
    
            @Override
            public void onClick(ClickEvent eventIn) {
            
                parameterInput.setValue(_defaultValue);
           }
        };
    }

    protected ClickHandler getCurrentButtonClickHandler() {
    
        return new ClickHandler() {

            @Override
            public void onClick(ClickEvent eventIn) {
            
                parameterInput.setValue(_currentValue);
           }
        };
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public SingleEntryWizardPanel(CanBeShownParent parentDialogIn, String nameIn, boolean requiredIn) {
        
        super(parentDialogIn, nameIn, requiredIn);
    }
        
    public SingleEntryWizardPanel(CanBeShownParent parentDialogIn, String nameIn, AbstractInputWidget inputCellIn,
                                  String descriptionIn, String currentIn, String defaultIn, boolean requiredIn)
            throws CentrifugeException {
            
        super(parentDialogIn, nameIn, requiredIn);
        
        _currentValue = ((null != currentIn) && (0 < currentIn.length())) ? currentIn : null;
        _defaultValue = ((null != defaultIn) && (0 < defaultIn.length())) ? defaultIn : null;

        //
        // Initialize the display objects
        //
        initializeObject(descriptionIn, inputCellIn);
    }

    public SingleEntryWizardPanel(CanBeShownParent parentDialogIn, String nameIn, AbstractInputWidget inputCellIn) throws CentrifugeException {

        this(parentDialogIn, nameIn, inputCellIn, null, null, null, true);
    }

    public SingleEntryWizardPanel(CanBeShownParent parentDialogIn, String nameIn, AbstractInputWidget inputCellIn, String descriptionIn) throws CentrifugeException {

        this(parentDialogIn, nameIn, inputCellIn, descriptionIn, null, null, true);
    }

    public SingleEntryWizardPanel(CanBeShownParent parentDialogIn, String nameIn, AbstractInputWidget inputCellIn, String descriptionIn,
                                  String currentIn, String defaultIn) throws CentrifugeException {
    
        this(parentDialogIn, nameIn, inputCellIn, descriptionIn, currentIn, defaultIn, true);
    }

    @Override
    public AbstractInputWidget getInputWidget() {

        return parameterInput;
    }

    public String getKey() {
        
        return getPanelName();
    }
    
    public String getText() throws CentrifugeException {
        
        return (null != parameterInput)
                    ? ((parameterInput instanceof FilteredPasswordTextBox)
                            ? ((FilteredPasswordTextBox)parameterInput).getPassword()
                            : parameterInput.getText())
                    : null;
    }
    
    public List<String> getList() throws CentrifugeException {
        
        List<String> myList = null;
        String myValue = parameterInput.getText();
        
        if (null != myValue) {
            
            myList = new ArrayList<String>();
            myList.add(myValue);
        }
        return myList;
    }
    
    public void enableInput() {
    }
    
    public void grabFocus() {
        
        parameterInput.grabFocus();
    }

    @Override
    public boolean isOkToLeave() {

        __isValid = parameterInput.isValid();
        __atReset = parameterInput.atReset();
        __isRequired = isRequired();
        
        return ((parameterInput instanceof BooleanInputWidget)
                || (__isValid && (!__atReset))
                || ((!__isRequired) && __atReset));
    }
    
    @Override
    public void suspendMonitoring() {

        parameterInput.suspendMonitoring();
    }
    
    @Override
    public void beginMonitoring() {
        
        parameterInput.beginMonitoring();
        fireEvent(new ValidityReportEvent(isOkToLeave()));
    }

    @Override
    public void destroy() {
        
        parameterInput.suspendMonitoring();
        parameterInput.destroy();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    protected void wireInHandlers() {
        
        //
        // Set up handlers to capture changes in validity status
        //
        parameterInput.addValidityReportEventHandler(handleValidityReportEvent);
    }

    protected void layoutDisplay() throws CentrifugeException {

        int myRequiredInfoHeight = (null != infoPanel) ? _requiredInfoHeight : 0;
        int myRequestedInfoHeight = (null != infoPanel) ? _requestedInfoHeight : 0;
        int myButtonReserve = (null != buttonPanel) ? Dialog.intButtonHeight + Dialog.intMargin : 0;
        int myPanelRequestedHeight = getRequestedExtendedHeight() + myRequestedInfoHeight;
        int myWidgetRequestedHeight = parameterInput.getRequestedHeight();
        int myHeight = _height - myButtonReserve;

        if (myHeight >= (myPanelRequestedHeight + myWidgetRequestedHeight)) {

            _infoTop = Dialog.intMargin;
            _infoHeight = myRequestedInfoHeight;
            _inputHeight = parameterInput.getRequestedHeight();
            _boxTop = myHeight - _boxHeight;
            _inputTop = _infoTop + _infoHeight + (((_boxTop - (_infoTop + _infoHeight)) - _inputHeight) / 2);

        } else {
            
            int myPanelRequiredHeight = getRequiredExtendedHeight() + myRequiredInfoHeight;
            int myWidgetRequiredHeight = Math.min((myHeight - myPanelRequiredHeight),
                                                    parameterInput.getRequiredHeight());
            int myTotalRequiredHeight = myPanelRequiredHeight + myWidgetRequiredHeight;

            _infoTop = 0;

            if (myHeight == myTotalRequiredHeight) {

                _infoHeight = myRequiredInfoHeight;
                _boxHeight = getRequiredExtendedHeight();

            } else if (myHeight > myTotalRequiredHeight) {

                _infoHeight = (myRequiredInfoHeight * myHeight) / myTotalRequiredHeight;
                _boxHeight = (getRequiredExtendedHeight() * myHeight) / myTotalRequiredHeight;

            } else {
                
                throw new CentrifugeException(i18n.singleEntryWizardException()); //$NON-NLS-1$
            }
            _boxTop = myHeight - _boxHeight;
            _inputTop = _infoTop + _infoHeight;
            _inputHeight = _boxTop - _inputTop;
        }
        
        if (null != infoPanel) {
            
            infoPanel.setPixelSize(_boxWidth, _infoHeight);
            setWidgetTopHeight(infoPanel, _infoTop, Unit.PX, _infoHeight, Unit.PX);
            setWidgetLeftRight(infoPanel, Dialog.intMargin, Unit.PX, Dialog.intMargin, Unit.PX);
        }
        
        layoutAccumulationDisplay();
        
        if (null != parameterInput) {
            
            parameterInput.setPixelSize(_boxWidth, _inputHeight);
            setWidgetTopHeight(parameterInput, _inputTop, Unit.PX, _inputHeight, Unit.PX);
            setWidgetLeftRight(parameterInput, Dialog.intMargin, Unit.PX, Dialog.intMargin, Unit.PX);
        }
        
        if (null != buttonPanel) {

            setWidgetBottomHeight(buttonPanel, 0, Unit.PX, Dialog.intButtonHeight, Unit.PX);
            setWidgetLeftRight(buttonPanel, Dialog.intMargin, Unit.PX, Dialog.intMargin, Unit.PX);
        }
    }
    
    protected boolean layoutAccumulationDisplay() {
        
        return false;
    }
    
    protected void createWidgets(String descriptionIn, AbstractInputWidget inputCellIn) {
        
        if (null != inputCellIn) {
            
            if ((null != descriptionIn) && (0 < descriptionIn.length())) {

                parameterLabel = new Label();
                parameterLabel.setText(descriptionIn);

                infoPanel = new VerticalPanel();
                infoPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
                infoPanel.add(parameterLabel);
                
                add(infoPanel);
            }
            
            parameterInput = inputCellIn;
            add(parameterInput);
            
            if ((null != _currentValue) || (null != _defaultValue)) {
                
                buttonPanel = new HorizontalPanel();
                
                buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
                
                if (null != _defaultValue) {
                    
                    SimpleButton myButton = new SimpleButton(i18n.singleEntryWizardDefaultButton()); //$NON-NLS-1$
                    myButton.addClickHandler(getDefaultButtonClickHandler());
                    buttonPanel.add(myButton);
                    
                    if (null != _currentValue) {
                        
                        Label mySpacer =  new Label(i18n.plusplus()); //$NON-NLS-1$
                        mySpacer.getElement().getStyle().setColor(Dialog.txtPanelColor);
                        buttonPanel.add(mySpacer);
                    }
                }
                
                if (null != _currentValue) {
                    
                    SimpleButton myButton = new SimpleButton(i18n.singleEntryWizardCurrentButton()); //$NON-NLS-1$
                    myButton.addClickHandler(getCurrentButtonClickHandler());
                    buttonPanel.add(myButton);
                }
                add(buttonPanel);
            }
        }
    }
    
    protected int getRequiredExtendedHeight() {
        
        return 0;
    }
    
    protected int getRequestedExtendedHeight() {

        return getRequiredExtendedHeight();
    }
}
