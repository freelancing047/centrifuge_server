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
package csi.client.gwt.viz.chart.drill;

import java.util.List;

import com.github.gwtbootstrap.client.ui.Paragraph;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DrillError extends AbstractPromotionCallbackProvidingDrillElement {

    @UiField
    DrillHeader header;
    @UiField
    Paragraph errorParagraph;

    interface SpecificUiBinder extends UiBinder<Widget, DrillError> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public DrillError() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void display(List<String> drillSelections, String categoryName, String categoryValue, long actual) {
        header.setText(drillSelections, categoryName, categoryValue);
        header.setChartPromotionCallback(getChartPromotionCallback());
        errorParagraph.setText(CentrifugeConstantsLocator.get().drillError_message(actual));
    }

}
