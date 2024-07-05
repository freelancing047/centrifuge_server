package csi.client.gwt.validation.validator;

import java.util.Collection;

/**
 * Created by centrifuge on 5/29/2019.
 */
public class VariableNotEmptyCollectionValidator implements Validator {

    private Collection<?> collection;

    public VariableNotEmptyCollectionValidator(Collection<?> collection){
        this.collection = collection;
    }

    public void replaceCollection(Collection<?> collectionIn) {

        collection = collectionIn;
    }

    @Override
    public boolean isValid() {
        return collection != null && !collection.isEmpty();
    }
}
