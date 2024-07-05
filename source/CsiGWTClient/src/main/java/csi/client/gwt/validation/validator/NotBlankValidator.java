package csi.client.gwt.validation.validator;

import com.google.gwt.user.client.TakesValue;

/**
 * @author Centrifuge Systems, Inc.
 */
public class NotBlankValidator implements Validator {

    private final TakesValue<String> valueBox;

    public NotBlankValidator(TakesValue<String> valueBox){
        this.valueBox = valueBox;
    }

    @Override
    public boolean isValid() {
        return !(valueBox.getValue() == null || valueBox.getValue().trim().isEmpty());
    }
}
