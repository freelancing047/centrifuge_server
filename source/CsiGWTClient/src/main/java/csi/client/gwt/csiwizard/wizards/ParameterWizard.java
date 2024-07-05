package csi.client.gwt.csiwizard.wizards;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.csiwizard.Wizard;
import csi.client.gwt.csiwizard.panels.AbstractWizardPanel;
import csi.client.gwt.csiwizard.support.ParameterPanelSet;
import csi.client.gwt.csiwizard.support.ParameterPanels;
import csi.client.gwt.events.UserInputEvent;
import csi.client.gwt.events.UserInputEventHandler;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.LaunchParam;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.ParameterHelper;

public class ParameterWizard extends Wizard {

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
 
    private ParameterPanels _panels;
    private UserInputEventHandler<List<LaunchParam>> _callBack;

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    private ClickHandler handleRadioButtonClick
    = new ClickHandler() {
        
        public void onClick(ClickEvent eventIn) {
            
            _finalDisplayIndex = _panels.getPanelCount() -  1;
            handleValidityReportEvent.onValidityReport(new ValidityReportEvent(true));
        }
    };

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ParameterWizard(List<QueryParameterDef> parameterListIn,
                            UserInputEventHandler<List<LaunchParam>> callBackIn)
                                    throws CentrifugeException {
        
        this(i18n.parameterWizardTitle(), null, Dialog.txtLaunchButton, parameterListIn, callBackIn); //$NON-NLS-1$
    }

    public ParameterWizard(String titleIn, String helpTargetIn,
            String finalizeButtonIn, List<QueryParameterDef> parameterListIn,
            UserInputEventHandler<List<LaunchParam>> callBackIn) throws CentrifugeException {
        
        super(titleIn, helpTargetIn, finalizeButtonIn);

        List<QueryParameterDef> myParameterList = ParameterHelper.filter(parameterListIn);

        if ((null != myParameterList) && (0 < myParameterList.size())) {

            _panels = new ParameterPanels(this, myParameterList, handleRadioButtonClick);

        } else {

            _panels = null;
        }
        _callBack = callBackIn;
    }
    
    public void show() {

        if (null != _panels) {

            displayNewPanel(0, null);
            handleValidityReportEvent.onValidityReport(new ValidityReportEvent(true));

        } else {

            _callBack.onUserInput(new UserInputEvent<List<LaunchParam>>(new ArrayList<LaunchParam>(), false));
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void execute(AbstractWizardPanel activePanelIn, ClickEvent eventIn) {
        
        try {
            
            _callBack.onUserInput(new UserInputEvent<List<LaunchParam>>(_panels.gatherParameterData(), false));

        } catch (Exception myException) {
            
            Dialog.showException(myException);
            
            _callBack.onUserInput(new UserInputEvent<List<LaunchParam>>(true));
        }

        //
        // free resources and leave
        //
        destroy();
    }

    @Override
    protected void cancel(AbstractWizardPanel activePanelIn, ClickEvent eventIn) {

        _callBack.onUserInput(new UserInputEvent<List<LaunchParam>>(true));
    }

    @Override
    protected void displayNewPanel(int indexIn, ClickEvent eventIn) {

        ParameterPanelSet myPanelSet = _panels.getPanel(indexIn);
        
        if (null != myPanelSet) {
            
            _finalDisplayIndex = myPanelSet.getCount() - 1;

            displayPanel(myPanelSet.getPanel(), myPanelSet.getInstructions());
        }
    }

}
