package csi.client.gwt.csiwizard.wizards;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.Wizard;
import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.csiwizard.dialogs.DataViewFromScratchSave;
import csi.client.gwt.csiwizard.dialogs.InstalledTableSave;
import csi.client.gwt.csiwizard.panels.AbstractWizardPanel;
import csi.client.gwt.csiwizard.dialogs.DataViewFromTemplateSave;
import csi.client.gwt.csiwizard.support.ParameterPanelSet;
import csi.client.gwt.csiwizard.support.ParameterPanels;
import csi.client.gwt.events.TransferCompleteEventHandler;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.AuthDO;
import csi.server.common.dto.LaunchParam;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.service.api.TestActionsServiceProtocol;


/**
 * Created by centrifuge on 6/14/2017.
 */
public class ResourceParameterWizard extends Wizard {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private AdHocDataSource _dataSource = null;
    private ResourceBasics _template = null;
    private boolean _sample = false;
    private ResourceParameterWizard _this = this;
    private String _title;
    private String _help;

    private ParameterPanels _parameterPanels = null;
    private List<LaunchParam> _parameterData = null;
    private List<DataSourceDef> _requiredAuthorizations = null;
    private List<QueryParameterDef> _parameters = null;
    private TransferCompleteEventHandler _handler = null;
    private int _index = 0;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected VortexEventHandler<Response<Object, List<QueryParameterDef>>> requirementsResponseHandler
            = new AbstractVortexEventHandler<Response<Object, List<QueryParameterDef>>>() {

        @Override
        public boolean onError(Throwable myException) {

            hideWatchBox();
            Display.error("ResourceParameterWizard", 1, myException);

            return false;
        }
        @Override
        public void onSuccess(Response<Object, List<QueryParameterDef>> responseIn) {

            try{

                if (ResponseHandler.isSuccess(responseIn)) {

                    if (responseIn.isAuthorizationRequired()) {

                        _requiredAuthorizations = responseIn.getAuthorizationList();
                    }
                    _parameters = responseIn.getResult();

                    if ((null != _parameters) && (0 < _parameters.size())) {

                        _parameterPanels = new ParameterPanels(_this, _parameters, handleRadioButtonClick);
                        _finalDisplayIndex = (null != _parameterPanels) ? _parameterPanels.getPanelCount() -1 : -1;

                        if (0 < _finalDisplayIndex) {

                            hideWatchBox();
                            // Display first panel and exit this routine to avoid default action.
                            displayNewPanel(0, null);
                            return;
                        }
                    }
                }
                // Found no panels to process, so remove this segment from wizard list and go to next.
                launch(getPriorDialog());
                destroy();

            } catch (Exception myException) {

                Display.error("ResourceParameterWizard", 2, myException);
            }
        }
    };

    private ClickHandler handleRadioButtonClick
            = new ClickHandler() {

        public void onClick(ClickEvent eventIn) {

            try {

                _finalDisplayIndex = _parameterPanels.getPanelCount() - 1;
                handleValidityReportEvent.onValidityReport(new ValidityReportEvent(true));

            } catch (Exception myException) {

                Display.error("ResourceParameterWizard", 3, myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    public ResourceParameterWizard(ResourceBasics templateIn, String titleIn, String helpIn) {

        super(titleIn, helpIn);

        try {

            _title = titleIn;
            _help = helpIn;
            _template = templateIn;
            _chainForward = true;

        } catch (Exception myException) {

            Display.error("ResourceParameterWizard", 4, myException);
        }
    }

    //
    //
    //
    public ResourceParameterWizard(WizardInterface priorDialogIn, ResourceBasics templateIn,
                                   String titleIn, String helpIn, boolean sampleIn) {

        super(titleIn, helpIn, priorDialogIn);

        try {

            _title = titleIn;
            _help = helpIn;
            _template = templateIn;
            _sample = sampleIn;
            _chainForward = true;

        } catch (Exception myException) {

            Display.error("ResourceParameterWizard", 5, myException);
        }
    }

    //
    //
    //
    public ResourceParameterWizard(WizardInterface priorDialogIn, AdHocDataSource dataSourceIn,
                                   String titleIn, String helpIn, TransferCompleteEventHandler handlerIn) {

        super(titleIn, helpIn, priorDialogIn);

        try {

            _title = titleIn;
            _help = helpIn;
            _dataSource = dataSourceIn;
            _handler = handlerIn;
            _chainForward = true;

        } catch (Exception myException) {

            Display.error("ResourceParameterWizard", 6, myException);
        }
    }

    @Override
    public void show() {

        try {

            if (_hiding) {

                super.show();

            } else {

                getLaunchRequirements();
            }

        } catch (Exception myException) {

            Display.error("ResourceParameterWizard", 7, myException);
        }
    }

    @Override
    public void destroy() {

        _parameterPanels = null;
        _parameterData = null;

        super.destroy();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void execute(AbstractWizardPanel activePanelIn, ClickEvent eventIn) {

        try {

            _parameterData = _parameterPanels.gatherParameterData();
            launch(this);

        } catch (Exception myException) {

            Display.error("ResourceParameterWizard", 8, myException);
        }
    }

    @Override
    protected void cancel(AbstractWizardPanel activePanelIn, ClickEvent eventIn) {

    }

    @Override
    protected void displayNewPanel(int indexIn, ClickEvent eventIn) {

        try {

            if(_finalDisplayIndex >= indexIn) {

                ParameterPanelSet myPanelSet = _parameterPanels.getPanel(indexIn);

                _index = indexIn;
                _finalDisplayIndex = myPanelSet.getCount() - 1;
                displayPanel(myPanelSet.getPanel(), myPanelSet.getInstructions());
            }

        } catch (Exception myException) {

            Display.error("ResourceParameterWizard", 9, myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    private void getLaunchRequirements() {

        VortexFuture<Response<Object, List<QueryParameterDef>>> myVortexFuture
                = WebMain.injector.getVortex().createFuture();

        try {

            List<AuthDO> myAuthorizationList = WebMain.injector.getMainPresenter().getAuthorizationList();

            myVortexFuture.addEventHandler(requirementsResponseHandler);
            if (null != _template) {

                myVortexFuture.execute(TestActionsServiceProtocol.class).getLaunchRequirements(_template.getUuid(),
                                                                                                myAuthorizationList);

            } else if (null != _dataSource) {

                myVortexFuture.execute(TestActionsServiceProtocol.class).getLaunchRequirements(_dataSource,
                                                                                                myAuthorizationList);
            }
            showWatchBox();

        } catch (Exception myException) {

            Display.error("ResourceParameterWizard", 10, myException);
        }
    }
    
    private void launch(WizardInterface returnDialog) throws CentrifugeException {

        if (null != _template) {

            // Found no panels to process, so remove this segment from wizard list and go to next.
            (new DataViewFromTemplateSave(returnDialog, _template, _parameterData, _requiredAuthorizations)).show();

        } else {

            if ((null != _dataSource) && (_dataSource.getResourceType().equals(AclResourceType.DATAVIEW))) {

                (new DataViewFromScratchSave(_dataSource, returnDialog, _title, _help,
                        _parameterData, _requiredAuthorizations)).show();

            } else if ((null != _dataSource) && (_dataSource.getResourceType().equals(AclResourceType.DATA_TABLE))) {

                (new InstalledTableSave(_dataSource, returnDialog, _title, _help,
                                        _handler, _parameterData, _requiredAuthorizations)).show();
            }
        }
    }
}
