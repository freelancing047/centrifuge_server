/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.widget.cells.readonly;

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

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class FieldDefNameCell<T extends DataStoreColumnAccess> extends AbstractCell<FieldDef> {

    interface FieldTemplate extends SafeHtmlTemplates {

        @Template("<span title=\"{3}\">&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{0}\"/>&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{1}\"/>&nbsp;&nbsp;<span style=\"color:{2}\">{3}</span></span>")
        SafeHtml display(SafeUri fieldUri, SafeUri dataUri, String color, String name);

    }

    interface EmptyTemplate extends SafeHtmlTemplates {

        @Template("<span style=\"color:{0}; text-align:center\">{1}</span>")
        SafeHtml display(String color, String name);

    }

    private static final FieldTemplate fieldTemplate = GWT.create(FieldTemplate.class);
    private static final EmptyTemplate emptyTemplate = GWT.create(EmptyTemplate.class);

    ListStore<? extends DataStoreColumnAccess> _dataStore = null;
    private String _defaultText = "";
    private String _defaultColor = "";
    private String _disabledColor = Dialog.txtDisabledColor;
    private String _errorColor = Dialog.txtErrorColor;
    private String _nullText = "";
    private FieldDefComboBox _comboBox = null;

    public FieldDefNameCell() {
        super();
    }

    public FieldDefNameCell(ListStore<? extends DataStoreColumnAccess> dataStoreIn, FieldDefComboBox comboBoxIn, String defaultTextIn) {
        super();

        _dataStore = dataStoreIn;
        _comboBox = comboBoxIn;
        //TODO: We allow compact here for now, not sure what best COA is
        _comboBox.removeStyleName("string-combo-style");
        _defaultText = defaultTextIn;
    }

    public FieldDefComboBox getComboBox() {
        
        return _comboBox;
    }

    public void setNullText(String textIn) {

        _nullText = textIn;
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context contextIn, FieldDef itemIn, SafeHtmlBuilder builderIn) {

        String myColor =  _defaultColor;
        String myText = _defaultText;


        if (null != _dataStore) {

            int myRow = contextIn.getIndex();

            if (null != _dataStore.get(myRow)) {

                if (_dataStore.get(myRow).isSelected()) {

                    myColor = _errorColor;

                } else {

                    myText = _nullText;
                }
            }
        }

        if (itemIn != null) {
            FieldType myFieldType = itemIn.getFieldType();
            CsiDataType myDataType = itemIn.getValueType();
            if ((null != myFieldType) && (null != myDataType)) {

                SafeUri myFieldUri = FieldDefUtils.getFieldTypeImage(myFieldType).getSafeUri();
                SafeUri myDataUri = FieldDefUtils.getDataTypeImage(myDataType).getSafeUri();

                myColor = myFieldType.getColor();
                myText = itemIn.getFieldName();

                builderIn.append(fieldTemplate.display(myFieldUri, myDataUri, myColor, myText));

            } else {

                builderIn.append(emptyTemplate.display(myColor, myText));
            }

        } else {

            builderIn.append(emptyTemplate.display(myColor, myText));
        }
    }
}
