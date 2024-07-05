package csi.client.gwt.validation.validator;

import com.sencha.gxt.widget.core.client.form.ComboBox;

public class ComboBoxBlankValidator implements Validator {

    private final ComboBox<?> comboBox;

    public ComboBoxBlankValidator(ComboBox<?> comboBox){
        this.comboBox = comboBox;
    }

    @Override
    public boolean isValid() {

        //Had to use getText() for comboBox, because getValue is undefined onselection sometimes.
        //May want to make a more generic HasText validator
        return !(comboBox.getText() == null || comboBox.getText().trim().isEmpty());
    }

}
