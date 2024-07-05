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

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Brand;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DrillHeader extends AbstractPromotionCallbackProvidingDrillElement {

    private List<String> promotionDrillSelections;

    @UiField
    Brand heading;

    interface SpecificUiBinder extends UiBinder<Widget, DrillHeader> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public DrillHeader() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setText(List<String> drillSelections, String categoryHeader, String categoryValue) {
        heading.setText(categoryHeader + ": " + categoryValue);
        promotionDrillSelections = new ArrayList<String>(drillSelections);
        promotionDrillSelections.add(categoryValue);
    }

    @UiHandler("promoteLink")
    void handleClick(ClickEvent e) {
        getChartPromotionCallback().promote(promotionDrillSelections);
    }
}
