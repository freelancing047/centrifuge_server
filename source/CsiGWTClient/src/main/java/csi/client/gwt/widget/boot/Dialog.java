/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.widget.boot;

import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.events.UserInputEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.util.AuthorizationObject;
import csi.client.gwt.util.credentials.CredentialBuilder;
import csi.server.common.dto.AuthDO;
import csi.server.common.model.DataSourceDef;
import csi.server.common.util.Format;

/**
 * Generic dialog that provides default action buttons and event handlers.
 * @author Centrifuge Systems, Inc.
 *
 */
public class Dialog extends DialogPrototype {

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public static final String txtExceptionTitle = i18n.dialog_ExceptionTitle();
    public static final String txtErrorTitle = i18n.dialog_ErrorTitle();
    public static final String txtUnknownError = i18n.dialog_UnknownError();
    public static final String txtWarningTitle = i18n.dialog_WarningTitle();
    public static final String txtProblemTitle = i18n.dialog_ProblemTitle();
    public static final String txtDecisionTitle = i18n.dialog_DecisionTitle();
    public static final String txtInfoTitle = i18n.dialog_InfoTitle();
    public static final String txtWatchBoxTitle = i18n.dialog_WatchBoxTitle();
    public static final String txtLogonTitle = i18n.dialog_LogonTitle();
    public static final String txtLogonPrompt = i18n.dialog_LogonPrompt();
    public static final String txtLogonButton = i18n.dialog_LogonButton();
    public static final String txtUsernamePrompt = i18n.dialog_UsernamePrompt();
    public static final String txtPasswordPrompt = i18n.dialog_PasswordPrompt();
    public static final String txtActionNotSupported = i18n.dialog_ActionNotSupported();
    public static final String txtPleaseWait = i18n.pleaseWait();
    public static final String txtRetryButton = i18n.dialog_RetryButton();
    public static final String txtRemoveButton = i18n.remove();
    public static final String txtUndoButton = i18n.dialog_UndoButton();
    public static final String txtYesButton = i18n.dialog_YesButton();
    public static final String txtNoButton = i18n.dialog_NoButton();
    public static final String txtTrueButton = i18n.dialog_TrueButton();
    public static final String txtFalseButton = i18n.dialog_FalseButton();
    public static final String txtSaveButton = i18n.dialog_SaveButton();
    public static final String txtOkayButton = i18n.dialog_OkayButton();
    public static final String txtContinueButton = i18n.dialog_ContinueButton();
    public static final String txtExecuteButton = i18n.dialog_ExecuteButton();
    public static final String txtCloseButton = i18n.close();
    public static final String txtAdvancedButton = i18n.dialog_AdvancedButton();
    public static final String txtDeleteButton = i18n.dialog_DeleteButton();
    public static final String txtExitButton = i18n.dialog_ExitButton();
    public static final String txtUpdateButton = i18n.dialog_UpdateButton();
    public static final String txtAddButton = i18n.dialog_AddButton();
    public static final String txtOverwriteButton = i18n.dialog_OverwriteButton();
    public static final String txtSkipButton = i18n.dialog_SkipButton();
    public static final String txtCancelButton = i18n.dialog_CancelButton();
    public static final String txtSelectButton = i18n.select();
    public static final String txtApplyButton = i18n.dialog_ApplyButton();
    public static final String txtCreateButton = i18n.dialog_CreateButton();
    public static final String txtNewFieldButton = i18n.dialog_NewFieldButton();
    public static final String txtOpenButton = i18n.dialog_OpenButton();
    public static final String txtNextButton = i18n.dialog_NextButton();
    public static final String txtPreviousButton = i18n.dialog_PreviousButton();
    public static final String txtHideTraceButton = i18n.dialog_HideTraceButton();
    public static final String txtShowTraceButton = i18n.dialog_ShowTraceButton();
    public static final String txtExportButton = i18n.dialog_ExportButton();
    public static final String txtImportButton = i18n.dialog_ImportButton();
    public static final String txtFinishButton = i18n.dialog_FinishButton();
    public static final String txtLaunchButton = i18n.dialog_LaunchButton();
    public static final String txtRefreshButton = i18n.dialog_RefreshButton();
    public static final String txtTestButton = i18n.dialog_TestButton();
    public static final String txtFieldsButton = i18n.dialog_FieldsButton();
    public static final String txtParametersButton = i18n.dialog_ParametersButton();
    public static final String txtMapButton = i18n.dialog_MapButton();
    public static final String txtInformationButton = i18n.dialog_InformationButton();
    public static final String txtPropertiesButton = i18n.dialog_PropertiesButton();
    public static final String txtUnmapButton = i18n.dialog_UnmapButton();
    public static final String txtShowFullMessageButton = i18n.dialog_ShowFullMessageButton();
    public static final String txtHideFullMessageButton = i18n.dialog_HideFullMessageButton();
    public static final String txtEditButton = i18n.dialog_EditButton();
    public static final String txtReloadButton = i18n.dialog_ReloadButton();
    public static final String txtInstallButton = i18n.dialog_InstallButton();
    public static final String txtRenameButton = i18n.dialog_RenameButton();
    public static final String txtReplaceButton = i18n.dialog_ReplaceButton();

