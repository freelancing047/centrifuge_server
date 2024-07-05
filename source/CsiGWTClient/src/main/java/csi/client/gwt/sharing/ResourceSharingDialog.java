package csi.client.gwt.sharing;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.PanelDialog;
import csi.client.gwt.csiwizard.panels.ResourceSharingPanel;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.SharingRequest;
import csi.server.common.dto.SharingDisplay;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.CsiUUID;
import csi.server.common.service.api.UserAdministrationServiceProtocol;


public class ResourceSharingDialog extends PanelDialog {

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ResourceSharingPanel topPanel;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private String _uuid = null;
    private List<String> _uuidList = null;
    private VortexEventHandler<Response<String, List<SharingDisplay>>> _sharingResponseHandler;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected VortexEventHandler<Response<String, List<SharingDisplay>>> handleSharingResponse
            = new AbstractVortexEventHandler<Response<String, List<SharingDisplay>>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {

            hideWatchBox();

            // Display error message.
            Display.error("ResourceSharingDialog", 1, exceptionIn);
            return false;
        }

        @Override
        public void onSuccess(Response<String, List<SharingDisplay>> responseIn) {

            hideWatchBox();

            if (ResponseHandler.isSuccess(responseIn)) {

                List<SharingDisplay> myResponse = responseIn.getResult();

                if (null != myResponse) {

                    Dialog.showInfo(_constants.sharing_Success());

                } else {

                    ResponseHandler.displayError(responseIn);
                }
            }
        }
    };

    private ClickHandler executeRequestHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            VortexFuture<Response<String, List<SharingDisplay>>> myVortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                SharingRequest myRequest = ((ResourceSharingPanel)getCurrentPanel()).getSharingRequest();

                if (null != myRequest) {

                    myVortexFuture.addEventHandler(_sharingResponseHandler);
                    myVortexFuture.execute(UserAdministrationServiceProtocol.class).share(CsiUUID.randomUUID(),
                                                                                            _uuidList, myRequest);
                    showWatchBox(_constants.sharing_WatchBoxInfo());
                }

            } catch (Exception myException) {

                Display.error("ResourceSharingDialog", 2, myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ResourceSharingDialog(AclResourceType resourceTypeIn, String uuidIn) {

        super(new ResourceSharingPanel(resourceTypeIn),
                _constants.resourceSharingDialog_SingleTitle((null != resourceTypeIn) ? resourceTypeIn.getLabel() : "???"),
                _constants.resourceSharingDialog_txtHelpTarget(), _constants.resourceSharingDialog_Instructions(
                                                                    _constants.leftUserHeaderDefault(),
                                                                    _constants.rightUserHeaderDefault(),
                                                                    _constants.leftGroupHeaderDefault(),
                                                                    _constants.rightGroupHeaderDefault()));

        _uuidList = new ArrayList<>();
        _uuidList.add(uuidIn);
        _sharingResponseHandler = handleSharingResponse;
        initLocal();
        topPanel.initializeDisplay(this, _uuidList);
    }

    public ResourceSharingDialog(AclResourceType resourceTypeIn, List<String> uuidListIn,
                                 VortexEventHandler<Response<String, List<SharingDisplay>>> sharingResponseHandlerIn) {

        super(new ResourceSharingPanel(resourceTypeIn),
                _constants.resourceSharingDialog_MultipleTitle((null != resourceTypeIn) ? resourceTypeIn.getLabel() : "???"),
                _constants.resourceSharingDialog_txtHelpTarget(), _constants.resourceSharingDialog_Instructions(
                                                                    _constants.leftUserHeaderDefault(),
                                                                    _constants.rightUserHeaderDefault(),
                                                                    _constants.leftGroupHeaderDefault(),
                                                                    _constants.rightGroupHeaderDefault()));

        _uuidList = uuidListIn;
        _sharingResponseHandler = sharingResponseHandlerIn;
        initLocal();
        topPanel.initializeDisplay(this, uuidListIn);
    }

    public void show() {

        dialog.show();
        getActionButton().setText(_constants.resourceTab_share());
        getActionButton().setEnabled(true);
    }

    @Override
    public void checkValidity() {

        dialog.suspendMonitoring();
        getActionButton().setEnabled(true);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void initLocal() {

        setRequestHandler(executeRequestHandler);
        topPanel = (ResourceSharingPanel) getCurrentPanel();
        topPanel.setParentDialog(this);
//        topPanel.setPixelSize();
        dialog.hideOnCancel();
    }
}
