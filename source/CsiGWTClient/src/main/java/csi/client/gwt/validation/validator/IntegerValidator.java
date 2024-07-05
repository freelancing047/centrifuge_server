package csi.client.gwt.validation.validator;

import com.google.gwt.user.client.TakesValue;

/**
 * @author Centrifuge Systems, Inc.
 */
public class IntegerValidator implements Validator {

    private final TakesValue<String> valueBox;

    public IntegerValidator(TakesValue<String> valueBox){
        this.valueBox = valueBox;
    }


    @Override
    public boolean isValid() {
        String value = valueBox.getValue();
        if(value == null || value.equals("")) //$NON-NLS-1$
            return true;

        try{
            Integer.parseInt(value.trim());
        }catch (Exception e){
            return false;
        }
        return true;
    }
}
