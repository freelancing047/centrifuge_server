package csi.client.gwt.validation.validator;

import com.google.gwt.user.client.TakesValue;

/**
 * @author Centrifuge Systems, Inc.
 */
public class IntNotBlankValidator implements Validator {

    private final TakesValue<Integer> valueBox;

    public IntNotBlankValidator(TakesValue<Integer> valueBox){
        this.valueBox = valueBox;
    }

    @Override
    public boolean isValid() {
        return !(valueBox.getValue() == null);
    }
}
