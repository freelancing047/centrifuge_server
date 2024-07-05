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

import java.util.Collection;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;
import com.sencha.gxt.widget.core.client.form.DoubleField;
import com.sencha.gxt.widget.core.client.form.TextField;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.util.FieldDefUtils.SortOrder;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.client.gwt.widget.input_boxes.FilteredDateTimeBox;
import csi.server.common.enumerations.OperandCardinality;
import csi.server.common.enumerations.RelationalOperator;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldReference;
import csi.server.common.model.FieldType;
import csi.server.common.model.filter.FilterOperandType;
import csi.server.common.model.filter.OperandTypeAndValue;
import csi.server.common.model.filter.ScalarValueDefinition;
import csi.server.common.model.filter.StaticFieldValueDefinition;
import csi.server.common.model.filter.ValueDefinition;
import csi.server.common.util.StringUtil;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ScalarValueDefinitionDialog extends AbstractValueDefinitionDialog {
    private DataModelDef dataModelDef;
    private TextField fieldScalarText;
    private FilteredDateTimeBox fieldScalarDate;
    private DoubleField fieldScalarDouble;
    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private RelationalOperator operator;
    private boolean initialized = false;

    private boolean anyCrossColumns;
    private boolean columnValueSelected;
    private Collection<FieldDef> crossColumns;
    private FieldDef selectedFieldDef;

    @UiField
    CardLayoutContainer cardContainer;

    @UiField
    LayoutPanel scalarContainer;

    @UiField
    SimpleLayoutPanel staticContainer;

    @UiField
    FieldDefComboBox staticFieldList;

//    @UiField
//    Label fieldName;
//
//    @UiField
//    Label operatorField;
//
//    @UiField
//    ?Listbox otherFields;  //"dataTypeSymbol columnName"
//
//    @UiField
//    Label proposedExpression;

    /*
     <dialog>
       <title>#{i18n.get.scalarValueDefinitionDialogTitle}</title>
       <field>#{controller.field.fieldName}</field><operator>#{controller.operatorField}</operator>

       <block render=#{controller.anyCrossColumns}>
         <radio switch=#{controller.columnValueSelected}>
           <radioOption>
             <prompt>value:</prompt><value>#{controller.scalarContainer}</value>
           </radioOption>
           <radioOption>
             <prompt>column:</prompt><value value="#{controller.scalarSelectedField}">#{controller.otherFields}</value>
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

    interface SpecificUiBinder extends UiBinder<Dialog,ScalarValueDefinitionDialog> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public ScalarValueDefinitionDialog(CanBeShownParent parentIn, DataModelDef dataModelDefIn,
                                       RelationalOperator operatorIn, Collection<FieldDef> crossColumns) {
       super(parentIn);
       dataModelDef = dataModelDefIn;
       operator = operatorIn;

       anyCrossColumns = !crossColumns.isEmpty();
       this.crossColumns = crossColumns;

//        fieldName.setText(getCurrentValueDefinition().toString());
//        operatorField.setText(operatorIn.name());
       // scalarContainer.add(field);
    }

   protected Dialog getDialog() {
      return uiBinder.createAndBindUi(this);
   }

//    public Label getFieldName() {
//       return fieldName;
//    }
//    public Label getOperatorField() {
//       return operatorField;
//    }

   public boolean columnsAvailable() {
      return anyCrossColumns;
   }

   public boolean usingColumn() {
      return columnValueSelected;
   }

   private void addHandlers() {
      dialog.getCancelButton().addClickHandler(new ClickHandler() {
         @Override
         public void onClick(ClickEvent event) {
            destroy();
         }
      });

      dialog.getActionButton().addClickHandler(new ClickHandler() {
         @Override
         public void onClick(ClickEvent event) {
            if (cardContainer.getActiveWidget() == scalarContainer) {
               ScalarValueDefinition<?> svd = new ScalarValueDefinition();

               svd.setDataType(getDataType());

               if (columnValueSelected) {
                  svd.setValue(new OperandTypeAndValue(FilterOperandType.COLUMN, selectedFieldDef));
               } else {
                  switch (getDataType()) {
                     case Boolean:
                     case String:
                        svd.setValue(new OperandTypeAndValue(FilterOperandType.STATIC, fieldScalarText.getValue()));
                        break;
                     case Date:
                        svd.setValue(new OperandTypeAndValue(FilterOperandType.STATIC, fieldScalarDate.getText()));
                        break;
                     case DateTime:
                        // fieldScalarDate.setMinView("");
                        svd.setValue(new OperandTypeAndValue(FilterOperandType.STATIC, fieldScalarDate.getText()));
                        break;
                     case Time:
                        // fieldScalarDate.setMinView("");
                        svd.setValue(new OperandTypeAndValue(FilterOperandType.STATIC, fieldScalarDate.getText()));
                        break;
                     case Integer:
                     case Number:
                        svd.setValue(new OperandTypeAndValue(FilterOperandType.STATIC,
                                                             fieldScalarDouble.getValue().toString()));
                        break;
                     case Unsupported:
                        throw new RuntimeException(_constants.dontKnowHowToHandle() + " " + getDataType());
                  }
               }
               getUpdater().update(svd);
            } else {
               StaticFieldValueDefinition sfvd = new StaticFieldValueDefinition();

               sfvd.setStaticFieldReference(new FieldReference(staticFieldList.getCurrentValue()));
               getUpdater().update(sfvd);
            }
            destroy();
         }
      });
   }

   private void initComponents() {
      staticFieldList.getStore().addAll(
         Lists.newArrayList(
            Iterables.filter(
               FieldDefUtils.getSortedStaticFields(this.dataModelDef, SortOrder.ALPHABETIC),
               new Predicate<FieldDef>() {
                  @Override
                  public boolean apply(@Nullable FieldDef input) {
                     return (input.getFieldType() != FieldType.STATIC) ||
                            (!Strings.isNullOrEmpty(input.getFieldName()) && (input.getValueType() == null));
                  }
               })
            )
         );

      try {
         switch (getDataType()) {
            case Boolean:
            case String:
               TextField textField = new TextField();

               textField.setAllowBlank(false);
               textField.addStyleName("compact");
               textField.addStyleName("filterValueField");
               scalarContainer.add(textField);

               fieldScalarText = textField;
               break;
            case Date:
               FilteredDateTimeBox dateField = FilteredDateTimeBox.createDateSelector();

               addCalendarDropDown(dateField);

               fieldScalarDate = dateField;
               break;
            case DateTime:
               FilteredDateTimeBox dateTimeField = FilteredDateTimeBox.createDateTimeSelector();

               addCalendarDropDown(dateTimeField);

               fieldScalarDate = dateTimeField;
               break;
            case Time:
               FilteredDateTimeBox timeField = FilteredDateTimeBox.createTimeSelector();

               addCalendarDropDown(timeField);

               fieldScalarDate = timeField;
               break;
            case Integer:
            case Number:
               DoubleField doubleField = new DoubleField();

               doubleField.setAllowBlank(false);
               doubleField.addStyleName("compact");
               scalarContainer.add(doubleField);

               fieldScalarDouble = doubleField;
               break;
            case Unsupported:
               throw new RuntimeException(_constants.dontKnowHowToHandle() + " " + getDataType());
         }
      } catch (Exception exception) {
         Dialog.showException(exception);
      }
   }

   public void show() {
      if (!initialized) {
         initComponents();
         addHandlers();
         setupCurrentValues();

         initialized = true;
      }
      super.show();

      DeferredCommand.add(new Command() {
         public void execute() {
            setFocus();
         }
      });
   }

   private void setupCurrentValues() {
      ValueDefinition valueDefinition = getCurrentValueDefinition();

      if (valueDefinition != null) {
         OperandCardinality oc = valueDefinition.getCardinality();

         if (oc == OperandCardinality.SCALAR) {
            if (valueDefinition instanceof ScalarValueDefinition) {
               ScalarValueDefinition<?> definition = (ScalarValueDefinition<?>) valueDefinition;

               if (getDataType() == definition.getDataType()) {
                  OperandTypeAndValue<?> value = definition.getValue();

                  switch (value.getType()) {
                     case STATIC:
                        switch (getDataType()) {
                           case Boolean:
                           case String:
                              fieldScalarText.setValue(fromSqlString((String) value.getValue()));
                              break;
                           case Date:
                           case DateTime:
                           case Time:
                              fieldScalarDate.setText((String) value.getValue());
                              break;
                           case Integer:
                           case Number:
                              fieldScalarDouble.setValue(Double.valueOf((String) value.getValue()));
                              break;
                           case Unsupported:
                              throw new RuntimeException(_constants.dontKnowHowToHandle() + " " + getDataType());
                        }
                        break;
                     case COLUMN:
                        selectedFieldDef = (FieldDef) value.getValue();
                        break;
                     case PARAMETER:
                        break;
                  }
               }
            } else if (valueDefinition instanceof StaticFieldValueDefinition) {
               StaticFieldValueDefinition sfvd = (StaticFieldValueDefinition) valueDefinition;

               if (sfvd.getStaticFieldReference().getField().getValueType() == getDataType()) {
                  staticFieldList.setValue(sfvd.getStaticFieldReference().getField());
               }
            }
         }
      }
   }

   private FilteredDateTimeBox addCalendarDropDown(FilteredDateTimeBox fieldScalarDateIn) {
      fieldScalarDate = fieldScalarDateIn;

      scalarContainer.add(fieldScalarDate);
      checkValidity();
      return fieldScalarDate;
   }

   private void checkValidity() {
      dialog.getActionButton().setEnabled(fieldScalarDate.isValid());

      DeferredCommand.add(new Command() {
         public void execute() {
            checkValidity();
         }
      });
   }

   private String toSqlString(String stringIn) {
      String result = stringIn;

      if ((stringIn != null) && RelationalOperator.LIKE.equals(operator)) {
         result = StringUtil.patternToSql(stringIn);
      }
      return result;
   }

   private String fromSqlString(String stringIn) {
      String result = stringIn;

      if ((stringIn != null) && RelationalOperator.LIKE.equals(operator)) {
         result = StringUtil.patternFromSql(stringIn);
      }
      return result;
   }

   private void setFocus() {
      switch (getDataType()) {
         case Boolean:
         case String:
            fieldScalarText.setSelectOnFocus(true);
            fieldScalarText.selectAll();
            fieldScalarText.focus();
            break;
         case Date:
         case DateTime:
         case Time:
            fieldScalarDate.grabFocus();
            break;
         case Integer:
         case Number:
            fieldScalarDouble.setSelectOnFocus(true);
            fieldScalarDouble.selectAll();
            fieldScalarDouble.focus();
            break;
         default:
            break;
      }
   }
}
