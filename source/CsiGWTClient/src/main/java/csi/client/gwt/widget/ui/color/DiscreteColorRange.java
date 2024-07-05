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
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

import csi.shared.core.color.BrewerColorSet;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DiscreteColorRange extends Composite {

    private static final int BLOCK_HEIGHT = 20;
    private static final int BLOCK_MARGIN = 10;

    private BrewerColorSet colorSet;
    private boolean selected;

    @UiField
    FlowPanel range;
    @UiField
    InlineLabel colorLabel;
    @UiField
    AbsolutePanel rangeContainer;

    interface SpecificUiBinder extends UiBinder<Widget, DiscreteColorRange> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public DiscreteColorRange() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public Widget getContainerWidget() {
        return range;
    }

    public BrewerColorSet getColorSet() {
        return colorSet;
    }

    public void setColorSet(BrewerColorSet colorSet) {
        this.colorSet = colorSet;
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        colorLabel.setText(getColorSet().getName());
        // Assuming 450px width.
        int width = (450 - getColorSet().getColors().size() * BLOCK_MARGIN) / getColorSet().getColors().size();
        if (width > 50) {
            width = 50;
        }
        int margin = (450 - getColorSet().getColors().size() * (BLOCK_MARGIN + width)) / 2;
        int index = 0;
        for (String color : getColorSet().getColors()) {
            FlowPanel block = new FlowPanel();

            block.setWidth(width + "px"); //$NON-NLS-1$
            block.setHeight(BLOCK_HEIGHT + "px"); //$NON-NLS-1$
            block.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
            String c = color.startsWith("#") ? color : "#" + color; //$NON-NLS-1$ //$NON-NLS-2$
            block.getElement().getStyle().setBackgroundColor(c);

            block.getElement().getStyle().setProperty("borderRadius", "5px"); //$NON-NLS-1$ //$NON-NLS-2$
            block.getElement().getStyle().setProperty("boxShadow", "3px 3px 5px #999"); //$NON-NLS-1$ //$NON-NLS-2$
            rangeContainer.add(block, index * (width + BLOCK_MARGIN) + margin, 5);
            index++;
        }
    }

    private void setSelectionStyle() {
        getWidget().getElement().getStyle().setBackgroundColor(selected ? "#FFFFBF" : "white"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        setSelectionStyle();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof DiscreteColorRange == false) {
            return false;
        } else {
            DiscreteColorRange typed = (DiscreteColorRange) obj;
            return this.getColorSet().equals(typed.getColorSet());
        }
    }

}
