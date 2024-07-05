package csi.client.gwt.csiwizard.widgets;

import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.client.gwt.widget.cells.ComboBoxEditCell;
import csi.server.common.model.query.QueryParameterDef;

public class QueryParameterDefComboBox extends ComboBox<QueryParameterDef> {



    public QueryParameterDefComboBox() {
        super(
                new ListStore<QueryParameterDef>(new ModelKeyProvider<QueryParameterDef>(){

                    @Override
                    public String getKey(QueryParameterDef item) {
                        return item.getLocalId();
                    }}),
                    new LabelProvider<QueryParameterDef>() {

                    @Override
                    public String getLabel(QueryParameterDef item) {
                        return item.getName();
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

        return ((ComboBoxEditCell<QueryParameterDef>)getCell()).getSelectionText();
    }

    private void initialize() {
        setTriggerAction(TriggerAction.ALL);
        setForceSelection(true);
        setWidth(200);
        getStore().applySort(true);
    }

}
