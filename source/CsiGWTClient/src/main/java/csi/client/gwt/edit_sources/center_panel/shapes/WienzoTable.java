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

import com.emitrom.lienzo.client.core.image.PictureLoader;
import com.emitrom.lienzo.client.core.shape.Rectangle;
import com.emitrom.lienzo.client.core.shape.Text;
import com.emitrom.lienzo.shared.core.types.TextAlign;
import com.emitrom.lienzo.shared.core.types.TextBaseLine;

import csi.client.gwt.edit_sources.center_panel.ConfigurationPresenter;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.enumerations.JdbcDriverType;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.util.Format;

/**
 * Note: Wienzo = Lienzo graphics widget. Just a unique name to namespace-separate the elements that make up the 
 * dataview configuration.
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public class WienzoTable extends WienzoComposite {

    public WienzoTable(DataSetOp dso, ConfigurationPresenter configurationPresenter) {
        super(dso, configurationPresenter, ColorValues.TABLE_FILL, ColorValues.ERROR_FILL, Dialog.txtErrorColor);
        updateInfo();
        migrateDsoName();
        init();
        addConnectors(true);
        addHandlers();
    }

    public void launchEditDisplay() {

        getConfigurationPresenter().showTableEditor(this);
    }

    public void updateInfo() {

        DataSetOp myDso = getDso();

        if (null != myDso) {

            SqlTableDef myTable = (null != myDso) ? myDso.getTableDef() : null;
            DataSourceDef mySource = (null != myTable) ? myTable.getSource() : null;
            String myDsoName = (null != myDso) ? myDso.getName() : "? ? ?";
            String mySourceName = (null != mySource) ? mySource.getName() : "? ? ?";
            boolean myTableOK = (null != myTable) ? myTable.hasSelectedColumns() : false;

            infoString = Format.value(myDsoName) + i18n.wienzoTabletooltipText() + Format.value(mySourceName);

            needsAttention = !myTableOK;
            messageString = needsAttention ? i18n.dataSourceEditor_NoSelectedFields() : null;

            myDso.updateName();

        } else {

            needsAttention = true;
            infoString = Format.value("? ? ?") + i18n.wienzoTabletooltipText() + Format.value("? ? ?");
        }
    }

    private void migrateDsoName() {

        String myDsoName =null;
        JdbcDriverType myDsoType = null;
        DataSetOp myDSO = getDso();

        if (null != myDSO) {

            SqlTableDef myTable = myDSO.getTableDef();

            if (null != myTable) {

                DataSourceDef mySource = myTable.getSource();

                if (null != mySource) {

                    ConnectionDef myConnection = mySource.getConnection();

                    if (null !=myConnection) {

                        myTable.setDsoType(JdbcDriverType.extractValue(myConnection.getType()));
                    }
                }
                myTable.setDsoName(myDSO.getName());
            }
        }
    }

    private void init() {
        borderRectangle = new Rectangle(TABLE_SPACING * 4 + DIM_ICON + LABEL_BOX_WIDTH, LABEL_BOX_HEIGHT + 2 * TABLE_SPACING,
                4.0);
        borderRectangle.setStrokeColor(ColorValues.TABLE_STROKE);
        borderRectangle.setFillColor(backgroundFill);
        getCoreGroup().add(borderRectangle);

        {
            // icon
            TableIcon icon = new TableIcon(getDso().getTableDef());
            icon.setX(TABLE_SPACING);
            icon.setY((borderRectangle.getHeight() - DIM_ICON) / 2.0);
            getCoreGroup().add(icon);
            PictureLoader.getInstance().registerCallback(null, new Runnable() {
                
                @Override
                public void run() {
                    getScene().draw();
                }
            });
        }

        connectionCircle.setStrokeColor(ColorValues.TABLE_FILL);
        connectionCircle.setX(borderRectangle.getWidth() - (ConnectionPoint.CIRCLE_RADIUS * 2));
        connectionCircle.setY(TABLE_SPACING * 2);
        getCoreGroup().add(connectionCircle);

        {
            // name
            title = new Text(buildDsoLabel(getDso()));
            title.setFontSize(8);
            title.setTextAlign(TextAlign.LEFT);
            title.setTextBaseLine(TextBaseLine.MIDDLE);
            title.setX(TABLE_SPACING + DIM_ICON + TABLE_SPACING);
            title.setY(TABLE_SPACING + (LABEL_BOX_HEIGHT / 4.0));
            title.setFillColor(ColorValues.TABLE_TEXT);

            // source
            Text mySource = new Text(buildSourceLabel(getDso()));
            mySource.setFontSize(8);
            mySource.setTextAlign(TextAlign.LEFT);
            mySource.setTextBaseLine(TextBaseLine.MIDDLE);
            mySource.setX(TABLE_SPACING + DIM_ICON + TABLE_SPACING);
            mySource.setY(TABLE_SPACING + (3 * (LABEL_BOX_HEIGHT / 4.0)));
            mySource.setFillColor(ColorValues.TABLE_TEXT);

            getCoreGroup().add(title);
            getCoreGroup().add(mySource);
        }

        getCoreGroup().setBoundingWidth(borderRectangle.getWidth());
        getCoreGroup().setBoundingHeight(borderRectangle.getHeight());

       
    } // end init
/*
    @Override
    public void onDragEnd(NodeDragEndEvent eventIn, boolean primary) {

        super.onDragEnd(eventIn, primary);

        if (primary && this.getWienzoParent() != null) {

            DragContext myContext = eventIn.getDragContext();

            if ((Math.abs(myContext.getDx()) > 40) || (Math.abs(myContext.getDy()) > 40)) {

                getWienzoParent().handleDetach(getDso());
                return;

            } else {

                this.setX(_lastX);
                this.setY(_lastY);
                draw();
            }
        }
    }

    @Override
    public void onDragEnd(NodeDragEndEvent eventIn, boolean primary) {

        if (primary && this.getWienzoParent() != null) {

            DragContext myContext = eventIn.getDragContext();

            if ((Math.abs(myContext.getDx()) > 40) || (Math.abs(myContext.getDy()) > 40)) {

                getWienzoParent().handleDetach(getDso());

            } else {

                this.setX(_lastX);
                this.setY(_lastY);
                draw();
            }
        }
        super.onDragEnd(eventIn, primary);
    }
*/
}