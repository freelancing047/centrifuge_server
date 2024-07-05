package csi.client.gwt.csiwizard.wizards;

import com.google.gwt.event.dom.client.ClickEvent;
import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.Wizard;
import csi.client.gwt.csiwizard.panels.AbstractWizardPanel;
import csi.client.gwt.csiwizard.panels.CapcoPanel;
import csi.client.gwt.csiwizard.panels.SecurityTagsPanel;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.Response;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.interfaces.MapByDataType;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.service.api.ModelActionsServiceProtocol;
import csi.server.common.util.ValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by centrifuge on 10/11/2017.
 */
public class ClassificationWizard extends Wizard {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public interface ClassificationCallback {

        public void onClassification(String uuidIn, CapcoInfo capcoIn,
                                     SecurityTagsInfo securityTagsIn, boolean isAuthorizedIn);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    SecurityTagsPanel _tagPanel;
    CapcoPanel _capcoPanel;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    String _uuid;
    String _name;
    AclResourceType _type;
    List<? extends MapByDataType> _list;
    CapcoInfo _capco = null;
    SecurityTagsInfo _securityTags = null;
    ClassificationCallback _callback;
    List<AbstractWizardPanel> _panelList;
    boolean _doCapco;
    boolean _doTags;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected VortexEventHandler<Response<String, ValuePair<List<? extends MapByDataType>,
            ValuePair<CapcoInfo, SecurityTagsInfo>>>> handleSecurityInfoResponse
            = new AbstractVortexEventHandler<Response<String, ValuePair<List<? extends MapByDataType>,
            ValuePair<CapcoInfo, SecurityTagsInfo>>>>() {
        @Override
        public boolean onError(Throwable myException) {

            hideWatchBox();
            Display.error(myException);
            cancel();
            return true;
        }

        @Override
        public void onSuccess(Response<String, ValuePair<List<? extends MapByDataType>,
                ValuePair<CapcoInfo, SecurityTagsInfo>>> responseIn) {

            try {

                hideWatchBox();

                if (ResponseHandler.isSuccess(responseIn)) {

                    ValuePair<List<? extends MapByDataType>, ValuePair<CapcoInfo, SecurityTagsInfo>> myInfo
                            = responseIn.getResult();
                    ValuePair<CapcoInfo, SecurityTagsInfo> mySecurity = myInfo.getValue2();

                    _list = myInfo.getValue1();
                    if (_doCapco) {

                        _capco = mySecurity.getValue1();
                        if (null == _capco) {

                            _capco = new CapcoInfo();
                        }
                    }
                    if (_doTags) {

                        _securityTags = mySecurity.getValue2();
                        if (null == _securityTags) {

                            _securityTags = new SecurityTagsInfo();
                        }
                    }
                    initializePanels();

                } else {

                    cancel();
                }

            } catch (Exception myException) {

                Display.error("Caught exception Initializing panels!", myException);
                cancel();
            }
        }
    };

