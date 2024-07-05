package csi.client.gwt.sharing;

import java.util.List;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ResizeComposite;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.widgets.PairedStringList;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.ValidatingDialog;
import csi.client.gwt.widget.input_boxes.FilteredIntegerInput;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;
import csi.server.common.dto.Response;
import csi.server.common.dto.system.ReaperControl;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.service.api.UserAdministrationServiceProtocol;

/**
 * Created by centrifuge on 8/25/2016.
 */
public class ConfigureReaperDialog extends ResizeComposite {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface SpecificUiBinder extends UiBinder<ValidatingDialog, ConfigureReaperDialog> {
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @UiField
    ValidatingDialog dialog;
    @UiField
    CsiStringListBox resourceType;
    @UiField
    TextBox remarks;
    @UiField
    RadioButton createdRadioButton;
    @UiField
    RadioButton accessedRadioButton;
    @UiField
    FilteredIntegerInput daysBeforeReaping;
    @UiField
    PairedStringList ownerSelection;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static String _txtTitle = _constants.administrationDialogs_ReaperPopupTitle();
    private static String _txtHelpPath = _constants.administrationDialogs_ReaperPopupHelpTarget();

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    private ReaperControl _reaperControl = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private VortexEventHandler<Response<String, List<String>>> handleUserRefreshResponse
            = new AbstractVortexEventHandler<Response<String, List<String>>>() {

        @Override
        public boolean onError(Throwable myException) {

            Display.error("ConfigureReaperDialog", 1, myException);
            return true;
        }

        @Override
        public void onSuccess(Response<String, List<String>> responseIn) {

            if (ResponseHandler.isSuccess(responseIn)) {

                List<String> myUserList = responseIn.getResult();

                if (null != _reaperControl) {

                    ownerSelection.initializeData(myUserList, _reaperControl.getOwners());

                } else {

                    ownerSelection.initializeData(myUserList, null);
                }
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ConfigureReaperDialog() {

        dialog = uiBinder.createAndBindUi(this);

        ownerSelection.setEmptyValue("All Users");
        refreshUsers();
        //
        // Set up the dialog title bar with help button
        //
        dialog.defineHeader(_txtTitle, _txtHelpPath, true);
        dialog.hideOnCancel();

        resourceType.addItem(AclResourceType.DATAVIEW.getLabel());
        resourceType.addItem(AclResourceType.TEMPLATE.getLabel());
        resourceType.setSelectedValue(AclResourceType.DATAVIEW.getLabel());

        createdRadioButton.setValue(false);
        accessedRadioButton.setValue(true);

        daysBeforeReaping.setRequired(true);
        dialog.addObject(daysBeforeReaping, false);
    }

    public ConfigureReaperDialog(ReaperControl reaperControlIn) {

        dialog = uiBinder.createAndBindUi(this);

        _reaperControl = reaperControlIn;
        ownerSelection.setEmptyValue("All Users");
        refreshUsers();
        //
        // Set up the dialog title bar with help button
        //
        dialog.defineHeader(_txtTitle, _txtHelpPath, true);
        dialog.hideOnCancel();

        resourceType.addItem(AclResourceType.DATAVIEW.getLabel());
        resourceType.addItem(AclResourceType.TEMPLATE.getLabel());
        resourceType.setSelectedValue(_reaperControl.getResourceType().getLabel());

        remarks.setText(_reaperControl.getRemarks());

        createdRadioButton.setValue(!_reaperControl.isAccessAge());
        accessedRadioButton.setValue(_reaperControl.isAccessAge());

        daysBeforeReaping.setValue((null != _reaperControl.getAge()) ? _reaperControl.getAge().toString() : null);
        daysBeforeReaping.setRequired(true);
    }

    public void show() {

        dialog.show(70);
        dialog.beginMonitoring();
    }

    private void refreshUsers() {

        // Request data from the server

        try {
            VortexFuture<Response<String, List<String>>> vortexFuture = WebMain.injector.getVortex().createFuture();

            vortexFuture.execute(UserAdministrationServiceProtocol.class).getActiveUserNames();
            vortexFuture.addEventHandler(handleUserRefreshResponse);

        } catch (Exception myException) {

            Display.error("ConfigureReaperDialog", 2, myException);
        }
    }
}
