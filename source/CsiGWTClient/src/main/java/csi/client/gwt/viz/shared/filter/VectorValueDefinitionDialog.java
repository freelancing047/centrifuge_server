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
package csi.client.gwt.viz.shared.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ListBox;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sencha.gxt.widget.core.client.form.DoubleField;
import com.sencha.gxt.widget.core.client.form.TextField;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.filter.PickListDialog.SelectedItemsCallback;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.gxt.form.ListStoreItemCloner;
import csi.client.gwt.widget.input_boxes.FilteredDateTimeBox;
import csi.client.gwt.widget.ui.form.EnterKeyBinder;
import csi.server.common.model.BundledFieldReference;
import csi.server.common.model.FieldDef;
import csi.server.common.model.filter.FilterExpression;
import csi.server.common.model.filter.FilterOperandType;
import csi.server.common.model.filter.MultiValueDefinition;
import csi.server.common.model.filter.OperandTypeAndValue;
import csi.shared.core.Constants;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class VectorValueDefinitionDialog extends AbstractValueDefinitionDialog {

    private static final String DATEPICKER_BTN_CLASSNAME = "btn-none";
    private TextField fieldScalarText;
    private FilteredDateTimeBox fieldScalarDate;
    private DoubleField fieldScalarDouble;
    private String dataViewUuid;
    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private boolean initialized = false;

    private Collection<FieldDef> crossColumns;

    @UiField
    HorizontalPanel fieldContainer;
    @UiField(provided = true)
    ListBox selectedValueList;
    @UiField
    Button buttonAdd;

//List crossColumnsList
//boolean columnValueSelected;

    /*
     <dialog>
       <title>#{i18n.get.vectorValueDefinitionDialogTitle}</title>
       <field>#{controller.field.fieldName}</field><operator>in</operator>

       <block render=#{controller.anyCrossColumns}>
         <radio switch=#{controller.columnValueSelected}>
           <radioOption>
             <prompt>value:</prompt><value>#{controller.scalarContainer}</value><button "add" />
           </radioOption>
           <radioOption>
             <prompt>column:</prompt><value value="#{controller.selectedValue}">#{controller.otherFields}</value><button "add" />
           </radioOption>
         </radio>
       </block>
       <block render=#{!controller.anyCrossColumns}>
         <prompt>value:</prompt><value>#{controller.scalarContainer}</value>
       </block>

       <label prompt="Proposed filter expression:">
         #{controller.proposedExpression}
       </label>

       <button "save" /><button "cancel" />
     </dialog>
     */

    interface SpecificUiBinder extends UiBinder<Dialog, VectorValueDefinitionDialog> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public VectorValueDefinitionDialog(CanBeShownParent parentIn, Collection<FieldDef> crossColumns) {
        super(parentIn);
    }

    protected Dialog getDialog() {

        selectedValueList = new ListBox(true);
        return uiBinder.createAndBindUi(this);
    }

    public void setDataViewUuid(String dataViewUuid) {
        this.dataViewUuid = dataViewUuid;
    }

    private void addHandlers() {
        dialog.getActionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                MultiValueDefinition mvd = new MultiValueDefinition();
                mvd.setDataType(getDataType());
//                List<Object> list = new ArrayList<Object>();
//                for (int i = 0; i < selectedValueList.getItemCount(); i++) {
//                    list.add(selectedValueList.getItemText(i));
//                }
                List<OperandTypeAndValue<?>> list = new ArrayList<OperandTypeAndValue<?>>();
                for (int i = 0; i < selectedValueList.getItemCount(); i++) {
                    list.add(new OperandTypeAndValue<String>(FilterOperandType.STATIC, selectedValueList.getItemText(i)));
                }
                mvd.setValues(list);
                getUpdater().update(mvd);
                destroy();
            }
        });
        dialog.getCancelButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                destroy();
            }
        });
    }

    private void initComponents() {
		try {
			switch (getDataType()) {
				case Boolean:
				case String: {
					TextField field = new TextField();
					field.addStyleName("compact");
					EnterKeyBinder.bind(field).to(buttonAdd);
					fieldContainer.add(field);
					fieldScalarText = field;
					break;
				}
                case Time: {
                    FilteredDateTimeBox field = FilteredDateTimeBox.createTimeSelector();
                    fieldContainer.add(field);
                    fieldScalarDate = field;
                    break;
                }
                case Date: {

                    FilteredDateTimeBox field = FilteredDateTimeBox.createDateSelector();
                    fieldContainer.add(field);
                    fieldScalarDate = field;
                    break;
                }
                case DateTime: {

                    FilteredDateTimeBox field = FilteredDateTimeBox.createDateTimeSelector();
                    fieldContainer.add(field);
                    fieldScalarDate = field;
                    break;
                }
				case Integer:
				case Number: {
					DoubleField field = new DoubleField();
					field.addStyleName("compact");
					EnterKeyBinder.bind(field).to(buttonAdd);
					fieldContainer.add(field);
					fieldScalarDouble = field;
					break;
				}
				case Unsupported:
					throw new RuntimeException(_constants.dontKnowHowToHandle() + " " + getDataType());
			}

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }

    @Override
    public void show() {

        if (!initialized) {

            initComponents();
            addHandlers();
            setupCurrentValues();
            initialized = true;
        }
        super.show();
    }

   private void setupCurrentValues() {
      if (getCurrentValueDefinition() instanceof MultiValueDefinition) {
         MultiValueDefinition<?> mvd = (MultiValueDefinition<?>) getCurrentValueDefinition();

         for (OperandTypeAndValue<?> value : mvd.getValues()) {
            selectedValueList.addItem(value.getValue().toString());
         }
      }
   }

    @UiHandler("buttonAdd")
    public void handleButtonAddValue(ClickEvent event) {
        switch (getDataType()) {
            case Boolean:
            case String: {
                if (!Strings.isNullOrEmpty(fieldScalarText.getValue())) {
                    selectedValueList.addItem(fieldScalarText.getValue());
                    fieldScalarText.clear();
                }
                break;
            }
            case Time:
            case Date:
            case DateTime: {
                if ((fieldScalarDate.getText() != null) && fieldScalarDate.isValid()) {
                    selectedValueList.addItem(DateTimeFormat.getFormat(Constants.DataConstants.FORMAT_DATE).format(
                            fieldScalarDate.getValue()));
                    fieldScalarDate.setText(null);
                }
                break;
            }
            case Integer:
            case Number: {
                if (fieldScalarDouble.getValue() != null) {
                    selectedValueList.addItem(fieldScalarDouble.getValue().toString());
                    fieldScalarDouble.clear();
                }
                break;
            }
            case Unsupported:
                throw new RuntimeException(_constants.dontKnowHowToHandle() + " " + getDataType());
        }
    }

    @UiHandler("buttonDelete")
    public void handleButtonDeleteValue(ClickEvent event) {
        List<Integer> indicesToRemove = new ArrayList<Integer>();
        for (int i = 0; i < selectedValueList.getItemCount(); i++) {
            if (selectedValueList.isItemSelected(i)) {
                indicesToRemove.add(i);
            }
        }
        // Sort descending so that we don't alter index locations.
        Collections.sort(indicesToRemove, new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });
        for (Integer index : indicesToRemove) {
            selectedValueList.removeItem(index);
        }
    }

    @UiHandler("buttonPickList")
    public void handleButtonPickListClick(ClickEvent event) {
        BundledFieldReference reference = new BundledFieldReference();
        FilterExpression expression = ListStoreItemCloner.getCloneWithChanges(getRecord());
        reference.setFieldDef(expression.getFieldDef());
        reference.setBundleFunction(expression.getBundleFunction());
        reference.setBundleFunctionParameters(expression.getBundleFunctionParameters());

        PickListDialog myDialog = new PickListDialog(this);
        myDialog.setFieldReference(reference);
        myDialog.setDataViewUuid(dataViewUuid);
        myDialog.setSelectedItemsCallback(new SelectedItemsCallback() {

            @Override
            public void onSuccess(List<String> selection) {
                Set<String> set = new HashSet<String>();
                for (int i = 0; i < selectedValueList.getItemCount(); i++) {
                    set.add(selectedValueList.getItemText(i));
                }
                set.addAll(selection);
                selectedValueList.clear();
                for (String value : set) {
                    selectedValueList.addItem(value);
                }
            }
        });
        myDialog.show();
    }
}
