package csi.client.gwt.widget.ui.color;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import csi.client.gwt.widget.cells.ComboBoxEditCell;


public class DiscreteColorTypeComboBox extends ComboBox<DiscreteColorType> {

    interface ComboBoxTemplates extends XTemplates {

        @XTemplate("<img width=\"16\" height=\"15\" src=\"{dataUri}\"/>&nbsp;&nbsp;{name}")
        SafeHtml html(SafeUri dataUri, String name);

    }

    private static final ComboBoxTemplates comboBoxTemplates = GWT
            .create(ComboBoxTemplates.class);

    public DiscreteColorTypeComboBox() {

        super(new ListStore<DiscreteColorType>(new ModelKeyProvider<DiscreteColorType>(){

                    @Override
                    public String getKey(DiscreteColorType item) { return item.getType(); }}),
                new LabelProvider<DiscreteColorType>() {

                    @Override
                    public String getLabel(DiscreteColorType item) { return DiscreteColorAttribute.getInternationalizedType(item); }
                });
        addStyleName("string-combo-style");
        initialize();
    }

    public DiscreteColorTypeComboBox(ListStore<DiscreteColorType> listStore, LabelProvider<DiscreteColorType> stringLabelProvider) {
        super(listStore, stringLabelProvider);
        addStyleName("string-combo-style");
        initialize();
    }

    public DiscreteColorTypeComboBox(ComboBoxCell<DiscreteColorType> comboBoxCell) {
        super(comboBoxCell);
        addStyleName("string-combo-style");
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

        return ((ComboBoxEditCell<DiscreteColorType>)getCell()).getSelectionText();
    }

    private void initialize() {
        setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        setForceSelection(true);
        setWidth(200);
        getStore().applySort(true);
    }
}
