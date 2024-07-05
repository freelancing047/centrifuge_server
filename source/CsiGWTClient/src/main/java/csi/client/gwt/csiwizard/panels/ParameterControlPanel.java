package csi.client.gwt.csiwizard.panels;

import java.util.List;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;

import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.csiwizard.support.ParameterPanelSupport;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.query.QueryParameterDef;


public class ParameterControlPanel extends AbstractWizardPanel {

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    Label infoLabel;
    Label totalCountLabel;
    Label usedCountLabel;
    Label requiredCountLabel;
    RadioButton displayAllRadioButton;
    RadioButton displayUsedRadioButton;
    RadioButton displayRequiredRadioButton;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static String _txtControlTitle = _constants.parameterInfo_ControlTitle();
    private static String _txtControlInfo = _constants.parameterInfo_ControlInfo();
    private static String _txtDisplayAll = _constants.parameterInfo_DisplayAll();
    private static String _txtDisplayUsed = _constants.parameterInfo_DisplayUsed();
    private static String _txtDisplayRequired = _constants.parameterInfo_DisplayRequired();

    private List<QueryParameterDef> _fullParameterList = null;
    private List<QueryParameterDef> _usedParameterList = null;
    private List<QueryParameterDef> _requiredParameterList = null;
    private ClickHandler _handleRadioButtonClick = null;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public ParameterControlPanel(WizardInterface parentDialogIn, List<QueryParameterDef> parameterListIn,
                                 ClickHandler handleRadioButtonClickIn) throws CentrifugeException{

        super(parentDialogIn, _txtControlTitle, true);

        _fullParameterList = parameterListIn;
        _usedParameterList = ParameterPanelSupport.buildUsedParameterList(parameterListIn);
        _requiredParameterList = ParameterPanelSupport.buildRequiredParameterList(parameterListIn);
        _handleRadioButtonClick = handleRadioButtonClickIn;
        
        initializeObject();
    }
    
    public boolean doFilter() {
        
        return displayRequiredRadioButton.getValue();
    }
    
    public String getInstructions() {
        
        int myTotal = (null != _fullParameterList) ? _fullParameterList.size() : 0;
        int myRequired = (null != _requiredParameterList) ? _requiredParameterList.size() : 0;
        int myOptional = myTotal - myRequired;
        
        return _constants.parameterInfo_ControlInstructions(myTotal, myOptional, myRequired);
    }
    
    public List<QueryParameterDef> getParameterList() {
        
        return (displayAllRadioButton.getValue()
                    ? _fullParameterList
                    : displayUsedRadioButton.getValue()
                            ? _usedParameterList
                            : _requiredParameterList);
    }
    
    public int getPanelCount() {
        
        return 1 + getParameterList().size();
    }

    @Override
    public String getText() throws CentrifugeException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void grabFocus() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void enableInput() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isOkToLeave() {
        
        return true;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    protected void createWidgets(String descriptionIn, AbstractInputWidget inputCellIn) {

        infoLabel = new Label(_txtControlInfo);
        totalCountLabel = new Label(_constants.parameterInfo_TotalCount(_fullParameterList.size()));
        usedCountLabel = new Label(_constants.parameterInfo_TotalCount(_usedParameterList.size()));
        requiredCountLabel = new Label(_constants.parameterInfo_FilterCount(_requiredParameterList.size()));
        
        displayAllRadioButton = new RadioButton("FilterControl", _txtDisplayAll); //$NON-NLS-1$
        displayAllRadioButton.setValue(false, false);
        displayUsedRadioButton = new RadioButton("FilterControl", _txtDisplayUsed); //$NON-NLS-1$
        displayUsedRadioButton.setValue(true, false);
        displayRequiredRadioButton = new RadioButton("FilterControl", _txtDisplayRequired); //$NON-NLS-1$
        displayRequiredRadioButton.setValue(false, false);
        
        add(infoLabel);
        add(totalCountLabel);
        add(usedCountLabel);
        add(requiredCountLabel);
        add(displayAllRadioButton);
        add(displayUsedRadioButton);
        add(displayRequiredRadioButton);
    }

    @Override
    protected void layoutDisplay() throws CentrifugeException {

        int myRequestedHeight = (5 * Dialog.intLabelHeight) + (2 * Dialog.intMargin);
        int myHeight = (myRequestedHeight <= _height) ? myRequestedHeight : _height;
        int mySpacing = (myHeight == myRequestedHeight) ? Dialog.intMargin : (_height - (5 * Dialog.intLabelHeight)) / 2;
        int myTop = (_height - myHeight) / 2;
        int mySmallStep = Dialog.intLabelHeight;
        int myBigStep = (0 < mySpacing) ? (mySmallStep + mySpacing) : mySmallStep;

        setWidgetTopHeight(infoLabel, myTop, Unit.PX, Dialog.intLabelHeight, Unit.PX);
        setWidgetLeftRight(infoLabel, Dialog.intMargin, Unit.PX, Dialog.intMargin, Unit.PX);

        myTop += myBigStep;
        setWidgetTopHeight(totalCountLabel, myTop, Unit.PX, Dialog.intLabelHeight, Unit.PX);
        setWidgetLeftRight(totalCountLabel, Dialog.intMargin, Unit.PX, Dialog.intMargin, Unit.PX);

        myTop += mySmallStep;
        setWidgetTopHeight(requiredCountLabel, myTop, Unit.PX, Dialog.intLabelHeight, Unit.PX);
        setWidgetLeftRight(requiredCountLabel, Dialog.intMargin, Unit.PX, Dialog.intMargin, Unit.PX);

        myTop += myBigStep;
        setWidgetTopHeight(displayAllRadioButton, myTop, Unit.PX, Dialog.intLabelHeight, Unit.PX);
        setWidgetLeftRight(displayAllRadioButton, Dialog.intMargin, Unit.PX, Dialog.intMargin, Unit.PX);

        myTop += mySmallStep;
        setWidgetTopHeight(displayUsedRadioButton, myTop, Unit.PX, Dialog.intLabelHeight, Unit.PX);
        setWidgetLeftRight(displayUsedRadioButton, Dialog.intMargin, Unit.PX, Dialog.intMargin, Unit.PX);

        myTop += mySmallStep;
        setWidgetTopHeight(displayRequiredRadioButton, myTop, Unit.PX, Dialog.intLabelHeight, Unit.PX);
        setWidgetLeftRight(displayRequiredRadioButton, Dialog.intMargin, Unit.PX, Dialog.intMargin, Unit.PX);
    }

    @Override
    protected void wireInHandlers() {
        
        if (null != _handleRadioButtonClick) {
            
            displayAllRadioButton.addClickHandler(_handleRadioButtonClick);
            displayUsedRadioButton.addClickHandler(_handleRadioButtonClick);
            displayRequiredRadioButton.addClickHandler(_handleRadioButtonClick);
        }
    }

}
