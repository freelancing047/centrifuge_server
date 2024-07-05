package csi.client.gwt.sharing;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.csi_resource.RefreshInstalledTable;
import csi.client.gwt.csiwizard.AdHocEditLauncher;
import csi.client.gwt.csiwizard.InstalledTableEditLauncher;
import csi.client.gwt.csiwizard.wizards.ParameterWizard;
import csi.client.gwt.events.UserInputEvent;
import csi.client.gwt.events.UserInputEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.AuthDO;
import csi.server.common.dto.LaunchParam;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.SharingDisplay;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.enumerations.InstallationType;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.service.api.DataViewActionServiceProtocol;
import csi.server.common.service.api.ModelActionsServiceProtocol;
import csi.server.common.service.api.TestActionsServiceProtocol;
import csi.server.common.service.api.UploadServiceProtocol;
import csi.server.common.util.Flags;
import csi.server.common.util.Format;
import csi.server.common.util.StringUtil;
import csi.server.common.util.ValuePair;

import java.util.List;

/**
 * Created by centrifuge on 7/8/2015.
 */
public class DataTableTab extends ResourceTab {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    protected List<QueryParameterDef> _requiredParameters;
    protected List<DataSourceDef> _requiredAuthorizations;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected ClickHandler getEditAuthorizationCheckHandler() {

        return new ClickHandler() {
            public void onClick(ClickEvent eventIn) {

                try {

                    VortexFuture<ValuePair<String, Boolean>> vortexFuture = WebMain.injector.getVortex().createFuture();
                    vortexFuture.addEventHandler(handleEditAuthorizationResponse);
                    vortexFuture.execute(ModelActionsServiceProtocol.class).isAuthorized(getSelection().getUuid(),
                                                                                            AclControlType.EDIT, false);

                } catch (Exception myException) {

                    AclResourceType myType = getResourceType();
                    String myTypeLabel = myType.getLabel();
                    String myTitle = AclResourceType.DATAVIEW.equals(myType)
                            ? _constants.openResourceResponse_Title(myTypeLabel)
                            : _constants.editResourceResponse_Title(myTypeLabel);

                    Display.error(myTitle, myException);
                }
            }
        };
    }

    private VortexEventHandler<Response<String, ValuePair<Boolean, InstalledTable>>> handleUpdateResponse
            = new VortexEventHandler<Response<String, ValuePair<Boolean, InstalledTable>>>() {
        @Override
        public void onSuccess(Response<String, ValuePair<Boolean, InstalledTable>> resultIn) {

            try {

                if (ResponseHandler.isSuccess(resultIn)) {

                    ValuePair<Boolean, InstalledTable> myResult = resultIn.getResult();

                    if (null != myResult) {

                        InstalledTable myTable = myResult.getValue2();

                        if (myResult.getValue1()) {

                            Display.success(_constants.installedTableUpdate_SuccessMessage(Format.value(myTable.getTablePath())));

                        } else {

                            switch (myTable.getFileType()) {

                                case ADHOC:

                                    Display.error(_constants.unexpectedError());
                                    break;

                                case DATAVIEW:

                                    Display.error(_constants.installedTableUpdate_IncorrectMethodTitle(),
                                                    _constants.installedTableUpdate_UseCapture(Format.value(myTable.getDriver()),
                                                            Format.value(_constants.menuKeyConstants_spawn()),
                                                            Format.value(_constants.extractTableDialog_UpdateTable()),
                                                            Format.value(Dialog.txtNextButton),
                                                            Format.value(myTable.getTablePath()),
                                                            Format.value(Dialog.txtFinishButton)), true, true, true);
                                    break;

                                default:

                                    Display.error(_constants.notSupportedTitle(),
                                                    _constants.installedTableUpdate_NotSupportedMessage());
                                    break;
                            }
                        }

                    } else {

                        Display.error(_constants.unexpectedNullFromServer());
                    }
                }

            } catch (Exception myException) {

                Display.error("DataTableTab", 1, myException);
            }
        }

        @Override
        public boolean onError(Throwable myException) {

            Display.error("Data Update Failed!", myException);

            return false;
        }

        @Override
        public void onUpdate(int taskProgess, String taskMessage) {

        }

        @Override
        public void onCancel() {

        }
    };

    private ClickHandler handleEditRequest = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {

            try {

                SharingDisplay mySelection = getSelection();
                String myUuid = (null != mySelection) ? mySelection.getUuid() : null;

                if (null != myUuid) {

                    (new RefreshInstalledTable(myUuid, handleUpdateResponse)).begin();
                }

            } catch (Exception myException) {

                Display.error("DataTableTab", 3, myException);
            }
        }
    };

    private ClickHandler handleCreateRequest = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {

            try {

                parent.saveState();
                (new AdHocEditLauncher(AclResourceType.DATA_TABLE, handleInstallComplete)).show();

            } catch (Exception myException) {

                Display.error("DataTableTab", 4, myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DataTableTab(ResourceSharingView parentIn) {

        super(parentIn, "sharing.DataTableTab", true);
        wireInHandlers();
    }

    @Override
    public IconType getIconType() {

        return IconType.TABLE;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected AclResourceType getResourceType() {

        return AclResourceType.DATA_TABLE;
    }

    protected ClickHandler getEditClickHandler() {

        return handleEditRequest;
    }

    protected ClickHandler getExportClickHandler() {

        return null;
//        return handleExportRequest;
    }

    protected ClickHandler getRenameClickHandler() {

        return null;
//        return handleRenameRequest;
    }

    protected ClickHandler getCreateClickHandler() {

        return handleCreateRequest;
    }

    protected ClickHandler getDeleteClickHandler() {

        return buildDeleteDialog();
    }

    protected ClickHandler getClassificationHandler() {

        return handleEditClassificationRequest;
    }

    protected ClickHandler getLaunchClickHandler() {

        return null;
    }

    protected String getEditButtonLabel() {

        return Dialog.txtReloadButton;
    }

    protected String getCreateButtonLabel() {

        return _constants.manageResources_DataViewTab_newDatTableButton();
    }

    protected String getResourceTypeString() {

        return "Installed Table";
    }

    protected String getResourceTypePluralString() {

        return "Installed Tables";
    }
}
