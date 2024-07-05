package csi.client.gwt.validation.validator;

import com.google.gwt.user.client.TakesValue;

/**
 * @author Centrifuge Systems, Inc.
 */
public class PositiveIntegerValidator implements Validator{
    private final TakesValue<String> valueBox;

    public PositiveIntegerValidator(TakesValue<String> valueBox) {
        this.valueBox = valueBox;
    }

    @Override
    public boolean isValid() {
        String value = valueBox.getValue();
        if(value == null || value.equals("")) //$NON-NLS-1$
            return true;

        try{
            int num = Integer.parseInt(value.trim());
            if(num <= 0)
                return false;
        }catch (Exception e){
            return false;
        }
        return true;
    }
}