    protected VortexEventHandler<Response<String, ValuePair<Boolean, ValuePair<CapcoInfo, SecurityTagsInfo>>>>
            handleSecurityUpdateResponse
                    = new AbstractVortexEventHandler<Response<String, ValuePair<Boolean, ValuePair<CapcoInfo, SecurityTagsInfo>>>>() {
        @Override
        public boolean onError(Throwable myException) {

            hideWatchBox();
            Display.error(myException);
            cancel();
            return true;
        }

        @Override
        public void onSuccess(Response<String, ValuePair<Boolean, ValuePair<CapcoInfo, SecurityTagsInfo>>> responseIn) {

            try {

                hideWatchBox();

                if (ResponseHandler.isSuccess(responseIn)) {

                    String myUuid = responseIn.getKey();
                    ValuePair<Boolean, ValuePair<CapcoInfo, SecurityTagsInfo>> myOuterPair = responseIn.getResult();
                    ValuePair<CapcoInfo, SecurityTagsInfo> myInnerPair = myOuterPair.getValue2();
                    Boolean myAuthorizedFlag = myOuterPair.getValue1();
                    CapcoInfo myCapcoInfo = myInnerPair.getValue1();
                    SecurityTagsInfo mySecurityTagsInfo = myInnerPair.getValue2();

                    _callback.onClassification(myUuid, myCapcoInfo, mySecurityTagsInfo, myAuthorizedFlag);
                    cancel();

                } else {

                    cancel();
                }

            } catch (Exception myException) {

                Display.error("Caught exception Initializing panels!", myException);
                cancel();
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ClassificationWizard(String titleIn, String helpIn, String uuidIn, String nameIn,
                                AclResourceType typeIn, ClassificationCallback callbackIn) {

        super(titleIn, helpIn, null, null);

        try {

            _callback = callbackIn;
            _panelList = new ArrayList<AbstractWizardPanel>();
            _uuid = uuidIn;
            _name = nameIn;
            _type = typeIn;
            _doTags = WebMain.getClientStartupInfo().isEnforceSecurityTags()
                    || WebMain.getClientStartupInfo().isProvideTagBanners();
            _doCapco = WebMain.getClientStartupInfo().isEnforceCapcoRestrictions()
                    || WebMain.getClientStartupInfo().isProvideCapcoBanners();
            requestSecurityInformation();

        } catch (Exception myException) {

            Display.error(myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void execute(AbstractWizardPanel activePanelIn, ClickEvent eventIn) {

        establishSecurity();
    }

    @Override
    protected void cancel(AbstractWizardPanel activePanelIn, ClickEvent eventIn) {

    }

    @Override
    protected void displayNewPanel(int indexIn, ClickEvent eventIn) {

        try {

            AbstractWizardPanel myPanel = _panelList.get(indexIn);

            displayPanel(myPanel, myPanel.getInstructions(Dialog.txtNextButton));

        } catch(Exception myException) {

            Display.error("Classification Wizard", myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void initializePanels() throws CentrifugeException {

        if (_doTags) {

            _tagPanel = new SecurityTagsPanel(this, _list, _securityTags);
            _panelList.add(_tagPanel);
            _finalDisplayIndex++;
        }
        if (_doCapco) {

            _capcoPanel = new CapcoPanel(this, _list, _capco);
            _panelList.add(_capcoPanel);
            _finalDisplayIndex++;
        }
        displayNewPanel(0, null);
    }

    private void requestSecurityInformation() {

        try {
            showWatchBox(_constants.resourceTab_retrieveSecurityInfo());

            VortexFuture<Response<String, ValuePair<List<? extends MapByDataType>,
                    ValuePair<CapcoInfo, SecurityTagsInfo>>>> vortexFuture = WebMain.injector.getVortex().createFuture();
            vortexFuture.addEventHandler(handleSecurityInfoResponse);
            vortexFuture.execute(ModelActionsServiceProtocol.class).getSecurityInfo(_uuid, _type, AclControlType.EDIT);
        } catch (Exception myException) {

            hideWatchBox();
            Display.error(myException);
            cancel();
        }
    }

    private void establishSecurity() {

        try {
            showWatchBox(_constants.resourceTab_updateSecurityInfo());

            _capco = (null != _capcoPanel) ? _capcoPanel.getResults() : null;
            _securityTags = (null != _tagPanel) ? _tagPanel.getResults() : null;

            VortexFuture<Response<String, ValuePair<Boolean, ValuePair<CapcoInfo, SecurityTagsInfo>>>>
                    vortexFuture = WebMain.injector.getVortex().createFuture();
            vortexFuture.addEventHandler(handleSecurityUpdateResponse);
            vortexFuture.execute(ModelActionsServiceProtocol.class).classifyResource(_uuid, _type, _capco, _securityTags);
        } catch (Exception myException) {

            hideWatchBox();
            Dialog.showException(myException);
            cancel();
        }
    }
}
