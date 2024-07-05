package csi.client.gwt.validation.validator;

import com.google.gwt.user.client.TakesValue;

/**
 * @author Centrifuge Systems, Inc.
 */
public class RangeValidator implements Validator {

    private final TakesValue<String> valueBox;
    private final double min;
    private final double max;

    public RangeValidator(TakesValue<String> valueBox, double min, double max) {
        this.valueBox = valueBox;
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean isValid() {
        String value = valueBox.getValue();
        double d = Double.valueOf(value);
        return d >= min && d <= max;
    }
}
