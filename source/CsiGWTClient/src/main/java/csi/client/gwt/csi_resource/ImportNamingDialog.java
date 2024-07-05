package csi.client.gwt.csi_resource;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import csi.client.gwt.csiwizard.PanelDialog;
import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel.SelectorMode;
import csi.client.gwt.events.ResourceSelectionEvent;
import csi.client.gwt.events.ResourceSelectionEventHandler;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.enumerations.AclResourceType;


/**
 * Created by centrifuge on 5/19/2016.
 */
public class ImportNamingDialog extends ResourceSelectorDialog {


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

    private CanBeShownParent _priorDialog = null;
    private String _remarks;
    private String _name;
    private boolean _force = false;
    private AclResourceType _type;
    private String _helpLink = null;
    private String _infoString = "";


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ImportNamingDialog(CanBeShownParent priorDialogIn, AclResourceType typeIn,
                              String nameIn, List<List<ResourceBasics>> tripleListIn) {

        super(SelectorMode.WRITE);

        try {

            _type = typeIn;
            _priorDialog = priorDialogIn;
            selectorWidget.setParentDialog(this);
            selectorWidget.setTripleList(tripleListIn, true);
            selectorWidget.setDefaultName(nameIn);
            selectorWidget.setPreSelect(nameIn);
            initializeDisplay(_type, null, _infoString, Dialog.txtRenameButton);

        } catch (Exception myException) {

            Dialog.showException("ImportNamingDialog", myException);
        }
    }

    @Override
    public void show() {

        try {

            _priorDialog.hide();
            super.show(_type, _helpLink, _infoString, Dialog.txtRenameButton);

        } catch (Exception myException) {

            Dialog.showException("ImportNamingDialog", myException);
        }
    }

    public String getName() {

        return _name;
    }

    public String getRemarks() {

        return _remarks;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void handleSelectionMade(ResourceBasics selectionIn, boolean forceOverWriteIn) {

        _name = selectionIn.getName();
        _remarks = selectionIn.getRemarks();
        _force = forceOverWriteIn;
        if (null != _priorDialog) {

            _priorDialog.showWithResults(null);
        }
        destroy();
    }

    @Override
    protected void onCancel() {

        if (null != _priorDialog) {

            _priorDialog.show();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

}
