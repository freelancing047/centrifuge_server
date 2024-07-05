package csi.client.gwt.csiwizard.dialogs;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.csiwizard.panels.SecurityTagsPanel;
import csi.client.gwt.csiwizard.panels.WizardTabPanel;
import csi.client.gwt.csiwizard.panels.CapcoPanel;
import csi.client.gwt.edit_sources.presenters.DataSourceEditorPresenter;
import csi.client.gwt.edit_sources.presenters.EditAdHocDataPresenter;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.interfaces.DataContainer;
import csi.server.common.interfaces.SecurityAccess;
import csi.server.common.model.FieldDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.util.Format;

import java.util.List;

/**
 * Created by centrifuge on 6/21/2017.
 */
public class CapcoDialog extends WizardDialog {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    CapcoPanel<ColumnDef> panel = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final String _txtConnectorSelectionInfo = _constants.dataviewFromScratchWizard_InfoString();

    DataSourceEditorPresenter _presenter = null;
    private DataContainer _container = null;


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

    public CapcoDialog(DataSourceEditorPresenter presenterIn,
                       WizardInterface priorDialogIn, String titleIn, String helpIn)
            throws CentrifugeException {

        super(priorDialogIn, new CapcoPanel<FieldDef>(null, presenterIn.getResource().getFieldList()),
                titleIn, helpIn, "");
        _presenter = presenterIn;
        _container = _presenter.getResource();
        panel = (CapcoPanel)getCurrentPanel();
        panel.setParentDialog(this);
        addControlPanel(panel, panel.getInstructions(Dialog.txtNextButton));
        if (!(_presenter instanceof EditAdHocDataPresenter)) {

            super.setAsFinal();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void createPanel() {

    }

    @Override
    protected void execute() {

        try {

            if (null != _container) {

                SecurityAccess mySecurityAccess = _container.getSecurityAccess();

                if (null != mySecurityAccess) {

                    mySecurityAccess.setCapcoInfo(panel.getResults());
                }
            }
            _presenter.saveResults(this);

        } catch (Exception myException) {

            Dialog.showException("CapcoDialog", 1, myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
}
