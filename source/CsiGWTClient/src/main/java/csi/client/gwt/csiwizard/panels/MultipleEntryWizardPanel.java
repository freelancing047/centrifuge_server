package csi.client.gwt.csiwizard.panels;

import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.grid.Grid;

import csi.client.gwt.csiwizard.StringListManager;
import csi.client.gwt.csiwizard.support.SimpleStringStore;
import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.csiwizard.support.ParameterValidator;
import csi.client.gwt.events.CarriageReturnEvent;
import csi.client.gwt.events.CountChangeEvent;
import csi.client.gwt.events.CountChangeEventHandler;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.events.ValidityReportEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.WidgetDescriptor;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.Button;
import csi.server.common.exception.CentrifugeException;


////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                        //
//                          Basic text entry panel for CsiWizard                          //
//                                                                                        //
////////////////////////////////////////////////////////////////////////////////////////////

public class MultipleEntryWizardPanel extends SingleEntryWizardPanel {
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface ParameterValueProperties extends PropertyAccess<String> {
        ModelKeyProvider<String> key();
        ValueProvider<String, String> value();
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Embedded Classes                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    Label parameterListLabel = null;
    Button addButton = null;
    Grid<SimpleStringStore> dataGrid = null;
    

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    private static final String _txtDefaultListLabel = _constants.multipleSelector_DefaultListLabel();

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private int _buttonWidth = 55;

    private String _listLabel = null;
    private String _errorMessage = null;
    private StringListManager _parameterValueList = null;
    private List<String> _currentList = null;
    private List<String> _defaultList = null;
    private ParameterValidator _validator = null;
    private boolean _isReady = !isRequired();

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
        
            // Set the "Add" button accordingly
            addButton.setEnabled(eventIn.isValid());
            
            publishStatus();
       }
    };

    private CountChangeEventHandler handleCountChangeEvent
    = new CountChangeEventHandler() {

        @Override
        public void onCountChange(CountChangeEvent eventIn) {
            
            publishStatus();
        }
    };

    //
    // Handle clicking the "Add" button placed onto a parent dialog!!
    //
    public ClickHandler handleAddButtonClick
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {
            
            try {
                
                if (_parameterValueList.addValue(parameterInput.getText())) {
                    
                    fireEvent(new ValidityReportEvent(true));
                }
                parameterInput.resetValue();
                parameterInput.grabFocus();
                addButton.setEnabled(false);
            
            } catch (Exception myException) {
                
                Dialog.showException(myException);
            }
        }
    };

    @Override
    protected ClickHandler getDefaultButtonClickHandler() {

        return new ClickHandler() {
    
            @Override
            public void onClick(ClickEvent eventIn) {
            
                _parameterValueList.initGridValues(_defaultList);
           }
        };
    }

    @Override
    protected ClickHandler getCurrentButtonClickHandler() {
    
        return new ClickHandler() {

            @Override
            public void onClick(ClickEvent eventIn) {
            
                _parameterValueList.initGridValues(_currentList);
           }
        };
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public MultipleEntryWizardPanel(CanBeShownParent parentDialogIn, String nameIn, AbstractInputWidget inputCellIn, String labelIn,
            String listLabelIn, List<String> currentListIn, List<String> defaultListIn,
            boolean requiredIn) throws CentrifugeException {
        
        super(parentDialogIn, nameIn, requiredIn);

        _currentList = ((null  != currentListIn) && (0 < currentListIn.size())) ? currentListIn : null;
        _defaultList = ((null  != defaultListIn) && (0 < defaultListIn.size())) ? defaultListIn : null;
        _listLabel = (null != listLabelIn) ? listLabelIn : _txtDefaultListLabel;
        
        _currentValue = (null != _currentList) ? _currentList.get(0) : null;
        _defaultValue = (null != _defaultList) ? _defaultList.get(0) : null;

        initializeObject(labelIn, inputCellIn);
        inputCellIn.setRequired(true);

        if (null != parameterInput) {

            _validator = parameterInput.getValidator();
            
            //
            // Only use the validator at this level if it validates lists
            //
            if ((null != _validator) && (!_validator.isListValidator())) {
                
                _validator = null;
            }
        }
    }
    
    public MultipleEntryWizardPanel(CanBeShownParent parentDialogIn, String nameIn, AbstractInputWidget inputCellIn, String labelIn,
            String listLabelIn, List<String> currentListIn, List<String> defaultListIn) throws CentrifugeException {

        this(parentDialogIn, nameIn, inputCellIn, labelIn, listLabelIn, currentListIn, defaultListIn, true);
    }
    
    public MultipleEntryWizardPanel(CanBeShownParent parentDialogIn, String nameIn, AbstractInputWidget inputCellIn, String labelIn,
            List<String> currentListIn, List<String> defaultListIn, boolean requiredIn) throws CentrifugeException {

        this(parentDialogIn, nameIn, inputCellIn, labelIn, null, currentListIn, defaultListIn, requiredIn);
    }
    
    public MultipleEntryWizardPanel(CanBeShownParent parentDialogIn, String nameIn, AbstractInputWidget inputCellIn, String labelIn,
            List<String> currentListIn, List<String> defaultListIn) throws CentrifugeException {

        this(parentDialogIn, nameIn, inputCellIn, labelIn, null, currentListIn, defaultListIn, true);
    }
    
    public MultipleEntryWizardPanel(CanBeShownParent parentDialogIn, AbstractInputWidget inputCellIn, String labelIn,
            String listLabelIn, boolean requiredIn) throws CentrifugeException {
        
        this(parentDialogIn, null, inputCellIn, labelIn, listLabelIn, null, null, requiredIn);
    }
    
    public MultipleEntryWizardPanel(CanBeShownParent parentDialogIn, AbstractInputWidget inputCellIn, String labelIn,
            boolean requiredIn) throws CentrifugeException {
        
        this(parentDialogIn, null, inputCellIn, labelIn, null, null, null, requiredIn);
    }
    
    public MultipleEntryWizardPanel(CanBeShownParent parentDialogIn, AbstractInputWidget inputCellIn, String labelIn,
            String listLabelIn) throws CentrifugeException {
        
        this(parentDialogIn, null, inputCellIn, labelIn, listLabelIn, null, null, true);
    }
    
    public MultipleEntryWizardPanel(CanBeShownParent parentDialogIn, AbstractInputWidget inputCellIn, String labelIn)
            throws CentrifugeException {
        
        this(parentDialogIn, null, inputCellIn, labelIn, null, null, null, true);
    }

    public AbstractInputWidget getInputWidget() {

        return parameterInput;
    }

    @Override
    public String getText() {
        
        List<String> myList = getList();
        
        return (null != myList) ? myList.get(0) : null;
    }

    @Override
    public List<String> getList() {
        
        List<String> myList = null;
        
        if (null != _parameterValueList) {
            
            myList = _parameterValueList.getList();
        }
        return myList;
    }

    @Override
    public boolean isOkToLeave() {

        boolean myReady = !(isRequired() && (0 == dataGrid.getStore().size())) && parameterInput.atReset();
        
        if (null != _validator) {
            
            _errorMessage = _validator.validateList(getList());
            
            if (null != _errorMessage) {
                
                myReady = false;
                parameterListLabel.setText(_errorMessage);

            } else {
                
                parameterListLabel.setText(_listLabel);
            }
        }
        return myReady;
    }
    
    @Override
    public void beginMonitoring() {
        
        parameterInput.beginMonitoring();
        fireEvent(new ValidityReportEvent(isOkToLeave()));
    }

    @Override
    public void handleCarriageReturn() {
        
        if (addButton.isEnabled()) {
            
            addButton.click();
            
        } else {
            
            fireEvent(new CarriageReturnEvent(isOkToLeave()));
        }
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
        addButton.addClickHandler(handleAddButtonClick);
        _parameterValueList.addCountChangeEventHandler(handleCountChangeEvent);
    }
    
    @Override
    protected boolean layoutAccumulationDisplay() {

        int myListTop = _boxTop + Dialog.intLabelHeight;
        int myListHeight = _boxHeight - Dialog.intLabelHeight;
       
        setWidgetTopHeight(parameterListLabel, _boxTop, Unit.PX, Dialog.intLabelHeight, Unit.PX);
        setWidgetLeftRight(parameterListLabel, Dialog.intMargin, Unit.PX, Dialog.intMargin, Unit.PX);

        _parameterValueList.setPixelSize(_boxWidth, myListHeight);
        
        setWidgetTopHeight(dataGrid, myListTop, Unit.PX, myListHeight, Unit.PX);
        setWidgetLeftRight(dataGrid, Dialog.intMargin, Unit.PX, Dialog.intMargin, Unit.PX);
        
        return true;
    }
    
    @Override
    protected void createWidgets(String labelIn, AbstractInputWidget inputCellIn) {
        
        int myListHeight = _boxHeight - Dialog.intLabelHeight;
        
        if (null != inputCellIn) {
            
            super.createWidgets(labelIn, inputCellIn);

            parameterListLabel = new Label();
            parameterListLabel.setText(_listLabel);
            add(parameterListLabel);
            
            _parameterValueList = new StringListManager(_boxWidth, myListHeight, inputCellIn.getComparator());
            dataGrid = _parameterValueList.getGrid();
            
            if (null != _currentList) {
                
                _parameterValueList.initGridValues(_currentList);
                
            } else if (null != _defaultList) {
                
                _parameterValueList.initGridValues(_defaultList);
            }
            add(dataGrid);

            addButton = new Button();
            addButton.setText(i18n.multipleEntryWizardAddButton()); //$NON-NLS-1$
            addButton.setEnabled(parameterInput.isValid());
            
            parameterInput.placeAddWidget(new WidgetDescriptor(addButton, Dialog.intButtonHeight, _buttonWidth));
        }
    }

    @Override
    protected int getRequiredExtendedHeight() {
        
        return 100;
    }
    
    @Override
    protected int getRequestedExtendedHeight() {

        return 135;
    }
    
    private void publishStatus() {
        
        boolean myOldReady = _isReady;
        
        _isReady = isOkToLeave();
        
        if (_isReady != myOldReady) {
            
            if (_isReady) {
                
                parameterListLabel.getElement().getStyle().setColor(Dialog.txtLabelColor);
                
            } else {
                
                parameterListLabel.getElement().getStyle().setColor(Dialog.txtErrorColor);
            }
            
            fireEvent(new ValidityReportEvent(_isReady));
        }
    }
}
