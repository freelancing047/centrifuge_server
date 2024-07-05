package csi.client.gwt.validation.feedback;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueBoxBase;

import csi.client.gwt.widget.boot.Dialog;

/**
 * @author Centrifuge Systems, Inc.
 */
public class InfoRestoredValueBoxErrorLabelValidationFeedback extends ValueBoxErrorLabelValidationFeedback {
    private final String infoText;

    public InfoRestoredValueBoxErrorLabelValidationFeedback(String infoText, ValueBoxBase<String> valueBox, Label errorLabel, String errorText) {
        super(valueBox, errorLabel, errorText);
        this.infoText = infoText;
    }

    @Override
    public void hideValidationFeedback() {
        super.hideValidationFeedback();
        getErrorLabel().setText(infoText);
        getErrorLabel().getElement().getStyle().setColor(Dialog.txtInfoColor);
    }
}
