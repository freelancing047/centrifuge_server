package csi.client.gwt.widget.boot;

import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Created by centrifuge on 5/24/2016.
 */
public class YesNoDialog extends ContinueDialog {

    public YesNoDialog(String titleIn, String messageIn, ClickHandler onContinueIn, ClickHandler onCancelIn) {

        super(titleIn, messageIn, onContinueIn, onCancelIn);
        setButtonText();
    }

    public YesNoDialog(String titleIn, String messageIn, ClickHandler onContinueIn) {

        super(titleIn, messageIn, onContinueIn);
        setButtonText();
    }

    public YesNoDialog(String messageIn, ClickHandler onContinueIn, ClickHandler onCancelIn) {

        super(messageIn, onContinueIn, onCancelIn);
        setButtonText();
    }

    public YesNoDialog(String messageIn, ClickHandler onContinueIn) {

        super(messageIn, onContinueIn);
        setButtonText();
    }

    public YesNoDialog(String titleIn, String messageIn, ClickHandler onContinueIn, ClickHandler onCancelIn, CanBeShownParent parentIn) {

        super(titleIn, messageIn, onContinueIn, onCancelIn, parentIn);
        setButtonText();
    }

    public YesNoDialog(String titleIn, String messageIn, ClickHandler onContinueIn, CanBeShownParent parentIn) {

        super(titleIn, messageIn, onContinueIn, parentIn);
        setButtonText();
    }

    public YesNoDialog(String messageIn, ClickHandler onContinueIn, ClickHandler onCancelIn, CanBeShownParent parentIn) {

        super(messageIn, onContinueIn, onCancelIn, parentIn);
        setButtonText();
    }

    public YesNoDialog(String messageIn, ClickHandler onContinueIn, CanBeShownParent parentIn) {

        super(messageIn, onContinueIn, parentIn);
        setButtonText();
    }

    protected void setButtonText() {

        buttonAction.setText(txtYesButton);
        buttonCancel.setText(txtNoButton);
    }
}
