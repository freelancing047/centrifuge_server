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
package csi.client.gwt.widget.ui.color;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import csi.client.gwt.WebMain;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.combo_boxes.StringComboBox;
import csi.server.common.service.api.ColorActionsServiceProtocol;
import csi.shared.core.color.BrewerColorSet;
import csi.shared.core.color.DiscreteColorModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DiscreteColorPicker extends AbstractColorPicker {

    private static Map<String, Map<Integer, List<BrewerColorSet>>> brewerColors;
    private DiscreteColorRange selectedRange;

    @UiField
    DiscreteColorTypeComboBox categoryList;
    @UiField
    StringComboBox sizeList;

    @UiField
    FlowPanel colorRangeContainer;

    interface SpecificUiBinder extends UiBinder<Dialog, DiscreteColorPicker> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public DiscreteColorPicker() {
        super();
        init(uiBinder.createAndBindUi(this));
    }

    @Override
    public void show() {
        if (brewerColors != null) {
            setup();
            super.show();
        } else {
            WebMain.injector.getVortex().execute(new Callback<Map<String, Map<Integer, List<BrewerColorSet>>>>() {

                @Override
                public void onSuccess(Map<String, Map<Integer, List<BrewerColorSet>>> result) {
                    brewerColors = result;
                    setup();
                    DiscreteColorPicker.super.show();
                }
            }, ColorActionsServiceProtocol.class).getBrewerColors();
        }
    }

    protected void setup() {
        List<String> list = new ArrayList<String>();
        for (String category : brewerColors.keySet()) {
            list.add(category);
        }
        Collections.sort(list);
        categoryList.getStore().clear();
        for (String category : list) {
            categoryList.getStore().add(DiscreteColorType.valueOf(category.toUpperCase()));
        }
        categoryList.addSelectionHandler(new SelectionHandler<DiscreteColorType>() {

            @Override
            public void onSelection(SelectionEvent<DiscreteColorType> event) {
                categoryList.setValue(event.getSelectedItem(), false);
                updateSizeList();
            }
        });
        updateSizeList();
        setupCurrentSelection();
    }

    private void setupCurrentSelection() {
        categoryList.setValue(DiscreteColorType.valueOf(this.<DiscreteColorModel> getColorModel().getCategory().toUpperCase()));
        sizeList.setValue(Integer.toString(this.<DiscreteColorModel> getColorModel().getColors().size()));
        updateAvailableColors();
    }

    protected void updateSizeList() {
        DiscreteColorType type = categoryList.getValue();
        String category = "";
        if(type == null){
            category = categoryList.getStore().get(0).getType();
            categoryList.setValue(DiscreteColorType.valueOf(category.toUpperCase()), false);
        } else {
            category = categoryList.getValue().getType();
        }
        List<Integer> list = new ArrayList<Integer>();
        for (Integer size : brewerColors.get(category).keySet()) {
            list.add(size);
        }
        Collections.sort(list);
        String last = sizeList.getValue();
        sizeList.clear();
        sizeList.getStore().clear();
        for (Integer size : list) {
                sizeList.getStore().add(size.toString());
            }
            if(last != null) {
                if (sizeList.getStore().hasRecord(last)) {
                    sizeList.setValue(last, false);
                }else {
                    sizeList.setValue(sizeList.getStore().get(0));
                }
            }
        sizeList.addSelectionHandler(new SelectionHandler<String>() {

            @Override
            public void onSelection(SelectionEvent<String> event) {
                sizeList.setValue(event.getSelectedItem(), false);
                updateAvailableColors();
            }
        });
        updateAvailableColors();
    }

    protected void updateAvailableColors() {
        colorRangeContainer.clear();
        String category = categoryList.getValue().getType();
        
        String sizeString = sizeList.getValue();
        Integer size;
        if(sizeString == null){
            size = this.<DiscreteColorModel> getColorModel().getColors().size();
        } else {
            size = Integer.parseInt(sizeList.getValue());
        }
        for (BrewerColorSet set : brewerColors.get(category).get(size)) {
            final DiscreteColorRange range = new DiscreteColorRange();
            range.setColorSet(set);
            if (range.getColorSet().getColors().equals(this.<DiscreteColorModel> getColorModel().getColors())) {
                setSelection(range);
            } else if (range.equals(selectedRange)) {
                range.setSelected(true);
            }
            colorRangeContainer.add(range);
            range.getContainerWidget().addDomHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    setSelection(range);
                }
            }, ClickEvent.getType());
        }
    }

    private void setSelection(DiscreteColorRange range) {
        range.setSelected(true);
        if (selectedRange != null) {
            selectedRange.setSelected(false);
        }
        selectedRange = range;
        DiscreteColorModel model = new DiscreteColorModel();
        model.setCategory(categoryList.getValue().getType());
        model.setColors(range.getColorSet().getColors());
        setColorModel(model);
    }
}
