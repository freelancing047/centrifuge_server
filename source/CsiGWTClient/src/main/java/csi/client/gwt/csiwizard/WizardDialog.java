package csi.client.gwt.csiwizard;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

import csi.client.gwt.csiwizard.panels.AbstractWizardPanel;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.CsiModal;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.BlueButton;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.buttons.GreenButton;
import csi.client.gwt.widget.buttons.SimpleButton;

/**
 * Created by centrifuge on 10/26/2015.
 */
public abstract class WizardDialog extends PanelDialog implements WizardInterface {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private WizardInterface priorDialog = null;

    private String _txtExecuteButtonText = Dialog.txtCreateButton;
    private boolean _finalPanel = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Abstract Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract void createPanel();
    protected abstract void execute();


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ClickHandler handlePriorButtonClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                onPrevious();
                if (null != priorDialog) {

                    priorDialog.show();
                }
                destroy();

            } catch (Exception myException) {

                Display.error("WizardDialog", 1, myException);
            }
        }
    };

    private ClickHandler handleNextButtonClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                execute();
                hide();

            } catch (Exception myException) {

                Display.error("WizardDialog", 2, myException);
            }
        }
    };

    private ClickHandler handleCancelButtonClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                cancel();

            } catch (Exception myException) {

                Display.error("WizardDialog", 3, myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public WizardDialog(WizardInterface priorDialogIn, AbstractWizardPanel panelIn,
                        String titleIn, String helpIn, String[] instructionsIn, String delimiterIn) {

        super(panelIn, titleIn, helpIn, instructionsIn, delimiterIn);
        setPriorDialog(priorDialogIn);
        createPanel();
    }

    public WizardDialog(AbstractWizardPanel panelIn,
                        String titleIn, String helpIn, String[] instructionsIn, String delimiterIn) {

        super(panelIn, titleIn, helpIn, instructionsIn, delimiterIn);
        setPriorDialog(null);
        createPanel();
    }

    public WizardDialog(WizardInterface priorDialogIn, AbstractWizardPanel panelIn,
                        String titleIn, String helpIn, String instructionsIn) {

        super(panelIn, titleIn, helpIn, instructionsIn);
        setPriorDialog(priorDialogIn);
        createPanel();
    }

    public WizardDialog(AbstractWizardPanel panelIn,
                        String titleIn, String helpIn, String instructionsIn) {

        super(panelIn, titleIn, helpIn, instructionsIn);
        setPriorDialog(null);
        createPanel();
    }

    public void setAsFinal() {

        _finalPanel = true;
        identifyButtons();
    }

    public void setPriorDialog(WizardInterface priorDialogIn) {

        priorDialog = priorDialogIn;
        setParent(priorDialog);
    }

    public WizardInterface getPriorDialog() {

        return priorDialog;
    }

    @Override
    public void show() {

        try {

            identifyButtons();
            show(Dialog.intWizardButtonWidth);
            if (null != priorDialog) {

                priorDialog.hide();
            }

        } catch (Exception myException) {

            Display.error("WizardDialog", 4, myException);
        }
    }

    @Override
    public void show(int buttonWidthIn) {

        try {

            identifyButtons();
            super.show(buttonWidthIn);
            if(null != priorDialog) {

                priorDialog.hide();
            }

        } catch (Exception myException) {

            Display.error("WizardDialog", 5, myException);
        }
    }

    public void show(String buttonTextIn) {

        try {

            _txtExecuteButtonText = buttonTextIn;
            identifyButtons();
            show(Dialog.intWizardButtonWidth);

        } catch (Exception myException) {

            Display.error("WizardDialog", 6, myException);
        }
    }

    public void show(String buttonTextIn, boolean isFinalIn) {

        try {

            _txtExecuteButtonText = buttonTextIn;
            _finalPanel = isFinalIn;
            identifyButtons();
            show(Dialog.intWizardButtonWidth);

        } catch (Exception myException) {

            Display.error("WizardDialog", 6, myException);
        }
    }

    public void show(String buttonTextIn, int buttonWidthIn) {

        try {

            _txtExecuteButtonText = buttonTextIn;
            identifyButtons();
            show(buttonWidthIn);

        } catch (Exception myException) {

            Display.error("WizardDialog", 7, myException);
        }
    }

    public void show(final Exception exceptionIn) {

        show();

        DeferredCommand.add(new Command() {
            public void execute() {
                Display.error(exceptionIn);
            }
        });
    }

    @Override
    public void cancel() {

        try {

            onCancel();

            if (null != priorDialog) {

                priorDialog.cancel();
                priorDialog = null;
            }
            hideWatchBox();
            destroy();
            CsiModal.clearAll();

        } catch (Exception myException) {

            Display.error("WizardDialog", 8, myException);
        }
    }

    public void clickPrior() {

        handlePriorButtonClick.onClick(null);
    }

    public void clickNext() {

        handleNextButtonClick.onClick(null);
    }

    public void clickCancel() {

        handleCancelButtonClick.onClick(null);
    }

    public void clickExecute() {

        handleExecuteButtonClick.onClick(null);
    }

    public void setExecuteText(String buttonTextIn) {

        _txtExecuteButtonText = buttonTextIn;

        super.setExecuteText(buttonTextIn);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected String getExecuteButtonText() {

        return _txtExecuteButtonText;
    }

    protected void exitAndDiscard() {

        if (null != priorDialog) {

            priorDialog.show();
        }
        destroy();
    }

    protected void exitAndReturn() {

        if (null != priorDialog) {

            priorDialog.showWithResults(this);

        } else {

            destroy();
        }
    }

    protected void onCancel() {

    }

    protected void onPrevious() {

    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void identifyButtons() {

        List<WizardButton> myList = new ArrayList<WizardButton>(4);

        //
        // Set up the dialog cancel button
        //
        myList.add(new WizardButton(new SimpleButton(), Dialog.txtCancelButton, handleCancelButtonClick, true, false));

        if (_finalPanel) {

            //
            // Set up the dialog action button
            //
            myList.add(new WizardButton(new GreenButton(), _txtExecuteButtonText, handleNextButtonClick, true, true));

            //
            // Set up the Next button
            //
            myList.add(new WizardButton(new BlueButton(), Dialog.txtNextButton, null, false, false));

        } else {

            //
            // Set up the dialog action button
            //
            myList.add(new WizardButton(new GreenButton(), _txtExecuteButtonText, null, false, false));

            //
            // Set up the Next button
            //
            myList.add(new WizardButton(new BlueButton(), Dialog.txtNextButton, handleNextButtonClick, true, true));
        }

        //
        // Set up the Previous button to simulate wizard appearance
        //
        if (null != priorDialog) {

            myList.add(new WizardButton(new SimpleButton(), Dialog.txtPreviousButton,
                    handlePriorButtonClick, true, false));

        } else {

            myList.add(new WizardButton(new SimpleButton(), Dialog.txtPreviousButton, null, false, false));
        }
        replaceButtons(myList, 1, 0);
    }
}
