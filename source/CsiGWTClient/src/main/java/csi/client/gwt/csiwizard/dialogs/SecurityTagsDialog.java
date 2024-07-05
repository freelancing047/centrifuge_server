package csi.client.gwt.csiwizard.dialogs;

        import csi.client.gwt.WebMain;
        import csi.client.gwt.csiwizard.WizardDialog;
        import csi.client.gwt.csiwizard.WizardInterface;
        import csi.client.gwt.csiwizard.panels.CapcoPanel;
        import csi.client.gwt.csiwizard.panels.WizardTabPanel;
        import csi.client.gwt.csiwizard.panels.SecurityTagsPanel;
        import csi.client.gwt.edit_sources.presenters.DataSourceEditorPresenter;
        import csi.client.gwt.edit_sources.presenters.EditAdHocDataPresenter;
        import csi.client.gwt.util.Display;
        import csi.client.gwt.widget.boot.Dialog;
        import csi.server.common.dto.user.UserSecurityInfo;
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
public class SecurityTagsDialog extends WizardDialog {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    SecurityTagsPanel<ColumnDef> panel = null;


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

    public SecurityTagsDialog(DataSourceEditorPresenter presenterIn,
                              WizardInterface priorDialogIn, String titleIn, String helpIn)
            throws CentrifugeException {

        super(priorDialogIn, new SecurityTagsPanel<FieldDef>(null, presenterIn.getResource().getFieldList()),
                                                                titleIn, helpIn, "");
        _presenter = presenterIn;
        _container = _presenter.getResource();
        panel = (SecurityTagsPanel)getCurrentPanel();
        panel.setParentDialog(this);
        addControlPanel(panel, panel.getInstructions(Dialog.txtNextButton));
        if ((!(_presenter instanceof EditAdHocDataPresenter))
                && (!((WebMain.getClientStartupInfo().isEnforceCapcoRestrictions()
                        || WebMain.getClientStartupInfo().isProvideCapcoBanners())))) {

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

            UserSecurityInfo myUserInfo = WebMain.injector.getMainPresenter().getUserInfo();
            boolean mySetSecurityOk = (null != myUserInfo) ? myUserInfo.getCanSetSecurity() : false;

            if (null != _container) {

                SecurityAccess mySecurityAccess = _container.getSecurityAccess();

                if (null != mySecurityAccess) {

                    mySecurityAccess.setSecurityTagsInfo(panel.getResults());
                }
            }
            if (mySetSecurityOk && (WebMain.getClientStartupInfo().isEnforceCapcoRestrictions()
                    || WebMain.getClientStartupInfo().isProvideCapcoBanners())) {

                (new CapcoDialog(_presenter, this, getDialogTitle(), getDialogHelp())).show();

            } else {

                _presenter.saveResults(this);
            }

        } catch (Exception myException) {

            Display.error("SecurityTagsDialog", 1, myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
}
