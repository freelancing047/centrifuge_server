package csi.client.gwt.widget.cells.readonly;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import csi.client.gwt.csi_resource.OptionControl;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.combo_boxes.ImportOptionComboBox;
import csi.server.common.interfaces.TrippleDisplay;

/**
 * Created by centrifuge on 4/22/2019.
 */
public class OptionCell<S extends TrippleDisplay, T extends OptionControl<S>> extends AbstractCell<S> {

    interface SelectedTemplate extends SafeHtmlTemplates {

        @Template("<span qtip=\"{3}\" qtitle=\"{2}\"><span style=\"color:{0}\">{1}</span></span>")
        SafeHtml display(String color, String name, String title, String description);
    }
    interface EmptyTemplate extends SafeHtmlTemplates {

        @Template("<span style=\"color:{0}\">{1}</span>")
        SafeHtml display(String color, String name);
    }

    private static final SelectedTemplate selectedTemplate = GWT.create(SelectedTemplate.class);
    private static final EmptyTemplate emptyTemplate = GWT.create(EmptyTemplate.class);

    ListStore<T> _dataStore = null;
    private String _defaultText = "Replace";
    private String _defaultColor = "";
    private String _disabledColor = Dialog.txtDisabledColor;
    private String _errorColor = Dialog.txtErrorColor;
    private String _specialColor = Dialog.txtInfoColor;
    private String _errorText = "Select option";
    private String _nullText = "No action";
    private ImportOptionComboBox<S> _comboBox = null;

    public OptionCell() {
        super();
    }

    public OptionCell(ImportOptionComboBox<S> comboBoxIn, String defaultTextIn) {
        super();

        _defaultText = defaultTextIn;
        _comboBox = comboBoxIn;
        //TODO: We allow compact here for now, not sure what best COA is
        _comboBox.removeStyleName("string-combo-style");
    }

    public OptionCell(ListStore<T> dataStoreIn, ImportOptionComboBox<S> comboBoxIn, String defaultTextIn) {
        super();

        _defaultText = defaultTextIn;
        _dataStore = dataStoreIn;
        _comboBox = comboBoxIn;
        //TODO: We allow compact here for now, not sure what best COA is
        _comboBox.removeStyleName("string-combo-style");
    }

    public void setStore(ListStore<T> dataStoreIn) {

        _dataStore = dataStoreIn;
    }

    public ImportOptionComboBox<S> getComboBox() {

        return _comboBox;
    }

    public void setNullText(String textIn) {

        _nullText = textIn;
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context contextIn, S itemIn, SafeHtmlBuilder builderIn) {

        SafeHtml myDisplay = emptyTemplate.display(_defaultColor, _nullText);

        if (null != _dataStore) {

            int myRow = contextIn.getIndex();

            if (null != _dataStore.get(myRow)) {

                T myObject = _dataStore.get(myRow);

                if (myObject.getSelected()) {

                    S myItem = (null != itemIn) ? itemIn : myObject.getOption();

                    if (myItem != null) {

                        String myTitle = myItem.getTitle();
                        String myDescription = myItem.getDescription();
                        String myName = myItem.getLabel();

                        myDisplay = selectedTemplate.display(_defaultColor, myName, myTitle, myDescription);

                    } else if (myObject.getConflicts()) {

                        myDisplay = emptyTemplate.display(_errorColor, _errorText);

                    } else {

                        myDisplay = emptyTemplate.display(_defaultColor, _defaultText);
                    }
                }
            }
        }
        builderIn.append(myDisplay);
    }
}
