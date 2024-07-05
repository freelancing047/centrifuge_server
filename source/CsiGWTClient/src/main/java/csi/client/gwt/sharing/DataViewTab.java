package csi.client.gwt.sharing;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.dialogs.DataViewFromTemplateDialog;
import csi.client.gwt.csiwizard.AdHocEditLauncher;
import csi.client.gwt.events.*;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.ApplicationToolbar;
import csi.client.gwt.mainapp.ApplicationToolbarLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.DecisionDialog;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.ButtonDef;
import csi.server.common.dto.SharingDisplay;
import csi.server.common.enumerations.AclResourceType;

/**
 * Created by centrifuge on 7/8/2015.
 */
public class DataViewTab extends ResourceTab {

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private static final List<ButtonDef> _buttonList = new ArrayList<ButtonDef>();

    private DecisionDialog dialog;

    public DataViewTab(ResourceSharingView parentIn) {

        super(parentIn, "sharing.DataViewTab", true);
        wireInHandlers();
    }

    private ClickHandler handleEditRequest = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {

            SharingDisplay myItem = getSelection();

            if (null != myItem) {

                ApplicationToolbarLocator.getInstance().enableMenus();
                WebMain.injector.getMainPresenter().beginOpenDataView(myItem);
            }
        }
    };

    private ClickHandler handleCreateRequest = new ClickHandler() {

        public void onClick(ClickEvent eventIn) {

            try {

                parent.saveState();
                if (_buttonList.size() == 0) {
                    _buttonList.add(new ButtonDef(_constants.manageResources_DataViewTab_fromTemplateButton(),
                            ButtonType.PRIMARY));
                    _buttonList.add(new ButtonDef(_constants.manageResources_DataViewTab_fromScratchButton(),
                            ButtonType.PRIMARY));
                }
                dialog = new DecisionDialog(_constants.manageResources_DataViewTab_createDialogTitle(),
                        _constants.manageResources_DataViewTab_createDialogContent(
                                _constants.manageResources_DataViewTab_fromTemplateButton(),
                                _constants.manageResources_DataViewTab_fromScratchButton()),
                        _buttonList, handleChoiceMadeEvent, 100);
                dialog.getCancelButton().setText(Dialog.txtCancelButton);

                dialog.show();

            } catch (Exception myException) {

                Display.error("DataViewTab", 1, myException);
            }
        }
    };

    //
    // Handle choice being made between templates and dataviews
    //
    private ChoiceMadeEventHandler handleChoiceMadeEvent
            = new ChoiceMadeEventHandler() {
        @Override
        public void onChoiceMade(ChoiceMadeEvent eventIn) {

            try {

                int _choice = eventIn.getChoice();

                switch (_choice) {

                    case 0:

                        break;

                    case 1:

                        ApplicationToolbarLocator.getInstance().enableMenus();
                        (new DataViewFromTemplateDialog(false)).show();

                        break;

                    case 2:

                        ApplicationToolbarLocator.getInstance().enableMenus();
                        (new AdHocEditLauncher(AclResourceType.DATAVIEW, handleInstallComplete)).show();
                        break;
                }

            } catch (Exception myException) {

                Display.error("DataViewTab", 2, myException);
            }
        }
    };

    @Override
    protected void wireInHandlers() {

        super.wireInHandlers();

        WebMain.injector.getEventBus().addHandler(DataViewNameChangeEvent.type, new DataViewNameChangeEventHandler() {

            @Override
            public void onDataViewNameChange(DataViewNameChangeEvent eventIn) {

                if (null != eventIn) {

                    renameLocalResourceEntry(eventIn.getUuid(), eventIn.getName(), eventIn.getRemarks());
                }
            }
        });
    }

    protected AclResourceType getResourceType() {

        return AclResourceType.DATAVIEW;
    }

    protected ClickHandler getEditClickHandler() {

        return handleEditRequest;
    }

    protected ClickHandler getExportClickHandler() {

        return handleExportRequest;
    }

    protected ClickHandler getRenameClickHandler() {

        return handleRenameRequest;
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

        return Dialog.txtOpenButton;
    }

    protected String getCreateButtonLabel() {

        return _constants.manageResources_DataViewTab_newDataViewButton();
    }

    protected String getResourceTypeString() {

        return "DataView";
    }

    protected String getResourceTypePluralString() {

        return "DataViews";
    }

    @Override
    public IconType getIconType() {

        return IconType.TABLE;
    }
}