    public static final String txtErrorColor = "red"; //$NON-NLS-1$
    public static final String txtWarningColor = "darkorange"; //$NON-NLS-1$
    public static final String txtProblemColor = "darkorange"; //$NON-NLS-1$
    public static final String txtDecisionColor = "darkgray"; //$NON-NLS-1$
    public static final String txtInfoColor = "blue"; //$NON-NLS-1$
    public static final String txtPatternColor = "#007f95"; //$NON-NLS-1$
    public static final String txtSuccessColor = "darkgreen"; //$NON-NLS-1$
    public static final String txtCorrectionColor = "green"; //$NON-NLS-1$
    public static final String txtLabelColor = "black"; //$NON-NLS-1$
    public static final String txtBorderColor = "#efefef"; //$NON-NLS-1$
    public static final String txtGridBorderColor = "#9f9f9f"; //$NON-NLS-1$
    public static final String txtPanelColor = "white"; //$NON-NLS-1$
    public static final String txtDisabledColor = "gray"; //$NON-NLS-1$
    public static final String txtDefaultBackground = "white"; //$NON-NLS-1$
    public static final String txtSelectedBackground = "lightblue"; //$NON-NLS-1$

    public static final int intMaxHeight = 1000000;
    public static final int intScollingStringHeight = 85;
    public static final int intLabelHeight = 20;
    public static final int intTextBoxHeight = 30;
    public static final int intButtonHeight = 30;
    public static final int intMiniButtonHeight = 25;
    public static final int intMiniMargin = 5;
    public static final int intMargin = 10;
    public static final int intIconSize = 10;
    public static final int intGridRowHeight = 22;
    public static final int intWizardButtonWidth = 60;
    public static final int intTabHeight = 35;

    public static void showException(String titleIn, int locationIn, Throwable exceptionIn) {

        String myTitle = ((null != titleIn) ? titleIn + " " : "? ? ? ") + Integer.toString(locationIn);

        if (null != exceptionIn) {

            (new ErrorDialog(myTitle, Format.value(exceptionIn))).show();

        } else {

            (new ErrorDialog(myTitle, i18n.dialogUnknownException())).show(); //$NON-NLS-1$
        }
    }

    public static void showException(String titleIn, Throwable exceptionIn) {

        if (null != exceptionIn) {

            (new ErrorDialog(titleIn, Format.value(exceptionIn))).show();

        } else {

            (new ErrorDialog(titleIn,i18n.dialogUnknownException())).show(); //$NON-NLS-1$
        }
    }

    public static void showException(Throwable exceptionIn) {

//        (new ExceptionDialog(exceptionIn)).show();
        if (null != exceptionIn) {

            (new ErrorDialog(Format.value(exceptionIn))).show();

        } else {

            (new ErrorDialog(i18n.dialogUnknownException())).show(); //$NON-NLS-1$
        }
    }

    public static void showException(String titleIn, Throwable exceptionIn, ClickHandler handlerIn) {

        if (null != exceptionIn) {

            (new ErrorDialog(titleIn, Format.value(exceptionIn), handlerIn)).show();

        } else {

            (new ErrorDialog(titleIn,i18n.dialogUnknownException(), handlerIn)).show(); //$NON-NLS-1$
        }
    }

