package csi.client.gwt.widget.combo_boxes;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.input_boxes.ValidityCheckCapable;

/**
 * Created by centrifuge on 1/7/2015.
 */
public class FilteredComboBox<T extends ComboBox> extends HorizontalPanel implements ValidityCheckCapable {

    boolean _isRequired = false;
    T _comboBox = null;

    public FilteredComboBox(T comboBoxIn) {

        _comboBox = comboBoxIn;
        add(_comboBox);
    }

    public boolean isConditionallyValid() {

        if (_comboBox.isEnabled() && _comboBox.isVisible()) {

            return isValid();

        } else {

            _comboBox.getElement().getStyle().setColor(Dialog.txtLabelColor);
            return true;
        }
    }

    public boolean isValid() {

        String myTest = _comboBox.getText();

        if ((null != myTest) && (0 < myTest.length())) {

            return true;

        } else if (_isRequired) {

            return false;

        } else {

            return true;
        }
    }

    public boolean isRequired() {

        return _isRequired;
    }

    public void setRequired(boolean isRequiredIn) {

        _isRequired = isRequiredIn;
    }

    public <T> ListStore<T> getStore(T sampleIn) {

        return _comboBox.getStore();
    }

    public void setEnabled(boolean enabledIn) {

        _comboBox.setEnabled(enabledIn);
    }

    public void setEmptyText(String stringIn) {

        _comboBox.setEmptyText(stringIn);
    }

    public void setWidth(int widthIn) {

        _comboBox.setWidth(widthIn);
    }

    @Override
    public void setWidth(String widthIn) {

        _comboBox.setWidth(widthIn);
    }

    public void setEditable(boolean isEditableIn) {

        _comboBox.setEditable(isEditableIn);
    }
}
