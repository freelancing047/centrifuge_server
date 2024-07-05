package csi.client.gwt.validation.validator;

import java.util.Collection;

import com.google.gwt.user.client.TakesValue;

/**
 * @author Centrifuge Systems, Inc.
 */
public class NotDuplicateValidatorTrimAndIgnoreCase implements Validator{

    private final TakesValue<String> valueBox;
    private final Collection<String> existingValues;

    public NotDuplicateValidatorTrimAndIgnoreCase(TakesValue<String> valueBox, Collection<String> existingValues){
        this.valueBox = valueBox;
        this.existingValues = existingValues;
    }

    @Override
    public boolean isValid() {
        for (String existing : existingValues) {
            if(existing.trim().equalsIgnoreCase(valueBox.getValue().trim())){
                return false;
            }
        }
        return true;
    }
}