    public static void showException(Throwable exceptionIn, ClickHandler handlerIn) {

//        (new ExceptionDialog(exceptionIn)).show();
        if (null != exceptionIn) {

            (new ErrorDialog(Format.value(exceptionIn), handlerIn)).show();

        } else {

            (new ErrorDialog(i18n.dialogUnknownException(), handlerIn)).show(); //$NON-NLS-1$
        }
    }

    public static void showException(String titleIn, Throwable exceptionIn, CanBeShownParent parentIn) {

//        (new ExceptionDialog(titleIn, exceptionIn)).show();
        if (null != exceptionIn) {

            (new ErrorDialog(titleIn, Format.value(exceptionIn), parentIn)).show();

        } else {

            (new ErrorDialog(titleIn,i18n.dialogUnknownException(), parentIn)).show(); //$NON-NLS-1$
        }
    }

    public static void showException(Throwable exceptionIn, CanBeShownParent parentIn) {

//        (new ExceptionDialog(exceptionIn)).show();
        if (null != exceptionIn) {

            (new ErrorDialog(Format.value(exceptionIn), parentIn)).show();

        } else {

            (new ErrorDialog(i18n.dialogUnknownException(), parentIn)).show(); //$NON-NLS-1$
        }
    }

    public static void showWarning(String titleIn, String errorIn, boolean stripFirstLineIn) {

        (new ErrorDialog(titleIn, errorIn, stripFirstLineIn, true, false)).show();
    }

    public static void showWarning(String titleIn, String errorIn) {

        (new ErrorDialog(titleIn, errorIn, false, true, false)).show();
    }

    public static void showError(String errorIn, boolean stripFirstLineIn) {

        (new ErrorDialog(errorIn, stripFirstLineIn)).show();
    }

    public static void showWarning(String errorIn, boolean stripFirstLineIn) {

        (new ErrorDialog(errorIn, stripFirstLineIn, true)).show();
    }

    public static void showError(String titleIn, String errorIn) {

        (new ErrorDialog(titleIn, errorIn)).show();
    }

    public static void showError(String titleIn, String errorIn, boolean stripFirstLineIn, ClickHandler handlerIn) {

        (new ErrorDialog(titleIn, errorIn, stripFirstLineIn, handlerIn)).show();
    }

    public static void showError(String titleIn, String errorIn, ClickHandler handlerIn) {

        (new ErrorDialog(titleIn, errorIn, handlerIn)).show();
    }

    public static void showError(String errorIn) {

        (new ErrorDialog(errorIn)).show();
    }

    public static void showError(String errorIn, boolean stripFirstLineIn, ClickHandler handlerIn) {

        (new ErrorDialog(errorIn, stripFirstLineIn, handlerIn)).show();
    }

    public static void showError(String errorIn, ClickHandler handlerIn) {

        (new ErrorDialog(errorIn, handlerIn)).show();
    }

    public static void showError(String titleIn, String errorIn, boolean stripFirstLineIn, CanBeShownParent parentIn) {

        (new ErrorDialog(titleIn, errorIn, stripFirstLineIn, parentIn)).show();
    }

    public static void showError(String errorIn, boolean stripFirstLineIn, CanBeShownParent parentIn) {

        (new ErrorDialog(errorIn, stripFirstLineIn, parentIn)).show();
    }

    public static void showError(String titleIn, String errorIn, CanBeShownParent parentIn) {

        (new ErrorDialog(titleIn, errorIn, parentIn)).show();
    }

    public static void showError(String errorIn, CanBeShownParent parentIn) {

        (new ErrorDialog(errorIn, parentIn)).show();
    }

    public static void showSuccess(String titleIn, String messageIn) {

        (new SuccessDialog(titleIn,messageIn)).show();
    }

    public static void showSuccess(String messageIn) {

        (new SuccessDialog(messageIn)).show();
    }

    public static void showSuccess(String titleIn, String messageIn, CanBeShownParent parentIn) {

        (new SuccessDialog(titleIn, messageIn, parentIn)).show();
    }

    public static void showSuccess(String messageIn, CanBeShownParent parentIn) {

        (new SuccessDialog(messageIn, parentIn)).show();
    }

    public static void showInfo(String titleIn, String infoIn) {

        (new InfoDialog(titleIn, infoIn)).show();
    }

