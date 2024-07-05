package csi.client.gwt.csi_resource;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.PanelDialog;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel.SelectorMode;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.KnowsParent;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.SelectionListData.SelectorBasics;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.service.api.ModelActionsServiceProtocol;
import csi.server.common.util.ValuePair;


/**
 * Created by centrifuge on 5/19/2016.
 */
public class RenameResourceDialog<T extends SelectorBasics> extends PanelDialog implements KnowsParent {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    CanBeShownParent parentDialog = null;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ResourceSelectorPanel<T> _panel;
    private String _uuid;
    private String _name;
    private String _remarks;
    private String _owner = null;
    private AclResourceType _type;
    private boolean _ready = false;
    private ClickHandler _updateCallBack = null;
    private ClickHandler _cancelCallBack = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private VortexEventHandler<Response<String, ValuePair<String, String>>> handleFinalizeRename
            = new AbstractVortexEventHandler<Response<String, ValuePair<String, String>>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {

            try {

                Display.error("RenameResourceDialog", 1, exceptionIn);

            } catch (Exception myException) {

                Display.error("RenameResourceDialog", 2, myException);
            }
            return false;
        }
        @Override
        public void onSuccess(Response<String, ValuePair<String, String>> responseIn) {

            try {
                hideWatchBox();
                if (responseIn.getKey() == _uuid) {

                    if (ResponseHandler.isSuccess(responseIn, getDialogTitle())) {

                        ValuePair<String, String> myResponse = responseIn.getResult();

                        if (null != myResponse) {

                            String myNewName = myResponse.getValue1();
                            String myNewRemarks = myResponse.getValue2();

                            Dialog.showInfo(_constants.renameResourceResponse_Title(_type.getLabel()),
                                    _constants.renameResourceResponse_Success(_type.getLabel(), myNewName));

                            if (AclResourceType.DATAVIEW.equals(_type)) {

                                WebMain.injector.getEventBus().fireEvent(
                                        new csi.client.gwt.events.DataViewNameChangeEvent(_uuid, myNewName, myNewRemarks));

                            } else if (AclResourceType.TEMPLATE.equals(_type)) {

                                WebMain.injector.getEventBus().fireEvent(
                                        new csi.client.gwt.events.TemplateNameChangeEvent(_uuid, myNewName, myNewRemarks));
                            }
                        }
                    }
                }

            } catch (Exception myException) {

                Display.error("RenameResourceDialog", 3, myException);
            }
        }
    };

    //
    // Handle selection made from resource list
    //
    protected ClickHandler handleApplyButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                _name = _panel.getName();
                _remarks = _panel.getRemarks();

                if (null != _updateCallBack) {

                    _updateCallBack.onClick(null);
                    parentDialog.show();

                } else {

                    renameResource();
                }

            } catch (Exception myException) {

                Display.error("RenameResourceDialog", 4, myException);
            }
        }
    };

    //
    // Handle cancel
    //
    protected ClickHandler handleCancelButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            if (null != _cancelCallBack) {

                _cancelCallBack.onClick(null);
                parentDialog.show();
            }
        }
    };

    private VortexEventHandler<List<T>> handleRejectionListResponse
            = new AbstractVortexEventHandler<List<T>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {

            try {

                hideWatchBox();
                Display.error(exceptionIn);

            } catch (Exception myException) {

                Display.error("RenameResourceDialog", 5, myException);
            }
            return true;
        }

        @Override
        public void onSuccess(List<T> responseIn) {

            try {

                _panel.setSingleList(responseIn);
                hideWatchBox();

            } catch (Exception myException) {

                Display.error("RenameResourceDialog", 6, myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public RenameResourceDialog(String nameIn, String titleIn, String typeSingularIn,
                                String typePluralIn, ClickHandler updateCallBackIn,
                                ClickHandler cancelCallBackIn, List<T> listIn) {

        super(new ResourceSelectorPanel<ResourceBasics>(null, SelectorMode.WRITE, 480, 335),
                titleIn, "HTTP://HELP", "Directions");

        _updateCallBack = updateCallBackIn;
        _cancelCallBack = cancelCallBackIn;
        initialize(nameIn, _constants.renamingInstructions(typePluralIn, typeSingularIn,
                                                        Dialog.txtApplyButton), listIn, typePluralIn);
    }

    public RenameResourceDialog(String uuidIn, AclResourceType typeIn, String nameIn, String titleIn) {

        super(new ResourceSelectorPanel<ResourceBasics>(null, SelectorMode.WRITE, 480, 335),
                titleIn, "HTTP://HELP", "Directions");

        try {

            initialize(uuidIn, typeIn, nameIn, _constants.renamingInstructions(typeIn.getPlural(), typeIn.getLabel(),
                    Dialog.txtApplyButton));

        } catch (Exception myException) {

            Display.error("RenameResourceDialog", 7, myException);
        }
    }

    public RenameResourceDialog(String uuidIn, AclResourceType typeIn, String nameIn, String titleIn, String ownerIn) {

        this(uuidIn, typeIn, nameIn, titleIn);
        _owner = ((null != ownerIn) && (0 < ownerIn.trim().length())) ? ownerIn.trim() : null;
    }

    @Override
    public void show() {

        try {

            super.show();

            if (!_ready) {

                showWatchBox(_constants.retrievingConflicts());
            }
            _panel.selectInput();

        } catch (Exception myException) {

            Display.error("RenameResourceDialog", 8, myException);
        }
    }

    public void show(CanBeShownParent parentDialogIn) {

        parentDialog = parentDialogIn;
        show();
        if (null != parentDialog) {

            parentDialog.hide();
        }
        _panel.selectInput();
    }

    public String getName() {

        return _name;
    }

    public String getRemarks() {

        return _remarks;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void initialize(String uuidIn, AclResourceType typeIn, String nameIn, String instructionsIn) {

        initialize(uuidIn, typeIn, nameIn, instructionsIn, null);
        _panel.initializeDisplay(_type, _constants.conflictListLabel(_type.getPlural()));
        getRejectionList();
    }

    private void initialize(String nameIn, String instructionsIn, List<T> listIn, String typePluralIn) {

        initialize(null, null, nameIn, instructionsIn, listIn);
        _panel.initializeDisplay(_type, _constants.conflictListLabel(typePluralIn));
        _ready = true;
    }

    private void initialize(String uuidIn, AclResourceType typeIn, String nameIn,
                            String instructionsIn, List<T> listIn) {

        if (null != instructionsIn) {

            addInstructions(instructionsIn);
        }
        _uuid = uuidIn;
        _name = nameIn;
        _type = typeIn;
        _panel = (ResourceSelectorPanel<T>) getCurrentPanel();
        _panel.rejectAllConflicts();
        _panel.setSingleList(listIn);
        _panel.suppressFilter();
        _panel.addValidityReportEventHandler(handleValidityReportEvent);
        _panel.setParentDialog(this);
        _panel.setCurrentName(_name);
        dialog.getActionButton().setText(Dialog.txtApplyButton);
        setRequestHandler(handleApplyButtonClick);
        setCancelHandler(handleCancelButtonClick);
    }

    private void getRejectionList() {

        try {

            VortexFuture<List<T>> vortexFuture = WebMain.injector.getVortex().createFuture();
            vortexFuture.addEventHandler(handleRejectionListResponse);
            if (null != _owner) {

                vortexFuture.execute(ModelActionsServiceProtocol.class).listUserResourceBasics(_type, _owner);

            } else {

                vortexFuture.execute(ModelActionsServiceProtocol.class).listUserResourceBasics(_type);
            }

        } catch (Exception myException) {

            hideWatchBox();
            Display.error("RenameResourceDialog", 9, myException);
            destroy();
        }
    }

    private void renameResource() {

        try {

            showWatchBox(_constants.renameResource_WatchMessage(_type.getLabel(), _name));
            VortexFuture<Response<String, ValuePair<String, String>>> vortexFuture
                                                = WebMain.injector.getVortex().createFuture();
            vortexFuture.addEventHandler(handleFinalizeRename);
            vortexFuture.execute(ModelActionsServiceProtocol.class).renameResource(_uuid, _type, _name, _remarks);

        } catch (Exception myException) {

            hideWatchBox();
            Display.error("RenameResourceDialog", 10, myException);
            destroy();
        }
    }
}
