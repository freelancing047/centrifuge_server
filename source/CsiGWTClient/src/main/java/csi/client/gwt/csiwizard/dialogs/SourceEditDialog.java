package csi.client.gwt.csiwizard.dialogs;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.AdHocInterface;
import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.edit_sources.presenters.DataSourceEditorPresenter;
import csi.client.gwt.mainapp.MainView;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.TitleBar;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.interfaces.DataContainer;

/**
 * Created by centrifuge on 12/6/2017.
 */
public class SourceEditDialog extends WizardDialog {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private AdHocInterface priorDialog;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final String _txtConnectorSelectionInfo = _constants.dataviewFromScratchWizard_InfoString();

    private String _titleBar = null;
    private boolean _refresh = false;
    private DataSourceEditorPresenter _presenter;

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

    public SourceEditDialog(DataSourceEditorPresenter presenterIn, AdHocInterface priorDialogIn,
                            String titleIn, String helpIn)
            throws CentrifugeException {

        super(priorDialogIn, null, titleIn, helpIn, "");

        _presenter = presenterIn;
        _titleBar = TitleBar.getInstance().getText();
        priorDialog = priorDialogIn;
    }

    @Override
    public void show() {

        WebMain.injector.getEventBus().fireEvent( new csi.client.gwt.events.EnterSourceEditModeEvent(_presenter, this));
    }

    public void abort(Exception exceptionIn) {
        
        abort();
        Display.error("SourceEditDialog", 1, exceptionIn);
    }
    
    public void abort() {

        clickCancel();
        resetDisplay();
    }

    public void respond(DataSourceEditorPresenter.ExitMode exitModeIn) {

        try {

            resetDisplay();

            switch (exitModeIn) {

                case NEXT:

                    clickNext();
                    break;

                case PREVIOUS:

                    close();
                    clickPrior();
                    break;

                case CANCEL:

                    close();
                    clickCancel();
                    break;
            }

        } catch (Exception myException) {

            Display.error("SourceEditDialog", 2, myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void createPanel() { }

    @Override
    protected void execute() {

        try {

            DataContainer myContainer = _presenter.getResource();
            UserSecurityInfo myUserInfo = WebMain.injector.getMainPresenter().getUserInfo();
            boolean mySetSecurityOk = (null != myUserInfo) ? myUserInfo.getCanSetSecurity() : false;

            if (mySetSecurityOk && (WebMain.getClientStartupInfo().isEnforceSecurityTags()
                    || WebMain.getClientStartupInfo().isProvideTagBanners())) {

                (new SecurityTagsDialog(_presenter, this, getDialogTitle(), getDialogHelp())).show();

            } else if (mySetSecurityOk && (WebMain.getClientStartupInfo().isEnforceCapcoRestrictions()
                        || WebMain.getClientStartupInfo().isProvideCapcoBanners())) {

                (new CapcoDialog(_presenter, this, getDialogTitle(), getDialogHelp())).show();

            } else {

                _presenter.saveResults(this);
            }

        } catch (Exception myException) {

            Dialog.showException("SourceEditDialog", 3, myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void resetDisplay() {

        WebMain.injector.getEventBus().fireEvent(
                new csi.client.gwt.events.ExitSourceEditModeEvent(_titleBar, _refresh));
        if (null != priorDialog) {

            priorDialog.restoreCaller();
        }
    }

    public SourceEditDialog close() {

        if (null != _presenter) {

            _presenter = _presenter.close();
        }
        return null;
    }
}
