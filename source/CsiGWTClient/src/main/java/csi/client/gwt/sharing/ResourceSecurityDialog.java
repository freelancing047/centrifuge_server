package csi.client.gwt.sharing;

import java.util.ArrayList;

import com.google.gwt.event.shared.HandlerManager;

import csi.client.gwt.csiwizard.PanelDialog;
import csi.client.gwt.csiwizard.panels.WizardTabPanel;
import csi.client.gwt.csiwizard.panels.CapcoPanel;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.model.column.ColumnDef;

/**
 * Created by centrifuge on 7/28/2015.
 */
public class ResourceSecurityDialog extends PanelDialog {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private HandlerManager _handlerManager;

    private CapcoPanel<ColumnDef> _capcoPanel = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ResourceSecurityDialog() {

        super(new WizardTabPanel(null), _constants.resourceSecurityDialog_txtTitle(), _constants.resourceSecurityDialog_txtHelpTarget(), "");
        getCurrentPanel().setParentDialog(this);

        _handlerManager = new HandlerManager(this);

        dialog.getActionButton().setEnabled(true);
        dialog.getActionButton().setText(Dialog.txtApplyButton);
        dialog.hideOnCancel();
    }

    public void show() {

        try {

            _capcoPanel = new CapcoPanel<ColumnDef>(this, new ArrayList<ColumnDef>());
            addControlPanel(_capcoPanel, "");

            dialog.show();

        } catch (Exception myException) {

            abortDialog(myException);
        }
    }
}