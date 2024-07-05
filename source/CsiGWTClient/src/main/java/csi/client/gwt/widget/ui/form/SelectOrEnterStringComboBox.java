package csi.client.gwt.widget.ui.form;

import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.event.TriggerClickEvent;
import com.sencha.gxt.widget.core.client.form.ComboBox;

/**
 * Creates a combo box where one can either Select a value from a ListStore or Enter a new value.
 * In order to populate the ListStore, call getStore().add(myString);
 *
 * @author Centrifuge Systems, Inc.
 */
public class SelectOrEnterStringComboBox extends ComboBox<String> {

    public SelectOrEnterStringComboBox(){
        super(new SimpleComboBoxCell(new ListStore<String>(new SimpleKeyProvider())));
        addStyleName("string-combo-style");
        addHandlerForClearingEnteredTextOnSelect();
    }

    private void addHandlerForClearingEnteredTextOnSelect() {
        addTriggerClickHandler(new TriggerClickEvent.TriggerClickHandler() {
            @Override
            public void onTriggerClick(TriggerClickEvent event) {
                setText(""); //$NON-NLS-1$

            }
        });
    }

    private static class SimpleComboBoxCell extends ComboBoxCell<String>{
        public SimpleComboBoxCell(ListStore<String> listStore){
            super(listStore, new SimpleLabelProvider());
        }

        @Override
        protected String getByValue(String value) {
            String existingValue = super.getByValue(value);
            if (existingValue == null) {
                return value;
            }
            return existingValue;
        }
    }

    private static class SimpleKeyProvider implements ModelKeyProvider<String> {
        public String getKey(String item) {
            return item;
        }
    }

    private static class SimpleLabelProvider implements LabelProvider<String> {
        @Override
        public String getLabel(String item) {
            return item;
        }
    }
}
