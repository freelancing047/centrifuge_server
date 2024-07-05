package csi.client.gwt.validation.feedback;

import com.google.gwt.user.client.ui.Label;

import csi.client.gwt.widget.boot.Dialog;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ErrorLabelValidationFeedback implements ValidationFeedback{

    private final Label errorLabel;
    private final String errorText;

    public ErrorLabelValidationFeedback(Label errorLabel, String errorText){
        this.errorLabel = errorLabel;
        this.errorText = errorText;
    }

    @Override
    public void showValidationFeedback() {
        errorLabel.getElement().getStyle().setColor(Dialog.txtErrorColor);
        errorLabel.setText(errorText);
    }

    @Override
    public void hideValidationFeedback() {
        errorLabel.setText(""); //$NON-NLS-1$
    }

    @Override
    public String getError() {
        return errorText;
    }

    protected Label getErrorLabel() {
        return errorLabel;
    }
}
