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
import csi.server.common.util.Format;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class WienzoJoin extends WienzoDualChildComposite {

    public WienzoJoin(DataSetOp dso, WienzoComposite left, WienzoComposite right,
            ConfigurationPresenter configurationPresenter) {
        super(dso, left, right, dso.getName(), configurationPresenter,
                ColorValues.JOIN_FILL, ColorValues.ERROR_FILL, Dialog.txtErrorColor);

        updateInfo();

        getTitle().setFillColor(ColorValues.JOIN_TEXT);

        left.getCpRight().setConnected(true);
        right.getCpLeft().setConnected(true);
        
        init();
        addConnectors(false);
        addHandlers();
    }

    public void launchEditDisplay() {

        getConfigurationPresenter().showJoinEditor(this);
    }

    public void updateInfo() {

        DataSetOp myDso = getDso();

        if (null != myDso) {

            DataSetOp myLeftChild = myDso.getLeftChild();
            DataSetOp myRightChild = myDso.getRightChild();
            String myLeftChildName = (null != myLeftChild) ? myLeftChild.getName() : "? ? ?";
            String myRightChildName = (null != myRightChild) ? myRightChild.getName() : "? ? ?";
            String myFunction = myDso.getJoinType().getSql();

            infoString = Format.value(myLeftChildName) + "  " + myFunction + "  " + Format.value(myRightChildName);

            needsAttention = !getDso().hasMapItems();
            messageString = needsAttention ? i18n.dataSourceEditor_NoJoinMapping() : null;

            myDso.updateName();
            replaceTitle(myDso.getName());

        } else {

            needsAttention = true;
            infoString = "\"? ? ?\" JOIN \"? ? ?\"";
        }
    }

    protected double placeChildren(double deltaX, double deltaY) {

        double myOverlap = (0.0 == deltaX) ? (ConnectionPoint.CIRCLE_RADIUS * 2.0) : 0.0;
        double myBaseHeight = Math.max(getLeft().getHeight(), getRight().getHeight());
        double myTitleHeight = (DIM_TITLE_OFFSET * 2) + getTitle().getFontSize() - deltaY;

        getLeft().setX(DIM_OFFSET - (deltaX / 2.0));
        getLeft().setY(((myBaseHeight - getLeft().getHeight()) / 2.0) + myTitleHeight);

        getRight().setX(getLeft().getX() + getLeft().getWidth() - myOverlap - deltaX);
        getRight().setY(((myBaseHeight - getRight().getHeight()) / 2.0) + myTitleHeight);

        return getRight().getX() + getRight().getWidth() + DIM_OFFSET - (deltaX / 2.0);
    }

    private void init() {

        double myDelta = (displayConnectors) ? 0.0 : ConnectionPoint.getLength();
        double myDeltaX = (0.0 != myDelta) ? myDelta / 2.0 : ConnectionPoint.CIRCLE_RADIUS;
        double myTitleHeight = (DIM_TITLE_OFFSET * 2.0) + getTitle().getFontSize() - myDelta;
        double myBaseHeight = Math.max(getLeft().getHeight(), getRight().getHeight());

        double myHeight = myTitleHeight + myBaseHeight + DIM_OFFSET - (myDelta / 2.0);
        double myWidth = (displayConnectors) ? placeChildren(0.0, 0.0) : placeChildren(myDelta, myDelta);

        double myCircleY = myTitleHeight + (myBaseHeight / 2);
        double myCircleX = getLeft().getX() + getLeft().getWidth() - myDeltaX;

        borderRectangle = new Rectangle(myWidth, myHeight, 4.0);
        borderRectangle.setStrokeColor(ColorValues.JOIN_STROKE);
        borderRectangle.setFillColor(backgroundFill);

        connectionCircle.setX(myCircleX);
        connectionCircle.setY(myCircleY);

        getCoreGroup().add(borderRectangle);
        getCoreGroup().addManagedChild(getLeft());
        getCoreGroup().addManagedChild(getRight());
        getCoreGroup().add(connectionCircle);
        getCoreGroup().add(getTitle());

        getCoreGroup().setBoundingWidth(borderRectangle.getWidth());
        getCoreGroup().setBoundingHeight(borderRectangle.getHeight());
    }

    protected WienzoComposite getLeft() {
        return getChild1();
    }

    protected WienzoComposite getRight() {
        return getChild2();
    }
}
