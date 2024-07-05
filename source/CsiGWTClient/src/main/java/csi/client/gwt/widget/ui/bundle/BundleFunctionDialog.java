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
package csi.client.gwt.widget.ui.bundle;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.Controls;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.Form;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.sencha.gxt.widget.core.client.form.DoubleField;
import com.sencha.gxt.widget.core.client.form.IntegerField;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.NotificationPopup;
import csi.client.gwt.widget.combo_boxes.BundleFunctionComboBox;
import csi.client.gwt.widget.misc.WidgetCallback;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.visualization.BundleFunctionParameter;
import csi.server.util.sql.api.BundleFunction;
import csi.server.util.sql.api.BundleParameterInfo;
import csi.server.util.sql.api.BundleParameterInfo.BundleParameterName;
import csi.server.util.sql.api.HasBundleFunction;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class BundleFunctionDialog {

    private Dialog dialog;
    private BundleFunctionEditCell<? extends HasBundleFunction> cell;
    private List<ValueBaseField<?>> paramValueFields = new ArrayList<ValueBaseField<?>>();
    private List<String> currentParamValues = new ArrayList<String>();
    private BundleFunction currentBundleFunction;
    private List<FluidRow> fluidRows = new ArrayList<FluidRow>();

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    @UiField
    BundleFunctionComboBox bundleFunctionListBox;
    @UiField
    Form form;
    private WidgetCallback callback;

    interface SpecificUiBinder extends UiBinder<Dialog, BundleFunctionDialog> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public BundleFunctionDialog() {
        dialog = uiBinder.createAndBindUi(this);
        dialog.hideOnCancel();
    }

    public void setCell(BundleFunctionEditCell<? extends HasBundleFunction> cell) {
        this.cell = cell;
    }

    public void show(final BundleFunction value, final List<BundleFunctionParameter> paramValues) {
        currentParamValues = new ArrayList<String>();
        for (BundleFunctionParameter bfp : paramValues){
            currentParamValues.add(bfp.getFunctionParameter());
        }

        currentBundleFunction = value;
        bundleFunctionListBox.clear();
        for (FluidRow fr : fluidRows) {
            fr.removeFromParent();
        }

        HasBundleFunction hbf = cell.getGridStore().findModelWithKey(cell.getCurrentContext().getKey().toString());
        CsiDataType dataType = hbf.getDataTypeForBundleFunction();
        bundleFunctionListBox.getStore().clear();
        bundleFunctionListBox.getStore().add(BundleFunction.NONE);

        for (BundleFunction bf : BundleFunction.forType(dataType)) {
            if(bf != null && bf != BundleFunction.NONE)
                bundleFunctionListBox.getStore().add(bf);
        }
        if (currentBundleFunction.isApplicableFor(dataType)) {
            bundleFunctionListBox.setValue(currentBundleFunction, false);
            bundleFunctionChanged();
        }

        bundleFunctionListBox.addSelectionHandler(new SelectionHandler() {
			@Override
			public void onSelection(SelectionEvent event) {
				bundleFunctionChanged();
			}
		});
        
        dialog.getActionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                onSave(event);
            }
        });

        dialog.show();
    }

    protected void onSave(ClickEvent event) {
        boolean paramsValid = true;
        for (ValueBaseField<?> field : paramValueFields) {
            if (!field.isValid()) {
                paramsValid = false;
            }
        }

        if (bundleFunctionListBox.getValue() == null) {
            paramsValid = false;
        }

        if (paramsValid) {
            callback.action();
        } else {
            NotificationPopup popup = new NotificationPopup(i18n.bundleFunctionDialogInvalidPopUpTitle(), i18n.bundleFunctionDialogInvalidPopUpMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            popup.show();
        }
    }

    @SuppressWarnings("unchecked")
    protected void bundleFunctionChanged() {
        for (FluidRow fr : fluidRows) {
            fr.removeFromParent();
        }
        paramValueFields.clear();
        
        int columnIndex = bundleFunctionListBox.getSelectedIndex();
        BundleFunction bf = bundleFunctionListBox.getStore().get(columnIndex);
        for (BundleParameterInfo bpi : bf.getParameterInfo()) {
            FluidRow row = new FluidRow();
            fluidRows.add(row);
            ControlGroup cg = new ControlGroup();
            row.add(cg);
            ControlLabel cl = new ControlLabel(internationalize(bpi.getName()));
            cg.add(cl);
            Controls controls = new Controls();
            cg.add(controls);

            ValueBaseField<?> field = null;
            switch (bpi.getDataType()) {
                case Integer:
                    field = new IntegerField();
                    switch (bf) {
                        case ROUND:
                            break;
                        default:
                            ((IntegerField) field).setAllowNegative(false);
                            break;
                    }
                    break;
                case Number:
                    field = new DoubleField();
                    break;
                case String:
                    field = new TextField();
                    break;
                default:
                    throw new RuntimeException(i18n.bundleFunctionDialogUnknownTypeException() + bpi.getDataType()); //$NON-NLS-1$
            }
            field.setAllowBlank(false);
            String id = DOM.createUniqueId();
            field.setId(id);
            cl.setFor(id);
            controls.add(field);
            paramValueFields.add(field);
            form.add(row);
        }
        if (bf == currentBundleFunction && currentParamValues.size() > 0) {
            for (int i = 0; i < paramValueFields.size(); i++) {
                ValueBaseField<Object> field = (ValueBaseField<Object>) paramValueFields.get(i);
                String paramValue = currentParamValues.get(i);
                switch (bf.getParameterInfo().get(i).getDataType()) {
                    case Integer:
                        field.setValue(Integer.parseInt(paramValue));
                        break;
                    case Number:
                        field.setValue(Double.parseDouble(paramValue));
                        break;
                    case String:
                        field.setValue(paramValue);
                        break;
                    default:
                        throw new RuntimeException(i18n.bundleFunctionDialogUnknownTypeException() //$NON-NLS-1$
                                + bf.getParameterInfo().get(i).getDataType());
                }
            }
            currentParamValues.clear();
        }
    }

    private String internationalize(BundleParameterName item) {
		String label = item.toString();
		switch(item){
		case DELIMITER: label = i18n.delimiter(); break;
		case DIVISOR: label = i18n.divisor(); break;
		case FLAGS: label = i18n.flags(); break;
		case INDEX: label = i18n.index(); break;
		case LENGTH: label = i18n.length(); break;
		case REGEX: label = i18n.regex(); break;
		case REGEX_REPLACE: label = i18n.regexReplace(); break;
		case REPLACEMENT: label = i18n.replacement(); break;
		case START: label = i18n.start(); break;
		case TRIM: label = i18n.trim(); break;
        case DECIMAL_PLACES: label = i18n.decimalPlace(); break;
		}
		return label;
	}

	public void hide() {
        dialog.hide();
    }

    public List<BundleFunctionParameter> getParameterValues() {
        List<BundleFunctionParameter> list = new ArrayList<BundleFunctionParameter>();
        for (ValueBaseField<?> field : paramValueFields) {
            BundleFunctionParameter bundleFunctionParameter = new BundleFunctionParameter();
            bundleFunctionParameter.setFunctionParameter(field.getValue().toString());
            list.add(bundleFunctionParameter);
        }
        return list;
    }

    public BundleFunction getSelectedBundleFunction() {
        return bundleFunctionListBox.getValue();
    }

    public void setCallback(WidgetCallback widgetCallback) {
        this.callback = widgetCallback;

    }
}
