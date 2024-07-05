package csi.client.gwt.widget.combo_boxes;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import csi.client.gwt.viz.shared.export.view.widget.ExportSize;
import csi.client.gwt.widget.cells.ComboBoxEditCell;
import csi.client.gwt.widget.misc.ExportSizeAttribute;

public class ExportSizeComboBox extends ComboBox<ExportSize> {
    interface ComboBoxTemplates extends XTemplates {

        @XTemplate("<span title=\"{name}\">&nbsp;&nbsp;{name}</span>")
        SafeHtml html(String name);
    }
    private static final ComboBoxTemplates comboBoxTemplates = GWT.create(ComboBoxTemplates.class);

    public ExportSizeComboBox() {
        super(new ListStore<ExportSize>(new ModelKeyProvider<ExportSize>(){

                    @Override
                    public String getKey(ExportSize item) { return item.getSizeDescription(); }}),
                new LabelProvider<ExportSize>() {

                    @Override
                    public String getLabel(ExportSize item) { return ExportSizeAttribute.getInternationalizedSize(item); }
                });
        addStyleName("string-combo-style");
        initialize();
    }

    public ExportSizeComboBox(ListStore<ExportSize> listStore, LabelProvider<ExportSize> stringLabelProvider) {
        super(listStore, stringLabelProvider);
        addStyleName("string-combo-style");
        initialize();
    }

    public ExportSizeComboBox(ComboBoxCell<ExportSize> comboBoxCell) {
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

        return ((ComboBoxEditCell<ExportSize>)getCell()).getSelectionText();
    }

    private void initialize() {
        setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        setForceSelection(true);
        setWidth(200);
        getStore().applySort(true);
    }

}