    public static void showInfo(String infoIn) {

        (new InfoDialog(infoIn)).show();
    }

    public static void showInfo(String titleIn, String infoIn, CanBeShownParent parentIn) {

        (new InfoDialog(titleIn, infoIn, parentIn)).show();
    }

    public static void showInfo(String titleIn, String infoIn, ClickHandler handlerIn) {

        (new InfoDialog(titleIn, infoIn, handlerIn)).show();
    }

    public static void showInfo(String infoIn, CanBeShownParent parentIn) {

        (new InfoDialog(infoIn, parentIn)).show();
    }

    public static void showProblem(String titleIn, String infoIn) {

        (new ProblemDialog(titleIn, infoIn)).show();
    }

    public static void showProblem(String infoIn) {

        (new ProblemDialog(infoIn)).show();
    }

    public static void showProblem(String titleIn, String infoIn, CanBeShownParent parentIn) {

        (new ProblemDialog(titleIn, infoIn, parentIn)).show();
    }

    public static void showProblem(String infoIn, CanBeShownParent parentIn) {

        (new ProblemDialog(infoIn, parentIn)).show();
    }

    public static void showCredentialDialogs(Map<String, AuthDO> authorizationMapIn,
                                             List<DataSourceDef> requiredListIn,
                                             UserInputEventHandler<Integer> processLogonIn) {

        (new CredentialBuilder(authorizationMapIn, requiredListIn, processLogonIn)).show();
    }

    public static void showCredentialDialogs(Map<String, AuthDO> authorizationMapIn,
                                             List<DataSourceDef> requiredListIn,
                                             UserInputEventHandler<Integer> processLogonIn, CanBeShownParent parentIn) {

        (new CredentialBuilder(authorizationMapIn, requiredListIn, processLogonIn, parentIn)).show();
    }

    public static <T> void showLogon(AuthorizationObject authorizationIn, UserInputEventHandler<T> processLogonIn) {

        (new LogonDialog<T>(authorizationIn, processLogonIn)).show();
    }

    public static <T> void showLogon(AuthorizationObject authorizationIn,
                                     UserInputEventHandler<T> processLogonIn, T keyIn) {

        (new LogonDialog<T>(authorizationIn, processLogonIn, keyIn)).show();
    }

    public static <T> void showLogon(AuthorizationObject authorizationIn,
                                     UserInputEventHandler<T> processLogonIn, CanBeShownParent parentIn) {

        (new LogonDialog<T>(authorizationIn, processLogonIn, parentIn)).show();
    }

    public static <T> void showLogon(AuthorizationObject authorizationIn, UserInputEventHandler<T> processLogonIn,
                                     T keyIn, CanBeShownParent parentIn) {

        (new LogonDialog<T>(authorizationIn, processLogonIn, keyIn, parentIn)).show();
    }

    public static <T> void showContinueDialog(String messageIn, ClickHandler continueHandlerIn) {

        (new ContinueDialog(messageIn, continueHandlerIn)).show();
    }

    public static <T> void showContinueDialog(String messageIn, ClickHandler continueHandlerIn, boolean hideCancelIn) {

        (new ContinueDialog(messageIn, continueHandlerIn, hideCancelIn)).show();
    }

    public static <T> void showContinueDialog(String messageIn, ClickHandler continueHandlerIn,
                                              ClickHandler cancelHandlerIn) {

        (new ContinueDialog(messageIn, continueHandlerIn, cancelHandlerIn)).show();
    }

    public static <T> void showContinueDialog(String titleIn, String messageIn, ClickHandler continueHandlerIn) {

        (new ContinueDialog(titleIn, messageIn, continueHandlerIn)).show();
    }

    public static <T> void showContinueDialog(String titleIn, String messageIn,
                                              ClickHandler continueHandlerIn, boolean hideCancelIn) {

        (new ContinueDialog(titleIn, messageIn, continueHandlerIn, hideCancelIn)).show();
    }

    public static <T> void showContinueDialog(String titleIn, String messageIn,
                                              ClickHandler continueHandlerIn, String actionTextIn, String cancelTextIn) {

        (new ContinueDialog(titleIn, messageIn, continueHandlerIn, actionTextIn, cancelTextIn)).show();
    }

