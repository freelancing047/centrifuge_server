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

import java.text.ParseException;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.form.PropertyEditor;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.filter.CreateEditFilterDialog.FilterExpressionPropertyAccess;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.gxt.form.TriggerBaseCell;
import csi.server.common.enumerations.OperandCardinality;
import csi.server.common.enumerations.RelationalOperator;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.filter.FilterExpression;
import csi.server.common.model.filter.FilterOperandType;
import csi.server.common.model.filter.MultiValueDefinition;
import csi.server.common.model.filter.NullValueDefinition;
import csi.server.common.model.filter.OperandTypeAndValue;
import csi.server.common.model.filter.ScalarValueDefinition;
import csi.server.common.model.filter.ValueDefinition;
import csi.server.common.util.StringUtil;
import csi.server.util.sql.api.BundleFunction;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ValueDefinitionCell extends TriggerBaseCell<ValueDefinition> {
   private static final DisplayTemplate displayTemplate = GWT.create(DisplayTemplate.class);

   interface DisplayTemplate extends SafeHtmlTemplates {
      @Template("<div>null</div>")
      public SafeHtml displayNull();
   }
   private ListStore<FilterExpression> gridStore;
   private CanBeShownParent parent;
   private FilterExpressionPropertyAccess propertyAccess;
   private DataModelDef dataModelDef;
   private String dataViewUuid;

   public ValueDefinitionCell(CanBeShownParent parentIn, ListStore<FilterExpression> storeIn,
                              FilterExpressionPropertyAccess propertyAccessIn, DataModelDef dataModelDefIn,
                              String dataViewUuidIn, int widthIn) {
      gridStore = storeIn;
      parent = parentIn;
      propertyAccess = propertyAccessIn;
      dataModelDef = dataModelDefIn;
      dataViewUuid = dataViewUuidIn;

      setHideTrigger(true);
      setWidth(Math.max(120, (widthIn - 20)));

      setPropertyEditor(
         new PropertyEditor<ValueDefinition>() {
            @Override
            public ValueDefinition parse(CharSequence text) throws ParseException {
               return null;
            }

            @Override
            public String render(ValueDefinition object) {
               if (object == null) {
                  return "";
               }
               if (object instanceof MultiValueDefinition) {
                  MultiValueDefinition valueDefinition = (MultiValueDefinition) object;

                  if ((valueDefinition.getValues() == null) || valueDefinition.getValues().isEmpty()) {
                     return "";
                  }
               }
               if (object.getShortValueDescription().equals("<multiple values>")) {
                  return CentrifugeConstantsLocator.get().multipleValues();
               } else {
                  return object.getShortValueDescription();
               }
            }
         });
   }

   private FilterExpression getFilterExpression(Context context) {
      Context ctx = (context == null) ? getCurrentContext() : context;

      return gridStore.findModelWithKey(ctx.getKey().toString());
   }

   @Override
   protected void onTriggerClick(com.google.gwt.cell.client.Cell.Context context, XElement parentIn, NativeEvent event,
                                 ValueDefinition value, ValueUpdater<ValueDefinition> updater) {
      FilterExpression filterExpression = getFilterExpression(context);
      Store<FilterExpression>.Record record = gridStore.getRecord(filterExpression);
      BundleFunction bundle = record.getValue(propertyAccess.bundleFunction());
      FieldDef fieldDef = record.getValue(propertyAccess.field());
      RelationalOperator operator = record.getValue(propertyAccess.operator());
      OperandCardinality cardinality = (operator == null) ? OperandCardinality.SCALAR : operator.getCardinality();

      switch (cardinality) {
         case NONE:
            updater.update(new NullValueDefinition());
            break;
         case SCALAR:
            ScalarValueDefinitionDialog scalarDialog =
               new ScalarValueDefinitionDialog(parent, dataModelDef, operator, filterExpression.getCrossColumns());

            scalarDialog.setDataType(bundle.getReturnType(fieldDef));
            scalarDialog.setRecord(record);
            scalarDialog.setUpdater(updater);
            scalarDialog.setCurrentValueDefinition(value);
            scalarDialog.show();
            break;
          case VECTOR:
            VectorValueDefinitionDialog vectorDialog =
               new VectorValueDefinitionDialog(parent, filterExpression.getCrossColumns());

            vectorDialog.setDataType(bundle.getReturnType(fieldDef));
            vectorDialog.setRecord(record);
            vectorDialog.setUpdater(updater);
            vectorDialog.setCurrentValueDefinition(value);
            vectorDialog.setDataViewUuid(this.dataViewUuid);
            vectorDialog.show();
            break;
          case RANGE:
//             IntervalDefinitionDialog intervalDialog =
//                new IntervalDefinitionDialog(parent, dataModelDef, operator, filterExpression.getCrossColumns());
//
//             intervalDialog.setDataType(bundle.getReturnType(fieldDef));
//             intervalDialog.setRecord(record);
//             intervalDialog.setUpdater(updater);
//             intervalDialog.setCurrentValueDefinition(value);
//             intervalDialog.show();
             break;
      }
   }

   @Override
   public void onBrowserEvent(Context context, Element parentIn, ValueDefinition value, NativeEvent event,
                              ValueUpdater<ValueDefinition> updater) {
      super.onBrowserEvent(context, parentIn, value, event, updater);

      String eventType = event.getType();

      if (BrowserEvents.CLICK.equals(eventType)) {
         FilterExpression filterExpression = getFilterExpression(context);
         Store<FilterExpression>.Record record = gridStore.getRecord(filterExpression);
         BundleFunction bundle = record.getValue(propertyAccess.bundleFunction());
         FieldDef fieldDef = record.getValue(propertyAccess.field());
         RelationalOperator operator = record.getValue(propertyAccess.operator());
         OperandCardinality cardinality = (operator == null) ? OperandCardinality.SCALAR : operator.getCardinality();

         switch (cardinality) {
            case NONE:
               updater.update(new NullValueDefinition());
               break;
            case SCALAR:
               ScalarValueDefinitionDialog scalarDialog =
                  new ScalarValueDefinitionDialog(parent, dataModelDef, operator, filterExpression.getCrossColumns());

               scalarDialog.setDataType(bundle.getReturnType(fieldDef));
               scalarDialog.setRecord(record);
               scalarDialog.setUpdater(updater);
               scalarDialog.setCurrentValueDefinition(value);
               scalarDialog.show();
               break;
            case VECTOR:
               VectorValueDefinitionDialog vectorDialog =
                  new VectorValueDefinitionDialog(parent, filterExpression.getCrossColumns());

               vectorDialog.setDataType(bundle.getReturnType(fieldDef));
               vectorDialog.setRecord(record);
               vectorDialog.setUpdater(updater);
               vectorDialog.setCurrentValueDefinition(value);
               vectorDialog.setDataViewUuid(this.dataViewUuid);
               vectorDialog.show();
               break;
            case RANGE:
//               IntervalDefinitionDialog intervalDialog =
//                  new IntervalDefinitionDialog(parent, dataModelDef, operator, filterExpression.getCrossColumns());
//
//               intervalDialog.setDataType(bundle.getReturnType(fieldDef));
//               intervalDialog.setRecord(record);
//               intervalDialog.setUpdater(updater);
//               intervalDialog.setCurrentValueDefinition(value);
//               intervalDialog.show();
               break;
         }
      }
   }

   @Override
   public void render(Context contextIn, ValueDefinition valueIn, SafeHtmlBuilder builderIn) {
      FilterExpression filterExpression = getFilterExpression(contextIn);
      Store<FilterExpression>.Record record = gridStore.getRecord(filterExpression);
      boolean isSelectionFilter = record.getValue(propertyAccess.isSelectionFilter());

      if (!isSelectionFilter) {
         RelationalOperator operator = record.getValue(ValueDefinitionCell.this.propertyAccess.operator());

         if (operator == null) {
            builderIn.append(displayTemplate.displayNull());
         } else {
            ValueDefinition valueDef = valueIn;

            if (RelationalOperator.LIKE.equals(operator) && (valueDef instanceof ScalarValueDefinition)) {
               valueDef = valueDef.cloneThis();
//TODO
               ((ScalarValueDefinition) valueDef).setValue(
                  new OperandTypeAndValue(FilterOperandType.STATIC,
                                          StringUtil.patternFromSql((String) ((ScalarValueDefinition) valueDef).getValue().getValue())));
            }
            super.render(contextIn, valueDef, builderIn);
         }
//            Display.success("Render String", builderIn.toSafeHtml().asString());
      }
   }
}
