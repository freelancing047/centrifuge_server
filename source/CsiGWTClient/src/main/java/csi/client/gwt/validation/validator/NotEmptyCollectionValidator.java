package csi.client.gwt.validation.validator;

import java.util.Collection;

/**
 * @author Centrifuge Systems, Inc.
 */
public class NotEmptyCollectionValidator implements Validator {

    private final Collection<?> collection;

    public NotEmptyCollectionValidator(Collection<?> collection){
        this.collection = collection;
    }

    @Override
    public boolean isValid() {
        return collection != null && !collection.isEmpty();
    }
}
