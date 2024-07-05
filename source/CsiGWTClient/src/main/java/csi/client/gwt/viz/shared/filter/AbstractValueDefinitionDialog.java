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

import com.google.gwt.cell.client.ValueUpdater;
import com.sencha.gxt.data.shared.Store;

import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.KnowsParent;
import csi.client.gwt.widget.boot.WatchingParent;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.filter.FilterExpression;
import csi.server.common.model.filter.ValueDefinition;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractValueDefinitionDialog extends WatchingParent implements KnowsParent {
   private ValueUpdater<ValueDefinition> updater;
   private Store<FilterExpression>.Record record;
   private CsiDataType dataType;
   private ValueDefinition currentValueDefinition;

   protected Dialog dialog;
   protected abstract Dialog getDialog();

   public AbstractValueDefinitionDialog(CanBeShownParent parentIn) {
      super(parentIn);

      dialog = getDialog();

      dialog.hideOnCancel();
   }

   public ValueUpdater<ValueDefinition> getUpdater() {
      return updater;
   }

   public Store<FilterExpression>.Record getRecord() {
      return record;
   }

   public CsiDataType getDataType() {
      return dataType;
   }

   public ValueDefinition getCurrentValueDefinition() {
      return currentValueDefinition;
   }

   public void setUpdater(ValueUpdater<ValueDefinition> updater) {
      this.updater = updater;
   }

   public void setRecord(Store<FilterExpression>.Record record) {
      this.record = record;
   }

   public void setDataType(CsiDataType dataType) {
      this.dataType = dataType;
   }

   public void setCurrentValueDefinition(ValueDefinition currentValueDefinition) {
      this.currentValueDefinition = currentValueDefinition;
   }

   @Override
   public void show() {
      CanBeShownParent parent = getParent();

      if (parent != null) {
         parent.hide();
      }
      dialog.show();
   }

   @Override
   public void hide() {
      dialog.hide();
   }

   @Override
   public void destroy() {
      CanBeShownParent parent = getParent();

      if (parent != null) {
         parent.show();
      }
      dialog.hide();
   }
}
