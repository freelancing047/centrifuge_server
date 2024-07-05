package csi.client.gwt.csiwizard.widgets;

import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.client.gwt.widget.cells.ComboBoxEditCell;
import csi.server.common.model.filter.FilterOperatorType;

public class FilterOperatorComboBox extends ComboBox<FilterOperatorType> {



    public FilterOperatorComboBox() {
        super(
                new ListStore<FilterOperatorType>(new ModelKeyProvider<FilterOperatorType>(){

                    @Override
                    public String getKey(FilterOperatorType item) {
                        return item.name();
                    }}),
                    new LabelProvider<FilterOperatorType>() {

                    @Override
                    public String getLabel(FilterOperatorType item) {
                        return item.getLabel();
                    }
                }
                );
        addStyleName("string-combo-style");
        this.setTypeAhead(false);
        initialize();
    }

    public int getItemCount() {
        return getStore().size();
    }

    public int getSelectedIndex() {
        return getStore().indexOf(getCurrentValue());
    }

    public void setSelectedIndex(int i) {
        setValue(getStore().get(i));
    }

    public String getCurrentCellText() {

        return ((ComboBoxEditCell<FilterOperatorType>)getCell()).getSelectionText();
    }

    private void initialize() {
        setTriggerAction(TriggerAction.ALL);
        setForceSelection(true);
        setWidth(200);
        getStore().applySort(true);
    }

}
