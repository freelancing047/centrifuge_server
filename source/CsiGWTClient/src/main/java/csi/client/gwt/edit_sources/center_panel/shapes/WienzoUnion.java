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
package csi.client.gwt.edit_sources.center_panel.shapes;

import com.emitrom.lienzo.client.core.shape.Rectangle;

import csi.client.gwt.edit_sources.center_panel.ConfigurationPresenter;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.model.DataSetOp;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class WienzoUnion extends WienzoDualChildComposite {

    public WienzoUnion(DataSetOp dso, WienzoComposite top, WienzoComposite bottom,
                       ConfigurationPresenter configurationPresenter) {
        super(dso, top, bottom, dso.getName(), configurationPresenter,
                ColorValues.APPEND_FILL, ColorValues.WARNING_FILL, Dialog.txtWarningColor);

        updateInfo();

        getTitle().setFillColor(ColorValues.APPEND_TEXT);

        top.getCpBottom().setConnected(true);
        bottom.getCpTop().setConnected(true);
        
        init();
        addConnectors(false);
        addHandlers();
    }

    public void updateInfo() {

        DataSetOp myDso = getDso();

        if (null != myDso) {

            DataSetOp myLeftChild = myDso.getLeftChild();
            DataSetOp myRightChild = myDso.getRightChild();
            String myLeftChildName = (null != myLeftChild) ? myLeftChild.getName() : "? ? ?";
            String myRightChildName = (null != myRightChild) ? myRightChild.getName() : "? ? ?";

            infoString = myDso.getAppendAll()
                            ? i18n.unionType_All_InfoString(myLeftChildName, myRightChildName)
                            : i18n.unionType_Unique_InfoString(myLeftChildName, myRightChildName);

            needsAttention = !getDso().hasMapItems();
            messageString = needsAttention ? i18n.dataSourceEditor_NoAppendMapping() : null;

            myDso.updateName();
            replaceTitle(myDso.getName());

        } else {

            needsAttention = true;
            infoString = "\"? ? ?\" UNION \"? ? ?\"";
        }
    }

    public void launchEditDisplay() {

        getConfigurationPresenter().showAppendEditor(this);
    }

    protected double placeChildren(double deltaX, double deltaY) {

        double myOverlap = (0.0 == deltaY) ? (ConnectionPoint.CIRCLE_RADIUS * 2.0) : 0.0;
        double myBaseWidth = Math.max(getTop().getWidth(), getBottom().getWidth()) - deltaX;

        getTop().setX(((myBaseWidth - getTop().getWidth()) / 2.0) + DIM_OFFSET);
        getTop().setY((DIM_TITLE_OFFSET * 2.0) + getTitle().getFontSize() - deltaY);

        getBottom().setX(((myBaseWidth - getBottom().getWidth()) / 2.0) + DIM_OFFSET);
        getBottom().setY(getTop().getY() + getTop().getHeight() - myOverlap - deltaY);

        return getBottom().getY() + getBottom().getHeight() + DIM_OFFSET - (deltaY / 2.0);
    }

    private void init() {

        double myDelta = (displayConnectors) ? 0.0 : ConnectionPoint.getLength();
        double myBaseWidth = Math.max(getTop().getWidth(), getBottom().getWidth()) - myDelta;
        double myWidth = myBaseWidth + (DIM_OFFSET * 2.0);
        double myHeight = (displayConnectors) ? placeChildren(0.0, 0.0) : placeChildren(myDelta, myDelta);
        double myCircleX = (myBaseWidth / 2.0) + DIM_OFFSET;
        double myCircleY = ((getTop().getY() + getTop().getHeight() + getBottom().getY()) / 2.0);

        borderRectangle = new Rectangle(myWidth, myHeight, 4.0);
        borderRectangle.setStrokeColor(ColorValues.APPEND_STROKE);
        borderRectangle.setFillColor(backgroundFill);

        connectionCircle.setX(myCircleX);
        connectionCircle.setY(myCircleY);

        getCoreGroup().add(borderRectangle);
        getCoreGroup().addManagedChild(getTop());
        getCoreGroup().addManagedChild(getBottom());
        getCoreGroup().add(connectionCircle);
        getCoreGroup().add(getTitle());

        getCoreGroup().setBoundingWidth(borderRectangle.getWidth());
        getCoreGroup().setBoundingHeight(borderRectangle.getHeight());
    }

    protected WienzoComposite getTop() {
        return getChild1();
    }

    protected WienzoComposite getBottom() {
        return getChild2();
    }
}
