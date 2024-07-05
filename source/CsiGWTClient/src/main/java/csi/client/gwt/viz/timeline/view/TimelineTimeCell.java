package csi.client.gwt.viz.timeline.view;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.sencha.gxt.data.shared.ListStore;

import csi.client.gwt.widget.gxt.grid.DataStoreColumnAccess;
import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.visualization.timeline.TimelineTimeSetting;

public class TimelineTimeCell extends AbstractCell<TimelineTimeSetting> {

    interface TimelineTimeTemplate extends SafeHtmlTemplates {

        @Template("<img width=\"16\" height=\"15\" src=\"{0}\"/>&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{1}\"/>&nbsp;&nbsp;<span style=\"color:{2}\">{3}<span>")
        SafeHtml display(SafeUri fieldUri, SafeUri dataUri, String color, String name);

    }

    interface EmptyTemplate extends SafeHtmlTemplates {

        @Template("<span style=\"color:{0}; text-align:center\">{1}<span>")
        SafeHtml display(String color, String name);

    }

    private static final TimelineTimeTemplate fieldTemplate = GWT.create(TimelineTimeTemplate.class);
    private static final EmptyTemplate emptyTemplate = GWT.create(EmptyTemplate.class);

    ListStore<? extends DataStoreColumnAccess> _dataStore = null;
    private String _defaultText = "";
    private String _defaultColor = "";
    private String _disabledColor = Dialog.txtDisabledColor;
    private String _errorColor = Dialog.txtErrorColor;
    private FieldDefComboBox _comboBox = null;

    public TimelineTimeCell() {
        super();

    }

    public TimelineTimeCell(ListStore<? extends DataStoreColumnAccess> dataStoreIn, FieldDefComboBox comboBoxIn,
            String defaultTextIn) {
        super();

        _dataStore = dataStoreIn;
        _comboBox = comboBoxIn;
        _defaultText = defaultTextIn;
    }

    public FieldDefComboBox getComboBox() {

        return _comboBox;
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context contextIn, TimelineTimeSetting itemIn,
            SafeHtmlBuilder builderIn) {

        String myColor = _defaultColor;
        String myText = _defaultText;

        if (null != _dataStore) {

            int myRow = contextIn.getIndex();

            if (null != _dataStore.get(myRow)) {

                if (_dataStore.get(myRow).isSelected()) {

                    myColor = _errorColor;

                } else {

                    myText = "";
                }
            }
        }

        if (itemIn != null) {
            FieldDef field = itemIn.getFieldDef();
            FieldType myFieldType = field.getFieldType();
            CsiDataType myDataType = field.getValueType();
            if ((null != myFieldType) && (null != myDataType)) {

                SafeUri myFieldUri = FieldDefUtils.getFieldTypeImage(myFieldType).getSafeUri();
                SafeUri myDataUri = FieldDefUtils.getDataTypeImage(myDataType).getSafeUri();

                myColor = myFieldType.getColor();
                myText = field.getFieldName();

                builderIn.append(fieldTemplate.display(myFieldUri, myDataUri, myColor, myText));

            } else {

                builderIn.append(emptyTemplate.display(myColor, myText));
            }

        } else {

            builderIn.append(emptyTemplate.display(myColor, myText));
        }
    }
}
