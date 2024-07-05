package csi.client.gwt.validation.feedback;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueBoxBase;

import csi.client.gwt.widget.boot.Dialog;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ValueBoxErrorLabelValidationFeedback extends ErrorLabelValidationFeedback{

    private final ValueBoxBase<String> valueBox;

    public ValueBoxErrorLabelValidationFeedback(ValueBoxBase<String> valueBox, Label errorLabel, String errorText){
        super(errorLabel, errorText);
        this.valueBox = valueBox;
    }

    @Override
    public void showValidationFeedback() {
        super.showValidationFeedback();
        valueBox.getElement().getStyle().setBorderColor(Dialog.txtErrorColor);
        valueBox.getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
        valueBox.getElement().getStyle().setBorderWidth(1, Style.Unit.PX);
    }

    @Override
    public void hideValidationFeedback() {
        super.hideValidationFeedback();
        valueBox.getElement().getStyle().clearBorderColor();
        valueBox.getElement().getStyle().clearBorderStyle();
        valueBox.getElement().getStyle().clearBorderWidth();
    }

}
