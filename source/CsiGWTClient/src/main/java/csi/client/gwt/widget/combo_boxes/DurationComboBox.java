package csi.client.gwt.widget.combo_boxes;

import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.client.gwt.dataview.fieldlist.editor.scripted.widget.DurationUnitLabelProvider;
import csi.client.gwt.widget.cells.ComboBoxEditCell;
import csi.server.common.model.DurationUnit;

public class DurationComboBox extends ComboBox<DurationUnit> {



    public DurationComboBox() {
        super(
                new ListStore<DurationUnit>(new ModelKeyProvider<DurationUnit>(){

                    @Override
                    public String getKey(DurationUnit item) {
                        return item.toString();
                    }}),
                    new DurationUnitLabelProvider()
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

        return ((ComboBoxEditCell<DurationUnit>)getCell()).getSelectionText();
    }

    private void initialize() {
        setTriggerAction(TriggerAction.ALL);
        setForceSelection(true);
        setWidth(200);
        getStore().applySort(true);
        
        for (DurationUnit unit : DurationUnit.values()) {
            this.getStore().add(unit);
        }
    }

}