    public static <T> void showContinueDialog(String titleIn, String messageIn,
                                              ClickHandler continueHandlerIn, ClickHandler cancelHandlerIn) {

        (new ContinueDialog(titleIn, messageIn, continueHandlerIn, cancelHandlerIn)).show();
    }

    public static <T> void showContinueDialog(String messageIn, ClickHandler continueHandlerIn, CanBeShownParent parentIn) {

        (new ContinueDialog(messageIn, continueHandlerIn, parentIn)).show();
    }

    public static <T> void showContinueDialog(String messageIn, ClickHandler continueHandlerIn,
                                              ClickHandler cancelHandlerIn, CanBeShownParent parentIn) {

        (new ContinueDialog(messageIn, continueHandlerIn, cancelHandlerIn, parentIn)).show();
    }

    public static <T> void showContinueDialog(String titleIn, String messageIn,
                                              ClickHandler continueHandlerIn, CanBeShownParent parentIn) {

        (new ContinueDialog(titleIn, messageIn, continueHandlerIn, parentIn)).show();
    }

    public static <T> void showContinueDialog(String titleIn, String messageIn, ClickHandler continueHandlerIn,
                                              ClickHandler cancelHandlerIn, CanBeShownParent parentIn) {

        (new ContinueDialog(titleIn, messageIn, continueHandlerIn, cancelHandlerIn, parentIn)).show();
    }

    public static <T> void showYesNoDialog(String messageIn, ClickHandler continueHandlerIn) {

        (new YesNoDialog(messageIn, continueHandlerIn)).show();
    }

    public static <T> void showYesNoDialog(String messageIn, ClickHandler continueHandlerIn, ClickHandler cancelHandlerIn) {

        (new YesNoDialog(messageIn, continueHandlerIn, cancelHandlerIn)).show();
    }

    public static <T> void showYesNoDialog(String titleIn, String messageIn, ClickHandler continueHandlerIn) {

        (new YesNoDialog(titleIn, messageIn, continueHandlerIn)).show();
    }

    public static <T> void showYesNoDialog(String titleIn, String messageIn, ClickHandler continueHandlerIn, ClickHandler cancelHandlerIn) {

        (new YesNoDialog(titleIn, messageIn, continueHandlerIn, cancelHandlerIn)).show();
    }

    public static <T> void showYesNoDialog(String messageIn, ClickHandler continueHandlerIn, CanBeShownParent parentIn) {

        (new YesNoDialog(messageIn, continueHandlerIn, parentIn)).show();
    }

    public static <T> void showYesNoDialog(String messageIn, ClickHandler continueHandlerIn,
                                           ClickHandler cancelHandlerIn, CanBeShownParent parentIn) {

        (new YesNoDialog(messageIn, continueHandlerIn, cancelHandlerIn, parentIn)).show();
    }

    public static <T> void showYesNoDialog(String titleIn, String messageIn,
                                           ClickHandler continueHandlerIn, CanBeShownParent parentIn) {

        (new YesNoDialog(titleIn, messageIn, continueHandlerIn, parentIn)).show();
    }

    public static <T> void showYesNoDialog(String titleIn, String messageIn, ClickHandler continueHandlerIn,
                                           ClickHandler cancelHandlerIn, CanBeShownParent parentIn) {

        (new YesNoDialog(titleIn, messageIn, continueHandlerIn, cancelHandlerIn, parentIn)).show();
    }

    public static <T> void showScrollingDialog(String titleIn, String messageIn) {

        (new ScrollingDialog(titleIn, messageIn)).show();
    }

    public static <T> void showScrollingDialog(String messageIn) {

        (new ScrollingDialog(messageIn)).show();
    }

    public static <T> void showScrollingDialog(String titleIn, String messageIn, CanBeShownParent parentIn) {

        (new ScrollingDialog(titleIn, messageIn, parentIn)).show();
    }

    public static <T> void showScrollingDialog(String messageIn, CsiModal parentIn) {

        (new ScrollingDialog(messageIn, parentIn)).show();
    }

    public Dialog(ClickHandler handlerIn) {
        super(txtSaveButton, txtCancelButton, handlerIn);
    }

    public Dialog(CanBeShownParent parentIn) {
        super(txtSaveButton, txtCancelButton, parentIn);
    }

    public Dialog() {
        super(txtSaveButton, txtCancelButton);
    }
}
